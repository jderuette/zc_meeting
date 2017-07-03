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
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
public class SlotHelper {
	private static final Logger LOG = LoggerFactory.getLogger(SlotHelper.class);

	private static SlotHelper instance;

	private SlotHelper() {
		// Singleton
	}

	public static SlotHelper get() {
		if (null == instance) {
			instance = new SlotHelper();
		}
		return instance;
	}

	private final List<DayDuration> PERIODE_WORK = new ArrayList<>(Arrays.asList(
			new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WORK_DAYS),
			new DayDuration(OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WORK_DAYS)));

	private final List<DayDuration> PERIODE_LUNCH = new ArrayList<>(
			Arrays.asList(new DayDuration(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
					OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC), DayOfWeekLists.STANDARD_WORK_DAYS)));

	private final List<DayDuration> PERIODE_NIGHT = new ArrayList<>(
			Arrays.asList(new DayDuration(OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC),
					OffsetTime.of(23, 59, 59, 999999999, ZoneOffset.UTC), DayOfWeekLists.STANDARD_WORK_DAYS)));

	private final List<DayDuration> PERIODE_WEEK_END = new ArrayList<>(Arrays.asList(
			new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WEEKEND_DAYS),
			new DayDuration(OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WEEKEND_DAYS)));

	private List<DayDuration> getPeriods(final Integer durationId) {
		if (durationId == 1) {
			// 8-12 and 14-18
			return this.PERIODE_WORK;
		} else if (durationId == 2) {
			// 12-14
			return this.PERIODE_LUNCH;
		} else if (durationId == 3) {
			// 20-24
			return this.PERIODE_NIGHT;
		} else if (durationId == 4) {
			// 8-22 Saturday and Monday (And vacation Days ?)
			return this.PERIODE_WEEK_END;
		}

		return new ArrayList<>(1);
	}

	/**
	 * Check if the date is in of the periods of the duration List (by ID)
	 *
	 * @param slotId
	 * @param checkedDate
	 * @return
	 */
	public Boolean isInOneOfPeriods(final Integer slotId, final ZonedDateTime checkedDate) {
		final List<DayDuration> periods = this.getPeriods(slotId);

		return this.isInOneOfPeriods(periods, checkedDate, null);
	}

	/**
	 * Check if the both date are in of the periods and the *same* of the
	 * duration List (by ID)
	 *
	 * @param slotId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Boolean isInOneOfPeriods(final Integer slotId, final ZonedDateTime startDate, final ZonedDateTime endDate) {
		final List<DayDuration> periods = this.getPeriods(slotId);

		return this.isInOneOfPeriods(periods, startDate, endDate);
	}

	/**
	 * Check if the date is in of the periods of the provided list
	 *
	 * @param durationId
	 * @param startDate
	 * @return
	 */
	public Boolean isInOneOfPeriods(final List<DayDuration> periods, final ZonedDateTime startDate,
			final ZonedDateTime endDate) {
		Boolean isInPeriod = Boolean.FALSE;
		for (final DayDuration period : periods) {
			if (null == endDate) {
				if (period.isInPeriod(startDate)) {
					LOG.info(startDate + " (" + startDate.getDayOfWeek() + ") is in period " + period);
					isInPeriod = Boolean.TRUE;
				}
			} else {
				if (period.isInPeriod(startDate, endDate)) {
					LOG.info(startDate + " and " + endDate + " (" + startDate.getDayOfWeek() + ") are in period "
							+ period);
					isInPeriod = Boolean.TRUE;
				}
			}
		}
		if (!isInPeriod) {
			LOG.info(startDate + " and " + endDate + " (" + startDate.getDayOfWeek() + ") are NOT in one of  periods "
					+ periods);
		}
		return isInPeriod;
	}

	public ZonedDateTime getNextValidDateTime(final Integer slotId, final ZonedDateTime checkedDate) {
		final List<DayDuration> periods = this.getPeriods(slotId);

		return this.getNextValidDateTime(periods, checkedDate, null);
	}

	public ZonedDateTime getNextValidDateTime(final Integer slotId, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate) {
		final List<DayDuration> periods = this.getPeriods(slotId);

		return this.getNextValidDateTime(periods, checkedDate, endDate);
	}

	public ZonedDateTime getNextValidDateTime(final List<DayDuration> periods, final ZonedDateTime checkedDate) {
		return this.getNextValidDateTime(periods, checkedDate, null);
	}

	public ZonedDateTime getNextValidDateTime(final List<DayDuration> periods, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate) {
		DayDuration nextValidDayDuration = null;

		for (final DayDuration period : periods) {
			if (null == endDate) {
				if (period.isInPeriod(checkedDate)) {
					nextValidDayDuration = period;
					break;
				}
			} else {
				if (period.isInPeriod(checkedDate, endDate)) {
					nextValidDayDuration = period;
					break;
				}
			}
		}
		if (null == nextValidDayDuration) {
			// this date isn't in a period, we try to found the next one
			nextValidDayDuration = this.getClosestForwardDayDuration(periods, checkedDate, endDate);
		}
		if (null == nextValidDayDuration) {
			return null;
		}

		// get the correct Day
		final DayOfWeek nextDayOfWeek;
		// if current time is before start we can use today else next valid day
		if (checkedDate.toLocalTime().isBefore(nextValidDayDuration.getStart().toLocalTime())) {
			nextDayOfWeek = this.getNextOrSameDayOfWeek(nextValidDayDuration, checkedDate);
		} else {
			nextDayOfWeek = this.getNextDayOfWeek(nextValidDayDuration, checkedDate);
		}

		final Boolean dayChanged = !checkedDate.getDayOfWeek().equals(nextDayOfWeek);

		if (dayChanged) {
			// we can use the first period in the periods as we are a new Day
			nextValidDayDuration = periods.get(0);
		}

		final LocalDate day = checkedDate.with(TemporalAdjusters.nextOrSame(nextDayOfWeek)).toLocalDate();
		final LocalDateTime nextValidLocalDate = LocalDateTime.of(day, nextValidDayDuration.getStart().toLocalTime());
		final ZonedDateTime nextValidDate = ZonedDateTime.of(nextValidLocalDate, checkedDate.getZone());
		LOG.info("Next Valid Date : " + nextValidDate);

		return nextValidDate;
	}

	/**
	 * Find the closest DayDuration (searching forward)
	 *
	 * @param periods
	 * @param checkedDate
	 * @return a valid Day Duration or null if none available
	 */
	private DayDuration getClosestForwardDayDuration(final List<DayDuration> periods, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate) {
		if (periods.size() == 1) {
			if (periods.get(0).isWeeklyerpetual()) {
				return periods.get(0);
			} else {
				return null;
			}
		}

		for (final DayDuration period : periods) {
			if (null == endDate) {
				if (period.isInValidFuture(checkedDate)) {
					return period;
				}
			} else {
				if (period.isInValidFuture(checkedDate, endDate)) {
					return period;
				}
			}
		}
		// the current hour is after each period, so the first one (tomorrow) is
		// the closest
		// FIXME Djer13 the firstOne may not be the good one if event is too
		// long for this period.
		// FIXME perpetual should be managed for the LIST, assuming here alla
		// period as same perpetuality of the first one
		if (periods.get(0).isWeeklyerpetual()) {
			return periods.get(0);
		} else {
			return null;
		}

	}

	private DayOfWeek getNextDayOfWeek(final DayDuration period, final ZonedDateTime checkedDate) {
		final DayOfWeek checkedDateDayOfWeek = checkedDate.getDayOfWeek();
		// search if a next day in week is available
		for (final DayOfWeek validDay : period.getValidDayOfWeek()) {
			if (validDay.getValue() > checkedDateDayOfWeek.getValue()) {
				return validDay;
			}
		}
		// No Next Day in week, the first validDay (next week) is the closest
		return period.getValidDayOfWeek().get(0);
	}

	private DayOfWeek getNextOrSameDayOfWeek(final DayDuration period, final ZonedDateTime checkedDate) {
		final DayOfWeek checkedDateDayOfWeek = checkedDate.getDayOfWeek();
		// search if a next day in week is available
		for (final DayOfWeek validDay : period.getValidDayOfWeek()) {
			if (validDay.getValue() >= checkedDateDayOfWeek.getValue()) {
				return validDay;
			}
		}
		// No Next Day in week, the first validDay (next week) is the closest
		return period.getValidDayOfWeek().get(0);
	}

	public static class DayOfWeekLists {
		public static final List<DayOfWeek> ALL_DAYS = new ArrayList<>(
				Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
						DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
		public static final List<DayOfWeek> STANDARD_WORK_DAYS = new ArrayList<>(Arrays.asList(DayOfWeek.MONDAY,
				DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

		public static final List<DayOfWeek> STANDARD_WEEKEND_DAYS = new ArrayList<>(
				Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
	}
}
