package org.zeroclick.meeting.shared.security;

import java.security.PermissionCollection;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;

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
 * Djer's addition :
 * <ul>
 * <li>method to map a text userId (login/email) to the Database user Id
 * (long)</li>
 * <li>Allow email as username</li>
 * <li>add a cache from username (login/email) => user (database) ID</li>
 * </ul>
 *
 * @author djer && EclipseScout
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
	protected PermissionCollection execLoadPermissions(final String userId) {
		return null;
	}

	@Override
	protected String getCurrentUserCacheKey() {
		return this.getUserIdOfCurrentUser();
	}

	/**
	 * Clear the cache with the passed userIds (login and Email)
	 *
	 * @param userId
	 *            login or email to user cache
	 */
	public void clearUserCache(final Set<String> userIds) {
		this.clearCache(userIds);

		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		acsHelper.clearUserIdsCache(userIds);
	}

}
