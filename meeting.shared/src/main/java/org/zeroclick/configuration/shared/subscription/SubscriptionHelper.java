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
package org.zeroclick.configuration.shared.subscription;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.meeting.shared.event.IEventService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class SubscriptionHelper {

	public static final int LEVEL_SUB_PERSO = 10;
	public static final int LEVEL_SUB_PRO = 20;
	public static final int LEVEL_SUB_BUSINESS = 30;

	public Integer getLevelForCurrentUser() {
		final IEventService eventService = BEANS.get(IEventService.class);
		Integer requiredLevel = LEVEL_SUB_PERSO;
		int nbEventWaiting = 0;
		final Map<Long, Integer> nbEventPendingByUsers = eventService.getUsersWithPendingMeeting();
		if (null != nbEventPendingByUsers && nbEventPendingByUsers.size() > 0) {
			final Iterator<Integer> itNbEvents = nbEventPendingByUsers.values().iterator();
			while (itNbEvents.hasNext()) {
				nbEventWaiting += itNbEvents.next();
			}
		}

		if (nbEventWaiting > 10) {
			requiredLevel = LEVEL_SUB_PRO;
		}

		return requiredLevel;
	}

}
