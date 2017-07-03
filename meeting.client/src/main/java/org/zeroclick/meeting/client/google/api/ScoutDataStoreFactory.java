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
package org.zeroclick.meeting.client.google.api;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.common.CallTrackerService;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

/**
 * @author djer
 *
 */
public class ScoutDataStoreFactory extends AbstractDataStoreFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ScoutDataStoreFactory.class);

	@Override
	protected <V extends Serializable> DataStore<V> createDataStore(final String id) throws IOException {
		return new ScoutDataStore<>(this, id);
	}

	public class ScoutDataStore<V extends Serializable> extends AbstractDataStore<V> {

		private final CallTrackerService<String> callTracker;

		// TODO Djer13 use a real cache manager (TTL, maxItems ?)
		private final Map<String, V> cache;

		protected ScoutDataStore(final DataStoreFactory dataStoreFactory, final String id) {
			super(dataStoreFactory, id);
			this.callTracker = new CallTrackerService<>(3, Duration.ofMinutes(1), "Manage Google Flow");
			this.cache = new HashMap<>();
		}

		@Override
		public Set<String> keySet() throws IOException {
			LOG.debug("Entering keySet()");
			final IApiService oAuthCredentialService = BEANS.get(IApiService.class);
			return oAuthCredentialService.getAllUserId();
		}

		@Override
		public Collection<V> values() throws IOException {
			LOG.debug("Entering values()");
			final IApiService oAuthCredentialService = BEANS.get(IApiService.class);
			final Collection<ApiFormData> apiFormDatas = oAuthCredentialService.loadGoogleData();

			// extract only Google usefull datas
			final List<V> result = Lists.newArrayList();
			for (final ApiFormData apiFormData : apiFormDatas) {
				result.add(IOUtils.deserialize(apiFormData.getProviderData()));
			}

			return result;
		}

		@Override
		public V get(final String key) throws IOException {
			LOG.debug("Entering get(" + key + ")");
			V retValue = null;

			if (this.cache.containsKey(key)) {
				LOG.debug("returning credential from local cache for (" + key + ")");
				return this.cache.get(key);
			}
			final IApiService oAuthCredentialService = BEANS.get(IApiService.class);

			this.callTracker.validateCanCall(key);

			final ApiFormData input = new ApiFormData();
			input.setUserId(new Long(key));

			final ApiFormData data = oAuthCredentialService.load(input);
			/*
			 * if (null != data.getAccessToken().getValue()) { exitingCredential
			 * = this.createBaseCredential(key);
			 * exitingCredential.setAccessToken(data.getAccessToken().getValue()
			 * );
			 * exitingCredential.setRefreshToken(data.getRefreshToken().getValue
			 * ()); exitingCredential.setExpirationTimeMilliseconds(data.
			 * getExpirationTimeMilliseconds().getValue()); }
			 */
			retValue = IOUtils.deserialize(data.getProviderData());
			if (null != retValue && !this.cache.containsKey(key)) {
				this.cache.put(key, retValue);
			}

			return retValue;
		}

		@Override
		public DataStore<V> set(final String key, final V value) throws IOException {
			LOG.debug("Entering set(" + key + ", " + value + ")");

			final IApiService apiService = BEANS.get(IApiService.class);

			final ApiFormData input = new ApiFormData();
			input.setUserId(new Long(key));
			input.getProvider().setValue(1);

			final ApiFormData createdData = apiService.create(input);
			createdData.setUserId(new Long(key));
			createdData.setProviderData(IOUtils.serialize(value));
			createdData.getProvider().setValue(1);

			if (value instanceof StoredCredential) {
				final StoredCredential credential = (StoredCredential) value;
				createdData.getAccessToken().setValue(credential.getAccessToken());
				createdData.getRefreshToken().setValue(credential.getRefreshToken());
				createdData.getExpirationTimeMilliseconds().setValue(credential.getExpirationTimeMilliseconds());
			} else {
				LOG.warn(
						"Could not extract detailed information from Google Data beacause its not an instance of Credential");
			}

			apiService.store(createdData);
			this.callTracker.resetNbCall(key);

			if (this.cache.containsKey(key)) {
				this.cache.remove(key);
			}
			this.cache.put(key, value);
			return this;
		}

		@Override
		public DataStore<V> clear() throws IOException {
			LOG.debug("Entering clear()");
			// TODO Auto-generated method stub
			LOG.warn("Clear not implemented");
			this.cache.clear(); // partial implementation !
			return null;
		}

		@Override
		public DataStore<V> delete(final String key) throws IOException {
			LOG.debug("Entering delete(" + key + ")");
			final IApiService oAuthCredentialService = BEANS.get(IApiService.class);
			final ApiFormData input = new ApiFormData();
			input.setUserId(new Long(key));
			oAuthCredentialService.delete(input);
			if (this.cache.containsKey(key)) {
				this.cache.remove(key);
			}
			return this;
		}

	}
}
