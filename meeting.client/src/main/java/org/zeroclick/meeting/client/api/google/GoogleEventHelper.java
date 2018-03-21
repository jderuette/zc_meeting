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
package org.zeroclick.meeting.client.api.google;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.ProviderDateHelper;
import org.zeroclick.meeting.client.api.event.AbstractEventHelper;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

/**
 * @author djer
 *
 */
public class GoogleEventHelper extends AbstractEventHelper<Event, EventDateTime> {

	private static final Logger LOG = LoggerFactory.getLogger(GoogleEventHelper.class);

	private ProviderDateHelper<EventDateTime> googleDateHelper;

	@Override
	protected ProviderDateHelper<EventDateTime> getDateHelper() {
		if (null == this.googleDateHelper) {
			this.googleDateHelper = new GoogleDateHelper();
		}
		return this.googleDateHelper;
	}

	@Override
	public List<Event> retrieveEvents(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId,
			final AbstractCalendarConfigurationTableRowData calendar) {
		final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

		ApiCalendar<Calendar, ApiCredential<Credential>> gCalendarService = null;

		try {
			gCalendarService = googleHelper.getCalendarService(calendar.getOAuthCredentialId());
		} catch (final UserAccessRequiredException uare) {
			LOG.error("Error while getting (Google) CalendarService for Api ID : " + calendar.getOAuthCredentialId(),
					uare);
			throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
		}

		final DateTime googledStartDate = this.toDateTime(startDate);
		final DateTime googledEndDate = this.toDateTime(endDate);

		final String calendarId = calendar.getExternalId();
		com.google.api.services.calendar.Calendar.Events.List eventQuery = null;

		if (null != gCalendarService) {
			try {
				eventQuery = gCalendarService.getCalendar().events().list(calendarId).setMaxResults(50)
						.setTimeMin(googledStartDate).setTimeMax(googledEndDate).setSingleEvents(true)
						.setOrderBy("startTime");
			} catch (final IOException e) {
				// DO nothing, query is null and null query is handled after
				eventQuery = null;
			}
		}

		final Events events = this.loadEvents(eventQuery, userId, calendar);
		return null == events ? null : events.getItems();
	}

	@Override
	public Boolean isNotRegiteredOn(final Event event, final String userEmail) {
		final String eventCreator = event.getCreator().getEmail();
		final List<EventAttendee> attendees = event.getAttendees();
		Boolean iAmRegistred = Boolean.FALSE;
		if (null != attendees && attendees.size() > 0) {
			for (final EventAttendee attendee : attendees) {
				if (userEmail.equalsIgnoreCase(attendee.getEmail())) {
					final String attendeeResponseStatus = attendee.getResponseStatus();
					if ("accepted".equals(attendeeResponseStatus)) {
						iAmRegistred = Boolean.TRUE;
					}
				}
			}
		}

		return !(userEmail.equalsIgnoreCase(eventCreator) || iAmRegistred);
	}

	@Override
	public Boolean isFree(final Event event) {
		return "transparent".equals(event.getTransparency());
	}

	@Override
	public Boolean isFullDay(final Event event) {
		return null != event.getStart().getDate();
	}

	private Events loadEvents(final com.google.api.services.calendar.Calendar.Events.List eventQuery, final Long userId,
			final AbstractCalendarConfigurationTableRowData calendar) {
		if (null == eventQuery) {
			LOG.warn("Trying to loadEvent with a null Query. Silently ignoring.");
			return null; // earlyBreak
		}

		final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
		Events events = null;
		try {
			events = eventQuery.execute();
		} catch (final GoogleJsonResponseException gjre) {
			if (gjre.getStatusCode() == 404) {
				LOG.warn("problem while geting user Event for user : " + userId
						+ " trying to auto-Configure is calendars");
				googleHelper.autoConfigureCalendars(userId);

				if (this.isMySelf(userId)) {
					final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
					notificationHelper.addProccessedNotification(
							"zc.meeting.calendar.notification.modifiedCalendarsConfig", TEXTS.get("zc.common.me"));
				}

				final ICalendarConfigurationService calendarConfigurationService = BEANS
						.get(ICalendarConfigurationService.class);

				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
				formData.getCalendarConfigurationId().setValue(calendar.getCalendarConfigurationId());

				final CalendarConfigurationFormData calendarAfterAutoConfigure = calendarConfigurationService
						.load(formData);

				if (LOG.isDebugEnabled() && null != calendarAfterAutoConfigure
						&& null == calendarAfterAutoConfigure.getCalendarConfigurationId().getValue()) {
					LOG.debug("Calendar was removed after synchro, continuing with the next one");
				} else {
					LOG.warn(
							"Calendar synchro do not removed calendar geenrating a GoogleJsonResponseException with code 404. Silently ignoring this calendar.");
				}
			}
		} catch (final IOException ioe) {
			LOG.error(new StringBuilder().append("Error while getting (Googlee) events for calendar ID : ")
					.append(calendar.getCalendarConfigurationId()).append(" for user ID :").append(userId)
					.append(" with query : ").append(eventQuery).toString(), ioe);
		}

		return events;
	}

