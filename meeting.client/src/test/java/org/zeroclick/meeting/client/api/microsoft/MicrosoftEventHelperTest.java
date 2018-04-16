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

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.meeting.client.api.microsoft.data.DateTimeTimeZone;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.service.CalendarAviability;

/**
 * @author djer Most code in parent ABstractEventHelper covered by
 *         GoogleEventHelper
 */
@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("PMD.MethodNamingConventions")
public class MicrosoftEventHelperTest {

	private static final String DEFAULT_TIME_ZONE = "Europe/Paris";

	private final MicrosoftEventHelper eventHelper = new MicrosoftEventHelper();

	@Test
	public void testGetCalendarAviability_noBlockingEvent() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 17, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final CalendarAviability calendarAviability = this.eventHelper.getCalendarAviability(startDate, endDate, events,
				userZoneId);

		Assert.assertNull("One freeTime should be found", calendarAviability.getFreeTimes());

		Assert.assertNull("Last event should be null when no event blocking search",
				calendarAviability.getEndLastEvent());
	}

	@Test
	public void testGetCalendarAviability_oneEventBloking_endOfPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 17, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-04-18T17:00:00+02:00", "2018-04-18T18:00:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final CalendarAviability calendarAviability = this.eventHelper.getCalendarAviability(startDate, endDate, events,
				userZoneId);

		Assert.assertTrue("One freeTime should be found", calendarAviability.getFreeTimes().size() == 1);
		final DayDuration firstFreeTime = calendarAviability.getFreeTimes().get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(16, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(17, 00, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		Assert.assertNotNull("Last event end time not provided", calendarAviability.getEndLastEvent());
		final ZonedDateTime expectedLastEventEnd = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 18, 0, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		// time zone representation change (+02:00 VS Europe/Paris) but instants
		// must be the same
		Assert.assertEquals("Invalid end of last Event", expectedLastEventEnd.getNano(),
				calendarAviability.getEndLastEvent().getNano());
	}

	private Event createBasicEvent(final String startIso8601, final String endIso8601) {
		return this.createBasicEvent(startIso8601, endIso8601, DEFAULT_TIME_ZONE);
	}

	private Event createBasicEvent(final String startIso8601, final String endIso8601, final String timeZone) {
		final Event event = new Event();

		event.setStart(this.createEventDateTime(startIso8601, timeZone));
		event.setEnd(this.createEventDateTime(endIso8601, timeZone));

		return event;
	}

	private DateTimeTimeZone createEventDateTime(final String dateIso8601, final String timeZone) {
		final DateTimeTimeZone dateTime = new DateTimeTimeZone();
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime tempDateTime = ZonedDateTime.parse(dateIso8601, DateTimeFormatter.ISO_DATE_TIME);

		dateTime.setDateTime(dateHelper.toDate(tempDateTime));
		dateTime.setTimeZone(timeZone);

		return dateTime;
	}

}
