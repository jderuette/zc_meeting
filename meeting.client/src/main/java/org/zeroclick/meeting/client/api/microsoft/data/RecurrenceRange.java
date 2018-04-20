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

import java.util.Date;

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/recurrencerange}
 */
public class RecurrenceRange {
	private Date endDate;
	private Integer numberOfOccurrences;
	private String recurrenceTimeZone;
	private Date startDate;
	/**
	 * One of : endDate, noEnd, numbered.
	 */
	private String type;

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final Date endDate) {
		this.endDate = endDate;
	}

	public Integer getNumberOfOccurrences() {
		return this.numberOfOccurrences;
	}

	public void setNumberOfOccurrences(final Integer numberOfOccurrences) {
		this.numberOfOccurrences = numberOfOccurrences;
	}

	public String getRecurrenceTimeZone() {
		return this.recurrenceTimeZone;
	}

	public void setRecurrenceTimeZone(final String recurrenceTimeZone) {
		this.recurrenceTimeZone = recurrenceTimeZone;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

}
