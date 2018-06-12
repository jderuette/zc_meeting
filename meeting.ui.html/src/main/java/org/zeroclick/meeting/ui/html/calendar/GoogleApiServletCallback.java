package org.zeroclick.meeting.ui.html.calendar;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.api.ApiCalendar;
import org.zeroclick.meeting.client.api.ApiCredential;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper.GoogleCallbackUrlProperty;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.ui.html.AbstractApiServletRequestHandler;
import org.zeroclick.meeting.ui.html.HtmlResponseHelper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 *
 * @author djer
 *
 */
public class GoogleApiServletCallback extends AbstractApiServletRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GoogleApiServletCallback.class);

	private final GoogleApiHelper googleApiHelper = BEANS.get(GoogleApiHelper.class);

	private final String registeredPath;

	public GoogleApiServletCallback() {
		this.registeredPath = BEANS.get(GoogleCallbackUrlProperty.class).getRelativeUri();
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
	protected boolean processGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final HtmlResponseHelper responseHelper = new HtmlResponseHelper();

		final Long currentUsderId = this.googleApiHelper.getCurrentUserId();
		LOG.info("Google API Storing User token for user Id : " + currentUsderId);

		// Store in DB
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiFormData input = new ApiFormData();
		input.setUserId(Long.valueOf(currentUsderId));
		input.getProvider().setValue(ProviderCodeType.GoogleCode.ID);

		// Not propagate creation (yet) because no accesToken, ... yet
		final ApiFormData createdData = apiService.create(input, Boolean.FALSE);

		final Credential credential = this.googleApiHelper.tryStoreCredential(req, resp,
				createdData.getApiCredentialId());

		final String mainAddressEmail = this.googleApiHelper.getAccountEmail(createdData.getApiCredentialId());

		// add the meailAdress to the API
		this.storeEmailAccount(createdData.getApiCredentialId(), mainAddressEmail);

		// final String aditionnalParams = "";
		if (null == credential) {
			LOG.warn("Google API Auth error for : " + currentUsderId);
			responseHelper.addErrorMessage("Error while trying to Store your credential");

			// aditionnalParams = "&Error=true";
			// resp.sendRedirect("addGoogleCalendar" + aditionnalParams);
		} else {
			responseHelper.addSuccessMessage(TEXTS.get("zc.api.added.google"));
		}

		resp.getWriter().write(responseHelper.getPageContent());

		List<ApiCalendar<Calendar, ApiCredential<Credential>>> services;
		try {
			services = this.googleApiHelper.getCalendarsServices(this.googleApiHelper.getCurrentUserId());
		} catch (final UserAccessRequiredException uare) {
			return false; // early break to let user handle google auth flow
		}

		if (null != services && services.size() > 0) {
			for (final ApiCalendar<Calendar, ApiCredential<Credential>> service : services) {
				this.displayCalendarList(service.getCalendar());
				// List the next 10 events from the primary calendar.
				this.displayNextEvent(service.getCalendar(), "primary", 5);
			}
		}
		return true;
	}

	@Override
	protected boolean processPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		return this.processGet(req, resp);
	}

	private ApiFormData storeEmailAccount(final Long apiCredentialId, final String accountEmail) throws IOException {
		final IApiService apiService = BEANS.get(IApiService.class);

		final ApiFormData apiDataAfterCredentialStored = apiService.load(apiCredentialId);
		apiDataAfterCredentialStored.getAccountEmail().setValue(accountEmail);
		final ApiFormData storedData = apiService.storeAccountEmail(apiDataAfterCredentialStored, accountEmail);

		if (!apiCredentialId.equals(apiDataAfterCredentialStored.getApiCredentialId())) {
			// An update Occur, the "newly" stored credential must be discarded
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
			googleHelper.removeCredential(apiCredentialId);
		}

		return storedData;
	}

	private void displayCalendarList(final Calendar service) throws IOException {
		// List of calendars
		final CalendarList calendars = service.calendarList().list().execute();

		final List<CalendarListEntry> calendarsItems = calendars.getItems();

		if (calendarsItems.size() == 0) {
			LOG.info("No calendars found.");
		} else {
			LOG.info(calendarsItems.size() + " Calendriers : ");
			if (LOG.isDebugEnabled()) {
				final StringBuilder builder = new StringBuilder(50);
				for (final CalendarListEntry calendar : calendarsItems) {
					builder.append(calendar.getSummary()).append(" : ").append(calendar.getId()).append('(')
							.append(calendar.isPrimary()).append(')');
					LOG.debug(builder.toString());
				}
			}
		}
	}

	private void displayNextEvent(final Calendar service, final String calendarId, final Integer nbEvent)
			throws IOException {
		final DateTime now = new DateTime(System.currentTimeMillis());
		final Events events = service.events().list(calendarId).setMaxResults(nbEvent).setTimeMin(now)
				.setOrderBy("startTime").setSingleEvents(true).execute();
		final List<Event> items = events.getItems();
		if (items.size() == 0) {
			LOG.info("No upcoming events found.");
		} else {
			LOG.info(items.size() + " Upcoming events");
			if (LOG.isDebugEnabled()) {
				for (final Event event : items) {
					DateTime start = event.getStart().getDateTime();
					if (start == null) {
						start = event.getStart().getDate();
					}
					LOG.debug(event.getSummary() + " at : " + start);
				}
			}
		}
	}
}
