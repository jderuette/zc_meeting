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
package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

/**
 * @author djer
 *
 */
public abstract class AbstractHierarchyCalendarConfigurationPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 2494061557452320583L;

	public static final int LEVEL_OWN = 10;

	private Long oAuthCredentialId;

	public AbstractHierarchyCalendarConfigurationPermission(final Integer level) {
		super(getPermissionName() + ".*", level);
	}

	public AbstractHierarchyCalendarConfigurationPermission(final Long oAuthCredentialId) {
		super(getPermissionName() + "." + oAuthCredentialId, LEVEL_UNDEFINED);
		this.oAuthCredentialId = oAuthCredentialId;
	}

	/**
	 * Must be overwrite by subClass
	 * 
	 * @return
	 */
	protected static String getPermissionName() {
		return null;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof AbstractHierarchyCalendarConfigurationPermission) {
			final Long oAuthCredentialId = ((AbstractHierarchyCalendarConfigurationPermission) other)
					.getoAuthCredentialId();
			if (BEANS.get(ICalendarConfigurationService.class).isOwn(oAuthCredentialId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getoAuthCredentialId() {
		return this.oAuthCredentialId;
	}

	public void setoAuthCredentialId(final Long oAuthCredentialId) {
		this.oAuthCredentialId = oAuthCredentialId;
	}

}
