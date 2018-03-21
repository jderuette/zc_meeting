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

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/attendee}
 */
public class Attendee {
	private ResponseStatus status;
	private String type;
	private EmailAddress emailAddress;

	public ResponseStatus getStatus() {
		return this.status;
	}

	public void setStatus(final ResponseStatus status) {
		this.status = status;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public EmailAddress getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(final EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}
}
