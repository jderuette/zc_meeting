package org.zeroclick.configuration.server.role;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.zeroclick.configuration.shared.role.CreatePermissionPermission;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.PermissionFormData;
import org.zeroclick.configuration.shared.role.PermissionTablePageData;
import org.zeroclick.configuration.shared.role.ReadPermissionPermission;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.UpdatePermissionPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.server.sql.SQLs;

public class AppPermissionService implements IAppPermissionService {

	@Override
	public PermissionFormData prepareCreate(final PermissionFormData formData) {
		if (!ACCESS.check(new CreatePermissionPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		// TODO Djer13 something to prepare ?
		return formData;
	}

	@Override
	public PermissionFormData create(final PermissionFormData formData) {
		if (!ACCESS.check(new CreatePermissionPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT, formData);
		BEANS.get(IAccessControlService.class).clearCache();
		return formData;
	}

	@Override
	public PermissionFormData load(final PermissionFormData formData) {
		if (!ACCESS.check(new ReadPermissionPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.selectInto(SQLs.ROLE_SELECT, formData);
		return formData;
	}

	@Override
	public PermissionFormData store(final PermissionFormData formData) {
		if (!ACCESS.check(new UpdatePermissionPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		throw new VetoException(TEXTS.get("zc.user.permissionStoreNotAllowed"));
		// return formData;
	}

	@Override
	public PermissionTablePageData getPermissionTableData(final SearchFilter filter) {
		final PermissionTablePageData pageData = new PermissionTablePageData();
		final String sql = SQLs.ROLE_PERMISSION_PAGE_SELECT + SQLs.ROLE_PERMISSION_PAGE_DATA_SELECT_INTO;
		SQL.selectInto(sql, new NVPair("rolePermission", pageData));
		return pageData;
	}

	@Override
	public PermissionTablePageData getpermissionsByRole(final Integer roleId) {
		final PermissionTablePageData pageData = new PermissionTablePageData();

		final RoleFormData roleFormData = new RoleFormData();
		roleFormData.setRoleId(roleId);

		final String sql = SQLs.ROLE_PERMISSION_PAGE_SELECT + SQLs.ROLE_PERMISSION_FILTER_ROLE
				+ SQLs.ROLE_PERMISSION_PAGE_DATA_SELECT_INTO;
		SQL.selectInto(sql, new NVPair("rolePermission", pageData), new NVPair("role", roleFormData));
		return pageData;
	}

	@Override
	public Object[][] getPermissionsByUser(final Long userId) {

		final StringBuilder sql = new StringBuilder();

		sql.append(SQLs.USER_PERMISSIONS_SELECT).append(SQLs.USER_PERMISSIONS_SELECT_FILTER_USER_ID)
				.append(SQLs.USER_PERMISSIONS_SELECT_GROUP_BY);
		return SQL.select(sql.toString(), new NVPair("userId", userId));
	}

	@Override
	public Object[][] getPermissionsByUser(final String userLoginOrEmail) {
		final IUserService userService = BEANS.get(IUserService.class);
		Object[][] permissions = null;

		final Long userid = userService.getUserId(userLoginOrEmail);

		if (null != userid) {
			permissions = this.getPermissionsByUser(userid);
		} else {
			permissions = new Object[0][0];
		}
		return permissions;
	}
}
