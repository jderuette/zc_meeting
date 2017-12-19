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
package org.zeroclick.configuration.server.role;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.configuration.shared.role.AssignToRoleFormData;
import org.zeroclick.configuration.shared.role.CreateAssignToRolePermission;
import org.zeroclick.configuration.shared.role.IRolePermissionService;
import org.zeroclick.configuration.shared.role.UpdateAssignToRolePermission;
import org.zeroclick.meeting.server.security.ServerAccessControlService;
import org.zeroclick.meeting.server.sql.SQLs;

/**
 * @author djer
 *
 */
public class RolePermissionService extends AbstractCommonService implements IRolePermissionService {

	private static final Logger LOG = LoggerFactory.getLogger(RolePermissionService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.zeroclick.configuration.shared.role.IRolePermissionService#create(
	 * org. zeroclick.configuration.shared.role.RoleFormData)
	 */
	@Override
	public AssignToRoleFormData create(final AssignToRoleFormData formData) {
		if (!ACCESS.check(new CreateAssignToRolePermission())) {
			super.throwAuthorizationFailed();
		}
		final Long roleId = formData.getRoleId().getValue();
		LOG.info("Adding new permission(s) to role :" + roleId + " : " + formData.getPermission());
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT, formData);

		this.clearCacheOfUsersWithRole(roleId);
		return formData;
	}

	@Override
	public void remove(final Long roleId, final List<String> permissions) {
		if (!ACCESS.check(new UpdateAssignToRolePermission())) {
			super.throwAuthorizationFailed();
		}
		LOG.info("Removing permission(s) to role :" + roleId + " : " + permissions);
		SQL.insert(SQLs.ROLE_PERMISSION_DELETE, new NVPair("roleId", roleId), new NVPair("permissions", permissions));
		this.clearCacheOfUsersWithRole(roleId);
	}

	@Override
	public void remove(final Long roleId) {
		if (!ACCESS.check(new UpdateAssignToRolePermission())) {
			super.throwAuthorizationFailed();
		}
		LOG.info("Removing ALL permission(s) to role :" + roleId);
		SQL.insert(SQLs.ROLE_PERMISSION_DELETE_BY_ROLE, new NVPair("roleId", roleId));
		this.clearCacheOfUsersWithRole(roleId);
	}

	/**
	 * CLear the user cache for all user having this role
	 *
	 * @param RoleId
	 */
	private void clearCacheOfUsersWithRole(final Long roleId) {
		final Object[][] users = this.getUsersByRole(roleId);
		final List<String> userIdList = new ArrayList<>();
		for (int i = 0; i < users.length; i++) {
			userIdList.add(String.valueOf(users[i][0]));
		}

		BEANS.get(ServerAccessControlService.class).clearCacheOfUsersIds(userIdList);
	}

	private Object[][] getUsersByRole(final Long roleId) {

		final StringBuilder sql = new StringBuilder();

		sql.append(SQLs.USER_ROLE_SELECT).append(SQLs.USER_ROLE_SELECT_FILTER_ROLE_ID);
		return SQL.select(sql.toString(), new NVPair("roleId", roleId));
	}

	private Object[][] getAllUsersRole() {

		final StringBuilder sql = new StringBuilder();

		sql.append(SQLs.USER_ROLE_SELECT);
		return SQL.select(sql.toString());
	}

	@Override
	public void setDefaultStartDateToExistingUserRole() {
		LOG.info("Adding default start date to All existing User_Role");

		// Important : start_date MUST be unique, so avoid update in one
		// statement
		final Object[][] existingUserRole = this.getAllUsersRole();

		if (null != existingUserRole && existingUserRole.length > 0) {
			for (int row = 0; row < existingUserRole.length; row++) {
				final Object[] userRole = existingUserRole[row];
				SQL.update(SQLs.USER_ROLE_UPDATE_START_DATE_BEFORE_NEW_PK, new NVPair("startDate", new Date()),
						new NVPair("userId", userRole[0]), new NVPair("roleId", userRole[1]));
			}
		}
	}

}
