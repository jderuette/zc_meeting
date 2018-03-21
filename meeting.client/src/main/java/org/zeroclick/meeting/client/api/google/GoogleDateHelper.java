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
package org.zeroclick.meeting.client.api.google;

import java.time.ZoneOffset;
import java.util.Date;

import org.zeroclick.meeting.client.api.AbstractProviderDateHelper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * @author djer
 *
 */
public class GoogleDateHelper extends AbstractProviderDateHelper<EventDateTime> {

	@Override
	public Date getDate(final EventDateTime providerSpecificDate) {
		final DateTime date = this.getGoogleDate(providerSpecificDate);
		return new Date(date.getValue());
	}

	private DateTime getGoogleDate(final EventDateTime providerSpecificDate) {
		DateTime date = null;
		if (null == providerSpecificDate.getDateTime()) {
			date = providerSpecificDate.getDate();
		} else {
			date = providerSpecificDate.getDateTime();
		}
		return date;
	}

	@Override
	protected ZoneOffset timeOffset(final EventDateTime providerSpecificDate) {
		final DateTime date = this.getGoogleDate(providerSpecificDate);
		Integer offsetHours = 0;
		Integer offsetMinutes = 0;

		offsetHours = date.getTimeZoneShift() / 60;
		offsetMinutes = date.getTimeZoneShift() % 60;

		return ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);
	}

}
