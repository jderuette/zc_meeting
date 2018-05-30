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
package org.zeroclick.meeting.service;

import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData.InvolvementTableRowData;

/**
 * Represent a participant for a meeting. <br/>
 * Allow to store email, without querying DataBase for each external event
 * created
 *
 * @author djer
 *
 */
public class ParticipantWithStatus {
	private InvolvementTableRowData involvementData;
	private String email;
	/**
	 * helper to access involvementData.state. <b>Always synchronized</b>
	 **/
	private String state;

	public ParticipantWithStatus() {

	}

	public ParticipantWithStatus(final InvolvementTableRowData involvementData, final String email) {
		this.involvementData = involvementData;
		this.state = involvementData.getState();
		this.email = email;
	}

	public InvolvementTableRowData getInvolvementData() {
		return this.involvementData;
	}

	public void setInvolvementData(final InvolvementTableRowData involvementData) {
		this.involvementData = involvementData;
		this.state = involvementData.getState();
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getState() {
		return this.state;
	}
}
