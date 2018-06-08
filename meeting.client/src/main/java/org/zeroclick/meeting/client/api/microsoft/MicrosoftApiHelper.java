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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.text.StringHelper;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.api.AbstractApiHelper;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.microsoft.data.Attendee;
import org.zeroclick.meeting.client.api.microsoft.data.DateTimeTimeZone;
import org.zeroclick.meeting.client.api.microsoft.data.EmailAddress;
import org.zeroclick.meeting.client.api.microsoft.data.Event;
import org.zeroclick.meeting.client.api.microsoft.data.ItemBody;
import org.zeroclick.meeting.client.api.microsoft.data.Location;
import org.zeroclick.meeting.client.api.microsoft.data.MicrosoftUser;
import org.zeroclick.meeting.client.api.microsoft.data.PagedResult;
import org.zeroclick.meeting.client.api.microsoft.data.ResponseStatus;
import org.zeroclick.meeting.client.api.microsoft.data.TokenResponse;
import org.zeroclick.meeting.client.api.microsoft.service.CalendarService;
import org.zeroclick.meeting.client.api.microsoft.service.MicrosoftServiceBuilder;
import org.zeroclick.meeting.client.api.microsoft.service.OutlookService;
import org.zeroclick.meeting.client.api.microsoft.service.TokenService;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData.CalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.rt.plateform.config.AbstractUrlConfigProperty;

import com.google.api.client.util.IOUtils;

/**
 * @author djer
 *
 */
