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
package org.zeroclick.meeting.ui.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.HttpSessionHelper;
import org.eclipse.scout.rt.ui.html.ISessionStore;
import org.slf4j.Logger;

/**
 * Helper request handler for APIs connection
 *
 * @author djer
 */
public abstract class AbstractApiServletRequestHandler extends AbstractUiServletRequestHandler {
	protected final HttpSessionHelper m_httpSessionHelper = BEANS.get(HttpSessionHelper.class);

	protected abstract String getRegistredServletPath();

	protected abstract Logger getLogger();

	@Override
	public final boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		Boolean processed = false;

		if (this.canProcess(req)) {
			this.getLogger().info("Entering do POST");
			processed = this.processPost(req, resp);
		}

		return processed;
	}

	protected boolean processPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		return false;
	}

	@Override
	public final boolean handleGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		Boolean processed = false;

		if (this.canProcess(req)) {
			this.getLogger().info("Entering do GET");
			processed = this.processGet(req, resp);
		}

		return processed;
	}

	protected boolean processGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		return false;
	}

	private Boolean canProcess(final HttpServletRequest req) {
		boolean processAllowed = false;
		if (req.getPathInfo().equals(this.getRegistredServletPath())) {
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug(new StringBuilder().append(this.getClass().getName())
						.append(" will handle the request ").append(req.getPathInfo()).toString());
			}
			processAllowed = true;

		} else {
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger()
						.debug(new StringBuilder().append(this.getClass().getName())
								.append(" ignore the request because requested : [").append(req.getPathInfo())
								.append("] path does not match configured : [").append(this.getRegistredServletPath())
								.append(']').toString());
			}
			processAllowed = false;
		}

		return processAllowed;
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

	protected String getBody(final HttpServletRequest request) throws IOException {

		String body = null;
		final StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			final InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				final char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (final IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (final IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	protected Object getSessionParam(final HttpSession session, final String paramName) {
		Object paramValue = null;
		if (null != session && null != session.getAttribute(paramName)) {
			paramValue = session.getAttribute(paramName);
		}

		return paramValue;
	}

}
