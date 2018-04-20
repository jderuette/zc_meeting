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
package org.zeroclick.meeting.client.api.event;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.meeting.client.api.ProviderDateHelper;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.service.CalendarAviability;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

/**
 * @author djer
 *
 */
public abstract class AbstractEventHelper<T, D> implements EventHelper {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEventHelper.class);

	protected abstract ProviderDateHelper<D> getDateHelper();

	/**
	 * Retrieve api provider events for the calendar. Should not apply user's
	 * filter
	 *
	 * @param startDate
	 * @param endDate
	 * @param userId
	 * @param calendar
	 * @return "raw" data from the calendar's provider
	 */
	public abstract List<T> retrieveEvents(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId, final AbstractCalendarConfigurationTableRowData calendar);

	public List<T> getEvents(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId,
			final AbstractCalendarConfigurationTableRowData calendar) {

		final List<T> events = this.retrieveEvents(startDate, endDate, userId, calendar);
		final List<T> calendarEvents = this.filterEvents(events, calendar);

		return calendarEvents;
	}

	public List<T> filterEvents(final List<T> events, final AbstractCalendarConfigurationTableRowData calendar) {

		final Boolean processFullDay = calendar.getProcessFullDayEvent();
		final Boolean processFree = calendar.getProcessFreeEvent();
		final Boolean processNotRegisteredOn = calendar.getProcessNotRegistredOnEvent();

		final List<T> calendarEvents = new ArrayList<>();
		String apiAccountsEmail = null;

		if (null != events && null != events && events.size() > 0) {
			for (final T event : events) {
				// dispo/busy
				if (!processFree && this.isFree(event)) {
					LOG.info(new StringBuilder(100).append(this.asLog(event))
							.append(") is ignored because processFree is False in this calendar configuration (")
							.append(calendar.getCalendarConfigurationId()).append(") and user is free on this event")
							.append(" from calendar : ").append(calendar.getExternalId()).toString());
					continue;
				}

				// event registred on
				if (!processNotRegisteredOn) {
					if (null == apiAccountsEmail) {
						final IApiService apiService = BEANS.get(IApiService.class);
						final ApiFormData apiConfg = apiService.load(calendar.getOAuthCredentialId());
						apiAccountsEmail = apiConfg.getAccountEmail().getValue();
					}

					if (this.isNotRegiteredOn(event, apiAccountsEmail)) {
						LOG.info(new StringBuilder(100).append(this.asLog(event))
								.append(") is ignored because processNotRegisteredOn is False in this calendar Configuration (")
								.append(calendar.getCalendarConfigurationId()).append(") and ").append(apiAccountsEmail)
								.append(" isn't organizer or hasen't accepted the event from calendar : ")
								.append(calendar.getExternalId()).toString());
						continue;
					}
				}

				// full day event
				if (!processFullDay && this.isFullDay(event)) {
					LOG.info(new StringBuilder().append(this.asLog(event))
							.append(" is ignored because is FullDay event from calendar : ")
							.append(calendar.getExternalId()).append(" ignored : ").toString());
					continue;
				}

				calendarEvents.add(event);
			}
		}
		return calendarEvents;
	}

	/**
	 * Use to filter event to apply user's custom CalendarConfiguration @see
	 * {@link AbstractCalendarConfigurationTableRowData#processNotRegistredOnEvent}
	 *
	 * @param event
	 * @param userEmail
	 *            email to check creator/registered (should be the api user's
	 *            account email)
	 * @return true if user is <b>not</b>registered on this event(this event
	 *         will be a candidate to be removed from analyzed Events), else
	 *         false
	 */
	public abstract Boolean isNotRegiteredOn(T event, String userEmail);

	/**
	 * Use to filter event to apply user's custom CalendarConfiguration @see
	 * {@link AbstractCalendarConfigurationTableRowData#processFreeEvent}
	 *
	 * @param event
	 * @return true if user is <b>free</b> during this event (this event will be
	 *         a candidate to be removed from analyzed Events), else false
	 */
	public abstract Boolean isFree(T event);

	/**
	 * Use to filter event to apply user's custom CalendarConfiguration @see
	 * {@link AbstractCalendarConfigurationTableRowData#processFreeEvent}
	 *
	 * @param event
	 * @return true if this event is a full day event (this event will be a
	 *         candidate to be removed from analyzed Events), else false
	 */
	public abstract Boolean isFullDay(T event);

	public T getLastEvent(final List<T> allConcurentEvent) {
		final T lastEvent = allConcurentEvent.get(allConcurentEvent.size() - 1);
		return lastEvent;
	}

	/**
	 * Search for freeTime in period startDate and endDate. Also retrieve last
	 * event ends
	 */
	@Override
	public CalendarAviability getCalendarAviability(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId, final AbstractCalendarConfigurationTableRowData calendar, final ZoneId userZoneId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(
					new StringBuilder().append("calculating calendar aviability for user ").append(userId).toString());
		}
		final List<T> allConcurentEvent = this.getEvents(startDate, endDate, userId, calendar);

		return this.getCalendarAviability(startDate, endDate, allConcurentEvent, userZoneId);
	}

	public CalendarAviability getCalendarAviability(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final List<T> allConcurentEvent, final ZoneId userZoneId) {
		LOG.info(new StringBuilder().append("Searching for calendar aviability in ").append(allConcurentEvent.size())
				.append(" events").toString());

		ZonedDateTime endLastEvent = null;
		List<DayDuration> freeTimes = null;
		if (!allConcurentEvent.isEmpty()) {
			freeTimes = this.getFreeTime(startDate, endDate, allConcurentEvent, userZoneId);
			final T lastEvent = this.getLastEvent(allConcurentEvent);
			endLastEvent = this.fromEventDateTime(this.getEventEnd(lastEvent));

			final int nbFreeTimePeriods = null == freeTimes ? 0 : freeTimes.size();
			final String lastEventEnd = null == endLastEvent ? "" : endLastEvent.toString();
			LOG.info("Calendars has " + nbFreeTimePeriods + " freeTimes periods, last blocking event ends at : "
					+ lastEventEnd + " (event : " + this.asLog(lastEvent) + ")");
		}

		return new CalendarAviability(endLastEvent, freeTimes);
	}

	public String asLog(final T event) {
		return this.asLog(event, Boolean.FALSE);
	}

	public abstract void sort(final List<T> events);

	public List<DayDuration> getFreeTime(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final List<T> events, final ZoneId userZoneId) {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);

		final ZonedDateTime userZoneStartDate = dateHelper.atZone(startDate, userZoneId);
		final ZonedDateTime userZonEndDate = dateHelper.atZone(endDate, userZoneId);

		final FreeTimeAppender freeTimeAppender = new FreeTimeAppender(userZoneStartDate, userZonEndDate,
				startDate.getDayOfWeek());
		this.sort(events);
		final Iterator<T> itEvent = events.iterator();

		while (itEvent.hasNext()) {
			final T event = itEvent.next();
			final ZonedDateTime eventZonedStartDate = this.getEventStartZoned(event);
			final DayOfWeek eventLocalStartDateDay = eventZonedStartDate.getDayOfWeek();
			final ZonedDateTime eventZonedEndDate = this.getEventEndZoned(event);

			freeTimeAppender.addEvent(dateHelper.atZone(eventZonedStartDate, userZoneId),
					dateHelper.atZone(eventZonedEndDate, userZoneId), eventLocalStartDateDay);
		}

		return freeTimeAppender.getFreeTimes();
	}

	/*
	 * public List<DayDuration> getFreeTime2(final ZonedDateTime startDate,
	 * final ZonedDateTime endDate, final List<T> events, final ZoneId
	 * userZoneId) { final FreeTimeAppender freeTimeAppender = new
	 * FreeTimeAppender(startDate, endDate, startDate.getDayOfWeek()); final
	 * List<DayDuration> freeTime = new ArrayList<>(); this.sort(events); final
	 * Iterator<T> itEvent = events.iterator(); Boolean isFirstEvent =
	 * Boolean.TRUE;
	 *
	 * if (LOG.isDebugEnabled()) { LOG.debug(new
	 * StringBuilder().append("Searching for freeTime from : ").append(startDate
	 * ).append(" to ")
	 * .append(endDate).append(" with : ").append(events.size()).
	 * append(" event(s) in period").toString()); }
	 *
	 * T event = null; while (itEvent.hasNext()) { final ZonedDateTime
	 * eventZonedStartDate = this.getEventStartZoned(event); final DayOfWeek
	 * eventLocalStartDateDay = eventZonedStartDate.getDayOfWeek(); final
	 * ZonedDateTime eventZonedEndDate = this.getEventEndZoned(event);
	 *
	 * // next should be call only the first time, the end of the while // move
	 * to the next (to get start of the next event) if (isFirstEvent) { event =
	 * itEvent.next(); isFirstEvent = Boolean.FALSE; } // TODO filter collected
	 * FreeTime with the current Event
	 *
	 * // this event block the whole period if
	 * (!eventZonedStartDate.isAfter(startDate) &&
	 * !eventZonedEndDate.isBefore(endDate)) { // clear optionally previously
	 * discovered freeTime freeTime.clear(); // stop searching freeTime, this
	 * event block the whole period break; }
	 *
	 * // events start Before AND ends during period -> FT endEvent-End //
	 * period // " " " AND ends after period -> NO FT // " " " AND ends before
	 * period -> NOT possible
	 *
	 * if (!eventZonedStartDate.isAfter(startDate)) { if
	 * (eventZonedEndDate.isBefore(endDate)) { // FreeTime at the end of the
	 * period // if This FreeTime not blocked by others events final OffsetTime
	 * freeTimeStart = eventZonedEndDate.toOffsetDateTime().toOffsetTime();
	 * final OffsetTime freeTimeEnd =
	 * endDate.toLocalTime().atOffset(endDate.getOffset());
	 *
	 * freeTimeAppender.add(freeTimeStart, freeTimeEnd, eventLocalStartDateDay);
	 * } else { // the whole period is blocked by this event freeTime.clear();
	 * break; } // No freeTime at the end of period, we move to next One if
	 * (!eventZonedEndDate.isBefore(endDate)) { if (itEvent.hasNext()) { event =
	 * itEvent.next(); } } } else { // FreeTime at the begin of the period
	 * freeTimeAppender.add(startDate.toOffsetDateTime().toOffsetTime(),
	 * eventZonedStartDate.toLocalTime().atOffset(eventZonedStartDate.getOffset(
	 * )), eventLocalStartDateDay); } // freeTime from end of this event to
	 * begin of the next (if // this event ends before the endDate) if
	 * (eventZonedEndDate.isBefore(endDate)) { T nextEvent = null; if
	 * (itEvent.hasNext()) { nextEvent = itEvent.next(); } ZonedDateTime
	 * nextEventLocalStartDate; ZonedDateTime nextEventLocalEndDate; ZoneOffset
	 * offset; if (null == nextEvent) { // no more event we are on the last one,
	 * We can't know the // end of this FreeTime period with the current data
	 * range. // Defaulting to end of the period nextEventLocalStartDate =
	 * endDate; offset = endDate.getOffset(); } else { nextEventLocalStartDate =
	 * this.getEventStartZoned(nextEvent); nextEventLocalEndDate =
	 * this.getEventEndZoned(nextEvent); offset =
	 * nextEventLocalStartDate.getOffset();
	 *
	 * // if the "next" event begin at the time of the current (to // event
	 * starting at the same time) if
	 * (nextEventLocalStartDate.isEqual(eventZonedStartDate)) { if
	 * (nextEventLocalEndDate.isBefore(eventZonedEndDate)) { // the "next" event
	 * is "inside" the current // we need the next one to get end of freeTime
	 * for // the current Event // We can safety ignore the "next" event because
	 * it // can't have Free Time as it is inside the "current // one" throw new
	 * RuntimeException(
	 * "Two events starting at the same time, no handled (Yet) in FreeTime calculation"
	 * ); } } else if (nextEventLocalStartDate.isBefore(endDate)) { // start
	 * before or during the period if (nextEventLocalEndDate.isAfter(endDate)) {
	 * // Next events ends BEFORE the ends of the period. // The is FreeTme from
	 * end of "current Event" and // ends of period
	 * freeTimeAppender.add(nextEventLocalEndDate.toOffsetDateTime().
	 * toOffsetTime(), endDate.toLocalTime().atOffset(endDate.getOffset()),
	 * eventLocalStartDateDay); } } }
	 * freeTimeAppender.add(eventZonedEndDate.toLocalTime().atOffset(offset),
	 * nextEventLocalStartDate.toLocalTime().atOffset(offset),
	 * eventLocalStartDateDay); event = nextEvent; } else { if
	 * (itEvent.hasNext()) { event = itEvent.next(); } } } if
	 * (LOG.isDebugEnabled()) { LOG.debug(new
	 * StringBuilder().append(freeTime.size()).
	 * append(" freeTime period(s) found").toString()); } return freeTime; }
	 */

	protected ZonedDateTime getEventStartZoned(final T event) {
		return this.getDateHelper().fromEventDateTime(this.getEventStart(event));
	}

	/**
	 * Convert a provider (Event) Date to a standard Java 8 LocalDateTime (JSR
	 * 310)
	 *
	 * @param date
	 * @return
	 */
	protected ZonedDateTime getEventEndZoned(final T event) {
		return this.getDateHelper().fromEventDateTime(this.getEventEnd(event));
	}

	protected abstract D getEventStart(T event);

	protected abstract D getEventEnd(T event);

	public abstract String asLog(T event, Boolean maximumDetails);

	// public abstract D toDateTime(final ZonedDateTime dateTime);

	// protected abstract D toDateTime(final ZonedDateTime dateTime, final
	// ZoneId zoneId);

	protected Boolean isMySelf(final Long userId) {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final Long currentUser = appUserHelper.getCurrentUserId();
		return currentUser.equals(userId);
	}

	public abstract String getHmlLink(T event);

	public ZonedDateTime fromEventDateTime(final D date) {
		return this.getDateHelper().fromEventDateTime(date);
	}

	protected abstract D toProviderDateTime(ZonedDateTime date);

	public abstract T create(T newEvent, CalendarConfigurationFormData calendarToStoreEvent);
}
