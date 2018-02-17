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
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.util.date.UTCDate;

/**
 * @author djer very similar to {@link IDateField}
 */
public interface IZeroClickUTCDateField extends IValueField<UTCDate> {
	String PROP_HAS_DATE = "hasDate";
	String PROP_HAS_TIME = "hasTime";
	String PROP_DATE_FORMAT_PATTERN = "dateFormatPattern";
	String PROP_TIME_FORMAT_PATTERN = "timeFormatPattern";
	String PROP_AUTO_DATE = "autoDate";
	String PROP_ALLOWED_DATES = "allowedDates";

	IZeroClickUTCDateFieldUIFacade getUIFacade();

	void setFormat(String format);

	String getFormat();

	void setDateFormatPattern(String dateFormatPattern);

	String getDateFormatPattern();

	void setTimeFormatPattern(String timeFormatPattern);

	String getTimeFormatPattern();

	/**
	 * @return the date-time format created using {@link #getFormat()} that
	 *         contains the date and time part
	 */
	DateFormat getDateFormat();

	/**
	 * @return the date format created using {@link #getFormat()} that only
	 *         contains the date part
	 */
	DateFormat getIsolatedDateFormat();

	/**
	 * @return the time format created using {@link #getFormat()} that only
	 *         contains the time part
	 */
	DateFormat getIsolatedTimeFormat();

	boolean isHasDate();

	void setHasDate(boolean hasDate);

	boolean isHasTime();

	void setHasTime(boolean hasTime);

	/**
	 * @param autoDate
	 *            The date to be used when setting a value "automatically", e.g.
	 *            when the date picker is opened initially or when a date or
	 *            time is entered and the other component has to be filled.
	 *            <code>null</code> means "use current date and time".
	 */
	void setAutoDate(UTCDate autoDate);

	/**
	 * @return the date to be used when setting a value "automatically", e.g.
	 *         when the date picker is opened initially or when a date or time
	 *         is entered and the other component has to be filled. If the
	 *         return value is <code>null</code>, the current date and time
	 *         should be used.
	 */
	UTCDate getAutoDate();

	/**
	 * @return the time value as a double in the range from [0..1[ for 00:00 -
	 *         23:59:59.
	 */
	Double getTimeValue();

	/**
	 * Set the time value as a double in the range from [0..1[ for 00:00 -
	 * 23:59:59.
	 */
	void setTimeValue(Double time);

	/**
	 * Sets a list of allowed dates. When the given list is not empty or null
	 * only the dates contained in the list can be chosen in the date-picker or
	 * entered manually in the date-field. All other dates are disabled. When
	 * the list is empty or null all dates are available again.
	 */
	void setAllowedDates(List<UTCDate> allowedDates);

	List<UTCDate> getAllowedDates();
}
