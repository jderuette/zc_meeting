package org.oneclick.meeting.shared.security;

import java.security.AccessController;
import java.security.PermissionCollection;
import java.util.Set;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.oneclick.configuration.shared.user.IUserService;

/**
 * <h3>{@link UserIdAccessControlService}</h3>
 *
 * {@link IAccessControlService} service that uses {@link ISession#getUserId()}
 * as internal cache key required by {@link AbstractAccessControlService}
 * implementation.
 * <p>
 * Replace this service at server side to load permission collection. It is
 * <b>not</b> required to implement {@link #execLoadPermissions(String)} at
 * client side.
 *
 * @author djer
 */
public class AccessControlService extends AbstractAccessControlService<String> {

	public AccessControlService() {
		// avoid removing "@" in userId to allow email address as Id

		super.setUserIdSearchPatterns(new Pattern[] { Pattern.compile(".*\\\\([^/@]+)"),
				Pattern.compile(".*\\\\([^/@]+)[/@].*"), Pattern.compile("([^/@]+)"),
				// original capture only BEFORE the @. '[^/@]+)[/@].*' ==>
				// '([^/@]+[/@].*)'
				Pattern.compile("([^/@]+[/@].*)"), });

	}

	@Override
	protected String getCurrentUserCacheKey() {
		return this.getUserIdOfCurrentUser();
	}

	@Override
	protected PermissionCollection execLoadPermissions(final String userId) {
		return null;
	}

	protected Long getUserIdAsLong(final String userId) {
		final IUserService userService = BEANS.get(IUserService.class);

		return userService.getUserId(userId);
	}

	public Long getZeroClickUserIdOfCurrentSubject() {
		final String userId = this.getUserId(Subject.getSubject(AccessController.getContext()));

		return this.getUserIdAsLong(userId);
	}

	public Set<String> getUserNotificationIds(final Long userId) {
		final IUserService userServcie = BEANS.get(IUserService.class);
		return userServcie.getUserNotificationIds(userId);
	}

}
