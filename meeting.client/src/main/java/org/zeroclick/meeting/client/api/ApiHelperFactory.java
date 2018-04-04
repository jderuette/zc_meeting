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

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;

/**
 * @author djer
 *
 */
public class ApiHelperFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ApiHelperFactory.class);

	/**
	 * Get the provider specific API Helper
	 *
	 * @param apiData
	 *            data extracted form the data base
	 * @return
	 */
	public static ApiHelper get(final ApiTableRowData apiData) {
		ApiHelper apiHelper = null;

		if (null != apiData && null != apiData.getProvider()) {
			if (ProviderCodeType.GoogleCode.ID.equals(apiData.getProvider())) {
				apiHelper = BEANS.get(GoogleApiHelper.class);
			} else if (ProviderCodeType.MicrosoftCode.ID.equals(apiData.getProvider())) {
				apiHelper = BEANS.get(MicrosoftApiHelper.class);
			} else {
				LOG.error(new StringBuilder()
						.append("Cannot find a provider specific implementation of ApiHelper for api ID : ")
						.append(apiData.getApiCredentialId()).append(" with Provider ID : " + apiData.getProvider())
						.toString());
			}
		}
		return apiHelper;
	}

	public static ApiHelper get(final CalendarConfigTableRowData calendar) {
		ApiHelper apiHelper = null;

		if (null != calendar && null != calendar.getOAuthCredentialId()) {
			final IApiService apiService = BEANS.get(IApiService.class);
			final ApiTableRowData apiData = apiService.getApi(calendar.getOAuthCredentialId());
			apiHelper = get(apiData);
		}

		return apiHelper;
	}

	/**
	 * Useful to get access to Abstract Base API Helper
	 *
	 * @return
	 */
	public static ApiHelper getCommonApiHelper() {
		return BEANS.get(GoogleApiHelper.class);
	}

}
