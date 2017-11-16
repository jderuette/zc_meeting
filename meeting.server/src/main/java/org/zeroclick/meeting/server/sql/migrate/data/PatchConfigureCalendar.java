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
public class PatchConfigureCalendar extends AbstractDataPatcher {

	public static final String AGENDA_CONFIG_TABLE_NAME = "AGENDA_CONFIGURATION";

	private static final Logger LOG = LoggerFactory.getLogger(PatchConfigureCalendar.class);

	public PatchConfigureCalendar() {
		this.setDescription("Add agenda configuration and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.8");
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
			LOG.info("Add agenda configuration will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add agenda configuration upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().existTable(AGENDA_CONFIG_TABLE_NAME)) {
			SQL.insert(SQLs.AGENDA_CONFIG_CREATE);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add agenda configuration upgraing default data");

		// BEANS.get(GoogleApiHelper.class);
		// TODO create a default configuration for each agenda for each existing
		// OAuthCredential

	}

	@Override
	public void undo() {
		LOG.info("Add agenda configuration downgrading data strcuture");
		this.getDatabaseHelper().dropTable(AGENDA_CONFIG_TABLE_NAME);
	}
}
