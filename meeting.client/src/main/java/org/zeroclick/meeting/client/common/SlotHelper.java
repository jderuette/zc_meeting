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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;

/**
 * @author djer
 *
 */
public class SlotHelper {
	private static final Logger LOG = LoggerFactory.getLogger(SlotHelper.class);

	private static SlotHelper instance;
	private static ISlotService slotService;

	private SlotHelper(final Boolean instanciateRemoteServiceFacade) {
		// Singleton
		if (instanciateRemoteServiceFacade && null == slotService) {
			slotService = BEANS.get(ISlotService.class);
		}
	}

	public static SlotHelper get() {
		if (null == instance) {
			instance = new SlotHelper(Boolean.TRUE);
		}
		return instance;
	}

	public static SlotHelper get(final Boolean instanciateRemoteServiceFacade) {
		if (null == instance) {
			instance = new SlotHelper(instanciateRemoteServiceFacade);
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

	/**
	 * Avoid using this method, a date (instant) is required to get actual user
	 * periods.
	 *
	 * @see getUserPeriods(final Long slotId, final Long userId, ZonedDateTime
	 *      forDate)
	 * @param slotId
	 * @param userId
	 * @return
	 */
	private List<DayDuration> getUserPeriods(final Long slotId, final Long userId) {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final ZoneId userZoneId = appUserHelper.getUserZoneId(userId);
		return this.getUserPeriods(slotId, userId, ZonedDateTime.now(userZoneId));
	}

	private List<DayDuration> getUserPeriods(final Long slotId, final Long userId, final ZonedDateTime forDate) {
		List<DayDuration> daysDurations = new ArrayList<>();

		final String slotName = "zc.meeting.slot." + slotId;

		final List<DayDurationFormData> daysDurationsData = slotService.getDayDurations(slotName, userId);

		if (null == daysDurationsData || daysDurationsData.isEmpty()) {
			// return default slots Data
			daysDurations = this.getPeriods(slotId);
		} else {
			final Calendar startCal = new GregorianCalendar();
			final Calendar endCal = new GregorianCalendar();

			final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
			final ZoneId userZoneId = appUserHelper.getUserZoneId(userId);

			for (final DayDurationFormData dayDurationData : daysDurationsData) {
				final Date startTime = dayDurationData.getSlotStart().getValue();
				final Date endTime = dayDurationData.getSlotEnd().getValue();

				startCal.setTime(startTime);
				endCal.setTime(endTime);

				daysDurations.add(new DayDuration(this.toUserOffsetTime(startCal, userZoneId),
						this.toUserOffsetTime(endCal, userZoneId), this.buildListOfWeekDay(dayDurationData)));
			}
		}

		return daysDurations;
	}

	public OffsetTime toUserOffsetTime(final Calendar dateCal, final ZoneId userZoneId) {
		return OffsetTime.of(dateCal.get(Calendar.HOUR_OF_DAY), dateCal.get(Calendar.MINUTE), 0, 0,
				userZoneId.getRules().getOffset(dateCal.toInstant()));
	}

	private List<DayOfWeek> buildListOfWeekDay(final DayDurationFormData dayDurationData) {
		final List<DayOfWeek> validDays = new ArrayList<>();

		if (dayDurationData.getMonday().getValue()) {
			validDays.add(DayOfWeek.MONDAY);
		}
		if (dayDurationData.getTuesday().getValue()) {
			validDays.add(DayOfWeek.TUESDAY);
		}
		if (dayDurationData.getWednesday().getValue()) {
			validDays.add(DayOfWeek.WEDNESDAY);
		}
		if (dayDurationData.getThursday().getValue()) {
			validDays.add(DayOfWeek.THURSDAY);
		}
		if (dayDurationData.getFriday().getValue()) {
			validDays.add(DayOfWeek.FRIDAY);
		}
		if (dayDurationData.getSaturday().getValue()) {
			validDays.add(DayOfWeek.SATURDAY);
		}
		if (dayDurationData.getSunday().getValue()) {
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
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId, checkedDate);

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
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId, startDate);

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
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId, checkedDate);

		return this.getNextValidDateTime(periods, checkedDate, null);
	}

