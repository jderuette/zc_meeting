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
package org.zeroclick.meeting.client.api.event;

import java.time.ZonedDateTime;

/**
 * @author djer
 *
 */
public class SimpleEvent {

	private final ZonedDateTime start;
	private final ZonedDateTime end;

	public SimpleEvent(final ZonedDateTime start, final ZonedDateTime end) {
		super();
		this.start = start;
		this.end = end;
	}

	public ZonedDateTime getStart() {
		return this.start;
	}

	public ZonedDateTime getEnd() {
		return this.end;
	}

}
