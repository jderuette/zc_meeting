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
package org.zeroclick.meeting.service;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationEnvProperty;
import org.zeroclick.meeting.client.api.ApiHelper;
import org.zeroclick.meeting.client.api.ApiHelperFactory;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.client.common.SlotHelper;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.AccessControlService;

import com.google.api.services.calendar.model.Event;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class CalendarService {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarService.class);

	@SuppressWarnings("PMD.EmptyCatchBlock")
	public Boolean isCalendarConfigured(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarsConfigurationFormData calendarsConfig = calendarConfigurationService
				.getCalendarConfigurationTableData(Boolean.FALSE);

		return calendarsConfig.getCalendarConfigTable().getRowCount() > 0;
	}

	public Boolean isCalendarConfigured() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		return this.isCalendarConfigured(acs.getZeroClickUserIdOfCurrentSubject());
	}

	public ZonedDateTime canCreateEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId,
			final ZoneId userZoneId) {
		LOG.info(new StringBuilder(150).append("Cheking for (Google) calendars events from : ").append(startDate)
				.append(" to ").append(endDate).append(" for user : ").append(userId).toString());

		if (!this.isCalendarConfigured(userId)) {
			LOG.info("Cannot check user clendar because no API configured for user : " + userId);
			// new recommended Date null, means "available" in user calendar
			return null; // early break
		}

		// getEvent from start to End for each calendar
		final Set<AbstractCalendarConfigurationTableRowData> activatedEventCalendars = this
				.getUserUsedEventCalendar(userId);

		ZonedDateTime recommendedNewDate = null;
		final List<Event> allConcurentEvent = new ArrayList<>();
		for (final AbstractCalendarConfigurationTableRowData calendar : activatedEventCalendars) {
			if (null != recommendedNewDate) {
				break;// a blocking event already found in an other calendar
			}

			final IApiService apiService = BEANS.get(IApiService.class);

			final ApiTableRowData calendarApiData = apiService.getApi(calendar.getOAuthCredentialId());
			final ApiHelper apiHelper = ApiHelperFactory.get(calendarApiData);

			final CalendarAviability creatEventAvaibilityInfo = apiHelper.getCalendarAviability(startDate, endDate,
					userId, calendar, userZoneId);

			if (null == creatEventAvaibilityInfo.getEndLastEvent()) {
				LOG.info(new StringBuilder().append("No event found in calendars from : ").append(startDate)
						.append(" to ").append(endDate).append(" for user : ").append(userId).toString());
				// Do nothing special, recommendedNewDate = null meaning
				// provided periods is OK
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder(100).append(allConcurentEvent.size())
							.append(" event(s) found in calendars from : ").append(startDate).append(" to ")
							.append(endDate).append(" for user : ").append(userId).toString());
				}
				final List<DayDuration> freeTimes = creatEventAvaibilityInfo.getFreeTimes();

				if (!freeTimes.isEmpty()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(100).append("FreeTime found in calendars from : ").append(startDate)
								.append(" to ").append(endDate).append(" with periods : ").append(freeTimes)
								.toString());
					}
					recommendedNewDate = SlotHelper.get().getNextValidDateTime(freeTimes, startDate, endDate);
					if (null != recommendedNewDate) {
						LOG.info(new StringBuilder().append("Recommanding new search from : ")
								.append(recommendedNewDate).append(" (cause : ").append(userId)
								.append(" has blocking event(s) but freeTime)").toString());
					}
				}
				if (null == recommendedNewDate) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(100).append("No avilable periods found in freeTime from : ")
								.append(startDate).append(" to ").append(endDate).append(" for user : ").append(userId)
								.toString());
					}
					// TODO Djer13 is required to add 1 minute ?
					recommendedNewDate = creatEventAvaibilityInfo.getEndLastEvent().plus(Duration.ofMinutes(1));
					LOG.info(new StringBuilder().append("Recommanding new search from : ").append(recommendedNewDate)
							.append(" (cause : ").append(userId).append(" has whole period blocked by ")
							.append(allConcurentEvent.size()).append(" event(s), last blocking event date ")
							.append(creatEventAvaibilityInfo.getEndLastEvent()).toString());
				}
			}
		}

		return recommendedNewDate;

	}

	public void deleteEvent(final Long eventId) {
		final IEventService eventService = BEANS.get(IEventService.class);
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final EventFormData event = eventService.load(eventId);

		if (null == event.getExternalIdOrganizer()) {
			LOG.info("Event : " + eventId + " has no external organizer ID, no event to delete in connected calendars");
		} else {
			final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
					.getCalendarToStoreEvents(event.getOrganizer().getValue());

			LOG.info(this.buildRejectLog("Deleting", eventId, event.getExternalIdOrganizer(),
					calendarToStoreEvent.getExternalId().getValue(), event.getOrganizer().getValue()));

			final IApiService apiService = BEANS.get(IApiService.class);
			final ApiTablePageData organizerApis = apiService.getApis(event.getOrganizer().getValue());

			Boolean eventDeleted = Boolean.FALSE;
			if (organizerApis.getRowCount() > 0) {
				for (final ApiTableRowData organizerApi : organizerApis.getRows()) {
					final ApiHelper apiHelper = ApiHelperFactory.get(organizerApi);

					if (null != apiHelper) {
						eventDeleted = apiHelper.delete(calendarToStoreEvent.getExternalId().getValue(),
								event.getExternalIdOrganizer(), organizerApi.getApiCredentialId());
						if (eventDeleted) {
							break;
						}
					}
				}
			}

			if (!eventDeleted) {
				LOG.warn(this.buildRejectLog("Error while deleting (No api has deleted the event)", eventId,
						event.getExternalIdOrganizer(), calendarToStoreEvent.getExternalId().getValue(),
						event.getOrganizer().getValue()));
				throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
			}
		}
	}

	private String buildRejectLog(final String prefix, final Long eventId, final String externalId,
			final String calendarId, final Long userId) {
		final StringBuilder builder = new StringBuilder(75);
		builder.append(prefix).append("  event : Id ").append(eventId).append(", external ID : ").append(externalId)
				.append(" In Calendar :").append(calendarId).append(" for user : ").append(userId);
		return builder.toString();
	}

	private ApiTableRowData getCalendarApi(final CalendarConfigurationFormData calendarConfiguration) {
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiTableRowData addCalendarApi = apiService
				.getApi(calendarConfiguration.getOAuthCredentialId().getValue());

		return addCalendarApi;
	}

	private CalendarConfigurationFormData getUserCreateEventCalendar(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
				.getCalendarToStoreEvents(userId);

		return calendarToStoreEvent;
	}

	public EventIdentification createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long forUserId, final String withEmail, final String subject, final String location,
			final Boolean guestAutoAcceptMeeting) throws IOException {
		final CalendarConfigurationFormData calendarToStoreEvent = this.getUserCreateEventCalendar(forUserId);
		final ApiTableRowData eventCreatorApi = this.getCalendarApi(calendarToStoreEvent);

		final ApiHelper apiHelper = ApiHelperFactory.get(eventCreatorApi);

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Creating Event : '").append(subject).append("' fom : ")
					.append(startDate).append(" to ").append(endDate).append(", for :").append(forUserId)
					.append(", in Calendar : ").append(calendarToStoreEvent.getExternalId()).append(" (attendee :")
					.append(withEmail).append(", autoAccept? ").append(guestAutoAcceptMeeting).append(")").toString());
		}

		final String envDisplay = new ApplicationEnvProperty().displayAsText();

		final String createdEventId = apiHelper.createEvent(startDate, endDate, subject, forUserId, location, withEmail,
				guestAutoAcceptMeeting, envDisplay, calendarToStoreEvent);

		return new EventIdentification(createdEventId, calendarToStoreEvent.getExternalId().getValue());
	}

	// public EventIdentification acceptCreatedEvent(final EventIdentification
	// eventIdentification, final Long userId,
	// final String attendeeEmail, final Long heldByUserId) throws IOException {
	// return this.acceptCreatedEvent(eventIdentification, userId,
	// attendeeEmail, heldByUserId);
	// }

	public EventIdentification acceptCreatedEvent(final EventIdentification eventOrganizerIdentification,
			final Long userId, final String attendeeEmail, final Long heldByUserId) throws IOException {
		LOG.info(new StringBuilder().append("Accepting (Google) Event id (").append(eventOrganizerIdentification)
				.append("), for :").append(userId).append(" with his email : ").append(attendeeEmail).toString());

		Long eventCreatorUserId = userId;

		if (!this.isCalendarConfigured(userId)) {
			LOG.info(new StringBuilder().append("User : ").append(userId)
					.append(" as no calendar configured (assuming no API acces), updating the (Google) Event with the heldBy user Id (")
					.append(heldByUserId).append(")").toString());
			eventCreatorUserId = heldByUserId;
		}

		final ApiTableRowData eventCreatorApi = this.getApi(eventOrganizerIdentification, eventCreatorUserId);

		final ApiHelper apiHelper = ApiHelperFactory.get(eventCreatorApi);

		apiHelper.acceptEvent(eventOrganizerIdentification, attendeeEmail, eventCreatorApi);

		return eventOrganizerIdentification;
	}

	protected ApiTableRowData getApi(final EventIdentification eventIdentification, final Long eventHeldBy) {
		final ICalendarConfigurationService calendarConfigService = BEANS.get(ICalendarConfigurationService.class);
		final IApiService apiService = BEANS.get(IApiService.class);

		final Long calendarLinkedApiId = calendarConfigService.getCalendarApiId(eventIdentification.getCalendarId(),
				eventHeldBy);
		final ApiTableRowData apiData = apiService.getApi(calendarLinkedApiId);

		return apiData;
	}

	public String getEventExternalLink(final EventIdentification eventIdentification, final Long eventHeldBy) {
		final ApiTableRowData apiData = this.getApi(eventIdentification, eventHeldBy);
		final ApiHelper apiHelper = ApiHelperFactory.get(apiData);

		String htmlLink = "unknow";
		htmlLink = apiHelper.getEventHtmlLink(eventIdentification, apiData.getApiCredentialId());

		return htmlLink;
	}

	private Set<AbstractCalendarConfigurationTableRowData> getUserUsedEventCalendar(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final Set<AbstractCalendarConfigurationTableRowData> usedCalendars = calendarConfigurationService
				.getUsedCalendars(userId);

		return usedCalendars;

	}

	public class EventIdentification {
		private final String eventId;
		private final String calendarId;

		public EventIdentification(final String eventId, final String calendarId) {
			super();
			this.eventId = eventId;
			this.calendarId = calendarId;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder(128);
			builder.append("EventIdentification [eventId=").append(this.eventId).append(", calendarId=")
					.append(this.calendarId).append("]");
			return builder.toString();
		}

		public String getEventId() {
			return this.eventId;
		}

		public String getCalendarId() {
			return this.calendarId;
		}
	}
}
