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

import org.eclipse.scout.rt.platform.holders.NVPair;
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
public class PatchUserLanguage extends AbstractDataPatcher {

	private static final Logger LOG = LoggerFactory.getLogger(PatchUserLanguage.class);

	public PatchUserLanguage() {
		this.setDescription("Add Language colum on User");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.3");
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
		LOG.info("User Language  upgrading data strcuture");
		if (!this.getDatabaseHelper().isColumnExists("app_user", "language")) {
			SQL.insert(SQLs.USER_ALTER_TABLE_LANGUAGE);
		}
	}

	private void migrateData() {
		LOG.info("User Language data migration start");
		SQL.update("UPDATE APP_USER set language='FR' where login=:login", new NVPair("login", "d"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher#undo()
	 */
	@Override
	protected void undo() {
		LOG.info("User Language downgrading data strcuture NOT RUN (need bug fix)");
	}

}
