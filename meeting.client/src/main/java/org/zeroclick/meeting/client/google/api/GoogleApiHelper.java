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
package org.zeroclick.meeting.client.google.api;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.common.CallTrackerService;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.shared.security.AccessControlService;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * @author djer
 *
 */
public class GoogleApiHelper {

	private static final Logger LOG = LoggerFactory.getLogger(GoogleApiHelper.class);

	private static GoogleApiHelper instance;
	private final CallTrackerService<Long> callTracker;
	private final DataStoreFactory dataStoreFactory;

	private GoogleApiHelper() {
		this.callTracker = new CallTrackerService<>(5, Duration.ofMinutes(1), "GoogleApiHelper create API flow");
		this.dataStoreFactory = new ScoutDataStoreFactory();
	}

	public static GoogleApiHelper get() {
		if (null == instance) {
			instance = new GoogleApiHelper();
		}
		return instance;
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
		final Collection<String> scopes = Collections.singleton(CalendarScopes.CALENDAR);

		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
				this.getClientSecret(), scopes).setDataStoreFactory(this.dataStoreFactory).setAccessType("offline")
						.setApprovalPrompt("force").build();
	}

	private GoogleAuthorizationCodeFlow initializeGoogleFlow() throws IOException {
		return (GoogleAuthorizationCodeFlow) this.initializeFlow();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.zeroclick.meeting.client.calendar.IGoogleApiHelper#getCurrentUserId()
	 */
	public Long getCurrentUserId() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		return acs.getZeroClickUserIdOfCurrentSubject();
	}

	private String getRedirectUri() throws ServletException, IOException {
		final String frontUrl = CONFIG.getPropertyValue(ApplicationUrlProperty.class);
		final GenericUrl url = new GenericUrl(frontUrl);
		final String callbackUrl = CONFIG.getPropertyValue(GoogleCallbackUrlProperty.class);
		url.setRawPath(callbackUrl);
		final String urlString = url.build();
		LOG.debug("Google APi redirect URI : " + urlString);
		return urlString;
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
		if (LOG.isDebugEnabled()) {
			LOG.info("Asking user credential throught Google OAuth2 Authoriszation Flow");
		}

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

	public Credential getCredential(final Long userId) throws IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching for Credential for user : " + userId);
		}

		Credential exitingCredential = null;
		if (null == this.flow) {
			this.flow = this.initializeFlow();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading Credential from Google Flow for user : " + userId);
		}
		exitingCredential = this.flow.loadCredential(userId.toString());
		if (null == exitingCredential) {
			LOG.warn("No save credential (Google) for user : " + userId);
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
			final Long userId) throws ServletException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Trying to store credential for user : " + userId);
		}
		this.callTracker.validateCanCall(userId);
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
			LOG.warn("Google response contain error : " + responseUrl.getError() + " for userId : " + userId);
		} else if (code == null) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			LOG.warn("Missing authorization code");
			resp.getWriter().print("Missing authorization code for userId : " + userId);
		} else {
			this.lock.lock();
			try {
				if (this.flow == null) {
					this.flow = this.initializeFlow();
				}
				final String redirectUri = this.getRedirectUri();
				final TokenResponse response = this.flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
				result = this.flow.createAndStoreCredential(response, userId.toString());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Credential Stored in default dataStore for : " + userId);
				}

				this.callTracker.resetNbCall(userId);

				// onSuccess(req, resp, credential);
			} finally {
				this.lock.unlock();
			}
		}
		return result;
	}

	public void removeCredential(final Long userId) throws IOException {
		if (this.flow == null) {
			this.flow = this.initializeFlow();
		}
		this.flow.getCredentialDataStore().delete(String.valueOf(userId));
	}

	/**
	 * Convert a Google (Event) Date to a standard Java 8 LocalDateTime (JSR
	 * 310)
	 *
	 * @param date
	 * @return
	 */
	public ZonedDateTime fromEventDateTime(final EventDateTime date) {
		if (null == date.getDateTime()) {
			return this.fromDateTime(date.getDate(), this.timeOffset(date));
		} else {
			return this.fromDateTime(date.getDateTime(), this.timeOffset(date));
		}
	}

	private ZonedDateTime fromDateTime(final DateTime dateTime, final ZoneOffset zoneOffset) {
		final ZonedDateTime localDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTime.getValue()), zoneOffset);

		return localDate;
	}

	public EventDateTime toEventDateTime(final LocalDateTime dateTime, final ZoneId zoneId) {
		final ZoneOffset zoneOffset = ZoneOffset.from(dateTime.atZone(zoneId));

		return this.toEventDateTime(dateTime, zoneOffset);
	}

	public EventDateTime toEventDateTime(final ZonedDateTime dateTime, final ZoneOffset zoneOffset) {
		final DateTime date = this.toDateTime(dateTime, zoneOffset);
		return new EventDateTime().setDateTime(date);
	}

	public EventDateTime toEventDateTime(final ZonedDateTime dateTime) {
		return this.toEventDateTime(dateTime, dateTime.getOffset());
	}

	public DateTime toDateTime(final ZonedDateTime dateTime) {

		return this.toDateTime(dateTime, dateTime.getZone());
	}

	public ZonedDateTime toZonedDateTime(final Date date, final ZoneId userZoneId) {
		ZonedDateTime zonedDateTime = null;
		if (null != date) {
			zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), userZoneId);
		}

		return zonedDateTime;
	}

	private DateTime toDateTime(final ZonedDateTime dateTime, final ZoneId zoneId) {
		return new DateTime(Date.from(dateTime.toInstant()), TimeZone.getTimeZone(zoneId));
	}

	/**
	 * Convert a offset from a google DateTime to a standard Java 8 ZoneOffset
	 * (JSR 310)
	 *
	 * @param dateTime
	 * @return
	 */
	public ZoneOffset timeOffset(final EventDateTime eventDateTime) {
		Integer offsetHours = 0;
		Integer offsetMinutes = 0;
		if (null == eventDateTime.getDateTime()) {
			offsetHours = eventDateTime.getDate().getTimeZoneShift() / 60;
			offsetMinutes = eventDateTime.getDate().getTimeZoneShift() % 60;
		} else {
			offsetHours = eventDateTime.getDateTime().getTimeZoneShift() / 60;
			offsetMinutes = eventDateTime.getDateTime().getTimeZoneShift() % 60;
		}

		return ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);
	}

	/**
	 * Build and return an authorized Calendar client service.
	 *
	 * @return an authorized Calendar client service
	 * @throws IOException
	 * @throws UserAccessRequiredException
	 *             when no credential can be found for the user
	 */
	public Calendar getCalendarService(final Long userId) throws IOException {
		final Credential credential = this.getCredential(userId);
		if (null != credential) {
			return new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
					.setApplicationName("0Click Meeting").build();
		}
		throw new UserAccessRequiredException();
	}

	@SuppressWarnings("PMD.EmptyCatchBlock")
	public Boolean isCalendarConfigured(final Long userId) {
		Boolean isConfigured = Boolean.FALSE;
		try {
			this.getCalendarService(userId);
			isConfigured = Boolean.TRUE;
		} catch (final UserAccessRequiredException uare) {
			// Do nothing default is FALSE
		} catch (final IOException ios) {
			// Do nothing default is FALSE
		}
		return isConfigured;

	}

	/**
	 * Check if current connected User as Calendar configured
	 *
	 * @return
	 */
	public Boolean isCalendarConfigured() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		return this.isCalendarConfigured(acs.getZeroClickUserIdOfCurrentSubject());
	}

	public String aslog(final Event event) {
		if (null != event) {
			final StringBuilder builder = new StringBuilder(100);
			builder.append("Google Event : ").append(event.getId()).append(", start : ").append(event.getStart())
					.append(", end : ").append(event.getEnd()).append(", status : ").append(event.getStatus())
					.append(", summary : ").append(event.getSummary());

			return builder.toString();
		}
		return "";

	}

	/**
	 * Callback used by Google API flow to handle user authentification response
	 */
	public static class GoogleCallbackUrlProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "/oauth2callback";
		}

		@Override
		public String getKey() {
			return "contacts.api.google.callback.url";
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
