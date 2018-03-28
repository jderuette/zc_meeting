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
package org.zeroclick.meeting.client.api.microsoft.service;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class MicrosoftServiceBuilder {
	/**
	 * Create a request interceptor to add headers that belong on every request
	 *
	 * @param accessToken
	 *            : accesToken to identify User doing request
	 * @param userEmail
	 *            : user email (mainly used to identify his mail box)
	 * @return
	 */
	private Interceptor buildRequestInterceptor(final String accessToken, final String userEmail) {
		final Interceptor requestInterceptor = new Interceptor() {
			@Override
			public Response intercept(final Interceptor.Chain chain) throws IOException {
				final Request original = chain.request();
				Builder builder = original.newBuilder().header("User-Agent", "java-tutorial")
						.header("client-request-id", UUID.randomUUID().toString())
						.header("return-client-request-id", "true")
						.header("Authorization", String.format("Bearer %s", accessToken))
						.method(original.method(), original.body());

				if (userEmail != null && !userEmail.isEmpty()) {
					builder = builder.header("X-AnchorMailbox", userEmail);
				}

				final Request request = builder.build();
				return chain.proceed(request);
			}
		};

		return requestInterceptor;
	}

	private HttpLoggingInterceptor buildHttpLogingInterceptor() {
		// Create a logging interceptor to log request and responses
		final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

		return loggingInterceptor;
	}

	private OkHttpClient buildClient(final Interceptor requestInterceptor) {
		final okhttp3.OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
				.addInterceptor(this.buildHttpLogingInterceptor());

		if (null != requestInterceptor) {
			okHttpClientBuilder.addInterceptor(requestInterceptor);
		}

		return okHttpClientBuilder.build();
	}

	private Retrofit buildRetrofit(final String baseUrl, final OkHttpClient client) {
		// Create and configure the Retrofit object
		final ObjectMapper customObjectMapper = new ObjectMapper();

		return new Retrofit.Builder().baseUrl(baseUrl).client(client)
				.addConverterFactory(JacksonConverterFactory.create(customObjectMapper)).build();
	}

	public CalendarService getCalendarService(final String accessToken, final String userEmail) {
		final Retrofit retrofit = this.buildRetrofit("https://graph.microsoft.com",
				this.buildClient(this.buildRequestInterceptor(accessToken, userEmail)));
		// Generate the token service
		return retrofit.create(CalendarService.class);
	}

	public TokenService getTokenService() {
		final Retrofit retrofit = this.buildRetrofit(MicrosoftApiHelper.AUTHORITY, this.buildClient(null));
		// Generate the token service
		return retrofit.create(TokenService.class);
	}

	public OutlookService getOutlookService(final Long apiCredentialId, final String userEmail) {
		final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);
		microsoftApiHelper.ensureTokens(apiCredentialId);
		final String accessToken = this.getToken(apiCredentialId);
		final Retrofit retrofit = this.buildRetrofit("https://graph.microsoft.com",
				this.buildClient(this.buildRequestInterceptor(accessToken, userEmail)));
		// Generate the token service
		return retrofit.create(OutlookService.class);
	}

	private String getToken(final Long apiCredentialId) {
		final IApiService apiService = BEANS.get(IApiService.class);
		String accessToken = null;

		final ApiFormData apiData = apiService.load(apiCredentialId);
		if (null != apiData) {
			accessToken = apiData.getAccessToken().getValue();
		}

		return accessToken;
	}
}