	public ZonedDateTime getNextValidDateTime(final Long slotId, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate, final Long userId) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId, checkedDate);

		return this.getNextValidDateTime(periods, checkedDate, endDate);
	}

	public ZonedDateTime getNextValidDateTime(final List<DayDuration> periods, final ZonedDateTime checkedDate) {
		return this.getNextValidDateTime(periods, checkedDate, null);
	}

	public ZonedDateTime getNextValidDateTime(final List<DayDuration> periods, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate) {
		LocalDateTime nextValidLocalDate = null;

		for (final DayDuration period : periods) {
			if (null == endDate) {
				if (period.isInPeriod(checkedDate)) {
					nextValidLocalDate = period.getNextOrSameDate(checkedDate);
					break;
				}
			} else {
				if (period.isInPeriod(checkedDate, endDate)) {
					nextValidLocalDate = period.getNextOrSameDate(checkedDate);
					break;
				}
			}
		}
		if (null == nextValidLocalDate) {
			nextValidLocalDate = this.getClosestForwardDate(periods, checkedDate, endDate);
		}
		if (null == nextValidLocalDate) {
			return null;
		}

		final ZonedDateTime nextValidZonedDate = ZonedDateTime.of(nextValidLocalDate, checkedDate.getZone());
		LOG.info("Next Valid Date : " + nextValidZonedDate);

		return nextValidZonedDate;
	}

	/**
	 * Find the closest DayDuration (searching forward)
	 *
	 * @param periods
	 * @param checkedDate
	 * @return a valid Day Duration or null if none available
	 */
	private LocalDateTime getClosestForwardDate(final List<DayDuration> periods, final ZonedDateTime checkedDate,
			final ZonedDateTime endDate) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Searching for closest forward period from : ").append(checkedDate)
					.append(" to ").append(endDate).append(" in periods : ").append(periods).toString());
		}
		if (periods.size() == 1) {
			if (periods.get(0).isWeeklyerpetual()) {
				return periods.get(0).getNextOrSameDate(checkedDate);
			} else {
				return null;
			}
		}

		for (final DayDuration period : periods) {
			if (period.isWeeklyerpetual()) {
				if (null == endDate) {
					if (period.isInValidFuture(checkedDate)) {
						return period.getNextOrSameDate(checkedDate);
					}
				} else {
					if (period.isInValidFuture(checkedDate, endDate)) {
						return period.getNextOrSameDate(checkedDate);
					}
				}
			}
		}

		// No period are valid "today" searching for the closest next valid day
		final LocalDateTime nextClosestsDate = this.getNextDate(periods, checkedDate);

		return nextClosestsDate;
	}

	private LocalDateTime getNextDate(final List<DayDuration> periods, final ZonedDateTime checkedDate) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Searching for closest forward date for : ").append(checkedDate)
					.append(" in periods : ").append(periods).toString());
		}
		LocalDateTime closestDate = null;
		final Map<DayDuration, LocalDateTime> nextValidDates = new HashMap<>();

		final Iterator<DayDuration> itPeriod = periods.iterator();

		while (itPeriod.hasNext()) {
			final DayDuration period = itPeriod.next();
			if (period.isWeeklyerpetual()) {
				nextValidDates.put(period, period.getNextOrSameDate(checkedDate));
			}
		}

		for (final DayDuration period : nextValidDates.keySet()) {
			final LocalDateTime periodDate = nextValidDates.get(period);
			if (null != periodDate && (null == closestDate || periodDate.isBefore(closestDate))) {
				if (LOG.isDebugEnabled()) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final StringBuilder builder = new StringBuilder();
					LOG.debug(builder.append(periodDate).append(" is Before ").append(closestDate)
							.append(" in period : ").append(period).toString());
				}
				closestDate = periodDate;
			}
		}

		return closestDate;
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
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		// TODO Djer hour are checked using 'locaDateTime" so we Should use
		// localized DateTime. Double check the Waring bellow !
		// WARNING we NEED to use UTC
		final ZonedDateTime zonedStart = dateHelper.getZonedValue(appUserHelper.getUserZoneId(userId), minimalDate);
		final ZonedDateTime zonedEnd = dateHelper.getZonedValue(appUserHelper.getUserZoneId(userId), maximalDate);

		final long nbDayDiff = dateHelper.getRelativeTimeShift(zonedStart, zonedEnd, Boolean.TRUE, ChronoUnit.DAYS);

		if (nbDayDiff >= 7) {
			hasMatchingDay = Boolean.TRUE;
		} else {

			final Set<DayOfWeek> avialableDaysInSlot = this.getDays(slotId, userId, zonedStart);
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

	private Set<DayOfWeek> getDays(final Long slotId, final Long userId, final ZonedDateTime checkedDate) {
		final List<DayDuration> periods = this.getUserPeriods(slotId, userId, checkedDate);
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
			final Long currentUserId, final Double durationinMinutes) {

		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		// WARNING we NEED to use UTC
		final ZonedDateTime zonedStart = dateHelper.getZonedValue(ZoneId.of("UTC"), minimalDate);
		final ZonedDateTime zonedEnd = dateHelper.getZonedValue(ZoneId.of("UTC"), maximalDate);

		final List<DayDuration> periods = this.getUserPeriods(slot, currentUserId, zonedStart);

		for (final DayDuration period : periods) {
			if (period.hasTimeOverlap(zonedStart, zonedEnd, durationinMinutes)) {
				return Boolean.TRUE; // earlyBreak
			}
		}

		return Boolean.FALSE;
	}

	public Boolean slotCanMatchDuration(final Long slot, final Long currentUserId, final Double durationinMinutes) {
		final List<DayDuration> periods = this.getUserPeriods(slot, currentUserId);

		for (final DayDuration period : periods) {
			if (period.getDuration(ChronoUnit.MINUTES) >= durationinMinutes) {
				return Boolean.TRUE; // early break
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
