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
package org.zeroclick.meeting.server.sql.migrate;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class DatabaseMigrateService {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseMigrateService.class);

	public void checkMigration() {
		final List<IBean<IDataPatcher>> patchs = this.getPatchers();
		// apply each patch
		for (final IBean<IDataPatcher> beanPatch : patchs) {
			final IDataPatcher patch = beanPatch.getInstance();
			patch.apply();
		}
	}

	public void undoMigration() {
		final List<IBean<IDataPatcher>> patchs = this.getPatchers();
		// apply each patch
		for (final IBean<IDataPatcher> beanPatch : patchs) {
			final IDataPatcher patch = beanPatch.getInstance();
			patch.remove();
			// this.updateDataVersion(patch.getVersion());
		}
	}

	/**
	 * Retrieve all patchers and order then
	 *
	 * @return
	 */
	private final List<IBean<IDataPatcher>> getPatchers() {
		final List<IBean<IDataPatcher>> patchs = BEANS.getBeanManager().getBeans(IDataPatcher.class);
		// order patch versions
		patchs.sort((vers1, vers2) -> vers1.getInstance().getVersion().compareTo(vers2.getInstance().getVersion()));
		return patchs;
	}

}
