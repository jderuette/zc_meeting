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
package org.oneclick.configuration.client.administration;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.configuration.client.role.PermissionTablePage;
import org.oneclick.configuration.client.role.RoleTablePage;
import org.oneclick.configuration.client.user.UserTablePage;
import org.oneclick.configuration.shared.role.ReadPermissionPermission;
import org.oneclick.configuration.shared.role.ReadRolePermission;
import org.oneclick.configuration.shared.user.ReadUserPermission;
import org.oneclick.meeting.shared.Icons;

/**
 * @author djer
 *
 */
@Order(10000)
public class AdministrationOutline extends AbstractOutline {

	@Override
	protected void initConfig() {
		super.initConfig();
		final Boolean isVisible = ACCESS.getLevel(new ReadUserPermission((Long) null)) > ReadUserPermission.LEVEL_OWN;
		this.setEnabledGranted(isVisible);
		this.setVisibleGranted(isVisible);
	}

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		super.execCreateChildPages(pageList);
		final RoleTablePage roleTablePage = new RoleTablePage();
		roleTablePage.setVisiblePermission(new ReadRolePermission());
		pageList.add(roleTablePage);

		final PermissionTablePage permissionTablePage = new PermissionTablePage();
		permissionTablePage.setVisiblePermission(new ReadPermissionPermission());
		pageList.add(permissionTablePage);

		final UserTablePage userTablePage = new UserTablePage();
		userTablePage.setVisibleGranted(
				ACCESS.getLevel(new ReadUserPermission((Long) null)) >= ReadUserPermission.LEVEL_OWN);
		pageList.add(userTablePage);
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.common.administration");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.ExclamationMark;
	}

}
