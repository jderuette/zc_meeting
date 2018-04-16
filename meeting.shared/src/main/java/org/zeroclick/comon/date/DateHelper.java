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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.user.AppUserHelper;

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

	public UTCDate toScoutUTCDate(final ZonedDateTime zonedDateTime) {
		UTCDate date = null;
		if (null != zonedDateTime) {
			date = TypeCastUtility.castValue(Date.from(zonedDateTime.toInstant()), UTCDate.class);
		}
		return date;
	}

	public Date toUtcDate(final Date dateFromUser) {
		final int dateMinutOeffsetFromUtc = dateFromUser.getTimezoneOffset();
		Date utcDate = dateFromUser;
		if (dateMinutOeffsetFromUtc != 0) {
			ZonedDateTime zdt = ZonedDateTime.ofInstant(dateFromUser.toInstant(), ZoneId.of("Z"));
			zdt = zdt.plusMinutes(dateMinutOeffsetFromUtc);
			utcDate = Date.from(zdt.toInstant());
		}
		return utcDate;
	}

	/**
	 * Convert a localized userDate time to is equivalent UTC time. <br/>
	 * <b>WARNING</b> the returned date as a different representation
	 *
	 * @param dateFromUser
	 * @param zoneId
	 * @return the dateFromUser transformed to UTC date (same instant in time,
	 *         but different time).
	 */
	public Date toUtcDate(final Date dateFromUser, final ZoneId zoneId) {
		final ZonedDateTime utcZonedDateTime = this.getZonedValue(ZoneOffset.UTC, dateFromUser);
		final ZonedDateTime userZonedDateTime = utcZonedDateTime.withZoneSameLocal(zoneId);
		final Date utcDate = this.toDate(userZonedDateTime);
		return utcDate;
	}

	/**
	 * Convert a UTC time to is equivalent User localized time. <br/>
	 * <b>WARNING</b> the returned date as a different representation
	 *
	 * @param utcDate
	 * @param zoneId
	 * @return the dateFromUser transformed to UTC date (same instant in time,
	 *         but different time).
	 */
	public Date fromUtcDate(final Date utcDate, final ZoneId zoneId) {
		final ZonedDateTime userZonedDateTime = this.getZonedValue(ZoneOffset.UTC, utcDate);
		final ZonedDateTime utcZonedDateTime = userZonedDateTime.withZoneSameLocal(zoneId);
		final Date userDateTime = this.toDate(utcZonedDateTime);
		return userDateTime;
	}

	public Date nowUtc() {
		final Instant instant = Instant.now();
		final OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
		final Date nowUtc = Date.from(odt.toInstant());
		return nowUtc;
	}

	public Date convertToUtcDate(final ZonedDateTime zonedDateTime) {
		Date date = null;
		if (null != zonedDateTime) {
			date = Date.from(zonedDateTime.minusSeconds(zonedDateTime.getOffset().getTotalSeconds()).toInstant());
		}
		return date;
	}

	public Date toUserDate(final ZonedDateTime zonedDateTime) {
		Date date = null;
		if (null != zonedDateTime) {
			date = Date.from(zonedDateTime.plusSeconds(zonedDateTime.getOffset().getTotalSeconds()).toInstant());
		}
		return date;
	}

	/**
	 * return a user localized Date from an UTC Date
	 *
	 * @param utcDateTime
	 * @param zoneId
	 *            current user zoneId
	 * @return
	 */
	public Date toUserDate(final Date utcDateTime, final ZoneId zoneId) {
		Date date = null;
		if (null != utcDateTime) {
			final ZonedDateTime zdt = this.getZonedValue(zoneId, utcDateTime);
			date = this.toUserDate(zdt);
		}
		return date;
	}

	public Date hoursToDate(final Date date) {
		return new Date(date.getTime());
	}

	public ZonedDateTime dateTimeFromHour(final String localizedHour) {
		return ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse("1970-01-01T" + localizedHour));
	}

	public String format(final Date date, final ZoneId userZoneId, final Boolean ignoreHours) {
		return this.format(this.getZonedValue(userZoneId, this.toUtcDate(date)), ignoreHours);
	}

	public String format(final Date date, final ZoneId userZoneId) {
		return this.format(this.getZonedValue(userZoneId, this.toUtcDate(date)));
	}

	public String formatForUi(final Date date, final ZoneId userZoneId) {
		// WARNING the \n is REQUIRED to allow scout detect
		// hours part of the date !
		final ZonedDateTime userZonedDate = this.getZonedValue(userZoneId, date);
		return this.format(userZonedDate, Boolean.TRUE) + "\n" + this.formatHours(userZonedDate);
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

	public String formatHoursForUi(final Date date, final ZoneId userZoneId) {
		// WARNING the \n is REQUIRED to allow scout detect
		// hours part of the date !
		return "\n" + this.formatHours(this.getZonedValue(userZoneId, date));
	}

	public String formatHours(final ZonedDateTime zonedDateTime, final Locale userlocale) {
		final DateFormat dateFormat = BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, userlocale);

		return dateFormat.format(this.toUserDate(zonedDateTime));
	}

	public String formatHours(final ZonedDateTime zonedDateTime) {
		return this.formatHours(zonedDateTime, NlsLocale.get());
	}

	public ZonedDateTime atZone(final ZonedDateTime date, final ZoneId zoneId) {
		return date.withZoneSameInstant(zoneId);
	}

	public String getRelativeDay(final Date value, final ZoneId zoneId) {
		return this.getRelativeDay(this.getZonedValue(zoneId, value));
	}

	public String getRelativeDay(final ZonedDateTime zonedValue, final Locale userLcoale) {
		return this.getRelativeDay(zonedValue, userLcoale, Boolean.FALSE, Boolean.FALSE);
	}

	@SuppressWarnings("PMD.EmptyIfStmt")
	public String getRelativeDay(final ZonedDateTime zonedValue, final Locale userLcoale,
			final Boolean emptyForAbsolute, final Boolean addParenthesis) {
		final String key = this.getRelativeDateKey(zonedValue, Boolean.TRUE);
		// the current date is required only when fallback to absolute happen
		// (aka: nb day shift is not a "named" day).
		final StringBuilder builder = new StringBuilder();

		if (emptyForAbsolute && key.contains(".absolute.")) {
			// let the empty StringBuilder
		} else {
			if (addParenthesis) {
				builder.append('(');
			}
			if (null == userLcoale) {
				builder.append(TEXTS.get(key, this.format(zonedValue, Boolean.TRUE)));
			} else {

				builder.append(TEXTS.get(userLcoale, key, this.format(zonedValue, Boolean.TRUE)));
			}
			if (addParenthesis) {
				builder.append(')');
			}
		}
		return builder.toString();
	}

	public String getRelativeDay(final ZonedDateTime zonedValue) {
		return this.getRelativeDay(zonedValue, null);
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
	 * unit of time between now and end date. /!\ with byFullTimeSlot, if now is
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
		return this.getRelativeTimeShift(start, end, byFullTimeSlot, unit);
	}

	/**
	 * unit of time between now and end date. /!\ with byFullTimeSlot, if now is
	 * in the "current" slot 0 is return else the number of slot between now and
	 * end date (java API differ, for "day" if there is less than 24hours, day
	 * diff will return 0)
	 *
	 * @param end
	 * @param byFullTimeSlot
	 * @param unit
	 * @return
	 */
	public long getRelativeTimeShift(final ZonedDateTime start, final ZonedDateTime end, final Boolean byFullTimeSlot,
			final ChronoUnit unit) {
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
		case MINUTES:
			unitShift = BigDecimal.valueOf(secondesShift)
					.divide(BigDecimal.valueOf(ChronoUnit.MINUTES.getDuration().getSeconds()), 0, RoundingMode.DOWN);
			break;
		default:
			LOG.warn(unit + " not supported");
			unitShift = BigDecimal.ZERO;
			break;
		}
		return unitShift.longValue();
	}

	/**
	 * unit of time between now and end date. /!\ with byFullTimeSlot, if now is
	 * in the "current" slot 0 is return else the number of slot between now and
	 * end date (java API differ, for "day" if there is less than 24hours, day
	 * diff will return 0)
	 *
	 * @param end
	 * @param byFullTimeSlot
	 * @param unit
	 * @return
	 */
	public long getRelativeTimeShift(final Date start, final Date end, final Boolean byFullTimeSlot,
			final ChronoUnit unit) {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final ZonedDateTime zonedStart = this.getZonedValue(appUserHelper.getCurrentUserTimeZone(), start);
		final ZonedDateTime zonedEnd = this.getZonedValue(appUserHelper.getCurrentUserTimeZone(), end);

		return this.getRelativeTimeShift(zonedStart, zonedEnd, byFullTimeSlot, unit);
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

	public Boolean isInPeriodInlcusiv(final ZonedDateTime checkedDate, final ZonedDateTime peridoStart,
			final ZonedDateTime periodEnd) {
		if (null == checkedDate) {
			return false; // early break
		}
		return !checkedDate.isBefore(peridoStart) && !checkedDate.isAfter(periodEnd);
	}

	public Boolean isPeriodOverlap(final ZonedDateTime checkedDateStart, final ZonedDateTime checkedDateEnd,
			final ZonedDateTime peridoStart, final ZonedDateTime periodEnd) {
		final Boolean isStartInPeriod = this.isInPeriodInlcusiv(checkedDateStart, peridoStart, periodEnd);
		final Boolean isEndInPeriod = this.isInPeriodInlcusiv(checkedDateEnd, peridoStart, periodEnd);

		return isStartInPeriod || isEndInPeriod;
	}

}