	public Event getEvent(final EventIdentification eventIdentification, final Calendar googleCalendarService) {
		Event event = null;
		try {
			event = googleCalendarService.events()
					.get(eventIdentification.getCalendarId(), eventIdentification.getEventId()).execute();
		} catch (final IOException ioe) {
			LOG.warn(
					new StringBuilder().append("Cannot load (Google) event : ").append(eventIdentification.getEventId())
							.append(" in calendar : ").append(eventIdentification.getCalendarId()).toString());
		}
		return event;
	}

	@Override
	public String getHmlLink(final Event event) {
		String htmlLink = "unknow";

		if (null != event) {
			htmlLink = event.getHtmlLink();
		}
		return htmlLink;
	}

	@Override
	protected EventDateTime getEventStart(final Event event) {
		return event.getStart();
	}

	@Override
	protected EventDateTime getEventEnd(final Event event) {
		return event.getEnd();
	}

	@Override
	public void sort(final List<Event> events) {
		events.sort(new GoogleEventStartComparator());
	}

	@Override
	public ZonedDateTime fromEventDateTime(final EventDateTime date) {
		return this.getDateHelper().fromEventDateTime(date);
	}

	@Override
	public String asLog(final Event event, final Boolean maximumDetails) {
		if (null != event) {
			final StringBuilder builder = new StringBuilder(100);
			builder.append("Google Event : ").append(event.getId()).append(", start : ").append(event.getStart())
					.append(", end : ").append(event.getEnd()).append(", status : ").append(event.getStatus())
					.append(", transparancy : ").append(event.getTransparency()).append(", from recurent event : ")
					.append(event.getRecurringEventId()).append(", summary : ").append(event.getSummary());
			if (maximumDetails) {
				builder.append(". ICalUID : ").append(event.getICalUID()).append(", link : ")
						.append(event.getHtmlLink()).append(" with ").append(event.getAttendees().size())
						.append(" attendee(s) at location : ").append(event.getLocation());
			}
			return builder.toString();
		}
		return "";
	}

	public DateTime toDateTime(final ZonedDateTime dateTime) {
		return new DateTime(Date.from(dateTime.toInstant()), TimeZone.getTimeZone(dateTime.getZone()));
	}

	public Event create(final Event newEvent, final CalendarConfigurationFormData calendarToStoreEvent,
			final ApiCalendar<Calendar, ApiCredential<Credential>> googleCalendarService) {
		Event createdEvent = null;
		try {
			createdEvent = googleCalendarService.getCalendar().events()
					.insert(calendarToStoreEvent.getExternalId().getValue(), newEvent).execute();
			LOG.info(new StringBuilder(250).append("Event created ").append(this.asLog(createdEvent, Boolean.TRUE))
					.append(" in calendar : ").append(calendarToStoreEvent.getExternalId()).toString());
		} catch (final IOException ioe) {
			LOG.error("Error while creating (Google) event", ioe);
		} catch (final UserAccessRequiredException uare) {
			LOG.error("Erorr while creating (Google) Event", uare);
		}

		return createdEvent;
	}

	public Boolean update(final EventIdentification eventOrganizerIdentification,
			final ApiCalendar<Calendar, ApiCredential<Credential>> eventCalendarService, final Event organizerEvent) {
		Boolean eventUpdated = Boolean.FALSE;
		final Calendar gCalendarService = eventCalendarService.getCalendar();

		try {
			gCalendarService.events().update(eventOrganizerIdentification.getCalendarId(),
					eventOrganizerIdentification.getEventId(), organizerEvent).execute();
		} catch (final GoogleJsonResponseException gjre) {
			if (gjre.getStatusCode() == 404) {
				// wait a few and re-try
				LOG.warn(new StringBuilder().append("(Google) exception while accepting recently created event (id :")
						.append(eventOrganizerIdentification.getEventId()).append(") with apiConfigurationId : ")
						.append(eventCalendarService.getMetaData().getApiCredentialId()).append(", re-trying")
						.toString(), gjre);
				try {
					Thread.sleep(200);
				} catch (final InterruptedException ie) {
					LOG.error("Error while waiting to re-try updating event " + eventOrganizerIdentification, ie);
				}
				try {
					gCalendarService.events().update(eventOrganizerIdentification.getCalendarId(),
							eventOrganizerIdentification.getEventId(), organizerEvent).execute();
					eventUpdated = Boolean.TRUE;
				} catch (final GoogleJsonResponseException gjre2) {
					LOG.error(new StringBuilder().append("(Google) exception while accepting recently created event : ")
							.append(eventOrganizerIdentification).append(")  with apiConfigurationId : ")
							.append(eventCalendarService.getMetaData().getApiCredentialId()).append(", in second try")
							.toString(), gjre);
				} catch (final IOException ioe2) {
					LOG.error(
							new StringBuilder().append("(Google) exception while accepting recently created event  : ")
									.append(eventOrganizerIdentification).append(")  with apiConfigurationId : ")
									.append(eventCalendarService.getMetaData().getApiCredentialId())
									.append(", in second try").toString(),
							ioe2);
				}
			}
		} catch (final IOException ioe) {
			LOG.error(new StringBuilder().append("(Google) exception while accepting recently created event :")
					.append(eventOrganizerIdentification).append(")  with apiConfigurationId : ")
					.append(eventCalendarService.getMetaData().getApiCredentialId()).toString(), ioe);
		}
		return eventUpdated;
	}
}
