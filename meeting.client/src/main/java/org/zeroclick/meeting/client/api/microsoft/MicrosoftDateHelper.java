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
package org.zeroclick.meeting.client.api.microsoft;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.zeroclick.meeting.client.api.AbstractProviderDateHelper;
import org.zeroclick.meeting.client.api.microsoft.data.DateTimeTimeZone;

/**
 * @author djer
 *
 */
public class MicrosoftDateHelper extends AbstractProviderDateHelper<DateTimeTimeZone> {

	@Override
	protected Date getDate(final DateTimeTimeZone providerSpecificDate) {
		return providerSpecificDate.getDateTime();
	}

	@Override
	protected ZoneOffset timeOffset(final DateTimeTimeZone providerSpecificDate) {
		String timeZone = null;
		ZoneOffset zoneOffset = null;
		final String microsoftTimeZone = providerSpecificDate.getTimeZone();

		if ("UTC".equals(microsoftTimeZone)) {
			timeZone = "Z";
		} else {
			// TODO Djer13 transform Microsoft timeZone in Java TimeZone for any
			// useCase
			timeZone = microsoftTimeZone;
		}

		try {
			// timeZone is +mm:hh or -hh:mm or similar
			zoneOffset = ZoneOffset.of(timeZone);
		} catch (final DateTimeException dte) {
			// timeZone is an ID (like Europe/Paris
			final ZoneId zoneId = ZoneId.of(timeZone);
			zoneOffset = zoneId.getRules().getOffset(providerSpecificDate.getDateTime().toInstant());
		}

		return zoneOffset;
	}
}
