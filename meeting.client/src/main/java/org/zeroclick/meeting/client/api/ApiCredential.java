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
package org.zeroclick.meeting.client.api;

import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;

/**
 * @author djer
 *
 */
public class ApiCredential<T> {
	T credential;
	ApiTableRowData metaData;

	public ApiCredential() {
		super();
		this.credential = null;
		this.metaData = null;
	}

	public ApiCredential(final T credential, final ApiTableRowData metaData) {
		super();
		this.credential = credential;
		this.metaData = metaData;
	}

	public T getCredential() {
		return this.credential;
	}

	public void setCredential(final T credential) {
		this.credential = credential;
	}

	public ApiTableRowData getMetaData() {
		return this.metaData;
	}

	public void setMetaData(final ApiTableRowData metaData) {
		this.metaData = metaData;
	}
}
