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
package org.zeroclick.comon.text;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class UserHelper {

	public Long getCurrentUserId() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return acsHelper.getZeroClickUserIdOfCurrentSubject();
	}

	public Subject getCurrentUserSubject() {
		return Subject.getSubject(AccessController.getContext());
	}

	public Boolean isCalendarAdmin() {
		final int currentUserCalendarConfigLevel = ACCESS
				.getLevel(new ReadCalendarConfigurationPermission((Long) null));
		return currentUserCalendarConfigLevel == ReadCalendarConfigurationPermission.LEVEL_ALL;
	}

	public Boolean isEventAdmin() {
		final int currentUserEventLevel = ACCESS.getLevel(new ReadEventPermission((Long) null));
		return currentUserEventLevel == ReadEventPermission.LEVEL_ALL;
	}

	public Boolean isEventUser() {
		final int currentUserEventLevel = ACCESS.getLevel(new ReadEventPermission((Long) null));
		return currentUserEventLevel >= ReadEventPermission.LEVEL_OWN;
	}

	public Boolean isSlotUser() {
		final int currentUserEventLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		return currentUserEventLevel == ReadSlotPermission.LEVEL_ALL;
	}
}
