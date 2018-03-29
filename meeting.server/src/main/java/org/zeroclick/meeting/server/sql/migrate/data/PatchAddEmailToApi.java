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
public class PatchAddEmailToApi extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "OAUHTCREDENTIAL";
	public static final String PATCHED_ADDED_COLUMN = "account_email";

	private static final Logger LOG = LoggerFactory.getLogger(PatchAddEmailToApi.class);

	public PatchAddEmailToApi() {
		this.setDescription("Add Email to API and default required params key/value");
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
			LOG.info("Add Email to API will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add Email to API upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)
				&& !this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_COLUMN)) {
			SQL.insert(SQLs.OAUHTCREDENTIAL_PATCH_ADD_EMAIL_COLUMN);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add Email to API upgraing default data");
	}

	@Override
	public void undo() {
		LOG.info("Add Email to API downgrading data strcuture");
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_ADDED_COLUMN);
	}
}