public class MicrosoftApiHelper extends AbstractApiHelper<String, CalendarService> {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftApiHelper.class);

	private MicrosoftEventHelper eventHelper;

	public static final String AUTHORITY = "https://login.microsoftonline.com";
	private static final String AUTHORIZE_URL = AUTHORITY + "/common/oauth2/v2.0/authorize";

	public static final String ADD_MICROSOFT_CALENDAR_URL = "/api/microsoft/addCalendar";

	private static String[] scopes = { "openid", "offline_access", "profile", "User.Read", "Calendars.ReadWrite" };

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

	@Override
	public String getAuthorisationLink() {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("<a>").append(this.buildAutorisationUrl(TEXTS.get("zc.api.provider.microsoft"))).append("</a>");

		return builder.toString();
	}

	@Override
	public String getAuthorisationLinksAsLi() {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("<li>").append(this.buildAutorisationUrl(TEXTS.get("zc.api.provider.microsoft.outlook")))
				.append("</li><li>").append(this.buildAutorisationUrl(TEXTS.get("zc.api.provider.microsoft.skype")))
				.append("</li><li>").append(this.buildAutorisationUrl(TEXTS.get("zc.api.provider.microsoft.exchange")))
				.append("</li><li>").append(this.buildAutorisationUrl(TEXTS.get("zc.api.provider.microsoft.office365")))
				.append("</li>");

		return builder.toString();
	}

	private String buildAutorisationUrl(final String label) {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("<a href='").append(ADD_MICROSOFT_CALENDAR_URL).append("' target='_blank'>").append(label)
				.append("</a>");

		return builder.toString();
	}

	private void loadConfig() {
		this.appId = CONFIG.getPropertyValue(MicrosoftClientIdProperty.class);
		this.appPassword = CONFIG.getPropertyValue(MicrosoftClientSecretProperty.class);
		this.redirectUrl = getRedirectUri();
	}

	public static String getRedirectUri() {
		return BEANS.get(MicrosoftCallbackUrlProperty.class).getAbsoluteURI();
		// return
		// CONFIG.getPropertyValue(MicrosoftCallbackUrlProperty.class).get;
	}

	@Override
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

		final String uri = urlBuilder.createURI().toString();

		LOG.info("User will be redirected to : " + uri);

		return uri;
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

	public TokenResponse ensureTokens(final Long apiCredentialId) {
		LOG.info("Checking if exisitng tokens are still valids for apiId : " + apiCredentialId);
		final Calendar now = Calendar.getInstance();
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);

		final ApiFormData savedData = this.getSavecApiCredential(apiCredentialId);
		final TokenResponse existingTokens = this.getTokenResponse(savedData);

		TokenResponse response = null;

		if (now.getTime().before(existingTokens.getExpirationTime())) {
			LOG.debug("Tokens are still valids");
			response = existingTokens;
		} else {
			LOG.info("Tokens are expired, refreshing them");
			final TokenService tokenService = microsoftServiceBuilder.getTokenService();

			try {
				response = tokenService.getAccessTokenFromRefreshToken(savedData.getTenantId().getValue(),
						this.getAppId(), this.getAppPassword(), "refresh_token", existingTokens.getRefreshToken(),
						this.getRedirectUrl()).execute().body();

				// Store the new Token for this Api Id
				savedData.getAccessToken().setValue(response.getAccessToken());
				savedData.getRefreshToken().setValue(response.getRefreshToken());
				savedData.getProvider().setValue(ProviderCodeType.MicrosoftCode.ID);
				savedData.getExpirationTimeMilliseconds().setValue(response.getExpirationTime().getTime());
				savedData.setProviderData(IOUtils.serialize(response));

				final IApiService apiService = BEANS.get(IApiService.class);
				apiService.store(savedData);

			} catch (final IOException e) {
				LOG.error("Error while refreshing Token with acces Token : " + existingTokens.getAccessToken()
						+ " from tenantId : " + savedData.getTenantId().getValue(), e);
				final TokenResponse error = new TokenResponse();
				error.setError("IOException");
				error.setErrorDescription(e.getMessage());
				response = error;
			}
		}
		return response;
	}

	private ApiFormData getSavecApiCredential(final Long apiCredentialId) {
		final IApiService apiService = BEANS.get(IApiService.class);

		final ApiFormData input = new ApiFormData();
		input.setApiCredentialId(apiCredentialId);

		final ApiFormData apiData = apiService.load(input);

		return apiData;
	}

	private TokenResponse getTokenResponse(final ApiFormData OAuthData) {
		TokenResponse tokens = null;
		try {
			tokens = IOUtils.deserialize(OAuthData.getProviderData());
		} catch (final IOException ioe) {
			LOG.error("Erro while trying to desirialize Saved Token Response from provider Data : "
					+ OAuthData.getProviderData(), ioe);
		}
		return tokens;
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
			for (final ApiCalendar<CalendarService, ApiCredential<String>> calendarApiService : calendarsServices) {
				calendars.putAll(this.buildCalendarConfig(calendarApiService));
			}
		}

		return calendars;
	}

	private Map<String, AbstractCalendarConfigurationTableRowData> buildCalendarConfig(
			final ApiCalendar<CalendarService, ApiCredential<String>> calendarApiService) {
		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();
		final Long userId = calendarApiService.getMetaData().getUserId();
		final Long apiId = calendarApiService.getMetaData().getApiCredentialId();
		try {
			final PagedResult<org.zeroclick.meeting.client.api.microsoft.data.Calendar> calendarsList = calendarApiService
					.getCalendar().getCalendars().execute().body();

			if (null != calendarsList && null != calendarsList.getValue()) {
				for (final org.zeroclick.meeting.client.api.microsoft.data.Calendar calendarItem : calendarsList
						.getValue()) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final StringBuilder calendarId = new StringBuilder();
					calendarId.append(userId).append('_').append(calendarItem.getId()).append('_').append(apiId);
					calendars.put(calendarId.toString(), this.toCalendarConfig(calendarItem, userId, apiId));
				}
			}

		} catch (final IOException e) {
			LOG.warn("Cannot get (Microsoft) calendar Service for user : " + userId, e);
			throw new VetoException("Cannot get your (Microsoft) calendar Data");
		}

		return calendars;
	}

	@Override
	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final ApiTableRowData aUserApi) {
		Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();
		final ApiCalendar<CalendarService, ApiCredential<String>> calendarApiService = this
				.getCalendarService(aUserApi.getApiCredentialId());

		calendars = this.buildCalendarConfig(calendarApiService);

		return calendars;
	}

	@Override
	public String createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final String subject,
			final Long forUserId, final String location, final String withEmail, final Boolean guestAutoAcceptMeeting,
			final String envDisplay, final CalendarConfigurationFormData calendarToStoreEvent,
			final String description) {

		final Event newEvent = new Event();

		newEvent.setStart(this.toProviderDateTime(startDate));
		newEvent.setEnd(this.toProviderDateTime(endDate));
		newEvent.setSubject(envDisplay + " " + subject + TextsHelper.get(forUserId, "zc.common.email.subject.suffix"));

		final Location microsoftLocation = new Location();
		final String locationText = TextsHelper.get(forUserId, location);
		microsoftLocation.setDisplayName(locationText);

		newEvent.setLocation(microsoftLocation);
		newEvent.setBodyPreview(subject);

		final ItemBody body = new ItemBody();
		body.setContentType("HTML");
		body.setContent(description);
		newEvent.setBody(body);

		final Collection<Attendee> attendees = new ArrayList<>();

		final Attendee atendee = new Attendee();
		final EmailAddress attendeeEmailAdress = new EmailAddress();
		attendeeEmailAdress.setAddress(withEmail);

		atendee.setEmailAddress(attendeeEmailAdress);

		final ResponseStatus status = new ResponseStatus();
		if (guestAutoAcceptMeeting) {
			status.setResponse("Accepted");
		} else {
			status.setResponse("None");
		}
		atendee.setStatus(status);

		attendees.add(atendee);
		newEvent.setAttendees(attendees);

		newEvent.setCategories(new ArrayList<>());

		newEvent.setResponseRequested(Boolean.FALSE);

		final Event event = this.getEventHelper().create(newEvent, calendarToStoreEvent);

		return null == event ? null : event.getId();
	}

	private DateTimeTimeZone toProviderDateTime(final ZonedDateTime startDate) {
		return this.getEventHelper().toProviderDateTime(startDate);
	}

	@Override
	protected Boolean delete(final String calendarId, final String eventId, final CalendarService calendarService) {
		Boolean eventDeleted = Boolean.FALSE;
		LOG.info(new StringBuilder().append("Deleting a Microsoft event : ").append(eventId).append(" in calendar : ")
				.append(eventId).toString());
		try {
			calendarService.deleteEvent(eventId).execute().body();
			eventDeleted = Boolean.TRUE;
		} catch (final IOException ioe) {
			LOG.error("Error while deleting (Microsoft) event", ioe);
			eventDeleted = Boolean.FALSE;
		}
		return eventDeleted;
	}

	@Override
	public Boolean acceptEvent(final EventIdentification eventOrganizerIdentification, final String attendeeEmail,
			final ApiTableRowData eventCreatorApi) {
		final Boolean eventUpdated = Boolean.FALSE;

		final ApiCalendar<CalendarService, ApiCredential<String>> apiCalendarService = this
				.getCalendarService(eventCreatorApi.getApiCredentialId());

		final Event organizerEvent = this.getEventHelper().getEvent(eventOrganizerIdentification,
				apiCalendarService.getCalendar());

		if (null == organizerEvent) {
			return eventUpdated;// early Break
		}

		final Collection<Attendee> attendees = organizerEvent.getAttendees();
		for (final Attendee eventAttendee : attendees) {
			if (attendeeEmail.equals(eventAttendee.getEmailAddress())) {
				if (LOG.isDebugEnabled()) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final StringBuilder builder = new StringBuilder();
					LOG.debug(builder.append("Updating (Microsoft) event : ").append(organizerEvent.getSubject())
							.append(" as accepted for attendee : ").append(eventAttendee.getEmailAddress()).toString());
				}

				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final ResponseStatus acceptedStatus = new ResponseStatus();
				acceptedStatus.setResponse("Accepted");
				eventAttendee.setStatus(acceptedStatus);
			}
		}

		try {
			apiCalendarService.getCalendar().updateEvent(eventOrganizerIdentification.getEventId(), organizerEvent)
					.execute().body();
		} catch (final IOException e) {
			LOG.error("Error while accepting (Microsoft) Event" + eventOrganizerIdentification + " by attendee : "
					+ attendeeEmail);
		}

		return Boolean.FALSE;
	}

	@Override
	public ApiCalendar<CalendarService, ApiCredential<String>> getCalendarService(final Long apiCredentialId) {
		final MicrosoftServiceBuilder microsoftServiceBuilder = BEANS.get(MicrosoftServiceBuilder.class);

		this.ensureTokens(apiCredentialId);

		final ApiCredential<String> apiCredential = this.getApiCredential(apiCredentialId);

		final CalendarService mCalendarService = microsoftServiceBuilder.getCalendarService(
				apiCredential.getMetaData().getAccessToken(), apiCredential.getMetaData().getAccountEmail());

		if (null != mCalendarService) {
			return new ApiCalendar<>(mCalendarService, apiCredential);
		}

		throw new UserAccessRequiredException();
	}

	@Override
	public String getEventHtmlLink(final EventIdentification eventIdentification, final Long apiCredentialId) {
		final ApiCalendar<CalendarService, ApiCredential<String>> calendarService = this
				.getCalendarService(apiCredentialId);

		final Event event = this.getEventHelper().getEvent(eventIdentification, calendarService.getCalendar());

		return this.getEventHelper().getHmlLink(event);
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
	private List<ApiCalendar<CalendarService, ApiCredential<String>>> getCalendarsServices(final Long userId)
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
		LOG.debug("Creating model data for (Microsoft) calendar data : " + cal);
		final CalendarConfigurationTableRowData calendarConfigData = new CalendarConfigurationTableRowData();
		Boolean isMain = Boolean.FALSE;

		// TODO Djer13 a way to know if a calendar is the main ?
		if ("Calendar".equals(cal.getName()) || "Calendrier".equals(cal.getName())) {
			isMain = Boolean.TRUE;
		}

		calendarConfigData.setExternalId(cal.getId());
		calendarConfigData.setName(cal.getName());
		calendarConfigData.setMain(isMain);
		calendarConfigData.setReadOnly(!cal.getCanEdit());
		calendarConfigData.setUserId(userId);
		calendarConfigData.setOAuthCredentialId(apiCredentialId);
		LOG.debug("Calendar Config model created with externalId : " + calendarConfigData.getExternalId()
				+ ", UserId : " + calendarConfigData.getUserId() + ", OAuthCredentialId : "
				+ calendarConfigData.getOAuthCredentialId());
		return calendarConfigData;
	}

	public String aslog(final Event event) {
		return this.getEventHelper().asLog(event);
	}

	/**
	 * Client id. This is recommended to use a credential File.
	 */
	public static class MicrosoftClientIdProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "none";
		}

		@Override
		public String getKey() {
			return "contacts.api.microsoft.client.id";
		}
	}

	/**
	 * Client secret. This is recommended to use a credential File.
	 */
	public static class MicrosoftClientSecretProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "noneSecret";
		}

		@Override
		public String getKey() {
			return "contacts.api.microsoft.client.secret";
		}
	}

	public static class MicrosoftCallbackUrlProperty extends AbstractUrlConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "/api/microsoft/oauth2callback";
		}

		@Override
		public String getKey() {
			return "contacts.api.microsoft.callback.url";
		}

		@Override
		protected String getDefautlBaseUrl() {
			return CONFIG.getPropertyValue(ApplicationUrlProperty.class);
		}
	}
}
