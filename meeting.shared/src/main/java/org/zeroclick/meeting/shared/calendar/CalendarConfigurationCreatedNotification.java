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
package org.zeroclick.meeting.shared.calendar;

import org.zeroclick.meeting.shared.AbstractNotification;

/**
 * @author djer
 *
 */
public class CalendarConfigurationCreatedNotification extends AbstractNotification<CalendarConfigurationFormData> {

	private static final long serialVersionUID = -2546211635305638271L;

	public CalendarConfigurationCreatedNotification(final CalendarConfigurationFormData newData) {
		super(newData);
	}

}
