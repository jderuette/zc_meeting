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
public class PatchAddEventDescription extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "EVENT";
	public static final String PATCHED_ADDED_DESCRIPTION_COLUMN = "description";

	private static final Logger LOG = LoggerFactory.getLogger(PatchAddEventDescription.class);

	public PatchAddEventDescription() {
		this.setDescription("Add description event's column and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.15");
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
			LOG.info("Add description event's column will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add description event's column upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (!this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_DESCRIPTION_COLUMN)) {
				final String blobType = this.getDatabaseHelper().getBlobType();
				SQL.insert(SQLs.EVENT_ALTER_TABLE_ADD_DESCRIPTION_DATE.replace("__blobType__", blobType));
				structureAltered = Boolean.TRUE;
			}
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add description event's column upgraing default data");
	}

	@Override
	public void undo() {
		LOG.info("Add Minimal and maximal event's date downgrading data strcuture");
		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_ADDED_DESCRIPTION_COLUMN)) {
				this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_ADDED_DESCRIPTION_COLUMN);
			}
		}
	}
}
