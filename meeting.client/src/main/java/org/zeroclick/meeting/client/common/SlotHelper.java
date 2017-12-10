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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.configuration.shared.slot.ISlotService;

/**
 * @author djer
 *
 */
public class SlotHelper {
	private static final Logger LOG = LoggerFactory.getLogger(SlotHelper.class);

	private static SlotHelper instance;
	private static ISlotService slotService;

	private SlotHelper() {
		// Singleton
		if (null == slotService) {
			slotService = BEANS.get(ISlotService.class);
		}
	}

	public static SlotHelper get() {
		if (null == instance) {
			instance = new SlotHelper();
		}
		return instance;
	}

	private final List<DayDuration> PERIODE_WORK = new ArrayList<>(Arrays.asList(
			new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WORK_DAYS, Boolean.TRUE, Boolean.TRUE),
			new DayDuration(OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WORK_DAYS, Boolean.TRUE, Boolean.TRUE)));

	private final List<DayDuration> PERIODE_LUNCH = new ArrayList<>(Arrays.asList(
			new DayDuration(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WORK_DAYS, Boolean.TRUE, Boolean.TRUE)));

	private final List<DayDuration> PERIODE_NIGHT = new ArrayList<>(Arrays.asList(new DayDuration(
			OffsetTime.of(20, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(23, 59, 59, 999999999, ZoneOffset.UTC),
			DayOfWeekLists.STANDARD_WORK_DAYS, Boolean.TRUE, Boolean.TRUE)));

	private final List<DayDuration> PERIODE_WEEK_END = new ArrayList<>(Arrays.asList(
			new DayDuration(OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WEEKEND_DAYS, Boolean.TRUE, Boolean.TRUE),
			new DayDuration(OffsetTime.of(14, 0, 0, 0, ZoneOffset.UTC), OffsetTime.of(18, 0, 0, 0, ZoneOffset.UTC),
					DayOfWeekLists.STANDARD_WEEKEND_DAYS, Boolean.TRUE, Boolean.TRUE)));

	private List<DayDuration> getPeriods(final Long durationId) {
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

	private List<DayDuration> getUserPeriods(final Long slotId, final Long userId) {
		List<DayDuration> daysDurations = new ArrayList<>();

		final String slotName = "zc.meeting.slot." + slotId;

		final Object[][] daysDurationsData = slotService.getDayDurations(slotName, userId);

		if (null == daysDurationsData || daysDurationsData.length == 0) {
			// return default slots Data
			daysDurations = this.getPeriods(slotId);
		} else {
			final Calendar startCal = new GregorianCalendar();
			final Calendar endCal = new GregorianCalendar();

			for (final Object[] dayDurationData : daysDurationsData) {
				final Date startTime = (Date) dayDurationData[2];
				final Date endTime = (Date) dayDurationData[3];

				startCal.setTime(startTime);
				endCal.setTime(endTime);

				daysDurations.add(new DayDuration(
						OffsetTime.of(startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE), 0, 0,
								ZoneOffset.UTC),
						OffsetTime.of(endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE), 0, 0,
								ZoneOffset.UTC),
						this.buildListOfWeekDay(dayDurationData)));
			}
		}

		return daysDurations;
	}

	private List<DayOfWeek> buildListOfWeekDay(final Object[] dayDurationData) {
		final List<DayOfWeek> validDays = new ArrayList<>();

		final Boolean monday = (Boolean) dayDurationData[4];
		final Boolean tuesday = (Boolean) dayDurationData[5];
		final Boolean wednesday = (Boolean) dayDurationData[6];
		final Boolean thursday = (Boolean) dayDurationData[7];
		final Boolean friday = (Boolean) dayDurationData[8];
		final Boolean saturday = (Boolean) dayDurationData[9];
		final Boolean sunday = (Boolean) dayDurationData[10];

		if (monday) {
			validDays.add(DayOfWeek.MONDAY);
		}
		if (tuesday) {
			validDays.add(DayOfWeek.TUESDAY);
		}
		if (wednesday) {
			validDays.add(DayOfWeek.WEDNESDAY);
		}
		if (thursday) {
			validDays.add(DayOfWeek.THURSDAY);
		}
		if (friday) {
			validDays.add(DayOfWeek.FRIDAY);
		}
		if (saturday) {
			validDays.add(DayOfWeek.SATURDAY);
		}
		if (sunday) {
			validDays.add(DayOfWeek.SUNDAY);
		}
		return validDays;
	}

	/**
	 * Check if the date is in of the periods of the duration List (by ID)
	 *
	 * @param slotId
	 * @param checkedDate
	 * @return
	 */
	public Boolean isInOneOfPeriods(final Long slotId, final ZonedDateTime checkedDate, final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId);

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
	public Boolean isInOneOfPeriods(final Long slotId, final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId);

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

	public ZonedDateTime getNextValidDateTime(final Long slotId, final ZonedDateTime checkedDate, final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId);

		return this.getNextValidDateTime(periods, checkedDate, null);
	}

	public ZonedDateTime getNextValidDateTime(final Long slotId, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate, final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId);

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
		// FIXME perpetual should be managed for the LIST, assuming here all
		// periods have same perpetuality of the first one
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

	/**
	 * Check if week days allow at least one week day in the selected slot
	 *
	 * @param minimalDate
	 * @param maximalDate
	 * @param slotId
	 * @param userId
	 * @return
	 */
	public boolean hasMatchingDays(final Date minimalDate, final Date maximalDate, final Long slotId,
			final Long userId) {
		Boolean hasMatchingDay = Boolean.FALSE;

		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		// WARNING we NEED to use UTC
		final ZonedDateTime zonedStart = dateHelper.getZonedValue(ZoneId.of("UTC"), minimalDate);
		final ZonedDateTime zonedEnd = dateHelper.getZonedValue(ZoneId.of("UTC"), maximalDate);

		final long nbDayDiff = dateHelper.getRelativeTimeShift(zonedStart, zonedEnd, Boolean.TRUE, ChronoUnit.DAYS);

		if (nbDayDiff >= 7) {
			hasMatchingDay = Boolean.TRUE;
		} else {

			final Set<DayOfWeek> avialableDaysInSlot = this.getDays(slotId, userId);
			if (avialableDaysInSlot.contains(zonedStart.getDayOfWeek())
					|| avialableDaysInSlot.contains(zonedEnd.getDayOfWeek())) {
				hasMatchingDay = Boolean.TRUE;
			} else if (nbDayDiff > 1) {
				// min and max are not the same day
				ZonedDateTime nextDayBetweenMinAndMax = zonedStart.plus(1, ChronoUnit.DAYS);
				while (!nextDayBetweenMinAndMax.isAfter(zonedEnd)) {
					if (avialableDaysInSlot.contains(nextDayBetweenMinAndMax.getDayOfWeek())) {
						hasMatchingDay = Boolean.TRUE;
						break;
					}
					nextDayBetweenMinAndMax = nextDayBetweenMinAndMax.plus(1, ChronoUnit.DAYS);
				}
			}
		}
		return hasMatchingDay;
	}

	private Set<DayOfWeek> getDays(final Long slotId, final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId);
		final Set<DayOfWeek> unicDaysInPeriod = new HashSet<>();

		for (final DayDuration period : periods) {
			for (final DayOfWeek day : period.getValidDayOfWeek()) {
				unicDaysInPeriod.add(day);
			}
		}
		return unicDaysInPeriod;
	}

	/**
	 * Check if the selected Hours allow some slot in the selected Slot
	 *
	 * @param minimalDate
	 * @param maximalDate
	 * @param slot
	 * @param currentUserId
	 * @return
	 */
	public boolean hasMatchingHours(final Date minimalDate, final Date maximalDate, final Long slot,
			final Long currentUserId, final Long duration) {

		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		// WARNING we NEED to use UTC
		final ZonedDateTime zonedStart = dateHelper.getZonedValue(ZoneId.of("UTC"), minimalDate);
		final ZonedDateTime zonedEnd = dateHelper.getZonedValue(ZoneId.of("UTC"), maximalDate);

		final List<DayDuration> periods = this.getUserPeriods(slot, currentUserId);

		for (final DayDuration period : periods) {
			if (period.hasTimeOverlap(zonedStart, zonedEnd, duration)) {
				return Boolean.TRUE; // earlyBreak
			}
		}

		return Boolean.FALSE;
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
