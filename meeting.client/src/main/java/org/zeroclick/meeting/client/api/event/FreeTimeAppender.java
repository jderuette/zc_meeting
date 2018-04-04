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
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.zeroclick.meeting.client.common.DayDuration;

/**
 * @author djer Helper to manage FreeTime in period
 */
public class FreeTimeAppender {
	List<DayDuration> freeTimes;

	List<DayDuration> eventTimeLine;

	private final ZonedDateTime periodStart;
	private final ZonedDateTime periodEnd;
	private final DayOfWeek validDayOfWeek;

	public FreeTimeAppender(final ZonedDateTime periodStart, final ZonedDateTime periodEnd,
			final DayOfWeek validDayOfWeek) {
		this.freeTimes = new ArrayList<>();
		this.eventTimeLine = new ArrayList<>();
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.validDayOfWeek = validDayOfWeek;
	}

	/**
	 * Add FreeTime to existing FreeTimes if possible
	 *
	 * @param start
	 * @param end
	 * @param validDayOfWeek
	 * @return true if New FreeTime added, else false (already exist)
	 */
	public Boolean add(final OffsetTime start, final OffsetTime end) {
		final DayDuration dayDuration = new DayDuration(start, end, CollectionUtility.arrayList(this.validDayOfWeek),
				Boolean.FALSE);
		return this.add(dayDuration);
	}

	public void addEvent(final ZonedDateTime eventZonedStartDate, final ZonedDateTime eventZonedEndDate,
			final DayOfWeek validDayOfWeek) {
		final ZonedDateTime eventStartInPeriod = eventZonedStartDate.isBefore(this.periodStart) ? this.periodStart
				: eventZonedStartDate;
		final ZonedDateTime eventEndInPeriod = eventZonedEndDate.isAfter(this.periodEnd) ? this.periodEnd
				: eventZonedEndDate;
		this.eventTimeLine.add(new DayDuration(this.toOffsetTime(eventStartInPeriod),
				this.toOffsetTime(eventEndInPeriod), CollectionUtility.arrayList(validDayOfWeek)));
	}

	public Boolean add(final DayDuration newFreeTime) {
		Boolean isAdded = Boolean.FALSE;

		if (null != newFreeTime && null != newFreeTime.getStart() && null != newFreeTime.getEnd()) {
			if (!this.freeTimes.contains(newFreeTime)) {
				// TODO Djer13 check if other FreeTime period already contain
				// this "new" FreeTime
				this.freeTimes.add(newFreeTime);
				isAdded = Boolean.TRUE;
			}
		}
		return isAdded;
	}

	public List<DayDuration> getFreeTimes() {
		final List<DayDuration> freeTimes = new ArrayList<>();
		final List<DayDuration> flattenedEvents = this.getFlattenEventTimeLine();
		OffsetTime previousEnd = null;
		OffsetTime previousStart = null;

		OffsetTime nextEnd = null;
		OffsetTime nextStart = null;
		Boolean isFirstLoop = Boolean.TRUE;

		// search for Free Time in simplified EventTimeLine
		final ListIterator<DayDuration> itFlattenedEvents = flattenedEvents.listIterator();

		DayDuration nextCleanEvent = null;
		DayDuration cleanEvent = null;
		while (itFlattenedEvents.hasNext()) {
			if (isFirstLoop) {
				cleanEvent = itFlattenedEvents.next();
				// init previous values
				previousEnd = this.toOffsetTime(this.periodStart);
				previousStart = this.toOffsetTime(this.periodStart);

				// init next values
				if (null == nextCleanEvent) {
					if (itFlattenedEvents.hasNext()) {
						nextCleanEvent = itFlattenedEvents.next();
						nextEnd = nextCleanEvent.getEnd();
						nextStart = nextCleanEvent.getStart();
					} else {
						nextEnd = this.toOffsetTime(this.periodEnd);
						nextStart = this.toOffsetTime(this.periodEnd); // ??
					}
				}
			} else {
				cleanEvent = nextCleanEvent;
				if (itFlattenedEvents.hasNext()) {
					nextCleanEvent = itFlattenedEvents.next();
					nextEnd = nextCleanEvent.getEnd();
					nextStart = nextCleanEvent.getStart();
				}
			}
			if (isFirstLoop && cleanEvent.getStart().isAfter(previousEnd)) {
				freeTimes.add(new DayDuration(previousEnd, cleanEvent.getStart(),
						CollectionUtility.arrayList(cleanEvent.getValidDayOfWeek())));
			}
			if (cleanEvent.getEnd().isBefore(nextStart)) {
				freeTimes.add(new DayDuration(cleanEvent.getEnd(), nextStart,
						CollectionUtility.arrayList(cleanEvent.getValidDayOfWeek())));
			}

			previousEnd = cleanEvent.getEnd();
			previousStart = cleanEvent.getStart();

			if (isFirstLoop) {
				isFirstLoop = Boolean.FALSE;
			}
		}

		return freeTimes;
	}

