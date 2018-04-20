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

import org.zeroclick.meeting.client.api.AbstractDateComparator;

import com.google.api.services.calendar.model.Event;

/**
 * @author djer13
 *
 *         Allow ordering of Google events from multiple calendars. Order is
 *         based on the start date of each events.
 */
public class GoogleEventStartComparator extends AbstractDateComparator<Event> {

	@Override
	protected Long extractDateAsLong(final Event event) {
		Long startValue;
		if (null == event.getStart().getDateTime()) {
			startValue = event.getStart().getDate().getValue();
		} else {
			startValue = event.getStart().getDateTime().getValue();
		}

		return startValue;
	}
}
