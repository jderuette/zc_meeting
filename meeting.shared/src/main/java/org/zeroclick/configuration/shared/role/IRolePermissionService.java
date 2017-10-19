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
package org.zeroclick.configuration.shared.role;

import java.util.List;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;

/**
 * @author djer
 *
 */
@TunnelToServer
public interface IRolePermissionService extends IService {

	AssignToRoleFormData create(AssignToRoleFormData formData);

	void remove(Long roleId, List<String> permissions);

	/**
	 * Remove ALL permissions associate with this role
	 * 
	 * @param roleId
	 */
	void remove(Long roleId);

	/**
	 * To help in data migration (when adding startDate as contributing PK)
	 */
	void setDefaultStartDateToExistingUserRole();

}
