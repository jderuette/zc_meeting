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
package org.zeroclick.comon.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

/**
 * @author djer
 *
 */
@SuppressWarnings("PMD.MethodNamingConventions")
public class DateHelperTest {

	private static final String DEFAULT_TEST_TIME_ZONE = "Europe/Paris";
	private static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";

	private ZonedDateTime buildZonedDateTime(final String zoneId, final String date) throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		final SimpleDateFormat dateformater = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		final ZonedDateTime ZonedDateTime = dateHelper.getZonedValue(ZoneId.of(zoneId), dateformater.parse(date));

		return ZonedDateTime;
	}

	private ZonedDateTime getZonedNow(final String zoneId, final Integer hours, final Integer minutes,
			final Integer secondes) {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final Calendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, secondes);
		cal.set(Calendar.MILLISECOND, 0);

		final ZonedDateTime nowWithTime = dateHelper.getZonedValue(ZoneId.of(zoneId), Date.from(cal.toInstant()));

		return nowWithTime;

	}

	@Test
	public void testGetRelativeTimeShift_tomorrow() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 9, 15, 0).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_tomorrowAlmost24Hours() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 14).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_tomorrow24Hours() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayAfter() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 19, 0, 0);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayBefore() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 9, 30, 0);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayExactlySame() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterday() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 23, 45, 0).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterdayAlmost24Hours() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 16).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterday24Hours() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_inSevenDays() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime zonedMeetingStart = zonedNow.plusDays(7);

		final long nbDays = dateHelper.getRelativeTimeShift(zonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + zonedNow + " to " + zonedMeetingStart, 7, nbDays);
	}

	@Test
	public void testGetStartPoint() throws ParseException {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);

		final ZonedDateTime startOfDay = dateHelper.getStartPoint(zonedStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("With byFullTimeSlot to TRUE, rounded start point and 'real' one should be the same day",
				zonedStart.getDayOfYear(), startOfDay.getDayOfYear());
	}

	@Test
	public void testIsInPeriodInlcusiv_before() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 12, 55, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isInPeriodInlcusiv(zonedStart, periodStart, periodEnd);

		assertFalse("Tested date " + zonedStart + " should not be in period, because it's before period : "
				+ periodStart + " - " + periodEnd, inPeriod);
	}

	@Test
	public void testIsInPeriodInlcusiv_sameStart() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isInPeriodInlcusiv(zonedStart, periodStart, periodEnd);

		assertTrue("Tested date " + zonedStart + " should be in period, because it has the same start of the period : "
				+ periodStart + " - " + periodEnd, inPeriod);
	}

	@Test
	public void testIsInPeriodInlcusiv_inPeriod() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 30, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isInPeriodInlcusiv(zonedStart, periodStart, periodEnd);

		assertTrue("Tested date " + zonedStart + " should be in period, because it between start/end of the period : "
				+ periodStart + " - " + periodEnd, inPeriod);
	}

	@Test
	public void testIsInPeriodInlcusiv_sameEnd() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isInPeriodInlcusiv(zonedStart, periodStart, periodEnd);

		assertTrue("Tested date " + zonedStart + " should be in period, because it the same end of the period : "
				+ periodStart + " - " + periodEnd, inPeriod);
	}

	@Test
	public void testIsInPeriodInlcusiv_after() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 25, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isInPeriodInlcusiv(zonedStart, periodStart, periodEnd);

		assertFalse("Tested date " + zonedStart + " should NOT be in period, because it after the period : "
				+ periodStart + " - " + periodEnd, inPeriod);
	}

	@Test
	public void testIsPeriodOverlap_before() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 12, 00, 00);
		final ZonedDateTime zonedEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 12, 45, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isPeriodOverlap(zonedStart, zonedEnd, periodStart, periodEnd);

		assertFalse(
				"Tested period " + zonedStart + " - " + zonedEnd
						+ " should NOT be in period, because it before the period : " + periodStart + " - " + periodEnd,
				inPeriod);
	}

	@Test
	public void testIsPeriodOverlap_endOverlap() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime zonedEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 15, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isPeriodOverlap(zonedStart, zonedEnd, periodStart, periodEnd);

		assertTrue("Tested period " + zonedStart + " - " + zonedEnd
				+ " should be in period, because it end overlap the period : " + periodStart + " - " + periodEnd,
				inPeriod);
	}

	@Test
	public void testIsPeriodOverlap_inside() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 15, 00);
		final ZonedDateTime zonedEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 45, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isPeriodOverlap(zonedStart, zonedEnd, periodStart, periodEnd);

		assertTrue(
				"Tested period " + zonedStart + " - " + zonedEnd
						+ " should be in period, because it inside the period : " + periodStart + " - " + periodEnd,
				inPeriod);
	}

	@Test
	public void testIsPeriodOverlap_beginOverlap() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);
		final ZonedDateTime zonedEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 45, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isPeriodOverlap(zonedStart, zonedEnd, periodStart, periodEnd);

		assertTrue("Tested period " + zonedStart + " - " + zonedEnd
				+ " should be in period, because it begin overlap the period : " + periodStart + " - " + periodEnd,
				inPeriod);
	}

	@Test
	public void testIsPeriodOverlap_after() {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime zonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 15, 00);
		final ZonedDateTime zonedEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 45, 00);

		final ZonedDateTime periodStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 00, 00);
		final ZonedDateTime periodEnd = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 14, 00, 00);

		final Boolean inPeriod = dateHelper.isPeriodOverlap(zonedStart, zonedEnd, periodStart, periodEnd);

		assertFalse(
				"Tested period " + zonedStart + " - " + zonedEnd
						+ " should NOT be in period, because it after the period : " + periodStart + " - " + periodEnd,
				inPeriod);
	}
}
