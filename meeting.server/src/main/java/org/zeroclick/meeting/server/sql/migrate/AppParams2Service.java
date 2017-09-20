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
package org.zeroclick.meeting.server.sql.migrate;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.SQLs;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class AppParamsService {

	private static final Logger LOG = LoggerFactory.getLogger(AppParamsService.class);

	public static final String KEY_DATA_VERSION = "dataVersion";

	public void create(final String key, final String value) {
		LOG.debug("Creating app_params with key : " + key + " and value : " + value);

		SQL.update(SQLs.PARAMS_INSERT, new NVPair("id", this.getNextId()), new NVPair("key", key),
				new NVPair("value", value));

	}

	public String getValue(final String key) {
		final Object value = this.getData(key, 2);
		return null == value ? null : (String) value;
	}

	protected Long getId(final String key) {
		final Object paramId = this.getData(key, 0);
		return null == paramId ? null : (Long) paramId;
	}

	public void store(final String key, final String value) {
		LOG.debug("Storing app_params with key : " + key + " and value : " + value);

		final Long existingId = this.getId(key);
		if (null == existingId) {
			this.create(key, value);
		} else {
			LOG.debug("Updatting app_params with id : " + existingId + " with key : " + key + " and value : " + value);
			SQL.update(SQLs.PARAMS_UPDATE, new NVPair("key", key), new NVPair("value", value));
		}
	}

	protected Object getData(final String key, final Integer columnNumber) {
		LOG.debug("Searching app_params for key : " + key);
		Object paramValue;
		final Object[][] datas = SQL.select(SQLs.PARAMS_SELECT + SQLs.PARAMS_SELECT_FILTER_KEY, new NVPair("key", key));

		if (null != datas && datas.length == 1) {
			paramValue = datas[0][columnNumber];
		} else {
			LOG.warn("Multiple values for key : " + key + ". Returning the first one");
			paramValue = datas[0][columnNumber];
		}

		return paramValue;
	}

	protected Long getNextId() {
		return SQL.getSequenceNextval("APP_PARAMS_ID_SEQ");
	}
}
