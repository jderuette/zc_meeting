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

import org.zeroclick.meeting.client.api.microsoft.data.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @author djer
 *
 */
public interface TokenService {

	@FormUrlEncoded
	@POST("/{tenantid}/oauth2/v2.0/token")
	Call<TokenResponse> getAccessTokenFromAuthCode(@Path("tenantid") String tenantId,
			@Field("client_id") String clientId, @Field("client_secret") String clientSecret,
			@Field("grant_type") String grantType, @Field("code") String code,
			@Field("redirect_uri") String redirectUrl);

	@FormUrlEncoded
	@POST("/{tenantid}/oauth2/v2.0/token")
	Call<TokenResponse> getAccessTokenFromRefreshToken(@Path("tenantid") String tenantId,
			@Field("client_id") String clientId, @Field("client_secret") String clientSecret,
			@Field("grant_type") String grantType, @Field("refresh_token") String code,
			@Field("redirect_uri") String redirectUrl);

}
