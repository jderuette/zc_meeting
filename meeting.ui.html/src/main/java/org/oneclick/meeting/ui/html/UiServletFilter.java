package org.oneclick.meeting.ui.html;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier;
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController;
import org.eclipse.scout.rt.server.commons.authentication.FormBasedAccessController.FormBasedAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;
import org.oneclick.common.security.ScoutServiceCredentialVerifier;

/**
 * <h3>{@link UiServletFilter}</h3> This is the main servlet filter used for the
 * HTML UI.
 *
 * @author djer
 */
public class UiServletFilter implements Filter {

	private TrivialAccessController m_trivialAccessController;
	private FormBasedAccessController m_formBasedAccessController;
	private DevelopmentAccessController m_developmentAccessController;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.m_trivialAccessController = BEANS.get(TrivialAccessController.class).init(new TrivialAuthConfig()
				.withExclusionFilter(filterConfig.getInitParameter("filter-exclude")).withLoginPageInstalled(true));
		this.m_formBasedAccessController = BEANS.get(FormBasedAccessController.class)
				.init(new FormBasedAuthConfig().withCredentialVerifier(BEANS.get(ConfigFileCredentialVerifier.class)));
		this.m_formBasedAccessController = BEANS.get(FormBasedAccessController.class)
				.init(new FormBasedAuthConfig()
						// .withCredentialVerifier(BEANS.get(ConfigFileCredentialVerifier.class)));
						.withCredentialVerifier(BEANS.get(ScoutServiceCredentialVerifier.class)));

		// this.m_formBasedAccessController =
		// BEANS.get(FormBasedAccessController.class).init(
		// new
		// FormBasedAuthConfig().withCredentialVerifier(BEANS.get(ScoutServiceCredentialVerifier.class)));

		this.m_developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;

		if (this.m_trivialAccessController.handle(req, resp, chain)) {
			return;
		}

		if (this.m_formBasedAccessController.handle(req, resp, chain)) {
			return;
		}

		if (this.m_developmentAccessController.handle(req, resp, chain)) {
			return;
		}

		BEANS.get(ServletFilterHelper.class).forwardToLoginForm(req, resp);
	}

	@Override
	public void destroy() {
		this.m_developmentAccessController.destroy();
		this.m_formBasedAccessController.destroy();
		this.m_trivialAccessController.destroy();
	}
}
