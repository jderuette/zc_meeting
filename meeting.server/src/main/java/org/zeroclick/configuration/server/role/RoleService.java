package org.zeroclick.configuration.server.role;

import java.util.UUID;

import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.configuration.shared.role.CreateRolePermission;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.ReadRolePermission;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.RoleTablePageData;
import org.zeroclick.configuration.shared.role.UpdateRolePermission;
import org.zeroclick.meeting.server.sql.SQLs;

public class RoleService extends CommonService implements IRoleService {

	private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

	@Override
	public RoleTablePageData getRoleTableData(final SearchFilter filter) {
		final RoleTablePageData pageData = new RoleTablePageData();

		final String sql = SQLs.ROLE_PAGE_SELECT + SQLs.ROLE_PAGE_DATA_SELECT_INTO;
		SQL.selectInto(sql, new NVPair("role", pageData));
		return pageData;
	}

	@Override
	public RoleFormData prepareCreate(final RoleFormData formData) {
		// add a unique Role id if necessary
		if (null == formData.getRoleId()) {
			formData.setRoleId(UUID.randomUUID().hashCode());
		}
		return this.store(formData);
	}

	@Override
	public RoleFormData create(final RoleFormData formData) {
		if (!ACCESS.check(new CreateRolePermission())) {
			super.throwAuthorizationFailed();
		}
		// add a unique Role id if necessary
		if (null == formData.getRoleId()) {
			formData.setRoleId(UUID.randomUUID().hashCode());
		}
		SQL.insert(SQLs.ROLE_INSERT, formData);
		return this.store(formData);
	}

	@Override
	public RoleFormData store(final RoleFormData formData) {
		if (!ACCESS.check(new UpdateRolePermission())) {
			super.throwAuthorizationFailed();
		}
		LOG.info("Updating Role with : " + formData.getRoleId() + "(new : " + formData.getRoleName() + ")");
		SQL.update(SQLs.ROLE_UPDATE, formData);
		return formData;
	}

	@Override
	public RoleFormData load(final RoleFormData formData) {
		if (!ACCESS.check(new ReadRolePermission())) {
			super.throwAuthorizationFailed();
		}
		SQL.selectInto(SQLs.ROLE_SELECT + SQLs.ROLE_SELECT_INTO, formData);
		return formData;
	}

}
