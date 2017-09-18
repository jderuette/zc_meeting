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
package org.zeroclick.comon.user;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class AppUserHelper {

	private static final Logger LOG = LoggerFactory.getLogger(AppUserHelper.class);

	protected String getUserTimeZone(final Long userId) {
		final IUserService userService = BEANS.get(IUserService.class);
		String userZoneId = userService.getUserTimeZone(userId);
		if (null == userZoneId) {
			userZoneId = "UTC";
		}
		return userZoneId;
	}

	public ZoneId getUserZoneId(final Long userId) {
		return ZoneId.of(this.getUserTimeZone(userId));
	}

	public ZoneId getCurrentUserTimeZone() {
		final IUserService userService = BEANS.get(IUserService.class);
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		ZoneId userZoneId = ZoneId.of("UTC");
		if (null != userService) {
			userZoneId = this.getUserZoneId(acs.getZeroClickUserIdOfCurrentSubject());
		} else {
			LOG.warn("No timeZone configured or user : " + acs.getZeroClickUserIdOfCurrentSubject());
		}
		return userZoneId;
	}

	public ZonedDateTime getUserNow(final Long userId) {
		return ZonedDateTime.now(Clock.system(this.getUserZoneId(userId)));
	}

	public Long getCurrentUserId() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUser = acs.getZeroClickUserIdOfCurrentSubject();
		return currentUser;
	}
}
