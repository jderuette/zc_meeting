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
package org.zeroclick.ui.form.fields.datefield;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 *         very similar to {@link UTCDate} and {@link AbstractDateField}. But
 *         implement "setValue" with an UTCDate instead of default Date to avoid
 *         error 500 on server
 */
public abstract class AbstractZeroClickUTCDateField extends AbstractValueField<UTCDate>
		implements IZeroClickUTCDateField {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractDateField.class);

	private IZeroClickUTCDateFieldUIFacade m_uiFacade;

	public AbstractZeroClickUTCDateField() {
		this(true);
	}

	public AbstractZeroClickUTCDateField(final boolean callInitializer) {
		super(callInitializer);
	}

	/**
	 * The date/time format, for a description see {@link SimpleDateFormat}
	 */
	@ConfigProperty(ConfigProperty.STRING)
	@Order(230)
	protected String getConfiguredFormat() {
		return null;
	}

	@ConfigProperty(ConfigProperty.STRING)
	@Order(231)
	protected String getConfiguredDateFormatPattern() {
		return null;
	}

	@ConfigProperty(ConfigProperty.STRING)
	@Order(232)
	protected String getConfiguredTimeFormatPattern() {
		return null;
	}

	@ConfigProperty(ConfigProperty.BOOLEAN)
	@Order(240)
	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredHasDate() {
		return true;
	}

	@ConfigProperty(ConfigProperty.BOOLEAN)
	@Order(241)
	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredHasTime() {
		return false;
	}

	/**
	 * Date to be used when setting a value "automatically", e.g. when the date
	 * picker is opened initially or when a date or time is entered and the
	 * other component has to be filled. If no auto date is set (which is the
	 * default), the current date (with time part "00:00:00.000") is used.
	 */
	@Order(270)
	protected UTCDate getConfiguredAutoDate() {
		return null;
	}

	/**
	 * @deprecated This method is never called for {@link IDateField}. The UI is
	 *             responsible for parsing a date.
	 */
	@Override
	@Deprecated
	protected UTCDate execParseValue(final String text) {
		return super.execParseValue(text);
	}

	/**
	 * <b>Important:</b> Make sure that this method only uses formats that are
	 * supported by the UI. Otherwise, a formatted date cannot be parsed again.
	 */
	@Override
	protected String execFormatValue(final UTCDate value) {
		return super.execFormatValue(value);
	}

	@Override
	protected void initConfig() {
		this.m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
		super.initConfig();

		this.setHasDate(this.getConfiguredHasDate());
		this.setHasTime(this.getConfiguredHasTime());
		this.setAutoDate(this.getConfiguredAutoDate());

		this.setDateFormatPattern(this.getConfiguredDateFormatPattern());
		this.setTimeFormatPattern(this.getConfiguredTimeFormatPattern());
		this.setFormat(this.getConfiguredFormat());
		this.setAllowedDates(Collections.<UTCDate>emptyList());
	}

	@Override
	public void setFormat(String format) {
		format = this.checkFormatPatternSupported(format);

		String dateFormatPattern = null;
		String timeFormatPattern = null;
		if (format != null) {
			// Try to extract date and time parts of pattern
			final int hPos = format.toLowerCase().indexOf('h');
			if (hPos >= 0) {
				dateFormatPattern = format.substring(0, hPos).trim();
				timeFormatPattern = format.substring(hPos).trim();
			} else {
				if (this.isHasDate()) {
					dateFormatPattern = format;
					timeFormatPattern = null;
					if (this.isHasTime()) {
						LOG.warn("Could not extract time part from pattern '{}', using default pattern.", format);
					}
				} else {
					dateFormatPattern = null;
					timeFormatPattern = this.isHasTime() ? format : null;
				}
			}
		}
		this.setDateFormatPattern(dateFormatPattern);
		this.setTimeFormatPattern(timeFormatPattern);
	}

	@Override
	public String getFormat() {
		String format = "";
		if (this.isHasDate()) {
			format = StringUtility.join(" ", format, this.getDateFormatPattern());
		}
		if (this.isHasTime()) {
			format = StringUtility.join(" ", format, this.getTimeFormatPattern());
		}
		return format;
	}

	@Override
	public void setDateFormatPattern(String dateFormatPattern) {
		dateFormatPattern = this.checkFormatPatternSupported(dateFormatPattern);
		if (dateFormatPattern == null) {
			dateFormatPattern = BEANS.get(DateFormatProvider.class)
					.getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_DATE, NlsLocale.get());
		}
		this.propertySupport.setPropertyString(PROP_DATE_FORMAT_PATTERN, dateFormatPattern);
		// Always update display text (format may be the same, but language
		// might have changed)
		this.refreshDisplayText();
	}

	@Override
	public String getDateFormatPattern() {
		return this.propertySupport.getPropertyString(PROP_DATE_FORMAT_PATTERN);
	}

	@Override
	public void setTimeFormatPattern(String timeFormatPattern) {
		timeFormatPattern = this.checkFormatPatternSupported(timeFormatPattern);
		if (timeFormatPattern == null) {
			timeFormatPattern = BEANS.get(DateFormatProvider.class)
					.getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, NlsLocale.get());
		}
		this.propertySupport.setPropertyString(PROP_TIME_FORMAT_PATTERN, timeFormatPattern);
		// Always update display text (format may be the same, but language
		// might have changed)
		this.refreshDisplayText();
	}

	@Override
	public String getTimeFormatPattern() {
		return this.propertySupport.getPropertyString(PROP_TIME_FORMAT_PATTERN);
	}

	protected String checkFormatPatternSupported(final String formatPattern) {
		// FIXME bsh: How to implement?
		return formatPattern;
	}

	@Override
	public boolean isHasTime() {
		return this.propertySupport.getPropertyBool(PROP_HAS_TIME);
	}

	@Override
	public void setHasTime(final boolean hasTime) {
		this.propertySupport.setPropertyBool(PROP_HAS_TIME, hasTime);
		if (this.isInitialized()) {
			this.setValue(this.getValue());
		}
	}

	@Override
	public boolean isHasDate() {
		return this.propertySupport.getPropertyBool(PROP_HAS_DATE);
	}

	@Override
	public void setHasDate(final boolean hasDate) {
		this.propertySupport.setPropertyBool(PROP_HAS_DATE, hasDate);
		if (this.isInitialized()) {
			this.setValue(this.getValue());
		}
	}

	@Override
	public void setAutoDate(final UTCDate autoDate) {
		this.propertySupport.setProperty(PROP_AUTO_DATE, autoDate);
	}

	@Override
	public UTCDate getAutoDate() {
		return (UTCDate) this.propertySupport.getProperty(PROP_AUTO_DATE);
	}

	@Override
	public IZeroClickUTCDateFieldUIFacade getUIFacade() {
		return this.m_uiFacade;
	}

	@Override
	protected String formatValueInternal(final UTCDate validValue) {
		if (validValue == null) {
			return "";
		}
		DateFormat dateFormat;
		final StringBuilder builder = new StringBuilder();
		dateFormat = this.getIsolatedDateFormat();
		builder.append(dateFormat == null ? "" : dateFormat.format(validValue)).append('\n');
		dateFormat = this.getIsolatedTimeFormat();
		builder.append(dateFormat == null ? "" : dateFormat.format(validValue));
		return builder.toString();
	}

	@Override
	protected UTCDate validateValueInternal(UTCDate rawValue) {
		rawValue = super.validateValueInternal(rawValue);

		if (rawValue == null) {
			return null;
		}

		// Check if date is allowed (if allowed dates are set)
		if (this.getAllowedDates().size() > 0) {
			final Date truncDate = DateUtility.truncDate(rawValue);
			boolean found = false;
			for (final Date allowedDate : this.getAllowedDates()) {
				if (allowedDate.compareTo(truncDate) == 0) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new VetoException(new ProcessingStatus(TEXTS.get("DateIsNotAllowed"), IStatus.ERROR));
			}
		}

		return rawValue;
	}

	@Override
	public Double getTimeValue() {
		return DateUtility.convertDateToDoubleTime(this.getValue());
	}

	@Override
	public void setTimeValue(final Double time) {
		this.setValue(TypeCastUtility.castValue(DateUtility.convertDoubleTimeToDate(time), UTCDate.class));
	}

	protected Date applyAutoDate(Date autoDate) {
		if (autoDate != null) {
			return autoDate;
		}
		autoDate = this.getAutoDate();
		if (autoDate == null) {
			// use today's date
			autoDate = new Date();
		}
		return autoDate;
	}

	@Override
	public DateFormat getDateFormat() {
		final String format = this.getFormat();
		if (format != null) {
			return new SimpleDateFormat(format, NlsLocale.get());
		}
		return null;
	}

	@Override
	public DateFormat getIsolatedDateFormat() {
		final DateFormat dateFormat = this.getDateFormat();
		if (dateFormat instanceof SimpleDateFormat) {
			final String pat = ((SimpleDateFormat) dateFormat).toPattern();
			final int hPos = pat.toLowerCase().indexOf('h');
			if (hPos >= 0) {
				try {
					return new SimpleDateFormat(pat.substring(0, hPos).trim(), NlsLocale.get());
				} catch (final Exception e) {
					LOG.error("could not isolate date pattern from '{}'", pat, e);
				}
			}
		}
		return dateFormat;
	}

	@Override
	public DateFormat getIsolatedTimeFormat() {
		final DateFormat dateFormat = this.getDateFormat();
		if (dateFormat instanceof SimpleDateFormat) {
			final String pat = ((SimpleDateFormat) dateFormat).toPattern();
			final int hPos = pat.toLowerCase().indexOf('h');
			if (hPos >= 0) {
				try {
					return new SimpleDateFormat(pat.substring(hPos).trim(), NlsLocale.get());
				} catch (final Exception e) {
					LOG.error("could not isolate time pattern from '{}'", pat, e);
				}
			}
		}
		return null;
	}

	@Override
	public void setAllowedDates(List<UTCDate> allowedDates) {
		if (allowedDates == null) {
			allowedDates = Collections.emptyList();
		} else {
			// Make sure each date is truncated and the list of dates is ordered
			// by date
			final List<UTCDate> sortedTruncatedDates = new ArrayList<>(allowedDates.size());
			for (final UTCDate date : allowedDates) {
				sortedTruncatedDates.add(TypeCastUtility.castValue(DateUtility.truncDate(date), UTCDate.class));
			}
			Collections.sort(sortedTruncatedDates);
			allowedDates = sortedTruncatedDates;
		}
		this.propertySupport.setProperty(PROP_ALLOWED_DATES, allowedDates);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UTCDate> getAllowedDates() {
		return new ArrayList<>((List<UTCDate>) this.propertySupport.getProperty(PROP_ALLOWED_DATES));
	}

	protected class P_UIFacade implements IZeroClickUTCDateFieldUIFacade {

		@Override
		public void setDateTimeFromUI(final UTCDate date) {
			if (!AbstractZeroClickUTCDateField.this.isEnabled() || !AbstractZeroClickUTCDateField.this.isVisible()) {
				return;
			}
			AbstractZeroClickUTCDateField.this.setValue(date);
		}

		@Override
		public void setDisplayTextFromUI(final String text) {
			if (!AbstractZeroClickUTCDateField.this.isEnabled() || !AbstractZeroClickUTCDateField.this.isVisible()) {
				return;
			}
			AbstractZeroClickUTCDateField.this.setDisplayText(text);
		}

		@Override
		public void setParseErrorFromUI() {
			final String invalidMessage = AbstractZeroClickUTCDateField.this.getDisplayText().replace("\n", " ");
			final ParsingFailedStatus status = new ParsingFailedStatus(
					ScoutTexts.get("InvalidValueMessageX", invalidMessage),
					AbstractZeroClickUTCDateField.this.getDisplayText());
			AbstractZeroClickUTCDateField.this.addErrorStatus(status);
		}

		@Override
		public void removeParseErrorFromUI() {
			AbstractZeroClickUTCDateField.this.removeErrorStatus(ParsingFailedStatus.class);
		}
	}

	protected static class LocalZeroClickUTCDateFieldExtension<OWNER extends AbstractZeroClickUTCDateField>
			extends LocalValueFieldExtension<UTCDate, OWNER> implements IZeroClickUTCDateFieldExtension<OWNER> {

		public LocalZeroClickUTCDateFieldExtension(final OWNER owner) {
			super(owner);
		}

	}

	@Override
	protected IZeroClickUTCDateFieldExtension<? extends AbstractZeroClickUTCDateField> createLocalExtension() {
		return new LocalZeroClickUTCDateFieldExtension<>(this);
	}
}
