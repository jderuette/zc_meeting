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

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.config.AbstractVersionConfigProperty;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public abstract class AbstractDataMigrate implements IDataMigrate {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDataMigrate.class);

	private String description;

	protected abstract void execute();

	protected Version getSourceCodeVersion() {
		return CONFIG.getPropertyValue(SourceCodeDataVersionProperty.class);
	}

	protected Version getDataVersion() {
		return CONFIG.getPropertyValue(DataVersionProperty.class);
	}

	@Override
	public void apply() {
		LOG.info("Launching migration");
		this.execute();
		LOG.info("Launching migration ENDS");
	}

	@Override
	/**
	 * ByDefault return TRUE if major/minor sourceCode version is strictly
	 * higher than data version<br />
	 * Follow the semVer standard (see http://semver.org/)<<br />
	 * General semVer format : Major.Minor.Patch-pre-release+Build metadata :
	 * 1.0.0-alpha.1+001<br />
	 * Example :<br />
	 * 1.0.0 vs 1.0.5 => FALSE<br />
	 * 1.0.0 vs 1.0.0 => FALSE<br />
	 * 1.0.1 vs 1.1.0 => TRUE<br />
	 * 1.5.3 vs 1.5.6 => FALSE<br />
	 * 1.5.6 vs 1.5.6-build48+bidule => false
	 *
	 */
	public Boolean isRequired() {
		return this.getSourceCodeVersion().getMajorVersion() >= this.getDataVersion().getMajorVersion()
				&& this.getSourceCodeVersion().getMinorVersion() >= this.getDataVersion().getMinorVersion();
	}

	@Override
	/**
	 * ByDefault return TRUE if the datasourceVersion is strictly higher than
	 * the dataVersion.
	 */
	public Boolean canMigrate() {
		return this.getSourceCodeVersion().greaterThan(this.getDataVersion());
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	protected void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * the data version required by sourceCode
	 */
	public static class SourceCodeDataVersionProperty extends AbstractVersionConfigProperty {

		@Override
		public String getKey() {
			return "zeroclick.version.sourcecode";
		}

		@Override
		protected Version getDefaultValue() {
			// TODO Djer extract the project MAVEN version
			return super.getDefaultValue();
		}
	}

	/**
	 * the data(base) version currently used
	 */
	public static class DataVersionProperty extends AbstractVersionConfigProperty {

		@Override
		public String getKey() {
			return "zeroclick.version.data";
		}
	}

}
