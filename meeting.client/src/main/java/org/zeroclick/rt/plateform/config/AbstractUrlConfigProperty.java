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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;

/**
 * @author djer
 *
 *         Manage URL config, can be relative "/myPath/myRessource", or full
 *         "url-http://myDNS/MyPath" or "url-https://myDNS/MyPath"<br/>
 *         <b>the prefix url- is required</b> to avoid
 *         PropertiesHelper.resolve:323 to remove the DNS part
 *
 */
public abstract class AbstractUrlConfigProperty extends AbstractStringConfigProperty {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractUrlConfigProperty.class);

	private static final String FULL_URL_PREFIX = "url-";

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
				final String baseUrl = this.getDefautlBaseUrl();
				LOG.info(new StringBuilder().append("Building URI for key : ").append(this.getKey())
						.append(" with Base URL : ").append(baseUrl).append(" and path : ").append(parsedValue)
						.toString());
				final GenericUrl url = new GenericUrl(baseUrl);
				url.setRawPath(parsedValue);
				urlString = url.build();
			} else {
				// remove the "url-" part
				urlString = parsedValue.substring(FULL_URL_PREFIX.length());
			}
		}

		LOG.info(new StringBuilder().append("Absolute URI built for key : ").append(this.getKey())
				.append(" with value : ").append(urlString).toString());
		return urlString;
	}

	/**
	 *
	 * @return a relative URI, required to register a servlet, without the
	 *         protocol and DNS part
	 */
	public String getRelativeUri() {
		String relativeUrlString = null;
		final String parsedValue = this.getValue();

		if (null != parsedValue) {
			if (parsedValue.startsWith(FULL_URL_PREFIX)) {
				// remove the "url-"
				final String urlString = parsedValue.substring(4);

				// remove protocol, DNS part and eventual query parameters
				final GenericUrl url = new GenericUrl(urlString);
				relativeUrlString = url.getRawPath();

			} else {
				relativeUrlString = parsedValue;
			}
		}

		LOG.info(new StringBuilder().append("Relative URI built for key : ").append(this.getKey())
				.append(" with value : ").append(relativeUrlString).toString());
		return relativeUrlString;
	}

}
