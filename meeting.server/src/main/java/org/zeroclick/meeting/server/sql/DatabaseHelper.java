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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.StringArrayHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.server.role.RolePermissionService;

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
		this.dropTable(tableName, Boolean.TRUE);
	}

	public void dropTable(final String tableName, final Boolean forceRefreshTableCache) {
		if (this.getExistingTables(forceRefreshTableCache).contains(tableName)) {
			SQL.update(SQLs.GENERIC_DROP_TABLE.replace("__tableName__", tableName));
		} else {
			LOG.info("Table : " + tableName + " already exists (tested with force refresh ? " + forceRefreshTableCache
					+ "), not created");
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
		} else {
			LOG.warn("Sequence : " + sequenceName + " dosen't exists, no DROP required");
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
		return this.getExistingTables().contains(tableName.toUpperCase());
	}

	public Boolean existTable(final String tableName, final Boolean refreshCache) {
		return this.getExistingTables(refreshCache).contains(tableName.toUpperCase());
	}

	/**
	 * Check if column "column" exists in the table/relation "table"
	 *
	 * @param table
	 * @param column
	 * @return
	 */
	public boolean isColumnExists(final String table, final String column) {
		Boolean columnExists = Boolean.TRUE;
		final Object[][] columnName = SQL.select(
				SQLs.SELECT_COLUMN_EXISTS_POSTGRESQL.replace("__table__", table).replaceAll("__column__", column));
		if (null == columnName || columnName.length == 0 || columnName[0].length == 0 || null == columnName[0][0]) {
			columnExists = Boolean.FALSE;
		}
		return columnExists;
	}

	/**
	 * remove column "column" from table "table" if exists
	 *
	 * @param table
	 * @param column
	 */
	public void removeColumn(final String table, final String column) {
		if (this.isColumnExists(table, column)) {
			SQLs.GENERIC_REMOVE_COLUMN_POSTGRESQL.replace("__table__", table).replaceAll("__column__", column);
		} else {
			LOG.warn("Cannot remove column : " + column + " on table : " + table
					+ " because this column dosen't exists !");
		}
	}

	public Long getNextVal(final String sequenceName) {
		return SQL.getSequenceNextval(sequenceName);
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

	public void addAdminPermission(final String permissionName) {
		this.addPermissionToRole(1L, permissionName, 100);
	}

	public void addAdminPermission(final String permissionName, final Integer level) {
		this.addPermissionToRole(1L, permissionName, level);
	}

	public void addStandardUserPermission(final String permissionName) {
		this.addPermissionToRole(2L, permissionName, 50);
	}

	public void addStandardUserPermission(final String permissionName, final Integer level) {
		this.addPermissionToRole(2L, permissionName, level);
	}

	public void addSubFreePermission(final String permissionName, final Integer level) {
		this.addPermissionToRole(3L, permissionName, level);
	}

	public void addSubProPermission(final String permissionName, final Integer level) {
		this.addPermissionToRole(4L, permissionName, level);
	}

	public void addSubBusinessPermission(final String permissionName, final Integer level) {
		this.addPermissionToRole(5L, permissionName, level);
	}

	public void removeAdminPermission(final String permissionName) {
		this.removePermissionToRole(1L, permissionName);
	}

	public void removeStandardUserPermission(final String permissionName) {
		this.removePermissionToRole(2L, permissionName);
	}

	public void removeSubFreePermission(final String permissionName) {
		this.removePermissionToRole(3L, permissionName);
	}

	public void removeSubProPermission(final String permissionName) {
		this.removePermissionToRole(4L, permissionName);
	}

	public void removeSubBusinessPermission(final String permissionName) {
		this.removePermissionToRole(5L, permissionName);
	}

	public void addPermissionToRole(final Long roleId, final String permissionName, final Integer level) {
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT_SAMPLE + SQLs.ROLE_GENERIC_VALUES_ADD
				.replaceAll("__roleId__", String.valueOf(roleId)).replaceAll("__permissionName__", permissionName)
				.replaceAll("__level__", String.valueOf(level)));
	}

	public void removePermissionToRole(final Long roleId, final String permissionName) {
		final RolePermissionService rolePermissionService = BEANS.get(RolePermissionService.class);
		final List<String> permissions = new ArrayList<>();
		permissions.add(permissionName);
		rolePermissionService.remove(roleId, permissions);
	}

	public void deletePrimaryKey(final String tableName) {
		final String constraintName = tableName + "_PK";
		this.deleteConstraints(tableName, constraintName);
	}

	public void deleteConstraints(final String tableName, final String constraintName) {
		if (this.existTable(tableName, Boolean.TRUE)) {
			SQL.insert(SQLs.GENERIC_DROP_CONSTRAINT.replaceAll("__table__", tableName).replaceAll("__constraintName__",
					constraintName));
		} else {
			LOG.warn("Can't drop constraints : " + constraintName + " beacause table " + tableName + " does not exist");
		}
	}
}
