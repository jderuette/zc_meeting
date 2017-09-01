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
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchSlotTable extends AbstractDataPatcher {

	private static final String SLOT_TABLE_NAME = "SLOT";
	public static final String SLOT_ID_SEQ = "SLOT_ID_SEQ";

	private static final String DAY_DURATION_TABLE_NAME = "DAY_DURATION";
	public static final String DAY_DURATION_ID_SEQ = "DAY_DURATION_ID_SEQ";

	private static final Logger LOG = LoggerFactory.getLogger(PatchSlotTable.class);

	public PatchSlotTable() {
		this.setDescription("Create SLOT and dAY_DURATION table and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.2.0");
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
			LOG.info("Create Slot and DayDuration table will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Create Slot and DayDuration table upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;
		if (!this.getDatabaseHelper().isSequenceExists(SLOT_ID_SEQ)) {
			this.getDatabaseHelper().createSequence(SLOT_ID_SEQ);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(SLOT_TABLE_NAME)) {
			SQL.insert(SQLs.SLOT_CREATE_TABLE);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().isSequenceExists(DAY_DURATION_ID_SEQ)) {
			this.getDatabaseHelper().createSequence(DAY_DURATION_ID_SEQ);
			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(DAY_DURATION_TABLE_NAME)) {
			SQL.insert(SQLs.DAY_DURATION_CREATE_TABLE);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Create Slot and DayDuration table upgraing default data");
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_DAY);
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_LUNCH);
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_EVENING);
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_WEEK_END);

		// admin permission
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.configuration.shared.slot.CreateSlotPermission",
				BasicHierarchyPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.configuration.shared.slot.ReadSlotPermission",
				BasicHierarchyPermission.LEVEL_ALL);
		this.getDatabaseHelper().addAdminPermission("org.zeroclick.configuration.shared.slot.UpdateSlotPermission",
				BasicHierarchyPermission.LEVEL_ALL);

		// standard user permission
		this.getDatabaseHelper()
				.addStandardUserPermission("org.zeroclick.configuration.shared.slot.CreateSlotPermission", 10);
		this.getDatabaseHelper().addStandardUserPermission("org.zeroclick.configuration.shared.slot.ReadSlotPermission",
				10);
		this.getDatabaseHelper()
				.addStandardUserPermission("org.zeroclick.configuration.shared.slot.UpdateSlotPermission", 10);

		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + SQLs.DAY_DURATION_VALUES_MORNING);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + SQLs.DAY_DURATION_VALUES_AFTERNOON);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + SQLs.DAY_DURATION_VALUES_LUNCH);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + SQLs.DAY_DURATION_VALUES_EVENING);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + SQLs.DAY_DURATION_VALUES_WWEEKEND);

	}

	@Override
	public void undo() {
		LOG.info("Create Slot and DayDuration table downgrading data strcuture");

		this.getDatabaseHelper().dropSequence(SLOT_ID_SEQ);
		this.getDatabaseHelper().dropTable(SLOT_TABLE_NAME, Boolean.FALSE);

		this.getDatabaseHelper().dropSequence(DAY_DURATION_ID_SEQ);
		this.getDatabaseHelper().dropTable(DAY_DURATION_TABLE_NAME, Boolean.FALSE);

	}
}
