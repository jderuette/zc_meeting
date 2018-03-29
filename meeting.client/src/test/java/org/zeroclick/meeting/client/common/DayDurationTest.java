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
package org.zeroclick.meeting.client.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.meeting.client.common.SlotHelper.DayOfWeekLists;

/**
 * @author djer
 *
 */
@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("PMD.MethodNamingConventions")
public class DayDurationTest {

	private DayDuration morningWorkDay;
	private DayDuration saturdayOnly;
	private DayDuration dayOverlap;
	private DayDuration oneDayOfTwo;

	@Before
	public void setUp() {
		this.morningWorkDay = new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC),
				OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), DayOfWeekLists.STANDARD_WORK_DAYS);

		this.saturdayOnly = new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC),
				OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC), Arrays.asList(DayOfWeek.SATURDAY));

		// Is it a valid DayDuration ?
		this.dayOverlap = new DayDuration(OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC),
				OffsetTime.of(6, 0, 0, 0, ZoneOffset.UTC), DayOfWeekLists.ALL_DAYS);

		this.oneDayOfTwo = new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC),
				OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC),
				Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY));
	}

	@Test
	public void testIsSameDay_Monday() {
		final LocalDate date1 = LocalDate.of(2017, 12, 4);
		final LocalDate date2 = LocalDate.of(2017, 12, 4);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1, date2);
		Assert.assertTrue("Date should be the same", isSameDay);
	}

	@Test
	public void testIsSameDay_Same_ButHours() {
		final LocalDateTime date1 = LocalDateTime.of(2017, 12, 4, 10, 30, 21);
		final LocalDateTime date2 = LocalDateTime.of(2017, 12, 4, 18, 30, 00);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1.toLocalDate(), date2.toLocalDate());
		Assert.assertTrue("Date should be the same", isSameDay);
	}

	@Test
	public void testIsSameDay_NotSame_WithHours() {
		final LocalDateTime date1 = LocalDateTime.of(2017, 12, 18, 10, 30, 21);
		final LocalDateTime date2 = LocalDateTime.of(2017, 12, 4, 10, 30, 00);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1.toLocalDate(), date2.toLocalDate());
		Assert.assertFalse("Date should NOT be the same", isSameDay);
	}

	@Test
	public void testIsSameDay_Monday_andOtherWeek() {
		final LocalDate date1 = LocalDate.of(2017, 12, 4);
		final LocalDate date2 = LocalDate.of(2017, 12, 11);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1, date2);
		Assert.assertFalse("Date should NOT be the same", isSameDay);
	}

	@Test
	public void testIsSameDay_Monday_andOtherYear() {
		final LocalDate date1 = LocalDate.of(2017, 12, 4);
		final LocalDate date2 = LocalDate.of(2016, 12, 4);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1, date2);
		Assert.assertFalse("Date should NOT be the same", isSameDay);
	}

	@Test
	public void testIsSameDay_NotSame() {
		final LocalDate date1 = LocalDate.of(2017, 11, 21);
		final LocalDate date2 = LocalDate.of(2017, 12, 11);

		final Boolean isSameDay = this.morningWorkDay.isSameDay(date1, date2);
		Assert.assertFalse("Date should NOT be the same", isSameDay);
	}

	@Test
	public void testGetTimeOverlapFullDay_InvalidWeekDay() {
		final LocalDate date1 = LocalDate.of(2017, 12, 9);

		final Long overlapTime = this.morningWorkDay.getTimeOverlapFullDay(date1);
		Assert.assertEquals("Date is not on valid Week day, overlap must be 0", 0l, overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlapFullDay_Morning() {
		final LocalDate date1 = LocalDate.of(2017, 12, 4);

		final Long overlapTime = this.morningWorkDay.getTimeOverlapFullDay(date1);
		Assert.assertEquals("Invalid timeOverlap", hoursToMins(4), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlapFullDay_Range_2days_Morning() {
		final LocalDate start = LocalDate.of(2017, 12, 4);
		final LocalDate end = LocalDate.of(2017, 12, 5);

		final Long overlapTime = this.morningWorkDay.getTimeOverlapFullDay(start, end);
		Assert.assertEquals("Invalid timeOverlap (4 hours during two days)", hoursToMins(8), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlapFullDay_Range_6days_With2Invalid_Morning() {
		final LocalDate start = LocalDate.of(2017, 12, 7);
		final LocalDate end = LocalDate.of(2017, 12, 12);

		final Long overlapTime = this.morningWorkDay.getTimeOverlapFullDay(start, end);
		Assert.assertEquals("Invalid timeOverlap (4 hours during 4 days (2 are invalid WeekDays))", hoursToMins(16),
				overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Monday_Morning_EndDateMatchEndDayDuration() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 4, 12, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals("Invalid timeOverlap (2 hours avialable in a unique DayDuration period)", hoursToMins(2),
				overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Monday_Morning_EndDateAfterPeriodEnd() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 4, 18, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap (2 hours avialable in a unique DayDuration period, 6 Hours over period)",
				hoursToMins(2), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Monday_Morning_StartDateMatchPeriodBegin() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 8, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 4, 10, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals("Invalid timeOverlap (2 hours avialable in a unique DayDuration period)", hoursToMins(2),
				overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Monday_Morning_StartDateBeforePeriodBegin() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 6, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 4, 10, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap (2 hours avialable in a unique DayDuration period (2 hours over period))",
				hoursToMins(2), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Monday_Morning_StartDateAndEndDateWidderThanPeriod() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 6, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 4, 15, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap (4 hours avialable in a unique DayDuration period (2+3 (5) hours over period))",
				hoursToMins(4), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateWidderThanPeriod_StartInvalidWeekDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 3, 6, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 5, 15, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap ((2*4) 8 hours avialable in 2 DayDuration period (3 hours over period on Last Day))",
				hoursToMins(8), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateWidderThanPeriod_EndInvalidWeekDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 5, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 9, 15, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap ((3*4) 14 hours avialable in 4 DayDuration period + 2 Hours first Day (LastDay ignored))",
				hoursToMins(14), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateWidderThanPeriod_EndAndStartInvalidWeekDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 3, 6, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 9, 15, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap ((5*4) 20 hours avialable in 5 DayDuration period (4 Hours ignore FirstDay, 4 Hours ignored Last Day))",
				hoursToMins(20), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MultipleWeeks() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2018, 1, 3, 9, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end);
		Assert.assertEquals(
				"Invalid timeOverlap (2 Hours firstDay + 4*4 (first partial Week) + (5*4*3) (3 Full weeks) + 2*4 (partial LastWeek) + 1 hour Lats Day = 87 hours avialable)",
				hoursToMins(2 + 4 * 4 + 5 * 4 * 3 + 2 * 4 + 1), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MultipleWeeks_2HoursDuration() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2018, 1, 3, 9, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end, 120D);
		Assert.assertEquals(
				"Invalid timeOverlap (2 Hours firstDay + 4*4 (first partial Week) + (5*4*3) (3 Full weeks) + 2*4 (partial LastWeek) + 1 hour Lats Day Ignored (to short) = 86 hours avialable)",
				hoursToMins(2 + 4 * 4 + 5 * 4 * 3 + 2 * 4), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MultipleWeeks_4HoursDuration() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 10, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2018, 1, 3, 9, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end, 240D);
		Assert.assertEquals(
				"Invalid timeOverlap (2 Hours firstDay Ignored + 4*4 (first partial Week) + (5*4*3) (3 Full weeks) + 2*4 (partial LastWeek) + 1 hour Lats Day Ignored (to short) = 84 hours avialable)",
				hoursToMins(4 * 4 + 5 * 4 * 3 + 2 * 4), overlapTime.longValue());
	}

	@Test
	public void testGetTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MeetingDurationLongerThanPeriod() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 7, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 6, 14, 0, 0);

		final Long overlapTime = this.morningWorkDay.getTimeOverlap(start, end, 360D);
		Assert.assertEquals(
				"Invalid timeOverlap (ALl should be ignored because meeting duration longer than pariod Duration)",
				hoursToMins(0), overlapTime.longValue());
	}

	@Test
	public void testHasTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MeetingDurationMatchFirstDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 9, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 6, 14, 0, 0);

		final Boolean hasOverlapTime = this.morningWorkDay.hasTimeOverlap(start, end, 120D);
		Assert.assertTrue("Invalid hasTimeOverlap (first (partial) day should be available)", hasOverlapTime);
	}

	@Test
	public void testHasTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MeetingDurationMatchLastDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 11, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 6, 11, 30, 0);

		final Boolean hasOverlapTime = this.morningWorkDay.hasTimeOverlap(start, end, 120D);
		Assert.assertTrue("Invalid hasTimeOverlap (last (partial) day should be available)", hasOverlapTime);
	}

	@Test
	public void testHasTimeOverlap_Morning_StartDateAndEndDateInsidePeriod_MeetingDurationMatchOneOfFullDay() {
		final LocalDateTime start = LocalDateTime.of(2017, 12, 4, 11, 0, 0);
		final LocalDateTime end = LocalDateTime.of(2017, 12, 6, 10, 0, 0);

		final Boolean hasOverlapTime = this.morningWorkDay.hasTimeOverlap(start, end, 120D);
		Assert.assertTrue("Invalid hasTimeOverlap (One of FUll day should be available)", hasOverlapTime);
	}

	private static long hoursToMins(final int hours) {
		return hours * 60;
	}

}
