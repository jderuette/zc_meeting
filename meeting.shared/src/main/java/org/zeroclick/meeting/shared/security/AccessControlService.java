package org.zeroclick.meeting.shared.security;

import java.security.AccessController;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.cache.AbstractCacheValueResolver;
import org.eclipse.scout.rt.shared.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.zeroclick.configuration.shared.user.IUserService;

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
 * <li>method to map a text userId (login/email) to the Dabatase user Id
 * (long)</li>
 * <li>Allow email as username</li>
 * <li>add a cache from username (login/email) => user (databse) ID</li>
 * </ul>
 *
 * @author djer && EclipseScout
 */
public class AccessControlService extends AbstractAccessControlService<String> {

	public static final String USER_IDS_SERVICE_CACHE_ID = "ZeroClickUserIds";

	private final ICache<String, Long> m_cacheUserIds;

	public AccessControlService() {
		// avoid removing "@" in userId to allow email address as Id
		super.setUserIdSearchPatterns(new Pattern[] { Pattern.compile(".*\\\\([^/@]+)"),
				Pattern.compile(".*\\\\([^/@]+)[/@].*"), Pattern.compile("([^/@]+)"),
				// original capture only BEFORE the @. '[^/@]+)[/@].*' ==>
				// '([^/@]+[/@].*)'
				Pattern.compile("([^/@]+[/@].*)"), });
		this.m_cacheUserIds = this.createCacheUserIdsBuilder().build();

	}

	/**
	 * Can be overridden to customize the cache builder
	 *
	 * @return {@link ICacheBuilder} for the internal cache
	 */
	protected ICacheBuilder<String, Long> createCacheUserIdsBuilder() {
		@SuppressWarnings("unchecked")
		final ICacheBuilder<String, Long> cacheBuilder = BEANS.get(ICacheBuilder.class);
		return cacheBuilder.withCacheId(USER_IDS_SERVICE_CACHE_ID)
				.withValueResolver(this.createCacheUserIdsValueResolver()).withShared(true).withClusterEnabled(true)
				.withTransactional(true).withTransactionalFastForward(true).withTimeToLive(1L, TimeUnit.HOURS, false);
	}

	protected ICacheValueResolver<String, Long> createCacheUserIdsValueResolver() {
		return new AbstractCacheValueResolver<String, Long>() {

			@Override
			public Long resolve(final String key) {
				return AccessControlService.this.getUserIdAsLong(key);
			}
		};
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
		return this.getUserIdsCache().get(userId);
	}

	public Set<String> getUserNotificationIds(final Long userId) {
		final IUserService userServcie = BEANS.get(IUserService.class);
		return userServcie.getUserNotificationIds(userId);
	}

	/**
	 * Clear the cache with the passed userIds (login and Email)
	 *
	 * @param userId
	 *            login or email to user cache
	 */
	public void clearUserCache(final Set<String> userIds) {
		this.clearCache(userIds);
		this.clearUserIdsCache(userIds);
	}

	public void clearUserIdsCache() throws ProcessingException {
		this.getUserIdsCache().invalidate(new AllCacheEntryFilter<String, Long>(), true);
	}

	public void clearUserIdsCacheOfCurrentUser() throws ProcessingException {
		this.clearUserIdsCache(Collections.singleton(this.getCurrentUserCacheKey()));
	}

	protected void clearUserIdsCache(final Collection<? extends String> cacheKeys) throws ProcessingException {
		if (cacheKeys != null && !cacheKeys.isEmpty()) {
			this.getUserIdsCache().invalidate(new KeyCacheEntryFilter<String, Long>(cacheKeys), true);
		}
	}

	private ICache<String, Long> getUserIdsCache() {
		return this.m_cacheUserIds;
	}

}
