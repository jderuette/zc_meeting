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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/datetimetimezone}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DateTimeTimeZone {
	/** ISO 8601 like 2015-11-08T19:00:00.0000000 **/
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private Date dateTime;
	private String timeZone;

	public Date getDateTime() {
		return this.dateTime;
	}

	public void setDateTime(final Date dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * A timeZone string LIKE : Etc/GMT+12, Etc/GMT+11, Pacifique/Honolulu,
	 * Amérique/Anchorage, Amérique/Santa_Isabel, Amérique/Los_Angeles,
	 * Europe/Paris, Asie/Yangon (Rangoon)
	 *
	 * @return
	 */
	public String getTimeZone() {
		return this.timeZone;
	}

	public void setTimeZone(final String timeZone) {
		this.timeZone = timeZone;
	}
}
