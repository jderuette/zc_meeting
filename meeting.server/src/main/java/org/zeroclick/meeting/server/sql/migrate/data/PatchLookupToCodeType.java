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
package org.zeroclick.meeting.server.sql.migrate.data;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;
import org.zeroclick.meeting.shared.event.IEventService;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchLookupToCodeType extends AbstractDataPatcher {

	private static final Logger LOG = LoggerFactory.getLogger(PatchLookupToCodeType.class);

	private static final String EVENT_TABLE_NAME = "event";
	private static final String PARAMS_TABLE_NAME = "app_params";

	public PatchLookupToCodeType() {
		this.setDescription("Patch refactor LookupCall to CodeType (and default values)");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.11");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher#execute()
	 */
	@Override
	protected void execute() {
		if (super.canMigrate()) {
			LOG.info("Patch refactor LookupCall to CodeType (and default values) will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info(
				"Patch refactor LookupCall to CodeType (and default values) upgrading data strcuture (no modifications)");
		return Boolean.TRUE;
	}

	private void migrateData() {
		LOG.info("Patch refactor LookupCall to CodeType (and default values) upgraing default data");

		if (this.getDatabaseHelper().existTable(EVENT_TABLE_NAME)) {
			// update duration from minute (15,60,120, ...) to Code ID
			final IEventService eventService = BEANS.get(IEventService.class);
			eventService.migrateDurationlookupToCodeType();
		}

		// add default params for event Tracker
		if (this.getDatabaseHelper().existTable(PARAMS_TABLE_NAME)) {
			final IAppParamsService paramService = BEANS.get(IAppParamsService.class);
			paramService.create(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX, String.valueOf(20),
					"CallTracker");
			paramService.create(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION, String.valueOf(3),
					"CallTracker");
		}
	}

	@Override
	public void undo() {
		LOG.info("Patch refactor LookupCall to CodeType (and default values) downgrading data strcuture");

		if (this.getDatabaseHelper().existTable(PARAMS_TABLE_NAME)) {
			final IAppParamsService paramService = BEANS.get(IAppParamsService.class);
			paramService.delete(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX);
			paramService.delete(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION);
		}
	}
}
