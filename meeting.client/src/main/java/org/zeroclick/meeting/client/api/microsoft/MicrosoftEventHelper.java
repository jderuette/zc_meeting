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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.event.AbstractEventHelper;
import org.zeroclick.meeting.client.api.microsoft.data.DateTimeTimeZone;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.service.CalendarService;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;

/**
 * @author djer
 *
 */
public class MicrosoftEventHelper extends AbstractEventHelper<Event, DateTimeTimeZone> {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftEventHelper.class);

	@Override
	public String asLog(final Event event, final Boolean maximumDetails) {
		if (null != event) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Microsoft event : ").append(event.getId()).append(", start : ").append(event.getStart())
					.append(", end : ").append(event.getEnd()).append(", status").append("TO DEFINE")
					.append(", transparency : ").append("TIO DEFINE").append(", from recurent event : ")
					.append("TO DEFINE").append(", summary : ").append(event.getSubject());
			if (maximumDetails) {
				builder.append(". ICalUID : ").append("TO DEFINE").append(", link : ").append("TO DEFINE")
						.append(" with ").append("TP DEFINE").append(" attendee(s)").append(" at location : ")
						.append("TO DEFINE");
			}
			return builder.toString();
		}
		return "";
	}

	@Override
	public DateTimeTimeZone toDateTime(final ZonedDateTime dateTime, final ZoneId zoneId) {
		final DateTimeTimeZone mDate = new DateTimeTimeZone();
		final Date javaDate = Date.from(dateTime.toInstant());
		mDate.setDateTime(javaDate);
		mDate.setTimeZone(TimeZone.getTimeZone(zoneId).getDisplayName(Locale.ENGLISH));
		return mDate;
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

		// Sort by start time in descending order
		final String sort = "start/dateTime DESC";
		// Only return the properties we care about
		final String properties = "organizer,subject,start,end";
		// Return at most 10 events
		final Integer maxResults = 50;

		PagedResult<Event> eventsPage = null;
		final List<Event> events = new ArrayList<>();
		try {
			eventsPage = mCalendarService.getCalendar().getEvents(sort, properties, maxResults).execute().body();
			events.addAll(CollectionUtility.arrayList(eventsPage.getValue()));

			// FIXEME Djer13 handle multi page results
			if (null != eventsPage.getNextPageLink()) {
				LOG.warn("Some event are not analysed because on the next page ! url :" + eventsPage.getNextPageLink());
			}
		} catch (final IOException e) {
			LOG.error("Microsft API error while trying to retrieve Events", e);
		}

		return events;
	}

	@Override
	public Boolean isNotRegiteredOn(final Event event, final String userEmail) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isFree(final Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isFullDay(final Event event) {
		// TODO Auto-generated method stub
		return null;
	}

}
