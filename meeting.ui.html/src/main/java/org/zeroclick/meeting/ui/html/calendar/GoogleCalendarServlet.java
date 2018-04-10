package org.zeroclick.meeting.ui.html.calendar;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.ui.html.AbstractApiServletRequestHandler;

/**
 * Mapped to /addGoogleCalendar
 *
 * @author djer
 *
 */
public class GoogleCalendarServlet extends AbstractApiServletRequestHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarServlet.class);

	private final GoogleApiHelper googleApiHelper = BEANS.get(GoogleApiHelper.class);
	private final String registeredPath;

	public GoogleCalendarServlet() {
		this.registeredPath = GoogleApiHelper.ADD_GOOGLE_CALENDAR_URL;
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
	protected boolean processGet(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		this.googleApiHelper.askUserCredential(response);
		return true;
	}

}
