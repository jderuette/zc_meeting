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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * @author djer
 *
 */
public class DateHelperTest {

	private static final String DEFAULT_TEST_TIME_ZONE = "Europe/Paris";
	private static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy hh:mm:ss";

	private ZonedDateTime buildZonedDateTime(final String zoneId, final String date) throws ParseException {
		final DateHelper dateHelper = DateHelper.get();
		final SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		final ZonedDateTime ZonedDateTime = dateHelper.getZonedValue(ZoneId.of(zoneId), df.parse(date));

		return ZonedDateTime;
	}

	private ZonedDateTime getZonedNow(final String zoneId, final Integer hours, final Integer minutes,
			final Integer secondes) {
		final DateHelper dateHelper = DateHelper.get();

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
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 9, 15, 0).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_tomorrowAlmost24Hours() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 14).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_tomorrow24Hours() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15).plusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayAfter() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 19, 0, 0);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayBefore() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 9, 30, 0);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_todayExactlySame() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 0, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterday() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 23, 45, 0).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterdayAlmost24Hours() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 16).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_yesterday24Hours() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15).minusDays(1);

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, -1, nbDays);
	}

	@Test
	public void testGetRelativeTimeShift_inSevenDays() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedNow = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);
		final ZonedDateTime ZonedMeetingStart = this.buildZonedDateTime(DEFAULT_TEST_TIME_ZONE, "15-08-2017 08:15:00");

		final long nbDays = dateHelper.getRelativeTimeShift(ZonedMeetingStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("Bad nb day from : " + ZonedNow + " to " + ZonedMeetingStart, 7, nbDays);
	}

	@Test
	public void testGetStartPoint() throws ParseException {
		final DateHelper dateHelper = DateHelper.get();

		final ZonedDateTime ZonedStart = this.getZonedNow(DEFAULT_TEST_TIME_ZONE, 13, 8, 15);

		final ZonedDateTime startOfDay = dateHelper.getStartPoint(ZonedStart, Boolean.TRUE, ChronoUnit.DAYS);

		assertEquals("With byFullTimeSlot to TRUE, rounded start point and 'real' one should be the same day",
				ZonedStart.getDayOfYear(), startOfDay.getDayOfYear());

	}
}
