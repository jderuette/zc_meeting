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
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;
import org.zeroclick.meeting.ui.html.AbstractApiServletRequestHandler;
import org.zeroclick.meeting.ui.html.HtmlResponseHelper;

/**
 * @author djer
 *
 */
public class MicrosoftCalendarServlet extends AbstractApiServletRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftCalendarServlet.class);

	private final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);

	private final String registeredPath;

	public MicrosoftCalendarServlet() {
		this.registeredPath = MicrosoftApiHelper.ADD_MICROSOFT_CALENDAR_URL;
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
		final UUID state = UUID.randomUUID();
		final UUID nonce = UUID.randomUUID();

		final HttpSession session = req.getSession();
		if (null == session) {
			final String erroMessage = "No Active Session, canno't create new (microsfot) API";
			LOG.error(erroMessage);
			responseHelper.addErrorMessage(erroMessage);
			resp.getWriter().write(responseHelper.getPageContent());
		} else {
			session.setAttribute("state", state);
			session.setAttribute("nonce", nonce);

			final String loginOAUthUrl = this.microsoftApiHelper.getLoginUrl(state, nonce);
			resp.sendRedirect(loginOAUthUrl);
		}

		return true;
	}

	@Override
	protected boolean processPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		return this.processGet(req, resp);
	}
}
