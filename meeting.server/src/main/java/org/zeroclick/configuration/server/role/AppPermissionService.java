package org.zeroclick.configuration.server.role;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.configuration.shared.role.CreatePermissionPermission;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.PermissionFormData;
import org.zeroclick.configuration.shared.role.PermissionTablePageData;
import org.zeroclick.configuration.shared.role.ReadPermissionPermission;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.UpdatePermissionPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateSubscription;

public class AppPermissionService extends AbstractCommonService implements IAppPermissionService {

	private static final Logger LOG = LoggerFactory.getLogger(AppPermissionService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public PermissionFormData prepareCreate(final PermissionFormData formData) {
		if (!ACCESS.check(new CreatePermissionPermission())) {
			super.throwAuthorizationFailed();
		}
		// TODO Djer13 something to prepare ?
		return formData;
	}

	@Override
	public PermissionFormData create(final PermissionFormData formData) {
		if (!ACCESS.check(new CreatePermissionPermission())) {
			super.throwAuthorizationFailed();
		}
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT, formData);
		BEANS.get(IAccessControlService.class).clearCache();
		return formData;
	}

	@Override
	public PermissionFormData load(final PermissionFormData formData) {
		if (!ACCESS.check(new ReadPermissionPermission())) {
			super.throwAuthorizationFailed();
		}
		SQL.selectInto(SQLs.ROLE_SELECT, formData);
		return formData;
	}

	@Override
	public PermissionFormData store(final PermissionFormData formData) {
		if (!ACCESS.check(new UpdatePermissionPermission())) {
			super.throwAuthorizationFailed();
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
	public PermissionTablePageData getpermissionsByRole(final Long roleId) {
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

		if (DatabaseHelper.get().isColumnExists(PatchCreateSubscription.PATCHED_TABLE_ROLE,
				PatchCreateSubscription.ADDED_ROLE_COLUMN)) {
			sql.append(SQLs.USER_PERMISSIONS_SELECT_ACTIVE_ROLE).append(SQLs.USER_PERMISSIONS_SELECT_FILTER_USER_ID)
					.append(SQLs.USER_PERMISSIONS_SELECT_GROUP_BY);
		} else {
			// before subscription patch, so all role are "always" available (no
			// "start date")
			sql.append(SQLs.USER_PERMISSIONS_SELECT_ACTIVE_ROLE_BEFORE_SUB_PATCH)
					.append(SQLs.USER_PERMISSIONS_SELECT_FILTER_USER_ID).append(SQLs.USER_PERMISSIONS_SELECT_GROUP_BY);
		}

		final Object[][] standardAndActiveSubscription = SQL.select(sql.toString(), new NVPair("userId", userId));

		// Object[][] activeSubscriptionpermisisons =
		// SQL.select(SQLs.USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_PERMISSIONS ,
		// new NVPair("userId", userId));

		return standardAndActiveSubscription;

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

	@Override
	public Object[][] getpermissions() {
		return this.getPermission(".zeroclick.");
	}

	private Object[][] getPermission(final String filter) {
		final ArrayList<String> rows = new ArrayList<>(30);
		final Set<Class<? extends Permission>> permissions = BEANS.get(IPermissionService.class)
				.getAllPermissionClasses();

		for (final Class<? extends Permission> permisison : permissions) {
			if (permisison.getCanonicalName().contains(filter)) {
				rows.add(permisison.getCanonicalName());
			}
		}

		Collections.sort(rows);
		final Object[][] data = new Object[rows.size()][1];
		for (int i = 0; i < rows.size(); i++) {
			data[i][0] = rows.get(i);
		}
		return data;
	}
}
