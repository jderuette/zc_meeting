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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;
import org.zeroclick.meeting.shared.event.CreateEventPermission;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchCreateSubscription extends AbstractDataPatcher {

	public static final String SUBSCRIPTION_TABLE_NAME = "SUBSCRIPTION";
	public static final String SUBSCRIPTION_ID_SEQ = "SUBSCRIPTION_ID_SEQ";
	public static final String SUBSCRIBE_TABLE_NAME = "SUBSCRIBE";

	private static final Logger LOG = LoggerFactory.getLogger(PatchCreateSubscription.class);

	public PatchCreateSubscription() {
		this.setDescription("Create Subscription table and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.5");
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
			LOG.info("Create Subscription table will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Create Subscription table upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().isSequenceExists(SUBSCRIPTION_ID_SEQ)) {
			this.getDatabaseHelper().createSequence(SUBSCRIPTION_ID_SEQ);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(SUBSCRIPTION_TABLE_NAME)) {
			SQL.insert(SQLs.SUBSCRIPTION_CREATE_TABLE);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(SUBSCRIBE_TABLE_NAME)) {
			SQL.insert(SQLs.SUBSCRIBE_CREATE_TABLE);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Create Subscription table table upgraing default data");
		SQL.insert(SQLs.SUBSCRIPTION_INSERT_SAMPLE + SQLs.SUBSCRIPTION_INSERT_VALUES_FREE);
		SQL.insert(SQLs.SUBSCRIPTION_INSERT_SAMPLE + SQLs.SUBSCRIPTION_INSERT_VALUES_SOLO);
		SQL.insert(SQLs.SUBSCRIPTION_INSERT_SAMPLE + SQLs.SUBSCRIPTION_INSERT_VALUES_BUSINESS);

		// params to indicate limit for free event's users
		SQL.insert(SQLs.PARAMS_INSERT_SAMPLE_WITH_CATEGORY + SQLs.PARAMS_INSERT_VALUES_SUB_FREE_EVENT_LIMIT);

		// change permission "createEvent" to level 0 for existing roles
		SQL.insert(SQLs.ROLE_PERMISSION_CHANGE_CREATE_EVENT_TO_HIERARCHIC);

		// add subscriptions roles
		// roles Event_free, event_pro, event_business
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_SUB_FREE);
		this.getDatabaseHelper().addSubFreePermission("org.zeroclick.meeting.shared.event.CreateEventPermission",
				CreateEventPermission.LEVEL_SUB_FREE);
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_SUB_PRO);
		this.getDatabaseHelper().addSubProPermission("org.zeroclick.meeting.shared.event.CreateEventPermission",
				CreateEventPermission.LEVEL_SUB_PRO);
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE + SQLs.ROLE_VALUES_SUB_BUSINESS);
		this.getDatabaseHelper().addSubBusinessPermission("org.zeroclick.meeting.shared.event.CreateEventPermission",
				CreateEventPermission.LEVEL_SUB_BUSINESS);

		// add event_free Role to all users
		final IUserService userService = BEANS.get(IUserService.class);
		userService.addSubFreeToAllUsers();

		// add default roles to manage appParams using GUI (and forms)
		this.getDatabaseHelper()
				.addAdminPermission("org.zeroclick.configuration.shared.params.CreateAppParamsPermission", 100);
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.configuration.shared.params.ReadAppParamsPermission",
				100);
		this.getDatabaseHelper()
				.addAdminPermission("org.zeroclick.configuration.shared.params.UpdateAppParamsPermission", 100);
	}

	@Override
	public void undo() {
		LOG.info("Create Subscription table table downgrading data strcuture");
		if (this.getDatabaseHelper().existTable(SUBSCRIPTION_TABLE_NAME)) {
			this.getDatabaseHelper().dropTable(SUBSCRIPTION_TABLE_NAME, Boolean.TRUE);
		}

	}
}
