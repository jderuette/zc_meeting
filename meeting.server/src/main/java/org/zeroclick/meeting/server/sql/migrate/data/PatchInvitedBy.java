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
public class PatchInvitedBy extends AbstractDataPatcher {

	private static final Logger LOG = LoggerFactory.getLogger(PatchInvitedBy.class);

	private static final String PATCHED_TABLE = "app_user";
	private static final String PATCHED_COLUMN = "invited_by";

	public PatchInvitedBy() {
		this.setDescription("Add Invited by colum on User");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.2");
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
			LOG.info("User Invited By will be apply to the data");
			this.migrateStrucutre();
			this.migrateData();
		}
	}

	private void migrateStrucutre() {
		LOG.info("User Invited By  upgrading data strcuture");
		if (!this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_COLUMN)) {
			SQL.insert(SQLs.USER_ALTER_TABLE_INVITED_BY);
			SQL.insert(SQLs.USER_ALTER_TABLE_INVITED_BY_CONSTRAINT);
		}
	}

	private void migrateData() {
		LOG.info("User Invited By No data migration needed");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher#undo()
	 */
	@Override
	protected void undo() {
		LOG.info("User Invited By downgrading data strcuture");
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_COLUMN);
	}

}
