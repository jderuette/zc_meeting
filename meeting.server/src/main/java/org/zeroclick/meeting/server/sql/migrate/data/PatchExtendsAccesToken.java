package org.zeroclick.meeting.server.sql.migrate.data;

import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

public class PatchExtendsAccesToken extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "OAUHTCREDENTIAL";
	public static final String PATCHED_COLUMN = "access_token";

	private static final Logger LOG = LoggerFactory.getLogger(PatchManageMicrosoftCalendars.class);

	public PatchExtendsAccesToken() {
		this.setDescription("Extends accesToken column to 2 500 char");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.14");
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
			LOG.info("Extends accesToken column will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Extends accesToken column upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (this.getDatabaseHelper().existTable(PATCHED_TABLE)) {
			if (this.getDatabaseHelper().isColumnExists(PATCHED_TABLE, PATCHED_COLUMN)) {
				SQL.insert(SQLs.OAUHTCREDENTIAL_PATCH_ALTER_ACCES_TOKEN_COLUMN_TO_2500_LENGHT);
				structureAltered = Boolean.TRUE;
			}
		}

		if (structureAltered) {
			// as it create a Table force a refresh of Table Cache
			this.getDatabaseHelper().resetExistingTablesCache();
		}

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Extends accesToken column upgraing default data");

	}

	@Override
	public void undo() {
		LOG.info("Extends accesToken column downgrading data strcuture");
		// TODO set the original column length
	}
}
