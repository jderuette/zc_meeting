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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.meeting.client.common.SlotHelper.DayOfWeekLists;

/**
 * represent a (list of) period(s) during one day. This day period can be
 * limited to some Week Day(s)
 *
 * @author djer13
 *
 */
public class DayDuration {

	private static final Logger LOG = LoggerFactory.getLogger(DayDuration.class);

	private final OffsetTime start;
	private final OffsetTime end;
	private final List<DayOfWeek> validDayOfWeek;
	private final Boolean weeklyerpetual;
	private final Boolean defaultFromSlot;

	public DayDuration(final OffsetTime start, final OffsetTime end) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = DayOfWeekLists.ALL_DAYS;
		this.weeklyerpetual = Boolean.TRUE;
		this.defaultFromSlot = Boolean.TRUE;
	}

	public DayDuration(final OffsetTime start, final OffsetTime end, final List<DayOfWeek> validDayOfWeek) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = validDayOfWeek;
		this.weeklyerpetual = Boolean.TRUE;
		this.defaultFromSlot = Boolean.TRUE;
	}

	public DayDuration(final OffsetTime start, final OffsetTime end, final List<DayOfWeek> validDayOfWeek,
			final Boolean weeklyerpetual) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = validDayOfWeek;
		this.weeklyerpetual = weeklyerpetual;
		this.defaultFromSlot = Boolean.TRUE;
	}

	public DayDuration(final OffsetTime start, final OffsetTime end, final List<DayOfWeek> validDayOfWeek,
			final Boolean weeklyerpetual, final Boolean isDefaultFromSlot) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = validDayOfWeek;
		this.weeklyerpetual = weeklyerpetual;
		this.defaultFromSlot = isDefaultFromSlot;
	}

	private Long getDuration(final ChronoUnit unit) {
		return this.start.until(this.end, unit);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	public Boolean isBeforeBegin(final LocalDateTime checkedDate) {
		Boolean result = Boolean.FALSE;
		if (this.validDayOfWeek.contains(checkedDate.getDayOfWeek())) {
			// compare to NOT before instead of after to handle equals Dates
			// (see : http://stackoverflow.com/a/13936632/8029150)
			if (!checkedDate.toLocalTime().isAfter(this.start.toLocalTime())) {
				result = Boolean.TRUE;
			}
		}
		return result;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private Boolean isAfterBegin(final LocalDateTime checkedDate) {
		Boolean result = Boolean.FALSE;
		if (this.validDayOfWeek.contains(checkedDate.getDayOfWeek())) {
			// compare to NOT before instead of after to handle equals Dates
			// (see : http://stackoverflow.com/a/13936632/8029150)
			if (!checkedDate.toLocalTime().isBefore(this.start.toLocalTime())) {
				result = Boolean.TRUE;
			}
		}
		return result;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private Boolean isBeforeEnd(final LocalDateTime checkedDate) {
		Boolean result = Boolean.FALSE;
		if (this.validDayOfWeek.contains(checkedDate.getDayOfWeek())) {
			// compare to NOT after instead of before to handle equals Dates
			// (see : http://stackoverflow.com/a/13936632/8029150)
			if (!checkedDate.toLocalTime().isAfter(this.end.toLocalTime())) {
				result = Boolean.TRUE;
			}
		}
		return result;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	public Boolean isAfterEnd(final LocalDateTime checkedDate) {
		Boolean result = Boolean.FALSE;
		if (this.validDayOfWeek.contains(checkedDate.getDayOfWeek())) {
			// compare to NOT after instead of before to handle equals Dates
			// (see : http://stackoverflow.com/a/13936632/8029150)
			if (!checkedDate.toLocalTime().isBefore(this.end.toLocalTime())) {
				result = Boolean.TRUE;
			}
		}
		return result;

	}

	public Boolean isInPeriod(final ZonedDateTime checkedDate) {
		Boolean isInperiod = Boolean.FALSE;
		final LocalDateTime localCheckdDateTime = checkedDate.toLocalDateTime();

		// Day of week is in on of the allowed Days
		if (this.validDayOfWeek.contains(checkedDate.getDayOfWeek())) {
			// Check if hours is between the start and end
			if (this.isAfterBegin(localCheckdDateTime)) {
				if (this.isBeforeEnd(localCheckdDateTime)) {
					isInperiod = Boolean.TRUE;
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(100).append(checkedDate)
								.append(" not valid beacuase is after period end ").append(this).toString());
					}
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(100).append(checkedDate)
							.append(" not valid beacuase is before period start ").append(this).toString());
				}
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(100).append(checkedDate).append('(').append(checkedDate.getDayOfWeek())
						.append(") is not in a valid day for period ").append(this).toString());
			}
		}

		return isInperiod;
	}

	public Boolean isInPeriod(final ZonedDateTime startDate, final ZonedDateTime endDate) {
		return this.isInPeriod(startDate) && this.isInPeriod(endDate);
	}

	public Boolean isInValidFuture(final ZonedDateTime checkedDate) {
		return this.isBeforeBegin(checkedDate.toLocalDateTime()) && this.isBeforeEnd(checkedDate.toLocalDateTime());
	}

	public Boolean isInValidFuture(final ZonedDateTime startDate, final ZonedDateTime endDate) {
		return this.isInValidFuture(startDate) && this.isInValidFuture(endDate);
	}

	public Boolean isPeriodPossible(final ZonedDateTime startDate, final ZonedDateTime endDate) {
		return this.isPeriodPossible(startDate, endDate, null);
	}

	public Boolean isPeriodPossible(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Double durationInMinutes) {
		Boolean isPeriodPossible = Boolean.FALSE;

		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		final long nbMinsDiff = dateHelper.getRelativeTimeShift(startDate, endDate, Boolean.TRUE, ChronoUnit.MINUTES);

		if (LOG.isDebugEnabled() && nbMinsDiff < durationInMinutes.intValue()) {
			LOG.debug(new StringBuilder(100).append("From ").append(startDate).append(" to ").append(endDate)
					.append(" is to short for a ").append(durationInMinutes).append(" meeting only ").append(nbMinsDiff)
					.append(" mins available").append(this).toString());
		}

		final Long minutesOverlaps = this.getTimeOverlap(startDate, endDate, durationInMinutes);
		if (minutesOverlaps < durationInMinutes) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(100).append("From ").append(startDate).append(" to ").append(endDate)
						.append(" is to short for a (DayDuration not enought long)").append(durationInMinutes)
						.append(" meeting only ").append(minutesOverlaps).append(" mins available").append(this)
						.toString());
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(100).append("From ").append(startDate).append(" to ").append(endDate)
						.append(' ').append(minutesOverlaps).append(" are available for meetings ").append(this)
						.toString());
			}
			isPeriodPossible = Boolean.TRUE;
		}

		return isPeriodPossible;
	}

	public Boolean hasTimeOverlap(final ZonedDateTime startDate, final ZonedDateTime endDate) {
		return this.hasTimeOverlap(startDate, endDate, null);
	}

	public Boolean hasTimeOverlap(final LocalDateTime startDate, final LocalDateTime endDate,
			final Double durationinMinutes) {
		return this.hasTimeOverlap(startDate.atZone(ZoneOffset.UTC), endDate.atZone(ZoneOffset.UTC), durationinMinutes);
	}

	public Boolean hasTimeOverlap(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Double durationinMinutes) {
		final Long amountOfTimeOverlap = this.getTimeOverlap(startDate, endDate, durationinMinutes, Boolean.TRUE);
		return amountOfTimeOverlap != 0L;
	}

	public Long getTimeOverlap(final LocalDateTime startDate, final LocalDateTime endDate) {
		return this.getTimeOverlap(startDate.atZone(ZoneOffset.UTC), endDate.atZone(ZoneOffset.UTC), null);
	}

	public Long getTimeOverlap(final LocalDateTime startDate, final LocalDateTime endDate, final Double duration,
			final Boolean stopWhenMeetingPossible) {
		return this.getTimeOverlap(startDate.atZone(ZoneOffset.UTC), endDate.atZone(ZoneOffset.UTC), duration,
				stopWhenMeetingPossible);
	}

	public Long getTimeOverlap(final LocalDateTime startDate, final LocalDateTime endDate, final Double duration) {
		return this.getTimeOverlap(startDate.atZone(ZoneOffset.UTC), endDate.atZone(ZoneOffset.UTC), duration);
	}

	public Long getTimeOverlap(final ZonedDateTime startDate, final ZonedDateTime endDate) {
		return this.getTimeOverlap(startDate, endDate, null);
	}

	public Long getTimeOverlap(final ZonedDateTime startDate, final ZonedDateTime endDate, final Double duration) {
		return this.getTimeOverlap(startDate, endDate, duration, Boolean.FALSE);
	}

	public Long getTimeOverlap(final ZonedDateTime startDate, final ZonedDateTime endDate, final Double duration,
			final Boolean stopWhenMeetingPossible) {
		final ChronoUnit unit = ChronoUnit.MINUTES;

		if (null != duration && this.getDuration(unit) < duration) {
			LOG.info(new StringBuilder().append("Period Duration (").append(this.getDuration(unit))
					.append(") is less than meeting duration (").append(duration).append("), no Hours available")
					.append(this).toString());
			return 0L;// early Break
		}

		Long timeOverlap = 0L;
		// Day of week is in on of the allowed Days
		final LocalDateTime localStartDateTime = startDate.toLocalDateTime();
		final LocalDateTime localEndDateTime = endDate.toLocalDateTime();

		LocalTime startPartialPeriod;
		LocalTime endPartialPeriod;

		if (!startDate.toLocalTime().isBefore(this.getStart().toLocalTime())) {
			startPartialPeriod = startDate.toLocalTime();
		} else {
			startPartialPeriod = this.getStart().toLocalTime();
		}

		if (!endDate.toLocalTime().isAfter(this.getEnd().toLocalTime())) {
			endPartialPeriod = endDate.toLocalTime();
		} else {
			endPartialPeriod = this.getEnd().toLocalTime();
		}

		if (this.isSameDay(localStartDateTime, this.getStart().atDate(endDate.toLocalDate()))) {
			if (this.validDayOfWeek.contains(localStartDateTime.getDayOfWeek())) {
				final Long maxTimeOverlap = startPartialPeriod.until(endPartialPeriod, unit);
				timeOverlap = this.getAvailableHours(maxTimeOverlap, duration);

				LOG.info(new StringBuilder().append("1 day TimeFrame : start ").append(startPartialPeriod)
						.append(" end ").append(endPartialPeriod).append(" total available : ").append(timeOverlap)
						.append(" on a max Time avialable of ").append(maxTimeOverlap).append(this).toString());
			}
		} else {
			// (partial) First Day + All days between Begin and End + (partial)
			// LastDay
			Long partialFirstDay = 0L;
			if (this.validDayOfWeek.contains(localStartDateTime.getDayOfWeek())) {
				final long maxPartialFirstDay = startPartialPeriod.until(this.getEnd().toLocalTime(), unit);
				partialFirstDay = this.getAvailableHours(maxPartialFirstDay, duration);
				if (stopWhenMeetingPossible && partialFirstDay >= duration) {
					LOG.info("Multiple days TimeFrame found avaible time in first (partial) day, stoping search");
					return partialFirstDay; // early Break
				}
			}
			Long partialLastDay = 0L;
			if (this.validDayOfWeek.contains(localEndDateTime.getDayOfWeek())) {
				final long maxPartialLastDay = this.getStart().toLocalTime().until(endPartialPeriod, unit);
				partialLastDay = this.getAvailableHours(maxPartialLastDay, duration);
				if (stopWhenMeetingPossible && partialLastDay >= duration) {
					LOG.info("Multiple days TimeFrame found avaible time in last (partial) day, stoping search");
					return partialLastDay; // early Break
				}
			}
			final LocalDate fullDayStart = startDate.toLocalDate().plus(1, ChronoUnit.DAYS);
			final LocalDate fullDayEnd = endDate.toLocalDate().minus(1, ChronoUnit.DAYS);
			if (stopWhenMeetingPossible) {
				final Long nbDays = fullDayStart.until(fullDayEnd, ChronoUnit.DAYS);
				if (nbDays >= 1) {
					LOG.info(
							"Multiple days TimeFrame found avaible time in a least ONE fullDay between min and max date, stoping search");
					return this.getDuration(ChronoUnit.MINUTES); // Early Break
				}
			}
			final Long fullDayOverlap = this.getTimeOverlapFullDay(fullDayStart, fullDayEnd);
			timeOverlap = partialFirstDay + fullDayOverlap + partialLastDay;
			LOG.info(new StringBuilder().append("Multiple days TimeFrame : start ").append(startPartialPeriod)
					.append(" end ").append(endPartialPeriod).append(" total available : ").append(timeOverlap)
					.append(", FirstDay : ").append(partialFirstDay).append(", fullDays : ").append(fullDayOverlap)
					.append(", LastDay : ").append(partialLastDay).append(this).toString());
		}
		return timeOverlap;

	}

	private Long getAvailableHours(final Long amountOfMinutes, final Double durationInMinutes) {
		Long timeOverlap = 0L;
		if (null == durationInMinutes) {
			timeOverlap = amountOfMinutes;
		} else if (amountOfMinutes >= durationInMinutes) {
			timeOverlap = amountOfMinutes;
			final long nbPossibleMeeting = Math.floorDiv(amountOfMinutes, durationInMinutes.longValue());
			timeOverlap = nbPossibleMeeting * durationInMinutes.longValue();

			LOG.info(new StringBuilder().append(nbPossibleMeeting).append(" meeting of ").append(durationInMinutes)
					.append(" mins are avaible in period, for a total of ").append(timeOverlap).append(" minutes")
					.append(this).toString());
		} else {
			LOG.info(new StringBuilder().append(timeOverlap).append(" minutes are available for meetings").append(this)
					.toString());
		}

		return timeOverlap;
	}

	/**
	 * get Nb Minutes between start and end date for this DayDuration.
	 *
	 * @param startDate
	 * @param endDate
	 * @return 0 min (if WeekDay not valid) or the full duration
	 */
	public Long getTimeOverlapFullDay(final LocalDate startDate, final LocalDate endDate) {
		Long fullDayOverlap = 0L;
		LocalDate nextDayBetweenMinAndMax = startDate;
		while (!nextDayBetweenMinAndMax.isAfter(endDate)) {
			fullDayOverlap += this.getTimeOverlapFullDay(nextDayBetweenMinAndMax);
			nextDayBetweenMinAndMax = nextDayBetweenMinAndMax.plus(1, ChronoUnit.DAYS);
		}
		return fullDayOverlap;
	}

	/**
	 * get NbMinutes available for this DayDuration.
	 *
	 * @param localDate
	 * @return 0 min (if WeekDay not valid) or the full duration
	 */
	public Long getTimeOverlapFullDay(final LocalDate localDate) {
		Long fullDayTimeOverlap = 0L;
		if (this.validDayOfWeek.contains(localDate.getDayOfWeek())) {
			fullDayTimeOverlap = this.getStart().until(this.getEnd(), ChronoUnit.MINUTES);
		}
		return fullDayTimeOverlap;
	}

	private boolean isSameDay(final LocalDateTime localDateTime, final OffsetDateTime atDate) {
		return this.isSameDay(localDateTime.toLocalDate(), atDate.toLocalDate());
	}

	public Boolean isSameDay(final LocalDate startDate, final LocalDate endDate) {
		return startDate.equals(endDate);
	}

	public DayOfWeek getClosestDayOfWeek(final ZonedDateTime checkedDate) {
		final DayOfWeek nextDayOfWeek;
		// if current time is before start we can use today else next valid day
		if (checkedDate.toLocalTime().isBefore(this.getStart().toLocalTime())) {
			nextDayOfWeek = this.getNextOrSameDayOfWeek(checkedDate);
		} else {
			nextDayOfWeek = this.getNextDayOfWeek(checkedDate);
		}

		return nextDayOfWeek;
	}

	public DayOfWeek getNextOrSameDayOfWeek(final ZonedDateTime checkedDate) {
		final DayOfWeek checkedDateDayOfWeek = checkedDate.getDayOfWeek();
		// search if a next day in week is available
		for (final DayOfWeek validDay : this.getValidDayOfWeek()) {
			if (validDay.getValue() >= checkedDateDayOfWeek.getValue()) {
				return validDay;
			}
		}
		// No Next Day in week, the first validDay (next week) is the closest
		// FIXME if NO valid Day OfWeek for this period ?
		return this.getValidDayOfWeek().get(0);
	}

	public DayOfWeek getNextDayOfWeek(final ZonedDateTime checkedDate) {
		if (this.getValidDayOfWeek().size() == 0) {
			LOG.debug("No valid day aviallable (desactivited) in : " + this);
			return null; // early Break;
		}
		final DayOfWeek checkedDateDayOfWeek = checkedDate.getDayOfWeek();
		// search if a next day in week is available
		for (final DayOfWeek validDay : this.getValidDayOfWeek()) {
			if (validDay.getValue() > checkedDateDayOfWeek.getValue()) {
				return validDay;
			}
		}
		// No Next Day in week, the first validDay (next week) is the closest
		return this.getValidDayOfWeek().get(0);
	}

	public LocalDateTime getNextOrSameDate(final ZonedDateTime checkedDate) {
		final DayOfWeek nextDayOfWeek = this.getClosestDayOfWeek(checkedDate);
		if (null == nextDayOfWeek) {
			return null; // early break
		}

		LocalDate day = null;

		if (nextDayOfWeek == checkedDate.getDayOfWeek()) {
			if (this.isBeforeBegin(checkedDate.toLocalDateTime())) {
				day = checkedDate.with(TemporalAdjusters.nextOrSame(nextDayOfWeek)).toLocalDate();
			} else {
				day = checkedDate.with(TemporalAdjusters.next(nextDayOfWeek)).toLocalDate();
			}
		} else {
			day = checkedDate.with(TemporalAdjusters.next(nextDayOfWeek)).toLocalDate();
		}

		final LocalDateTime nextValidLocalDate = LocalDateTime.of(day, this.getStart().toLocalTime());

		return nextValidLocalDate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(75);
		builder.append("DayDuration [start=").append(this.start).append(", end=").append(this.end)
				.append(", validDayOfWeek=").append(this.validDayOfWeek).append(']');
		return builder.toString();
	}

	public OffsetTime getStart() {
		return this.start;
	}

	public OffsetTime getEnd() {
		return this.end;
	}

	public List<DayOfWeek> getValidDayOfWeek() {
		return this.validDayOfWeek;
	}

	public Boolean isWeeklyerpetual() {
		return this.weeklyerpetual;
	}

	public Boolean isDefaultFromSlot() {
		return this.defaultFromSlot;
	}
}
