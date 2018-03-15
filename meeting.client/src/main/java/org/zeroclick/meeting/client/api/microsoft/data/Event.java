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
package org.zeroclick.meeting.client.api.microsoft.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author djer
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
	private String id;
	private String subject;
	private Recipient organizer;
	private DateTimeTimeZone start;
	private DateTimeTimeZone end;

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public Recipient getOrganizer() {
		return this.organizer;
	}

	public void setOrganizer(final Recipient organizer) {
		this.organizer = organizer;
	}

	public DateTimeTimeZone getStart() {
		return this.start;
	}

	public void setStart(final DateTimeTimeZone start) {
		this.start = start;
	}

	public DateTimeTimeZone getEnd() {
		return this.end;
	}

	public void setEnd(final DateTimeTimeZone end) {
		this.end = end;
	}
}
