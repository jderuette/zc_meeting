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
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchAddSlotCode extends AbstractDataPatcher {

	private static final String SLOT_TABLE_NAME = "slot";
	public static final String SLOT_PATCHED_COLUMN = "slot_code";

	private static final Logger LOG = LoggerFactory.getLogger(PatchAddSlotCode.class);

	public PatchAddSlotCode() {
		this.setDescription("Add Slot Code to SLOT table and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.4");
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
			LOG.info("Add Slot Code to SLOT table  will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Add Slot Code to SLOT table upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().isColumnExists(SLOT_TABLE_NAME, SLOT_PATCHED_COLUMN)) {
			SQL.insert(SQLs.SLOT_ALTER_TABLE_ADD_CODE);
			structureAltered = Boolean.TRUE;
		}

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Add Slot Code to SLOT table upgraing default data");
		final ISlotService slotService = BEANS.get(ISlotService.class);
		slotService.addDefaultCodeToExistingSlot();
	}

	@Override
	public void undo() {
		LOG.info("Add Slot Code to SLOT table downgrading data strcuture");
		this.getDatabaseHelper().removeColumn(SLOT_TABLE_NAME, SLOT_PATCHED_COLUMN);
	}
}
