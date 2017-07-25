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
		// getAll patcher
		final List<IBean<IDataMigrate>> patchs = BEANS.getBeanManager().getBeans(IDataMigrate.class);
		// order patch versions
		patchs.sort((vers1, vers2) -> vers1.getInstance().getVersion().compareTo(vers2.getInstance().getVersion()));
		// apply each patch
		for (final IBean<IDataMigrate> beanPatch : patchs) {
			final IDataMigrate patch = beanPatch.getInstance();
			LOG.info("Applying patch : " + patch.getDescription());
			patch.apply();
		}
	}
}
