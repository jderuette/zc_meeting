/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.client.api.microsoft;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.ProviderDateHelper;
import org.zeroclick.meeting.client.api.event.AbstractEventHelper;
import org.zeroclick.meeting.client.api.microsoft.data.Attendee;
import org.zeroclick.meeting.client.api.microsoft.data.DateTimeTimeZone;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.service.CalendarService;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;

/**
 * @author djer
 *
 */
public class MicrosoftEventHelper extends AbstractEventHelper<Event, DateTimeTimeZone> {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftEventHelper.class);

	private ProviderDateHelper<DateTimeTimeZone> microsoftDateHelper;

	@Override
	protected ProviderDateHelper<DateTimeTimeZone> getDateHelper() {
		if (null == this.microsoftDateHelper) {
			this.microsoftDateHelper = new MicrosoftDateHelper();
		}
		return this.microsoftDateHelper;
	}

	@Override
	public String asLog(final Event event, final Boolean maximumDetails) {
		if (null != event) {
			String responseStatus = "notSet";
			if (null != event.getResponseStatus()) {
				responseStatus = event.getResponseStatus().getResponse();
			}
			final StringBuilder builder = new StringBuilder();
			builder.append("Microsoft event : ").append(event.getId()).append(", start : ").append(event.getStart())
					.append(", end : ").append(event.getEnd()).append(", status : ").append(responseStatus)
					.append(", showAs : ").append(event.getShowAs()).append(", from recurent event : ")
					.append(event.getRecurrence()).append(", summary : ").append(event.getSubject());
			if (maximumDetails) {
				builder.append(". ICalUID : ").append(event.getiCalUId()).append(", link : ").append(event.getWebLink())
						.append(" with ").append(event.getAttendees().size()).append(" attendee(s)  at location : ")
						.append(event.getLocation());
			}
			return builder.toString();
		}
		return "";
	}

	@Override
	public Event create(final Event newEvent, final CalendarConfigurationFormData calendarToStoreEvent) {
		final MicrosoftApiHelper apiHelper = BEANS.get(MicrosoftApiHelper.class);

		final ApiCalendar<CalendarService, ApiCredential<String>> microsoftCalendarService = apiHelper
				.getCalendarService(calendarToStoreEvent.getOAuthCredentialId().getValue());

		return this.create(newEvent, calendarToStoreEvent, microsoftCalendarService);
	}

	private Event create(final Event newEvent, final CalendarConfigurationFormData calendarToStoreEvent,
			final ApiCalendar<CalendarService, ApiCredential<String>> microsoftCalendarService) {
		Event createdEvent = null;

		try {
			createdEvent = microsoftCalendarService.getCalendar()
					.createEvent(calendarToStoreEvent.getExternalId().getValue(), newEvent).execute().body();

		} catch (final IOException ioe) {
			LOG.error("Error while creating (microsoft) Event", ioe);
		}

		return createdEvent;
	}

	@Override
	public List<Event> retrieveEvents(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId,
			final AbstractCalendarConfigurationTableRowData calendar) {
		final MicrosoftApiHelper microsoftHelper = BEANS.get(MicrosoftApiHelper.class);

		ApiCalendar<CalendarService, ApiCredential<String>> mCalendarService = null;

		try {
			mCalendarService = microsoftHelper.getCalendarService(calendar.getOAuthCredentialId());
		} catch (final UserAccessRequiredException uare) {
			LOG.error("Error while getting (Microsoft) CalendarService for Api ID : " + calendar.getOAuthCredentialId(),
					uare);
			throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
		}

		final String microsoftStartDate = this.toISO8601DateTime(startDate);
		final String microsoftEndDate = this.toISO8601DateTime(endDate);

		// Sort by start time in descending order
		final String sort = "start/dateTime DESC";
		// Only return the properties we care about
		final String properties = "organizer,subject,start,end,iCalUId,isAllDay,isOrganizer,showAs,webLink";
		// Return at most 10 events
		final Integer maxResults = 50;

		PagedResult<Event> eventsPage = null;
		final List<Event> events = new ArrayList<>();
		try {
			eventsPage = mCalendarService.getCalendar()
					.getEvents(microsoftStartDate, microsoftEndDate, sort, properties, maxResults).execute().body();
			events.addAll(CollectionUtility.arrayList(eventsPage.getValue()));

			// FIXME Djer13 handle multi page results
			if (null != eventsPage.getNextPageLink()) {
				LOG.warn("Some event are not analysed because on the next page ! url :" + eventsPage.getNextPageLink());
			}
		} catch (final IOException e) {
			LOG.error("Microsft API error while trying to retrieve Events", e);
		}

		return events;
	}

	private String toISO8601DateTime(final ZonedDateTime date) {
		return date.toOffsetDateTime().toString();
	}

	public Event getEvent(final EventIdentification eventIdentification, final CalendarService calendar) {
		Event event = null;
		try {
			event = calendar.getEvent(eventIdentification.getEventId()).execute().body();
		} catch (final IOException ioe) {
			LOG.error("Microsft API error while trying to retrieve Event with ID : " + eventIdentification.getEventId(),
					ioe);
		}

		return event;
	}

	@Override
	public Boolean isNotRegiteredOn(final Event event, final String userEmail) {
		Boolean iAmRegistred = Boolean.FALSE;
		final String eventCreator = event.getOrganizer().getEmailAddress().getAddress();
		final Attendee attendee = this.searchAttendee(event, userEmail);

		if (null != attendee) {
			final String attendeeResponseStatus = attendee.getStatus().getResponse();
			if ("Accepted".equals(attendeeResponseStatus)) {
				iAmRegistred = Boolean.TRUE;
			}
		}

		return !(userEmail.equalsIgnoreCase(eventCreator) || iAmRegistred);
	}

	@Override
	public Boolean isFree(final Event event) {
		Boolean isFree = Boolean.FALSE;
		final String showAs = event.getShowAs();
		if ("Free".equals(showAs)) {
			isFree = Boolean.TRUE;
		}
		return isFree;
	}

	@Override
	public Boolean isFullDay(final Event event) {
		return event.getIsAllDay();
	}

	private Attendee searchAttendee(final Event event, final String emailAdress) {
		Attendee foundAttende = null;
		final Collection<Attendee> attendees = event.getAttendees();

		if (null != attendees && attendees.size() > 0) {
			for (final Attendee attendee : attendees) {
				if (emailAdress.equalsIgnoreCase(attendee.getEmailAddress().getAddress())) {
					foundAttende = attendee;
					break;
				}
			}
		}
		return foundAttende;
	}

	@Override
	public String getHmlLink(final Event event) {
		String htmlLink = "unknow";

		if (null != event) {
			htmlLink = event.getWebLink();
		}
		return htmlLink;
	}

	@Override
	public void sort(final List<Event> events) {
		events.sort(new MicrosoftEventStartComparator());

	}

	@Override
	protected DateTimeTimeZone getEventStart(final Event event) {
		return event.getStart();
	}

	@Override
	protected DateTimeTimeZone getEventEnd(final Event event) {
		return event.getEnd();
	}

	@Override
	public DateTimeTimeZone toProviderDateTime(final ZonedDateTime date) {
		final DateTimeTimeZone providerDate = new DateTimeTimeZone();
		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		providerDate.setDateTime(dateHelper.toUserDate(date));
		providerDate.setTimeZone(date.getZone().getId());
		return providerDate;
	}

}
