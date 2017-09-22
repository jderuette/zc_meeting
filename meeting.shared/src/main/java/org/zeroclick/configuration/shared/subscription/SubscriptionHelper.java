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
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.event.IEventService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class SubscriptionHelper {

	public static final int LEVEL_SUB_FREE = 10;
	public static final int LEVEL_SUB_PRO = 20;
	public static final int LEVEL_SUB_BUSINESS = 30;

	public int getLevelForCurrentUser() {
		return this.collectUserRequirement().getUserRequiredLevel();
	}

	protected Integer getMaxAllowedEvent() {
		final IAppParamsService paramService = BEANS.get(IAppParamsService.class);
		return Integer.valueOf(paramService.getValue("subFreeEventLimit"));
	}

	public SubscriptionHelperData canCreateEvent() {
		return this.collectUserRequirement();
	}

	private SubscriptionHelperData collectUserRequirement() {
		final IEventService eventService = BEANS.get(IEventService.class);
		Integer requiredLevel = LEVEL_SUB_FREE;
		int nbEventWaiting = 0;
		final Map<Long, Integer> nbEventPendingByUsers = eventService.getNbEventsByUser("ASKED");
		if (null != nbEventPendingByUsers && nbEventPendingByUsers.size() > 0) {
			final Iterator<Integer> itNbEvents = nbEventPendingByUsers.values().iterator();
			while (itNbEvents.hasNext()) {
				nbEventWaiting += itNbEvents.next();
			}
		}

		if (nbEventWaiting >= this.getMaxAllowedEvent()) {
			requiredLevel = LEVEL_SUB_PRO;
		}

		return new SubscriptionHelperData(requiredLevel, ACCESS.getLevel(new CreateEventPermission()), nbEventWaiting,
				this.getMaxAllowedEvent());
	}

	public class SubscriptionHelperData {
		private int userRequiredLevel;
		private int userCurrentLevel;
		private Integer userNbAskedEvent;
		private Integer subscriptionAllowedEvent;
		private final Boolean accessAllowed;
		private final String messageKey;

		public SubscriptionHelperData(final int userRequiredLevel, final int userCurrentLevel,
				final Integer userNbAskedEvent, final Integer subscriptionAllowedEvent) {
			super();
			this.userRequiredLevel = userRequiredLevel;
			this.userCurrentLevel = userCurrentLevel;
			this.userNbAskedEvent = userNbAskedEvent;
			this.subscriptionAllowedEvent = subscriptionAllowedEvent;
			this.accessAllowed = userCurrentLevel >= userRequiredLevel;
			this.messageKey = "zc.subscription.notAllowed";
		}

		public String getLogMessage() {
			final StringBuilder builder = new StringBuilder(64);
			builder.append(this.messageKey).append(" User level : ").append(this.userCurrentLevel)
					.append(" required : ").append(this.userRequiredLevel).append(" nbEvent : ")
					.append(this.userNbAskedEvent).append('/').append(this.subscriptionAllowedEvent);

			return builder.toString();
		}

		public String getUserMessage() {
			return TEXTS.get(this.messageKey, String.valueOf(this.userCurrentLevel),
					String.valueOf(this.userRequiredLevel), String.valueOf(this.userNbAskedEvent),
					String.valueOf(this.subscriptionAllowedEvent));
		}

		public int getUserRequiredLevel() {
			return this.userRequiredLevel;
		}

		public void setUserRequiredLevel(final int userLevel) {
			this.userRequiredLevel = userLevel;
		}

		public int getUserCurrentLevel() {
			return this.userCurrentLevel;
		}

		public void setUserCurrentLevel(final int userCurrentLevel) {
			this.userCurrentLevel = userCurrentLevel;
		}

		public Integer getUserNbAskedEvent() {
			return this.userNbAskedEvent;
		}

		public void setUserNbAskedEvent(final Integer userNbAskedEvent) {
			this.userNbAskedEvent = userNbAskedEvent;
		}

		public Integer getSubscriptionAllowedEvent() {
			return this.subscriptionAllowedEvent;
		}

		public void setSubscriptionAllowedEvent(final Integer subscriptionAllowedEvent) {
			this.subscriptionAllowedEvent = subscriptionAllowedEvent;
		}

		public Boolean isAccessAllowed() {
			return this.accessAllowed;
		}
	}
}
