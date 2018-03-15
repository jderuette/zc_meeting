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
package org.zeroclick.meeting.client.api.microsoft;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.text.StringHelper;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.client.api.AbstractApiHelper;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.MicrosoftUser;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.data.TokenResponse;
import org.zeroclick.meeting.client.api.microsoft.service.CalendarService;
import org.zeroclick.meeting.client.api.microsoft.service.MicrosoftServiceBuilder;
import org.zeroclick.meeting.client.api.microsoft.service.OutlookService;
import org.zeroclick.meeting.client.api.microsoft.service.TokenService;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.service.CalendarAviability;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData.CalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;

/**
 * @author djer
 *
 */
public class MicrosoftApiHelper extends AbstractApiHelper<String, CalendarService> {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftApiHelper.class);

	private MicrosoftEventHelper eventHelper;

	public static final String AUTHORITY = "https://login.microsoftonline.com";
	private static final String AUTHORIZE_URL = AUTHORITY + "/common/oauth2/v2.0/authorize";

	private static String[] scopes = { "openid", "offline_access", "profile", "User.Read", "Calendars.Read" };

	private String appId;
	private String appPassword;
	private String redirectUrl;

	public MicrosoftApiHelper() {
		this.loadConfig();
	}

	private String getAppId() {
		return this.appId;
	}

	private String getAppPassword() {
		return this.appPassword;
	}

	private String getRedirectUrl() {
		return this.redirectUrl;
	}

	private static String getScopes() {
		final StringBuilder builder = new StringBuilder();
		for (final String scope : scopes) {
			builder.append(scope).append(' ');
		}
		return builder.toString().trim();
	}

	private void loadConfig() {
		this.appId = "fedc1131-18df-41ab-a693-c90c430167c3";
		this.appPassword = "aSYXS918?}^vlabsoDWP41?";
		this.redirectUrl = "http://localhost:8082/api/microsoft/oauth2callback";
	}

	protected MicrosoftEventHelper getEventHelper() {
		if (null == this.eventHelper) {
			this.eventHelper = new MicrosoftEventHelper();
		}
		return this.eventHelper;
	}

	public String getLoginUrl(final UUID state, final UUID nonce) {

		final UriBuilder urlBuilder = new UriBuilder(AUTHORIZE_URL);
		urlBuilder.parameter("client_id", this.getAppId());
		urlBuilder.parameter("redirect_uri", this.getRedirectUrl());
		urlBuilder.parameter("response_type", "code id_token");
		urlBuilder.parameter("scope", getScopes());
		urlBuilder.parameter("state", state.toString());
		urlBuilder.parameter("nonce", nonce.toString());
		urlBuilder.parameter("response_mode", "form_post");

		return urlBuilder.createURI().toString();
	}

	public TokenResponse getTokenFromAuthCode(final String authCode, final String tenantId) {
		LOG.info("Generating AccesToken with authCode : " + authCode);
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);
		final TokenService tokenService = microsoftServiceBuilder.getTokenService();

		TokenResponse response;

		try {
			response = tokenService.getAccessTokenFromAuthCode(tenantId, this.getAppId(), this.getAppPassword(),
					"authorization_code", authCode, this.getRedirectUrl()).execute().body();
		} catch (final IOException e) {
			LOG.error("Error while gettig Token from code : " + authCode + " from tenantId : " + tenantId, e);
			final TokenResponse error = new TokenResponse();
			error.setError("IOException");
			error.setErrorDescription(e.getMessage());
			response = error;
		}

		return response;
	}

	public TokenResponse ensureTokens(final TokenResponse tokens, final String tenantId) {
		LOG.info("Checking if exisitng tokens are still valids");
		final Calendar now = Calendar.getInstance();
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);

		TokenResponse response = null;

		if (now.getTime().before(tokens.getExpirationTime())) {
			LOG.info("Tokens are still valids");
			response = tokens;
		} else {
			LOG.info("Tokens are expired, refreshing them");
			final TokenService tokenService = microsoftServiceBuilder.getTokenService();

			try {
				response = tokenService.getAccessTokenFromRefreshToken(tenantId, this.getAppId(), this.getAppPassword(),
						"refresh_token", tokens.getRefreshToken(), this.getRedirectUrl()).execute().body();
			} catch (final IOException e) {
				LOG.error("Error while refreshing Token with acces TOken : " + tokens.getAccessToken()
						+ " from tenantId : " + tenantId, e);
				final TokenResponse error = new TokenResponse();
				error.setError("IOException");
				error.setErrorDescription(e.getMessage());
				response = error;
			}
		}
		return response;
	}

	/**
	 * Used to check api creation during the "add calendar" process.
	 *
	 * @param accessToken
	 * @param userEmail
	 * @return
	 */
	public PagedResult<Event> getEvents(final String accessToken, final String userEmail) {
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);

		final CalendarService calendarService = microsoftServiceBuilder.getCalendarService(accessToken, userEmail);

		// Sort by start time in descending order
		final String sort = "start/dateTime DESC";
		// Only return the properties we care about
		final String properties = "organizer,subject,start,end";
		// Return at most 10 events
		final Integer maxResults = 10;

		PagedResult<Event> events = null;
		try {
			events = calendarService.getEvents(sort, properties, maxResults).execute().body();
		} catch (final IOException e) {
			LOG.error("Microsft API error while trying to retrieve Events", e);
		}

		return events;
	}

	@Override
	public String getAccountEmail(final Long apiCredentialId) {
		LOG.info("Retrieving (Microsoft) connected user account email");
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);
		final OutlookService outlookService = microsoftServiceBuilder.getOutlookService(apiCredentialId, null);

		String email = null;

		MicrosoftUser currentUser = null;
		try {
			currentUser = outlookService.getCurrentUser().execute().body();
		} catch (final IOException e) {
			LOG.error("Microsft API error while trying to get user account's Email", e);
		}

		if (null != currentUser) {
			if (null != currentUser.getMail()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("User Account email, retrieve using the email filed of connected User");
				}
				email = currentUser.getMail();
			} else {
				final String userPricnipalName = currentUser.getUserPrincipalName();
				final StringHelper stringHelper = BEANS.get(StringHelper.class);
				if (stringHelper.isValidEmail(userPricnipalName)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(
								"User Account email, retrieve using the UserPrincipalName (wich is an email) filed of connected User");
					}
					email = userPricnipalName;
				}
			}
		}

		return email;
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public List<ApiCredential<String>> getCredentials(final Long userId) throws IOException {
		final IApiService apiService = BEANS.get(IApiService.class);
		final List<ApiCredential<String>> credentials = new ArrayList<>();

		final ApiTablePageData userApis = apiService.getApis(userId);

		if (null != userApis && userApis.getRowCount() > 0) {
			for (final ApiTableRowData aUserApi : userApis.getRows()) {
				credentials.add(this.getApiCredential(aUserApi));
			}
		}
		return credentials;
	}

	protected ApiCredential<String> getApiCredential(final Long apiCredentialId) {
		final IApiService apiService = BEANS.get(IApiService.class);

		final ApiTableRowData apiData = apiService.getApi(apiCredentialId);

		final String credential = this.getCredential(apiCredentialId);

		return new ApiCredential<>(credential, apiData);
	}

	@Override
	protected ApiCredential<String> getApiCredential(final ApiTableRowData apiData) {
		String credential = null;
		if (null == apiData.getAccessToken()) {
			credential = this.getCredential(apiData.getApiCredentialId());
		} else {
			credential = apiData.getAccessToken();
		}
		return new ApiCredential<>(credential, apiData);
	}

	@Override
	public String getCredential(final Long oAuthCredentialId) {
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiFormData userApiData = apiService.load(oAuthCredentialId);
		return userApiData.getAccessToken().getValue();
	}

	@Override
	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final Long userId) {
		LOG.info("Retrieving (Microsoft) calendars for userId : " + userId);
		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();

		List<ApiCalendar<CalendarService, ApiCredential<String>>> calendarsServices;
		try {
			calendarsServices = this.getCalendarsServices(userId);
		} catch (final IOException e1) {
			LOG.warn("Cannot get calendars lists for user : " + userId);
			return calendars;
		}

		if (null != calendarsServices && calendarsServices.size() > 0) {
			for (final ApiCalendar<CalendarService, ApiCredential<String>> calendarService : calendarsServices) {
				try {
					final PagedResult<org.zeroclick.meeting.client.api.microsoft.data.Calendar> calendarsList = calendarService
							.getCalendar().getCalendars().execute().body();

					// final List<CalendarListEntry> calendarItems =
					// calendarsList.getItems();
					for (final org.zeroclick.meeting.client.api.microsoft.data.Calendar calendarItem : calendarsList
							.getValue()) {
						final StringBuilder calendarId = new StringBuilder();
						calendarId.append(userId).append('_').append(calendarItem.getId()).append('_')
								.append(calendarService.getMetaData().getApiCredentialId());
						calendars.put(calendarId.toString(), this.toCalendarConfig(calendarItem, userId,
								calendarService.getMetaData().getApiCredentialId()));
					}

				} catch (final IOException e) {
					LOG.warn("Cannot get (Google) calendar Service for user : " + userId, e);
					throw new VetoException("Cannot get your Google calendar Data");
				}
			}
		}

		return calendars;
	}

	@Override
	public String createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final String subject,
			final Long forUserId, final String location, final String withEmail, final Boolean guestAutoAcceptMeeting,
			final String envDisplay, final CalendarConfigurationFormData calendarToStoreEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Boolean delete(final String calendarId, final String eventId, final CalendarService calendarService) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;
	}

	@Override
	public Boolean acceptEvent(final EventIdentification eventOrganizerIdentification, final String attendeeEmail,
			final ApiTableRowData eventCreatorApi) {
		// TODO Auto-generated method stub
		return Boolean.FALSE;
	}

	@Override
	public ApiCalendar<CalendarService, ApiCredential<String>> getCalendarService(final Long apiCredentialId) {
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);
		final ApiCredential<String> apiCredential = this.getApiCredential(apiCredentialId);

		final CalendarService mCalendarService = microsoftServiceBuilder.getCalendarService(
				apiCredential.getMetaData().getAccessToken(), apiCredential.getMetaData().getAccountEmail());

		if (null != mCalendarService) {
			return new ApiCalendar<>(mCalendarService, apiCredential);
		}

		throw new UserAccessRequiredException();
	}

	@Override
	public String getEventHtmlLink(final EventIdentification eventIdentification, final Long eventHeldBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CalendarAviability getCalendarAviability(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId, final AbstractCalendarConfigurationTableRowData calendar, final ZoneId userZoneId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 * @throws UserAccessRequiredException
	 *             when no credential can be found for the user
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public List<ApiCalendar<CalendarService, ApiCredential<String>>> getCalendarsServices(final Long userId)
			throws IOException {
		final List<ApiCalendar<CalendarService, ApiCredential<String>>> calendarsServices = new ArrayList<>();
		final List<ApiCredential<String>> apiCredentials = this.getCredentials(userId);
		if (null != apiCredentials && apiCredentials.size() > 0) {
			for (final ApiCredential<String> apiCredential : apiCredentials) {
				final ApiCalendar<CalendarService, ApiCredential<String>> apiCalendarsService = this
						.getCalendarService(apiCredential.getMetaData().getApiCredentialId());
				calendarsServices.add(apiCalendarsService);
			}
			return calendarsServices;
		}
		throw new UserAccessRequiredException();
	}

	private AbstractCalendarConfigurationTableRowData toCalendarConfig(
			final org.zeroclick.meeting.client.api.microsoft.data.Calendar cal, final Long userId,
			final Long apiCredentialId) throws IOException {
		LOG.debug("Creating model data for (Google) calendar data : " + cal);
		final CalendarConfigurationTableRowData calendarConfigData = new CalendarConfigurationTableRowData();
		calendarConfigData.setExternalId(cal.getId());
		calendarConfigData.setName(cal.getName());
		// TODO Djer13 a way to know if a calendar is the main ?
		calendarConfigData.setMain(null);
		calendarConfigData.setReadOnly(!cal.getCanEdit());
		calendarConfigData.setUserId(userId);
		calendarConfigData.setOAuthCredentialId(apiCredentialId);
		LOG.debug("Calendar Config model created with externalId : " + calendarConfigData.getExternalId()
				+ ", UserId : " + calendarConfigData.getUserId() + ", OAuthCredentialId : "
				+ calendarConfigData.getOAuthCredentialId());
		return calendarConfigData;
	}

	public String aslog(final Event event) {
		if (null != event) {
			final StringBuilder builder = new StringBuilder(100);
			builder.append("Microsoft Event : ").append(event.getId()).append(", start : ").append(event.getStart())
					.append(", end : ").append(event.getEnd()).append(", subject : ").append(event.getSubject())
					.append(" , organizer : ").append(event.getOrganizer());
			return builder.toString();
		}
		return "";
	}

}
