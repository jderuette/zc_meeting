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
package org.oneclick.configuration.server.role;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.configuration.shared.role.AssignToRoleFormData;
import org.oneclick.configuration.shared.role.CreateAssignToRolePermission;
import org.oneclick.configuration.shared.role.IRolePermissionService;
import org.oneclick.meeting.server.security.ServerAccessControlService;
import org.oneclick.meeting.server.sql.SQLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
public class RolePermissionService implements IRolePermissionService {

	private static final Logger LOG = LoggerFactory.getLogger(RolePermissionService.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.oneclick.configuration.shared.role.IRolePermissionService#create(org.
	 * oneclick.configuration.shared.role.RoleFormData)
	 */
	@Override
	public AssignToRoleFormData create(final AssignToRoleFormData formData) {
		if (!ACCESS.check(new CreateAssignToRolePermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		final Long roleId = formData.getRoleId().getValue();
		LOG.info("Adding new permission(s) to role :" + roleId + " : " + formData.getPermission());
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT, formData);

		this.clearCacheOfUsersWithRole(roleId);
		return formData;
	}

	@Override
	public void remove(final Integer roleId, final List<String> permissions) {
		LOG.info("Removing permission(s) to role :" + roleId + " : " + permissions);
		SQL.insert(SQLs.ROLE_PERMISSION_DELETE, new NVPair("roleId", roleId), new NVPair("permissions", permissions));
		this.clearCacheOfUsersWithRole(new Long(roleId));
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
			userIdList.add((String) users[i][0]);
		}

		BEANS.get(ServerAccessControlService.class).clearCacheOfUsersIds(userIdList);
	}

	private Object[][] getUsersByRole(final Long roleId) {

		final StringBuilder sql = new StringBuilder();

		sql.append(SQLs.USER_ROLE_SELECT).append(SQLs.USER_ROLE_SELECT_FILTER_ROLE);
		return SQL.select(sql.toString(), new NVPair("roleId", roleId));
	}
}
