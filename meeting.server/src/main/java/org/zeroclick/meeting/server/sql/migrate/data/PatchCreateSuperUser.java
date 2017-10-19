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

import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.IRoleTypeLookupService;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.server.security.ServerAccessControlService;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchCreateSuperUser extends AbstractDataPatcher {

	public static final String ROLE_ID_SEQ = "ROLE_ID_SEQ";

	private static final Logger LOG = LoggerFactory.getLogger(PatchCreateSuperUser.class);

	private Long superUserId;
	private Long superUserRoleId;

	public PatchCreateSuperUser() {
		this.setDescription("Create super User");
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
			LOG.info("Create super User will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}
		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Create super User upgrading data strcuture");
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
		LOG.info("Create super User upgraing default data");
		final String firstSuperUserPrincipal = this.getSuperUserPrincipal();

		if (null != firstSuperUserPrincipal && !"".equals(firstSuperUserPrincipal)) {
			final IUserService userService = BEANS.get(IUserService.class);
			if (userService.isLoginAlreadyUsed(firstSuperUserPrincipal)) {
				LOG.info("A (super ?) user already exists with login : " + firstSuperUserPrincipal);
				return; // early break
			}

			// create super role, should only apply to this create User
			final RoleFormData superUserRoleFormData = new RoleFormData();
			// get the RoleId
			this.superUserRoleId = DatabaseHelper.get().getNextVal(ROLE_ID_SEQ);

			superUserRoleFormData.setRoleId(this.superUserRoleId);
			superUserRoleFormData.getRoleName().setValue(SUPER_USER_ROLE_NAME);
			superUserRoleFormData.getType().setValue(IRoleTypeLookupService.TYPE_TECH);

			SQL.insert(SQLs.ROLE_INSERT, superUserRoleFormData);
			SQL.update(SQLs.ROLE_UPDATE, superUserRoleFormData);

			// add "ALL" permission to superUser Role
			this.addAllPermissions(this.superUserRoleId);

			this.superUserId = DatabaseHelper.get().getNextVal("USER_ID_SEQ");
			final UserFormData superUserFormData = new UserFormData();
			superUserFormData.getUserId().setValue(this.superUserId);
			superUserFormData.getLogin().setValue(firstSuperUserPrincipal);
			superUserFormData.getTimeZone()
					.setValue(ZoneOffset.UTC.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH));
			// this user has NO PASSWORD to avoid login with this **very**
			// specific user
			superUserFormData.getRolesBox().setValue(new HashSet<>());
			superUserFormData.getRolesBox().getValue().add(Long.valueOf(this.superUserRoleId));
			SQL.insert(SQLs.USER_INSERT, superUserFormData);
			SQL.update(SQLs.USER_UPDATE, superUserFormData);

			// remove existing "superAdmin" role for super User if already
			// granted
			SQL.update(SQLs.USER_ROLE_REMOVE, new NVPair("userId", superUserFormData.getUserId().getValue()),
					new NVPair("rolesBox", superUserFormData.getRolesBox().getValue()));
			// add the superUser role
			SQL.update(SQLs.USER_ROLE_INSERT, new NVPair("userId", superUserFormData.getUserId().getValue()),
					new NVPair("rolesBox", superUserFormData.getRolesBox().getValue()));

			// Clear user Cache
			BEANS.get(ServerAccessControlService.class)
					.clearCacheOfUsersIds(CollectionUtility.arrayList(firstSuperUserPrincipal));

			// clear permission cache
			BEANS.get(IAccessControlService.class).clearCache();
			LOG.info("SuperUser for data migration created");
		} else {
			LOG.error(
					"Cannot create app user for super user, because the superUser has no principal, see SuperUserSubjectProperty");
		}
	}

	@Override
	public void undo() {
		LOG.info("Create super User downgrading data strcuture");

		this.getDatabaseHelper().dropSequence(ROLE_ID_SEQ);

		// delete role
		final IRoleService roleService = BEANS.get(IRoleService.class);
		final RoleFormData roleFormData = new RoleFormData();
		roleFormData.getRoleName().setValue(SUPER_USER_ROLE_NAME);
		try {
			roleService.delete(roleFormData);
		} catch (final VetoException ve) {
			LOG.warn("Cannot delete role : " + SUPER_USER_ROLE_NAME, ve);
		}

		// delete user
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userFormData = new UserFormData();
		userFormData.getLogin().setValue(this.getSuperUserPrincipal());

		try {
			userService.delete(userFormData);
		} catch (final VetoException ve) {
			LOG.warn("Cannot delete user : " + this.getSuperUserPrincipal(), ve);
		}
	}
}
