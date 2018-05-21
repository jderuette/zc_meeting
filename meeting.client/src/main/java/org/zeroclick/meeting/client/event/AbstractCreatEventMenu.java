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
package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.configuration.client.user.ValidateCpsForm;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.meeting.client.api.ApiHelper;
import org.zeroclick.meeting.client.api.ApiHelperFactory;
import org.zeroclick.meeting.service.CalendarService;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;
import org.zeroclick.ui.action.menu.AbstractNewMenu;

/**
 * @author djer
 *
 */
public abstract class AbstractCreatEventMenu extends AbstractNewMenu {
	@Override
	protected String getConfiguredText() {
		return TEXTS.get("zc.meeting.addEvent");
	}

	@Override
	protected void execInitAction() {
		this.setVisibleGranted(ACCESS.getLevel(new CreateEventPermission()) >= CreateEventPermission.LEVEL_SUB_FREE);
	}

	@Override
	protected boolean getConfiguredEnabled() {
		return Boolean.TRUE;
	}

	protected boolean isUserCalendarConfigured() {
		return BEANS.get(CalendarService.class).isCalendarConfigured();
	}

	protected boolean isUserAddCalendarConfigured() {
		return BEANS.get(CalendarService.class).isAddCalendarConfigured();
	}

	protected Long getCurrentUserId() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return acsHelper.getZeroClickUserIdOfCurrentSubject();
	}

	@Override
	protected void execAction() {
		final ApiHelper apiHelper = ApiHelperFactory.getCommonApiHelper();
		if (!this.isUserCalendarConfigured()) {
			apiHelper.askToAddApi(this.getCurrentUserId());
		} else if (!this.isUserAddCalendarConfigured()) {
			// External calendar added, but no calendar chosen to add
			// events
			apiHelper.askToChooseCalendarToAddEvent(this.getCurrentUserId());
		}

		if (!this.isUserAddCalendarConfigured()) {
			// User won't configure required Data
			return; // earlyBreak
		}

		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);
		final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();

		if (subscriptionData.isAccessAllowed()) {
			this.loadEventForm();
		} else {
			final int userDecision = MessageBoxes.createYesNo()
					.withHeader(TEXTS.get("zc.subscription.notAllowed.title"))
					.withBody(subscriptionData.getUserMessage())
					.withYesButtonText(TEXTS.get("zc.subscription.notAllowed.yesButton"))
					.withIconId(Icons.ExclamationMark).withSeverity(IStatus.WARNING).show();

			if (userDecision == IMessageBox.YES_OPTION) {
				final ValidateCpsForm validateCpsForm = new ValidateCpsForm();
				validateCpsForm.getUserIdField().setValue(this.getCurrentUserId());
				validateCpsForm.setModal(Boolean.TRUE);
				validateCpsForm.startNew();
				validateCpsForm.waitFor();
			}
			// if user subscribe to subscription witch give him access
			final SubscriptionHelperData subscriptionAfterData = subHelper.canCreateEvent();
			if (subscriptionAfterData.isAccessAllowed()) {
				this.loadEventForm();
			}
		}
	}

	private void loadEventForm() {
		final EventForm form = new EventForm();
		// form.setEnabledGranted(subscriptionData.isAccessAllowed());
		form.setVisibleGranted(ACCESS.getLevel(new CreateEventPermission()) >= CreateEventPermission.LEVEL_SUB_FREE);
		form.startNew();
	}
}