package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.role.PermissionFormData;
import org.zeroclick.configuration.shared.role.PermissionTablePageData;

@TunnelToServer
public interface IAppPermissionService extends IService {

	PermissionFormData prepareCreate(PermissionFormData formData);

	PermissionFormData create(PermissionFormData formData);

	PermissionFormData load(PermissionFormData formData);

	PermissionFormData store(PermissionFormData formData);

	PermissionTablePageData getPermissionTableData(SearchFilter filter);

	PermissionTablePageData getpermissionsByRole(Integer roleId);

	Object[][] getPermissionsByUser(Long userId);

	Object[][] getPermissionsByUser(String userLoginOrEmail);
}
