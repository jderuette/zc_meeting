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
package org.zeroclick.meeting.client.api.google;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.api.AbstractApiHelper;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData.CalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.rt.plateform.config.AbstractUrlConfigProperty;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Person;

/**
 * @author djer
 *
 */
public class GoogleApiHelper extends AbstractApiHelper<Credential, Calendar> {

	private static final Logger LOG = LoggerFactory.getLogger(GoogleApiHelper.class);

	public static final String ADD_GOOGLE_CALENDAR_URL = "/api/microsot/addGoogleCalendar";
	private GoogleEventHelper eventHelper;

	private DataStoreFactory dataStoreFactory;

	@PostConstruct
	public void init() {
		this.dataStoreFactory = new ScoutDataStoreFactory();
	}

	@Override
	protected GoogleEventHelper getEventHelper() {
		if (null == this.eventHelper) {
			this.eventHelper = new GoogleEventHelper();
		}
		return this.eventHelper;
	}

	@Override
	public String getAuthorisationLink() {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("<a href='").append(ADD_GOOGLE_CALENDAR_URL).append("' target='_blank'>")
				.append(TEXTS.get("zc.api.provider.google")).append("</a>");

		return builder.toString();
	}

	@Override
	public String getAuthorisationLinksAsLi() {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("<li>").append(this.getAuthorisationLink()).append("</li>");

		return builder.toString();
	}

	private GoogleClientSecrets getClientSecret() throws IOException {
		final String fileName = CONFIG.getPropertyValue(GoogleClientCredentialFileProperty.class);
		final File clientSecretFile = new File(fileName);
		final Reader reader = Files.newBufferedReader(clientSecretFile.toPath());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting Client credentails using : " + fileName);
		}

