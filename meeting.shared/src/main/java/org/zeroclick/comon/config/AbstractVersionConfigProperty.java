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
package org.zeroclick.comon.config;

import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public abstract class AbstractVersionConfigProperty extends AbstractConfigProperty<Version> {

	@Override
	protected Version parse(final String value) {
		final Version semVer = Version.valueOf(value);
		return semVer;
	}

}
