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
public class PatchSubscriptionRoleDocumentLink extends AbstractDataPatcher {
	private static final Logger LOG = LoggerFactory.getLogger(PatchSubscriptionRoleDocumentLink.class);

	public PatchSubscriptionRoleDocumentLink() {
		this.setDescription("Create Subscription table and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.6");
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
			LOG.info("Modify Subscription's link between role and document will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Modify Subscription's link between role and document upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME)) {

			this.getDatabaseHelper().deletePrimaryKey(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME);
			SQL.insert(SQLs.ROLE_DOCUMENT_ADD_PK_WITHOUT_START_DATE);

			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		// this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Modify Subscription's link between role and document upgraing default data");

	}

	@Override
	public void undo() {
		LOG.info("Modify Subscription's link between role and document downgrading data strcuture");

		// Drop current PK and re-create the old one
		this.getDatabaseHelper().deletePrimaryKey(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME);
		SQL.insert(SQLs.ROLE_DOCUMENT_ADD_PK);

	}
}
