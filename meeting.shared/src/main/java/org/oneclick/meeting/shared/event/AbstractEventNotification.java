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
package org.oneclick.meeting.shared.event;

import java.io.Serializable;

/**
 * @author djer
 *
 */
public class AbstractEventNotification implements Serializable {

	private static final long serialVersionUID = -2546211635305638271L;

	private final EventFormData eventForm;

	public AbstractEventNotification(final EventFormData newEvent) {
		this.eventForm = newEvent;
	}

	public EventFormData getEventForm() {
		return this.eventForm;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("EventNotification [eventForm=").append(this.eventForm).append("]");
		return builder.toString();
	}

}
