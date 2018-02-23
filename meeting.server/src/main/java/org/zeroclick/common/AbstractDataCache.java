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
package org.zeroclick.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.cache.AbstractCacheValueResolver;
import org.eclipse.scout.rt.shared.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.KeyCacheEntryFilter;

/**
 * @author djer
 *
 */
public abstract class AbstractDataCache<K, V> implements IDataCache<K, V> {

	protected ICache<K, V> dataCache;

	public AbstractDataCache() {
		this.dataCache = this.createCacheDataBuilder().build();
	}

	/**
	 * Can be overridden to customize the cache builder
	 *
	 * @return {@link ICacheBuilder} for the internal cache
	 */
	protected ICacheBuilder<K, V> createCacheDataBuilder() {
		@SuppressWarnings("unchecked")
		final ICacheBuilder<K, V> cacheBuilder = BEANS.get(ICacheBuilder.class);
		return cacheBuilder.withCacheId(this.getCacheDataId()).withValueResolver(this.createCacheDataValueResolver())
				.withShared(true).withClusterEnabled(true).withTransactional(true).withTransactionalFastForward(true)
				.withTimeToLive(1L, TimeUnit.HOURS, false).withSizeBound(500);
	}

	protected ICacheValueResolver<K, V> createCacheDataValueResolver() {
		return new AbstractCacheValueResolver<K, V>() {

			@Override
			public V resolve(final K key) {
				return AbstractDataCache.this.loadForCache(key);
			}
		};
	}

	protected String getCacheDataId() {
		return this.getClass().getName();
	}

	protected void clearDataCache(final Collection<? extends K> cacheKeys) throws ProcessingException {
		if (cacheKeys != null && !cacheKeys.isEmpty()) {
			this.getCache().invalidate(new KeyCacheEntryFilter<K, V>(cacheKeys), true);
		}
	}

	@Override
	public void clearCache() throws ProcessingException {
		this.getCache().invalidate(new AllCacheEntryFilter<K, V>(), true);
	}

	@Override
	public void clearCache(final K dataId) {
		final Set<K> ids = new HashSet<>();
		ids.add(dataId);
		this.clearDataCache(ids);
	}

	@Override
	public abstract V loadForCache(final K key);

	public ICache<K, V> getCache() {
		return this.dataCache;
	}
}
