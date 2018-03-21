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

import java.util.Base64;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author djer
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdToken {

	private static final Logger LOG = LoggerFactory.getLogger(IdToken.class);

	// NOTE: This is just a subset of the claims returned in the
	// ID token. For a full listing, see:
	// https://azure.microsoft.com/en-us/documentation/articles/active-directory-v2-tokens/#idtokens
	@JsonProperty("exp")
	private long expirationTime;
	@JsonProperty("nbf")
	private long notBefore;
	@JsonProperty("tid")
	private String tenantId;
	private String nonce;
	private String name;
	private String email;
	@JsonProperty("preferred_username")
	private String preferredUsername;
	@JsonProperty("oid")
	private String objectId;

	public static IdToken parseEncodedToken(final String encodedToken, final String nonce) {
		// Encoded token is in three parts, separated by '.'
		final String[] tokenParts = encodedToken.split("\\.");

		// The three parts are: header.token.signature
		final String idToken = tokenParts[1];

		final byte[] decodedBytes = Base64.getUrlDecoder().decode(idToken);

		final ObjectMapper mapper = new ObjectMapper();
		IdToken newToken = null;
		try {
			newToken = mapper.readValue(decodedBytes, IdToken.class);
			if (!newToken.isValid(nonce)) {
				return null;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return newToken;
	}

	public long getExpirationTime() {
		return this.expirationTime;
	}

	public void setExpirationTime(final long expirationTime) {
		this.expirationTime = expirationTime;
	}

	public long getNotBefore() {
		return this.notBefore;
	}

	public void setNotBefore(final long notBefore) {
		this.notBefore = notBefore;
	}

	public String getTenantId() {
		return this.tenantId;
	}

	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	public String getNonce() {
		return this.nonce;
	}

	public void setNonce(final String nonce) {
		this.nonce = nonce;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getPreferredUsername() {
		return this.preferredUsername;
	}

	public void setPreferredUsername(final String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}

	public String getObjectId() {
		return this.objectId;
	}

	public void setObjectId(final String objectId) {
		this.objectId = objectId;
	}

	private Date getUnixEpochAsDate(final long epoch) {
		// Epoch timestamps are in seconds,
		// but Jackson converts integers as milliseconds.
		// Rather than create a custom deserializer, this helper will do
		// the conversion.
		return new Date(epoch * 1000);
	}

	private boolean isValid(final String nonce) {
		// This method does some basic validation
		// For more information on validation of ID tokens, see
		// https://azure.microsoft.com/en-us/documentation/articles/active-directory-v2-tokens/#validating-tokens
		final Date now = new Date();

		// Check expiration and not before times
		if (now.after(this.getUnixEpochAsDate(this.expirationTime))
				|| now.before(this.getUnixEpochAsDate(this.notBefore))) {
			LOG.error(new StringBuilder().append("Token is not within it's valid 'time'. After : ")
					.append(this.expirationTime).append(" Or before : ").append(this.notBefore).toString());
			return false;
		}

		// Check nonce
		if (!nonce.equals(this.getNonce())) {
			LOG.error("Nonce mismatch, expected : " + nonce + ", sended value :" + this.getNonce());
			return false;
		}

		return true;
	}
}
