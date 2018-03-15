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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author djer
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResult<T> {
	@JsonProperty("@odata.nextLink")
	private String nextPageLink;
	private T[] value;

	public String getNextPageLink() {
		return this.nextPageLink;
	}

	public void setNextPageLink(final String nextPageLink) {
		this.nextPageLink = nextPageLink;
	}

	public T[] getValue() {
		return this.value;
	}

	public void setValue(final T[] value) {
		this.value = value;
	}
}
