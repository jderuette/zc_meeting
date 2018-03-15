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
import java.security.Principal;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.MaxUserIdleTimeProperty;
import org.eclipse.scout.rt.ui.html.json.JsonRequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;

/**
 * @author djer
 *
 */
public class MicrosoftCalendarServlet extends AbstractUiServletRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MicrosoftCalendarServlet.class);

	private final int m_maxUserIdleTime = CONFIG.getPropertyValue(MaxUserIdleTimeProperty.class).intValue();
	private final HttpSessionHelper m_httpSessionHelper = BEANS.get(HttpSessionHelper.class);
	private final JsonRequestHelper m_jsonRequestHelper = BEANS.get(JsonRequestHelper.class);

	private final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);

	private static final String SERVLET_PATH = "/api/microsot/addCalendar";

	@Override
	public boolean handleGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {

		if (!req.getPathInfo().equals(SERVLET_PATH)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(
						new StringBuilder().append("MicrosoftCalendarServlet ignore the request because requested : [")
								.append(req.getPathInfo()).append("] path does not match configured : [")
								.append(SERVLET_PATH).append(']').toString());
			}
			return false;
		}

		LOG.info("Entering do get");

		final UUID state = UUID.randomUUID();
		final UUID nonce = UUID.randomUUID();

		final HttpSession session = req.getSession();
		if (null == session) {
			LOG.error("No Active Session, canno't create new (microsfot) API");
		} else {
			session.setAttribute("state", state);
			session.setAttribute("nonce", nonce);

			final String loginOAUthUrl = this.microsoftApiHelper.getLoginUrl(state, nonce);
			resp.sendRedirect(loginOAUthUrl);
		}

		return true;
	}

	protected IClientSession loadSession(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		final HttpSession httpSession = req.getSession();
		final ISessionStore sessionStore = this.m_httpSessionHelper.getSessionStore(httpSession);

		final String currentUser = req.getRemoteUser();

		IClientSession currentUserSession = null;

		for (final IClientSession session : sessionStore.getClientSessionMap().values()) {
			for (final Principal pricnipal : session.getSubject().getPrincipals()) {
				if (pricnipal.getName().equals(currentUser)) {
					currentUserSession = session;
				}
			}
		}

		return currentUserSession;
	}
}
