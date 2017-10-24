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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class DateHelper {
	private static final Logger LOG = LoggerFactory.getLogger(DateHelper.class);

	/**
	 * @deprecated use toZonedDateTime(...) instead
	 * @param date
	 * @return
	 */
	@Deprecated
	public LocalDateTime toLocalDateTime(final Date date) {
		LocalDateTime localDateTime = null;
		if (null != date) {
			localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		}

		return localDateTime;
	}

	/**
	 * deprecated use toDate(ZonedDateTime) instead
	 *
	 * @deprecated
	 * @param localDateTime
	 * @return
	 */
	@Deprecated
	public Date toDate(final LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public ZonedDateTime getZonedValue(final ZoneId userZoneId, final Date date) {
		ZonedDateTime zdt = null;
		if (null != date) {
			zdt = ZonedDateTime.ofInstant(date.toInstant(), userZoneId);
		}
		return zdt;
	}

	public Date toDate(final ZonedDateTime zonedDateTime) {
		Date date = null;
		if (null != zonedDateTime) {
			date = Date.from(zonedDateTime.toInstant());
		}
		return date;
	}

	public Date toUtcDate(final ZonedDateTime zonedDateTime) {
		Date date = null;
		if (null != zonedDateTime) {
			date = Date.from(zonedDateTime.toInstant());
		}
		return date;
	}

	public Date toUtcDate(final Date dateFromUser) {
		final int dateMinutOeffsetFromUtc = dateFromUser.getTimezoneOffset();
		Date utcDate = dateFromUser;
		if (dateMinutOeffsetFromUtc == 0) {
			// Already UTC Time
		} else {
			final ZonedDateTime zdt = ZonedDateTime.from(dateFromUser.toInstant());
			zdt.plusMinutes(dateMinutOeffsetFromUtc);
			utcDate = Date.from(zdt.toInstant());
		}
		return utcDate;
	}

	public Date nowUtc() {
		final Instant instant = Instant.now();
		final OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
		final Date nowUtc = Date.from(odt.toInstant());
		return nowUtc;	
	}

	public Date toUserDate(final ZonedDateTime zonedDateTime) {
		Date date = null;
		if (null != zonedDateTime) {
			date = Date.from(zonedDateTime.plusSeconds(zonedDateTime.getOffset().getTotalSeconds()).toInstant());
		}
		return date;
	}

	public String format(final Date date, final ZoneId userZoneId) {
		return this.format(this.getZonedValue(userZoneId, this.toUtcDate(date)));
	}

	public String format(final ZonedDateTime zoneDateTime) {
		return this.format(zoneDateTime, Boolean.FALSE);
	}

	public String format(final ZonedDateTime zonedDateTime, final Boolean ignoreHours) {
		DateFormat dateFormat;
		if (ignoreHours) {
			dateFormat = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.SHORT, NlsLocale.get());
		} else {
			dateFormat = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
					NlsLocale.get());
		}

		return dateFormat.format(this.toUserDate(zonedDateTime));
	}

	public String formatHours(final Date date, final ZoneId userZoneId) {
		return this.formatHours(this.getZonedValue(userZoneId, date));
	}

	private String formatHours(final ZonedDateTime zonedDateTime) {
		final DateFormat dateFormat = BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT,
				NlsLocale.get());

		return dateFormat.format(this.toUserDate(zonedDateTime));
	}

	public String getRelativeDay(final Date value, final ZoneId zoneId) {
		return this.getRelativeDay(this.getZonedValue(zoneId, value));
	}

	public String getRelativeDay(final ZonedDateTime zonedValue) {
		final String key = this.getRelativeDateKey(zonedValue, Boolean.TRUE);
		// the current date is required only when fallback to absolute happen
		// (aka: nb day shift is not a "named" day).
		final String value = TEXTS.get(key, this.format(zonedValue, Boolean.TRUE));
		return value;
	}

	/**
	 * Build a relative day string for localization<br />
	 * You should NOT use this method @see getRelativeDay(ZonedDateTime) wich is
	 * better <br />
	 * Basis :
	 * {prefix}.{relative|absolute}.{period(Days)}[.distanceString]<br />
	 * Sample : <br />
	 * zc.common.date.relative.Days.yesterday (the next day)<br/>
	 * zc.common.date.relative.Days.today (the current day)<br/>
	 * zc.common.date.relative.Days.tomorrow (the previous day)<br/>
	 *
	 * zc.common.date.absolute.Days (when not a "named" day)<br/>
	 *
	 * Note : the time part of the ZoneDateTime is NOT used for key, only to get
	 * distance.
	 *
	 * @param zonedDateTime
	 *            a date time to calculate the "distance" from now
	 * @param byFullTimeSlot,
	 *            if TRUE, a day is from 00:00 to 00:00, else a day is 24h from
	 *            "now"
	 * @return a string to use in as translation key
	 */
	public String getRelativeDateKey(final ZonedDateTime zonedDateTime, final Boolean byFullTimeSlot) {

		final Map<Long, String> nbDayToString = new HashMap<>();

		nbDayToString.put(-1l, "yesterday");
		nbDayToString.put(0l, "today");
		nbDayToString.put(1l, "tomorrow");

		final long nbDays = this.getRelativeTimeShift(zonedDateTime, byFullTimeSlot, ChronoUnit.DAYS);
		final String nbDaysString;

		final StringBuilder builder = new StringBuilder(64);
		builder.append("zc.common.date");

		if (nbDayToString.containsKey(nbDays)) {
			nbDaysString = nbDayToString.get(nbDays);
			builder.append(".relative.").append(ChronoUnit.DAYS.toString()).append('.').append(nbDaysString);
		} else {
			// TODO Djer13 allow configuration ? (use absolute date, or relative
			// + number)
			builder.append(".absolute.").append(ChronoUnit.DAYS.toString());
		}

		return builder.toString();

	}

	/**
	 * unit of time between now and end date. /!\ <ith byFullTimeSlot, if now is
	 * in the "current" slot 0 is return else the number of slot between now and
	 * end date (java API differ, for "day" if there is less than 24hours, day
	 * diff will return 0)
	 *
	 * @param end
	 * @param byFullTimeSlot
	 * @param unit
	 * @return
	 */
	public long getRelativeTimeShift(final ZonedDateTime end, final Boolean byFullTimeSlot, final ChronoUnit unit) {
		final ZonedDateTime start = this.getStartPoint(end, byFullTimeSlot, unit);
		final long secondesShift = start.until(end, ChronoUnit.SECONDS);
		BigDecimal unitShift;
		switch (unit) {
		case DAYS:
			// this algo dosen't works well for "today", early test
			if (start.toLocalDate().equals(end.toLocalDate())) {
				unitShift = BigDecimal.ZERO;
			} else {
				RoundingMode roundingMode;
				if (secondesShift > 0) {
					// can't be today, so we can safely round down to remove one
					// "partial" day
					roundingMode = RoundingMode.DOWN;
				} else {
					roundingMode = RoundingMode.UP;
				}

				unitShift = BigDecimal.valueOf(secondesShift)
						.divide(BigDecimal.valueOf(ChronoUnit.DAYS.getDuration().getSeconds()), 0, roundingMode);
			}
			break;
		default:
			LOG.warn(unit + " not supported");
			unitShift = BigDecimal.ZERO;
			break;
		}
		return unitShift.longValue();
	}

	public ZonedDateTime getStartPoint(final ZonedDateTime zonedDateTime, final Boolean byFullTimeSlot,
			final ChronoUnit unit) {

		if (!unit.equals(ChronoUnit.DAYS)) {
			throw new UnsupportedOperationException("Only ChronoUnit.DAYS suported");
		}

		final ZonedDateTime now = ZonedDateTime.now(zonedDateTime.getZone());

		ZonedDateTime startPoint;

		if (byFullTimeSlot) {
			startPoint = now.toLocalDate().atStartOfDay(zonedDateTime.getZone());
		} else {
			startPoint = now;
		}

		return startPoint;
	}

}
