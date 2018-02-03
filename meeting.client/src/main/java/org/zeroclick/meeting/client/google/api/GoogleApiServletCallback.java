package org.zeroclick.meeting.client.google.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper.ApiCalendar;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * Mapped to /oauth2callback
 *
 * @author djer
 *
 */
public class GoogleApiServletCallback extends HttpServlet {

	// AbstractUiServletRequestHandler ???

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(GoogleApiServletCallback.class);

	private final GoogleApiHelper googleApiHelper = BEANS.get(GoogleApiHelper.class);

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final Long currentUsderId = this.googleApiHelper.getCurrentUserId();
		LOG.info("Google API Storing User token for user Id : " + currentUsderId);

		// Store in DB
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiFormData input = new ApiFormData();
		input.setUserId(Long.valueOf(currentUsderId));
		input.getProvider().setValue(ProviderCodeType.GoogleCode.ID);

		final ApiFormData createdData = apiService.create(input);

		final Credential credential = this.googleApiHelper.tryStoreCredential(req, resp,
				createdData.getApiCredentialId());

		resp.getWriter().write("<html><head></head><body><b>");

		// final String aditionnalParams = "";
		if (null == credential) {
			LOG.warn("Google API Auth error for : " + currentUsderId);
			resp.getWriter().write("Error while trying to Store your credential");

			// aditionnalParams = "&Error=true";
			// resp.sendRedirect("addGoogleCalendar" + aditionnalParams);
		} else {
			resp.getWriter().write(TEXTS.get("zc.api.added.google"));
		}

		resp.getWriter().write("<b></body></html>");

		List<ApiCalendar> services;
		try {
			services = this.googleApiHelper.getCalendarsServices(this.googleApiHelper.getCurrentUserId());
		} catch (final UserAccessRequiredException uare) {
			return; // early break to let user handle google auth flow
		}

		if (null != services && services.size() > 0) {
			for (final ApiCalendar service : services) {
				this.displayCalendarList(service.getCalendar());
				// List the next 10 events from the primary calendar.
				this.displayNextEvent(service.getCalendar(), "primary", 10);
			}
		}
	}

	private void displayCalendarList(final Calendar service) throws IOException {
		// List of calendars
		final CalendarList calendars = service.calendarList().list().execute();

		final List<CalendarListEntry> calendarsItems = calendars.getItems();

		if (calendarsItems.size() == 0) {
			System.out.println("No calendars found.");
		} else {
			System.out.println("Calendriers : ");
			for (final CalendarListEntry calendar : calendarsItems) {
				System.out.println(calendar.getSummary() + " : " + calendar.getId() + "(" + calendar.isPrimary() + ")");
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
			System.out.println("No upcoming events found.");
		} else {
			System.out.println("Upcoming events");
			for (final Event event : items) {
				DateTime start = event.getStart().getDateTime();
				if (start == null) {
					start = event.getStart().getDate();
				}
				System.out.printf("%s (%s)\n", event.getSummary(), start);
			}
		}
	}
}
