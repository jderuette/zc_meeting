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
package org.zeroclick.meeting.client.api;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author djer
 *
 * @param D
 *            provider specific Date representation
 */
public abstract class AbstractProviderDateHelper<D> implements ProviderDateHelper<D> {

	/**
	 * Convert a provider specific (Event) Date to a standard Java 8
	 * LocalDateTime (JSR 310)
	 *
	 * @param date
	 * @return
	 */
	@Override
	public ZonedDateTime fromEventDateTime(final D providerSpecificDate) {
		final Date javaDate = this.getDate(providerSpecificDate);
		return this.fromDateTime(javaDate, this.timeOffset(providerSpecificDate));
	}

	protected abstract Date getDate(D providerSpecificDate);

	/**
	 * Convert a offset from a provider specific (Event) Date to a standard Java
	 * 8 ZoneOffset (JSR 310)
	 *
	 * @param dateTime
	 * @return
	 */
	protected abstract ZoneOffset timeOffset(final D date);

	private ZonedDateTime fromDateTime(final Date dateTime, final ZoneOffset zoneOffset) {
		final ZonedDateTime localDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTime.getTime()), zoneOffset);

		return localDate;
	}
}
