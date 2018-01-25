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
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;
import org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchConfigureCalendar extends AbstractDataPatcher {

	public static final String CALENDAR_CONFIG_TABLE_NAME = "CALENDAR_CONFIGURATION";
	public static final String CALENDAR_CONFIG_ID_SEQ = "CALENDAR_CONFIGURATION_ID_SEQ";

	private static final Logger LOG = LoggerFactory.getLogger(PatchConfigureCalendar.class);

	public PatchConfigureCalendar() {
		this.setDescription("Add calendar configuration and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.8");
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
			LOG.info("Add calendar configuration will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add canledar configuration upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().existTable(CALENDAR_CONFIG_TABLE_NAME)) {
			SQL.insert(SQLs.CALENDAR_CONFIG_CREATE);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().isSequenceExists(CALENDAR_CONFIG_ID_SEQ)) {
			this.getDatabaseHelper().createSequence(CALENDAR_CONFIG_ID_SEQ);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add calendar configuration upgraing default data");

		final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);
		// adding calendar and config must be done in UI (by User) in calendar
		// Confiuration Page

		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission",
				CreateCalendarConfigurationPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission",
				ReadCalendarConfigurationPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission",
				UpdateCalendarConfigurationPermission.LEVEL_ALL);
		// -- for standard users
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission",
				CreateCalendarConfigurationPermission.LEVEL_OWN);
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission",
				UpdateAssignSubscriptionToUserPermission.LEVEL_OWN);
		this.getDatabaseHelper().addStandardUserPermission(
				"org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission",
				ReadAssignSubscriptionToUserPermission.LEVEL_OWN);

		LOG.info("create a default value for TOS URL");
		appParamsService.create(IAppParamsService.APP_PARAM_KEY_TOS_URL, "https://www.elycoop.fr/", "contract");

		LOG.info("Add missing roles to admin");

		this.getDatabaseHelper().addAdminPermission(
				"org.zeroclick.configuration.shared.role.ReadAssignToRolePermission",
				ReadAssignSubscriptionToUserPermission.LEVEL_ALL);

	}

	@Override
	public void undo() {
		LOG.info("Add calendar configuration downgrading data strcuture");
		this.getDatabaseHelper().dropTable(CALENDAR_CONFIG_TABLE_NAME);
		this.getDatabaseHelper().dropSequence(CALENDAR_CONFIG_ID_SEQ);
	}
}
