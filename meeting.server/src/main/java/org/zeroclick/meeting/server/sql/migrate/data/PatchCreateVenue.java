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

import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchCreateVenue extends AbstractDataPatcher {

	private static final String APP_PARAMS_TABLE_NAME = PatchCreateParamsTable.APP_PARAMS_TABLE_NAME;
	public static final String APP_PARAMS_PATCHED_COLUMN = "category";
	private static final String EVENT_TABLE_NAME = "event";
	public static final String EVENT_PATCHED_COLUMN = "venue";
	private static final Logger LOG = LoggerFactory.getLogger(PatchCreateVenue.class);

	public PatchCreateVenue() {
		this.setDescription("Create Venue for event (and default values)");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.4");
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
			LOG.info("Create Venue column and default values will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Create Venue column and default values upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().isColumnExists(APP_PARAMS_TABLE_NAME, APP_PARAMS_PATCHED_COLUMN)) {
			SQL.insert(SQLs.PARAMS_ALTER_TABLE_ADD_CATEGORY);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().isColumnExists(EVENT_TABLE_NAME, EVENT_PATCHED_COLUMN)) {
			SQL.insert(SQLs.EVENT_ALTER_TABLE_ADD_VENUE);
			structureAltered = Boolean.TRUE;
		}

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Create Venue column and default values upgraing default data");
		SQL.insert(SQLs.PARAMS_INSERT_SAMPLE_WITH_CATEGORY + SQLs.PARAMS_INSERT_VALUES_SKYPE);
		SQL.insert(SQLs.PARAMS_INSERT_SAMPLE_WITH_CATEGORY + SQLs.PARAMS_INSERT_VALUES_PHONE);
	}

	@Override
	public void undo() {
		LOG.info("Create Venue column and default values downgrading data strcuture");
		this.getDatabaseHelper().removeColumn(APP_PARAMS_TABLE_NAME, APP_PARAMS_PATCHED_COLUMN);
		this.getDatabaseHelper().removeColumn(EVENT_TABLE_NAME, EVENT_PATCHED_COLUMN);
	}

}
