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
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.service.CalendarAviability;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;

/**
 * @author djer
 *
 */
@ApplicationScoped
public interface ApiHelper {

	public Long getCurrentUserId();

	public void askToAddApi(final Long userId);

	public void askToChooseCalendarToAddEvent(Long userId);

	public void displayAddCalendarForm(final Long userId);

	public String getAuthorisationLinksAsLi();

	public String getAccountEmail(final Long apiCredentialId);

	public void autoConfigureCalendars();

	public void autoConfigureCalendars(final Long userId);

	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars();

	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final Long userId);

	public Map<String, AbstractCalendarConfigurationTableRowData> getCalendars(final ApiTableRowData aUserApi);

	public Boolean delete(final String calendarId, final String eventId, final Long apiCredentialId);

	public String createEvent(ZonedDateTime startDate, ZonedDateTime endDate, String subject, Long forUserId,
			String location, String withEmail, Boolean guestAutoAcceptMeeting, String envDisplay,
			CalendarConfigurationFormData calendarToStoreEvent, String description);

	public String getEventHtmlLink(EventIdentification eventIdentification, Long apiCredentialId);

	public Boolean acceptEvent(EventIdentification eventOrganizerIdentification, String attendeeEmail,
			ApiTableRowData eventCreatorApi);

	public CalendarAviability getCalendarAviability(ZonedDateTime startDate, ZonedDateTime endDate, Long userId,
			AbstractCalendarConfigurationTableRowData calendar, ZoneId userZoneId);

}