		final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), reader);

		return clientSecrets;
	}

	private AuthorizationCodeFlow initializeFlow() throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initilazing Google API Flow");
		}
		final Collection<String> scopes = CollectionUtility.arrayList(CalendarScopes.CALENDAR,
				PeopleServiceScopes.USERINFO_PROFILE, PeopleServiceScopes.USER_EMAILS_READ);
		// Collections.singleton();

		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
				this.getClientSecret(), scopes).setDataStoreFactory(this.dataStoreFactory).setAccessType("offline")
						.setApprovalPrompt("force").build();
	}

	public static String getRedirectUri() {
		return BEANS.get(GoogleCallbackUrlProperty.class).getAbsoluteURI();
		// return CONFIG.getPropertyValue(GoogleCallbackUrlProperty.class);
	}

	/** Lock on the flow and credential. */
	private final Lock lock = new ReentrantLock();

	/**
	 * Authorization code flow to be used across all HTTP servlet requests or
	 * {@code null} before initialized in {@link #initializeFlow()}.
	 */
	private AuthorizationCodeFlow flow;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.client.calendar.IGoogleApiHelper#
	 * getOrRetrieveCredential(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void askUserCredential(final HttpServletResponse resp) throws IOException, ServletException {
		LOG.info("Asking user credential throught Google OAuth2 Authoriszation Flow");

		this.lock.lock();
		try {
			// redirect to the authorization flow
			if (null == this.flow) {
				this.flow = this.initializeFlow();
			}
			final AuthorizationCodeRequestUrl authorizationUrl = this.flow.newAuthorizationUrl();
			authorizationUrl.setRedirectUri(this.getRedirectUri());
			/**
			 * <pre>
			 * &#64;Override
			 * protected void onAuthorization(HttpServletRequest req, HttpServletResponse resp,
			 * 		AuthorizationCodeRequestUrl authorizationUrl) throws ServletException, IOException {
			 * 	authorizationUrl.setState("xyz");
			 * 	super.onAuthorization(req, resp, authorizationUrl);
			 * }
			 * </pre>
			 */
			resp.sendRedirect(authorizationUrl.build());
		} finally {
			this.lock.unlock();
		}
	}

	private List<ApiCredential<Credential>> getCredentials(final Long userId) throws IOException {
		final IApiService apiService = BEANS.get(IApiService.class);
		final List<ApiCredential<Credential>> credentials = new ArrayList<>();

		final ApiTablePageData userApis = apiService.getApis(userId);

		if (null != userApis && userApis.getRowCount() > 0) {
			for (final ApiTableRowData aUserApi : userApis.getRows()) {
				if (aUserApi.getProvider().equals(ProviderCodeType.GoogleCode.ID)) {
					final ApiCredential<Credential> apiCredential = this.getApiCredential(aUserApi);
					if (null != apiCredential) {
						credentials.add(apiCredential);
					}
				}
			}
		}
		return credentials;
	}

	protected ApiCredential<Credential> getApiCredential(final Long apiCredentialId) {
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiTableRowData apiData = apiService.getApi(apiCredentialId);
		return this.getApiCredential(apiData);
	}

	@Override
	protected ApiCredential<Credential> getApiCredential(final ApiTableRowData apiData) {
		ApiCredential<Credential> apiCrendetial = null;
		Credential cedential;
		cedential = this.getCredential(apiData.getApiCredentialId());
		apiCrendetial = new ApiCredential<>(cedential, apiData);
		return apiCrendetial;
	}

	@Override
	public Credential getCredential(final Long oAuthCredentialId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching for Credential for oAuthCredentialId : " + oAuthCredentialId);
		}

		Credential exitingCredential = null;
		if (null == this.flow) {
			try {
				this.flow = this.initializeFlow();
			} catch (final IOException ioe) {
				LOG.error("Cannot initialize Google Flow", ioe);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading Credential from Google Flow for oAuthCredentialId : " + oAuthCredentialId);
		}
		try {
			exitingCredential = this.flow.loadCredential(oAuthCredentialId.toString());
		} catch (final IOException ioe) {
			LOG.error("Cannot load user Credential with apiId : " + oAuthCredentialId);
		}
		if (null == exitingCredential) {
			LOG.warn("No save credential (Google) for oAuthCredentialId : " + oAuthCredentialId);
		}
		return exitingCredential;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.client.calendar.IGoogleApiHelper#
	 * tryStoreCredential( javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public Credential tryStoreCredential(final HttpServletRequest req, final HttpServletResponse resp,
			final Long oAuthCredentialId) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Trying to store credential for oAuthCredentialId : " + oAuthCredentialId);
		}
		this.callTracker.validateCanCall(oAuthCredentialId);
		Credential result = null;
		final StringBuffer buf = req.getRequestURL();
		if (req.getQueryString() != null) {
			buf.append('?').append(req.getQueryString());
		}
		final AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(buf.toString());
		final String code = responseUrl.getCode();
		if (responseUrl.getError() != null) {
			// onError(req, resp, responseUrl);
			// DO nothing default return is false;
			LOG.warn("Google response contain error : " + responseUrl.getError() + " for oAuthCredentialId : "
					+ oAuthCredentialId);
		} else if (code == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			LOG.warn("Missing authorization code");
			resp.getWriter().print("Missing authorization code for oAuthCredentialId : " + oAuthCredentialId);
		} else {
			this.lock.lock();
			try {
				if (this.flow == null) {
					this.flow = this.initializeFlow();
				}
				final String redirectUri = this.getRedirectUri();
				final TokenResponse response = this.flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
				result = this.flow.createAndStoreCredential(response, oAuthCredentialId.toString());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Credential Stored in default dataStore for oAuthCredentialId : " + oAuthCredentialId);
				}

				this.callTracker.resetNbCall(oAuthCredentialId);

				// onSuccess(req, resp, credential);
			} finally {
				this.lock.unlock();
			}
		}
		return result;
	}

	public void removeCredential(final Long oAuthCredentialId) throws IOException {
		if (this.flow == null) {
			this.flow = this.initializeFlow();
		}
		this.flow.getCredentialDataStore().delete(String.valueOf(oAuthCredentialId));
	}

	public EventDateTime toEventDateTime(final ZonedDateTime dateTime) {
		return this.getEventHelper().toEventDateTime(dateTime, dateTime.getOffset());
	}

	public ZonedDateTime toZonedDateTime(final Date date, final ZoneId userZoneId) {
		ZonedDateTime zonedDateTime = null;
		if (null != date) {
			zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), userZoneId);
		}

		return zonedDateTime;
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws UserAccessRequiredException
	 *             when no credential can be found for the user
	 */
	protected Calendar getCalendarService(final Credential credential) {
		if (null != credential) {
			return new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
					.setApplicationName("0Click Meeting").build();
		}
		throw new UserAccessRequiredException();
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws UserAccessRequiredException
	 *             when no credential can be found for the user
	 */
	@Override
	public ApiCalendar<Calendar, ApiCredential<Credential>> getCalendarService(final Long apiCredentialId) {
		final ApiCredential<Credential> apiCredential = this.getApiCredential(apiCredentialId);

		final Calendar calendarService = this.getCalendarService(apiCredential.getCredential());
		if (null != calendarService) {
			return new ApiCalendar<>(calendarService, apiCredential);
		}
		throw new UserAccessRequiredException();
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 * @throws UserAccessRequiredException
	 *             when no credential can be found for the user
	 */
	public PeopleService getPeopleService(final Long apiCredentialId) throws IOException {
		final Credential credential = this.getCredential(apiCredentialId);
		if (null != credential) {
			return new PeopleService.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
					.setApplicationName("0Click Meeting").build();
		}
		throw new UserAccessRequiredException();
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
	public List<ApiCalendar<Calendar, ApiCredential<Credential>>> getCalendarsServices(final Long userId)
			throws IOException {
		final List<ApiCalendar<Calendar, ApiCredential<Credential>>> calendarsServices = new ArrayList<>();
		final List<ApiCredential<Credential>> apiCredentials = this.getCredentials(userId);
		if (null != apiCredentials && apiCredentials.size() > 0) {
			for (final ApiCredential<Credential> apiCredential : apiCredentials) {
				final Calendar gcalendarService = this.getCalendarService(apiCredential.getCredential());
				calendarsServices
						.add(new ApiCalendar<Calendar, ApiCredential<Credential>>(gcalendarService, apiCredential));
			}
			return calendarsServices;
		}
		throw new UserAccessRequiredException();
	}

	/**
	 * Retrieve calendar's list in Google account.
	 *
	 * @param userId
	 * @return a map, id is the Google calendar Id, value is a **partial** table
	 *         Row (some data not exist in, Google configuration)
	 */
	@Override
	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final Long userId) {
		LOG.info("Retireving all (Google) calendar for user : " + userId);
		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();

		List<ApiCalendar<Calendar, ApiCredential<Credential>>> calendarsServices;
		try {
			calendarsServices = this.getCalendarsServices(userId);
		} catch (final IOException e1) {
			LOG.warn("Cannot get calendars lists for user : " + userId);
			return calendars;
		}

		if (null != calendarsServices && calendarsServices.size() > 0) {
			for (final ApiCalendar<Calendar, ApiCredential<Credential>> calendarApiService : calendarsServices) {
				calendars.putAll(this.buildCalendarConfig(calendarApiService));
			}
		}

		return calendars;
	}

	private Map<String, AbstractCalendarConfigurationTableRowData> buildCalendarConfig(
			final ApiCalendar<Calendar, ApiCredential<Credential>> calendarApiService) {
		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();
		final Long userId = calendarApiService.getMetaData().getUserId();
		final Long apiId = calendarApiService.getMetaData().getApiCredentialId();

		try {
			final CalendarList calendarsList = calendarApiService.getCalendar().calendarList().list().execute();

			final List<CalendarListEntry> calendarItems = calendarsList.getItems();
			for (final CalendarListEntry calendarItem : calendarItems) {
				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final StringBuilder calendarId = new StringBuilder();
				calendarId.append(userId).append('_').append(calendarItem.getId()).append('_').append(apiId);
				calendars.put(calendarId.toString(), this.toCalendarConfig(calendarItem, userId, apiId));
			}

		} catch (

		final IOException ioe) {
			LOG.warn("Cannot get (Google) calendar Service for user : " + userId, ioe);
			throw new VetoException("Cannot get your Google calendar Data");
		}

		return calendars;
	}

	/**
	 * Retrieve calendar's list in Google account.
	 *
	 * @param userId
	 * @return a map, id is the Google calendar Id, value is a **partial** table
	 *         Row (some data not exist in, Google configuration)
	 */
	@Override
	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final ApiTableRowData aUserApi) {
		Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();
		final ApiCalendar<Calendar, ApiCredential<Credential>> calendarApiService = this
				.getCalendarService(aUserApi.getApiCredentialId());

		calendars = this.buildCalendarConfig(calendarApiService);

		return calendars;
	}

	private AbstractCalendarConfigurationTableRowData toCalendarConfig(final CalendarListEntry cal, final Long userId,
			final Long apiCredentialId) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Creating model data for (Google) calendar data : ").append(cal)
					.toString());
		}
		final CalendarConfigurationTableRowData calendarConfigData = new CalendarConfigurationTableRowData();
		final String accessRole = cal.getAccessRole();
		final boolean isWritable = accessRole.equalsIgnoreCase("writer") || accessRole.equalsIgnoreCase("owner");
		calendarConfigData.setExternalId(cal.getId());
		calendarConfigData.setName(cal.getSummary());
		calendarConfigData.setMain(cal.isPrimary());
		calendarConfigData.setReadOnly(!isWritable);
		calendarConfigData.setUserId(userId);
		calendarConfigData.setOAuthCredentialId(apiCredentialId);
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Calendar Config model created with externalId : ")
					.append(calendarConfigData.getExternalId()).append(", UserId : ")
					.append(calendarConfigData.getUserId()).append(", OAuthCredentialId : ")
					.append(calendarConfigData.getOAuthCredentialId()).toString());
		}
		return calendarConfigData;
	}

	@Override
	public String getAccountEmail(final Long apiCredentialId) {
		PeopleService peopleService;
		List<EmailAddress> emailsAdresses = null;
		try {
			peopleService = this.getPeopleService(apiCredentialId);
			final Person profile = peopleService.people().get("people/me").setPersonFields("emailAddresses").execute();
			emailsAdresses = profile.getEmailAddresses();
		} catch (final IOException ioe) {
			LOG.error("Error while retrieving user adress email for api ID : " + apiCredentialId, ioe);
		}

		String mainAddressEmail = null;

		if (null != emailsAdresses && emailsAdresses.size() > 0) {
			for (final EmailAddress emailAdress : emailsAdresses) {
				if (emailAdress.getMetadata().getPrimary()) {
					mainAddressEmail = emailAdress.getValue();
					LOG.info(new StringBuffer().append("Primary email adress found for api ID : ")
							.append(apiCredentialId + " : ").append(mainAddressEmail).toString());
					break;
				}
			}
		}

		return mainAddressEmail;
	}

	public String getUserCreateEventCalendar(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		String calendarId = "primary";

		final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
				.getCalendarToStoreEvents(userId);

		if (null != calendarToStoreEvent) {
			calendarId = calendarToStoreEvent.getExternalId().getValue();
		} else {
			LOG.warn("No calendar configured to store (new) event for user id : " + userId);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Event will be store in : ").append(calendarId).append(" for user : ")
					.append(userId).toString());
		}

		return calendarId;
	}

	public Event getEvent(final EventIdentification eventIdentification, final Long googleApiUserId)
			throws IOException {
		final ApiCalendar<Calendar, ApiCredential<Credential>> apiCalendarService = this
				.getCalendarService(googleApiUserId);

		return this.getEvent(eventIdentification, apiCalendarService);
	}

	protected Event getEvent(final EventIdentification eventIdentification,
			final ApiCalendar<Calendar, ApiCredential<Credential>> apiCalendarService) {
		final Event event = this.getEventHelper().getEvent(eventIdentification, apiCalendarService.getCalendar());

		return event;
	}

	@Override
	public String createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final String subject,
			final Long forUserId, final String location, final String withEmail, final Boolean guestAutoAcceptMeeting,
			final String envDisplay, final CalendarConfigurationFormData calendarToStoreEvent,
			final String description) {
		final Event newEvent = new Event();
		newEvent.setStart(this.toEventDateTime(startDate));
		newEvent.setEnd(this.toEventDateTime(endDate));
		newEvent.setSummary(envDisplay + " " + subject + TextsHelper.get(forUserId, "zc.common.email.subject.suffix"));
		newEvent.setLocation(TextsHelper.get(forUserId, location));
		newEvent.setDescription(subject);
		newEvent.setDescription(description);

		final EventAttendee attendeeEmail = new EventAttendee().setEmail(withEmail);
		if (guestAutoAcceptMeeting) {
			attendeeEmail.setResponseStatus("accepted");
		}

		final EventAttendee[] attendees = new EventAttendee[] { attendeeEmail };
		newEvent.setAttendees(Arrays.asList(attendees));

		final Event event = this.getEventHelper().create(newEvent, calendarToStoreEvent);

		return null == event ? null : event.getId();
	}

	@Override
	protected Boolean delete(final String calendarId, final String eventId, final Calendar apiCalendarService) {
		Boolean eventDeleted = Boolean.FALSE;

		LOG.info(new StringBuilder().append("Deleting a Google event : ").append(eventId).append(" in calendar : ")
				.append(eventId).toString());

		try {
			apiCalendarService.events().delete(calendarId, eventId).execute();
			eventDeleted = Boolean.TRUE;

		} catch (final GoogleJsonResponseException gjre) {
			if (gjre.getStatusCode() == 410) { // 410 = Resource has been
												// deleted
				LOG.info(new StringBuilder().append("Trying to delete an already deleted Event : ").append(eventId)
						.append(" in calendar : ").append(eventId).toString());
				eventDeleted = Boolean.TRUE;
			} else {
				LOG.error("Error while deleting event", gjre);
				eventDeleted = Boolean.FALSE;
			}
		} catch (final IOException ioe) {
			LOG.error("Error while deleting event", ioe);
			eventDeleted = Boolean.FALSE;
		}
		return eventDeleted;
	}

	@Override
	public String getEventHtmlLink(final EventIdentification eventIdentification, final Long apiCredentialId) {
		final ApiCalendar<Calendar, ApiCredential<Credential>> calendarService = this
				.getCalendarService(apiCredentialId);

		final Event event = this.getEventHelper().getEvent(eventIdentification, calendarService.getCalendar());

		return this.getEventHelper().getHmlLink(event);
	}

	@Override
	public Boolean acceptEvent(final EventIdentification eventOrganizerIdentification, final String attendeeEmail,
			final ApiTableRowData eventCreatorApi) {
		Boolean eventUpdated = Boolean.FALSE;
		final ApiCalendar<Calendar, ApiCredential<Credential>> eventCalendarService = this
				.getCalendarService(eventCreatorApi.getApiCredentialId());

		final Event organizerEvent = this.getEvent(eventOrganizerIdentification, eventCalendarService);

		if (null == organizerEvent) {
			return eventUpdated;// early Break
		}

		final List<EventAttendee> attendees = organizerEvent.getAttendees();
		for (final EventAttendee eventAttendee : attendees) {
			if (attendeeEmail.equals(eventAttendee.getEmail())) {
				if (LOG.isDebugEnabled()) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final StringBuilder builder = new StringBuilder();
					LOG.debug(builder.append("Updating (Google) event : ").append(organizerEvent.getSummary())
							.append(" as accepted for attendee : ").append(eventAttendee.getEmail()).toString());
				}
				eventAttendee.setResponseStatus("accepted");
			}
		}

		// Update the event
		if (null != eventCalendarService) {
			eventUpdated = this.getEventHelper().update(eventOrganizerIdentification, eventCalendarService,
					organizerEvent);
			eventUpdated = Boolean.TRUE;
		}

		if (!eventUpdated) {
			LOG.error("Error while update Event, cannot acces external event");
		}

		return eventUpdated;
	}

	/**
	 * Callback used by Google API flow to handle user authentification response
	 */
	public static class GoogleCallbackUrlProperty extends AbstractUrlConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "/oauth2callback";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.callback.url";
		}

		@Override
		protected String getDefautlBaseUrl() {
			return CONFIG.getPropertyValue(ApplicationUrlProperty.class);
		}
	}

	/**
	 * File containing client id and secret.
	 */
	public static class GoogleClientCredentialFileProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "/client_secret.json";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.client.auth.file";
		}
	}

	/**
	 * Client id. This is recommended to use a credential File.
	 */
	public static class GoogleClientIdProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "none";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.client.id";
		}
	}

	/**
	 * Client secret. This is recommended to use a credential File.
	 */
	public static class GoogleClientSecretProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "noneSecret";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.client.secret";
		}
	}

	/**
	 * User directory to store user accepted api call.
	 */
	public static class GoogleUserStorageDirectoryProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "/.secret/google";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.user.storage.dir";
		}
	}
}
