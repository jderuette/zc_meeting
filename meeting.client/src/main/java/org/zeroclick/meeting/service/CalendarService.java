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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.configuration.shared.slot.SlotCodeType;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationEnvProperty;
import org.zeroclick.meeting.client.api.ApiHelper;
import org.zeroclick.meeting.client.api.ApiHelperFactory;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.client.common.SlotHelper;
import org.zeroclick.meeting.client.event.DateReturn;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

import com.google.api.services.calendar.model.Event;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class CalendarService {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarService.class);

	public Boolean isCalendarConfigured() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return this.isCalendarConfigured(acsHelper.getZeroClickUserIdOfCurrentSubject());
	}

	public Boolean isCalendarConfigured(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarConfigurationTablePageData calendarsConfig = calendarConfigurationService
				.getCalendarConfiguration(userId);

		return calendarsConfig.getRowCount() > 0;
	}

	public Boolean isAddCalendarConfigured() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return this.isAddCalendarConfigured(acsHelper.getZeroClickUserIdOfCurrentSubject());
	}

	public Boolean isAddCalendarConfigured(final Long userId) {
		Boolean addCalendarConfigured = Boolean.FALSE;
		final CalendarConfigurationFormData configuredAddCalendar = this.getUserCreateEventCalendar(userId);

		if (null != configuredAddCalendar && null != configuredAddCalendar.getCalendarConfigurationId()) {
			addCalendarConfigured = Boolean.TRUE;
		}
		return addCalendarConfigured;
	}

	public ZonedDateTime canCreateEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId,
			final ZoneId userZoneId) {
		LOG.info(new StringBuilder(150).append("Checking for calendars events from : ").append(startDate).append(" to ")
				.append(endDate).append(" for user : ").append(userId).toString());

		if (!this.isCalendarConfigured(userId)) {
			LOG.info("Cannot check user calendar because no API configured for user : " + userId);
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

			final CalendarAviability createEventAvaibilityInfo = apiHelper.getCalendarAviability(startDate, endDate,
					userId, calendar, userZoneId);

			if (null == createEventAvaibilityInfo.getEndLastEvent()) {
				LOG.info(new StringBuilder().append("No event found in calendars from : ").append(startDate)
						.append(" to ").append(endDate).append(" for user : ").append(userId).toString());
				// Do nothing special, recommendedNewDate = null meaning
				// provided periods is OK
			} else {
				if (null != createEventAvaibilityInfo.getEndLastEvent()
						&& createEventAvaibilityInfo.getEndLastEvent().equals(startDate)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(100)
								.append("Last event end at the same start, slot is aviallable from calendars from : ")
								.append(startDate).append(" to ").append(endDate).append(" for user : ").append(userId)
								.toString());
						// Do nothing special, recommendedNewDate = null meaning
						// provided periods is OK
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder(100).append(allConcurentEvent.size())
								.append(" event(s) found in calendars from : ").append(startDate).append(" to ")
								.append(endDate).append(" for user : ").append(userId).toString());
					}
					final List<DayDuration> freeTimes = createEventAvaibilityInfo.getFreeTimes();

					if (!freeTimes.isEmpty()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug(new StringBuilder(100).append("FreeTime found in calendars from : ")
									.append(startDate).append(" to ").append(endDate).append(" with periods : ")
									.append(freeTimes).toString());
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
									.append(startDate).append(" to ").append(endDate).append(" for user : ")
									.append(userId).toString());
						}
						// TODO Djer13 is required to add 1 minute ?
						recommendedNewDate = createEventAvaibilityInfo.getEndLastEvent().plus(Duration.ofMinutes(1));
						LOG.info(
								new StringBuilder().append("Recommanding new search from : ").append(recommendedNewDate)
										.append(" (cause : ").append(userId).append(" has whole period blocked by ")
										.append(allConcurentEvent.size()).append(" event(s), last blocking event date ")
										.append(createEventAvaibilityInfo.getEndLastEvent()).toString());
					}
				}
			}
		}
		return recommendedNewDate;
	}

	public DateReturn searchNextDate(final Long eventId, final Long durationId, final Long slotId,
			final Long organizerId, final Long currentUserId, final ZonedDateTime minimalStart,
			final ZonedDateTime maximalStart, final ZonedDateTime newStartDate, final ZonedDateTime currentStartDate,
			final Boolean askedByUser) {

		final DurationCodeType durationCodes = BEANS.get(DurationCodeType.class);
		final ICode<Long> duration = durationCodes.getCode(durationId);

		final SlotCodeType slotCodes = BEANS.get(SlotCodeType.class);
		final ICode<Long> slot = slotCodes.getCode(slotId);

		return this.searchNextDate(eventId, duration, slot, organizerId, currentUserId, minimalStart, maximalStart,
				newStartDate, currentStartDate, askedByUser);

	}

	public DateReturn searchNextDate(final Long eventId, final ICode<Long> duration, final ICode<Long> slot,
			final Long organizerId, final Long currentUserId, final ZonedDateTime minimalStart,
			final ZonedDateTime maximalStart, final ZonedDateTime newStartDate, final ZonedDateTime currentStartDate,
			final Boolean askedByUser) {

		final CallTrackerHelper callTrackerHelper = BEANS.get(CallTrackerHelper.class);
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Changing Next date for Event ID : " + eventId + " with start Date : " + newStartDate);
		}

		final ZonedDateTime nextStartDate = this.addReactionTime(newStartDate);
		DateReturn newPossibleDate;

		newPossibleDate = this.tryChangeDatesNext(nextStartDate, duration, slot, organizerId, currentUserId,
				minimalStart, maximalStart, currentStartDate);

		while (!newPossibleDate.isCreated()) {
			// check if the timeSlot available in Calendars
			// if not, try with the new available start date (in
			// calendars)
			if (callTrackerHelper.getEventCallTracker().canIncrementNbCall(eventId)) {
				if (newPossibleDate.isNoAvailableDate()) {
					return newPossibleDate;// break new Date search
				}
				newPossibleDate = this.tryChangeDatesNext(newPossibleDate.getStart(), duration, slot, organizerId,
						currentUserId, minimalStart, maximalStart, currentStartDate);
			} else {
				if (askedByUser) {
					final int continueSearch = MessageBoxes.createYesNo()
							.withHeader(TEXTS.get("zc.meeting.event.maxSearchReached.title"))
							.withBody(TEXTS.get("zc.meeting.event.maxSearchReached.message",
									dateHelper.format(newPossibleDate.getStart())))
							.withYesButtonText(TEXTS.get("YesButton")).withIconId(Icons.ExclamationMark)
							.withSeverity(IStatus.INFO).show();
					if (continueSearch == IMessageBox.YES_OPTION) {
						callTrackerHelper.getEventCallTracker().resetNbCall(eventId);
					} else {
						break; // stop search
					}
				} else {
					break; // stop search without prompt, because not a user
							// action
				}
			}
		}
		callTrackerHelper.getEventCallTracker().resetNbCall(eventId);

		if (newPossibleDate.isNoAvailableDate()) {
			// reached end of available date (after maximal)
			// and the new proposed date is after the one currently
			// displayed, so now over date are available

			newPossibleDate.setMessageKey("zc.meeting.notification.NoAvailableNextDate");
			newPossibleDate.setIcon(Icons.ExclamationMark);
		} else {
			if (newPossibleDate.isCreated()) {
				if (newPossibleDate.isLoopInDates()) {
					newPossibleDate.setMessageKey("zc.meeting.notification.newMeetingDateFoundFromBegin");
				} else {
					newPossibleDate.setMessageKey("zc.meeting.notification.newMeetingDateFound");
				}
				newPossibleDate.setIcon(null);
			}
		}

		return newPossibleDate;

	}

	/**
	 * Try to provide new valid start/end date for user (by updating cells in
	 * the current selected row)
	 *
	 * @param startDate
	 *            the minimum start date to search a new valid date
	 * @return a new recommend date to perform a new search, null if a valid
	 *         date is found
	 * @throws IOException
	 */
	protected DateReturn tryChangeDatesNext(ZonedDateTime startDate, final ICode<Long> duration, final ICode<Long> slot,
			final Long organizerUserId, final Long guestUserId, final ZonedDateTime minimalStartDate,
			final ZonedDateTime maximalStartDate, final ZonedDateTime currentStartDate) {
		LOG.info("Checking to create an event starting at " + startDate);

		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);

		Boolean loopInDates = Boolean.FALSE;
		final int selectEventDuration = duration.getValue().intValue();
		final Long selectSlotId = slot.getId().longValue();

		if (null != minimalStartDate && startDate.isBefore(minimalStartDate)) {
			LOG.info("startDate is before the mnimal : " + minimalStartDate);
			startDate = minimalStartDate;
		} else if (null != maximalStartDate && startDate.isAfter(maximalStartDate)) {
			LOG.info("startDate is after the maximal : " + maximalStartDate + " Loop back to the minimal : "
					+ minimalStartDate);
			loopInDates = Boolean.TRUE;
			if (null != minimalStartDate) {
				startDate = minimalStartDate;
			} else {
				startDate = appUserHelper.getUserNow(guestUserId);
			}
		}

		final ZonedDateTime nextEndDate = startDate.plus(Duration.ofMinutes(selectEventDuration));

		// Localized Start and End for organizer
		final ZonedDateTime organizerStartDate = this.atZone(startDate, organizerUserId);
		final ZonedDateTime organizerEndDate = this.atZone(nextEndDate, organizerUserId);

		DateReturn proposedDate = null;
		// Check guest (current connected user) slot configuration
		if (!SlotHelper.get().isInOneOfPeriods(selectSlotId, startDate, nextEndDate, guestUserId)) {
			proposedDate = new DateReturn(
					SlotHelper.get().getNextValidDateTime(selectSlotId, startDate, nextEndDate, guestUserId),
					loopInDates);
		}

		if (null == proposedDate) {
			// check Organizer Slot configuration
			if (!SlotHelper.get().isInOneOfPeriods(selectSlotId, organizerStartDate, organizerEndDate,
					organizerUserId)) {
				proposedDate = new DateReturn(this.atZone(SlotHelper.get().getNextValidDateTime(selectSlotId,
						organizerStartDate, organizerEndDate, organizerUserId), guestUserId), loopInDates);
			}
		}

		final CalendarService calendarService = BEANS.get(CalendarService.class);

		if (null == proposedDate) {
			// check guest (current connected user) calendars
			final ZoneId userZoneId = appUserHelper.getUserZoneId(guestUserId);
			final ZonedDateTime calendareRecommendedDate = calendarService.canCreateEvent(startDate, nextEndDate,
					guestUserId, userZoneId);

			// this.tryCreateEvent(startDate, nextEndDate,
			// Duration.ofMinutes(selectEventDuration), guestUserId);
			if (calendareRecommendedDate != null) {
				return new DateReturn(this.atZone(this.addReactionTime(calendareRecommendedDate), guestUserId),
						loopInDates);
			}
		}

		if (null == proposedDate) {
			// Check organizer calendars
			final ZoneId userZoneId = appUserHelper.getUserZoneId(organizerUserId);

			final ZonedDateTime organizerCalendareRecommendedDate = calendarService.canCreateEvent(startDate,
					nextEndDate, organizerUserId, userZoneId);

			// this.tryCreateEvent(organizerStartDate,
			// organizerEndDate, Duration.ofMinutes(selectEventDuration),
			// organizerUserId);
			if (organizerCalendareRecommendedDate != null) {
				return new DateReturn(this.addReactionTime(this.atZone(organizerCalendareRecommendedDate, guestUserId)),
						loopInDates);
			}
		}

		if (null != proposedDate && loopInDates
				&& (null == currentStartDate || proposedDate.getStart().isAfter(currentStartDate))) {
			// Loop and proposed date is after the current, so out of range,
			// and no available date)
			proposedDate.setNoAvailableDate(Boolean.TRUE);
			proposedDate.setStart(currentStartDate);
		} else if (null == proposedDate) {
			proposedDate = new DateReturn(startDate, nextEndDate, loopInDates);
		}

		return proposedDate;
	}

	private ZonedDateTime atZone(final ZonedDateTime date, final Long userId) {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		return dateHelper.atZone(date, appUserHelper.getUserZoneId(userId));
	}

	private ZonedDateTime addReactionTime(final ZonedDateTime date) {
		// TODO Djer13 add in (user ?) configuration
		final Integer minReactionDelay = 10;
		return this.addReactionTime(date, minReactionDelay);
	}

	/**
	 * Add xx mins if the date provided is too close. Always round to next
	 * Quarter (even if date is far enough)
	 *
	 * @param date
	 * @param reactionDelayMin
	 * @return
	 */
	private ZonedDateTime addReactionTime(final ZonedDateTime date, final Integer reactionDelayMin) {
		ZonedDateTime minimalStart = ZonedDateTime.now(date.getZone()).plus(Duration.ofMinutes(reactionDelayMin));
		minimalStart = this.roundToNextHourQuarter(minimalStart);
		if (date.isBefore(minimalStart)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(date + " is too close with reactionTime of " + reactionDelayMin + " mins. Using : "
						+ minimalStart);
			}
			// startDate is too close
			return minimalStart;
		} else {
			return this.roundToNextHourQuarter(date);
		}
	}

	private Integer roundToNextHourQuarter(final Integer minutes) {
		Integer newMins = minutes;
		if (minutes % 15 != 0) {
			final Integer nbQuarter = minutes / 15;
			newMins = 15 * (nbQuarter + 1);
			if (newMins >= 60) {
				newMins = 0;
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(minutes + " rounded to (next quarter) : " + newMins);
		}
		return newMins;
	}

	private ZonedDateTime roundToNextHourQuarter(final ZonedDateTime date) {
		final Integer currentMins = date.getMinute();
		final Integer newMins = this.roundToNextHourQuarter(currentMins);
		ZonedDateTime roundedDate = date;
		if (currentMins == newMins) {
			roundedDate = date;
		} else {
			if (newMins == 0) {
				// add oneHour
				roundedDate = date.plusHours(1);
			}
			roundedDate = roundedDate.withMinute(newMins).withSecond(0).withNano(0);
		}

		return roundedDate;
	}

	public void deleteEvent(final Long eventId) {
		final IEventService eventService = BEANS.get(IEventService.class);
		final EventFormData event = eventService.load(eventId);

		final String externalIdOrganizerId = event.getExternalIdOrganizer();
		final Boolean organizerEventDeleted = this.deleteEvent(event, externalIdOrganizerId,
				event.getOrganizer().getValue());
		Boolean attendeeEventDeleted = null;

		final String externalIdAttendeeId = event.getExternalIdRecipient();
		if (null != externalIdAttendeeId && null != externalIdOrganizerId
				&& !externalIdOrganizerId.equals(externalIdAttendeeId)) {
			attendeeEventDeleted = this.deleteEvent(event, externalIdAttendeeId, event.getGuestId().getValue());
		}

		if (!organizerEventDeleted) {
			throw new VetoException("Error Organizer event not deleted");
		}
		if (null != attendeeEventDeleted && !attendeeEventDeleted) {
			throw new VetoException("Error Guest event not deleted");
		}
	}

	private Boolean deleteEvent(final EventFormData event, final String externalEventId, final Long userId) {
		Boolean eventDeleted = Boolean.FALSE;
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		if (null == externalEventId) {
			LOG.info("Event : " + event.getEventId()
					+ " has no external Event Id, no event to delete in connected calendars");
			// no error during delete, because no deletion required
			eventDeleted = Boolean.TRUE;
		} else {
			final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
					.getCalendarToStoreEvents(userId);

			LOG.info(this.buildRejectLog("Deleting", event.getEventId(), externalEventId,
					calendarToStoreEvent.getExternalId().getValue(), userId));

			final IApiService apiService = BEANS.get(IApiService.class);
			final ApiTablePageData userApis = apiService.getApis(userId);

			if (userApis.getRowCount() > 0) {
				for (final ApiTableRowData userApi : userApis.getRows()) {
					final ApiHelper apiHelper = ApiHelperFactory.get(userApi);

					if (null != apiHelper) {
						eventDeleted = apiHelper.delete(calendarToStoreEvent.getExternalId().getValue(),
								externalEventId, userApi.getApiCredentialId());
						if (eventDeleted) {
							LOG.info(this.buildRejectLog("Deleted", event.getEventId(), externalEventId,
									calendarToStoreEvent.getExternalId().getValue(), userId));
							break;
						}
					}
				}
			}

			if (!eventDeleted) {
				LOG.warn(this.buildRejectLog("Error while deleting event (No api has deleted the event)",
						event.getEventId(), externalEventId, calendarToStoreEvent.getExternalId().getValue(), userId));
			}
		}

		return eventDeleted;
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

	public CalendarConfigurationFormData getUserCreateEventCalendar(final Long userId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
				.getCalendarToStoreEvents(userId);

		return calendarToStoreEvent;
	}

	public EventIdentification createEvent(final Date startDate, final Date endDate, final Long forUserId,
			final String organizerEmail, final Collection<ParticipantWithStatus> participants, final String subject,
			final String location, final String description) throws IOException {
		if (null == forUserId) {
			LOG.error("Error while creating external event for a NULL userId as Holder");
			throw new VetoException("Technical Error while creating external Event");
		}

		final CalendarConfigurationFormData calendarToStoreEvent = this.getUserCreateEventCalendar(forUserId);
		final ApiTableRowData eventCreatorApi = this.getCalendarApi(calendarToStoreEvent);

		final ApiHelper apiHelper = ApiHelperFactory.get(eventCreatorApi);

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Creating Event : '").append(subject).append("' fom : ")
					.append(startDate).append(" to ").append(endDate).append(", for :").append(forUserId)
					.append(", in Calendar : ").append(calendarToStoreEvent.getExternalId().getValue())
					.append(" (attendee :").append(participants).append(", autoAccept? ").append(")").toString());
		}

		final String envDisplay = new ApplicationEnvProperty().displayAsText();

		final String createdEventId = apiHelper.createEvent(startDate, endDate, subject, forUserId, organizerEmail,
				location, participants, envDisplay, calendarToStoreEvent, description);

		return new EventIdentification(createdEventId, calendarToStoreEvent);
	}

	// public EventIdentification acceptCreatedEvent(final EventIdentification
	// eventIdentification, final Long userId,
	// final String attendeeEmail, final Long heldByUserId) throws IOException {
	// return this.acceptCreatedEvent(eventIdentification, userId,
	// attendeeEmail, heldByUserId);
	// }

	public EventIdentification acceptCreatedEvent(final EventIdentification eventOrganizerIdentification,
			final Long userId, final String attendeeEmail, final Long heldByUserId) throws IOException {
		LOG.info(new StringBuilder().append("Accepting Event id (").append(eventOrganizerIdentification)
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
		// final ICalendarConfigurationService calendarConfigService =
		// BEANS.get(ICalendarConfigurationService.class);
		final IApiService apiService = BEANS.get(IApiService.class);

		// final Long calendarLinkedApiId =
		// calendarConfigService.getCalendarApiId(eventIdentification.getCalendarId(),
		// eventHeldBy);
		final Long calendarLinkedApiId = eventIdentification.getCalendarData().getOAuthCredentialId().getValue();

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

	public void autoConfigureCalendars() {
		this.autoConfigureCalendars(this.getCurrentUserId());
	}

	public void autoConfigureCalendars(final Long userId) {
		LOG.info("AutoConfiguring all Calendars for user : " + userId);

		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiTablePageData userApis = apiService.getApis(userId);

		final Map<String, AbstractCalendarConfigurationTableRowData> calendars = new HashMap<>();

		if (null != userApis && userApis.getRowCount() > 0) {
			for (final ApiTableRowData aUserApi : userApis.getRows()) {
				final ApiHelper apiHelper = ApiHelperFactory.get(aUserApi);
				calendars.putAll(apiHelper.getCalendars(aUserApi));
				// apiHelper.autoConfigureCalendars(aUserApi);
			}

			final ICalendarConfigurationService calendarConfigurationService = BEANS
					.get(ICalendarConfigurationService.class);
			calendarConfigurationService.autoConfigure(calendars);
		}
	}

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

	private Long getCurrentUserId() {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final Long currentUser = appUserHelper.getCurrentUserId();
		return currentUser;
	}

	public class EventIdentification {
		private final String eventId;
		private final CalendarConfigurationFormData calendarData;
		private ExternalEventFormData externalEventData;

		public EventIdentification(final String eventId, final CalendarConfigurationFormData calendarData) {
			super();
			this.eventId = eventId;
			this.calendarData = calendarData;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder(128);
			builder.append("EventIdentification [eventId=").append(this.eventId).append(", calendarId=")
					.append(this.getCalendarData().getCalendarConfigurationId().getValue()).append("]");
			return builder.toString();
		}

		public String getEventId() {
			return this.eventId;
		}

		public CalendarConfigurationFormData getCalendarData() {
			return this.calendarData;
		}

		public ExternalEventFormData getExternalEventData() {
			return this.externalEventData;
		}

		public void setExternalEventData(final ExternalEventFormData externalEventData) {
			this.externalEventData = externalEventData;
		}
	}

}
