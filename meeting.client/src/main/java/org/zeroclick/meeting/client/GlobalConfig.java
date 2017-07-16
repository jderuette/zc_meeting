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
package org.zeroclick.meeting.client;

import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

/**
 * @author djer
 *
 */
public class GlobalConfig {

	/**
	 * Backend server protocol. To avoid url transformed to file in
	 * #PropertiesHelper.resolve line 323
	 */
	public static class ServerUrlProtocolProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "https";
		}

		@Override
		public String getKey() {
			return "zeroclick.server.url.protocol";
		}
	}

	/**
	 * FrontEnd URL, use to build absolute URL to the application (email, ...)
	 */
	public static class ApplicationUrlProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "http://localhost:8082";
		}

		@Override
		public String getKey() {
			return "zeroclick.url";
		}
	}

	/**
	 * Environment type. You can use #displayAsText to display formated text
	 * environment value.
	 */
	public static class ApplicationEnvProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "Local";
		}

		@Override
		public String getKey() {
			return "zeroclick.env";
		}

		/**
		 * Display the current env name. return "" for env with name "prod".
		 *
		 * @return a user friendly text.
		 */
		public String displayAsText() {
			if ("prod".equals(this.getValue())) {
				return "";
			}
			final StringBuffer sb = new StringBuffer();
			sb.append("[").append(this.getValue()).append("]");
			return sb.toString();
		}
	}

}
