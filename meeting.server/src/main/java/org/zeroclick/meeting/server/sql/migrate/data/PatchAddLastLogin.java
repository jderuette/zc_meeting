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
public class PatchAddLastLogin extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "APP_USER";
	public static final String PATCHED_ADDED_COLUMN = "last_login";

	private static final Logger LOG = LoggerFactory.getLogger(PatchAddLastLogin.class);

	public PatchAddLastLogin() {
		this.setDescription("Add Last Login Column and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.7");
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
			LOG.info("Add Last Login will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add Last Login upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (!this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_COLUMN)) {
				SQL.insert(SQLs.USER_ALTER_TABLE_ADD_LAST_LOGIN);
				structureAltered = Boolean.TRUE;
			}
		}

		// as it create a Table force a refresh of Table Cache
		// this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add Last Login upgraing default data");
	}

	@Override
	public void undo() {
		LOG.info("Add Last Login downgrading data strcuture");
		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_COLUMN)) {
				this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_ADDED_COLUMN);
			}
		}
	}
}
