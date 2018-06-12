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

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper.MicrosoftCallbackUrlProperty;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.IdToken;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.data.TokenResponse;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.ui.html.AbstractApiServletRequestHandler;
import org.zeroclick.meeting.ui.html.HtmlResponseHelper;

import com.google.api.client.util.IOUtils;

/**
 * @author djer
 *
 */
public class MicrosoftApiOAuthCallback extends AbstractApiServletRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftApiOAuthCallback.class);

	private final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);

	private final String registeredPath;

	public MicrosoftApiOAuthCallback() {
		this.registeredPath = BEANS.get(MicrosoftCallbackUrlProperty.class).getRelativeUri();
	}

	@Override
	protected String getRegistredServletPath() {
		return this.registeredPath;
	}

	@Override
	protected Logger getLogger() {
		return LOG;
	}

	@Override
	protected boolean processPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final HtmlResponseHelper responseHelper = new HtmlResponseHelper();

		final Long currentUsderId = this.microsoftApiHelper.getCurrentUserId();
		LOG.info("Microsfot API Storing User token for user Id : " + currentUsderId);

		final HttpSession session = req.getSession();

		final String expectedNonce = this.getUUIDSessionValue(session, "nonce");
		if (null == expectedNonce) {
			responseHelper.addErrorMessage("No nonce attribute in current Session, canno't create new (microsfot) API");
			return false; // early break
		}

		final String expectedState = this.getUUIDSessionValue(session, "state");
		if (null == expectedState) {
			responseHelper.addErrorMessage("No state attribute in current Session, canno't create new (microsfot) API");
			return false; // early break
		}

		ApiFormData userApi = null;
		// Store in DB
		final IApiService apiService = BEANS.get(IApiService.class);

		final String code = req.getParameter("code");
		final String idToken = req.getParameter("id_token");
		final String state = req.getParameter("state");

		if (expectedState.equals(state)) {
			final IdToken idTokenObj = IdToken.parseEncodedToken(idToken, expectedNonce);
			if (idTokenObj != null) {
				// generate accessToken
				final TokenResponse oAuthTokens = this.microsoftApiHelper.getTokenFromAuthCode(code,
						idTokenObj.getTenantId());

				if (null != oAuthTokens) {
					final ApiFormData input = new ApiFormData();
					input.setUserId(Long.valueOf(currentUsderId));
					input.getProvider().setValue(ProviderCodeType.MicrosoftCode.ID);

					input.getAccessToken().setValue(oAuthTokens.getAccessToken());
					input.getAccountEmail().setValue(idTokenObj.getEmail());
					input.getRefreshToken().setValue(oAuthTokens.getRefreshToken());
					input.getProvider().setValue(ProviderCodeType.MicrosoftCode.ID);
					input.getExpirationTimeMilliseconds().setValue(oAuthTokens.getExpirationTime().getTime());
					input.getTenantId().setValue(idTokenObj.getTenantId());
					input.setProviderData(IOUtils.serialize(oAuthTokens));

					final ApiFormData createdData = apiService.create(input, Boolean.FALSE);

					String accountEmail = null;
					if (null == input.getAccountEmail().getValue()) {
						// No Email in IdObject, try with outlookService
						accountEmail = this.microsoftApiHelper.getAccountEmail(createdData.getApiCredentialId());
					}

					if (null == accountEmail) {
						final StringBuilder builder = new StringBuilder();
						builder.append("Cannot get User account email to store in his api for user ");
						responseHelper.addErrorMessage(builder.toString());
						builder.append(currentUsderId);
						LOG.error(builder.toString());
					} else {
						userApi = apiService.storeAccountEmail(createdData, accountEmail);
					}

					// try to get event to check connection
					final PagedResult<Event> events = this.microsoftApiHelper.getEvents(oAuthTokens.getAccessToken(),
							idTokenObj.getEmail());
					this.debugLastEvent(events);
				} else {
					final StringBuilder builder = new StringBuilder();
					builder.append("No acces Token generate with Id Token with email : ").append(idTokenObj.getEmail());
					responseHelper.addErrorMessage(builder.toString());
					LOG.error(builder.toString());
				}
			} else {
				final StringBuilder builder = new StringBuilder();
				builder.append("Microsft API cannot generate AccessToken");
				responseHelper.addErrorMessage(builder.toString());
				builder.append(" from code : ").append(code)
						.append(" idTokenObj is nul, id token String to parse is : ").append(idToken);
				LOG.error(builder.toString());
			}
		} else {
			final StringBuilder builder = new StringBuilder();
			builder.append("Invalid state, canno't create new (microsfot) API");
			responseHelper.addErrorMessage(builder.toString());
			builder.append(", expected : ").append(expectedState).append(", actual :").append(state);
			LOG.error(builder.toString());
		}

		if (null != userApi) {
			responseHelper.addSuccessMessage("Microsoft connection OK");
		}

		resp.getWriter().append(responseHelper.getPageContent());

		return true;
	}

	private String getUUIDSessionValue(final HttpSession session, final String paramName) {
		final Object paramValue = this.getSessionParam(session, paramName);
		final String uuidAsString = null == paramValue ? null : ((UUID) paramValue).toString();

		return uuidAsString;
	}

	private void debugLastEvent(final PagedResult<Event> events) {
		if (null == events || events.getValue().length == 0) {
			LOG.info("No upcomming events");
		} else {
			LOG.info(events.getValue().length + " Upcoming events");
			if (LOG.isDebugEnabled()) {
				for (final Event event : events.getValue()) {
					this.microsoftApiHelper.aslog(event);
				}
			}
		}
	}
}
