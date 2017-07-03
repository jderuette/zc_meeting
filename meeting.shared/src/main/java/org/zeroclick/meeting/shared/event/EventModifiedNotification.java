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
package org.zeroclick.meeting.shared.event;

import org.zeroclick.meeting.shared.event.EventFormData;

/**
 * @author djer
 *
 */
public class EventModifiedNotification extends AbstractEventNotification {

	private static final long serialVersionUID = -2546211635305638271L;

	public EventModifiedNotification(final EventFormData modifiedEvent) {
		super(modifiedEvent);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("EventModifiedNotification [eventForm=").append(super.getEventForm()).append("]");
		return builder.toString();
	}
}
