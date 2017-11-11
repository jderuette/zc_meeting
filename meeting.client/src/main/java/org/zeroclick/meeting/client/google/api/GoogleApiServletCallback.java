package org.zeroclick.meeting.client.google.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;

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
		LOG.info("Google API Storing User token");
		final Credential credential = this.googleApiHelper.tryStoreCredential(req, resp,
				this.googleApiHelper.getCurrentUserId());
		String aditionnalParams = "";
		if (null == credential) {
			LOG.warn("Google API Auth error");
			aditionnalParams = "&Error=true";
		}
		resp.sendRedirect("addGoogleCalendar" + aditionnalParams);

	}
}
