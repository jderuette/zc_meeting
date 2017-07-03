package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.RoleTablePageData;

@TunnelToServer
public interface IRoleService extends IService {

	RoleTablePageData getRoleTableData(SearchFilter filter);

	RoleFormData load(RoleFormData formData);

	RoleFormData store(RoleFormData formData);

	RoleFormData prepareCreate(RoleFormData formData);

	RoleFormData create(RoleFormData formData);

}