	public List<DayDuration> getFlattenEventTimeLine() {
		final List<DayDuration> flattenedEvents = new ArrayList<>();
		final OffsetTime offsetPeriodStart = this.toOffsetTime(this.periodStart);
		final OffsetTime offsetPeriodEnd = this.toOffsetTime(this.periodEnd);
		final List<DayOfWeek> validDays = CollectionUtility.arrayList(this.validDayOfWeek);
		Boolean isFirstEvent = Boolean.TRUE;
		DayDuration eventTOAdd = null;

		if (null == this.eventTimeLine) {
			// whole period available
			this.freeTimes.add(new DayDuration(offsetPeriodStart, offsetPeriodEnd, validDays));
		} else {

			for (final DayDuration simpleEvent : this.eventTimeLine) {
				if (isFirstEvent) {
					isFirstEvent = Boolean.FALSE;
					eventTOAdd = simpleEvent;
				} else {

					if (null != eventTOAdd) {
						// previous loop need to add itself
						flattenedEvents.add(eventTOAdd);
						eventTOAdd = null;
					}
					// check if the current event need to extends an existing
					// "flatenned Event"

					final ListIterator<DayDuration> ItFlattenedEvents = flattenedEvents.listIterator();

					while (ItFlattenedEvents.hasNext()) {
						final DayDuration flattenedEvent = ItFlattenedEvents.next();
						// current Event is inside a Flattened Event
						if (flattenedEvent.isInPeriod(simpleEvent.getStart().toLocalTime(),
								simpleEvent.getEnd().toLocalTime())) {
							// ignore currentEvent
							eventTOAdd = null;
							continue;
						}
						// Current Event enclose a flattened Event
						if (simpleEvent.isInPeriod(flattenedEvent.getStart().toLocalTime(),
								flattenedEvent.getEnd().toLocalTime())) {
							// FlatenedEvent change start/end to current Event
							flattenedEvent.setStart(simpleEvent.getStart());
							flattenedEvent.setEnd(simpleEvent.getEnd());
							eventTOAdd = null;
						} else if (simpleEvent.isInPeriod(flattenedEvent.getStart().toLocalTime())) {
							// this event start in the flattened Event (and ends
							// after, because no "inside" or "enclosed" occurs)
							flattenedEvent.setStart(simpleEvent.getStart());
							eventTOAdd = null;
						} else if (simpleEvent.isInPeriod(flattenedEvent.getEnd().toLocalTime())) {
							// this event ends in the flattened Event (and
							// starts before, because no "inside" or "enclosed"
							// occurs)
							flattenedEvent.setEnd(simpleEvent.getEnd());
							eventTOAdd = null;
						} else {
							// this event create a new FlattenEvent inside the
							// period
							eventTOAdd = simpleEvent;
						}
					}
				}
			}
			// if last Event need to be added
			if (null != eventTOAdd) {
				flattenedEvents.add(eventTOAdd);
			}
		}

		return flattenedEvents;
	}

	protected OffsetTime toOffsetTime(final ZonedDateTime date) {
		return date.toLocalTime().atOffset(date.getOffset());
	}
}