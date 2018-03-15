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
package org.zeroclick.meeting.ui.html.calendar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.IdToken;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.data.TokenResponse;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

/**
 * @author djer
 *
 */
public class MicrosoftApiOAuthCallback extends AbstractUiServletRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftApiOAuthCallback.class);

	private final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);

	private static final String SERVLET_PATH = "/api/microsoft/oauth2callback";

	@Override
	public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		if (!req.getPathInfo().equals(SERVLET_PATH)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(
						new StringBuilder().append("MicrosoftApiOAuthCallback ignore the request because requested : [")
								.append(req.getPathInfo()).append("] path does not match configured : [")
								.append(SERVLET_PATH).append(']').toString());
			}
			return false;
		}

		final Long currentUsderId = this.microsoftApiHelper.getCurrentUserId();
		LOG.info("Microsfot API Storing User token for user Id : " + currentUsderId);

		// if (LOG.isDebugEnabled()) {
		// LOG.debug(this.getBody(req));
		// }

		// Store in DB
		final IApiService apiService = BEANS.get(IApiService.class);

		final ApiFormData input = new ApiFormData();
		input.setUserId(Long.valueOf(currentUsderId));
		input.getProvider().setValue(ProviderCodeType.MicrosoftCode.ID);

		final String code = req.getParameter("code");
		final String idToken = req.getParameter("id_token");
		final String state = req.getParameter("state");

		final HttpSession session = req.getSession();

		String expectedNonce = "";
		String expectedState = "";

		if (null != session && null != session.getAttribute("nonce") && null != session.getAttribute("state")) {
			expectedNonce = ((UUID) session.getAttribute("nonce")).toString();
			expectedState = ((UUID) session.getAttribute("state")).toString();

			if (expectedState.equals(state)) {
				final IdToken idTokenObj = IdToken.parseEncodedToken(idToken, expectedNonce);
				if (idTokenObj != null) {
					// generate accessToken
					final TokenResponse oAuthTokens = this.microsoftApiHelper.getTokenFromAuthCode(code,
							idTokenObj.getTenantId());

					if (null != oAuthTokens) {
						input.getAccessToken().setValue(oAuthTokens.getAccessToken());
						input.getAccountEmail().setValue(idTokenObj.getEmail());
						input.getRefreshToken().setValue(oAuthTokens.getRefreshToken());
						input.getProvider().setValue(ProviderCodeType.MicrosoftCode.ID);
						input.getExpirationTimeMilliseconds().setValue(oAuthTokens.getExpirationTime().getTime());

						final ApiFormData createdData = apiService.create(input, Boolean.FALSE);

						String accountEmail = null;
						if (null == input.getAccountEmail().getValue()) {
							// No Email in IdObject, try with outlookService
							accountEmail = this.microsoftApiHelper.getAccountEmail(createdData.getApiCredentialId());
						}

						if (null == accountEmail) {
							LOG.error("Cannot get User account email to store in his api for user " + currentUsderId);
						} else {
							apiService.storeAccountEmail(createdData, accountEmail);
						}

						// try to get event to check connection
						final PagedResult<Event> events = this.microsoftApiHelper
								.getEvents(oAuthTokens.getAccessToken(), idTokenObj.getEmail());
						this.debugLastEvent(events);
					} else {
						LOG.error("No acces Token generate with Id Token with email : " + idTokenObj.getEmail());
					}
				} else {
					LOG.error("Microsft API cannot generate AccessToken from code : " + code);
				}
			} else {
				LOG.error("Invalid state, canno't create new (microsfot) API");
			}
		} else {
			LOG.error("No nonce/state attribute in current Session, canno't create new (microsfot) API");
		}

		return true;

	}

	private void debugLastEvent(final PagedResult<Event> events) {

		if (null == events || events.getValue().length == 0) {
			System.out.println("No upcomming events");
		} else {
			System.out.println("Upcoming events");
			for (final Event event : events.getValue()) {
				this.microsoftApiHelper.aslog(event);
			}
		}

	}

	private String getBody(final HttpServletRequest request) throws IOException {

		String body = null;
		final StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			final InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				final char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (final IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (final IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

}
