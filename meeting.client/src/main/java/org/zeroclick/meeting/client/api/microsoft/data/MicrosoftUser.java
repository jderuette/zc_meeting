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
public class MicrosoftUser {
	private String id;
	private String mail;
	private String displayName;
	private String surname;
	private String givenName;
	private String userPrincipalName;

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getMail() {
		return this.mail;
	}

	public void setMail(final String emailAddress) {
		this.mail = emailAddress;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(final String surname) {
		this.surname = surname;
	}

	public String getGivenName() {
		return this.givenName;
	}

	public void setGivenName(final String givenName) {
		this.givenName = givenName;
	}

	public String getUserPrincipalName() {
		return this.userPrincipalName;
	}

	public void setUserPrincipalName(final String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}
}
