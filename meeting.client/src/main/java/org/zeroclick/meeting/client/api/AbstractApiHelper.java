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
package org.zeroclick.meeting.client.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.meeting.client.api.event.EventHelper;
import org.zeroclick.meeting.service.CalendarAviability;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

/**
 *
 * @author djer
 *
 * @param <E>
 *            type of credential
 * @param <F>
 *            type of Calendar service
 */
public abstract class AbstractApiHelper<E, F> implements ApiHelper {

	protected abstract EventHelper getEventHelper();

	@Override
	public Long getCurrentUserId() {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final Long currentUser = appUserHelper.getCurrentUserId();
		return currentUser;
	}

	protected Boolean isMySelf(final Long userId) {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final Long currentUser = appUserHelper.getCurrentUserId();
		return currentUser.equals(userId);
	}

	public abstract String getAuthorisationLink();

	@SuppressWarnings("PMD.EmptyCatchBlock")
	public Boolean isCalendarConfigured(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarsConfigurationFormData calendarsConfig = calendarConfigurationService
				.getCalendarConfigurationTableData(Boolean.FALSE);

		return calendarsConfig.getCalendarConfigTable().getRowCount() > 0;
	}

	@Override
	public String getAccountsEmail(final CalendarConfigTableRowData[] calendarsConfigurationRows) {
		final StringBuilder accountsEmails = new StringBuilder();
		final IApiService apiService = BEANS.get(IApiService.class);

		if (null != calendarsConfigurationRows && calendarsConfigurationRows.length > 0) {

			final Set<Long> modifiedOAuthIds = new HashSet<>();
			for (final CalendarConfigTableRowData calendarConfig : calendarsConfigurationRows) {
				if (null != calendarConfig.getOAuthCredentialId()) {
					modifiedOAuthIds.add(calendarConfig.getOAuthCredentialId());
				}
			}

			if (null != modifiedOAuthIds && modifiedOAuthIds.size() > 0) {
				final String separator = ", ";
				for (final Long modifiedOAuthId : modifiedOAuthIds) {
					final ApiFormData apiData = apiService.load(modifiedOAuthId);
					accountsEmails.append(apiData.getAccountEmail().getValue()).append(separator);
				}
				if (accountsEmails.length() >= separator.length()) {
					accountsEmails.delete(accountsEmails.length() - separator.length(), accountsEmails.length());
				}
			}
		}

		return accountsEmails.toString();
	}

	public abstract E getCredential(final Long oAuthCredentialId);

	protected abstract ApiCredential<E> getApiCredential(final ApiTableRowData apiData);

	protected abstract ApiCalendar<F, ApiCredential<E>> getCalendarService(final Long apiCredentialId);

	@Override
	public void autoConfigureCalendars() {
		this.autoConfigureCalendars(this.getCurrentUserId());
	}

	@Override
	public void autoConfigureCalendars(final Long userId) {
		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = this.getCalendars(userId);

		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);
		calendarConfigurationService.autoConfigure(calendars);
	}

	@Override
	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars() {
		return this.getCalendars(this.getCurrentUserId());
	}

	@Override
	public Boolean delete(final String calendarId, final String eventId, final Long apiCredentialId) {
		final F calendarService = this.getCalendarService(apiCredentialId).getCalendar();

		return this.delete(calendarId, eventId, calendarService);
	}

	protected abstract Boolean delete(String calendarId, String eventId, F calendarService);

	@Override
	public CalendarAviability getCalendarAviability(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId, final AbstractCalendarConfigurationTableRowData calendar, final ZoneId userZoneId) {
		return this.getEventHelper().getCalendarAviability(startDate, endDate, userId, calendar, userZoneId);
	}

	@Override
	public void askToAddApi(final Long userId) {
		final int userDecision = MessageBoxes.createYesNo().withHeader(TEXTS.get("zc.api.calendarRequired.title"))
				.withBody(TEXTS.get("zc.api.calendarRequired.message"))
				// .withYesButtonText(TEXTS.get("zc.subscription.notAllowed.yesButton"))
				.withIconId(Icons.ExclamationMark).withSeverity(IStatus.WARNING).show();

		if (userDecision == IMessageBox.YES_OPTION) {
			this.displayAddCalendarForm(userId);
		}
	}

	public void displayAddCalendarForm(final Long userId) {
		final OnBoardingUserForm form = new OnBoardingUserForm();
		form.getUserIdField().setValue(userId);
		form.setEnabledPermission(new UpdateUserPermission(userId));
		form.startModify();
		form.waitFor();
	}

}
