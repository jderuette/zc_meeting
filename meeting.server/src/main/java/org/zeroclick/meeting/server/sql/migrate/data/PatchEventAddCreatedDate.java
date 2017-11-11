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
public class PatchEventAddCreatedDate extends AbstractDataPatcher {

	private static final Logger LOG = LoggerFactory.getLogger(PatchEventAddCreatedDate.class);

	private static final String PATCHED_TABLE = "event";
	public static final String PATCHED_COLUMN = "created_date";

	public PatchEventAddCreatedDate() {
		this.setDescription("Add created Date column on event");
	}

	@Override
	protected void execute() {
		if (super.canMigrate()) {
			LOG.info("Add created Date column on event will be apply to the data");
			this.migrateStrucutre();
			this.migrateData();
		}
	}

	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.6");
	}

	private void migrateStrucutre() {
		LOG.info("Add created Date column on event upgrading data strcuture");
		if (!this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_COLUMN)) {
			SQL.insert(SQLs.EVENT_ALTER_TABLE_ADD_CREATED_DATE);
		}

	}

	private void migrateData() {
		LOG.info("Add created Date column on event No data migration needed");
	}

	@Override
	public void undo() {
		LOG.info("EAdd created Date column on event downgrading data strcuture");
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_COLUMN);
	}
}
