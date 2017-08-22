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

	private TrivialAccessController trivialAccessController;
	private ServiceTunnelAccessTokenAccessController tunnelAccessController;
	private DevelopmentAccessController developmentAccessController;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.trivialAccessController = BEANS.get(TrivialAccessController.class)
				.init(new TrivialAuthConfig().withExclusionFilter(filterConfig.getInitParameter("filter-exclude")));
		this.tunnelAccessController = BEANS.get(ServiceTunnelAccessTokenAccessController.class).init();
		this.developmentAccessController = BEANS.get(DevelopmentAccessController.class).init();
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse resp = (HttpServletResponse) response;

		LOG.info("Filtering request : " + req.getContentType() + ", " + req.getRequestURL() + " with principal : "
				+ req.getUserPrincipal() + " with response type : " + resp.getContentType());

		if (this.trivialAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by trivialAccessController");
			return;
		}

		if (this.tunnelAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by tunnelAccessController");
			return;
		}

		if (this.developmentAccessController.handle(req, resp, chain)) {
			LOG.info("request : " + req + ", handled by developmentAccessController");
			return;
		}

		LOG.warn("No Access Controller handle the request : " + req.getRequestURL());

		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	public void destroy() {
		this.developmentAccessController.destroy();
		this.tunnelAccessController.destroy();
		this.trivialAccessController.destroy();
	}
}
