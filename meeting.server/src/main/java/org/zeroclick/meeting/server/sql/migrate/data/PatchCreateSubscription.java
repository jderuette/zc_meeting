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
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
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

	public static final String SUBSCRIPTION_TABLE_NAME = "SUBSCRIPTION_METADATA";
	public static final String SUBSCRIPTION_ID_SEQ = "SUBSCRIPTION_ID_SEQ";
	public static final String PATCHED_TABLE_USER_ROLE = "USER_ROLE";
	public static final String ADDED_USER_ROLE_COLUMN = "start_date";
	public static final String PATCHED_TABLE_ROLE = "ROLE";
	public static final String ADDED_ROLE_COLUMN = "type";
	public static final String DOCUMENT_TABLE_NAME = "DOCUMENT";
	public static final String DOCUMENT_ID_SEQ = "DOCUMENT_ID_SEQ";
	public static final String ROLE_DOCUMENT_TABLE_NAME = "ROLE_DOCUMENT";

	private static final String PARAMS_CATEGORY = "subscription";

	private static final String PERMISSION_CREATE_EVENT_NAME = "org.zeroclick.meeting.shared.event.CreateEventPermission";

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

		// if (!this.getDatabaseHelper().isSequenceExists(SUBSCRIPTION_ID_SEQ))
		// {
		// this.getDatabaseHelper().createSequence(SUBSCRIPTION_ID_SEQ);
		// structureAltered = Boolean.TRUE;
		// }

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE_USER_ROLE)) {
			SQL.insert(SQLs.USER_ROLE_ADD_START_DATE);
			SQL.insert(SQLs.USER_ROLE_START_DATE_ADD_DEFAULT);

			SQL.insert(SQLs.USER_ROLE_PK_DROP);
			SQL.insert(SQLs.USER_ROLE_START_DATE_NOW_TO_EXISTING);
			SQL.insert(SQLs.USER_ROLE_PK_ADD_START_DATE);

			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(SUBSCRIPTION_TABLE_NAME)) {
			SQL.insert(SQLs.SUBSCRIPTION_CREATE_TABLE);
			structureAltered = Boolean.TRUE;
		}

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE_ROLE)) {
			SQL.insert(SQLs.ROLE_ADD_TYPE);
			// define default type for existing roles
			SQL.insert(SQLs.ROLE_ADD_DEFAULT_TYPE_TO_EXISTING);
			SQL.insert(SQLs.ROLE_ALTER_TYPE_NOT_NULL);
			structureAltered = Boolean.TRUE;
		}

		// Document table and Data
		if (!this.getDatabaseHelper().existTable(DOCUMENT_TABLE_NAME)) {
			final String blobType = this.getDatabaseHelper().getBlobType();
			SQL.insert(SQLs.DOCUMENT_CREATE.replace("__blobType__", blobType));
			structureAltered = Boolean.TRUE;
		}
		this.getDatabaseHelper().createSequence(DOCUMENT_ID_SEQ);

		if (!this.getDatabaseHelper().existTable(ROLE_DOCUMENT_TABLE_NAME)) {
			SQL.insert(SQLs.ROLE_DOCUMENT_CREATE);
			SQL.insert(SQLs.ROLE_DOCUMENT_ADD_PK);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Create Subscription table table upgraing default data");
		final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);
		// params to indicate limit for free event's users
		SQL.insert(SQLs.PARAMS_INSERT_SAMPLE_WITH_CATEGORY + SQLs.PARAMS_INSERT_VALUES_SUB_FREE_EVENT_LIMIT);
		SQL.insert(SQLs.PARAMS_INSERT_SAMPLE_WITH_CATEGORY + SQLs.PARAMS_INSERT_VALUES_SUB_INFO_EMAIL);

		// change permission "createEvent" to level 0 for existing roles
		SQL.insert(SQLs.ROLE_PERMISSION_CHANGE_EVENT_TO_HIERARCHIC);

		// add subscriptions roles
		// roles Event_free, event_pro, event_business
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE_WITH_TYPE + SQLs.ROLE_VALUES_SUB_FREE);
		this.getDatabaseHelper().addSubFreePermission(PERMISSION_CREATE_EVENT_NAME,
				CreateEventPermission.LEVEL_SUB_FREE);
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE_WITH_TYPE + SQLs.ROLE_VALUES_SUB_PRO);
		this.getDatabaseHelper().addSubProPermission(PERMISSION_CREATE_EVENT_NAME, CreateEventPermission.LEVEL_SUB_PRO);
		SQL.insert(SQLs.ROLE_INSERT_SAMPLE_WITH_TYPE + SQLs.ROLE_VALUES_SUB_BUSINESS);
		this.getDatabaseHelper().addSubBusinessPermission(PERMISSION_CREATE_EVENT_NAME,
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

		// add permission to asign/modify subscription meta Data
		// -- for admins
		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission",
				CreateAssignSubscriptionToUserPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission",
				UpdateAssignSubscriptionToUserPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission",
				ReadAssignSubscriptionToUserPermission.LEVEL_ALL);
		// -- for standard users
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission",
				CreateAssignSubscriptionToUserPermission.LEVEL_OWN);
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission",
				UpdateAssignSubscriptionToUserPermission.LEVEL_OWN);
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission",
				ReadAssignSubscriptionToUserPermission.LEVEL_OWN);

		// default params for URL for subscriptions
		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_BASE + "3", null, PARAMS_CATEGORY);
		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "3", null, PARAMS_CATEGORY);

		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_BASE + "4",
				"https://subscriptions.zoho.com/subscribe/e0c71c8b88c7cb1944d3227cb7edba566a2bba0f6b053217afe8ded60e8a6aa6/TEST_PRO",
				PARAMS_CATEGORY);
		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "4", "zc.user.role.pro", PARAMS_CATEGORY);

		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_BASE + "5",
				"https://subscriptions.zoho.com/subscribe/e0c71c8b88c7cb1944d3227cb7edba566a2bba0f6b053217afe8ded60e8a6aa6/TEST_BUSINESS",
				PARAMS_CATEGORY);
		appParamsService.create(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "5", "zc.user.role.business",
				PARAMS_CATEGORY);

		// acces or admin to documents
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.common.shared.document.CreateDocumentPermission");
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.common.shared.document.UpdateDocumentPermission");
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.common.shared.document.ReadDocumentPermission");
		// -- for standard users
		this.getDatabaseHelper()
				.addStandardUserPermission("org.zeroclick.common.shared.document.ReadDocumentPermission");

	}

	@Override
	public void undo() {
		LOG.info("Create Subscription table table downgrading data strcuture");
		final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);
		if (this.getDatabaseHelper().existTable(SUBSCRIPTION_TABLE_NAME)) {
			this.getDatabaseHelper().dropTable(SUBSCRIPTION_TABLE_NAME, Boolean.TRUE);
		}

		this.getDatabaseHelper().dropSequence(SUBSCRIPTION_ID_SEQ);

		this.getDatabaseHelper().dropSequence(DOCUMENT_ID_SEQ);
		this.getDatabaseHelper().dropTable(DOCUMENT_TABLE_NAME);
		this.getDatabaseHelper().dropTable(ROLE_DOCUMENT_TABLE_NAME);

		this.getDatabaseHelper().removeColumn(PATCHED_TABLE_USER_ROLE, ADDED_USER_ROLE_COLUMN);

		this.getDatabaseHelper().removeColumn(PATCHED_TABLE_ROLE, ADDED_ROLE_COLUMN);

		LOG.info("Create Subscription table table downgrading (default) data");

		this.getDatabaseHelper().removeSubFreePermission(PERMISSION_CREATE_EVENT_NAME);
		SQL.insert(SQLs.ROLE_DELETE, new NVPair("roleId", 3));
		this.getDatabaseHelper().removeSubProPermission(PERMISSION_CREATE_EVENT_NAME);
		SQL.insert(SQLs.ROLE_DELETE, new NVPair("roleId", 4));
		this.getDatabaseHelper().removeSubBusinessPermission(PERMISSION_CREATE_EVENT_NAME);
		SQL.insert(SQLs.ROLE_DELETE, new NVPair("roleId", 5));

		// Remove default roles to manage appParams using GUI (and forms)
		this.getDatabaseHelper()
				.removeAdminPermission("org.zeroclick.configuration.shared.params.CreateAppParamsPermission");
		this.getDatabaseHelper()
				.removeAdminPermission("org.zeroclick.configuration.shared.params.ReadAppParamsPermission");
		this.getDatabaseHelper()
				.removeAdminPermission("org.zeroclick.configuration.shared.params.UpdateAppParamsPermission");

		if (this.getDatabaseHelper().existTable("APP_PARAMS")) {
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_BASE + "3");
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "3");
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_BASE + "4");
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "4");
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_BASE + "5");
			appParamsService.delete(SubscriptionHelper.PARAM_KEY_URL_NAME_BASE + "5");
		}

	}
}
