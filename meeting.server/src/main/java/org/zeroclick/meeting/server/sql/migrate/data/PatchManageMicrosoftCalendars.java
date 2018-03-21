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
public class PatchManageMicrosoftCalendars extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "OAUHTCREDENTIAL";
	public static final String PATCHED_COLUMN = "access_token";
	public static final String PATCHED_COLUMN_REFRESH_TOKEN = "refresh_token";
	public static final String PATCHED_ADDED_COLUMN_TENANT_ID = "tenant_id";

	private static final Logger LOG = LoggerFactory.getLogger(PatchManageMicrosoftCalendars.class);

	public PatchManageMicrosoftCalendars() {
		this.setDescription("Add Microsoft Apis and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.12");
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
			LOG.info("Add Microsoft Apis will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add Microsoft Apis upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_COLUMN)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_PATCH_ALTER_ACCES_TOKEN_COLUMN_LENGHT);
				structureAltered = Boolean.TRUE;
			}

			if (this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_COLUMN_REFRESH_TOKEN)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_PATCH_ALTER_REFRESH_TOKEN_COLUMN_LENGHT);
				structureAltered = Boolean.TRUE;
			}

			if (!this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_COLUMN_TENANT_ID)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_PATCH_ALTER_ADD_TENANT_ID_COLUMN);
				structureAltered = Boolean.TRUE;
			}
		}

		if (structureAltered) {
			// as it create a Table force a refresh of Table Cache
			this.getDatabaseHelper().resetExistingTablesCache();
		}

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add Microsoft Apis upgraing default data");

	}

	@Override
	public void undo() {
		LOG.info("Add calendar configuration downgrading data strcuture");
		// TODO set the original column length
	}
}
