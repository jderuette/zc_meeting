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
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/location}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

	private PhysicalAddress address;
	private String displayName;
	private String locationEmailAddress;

	public PhysicalAddress getAddress() {
		return this.address;
	}

	public void setAddress(final PhysicalAddress address) {
		this.address = address;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public String getLocationEmailAddress() {
		return this.locationEmailAddress;
	}

	public void setLocationEmailAddress(final String locationEmailAddress) {
		this.locationEmailAddress = locationEmailAddress;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(100);
		builder.append("Location [address=").append(this.address).append(", displayName=").append(this.displayName)
				.append(", locationEmailAddress=").append(this.locationEmailAddress).append(']');
		return builder.toString();
	}

}
