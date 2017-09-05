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

import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.user.IUserService;
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
		// SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_DAY);
		// SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_LUNCH);
		// SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_EVENING);
		// SQL.insert(SQLs.SLOT_INSERT_SAMPLE + SQLs.SLOT_VALUES_WEEK_END);

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

		final IUserService userService = BEANS.get(IUserService.class);
		final Set<Long> allUserIds = userService.getAllUserId();
		if (null != allUserIds) {
			for (final Long userId : allUserIds) {
				this.createSlotAndDayDurationForUser(userId);
			}
		}

		// SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE +
		// SQLs.DAY_DURATION_VALUES_MORNING.replace("__slot_id__", "1"));
		// SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE +
		// SQLs.DAY_DURATION_VALUES_AFTERNOON.replace("__slot_id__", "1"));
		// SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE +
		// SQLs.DAY_DURATION_VALUES_LUNCH.replace("__slot_id__", "2"));
		// SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE +
		// SQLs.DAY_DURATION_VALUES_EVENING.replace("__slot_id__", "3"));
		// SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE +
		// SQLs.DAY_DURATION_VALUES_WWEEKEND.replace("__slot_id__", "4"));
	}

	private void createSlotAndDayDurationForUser(final Long userId) {
		LOG.info("Creating data for user : " + userId);
		Long slotId = this.createSlot("zc.meeting.slot.1", userId);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_MORNING, slotId));
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_AFTERNOON, slotId));

		slotId = this.createSlot("zc.meeting.slot.2", userId);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_LUNCH, slotId));

		slotId = this.createSlot("zc.meeting.slot.3", userId);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_EVENING, slotId));

		slotId = this.createSlot("zc.meeting.slot.4", userId);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_WWEEKEND, slotId));
	}

	/**
	 * Create a new Slot and return the Slot Id
	 *
	 * @param slotName
	 * @param userId
	 * @return
	 */
	private Long createSlot(final String slotName, final Long userId) {
		final Long slotId = this.getDatabaseHelper().getNextVal(SLOT_ID_SEQ);
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE + this.forSlot(SQLs.SLOT_VALUES_GENERIC, slotId)
				.replace("__slotName__", slotName).replace("__userId__", String.valueOf(userId)));
		return slotId;
	}

	private String forSlot(final String sql, final Long slotId) {
		return sql.replace("__slotId__", String.valueOf(slotId));
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
