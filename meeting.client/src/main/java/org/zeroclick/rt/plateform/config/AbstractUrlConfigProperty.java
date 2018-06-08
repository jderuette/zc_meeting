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
package org.zeroclick.rt.plateform.config;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

import com.google.api.client.http.GenericUrl;

/**
 * @author djer
 *
 *         Manage URL config, can be relative "/myPath/myRessource", or full
 *         "http://myDNS/MyPath" or "https://myDNS/MyPath"
 *
 */
public abstract class AbstractUrlConfigProperty extends AbstractStringConfigProperty {

	/**
	 * The base URL to build the FULL url, can be
	 * CONFIG.getPropertyValue(ApplicationUrlProperty.class);
	 *
	 * @return
	 */
	protected abstract String getDefautlBaseUrl();

	public String getAbsoluteURI() {
		String urlString = null;
		final String parsedValue = this.getValue();

		if (null != parsedValue) {
			if ('/' == parsedValue.charAt(0)) {
				final GenericUrl url = new GenericUrl(this.getDefautlBaseUrl());
				url.setRawPath(parsedValue);
				urlString = url.build();
			} else {
				urlString = parsedValue;
			}
		}
		return urlString;
	}

}
