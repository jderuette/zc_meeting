package org.zeroclick.meeting.server;

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
import org.eclipse.scout.rt.server.commons.authentication.DevelopmentAccessController;
import org.eclipse.scout.rt.server.commons.authentication.ServiceTunnelAccessTokenAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController.TrivialAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>{@link ServerServletFilter}</h3> This is the main server side servlet
 * filter.
 *
 * @author djer
 */
public class ServerServletFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(ServerServletFilter.class);

	private TrivialAccessController m_trivialAccessController;
	private ServiceTunnelAccessTokenAccessController m_tunnelAccessController;
	private DevelopmentAccessController m_developmentAccessController;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.m_trivialAccessController = BEANS.get(TrivialAccessController.class)
				.init(new TrivialAuthConfig().withExclusionFilter(filterConfig.getInitParameter("filter-exclude")));
		this.m_tunnelAccessController = BEANS.get(ServiceTunnelAccessTokenAccessController.class).init();
		this.m_developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;

		LOG.info("Filtering request : " + req.getContentType() + ", " + req.getRequestURL() + " with principal : "
				+ req.getUserPrincipal() + " with response type : " + resp.getContentType());

		if (this.m_trivialAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by m_trivialAccessController");
			return;
		}

		if (this.m_tunnelAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by m_tunnelAccessController");
			return;
		}

		if (this.m_developmentAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by m_developmentAccessController");
			return;
		}

		LOG.warn("No Access Controller handle the request : " + req.getRequestURI());

		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	public void destroy() {
		this.m_developmentAccessController.destroy();
		this.m_tunnelAccessController.destroy();
		this.m_trivialAccessController.destroy();
	}
}
