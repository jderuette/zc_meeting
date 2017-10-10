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

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
@ApplicationScoped
public interface IDataPatcher {

	/**
	 * A synthetic human readable description of this data Patch
	 *
	 * @return
	 */
	String getDescription();

	/**
	 * Execute the patch actions
	 */
	void apply();

	/**
	 * remove this patch
	 */
	public void remove();

	/**
	 * The (data) version this patch will migrate the dataBase to.
	 *
	 * @return
	 */
	public Version getVersion();

	/**
	 * Is this data patch required (should be executed automatically as soon as
	 * possible)
	 *
	 * @return
	 */
	Boolean isRequired();

	/**
	 * Dose this match match the required "sourceCode" version to migrate ?
	 *
	 * @return
	 */
	Boolean canMigrate();

}
