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

/**
 * @author djer
 *
 */
@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
@SuppressWarnings("PMD.MethodNamingConventions")
public class SlotHelperTest {

	private static final String DEFAULT_TIME_ZONE = "Europe/Paris";

	SlotHelper slotHelper = SlotHelper.get(Boolean.FALSE);

	/**
	 * Periods are NOT Weekly perpetual, as this test check for searching
	 * available time in FreeTime extracted from a period of calendar time
	 */
	@Test
	public void testGetNextValidDateTime_usingFreeTime_noNextAvailable() {
		final ZonedDateTime checkedDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 17, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final List<DayDuration> periods = new ArrayList<>();
		final OffsetTime start = OffsetTime.of(16, 15, 0, 0, checkedDate.getOffset());
		final OffsetTime end = OffsetTime.of(17, 0, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeek = new ArrayList<>();
		validDayOfWeek.add(DayOfWeek.WEDNESDAY);
		final DayDuration firstPeriod = new DayDuration(start, end, validDayOfWeek, false, false);

		periods.add(firstPeriod);

		final ZonedDateTime nextValidDate = this.slotHelper.getNextValidDateTime(periods, checkedDate, endDate);

		Assert.assertNull("No next valid date should be found in the freeTime periods provided (meeting is too long)",
				nextValidDate);
	}

	@Test
	public void testGetNextValidDateTime_usingFreeTime_nextBeginOfPeriod() {
		final ZonedDateTime checkedDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final List<DayDuration> periods = new ArrayList<>();
		final OffsetTime start = OffsetTime.of(16, 15, 0, 0, checkedDate.getOffset());
		final OffsetTime end = OffsetTime.of(17, 00, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeek = new ArrayList<>();
		validDayOfWeek.add(DayOfWeek.WEDNESDAY);
		final DayDuration firstPeriod = new DayDuration(start, end, validDayOfWeek, false, false);

		periods.add(firstPeriod);

		final ZonedDateTime nextValidDate = this.slotHelper.getNextValidDateTime(periods, checkedDate, endDate);

		final ZonedDateTime expectedNextValidDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		Assert.assertNotNull("Missing next valid Date (null)", nextValidDate);
		Assert.assertEquals("Next valid Date is invalid", expectedNextValidDate.getNano(), nextValidDate.getNano());
	}

	@Test
	public void testGetNextValidDateTime_usingFreeTime_nextInsideOfPeriod() {
		final ZonedDateTime checkedDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 15, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final List<DayDuration> periods = new ArrayList<>();
		final OffsetTime start = OffsetTime.of(16, 0, 0, 0, checkedDate.getOffset());
		final OffsetTime end = OffsetTime.of(16, 5, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeek = new ArrayList<>();
		validDayOfWeek.add(DayOfWeek.WEDNESDAY);
		final DayDuration firstPeriod = new DayDuration(start, end, validDayOfWeek, false, false);

		periods.add(firstPeriod); // is Too Short

		periods.add(firstPeriod);

		final OffsetTime startSecondPeriod = OffsetTime.of(16, 10, 0, 0, checkedDate.getOffset());
		final OffsetTime endSecondPeriod = OffsetTime.of(17, 30, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeekSecondPeriod = new ArrayList<>();
		validDayOfWeekSecondPeriod.add(DayOfWeek.WEDNESDAY);
		final DayDuration secondPeriod = new DayDuration(startSecondPeriod, endSecondPeriod, validDayOfWeekSecondPeriod,
				false, false);

		periods.add(secondPeriod);

		final ZonedDateTime nextValidDate = this.slotHelper.getNextValidDateTime(periods, checkedDate, endDate);

		final ZonedDateTime expectedNextValidDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 10, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		Assert.assertNotNull("Missing next valid Date (null)", nextValidDate);
		Assert.assertEquals("Next valid Date is invalid", expectedNextValidDate.getNano(), nextValidDate.getNano());

	}

	@Test
	public void testGetNextValidDateTime_usingFreeTime_NoNextTwoPeriodTooShort() {
		final ZonedDateTime checkedDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 16, 30, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));
		final ZonedDateTime endDate = ZonedDateTime.of(LocalDateTime.of(2018, 04, 18, 17, 00, 00),
				ZoneId.of(DEFAULT_TIME_ZONE));

		final List<DayDuration> periods = new ArrayList<>();

		final OffsetTime start = OffsetTime.of(16, 15, 0, 0, checkedDate.getOffset());
		final OffsetTime end = OffsetTime.of(16, 45, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeek = new ArrayList<>();
		validDayOfWeek.add(DayOfWeek.WEDNESDAY);
		final DayDuration firstPeriod = new DayDuration(start, end, validDayOfWeek, false, false);

		periods.add(firstPeriod);

		final OffsetTime startSecondPeriod = OffsetTime.of(17, 00, 0, 0, checkedDate.getOffset());
		final OffsetTime endSecondPeriod = OffsetTime.of(17, 30, 0, 0, checkedDate.getOffset());
		final List<DayOfWeek> validDayOfWeekSecondPeriod = new ArrayList<>();
		validDayOfWeekSecondPeriod.add(DayOfWeek.WEDNESDAY);
		final DayDuration secondPeriod = new DayDuration(startSecondPeriod, endSecondPeriod, validDayOfWeekSecondPeriod,
				false, false);

		periods.add(secondPeriod);

		final ZonedDateTime nextValidDate = this.slotHelper.getNextValidDateTime(periods, checkedDate, endDate);

		Assert.assertNull("No next valid date should be found in the freeTime periods provided (meeting is too long)",
				nextValidDate);
	}

}
