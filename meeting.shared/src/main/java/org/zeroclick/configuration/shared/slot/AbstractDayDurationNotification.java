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
package org.zeroclick.configuration.shared.slot;

import java.io.Serializable;

/**
 * @author djer
 *
 */
public abstract class AbstractDayDurationNotification implements Serializable {

	private static final long serialVersionUID = 5119185521903667929L;

	private final DayDurationFormData dayDurationForm;
	private final String slotCode;

	public AbstractDayDurationNotification(final DayDurationFormData dayDuration, final String slotCode) {
		this.dayDurationForm = dayDuration;
		this.slotCode = slotCode;
	}

	public DayDurationFormData getDayDurationForm() {
		return this.dayDurationForm;
	}

	public String getSlotCode() {
		return this.slotCode;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(50);
		builder.append("DayDurationNotification [dayDurationForm=").append(this.dayDurationForm).append(']');
		return builder.toString();
	}

}
