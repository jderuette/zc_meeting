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
package org.zeroclick.meeting.server.sql;

import java.util.Set;

import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.StringArrayHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
public class DatabaseHelper {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

	private Set<String> existingTables;
	private static DatabaseHelper instance;

	public DatabaseHelper() {

	}

	public static DatabaseHelper get() {
		if (null == instance) {
			instance = new DatabaseHelper();
		}
		return instance;
	}

	public void dropTable(final String tableName) {
		if (this.getExistingTables(Boolean.TRUE).contains(tableName)) {
			SQL.update(SQLs.GENERIC_DROP_TABLE.replace("__tableName__", tableName));
		}
	}

	public void createSequence(final String sequenceName, final Integer start) {
		if (!this.isSequenceExists(sequenceName)) {
			SQL.update(SQLs.GENERIC_CREATE_SEQUENCE.replace("__seqName__", sequenceName).replace("__seqStart__",
					start.toString()));
		} else {
			LOG.info("Sequence : " + sequenceName + " already exists, not created");
		}
	}

	public void createSequence(final String sequenceName) {
		this.createSequence(sequenceName, 1);
	}

	public void dropSequence(final String sequenceName) {
		if (this.isSequenceExists(sequenceName)) {
			SQL.update(SQLs.GENERIC_DROP_SEQUENCE.replace("__seqName__", sequenceName));
		}
	}

	public Boolean isSequenceExists(final String sequenceName) {
		Boolean sequenceExists = Boolean.TRUE;

		final Object sequences[][] = SQL.select(SQLs.GENERIC_SEQUENCE_EXISTS.replace("__seqName__", sequenceName));

		if (null == sequences || sequences.length == 0 || sequences[0].length == 0 || null == sequences[0][0]) {
			sequenceExists = Boolean.FALSE;
		}
		return sequenceExists;
	}

	public String getBlobType() {
		final String stylClassName = SQL.getSqlStyle().getClass().getName();
		String blobType = "BLOB";
		if ("org.eclipse.scout.rt.server.jdbc.postgresql.PostgreSqlStyle".equals(stylClassName)) {
			blobType = "bytea";
		}
		return blobType;
	}

	public Set<String> retrieveExistingTables() {
		final StringArrayHolder tables = new StringArrayHolder();
		final String stylClassName = SQL.getSqlStyle().getClass().getName();
		if ("org.eclipse.scout.rt.server.jdbc.postgresql.PostgreSqlStyle".equals(stylClassName)) {
			SQL.selectInto(SQLs.SELECT_TABLE_NAMES_POSTGRESQL, new NVPair("result", tables));
		} else if ("org.eclipse.scout.rt.server.jdbc.derby.DerbySqlStyle".equals(stylClassName)) {
			SQL.selectInto(SQLs.SELECT_TABLE_NAMES_DERBY, new NVPair("result", tables));
		} else {
			LOG.error("Unimplemented getExistingTables for SqlStyle : " + stylClassName);
		}

		return CollectionUtility.hashSet(tables.getValue());
	}

	/**
	 * By default existing tables are cached, use
	 * {@link #getExistingTables(Boolean)} if you need cache refresh
	 *
	 * @return
	 */
	public Set<String> getExistingTables() {
		return this.getExistingTables(Boolean.FALSE);
	}

	public Boolean existTable(final String tableName) {
		return this.getExistingTables().contains(tableName);
	}

	/**
	 * get exiting tables in DataBase.
	 *
	 * @param refreshCache
	 *            : if FALSE and a cache exist, cached tables are returned.
	 *
	 * @return
	 */
	public Set<String> getExistingTables(final Boolean refreshCache) {
		if (null == this.existingTables || refreshCache) {
			this.existingTables = this.retrieveExistingTables();
		}
		return this.existingTables;
	}

	public void resetExistingTablesCache() {
		this.existingTables = null;
	}

}
