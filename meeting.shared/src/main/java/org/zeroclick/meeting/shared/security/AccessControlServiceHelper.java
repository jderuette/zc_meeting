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
package org.zeroclick.meeting.shared.security;

import java.security.AccessController;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.cache.AbstractCacheValueResolver;
import org.eclipse.scout.rt.shared.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.KeyCacheEntryFilter;
import org.zeroclick.configuration.shared.user.IUserService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class AccessControlServiceHelper implements IAccessControlServiceHelper {

	public static final String USER_IDS_SERVICE_CACHE_ID = "ZeroClickUserIds";

	private final ICache<String, Long> m_cacheUserIds;

	public AccessControlServiceHelper() {
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
				return AccessControlServiceHelper.this.getUserIdAsLong(key);
			}
		};
	}

	protected Long getUserIdAsLong(final String userId) {
		final IUserService userService = BEANS.get(IUserService.class);

		return userService.getUserId(userId);
	}

	@Override
	public Long getZeroClickUserIdOfCurrentSubject() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final String userId = acs.getUserId(Subject.getSubject(AccessController.getContext()));
		return this.getUserIdsCache().get(userId);
	}

	@Override
	public Set<String> getUserNotificationIds(final Long userId) {
		final IUserService userServcie = BEANS.get(IUserService.class);
		return userServcie.getUserNotificationIds(userId);
	}

	protected String getCurrentUserCacheKey() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		return acs.getUserIdOfCurrentUser();
	}

	public void clearUserIdsCache() throws ProcessingException {
		this.getUserIdsCache().invalidate(new AllCacheEntryFilter<String, Long>(), true);
	}

	public void clearUserIdsCacheOfCurrentUser() throws ProcessingException {
		this.clearUserIdsCache(Collections.singleton(this.getCurrentUserCacheKey()));
	}

	@Override
	public void clearUserIdsCache(final Collection<? extends String> cacheKeys) throws ProcessingException {
		if (cacheKeys != null && !cacheKeys.isEmpty()) {
			this.getUserIdsCache().invalidate(new KeyCacheEntryFilter<String, Long>(cacheKeys), true);
		}
	}

	private ICache<String, Long> getUserIdsCache() {
		return this.m_cacheUserIds;
	}

}
