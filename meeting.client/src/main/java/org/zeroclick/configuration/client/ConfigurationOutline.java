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
package org.oneclick.configuration.client;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.configuration.client.api.ApiTablePage;
import org.oneclick.meeting.shared.Icons;
import org.oneclick.meeting.shared.calendar.ReadApiPermission;

/**
 * @author djer
 *
 */
@Order(2000)
public class ConfigurationOutline extends AbstractOutline {

	@Override
	protected void initConfig() {
		super.initConfig();
		final Boolean isVisible = ACCESS.getLevel(new ReadApiPermission((Long) null)) > ReadApiPermission.LEVEL_OWN;
		this.setEnabledGranted(isVisible);
		this.setVisibleGranted(isVisible);
	}

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		// super.execCreateChildPages(pageList);
		final ApiTablePage apiTablePage = new ApiTablePage();

		this.setVisibleGranted(ACCESS.getLevel(new ReadApiPermission((Long) null)) >= ReadApiPermission.LEVEL_OWN);
		pageList.add(apiTablePage);
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("Configurations");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Pencil;
	}

}
