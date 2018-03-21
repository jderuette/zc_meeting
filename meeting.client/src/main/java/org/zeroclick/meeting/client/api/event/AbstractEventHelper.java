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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.meeting.client.api.ProviderDateHelper;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.service.CalendarAviability;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
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

	@Override
	public CalendarAviability getCalendarAviability(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final Long userId, final AbstractCalendarConfigurationTableRowData calendar, final ZoneId userZoneId) {
		final List<T> allConcurentEvent = new ArrayList<>();
		allConcurentEvent.addAll(this.getEvents(startDate, endDate, userId, calendar));

		ZonedDateTime endLastEvent = null;
		List<DayDuration> freeTimes = null;
		if (!allConcurentEvent.isEmpty()) {
			freeTimes = this.getFreeTime(startDate, endDate, allConcurentEvent, userZoneId);
			final T lastEvent = this.getLastEvent(allConcurentEvent);
			endLastEvent = this.fromEventDateTime(this.getEventEnd(lastEvent));
		}

		return new CalendarAviability(endLastEvent, freeTimes);
	}

	public String asLog(final T event) {
		return this.asLog(event, Boolean.FALSE);
	}

	public abstract void sort(final List<T> events);

	private List<DayDuration> getFreeTime(final ZonedDateTime startDate, final ZonedDateTime endDate,
			final List<T> events, final ZoneId userZoneId) {
		final List<DayDuration> freeTime = new ArrayList<>();
		this.sort(events);
		final Iterator<T> itEvent = events.iterator();
		Boolean isFirstEvent = Boolean.TRUE;

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Searching for freeTime from : ").append(startDate).append(" to ")
					.append(endDate).append(" with : ").append(events.size()).append(" event(s) in period").toString());
		}

		T event = null;
		while (itEvent.hasNext()) {
			// next should be call only the first time, the end of the while
			// move to the next (to get start of the next event)
			if (isFirstEvent) {
				event = itEvent.next();
				isFirstEvent = Boolean.FALSE;
			}
			final ZonedDateTime eventZonedStartDate = this.getEventStartZoned(event);
			final DayOfWeek eventLocalStartDateDay = eventZonedStartDate.getDayOfWeek();
			final ZonedDateTime eventZonedEndDate = this.getEventEndZoned(event);
			if (eventZonedStartDate.isAfter(startDate)) {
				// freeTime from startDate to the beginning of the event
				freeTime.add(new DayDuration(startDate.toOffsetDateTime().toOffsetTime(),
						eventZonedEndDate.toLocalTime().atOffset(eventZonedEndDate.getOffset()),
						CollectionUtility.arrayList(eventLocalStartDateDay), Boolean.FALSE));
				if (itEvent.hasNext()) {
					event = itEvent.next();
				}
			} else {
				// freeTime from end of this event to begin of the next (if
				// this event ends before the endDate)
				if (eventZonedEndDate.isBefore(endDate)) {
					T nextEvent = null;
					if (itEvent.hasNext()) {
						nextEvent = itEvent.next();
					}
					ZonedDateTime nextEventLocalStartDate;
					ZoneOffset offset;
					if (null == nextEvent) {
						// no more event we are on the last one, this
						// freeTime ends at endDate
						nextEventLocalStartDate = endDate;
						offset = endDate.getOffset();
					} else {
						nextEventLocalStartDate = this.getEventStartZoned(nextEvent);
						offset = nextEventLocalStartDate.getOffset();
					}
					freeTime.add(new DayDuration(eventZonedEndDate.toLocalTime().atOffset(offset),
							nextEventLocalStartDate.toLocalTime().atOffset(offset),
							CollectionUtility.arrayList(eventLocalStartDateDay), Boolean.FALSE));
					event = nextEvent;
				} else {
					if (itEvent.hasNext()) {
						event = itEvent.next();
					}
				}
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append(freeTime.size()).append(" freeTime period(s) found").toString());
		}
		return freeTime;
	}

	protected ZonedDateTime getEventStartZoned(final T event) {
		return this.getDateHelper().fromEventDateTime(this.getEventStart(event));
	}

	/**
	 * Convert a Google (Event) Date to a standard Java 8 LocalDateTime (JSR
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
}
