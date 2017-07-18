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
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	public DayDuration(final OffsetTime start, final OffsetTime end) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = DayOfWeekLists.ALL_DAYS;
		this.weeklyerpetual = Boolean.TRUE;

	}

	public DayDuration(final OffsetTime start, final OffsetTime end, final List<DayOfWeek> validDayOfWeek) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = validDayOfWeek;
		this.weeklyerpetual = Boolean.TRUE;
	}

	public DayDuration(final OffsetTime start, final OffsetTime end, final List<DayOfWeek> validDayOfWeek,
			final Boolean weeklyerpetual) {
		this.start = start;
		this.end = end;
		this.validDayOfWeek = validDayOfWeek;
		this.weeklyerpetual = weeklyerpetual;
	}

	private Boolean isBeforeBegin(final LocalDateTime checkedDate) {
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

	private Boolean isAfterEnd(final LocalDateTime checkedDate) {
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
						final StringBuilder builder = new StringBuilder(100);
						builder.append(checkedDate).append(" not valid beacuase is after period end ").append(this);
						LOG.debug(builder.toString());
					}
				}
			} else {
				if (LOG.isDebugEnabled()) {
					final StringBuilder builder = new StringBuilder(100);
					builder.append(checkedDate).append(" not valid beacuase is before period start ").append(this);
					LOG.debug(builder.toString());
				}
			}
		} else {
			if (LOG.isDebugEnabled()) {
				final StringBuilder builder = new StringBuilder(100);
				builder.append(checkedDate).append('(').append(checkedDate.getDayOfWeek())
						.append(") is not in a valid day for period ").append(this);
				LOG.debug(builder.toString());
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

}
