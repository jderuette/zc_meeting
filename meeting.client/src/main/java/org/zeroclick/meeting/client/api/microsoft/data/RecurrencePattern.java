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
package org.zeroclick.meeting.client.api.microsoft.data;

import java.util.Collection;

/**
 * @author djer
 * @se {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/recurrencepattern}
 *
 */
public class RecurrencePattern {

	private Integer dayOfMonth;
	/**
	 * One of : sunday, monday, tuesday,wednesday, thursday, friday et saturday.
	 */
	private Collection<String> daysOfWeek;
	private String firstDayOfWeek;
	private String index;
	private Integer interval;
	private Integer month;
	/**
	 * One of : daily, weekly, absoluteMonthly, relativeMonthly, absoluteYearly,
	 * relativeYearly.
	 */
	private String type;

	public Integer getDayOfMonth() {
		return this.dayOfMonth;
	}

	public void setDayOfMonth(final Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public Collection<String> getDaysOfWeek() {
		return this.daysOfWeek;
	}

	public void setDaysOfWeek(final Collection<String> daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}

	public String getFirstDayOfWeek() {
		return this.firstDayOfWeek;
	}

	public void setFirstDayOfWeek(final String firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

	public String getIndex() {
		return this.index;
	}

	public void setIndex(final String index) {
		this.index = index;
	}

	public Integer getInterval() {
		return this.interval;
	}

	public void setInterval(final Integer interval) {
		this.interval = interval;
	}

	public Integer getMonth() {
		return this.month;
	}

	public void setMonth(final Integer month) {
		this.month = month;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

}
