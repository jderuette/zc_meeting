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

import java.util.Date;

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

		// add "missing" subscription metadata for default "free" subscription
		// for existing users

		final Object[][] existingUserWithRole = SQL.select(SQLs.USER_ROLE_SELECT_ALL);

		if (null != existingUserWithRole && existingUserWithRole.length > 0) {
			for (int row = 0; row < existingUserWithRole.length; row++) {
				final Long userId = (Long) existingUserWithRole[row][0];
				final Long roleId = (Long) existingUserWithRole[row][1];
				final Date startDate = (Date) existingUserWithRole[row][2];

				// check if subscription metaData exists
				final Object[][] subscriptionMetadata = SQL.select(
						SQLs.SUBSCRIPTION_SELECT_ALL + SQLs.USER_ROLE_FILTER_USER_ID
								+ SQLs.USER_ROLE_FILTER_SUBSCRIPTION_ID + SQLs.USER_ROLE_FILTER_START_DATE,
						new NVPair("userId", userId), new NVPair("subscriptionId", roleId),
						new NVPair("startDate", startDate));

				if (null == subscriptionMetadata || subscriptionMetadata.length == 0) {
					LOG.info("Adding missing subscription metadata for userId : " + userId
							+ ", subscriptionId (roleId) " + roleId + ", StartDate " + startDate);
					SQL.insert(SQLs.SUBSCRIPTION_INSERT, new NVPair("userId", userId),
							new NVPair("subscriptionId", roleId), new NVPair("startDate", startDate));
				} else {
					LOG.debug("Subscription metadata already created for userId : " + userId
							+ ", subscriptionId (roleId) " + roleId + ", StartDate " + startDate);
				}
			}

		}

	}

	@Override
	public void undo() {
		LOG.info("Modify Subscription's link between role and document downgrading data strcuture");

		// Drop current PK and re-create the old one
		this.getDatabaseHelper().deletePrimaryKey(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME);
		if (this.getDatabaseHelper().existTable(PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME)) {
			SQL.insert(SQLs.ROLE_DOCUMENT_ADD_PK);
		}

	}
}
