package org.zeroclick.meeting.client.calendar;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;

/**
 * Mapped to /addGoogleCalendar
 *
 * @author djer
 *
 */
public class CalendarServletSample extends HttpServlet {

	// // AbstractUiServletRequestHandler ???

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(CalendarServletSample.class);

	private final GoogleApiHelper googleApiHelper = BEANS.get(GoogleApiHelper.class);

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		LOG.info("Entering do get");

		this.googleApiHelper.askUserCredential(response);
	}

}
