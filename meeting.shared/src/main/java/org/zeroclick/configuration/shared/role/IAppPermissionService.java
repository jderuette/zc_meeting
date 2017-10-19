package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface IAppPermissionService extends IService {

	PermissionFormData prepareCreate(PermissionFormData formData);

	PermissionFormData create(PermissionFormData formData);

	PermissionFormData load(PermissionFormData formData);

	PermissionFormData store(PermissionFormData formData);

	PermissionTablePageData getPermissionTableData(SearchFilter filter);

	/**
	 * Retrieve permission granted by this role.
	 * 
	 * @param roleId
	 * @return
	 */
	PermissionTablePageData getpermissionsByRole(Long roleId);

	/**
	 * Gather all exiting permission from source code. By default filter
	 * permission containing ".zeroclick." in there (package) name.
	 *
	 * @return
	 */
	Object[][] getpermissions();

	/**
	 * Retrieve permissions granted for this user.
	 *
	 * @param userId
	 * @return
	 */
	Object[][] getPermissionsByUser(Long userId);

	/**
	 * Retrieve permissions granted to this user (by login or email).
	 *
	 * @param userLoginOrEmail
	 * @return
	 */
	Object[][] getPermissionsByUser(String userLoginOrEmail);
}
