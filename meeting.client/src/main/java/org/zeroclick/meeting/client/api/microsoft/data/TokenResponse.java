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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author djer
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse implements Serializable {

	private static final long serialVersionUID = -3712320945577114983L;

	@JsonProperty("token_type")
	private String tokenType;
	private String scope;
	@JsonProperty("expires_in")
	private int expiresIn;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("id_token")
	private String idToken;
	private String error;
	@JsonProperty("error_description")
	private String errorDescription;
	@JsonProperty("error_codes")
	private int[] errorCodes;
	private Date expirationTime;

	public String getTokenType() {
		return this.tokenType;
	}

	public void setTokenType(final String tokenType) {
		this.tokenType = tokenType;
	}

	public String getScope() {
		return this.scope;
	}

	public void setScope(final String scope) {
		this.scope = scope;
	}

	public int getExpiresIn() {
		return this.expiresIn;
	}

	public void setExpiresIn(final int expiresIn) {
		this.expiresIn = expiresIn;
		final Calendar now = Calendar.getInstance();
		now.add(Calendar.SECOND, expiresIn);
		this.expirationTime = now.getTime();
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(final String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return this.refreshToken;
	}

	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getIdToken() {
		return this.idToken;
	}

	public void setIdToken(final String idToken) {
		this.idToken = idToken;
	}

	public String getError() {
		return this.error;
	}

	public void setError(final String error) {
		this.error = error;
	}

	public String getErrorDescription() {
		return this.errorDescription;
	}

	public void setErrorDescription(final String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public int[] getErrorCodes() {
		return this.errorCodes;
	}

	public void setErrorCodes(final int[] errorCodes) {
		this.errorCodes = errorCodes;
	}

	public Date getExpirationTime() {
		return this.expirationTime;
	}

}
