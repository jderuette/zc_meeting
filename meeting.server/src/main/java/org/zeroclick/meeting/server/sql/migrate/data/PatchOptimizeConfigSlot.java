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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchOptimizeConfigSlot extends AbstractDataPatcher {

	public static final String ROLE_ID_SEQ = "ROLE_ID_SEQ";

	private static final Logger LOG = LoggerFactory.getLogger(PatchOptimizeConfigSlot.class);

	public PatchOptimizeConfigSlot() {
		this.setDescription("Optmimize Slot TABLE and default required params key/value");
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
			LOG.info("Optmimize Slot will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Optmimize Slot upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().isSequenceExists(ROLE_ID_SEQ)) {
			this.getDatabaseHelper().createSequence(ROLE_ID_SEQ, 6);
			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		// this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Optmimize Slot upgraing default data");

		final ISlotService slotService = BEANS.get(ISlotService.class);
		slotService.updateDayDurationsByTemplate("zc.meeting.slot.3", "20:00:00", "23:30:00", "19:00:00", "21:00:00");
		slotService.updateDayDurationsByTemplate("zc.meeting.slot.4", "10:00:00", "23:00:00", "08:00:00", "18:00:00");
	}

	@Override
	public void undo() {
		LOG.info("Optmimize Slot downgrading data strcuture");
	}
}
