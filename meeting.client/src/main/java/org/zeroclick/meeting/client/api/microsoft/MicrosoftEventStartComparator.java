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

import org.zeroclick.meeting.client.api.AbstractDateComparator;
import org.zeroclick.meeting.client.api.microsoft.data.Event;

/**
 * @author djer
 *
 */
public class MicrosoftEventStartComparator extends AbstractDateComparator<Event> {

	@Override
	protected Long extractDateAsLong(final Event objectContainingDate) {
		return objectContainingDate.getStart().getDateTime().getTime();
	}
}
