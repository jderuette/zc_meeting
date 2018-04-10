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

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.service.CalendarAviability;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * @author djer
 *
 */
@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("PMD.MethodNamingConventions")
public class GoogleEventHelperTest {

	// Be aware, changing zimeZone "name" may imply changing some test case
	// data (event start and end)
	// test Data assume the TImeZone is GMT+2 (Europe/Paris summer time) in some
	// date creation
	private static final String DEFAULT_TIME_ZONE = "Europe/Paris";

	private final GoogleEventHelper eventHelper = new GoogleEventHelper();

	@Test
	public void testGetFreeTime_OneEventBlockWholePeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_OneEventBlockWholePeriodStartsBefore() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:45:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_OneEventBlockWholePeriodEndsAfter() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:45:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_OneEventBlockWholePeriodLongerThanMeeting() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:00:00+02:00", "2018-03-26T10:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_TwoEventBlockWholePeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:00:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:10:00+02:00", "2018-03-26T10:00:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_TwoEventBlockWholePeriodStartsBefore() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:00:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:10:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_TwoEventBlockWholePeriodEndsAfter() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:00:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:10:00+02:00", "2018-03-26T09:15:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_TwoEventBlockWholePeriodLongerThanMeeting() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:10:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_ThreeEventBlockWholePeriodLongerThanMeeting_OneEventOverlapPotetialFreeTime() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:08:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T07:08:00+02:00", "2018-03-26T08:15:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_ThreeEventBlockWholePeriodLongerThanMeeting_StartBeforeTwoUseless() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:00:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:45:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:25:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should NOT exists but found " + freeTime, freeTime.isEmpty());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeBeginsOfPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:15:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 0, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeBeginsOfPeriodWithOneUseless() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:05:00+02:00", "2018-03-26T08:30:00+02:00")); // 5Mins
		events.add(this.createBasicEvent("2018-03-26T08:15:00+02:00", "2018-03-26T08:30:00+02:00")); // 15Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 0, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 5, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeBeginsOfPeriodWithTwoUseless() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:05:00+02:00", "2018-03-26T08:30:00+02:00")); // 5Mins
		events.add(this.createBasicEvent("2018-03-26T08:05:00+02:00", "2018-03-26T08:15:00+02:00")); // 5Mins
		events.add(this.createBasicEvent("2018-03-26T08:15:00+02:00", "2018-03-26T08:30:00+02:00")); // 15Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 0, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 5, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeBeginsOfPeriodWithOneLongerThanPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:05:00+02:00", "2018-03-26T10:00:00+02:00")); // 5Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 0, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 5, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeBeginsOfPeriodWithOneStartingAtPeriodEnds() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:05:00+02:00", "2018-03-26T10:00:00+02:00")); // 5Mins
		events.add(this.createBasicEvent("2018-03-26T08:30:00+02:00", "2018-03-26T10:45:00+02:00")); // 5Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 0, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 5, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeEndsOfPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:20:00+02:00")); // 10Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 20, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeEndsOfPeriodWithTwoUseless() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:20:00+02:00")); // 10Mins
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:10:00+02:00")); // 20Mins
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:15:00+02:00")); // 15Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 20, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeEndsOfPeriodWithOneLongerThanPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:20:00+02:00")); // 10Mins

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 20, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeEndsOfPeriodWithOneStartingAtPeriodEnds() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:20:00+02:00")); // 10Mins
		events.add(this.createBasicEvent("2018-03-26T08:30:00+02:00", "2018-03-26T10:00:00+02:00")); // after
																										// period

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 20, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

	}

	@Test
	public void testGetFreeTime_OneFreeTimeEndsOfPeriodWithOneEndingAtPeriodBegins() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:20:00+02:00")); // 10Mins
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:00:00+02:00")); // before

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 20, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeMidleOfPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:12:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeMidleOfPeriodWithOneStartingBeforePeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:12:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeMidleOfPeriodWithOneEndingAfterPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:12:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:45:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_OneFreeTimeMidleOfPeriodWithBothWiderThanPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:15:00+02:00", "2018-03-26T08:12:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:45:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only ONE period " + freeTime, freeTime.size() == 1);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());

		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_TwoFreeTimeMidleOfPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only TWO period " + freeTime, freeTime.size() == 2);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		final DayDuration secondFreeTime = freeTime.get(1);

		final OffsetTime expectedSecondStartFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime start is not valid", expectedSecondStartFreeTime,
				secondFreeTime.getStart());
		final OffsetTime expectedSecondEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime end is not valid", expectedSecondEndFreeTime, secondFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_TwoFreeTimeMidleOfPeriodStartingBeforePeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:30:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only TWO period " + freeTime, freeTime.size() == 2);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		final DayDuration secondFreeTime = freeTime.get(1);

		final OffsetTime expectedSecondStartFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime start is not valid", expectedSecondStartFreeTime,
				secondFreeTime.getStart());
		final OffsetTime expectedSecondEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime end is not valid", expectedSecondEndFreeTime, secondFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_TwoFreeTimeMidleOfPeriodEndingBeforePeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:15:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only TWO period " + freeTime, freeTime.size() == 2);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		final DayDuration secondFreeTime = freeTime.get(1);

		final OffsetTime expectedSecondStartFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime start is not valid", expectedSecondStartFreeTime,
				secondFreeTime.getStart());
		final OffsetTime expectedSecondEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime end is not valid", expectedSecondEndFreeTime, secondFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_TwoFreeTimeMidleOfPeriodWiderThanPeriod() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:10:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:15:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only TWO period " + freeTime, freeTime.size() == 2);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		final DayDuration secondFreeTime = freeTime.get(1);

		final OffsetTime expectedSecondStartFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime start is not valid", expectedSecondStartFreeTime,
				secondFreeTime.getStart());
		final OffsetTime expectedSecondEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime end is not valid", expectedSecondEndFreeTime, secondFreeTime.getEnd());
	}

	@Test
	public void testGetFreeTime_TwoFreeTimeMidleOfPeriodWiderThanPeriodWithUseleddEvent() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		// == Event1
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:10:00+02:00"));
		// Event1.1 : inside Event1, same start
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:08:00+02:00"));
		// Event1.2 : inside Event1 same end
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:08:00+02:00"));
		// Event1.3 inside Event1
		events.add(this.createBasicEvent("2018-03-26T08:00:00+02:00", "2018-03-26T08:05:00+02:00"));
		// == Event2
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		// Event2.1 : inside Event2, same start
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		// Event2.2 : inside Event2 same end
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:15:00+02:00"));
		// Event2.3 : inside Event2
		events.add(this.createBasicEvent("2018-03-26T08:12:00+02:00", "2018-03-26T08:14:00+02:00"));
		// == Event3
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:15:00+02:00"));
		// Event3.1 : inside Event3 same start, ends before period end
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:29:00+02:00"));
		// Event3.2 : inside Event3 same start, ends before period end
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T08:40:00+02:00"));
		// Event3.3 : inside Event3 same end
		events.add(this.createBasicEvent("2018-03-26T08:25:00+02:00", "2018-03-26T09:15:00+02:00"));
		// Event3.4 : inside, end before period ends
		events.add(this.createBasicEvent("2018-03-26T08:27:00+02:00", "2018-03-26T08:29:00+02:00"));
		// Event3.5 : inside, end before period ends
		events.add(this.createBasicEvent("2018-03-26T08:27:00+02:00", "2018-03-26T08:45:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final List<DayDuration> freeTime = this.eventHelper.getFreeTime(startDate, endDate, events, userZoneId);

		Assert.assertTrue("FreeTime should exists for only TWO period " + freeTime, freeTime.size() == 2);

		final DayDuration firstFreeTime = freeTime.get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 12, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		final DayDuration secondFreeTime = freeTime.get(1);

		final OffsetTime expectedSecondStartFreeTime = OffsetTime.of(8, 15, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime start is not valid", expectedSecondStartFreeTime,
				secondFreeTime.getStart());
		final OffsetTime expectedSecondEndFreeTime = OffsetTime.of(8, 25, 0, 0, startDate.getOffset());
		Assert.assertEquals("Second FreeTime end is not valid", expectedSecondEndFreeTime, secondFreeTime.getEnd());
	}

	@Test
	public void testGetCalendarAviability_noBlockingEvent() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
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
	public void testGetCalendarAviability_oneEventBloking() {
		final ZonedDateTime startDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final ArrayList<Event> events = new ArrayList<>();
		events.add(this.createBasicEvent("2018-03-26T07:30:00+02:00", "2018-03-26T08:10:00+02:00"));

		final ZoneId userZoneId = ZoneId.of(DEFAULT_TIME_ZONE);

		final CalendarAviability calendarAviability = this.eventHelper.getCalendarAviability(startDate, endDate, events,
				userZoneId);

		Assert.assertTrue("One freeTime should be found", calendarAviability.getFreeTimes().size() == 1);
		final DayDuration firstFreeTime = calendarAviability.getFreeTimes().get(0);

		final OffsetTime expectedStartFreeTime = OffsetTime.of(8, 10, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime start is not valid", expectedStartFreeTime, firstFreeTime.getStart());
		final OffsetTime expectedEndFreeTime = OffsetTime.of(8, 30, 0, 0, startDate.getOffset());
		Assert.assertEquals("First FreeTime end is not valid", expectedEndFreeTime, firstFreeTime.getEnd());

		Assert.assertNotNull("Last event end time not provided", calendarAviability.getEndLastEvent());
		final ZonedDateTime expectedLastEventEnd = ZonedDateTime.of(LocalDateTime.of(2018, 03, 26, 8, 10, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		// time zone representation change (+02:00 VS Europe/Paris) but instants
		// must be the same
		Assert.assertEquals("Invalid end of last Event", expectedLastEventEnd.getNano(),
				calendarAviability.getEndLastEvent().getNano());
	}

	private Event createBasicEvent(final String startRfc3339, final String endRfc3339) {
		return this.createBasicEvent(startRfc3339, endRfc3339, DEFAULT_TIME_ZONE);
	}

	private Event createBasicEvent(final String startRfc3339, final String endRfc3339, final String timeZone) {
		final Event event = new Event();

		final EventDateTime start = new EventDateTime();
		start.setDate(DateTime.parseRfc3339(startRfc3339));
		start.setTimeZone(timeZone);

		final EventDateTime end = new EventDateTime();
		end.setDate(DateTime.parseRfc3339(endRfc3339));
		end.setTimeZone(timeZone);

		event.setStart(start);
		event.setEnd(end);

		return event;
	}

}
