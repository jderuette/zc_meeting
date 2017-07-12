package org.zeroclick.meeting.client.event;

import java.io.IOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.client.calendar.GoogleEventStartComparator;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.client.common.SlotHelper;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.client.event.EventTablePage.Table;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;

@Data(EventTablePageData.class)
public class EventTablePage extends AbstractEventsTablePage<Table> {

	private static final Logger LOG = LoggerFactory.getLogger(EventTablePage.class);

	private Integer nbEventToProcess = 0;

	protected Integer getNbEventToProcess() {
		return this.nbEventToProcess;
	}

	protected void setNbEventToProcess(final Integer nbEventToProcess) {
		this.nbEventToProcess = nbEventToProcess;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		final EventTablePageData pageData = BEANS.get(IEventService.class).getEventTableData(filter);
		this.importPageData(pageData);
		this.setNbEventToProcess(pageData.getRowCount());

		this.refreshTitle();
	}

	@Override
	protected String getConfiguredTitle() {
		return this.buildTitle();
	}

	protected String buildTitle() {
		return TEXTS.get("zc.meeting.events", null == this.nbEventToProcess ? "0" : this.nbEventToProcess.toString());
	}

	@Override
	protected boolean getConfiguredTableStatusVisible() {
		return Boolean.FALSE;
	}

	protected void incNbEventToProcess() {
		Integer currentNbEventToProcess = this.getNbEventToProcess();
		this.setNbEventToProcess(++currentNbEventToProcess);
		this.refreshTitle();
	}

	protected void decNbEventToProcess() {
		Integer currentNbEventToProcess = this.getNbEventToProcess();
		this.setNbEventToProcess(--currentNbEventToProcess);
		this.refreshTitle();
	}

	protected void refreshTitle() {
		final Cell cell = this.getCellForUpdate();
		cell.setText(this.buildTitle());
	}

	@Override
	protected void onNewEvent(final EventFormData formData) {
		LOG.debug("New event detected incrementing nb Event to Process");
		this.incNbEventToProcess();
	}

	@Override
	protected void onModifiedEvent(final EventFormData formData) {
		LOG.debug("Modified event detected changing nb Event to Process");
		if ("ASKED".equals(formData.getState())) {
			this.incNbEventToProcess();
		} else {
			this.decNbEventToProcess();
		}
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void execInitTable() {
			// this.importFromTableRowBeanData(rowDatas, rowType);
			// this.importPageData(BEANS.get(IEventService.class).getEventTableData(null));
		}

		@Override
		protected void initConfig() {
			super.initConfig();
			this.addDefaultFilters();
		}

		protected void addDefaultFilters() {
			if (null == this.getUserFilterManager()) {
				this.setUserFilterManager(this.createUserFilterManager());
			}
			if (this.getUserFilterManager().getFilter(this.getStateColumn().getColumnId()) == null) {
				final TextColumnUserFilterState askedFilter = new TextColumnUserFilterState(this.getStateColumn());
				final Set<Object> selectedValues = new HashSet<>();
				selectedValues.add(TEXTS.get("zc.meeting.state.asked"));
				askedFilter.setSelectedValues(selectedValues);
				// askedFilter.setFreeText(TEXTS.get("zc.meeting.state.asked"));
				this.getUserFilterManager().addFilter(askedFilter);
			}
		}

		@Override
		protected String getConfiguredTitle() {
			return EventTablePage.this.buildTitle();
		}

		@Override
		protected void execContentChanged() {
			this.autoFillDates();
			super.execContentChanged();
		}

		@Override
		protected void execDecorateRow(final ITableRow row) {
			// this.autoFillDates(row);
			super.execDecorateRow(row);
		}

		@Override
		protected void autoFillDates(final ITableRow row) {
			if (this.canAutofillDates(row)) {
				LOG.info("Calculating new Date (autofill) for row : " + row);
				try {
					this.changeDatesNext(row);
				} catch (final IOException e) {
					LOG.warn("Canno't auto calculate start/end meeting for row " + row, e);
				}
			}
		}

		protected void autoFillDates() {
			final List<ITableRow> rows = this.getRows();
			for (final ITableRow row : rows) {
				this.autoFillDates(row);
			}
		}

		protected void resetInvalidatesEvent(final ZonedDateTime start, final ZonedDateTime end) {
			final List<ITableRow> rows = this.getRows();
			for (final ITableRow row : rows) {
				this.invalidateIfSlotAlreadyUsed(row, start, end);
			}
		}

		private void invalidateIfSlotAlreadyUsed(final ITableRow row, final ZonedDateTime newAcceptedEventstart,
				final ZonedDateTime newAcceptedEventEnd) {
			final ZonedDateTime rowStart = this.getStartDateColumn().getZonedValue(row.getRowIndex());
			final ZonedDateTime rowEnd = this.getEndDateColumn().getZonedValue(row.getRowIndex());

			if (null != rowStart && null != rowEnd) {
				if (!rowStart.isBefore(newAcceptedEventstart) && !rowEnd.isAfter(rowEnd)) {
					this.getStartDateColumn().setValue(row.getRowIndex(), (Date) null);
					this.getEndDateColumn().setValue(row.getRowIndex(), (Date) null);
				}
			}
		}

		private void changeDatesNext() throws IOException {
			this.changeDatesNext(Table.this.getSelectedRow());
		}

		class DateReturn {
			private final ZonedDateTime start;
			private final ZonedDateTime end;
			private final Boolean created;

			public DateReturn(final ZonedDateTime recommandedStart) {
				this.start = recommandedStart;
				this.end = null;
				this.created = Boolean.FALSE;
			}

			public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate) {
				this.start = start;
				this.end = calculatedEndDate;
				this.created = Boolean.TRUE;
			}

			public ZonedDateTime getStart() {
				return this.start;
			}

			public ZonedDateTime getEnd() {
				return this.end;
			}

			public Boolean isCreated() {
				return this.created;
			}
		}

		protected ZonedDateTime getUserNow(final Long userId) {
			return ZonedDateTime.now(Clock.system(this.getUserZoneId(userId)));
		}

		protected void changeDatesNext(final ITableRow row) throws IOException {
			final Long eventId = this.getEventIdColumn().getValue(row.getRowIndex());
			final Long guestId = this.getGuestIdColumn().getValue(row.getRowIndex());

			final ZonedDateTime currentStartDate = this.toZonedDateTime(
					this.getStartDateColumn().getValue(row.getRowIndex()), this.getUserZoneId(guestId));

			LOG.debug("Changing Next date for Event ID : " + eventId + " with start Date : " + currentStartDate);

			ZonedDateTime nextStartDate = null;
			DateReturn newPossibleDate;
			if (null == currentStartDate) {
				nextStartDate = this.addReactionTime(this.getUserNow(guestId));
			} else {
				// TODO Djer13 : try to stick endOfPeriod if (Set next.end to
				// period.end, if next.start is after previous.start this a
				// valid event period) ??
				nextStartDate = this.addReactionTime(currentStartDate.plus(Duration.ofMinutes(45)));
			}

			newPossibleDate = this.tryChangeDatesNext(nextStartDate, row);

			while (!newPossibleDate.isCreated()) {
				// check if the timeSlot available in Calendars
				// if not, try with the new available start date (in
				// calendars)
				if (EventTablePage.this.callTracker.canIncrementNbCall(eventId)) {
					newPossibleDate = this.tryChangeDatesNext(newPossibleDate.getStart(), row);
				} else {
					throw new VetoException(TEXTS.get("zc.meeting.googleTooManyCall"));
				}
			}
			EventTablePage.this.callTracker.resetNbCall(eventId);

			if (newPossibleDate.isCreated()) {
				// update start and end date
				row.setCellValue(this.getStartDateColumn().getColumnIndex(), this.toDate(newPossibleDate.getStart()));
				this.getStartDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));
				row.setCellValue(this.getEndDateColumn().getColumnIndex(), this.toDate(newPossibleDate.getEnd()));
				this.getEndDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));
			}
		}

		private DateReturn tryChangeDatesNext(final ZonedDateTime startDate, final ITableRow row) throws IOException {
			final Integer rowIndex = row.getRowIndex();
			final DateReturn newPossibleDate = this.tryChangeDatesNext(startDate,
					this.getDurationColumn().getValue(rowIndex), this.getSlotColumn().getValue(rowIndex),
					this.getOrganizerColumn().getValue(rowIndex), this.getGuestIdColumn().getValue(rowIndex));

			return newPossibleDate;
		}

		private Integer getReactionDelayMin() {
			// TODO Djer13 allow configuration of reactTimeDuration
			final Integer reactionDelayMin = 10;
			return reactionDelayMin;
		}

		protected ZonedDateTime addReactionTime(final ZonedDateTime date) {
			return this.addReactionTime(date, this.getReactionDelayMin());
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
			ZonedDateTime minimalStart = ZonedDateTime.now().plus(Duration.ofMinutes(reactionDelayMin));
			minimalStart = this.roundToNextHourQuarter(minimalStart);
			if (date.isBefore(minimalStart)) {
				LOG.debug(date + " is too close with reactionTime of " + reactionDelayMin + " mins. Using : "
						+ minimalStart);
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
			LOG.debug(minutes + " rounded to (next quarter) : " + newMins);
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

		/**
		 * Try to provide new valid start/end date for user (by updating cells
		 * in the current selected row)
		 *
		 * @param startDate
		 *            the minimum start date to search a new valid date
		 * @return a new recommend date to perform a new search, null if a valid
		 *         date is found
		 * @throws IOException
		 */
		protected DateReturn tryChangeDatesNext(final ZonedDateTime startDate, final Integer selectEventDuration,
				final Integer selectSlotId, final Long organizerUserId, final Long guestUserId) throws IOException {
			LOG.info("Checking to create an event starting at " + startDate);

			final ZonedDateTime nextEndDate = startDate.plus(Duration.ofMinutes(selectEventDuration));

			if (!SlotHelper.get().isInOneOfPeriods(selectSlotId, startDate, nextEndDate)) {
				return new DateReturn(SlotHelper.get().getNextValidDateTime(selectSlotId, startDate, nextEndDate));
			}

			// check guest (current connected user) calendars
			final ZonedDateTime calendareRecommendedDate = this.tryCreateEvent(startDate, nextEndDate,
					Duration.ofMinutes(selectEventDuration), guestUserId);
			if (calendareRecommendedDate != null) {
				return new DateReturn(this.addReactionTime(calendareRecommendedDate));
			}

			// Check organizer calendars
			final ZonedDateTime organizerCalendareRecommendedDate = this.tryCreateEvent(startDate, nextEndDate,
					Duration.ofMinutes(selectEventDuration), organizerUserId);
			if (organizerCalendareRecommendedDate != null) {
				return new DateReturn(this.addReactionTime(organizerCalendareRecommendedDate));
			}

			return new DateReturn(startDate, nextEndDate);
		}

		/**
		 * Try to create an event in (Google) calendar for user userId
		 *
		 * @param startDate
		 * @param endDate
		 * @param eventDuration
		 * @param userId
		 * @return
		 * @throws IOException
		 */
		private ZonedDateTime tryCreateEvent(final ZonedDateTime startDate, final ZonedDateTime endDate,
				final Duration eventDuration, final Long userId) throws IOException {
			LOG.info("Cheking for (Google) calendars events from : " + startDate + " to " + endDate + " for user : "
					+ userId);

			final GoogleApiHelper googleHelper = GoogleApiHelper.get();
			Calendar googleCalendarService;
			ZonedDateTime recommendedNewDate = null;

			try {
				googleCalendarService = googleHelper.getCalendarService(userId);
			} catch (final UserAccessRequiredException uare) {
				throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
			}

			// getEvent from start to End for each calendar
			final List<CalendarListEntry> synchronizedCalendars = CollectionUtility
					.arrayList(googleCalendarService.calendarList().get("primary").execute());

			// final DateTime googledStartDate = new
			// DateTime(Date.from(startDate.toInstant(ZoneOffset.UTC)),
			// TimeZone.getTimeZone(ZoneOffset.UTC));
			// final DateTime googledEndDate = new
			// DateTime(Date.from(endDate.toInstant(ZoneOffset.UTC)),
			// TimeZone.getTimeZone(ZoneOffset.UTC));

			final ZoneId userZoneId = this.getUserZoneId(userId);

			final DateTime googledStartDate = googleHelper.toDateTime(startDate);
			final DateTime googledEndDate = googleHelper.toDateTime(endDate);

			final List<Event> allConcurentEvent = new ArrayList<>();
			for (final CalendarListEntry calendar : synchronizedCalendars) {
				final Events events = googleCalendarService.events().list(calendar.getId()).setMaxResults(50)
						.setTimeMin(googledStartDate).setTimeMax(googledEndDate).setOrderBy("startTime")
						.setSingleEvents(true).execute();
				allConcurentEvent.addAll(events.getItems());
			}
			// if no exiting event, a new one can be created
			if (allConcurentEvent.isEmpty()) {
				LOG.info(
						"No event found in calendars from : " + startDate + " to " + endDate + " for user : " + userId);
				// Do nothing special, recommendedNewDate = null meaning
				// provided periods is OK
			} else {
				// else try to find a new appropriate new startDate
				LOG.debug(allConcurentEvent.size() + " event(s) found in calendars from : " + startDate + " to "
						+ endDate + " for user : " + userId);
				final List<DayDuration> freeTimes = this.getFreeTime(startDate, endDate, allConcurentEvent, userZoneId);

				if (!freeTimes.isEmpty()) {
					LOG.debug("FreeTime found in calendars from : " + startDate + " to " + endDate + " with periods : "
							+ freeTimes);
					recommendedNewDate = SlotHelper.get().getNextValidDateTime(freeTimes, startDate, endDate);
					if (null != recommendedNewDate) {
						LOG.info("Recommanding new search from : " + recommendedNewDate + " (cause : " + userId
								+ " has blocking event(s) but freeTime)");
					}
				}
				if (null == recommendedNewDate) {
					LOG.debug("No avilable periods found in freeTime from : " + startDate + " to " + endDate
							+ " for user : " + userId);
					// The new potential start is the end of the last event
					final Event lastEvent = this.getLastEvent(allConcurentEvent);
					LOG.debug("Last (Google) blocking event " + googleHelper.aslog(lastEvent));
					final ZonedDateTime endLastEvent = googleHelper.fromEventDateTime(lastEvent.getEnd());
					// TODO Djer13 is required to add 1 minute ?
					recommendedNewDate = endLastEvent.plus(Duration.ofMinutes(1));
					LOG.info("Recommanding new search from : " + recommendedNewDate + " (cause : " + userId
							+ " whole period blocked by " + allConcurentEvent.size() + " event(s)");
				}
			}

			return recommendedNewDate;
		}

		private Event getLastEvent(final List<Event> allConcurentEvent) {
			final Event lastEvent = allConcurentEvent.get(allConcurentEvent.size() - 1);
			return lastEvent;
		}

		private List<DayDuration> getFreeTime(final ZonedDateTime startDate, final ZonedDateTime endDate,
				final List<Event> events, final ZoneId userZoneId) {
			final List<DayDuration> freeTime = new ArrayList<>();
			events.sort(new GoogleEventStartComparator());
			final Iterator<Event> itEvent = events.iterator();
			Boolean isFirstEvent = Boolean.TRUE;

			LOG.debug("Searching for freeTime from : " + startDate + " to " + endDate + " with : " + events.size()
					+ " event(s) in period");

			Event event = null;
			while (itEvent.hasNext()) {
				// next should be call only the first time, the end of the while
				// move to the next (to get start of the next event)
				if (isFirstEvent) {
					event = itEvent.next();
					isFirstEvent = Boolean.FALSE;
				}
				final ZonedDateTime eventZonedStartDate = GoogleApiHelper.get().fromEventDateTime(event.getStart());
				final DayOfWeek eventLocalStartDateDay = eventZonedStartDate.getDayOfWeek();
				if (eventZonedStartDate.isAfter(startDate)) {
					// freeTime from startDate to the beginning of the event
					freeTime.add(new DayDuration(startDate.toOffsetDateTime().toOffsetTime(),
							eventZonedStartDate.toLocalTime()
									.atOffset(GoogleApiHelper.get().timeOffset(event.getStart())),
							CollectionUtility.arrayList(eventLocalStartDateDay), Boolean.FALSE));
					if (itEvent.hasNext()) {
						event = itEvent.next();
					}
				} else {
					// freeTime from end of this event to begin of the next (if
					// this event ends before the endDate)
					final ZonedDateTime eventZonedEndDate = GoogleApiHelper.get().fromEventDateTime(event.getEnd());
					if (eventZonedEndDate.isBefore(endDate)) {
						Event nextEvent = null;
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
							nextEventLocalStartDate = GoogleApiHelper.get().fromEventDateTime(nextEvent.getStart());
							offset = GoogleApiHelper.get().timeOffset(nextEvent.getStart());
						}
						freeTime.add(new DayDuration(eventZonedStartDate.toLocalTime().atOffset(offset),
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
			LOG.debug(freeTime.size() + " freeTime periods found");
			return freeTime;
		}

		private Event createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long forUserId,
				final String withEmail, final String subject) throws IOException {
			LOG.debug("Creating (Google) Event from : " + startDate + " to " + endDate + ", for :" + forUserId + "("
					+ withEmail + ")");

			final GoogleApiHelper googleHelper = GoogleApiHelper.get();

			Calendar googleCalendarService;

			try {
				googleCalendarService = googleHelper.getCalendarService(forUserId);
			} catch (final UserAccessRequiredException uare) {
				throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
			}

			final Event newEvent = new Event();
			newEvent.setStart(googleHelper.toEventDateTime(startDate));
			newEvent.setEnd(googleHelper.toEventDateTime(endDate));
			newEvent.setSummary("Meeting with : " + withEmail);
			newEvent.setDescription(subject);

			final EventAttendee[] attendees = new EventAttendee[] { new EventAttendee().setEmail(withEmail) };
			newEvent.setAttendees(Arrays.asList(attendees));

			final Event createdEvent = googleCalendarService.events().insert("primary", newEvent).execute();

			LOG.info("(Google) Event created  with id : " + createdEvent.getId() + " from " + startDate + " to "
					+ endDate + ", for : " + forUserId + " ICalUID : " + createdEvent.getICalUID() + " link : "
					+ createdEvent.getHtmlLink());

			return createdEvent;
		}

		@Order(1500)
		public class EditMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.common.edit");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Pencil;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			// private Boolean isWorkFlowVisible() {
			// final String currentState = (String)
			// EventTablePage.this.getTable().getSelectedRow()
			// .getCell(Table.this.getStateColumn()).getValue();
			// return "ASKED".equals(currentState);
			// }

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final AccessControlService acs = BEANS.get(AccessControlService.class);

				final Long rowId = Table.this.getEventIdColumn().getSelectedValue();
				// final Boolean isHeld =
				// Table.this.isHeldByCurrentUser(EventTablePage.this.getTable().getSelectedRow());
				// this.setVisible(this.isWorkFlowVisible() && (isHeld || acs
				// .getPermissionLevel(new UpdateEventPermission(rowId)) >=
				// UpdateEventPermission.LEVEL_ALL));
				// Only for admin
				this.setVisible(
						acs.getPermissionLevel(new UpdateEventPermission(rowId)) >= UpdateEventPermission.LEVEL_ALL);

				this.setEnabled(EventTablePage.this.isUserCalendarConfigured());
			}

			@Override
			protected void execAction() {
				final EventForm form = new EventForm();
				final Long currentEventId = Table.this.getEventIdColumn().getSelectedValue();
				form.setEventId(currentEventId);
				form.addFormListener(new EventFormListener());
				form.setEnabledPermission(new UpdateEventPermission(currentEventId));
				// start the form using its modify handler
				form.startModify();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "e");
			}
		}

		@Order(2000)
		public class NewMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("New");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.EmptySpace);
			}

			@Override
			protected void execInitAction() {
				this.setVisiblePermission(new CreateEventPermission());
			}

			@Override
			protected boolean getConfiguredVisible() {
				return EventTablePage.this.isUserCalendarConfigured();
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				this.setVisible(EventTablePage.this.isUserCalendarConfigured());
			}

			@Override
			protected void execAction() {
				final EventForm form = new EventForm();
				// form.addFormListener(new EventFormListener());
				form.setEnabledPermission(new CreateEventPermission());
				form.startNew();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "n");
			}
		}

		@Order(2000)
		public class AcceptMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.Accept");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Calendar;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			private Boolean isWorkflowVisible(final String currentState) {
				Boolean isVisible = Boolean.FALSE;
				if ("ASKED".equals(currentState)) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(Table.this.userCanAccept(row) && this.isWorkflowVisible(Table.this.getState(row))
							&& Table.this.isGuestCurrentUser(row));

					final Boolean hasStartDate = null != Table.this.getStartDateColumn().getValue(row.getRowIndex());
					this.setEnabled(hasStartDate && EventTablePage.this.isUserCalendarConfigured());

					if (hasStartDate) {
						this.setTooltipText(null);
					} else {
						this.setTooltipText(TEXTS.get("zc.meeting.accept.require.startDate"));
					}
				}
			}

			@Override
			protected void execAction() {
				final IUserService userService = BEANS.get(IUserService.class);
				final ZonedDateTime start = Table.this.getStartDateColumn().getSelectedZonedValue();
				final ZonedDateTime end = Table.this.getEndDateColumn().getSelectedZonedValue();
				Long eventHeldBy = Table.this.getOrganizerColumn().getSelectedValue();
				final String eventHeldEmail = Table.this.getOrganizerEmailColumn().getSelectedValue();
				Long eventGuest = Table.this.getGuestIdColumn().getSelectedValue();
				final String eventGuestEmail = Table.this.getEmailColumn().getSelectedValue();
				final String subject = Table.this.getSubjectColumn().getSelectedValue();

				try {
					if (null == start || null == end) {
						Table.this.changeDatesNext();
						throw new VetoException(TEXTS.get("zc.meeting.chooseDateFirst"));
					}

					if (null == eventGuest) {
						eventGuest = userService.getUserIdByEmail(eventGuestEmail);
					}
					// external event for guest
					final Event externalGuestEvent = Table.this.createEvent(start, end, eventGuest, eventHeldEmail,
							subject);
					Table.this.getExternalIdRecipientColumn().setValue(Table.this.getSelectedRow(),
							externalGuestEvent.getId());

					this.sendConfirmationEmail(eventGuestEmail, externalGuestEvent);

					if (null == eventHeldBy) {
						eventHeldBy = userService.getUserIdByEmail(eventHeldEmail);
					}
					// External event for holder
					final Event externalOrganizerEvent = Table.this.createEvent(start, end, eventHeldBy,
							eventGuestEmail, subject);
					Table.this.getExternalIdOrganizerColumn().setValue(Table.this.getSelectedRow(),
							externalOrganizerEvent.getId());
					this.sendConfirmationEmail(eventHeldEmail, externalOrganizerEvent);

					Table.this.getStateColumn().setValue(Table.this.getSelectedRow(), "ACCEPTED");

					Table.this.saveEventCurrentRow();

					Table.this.resetInvalidatesEvent(start, end);
					Table.this.autoFillDates();

				} catch (final IOException e) {
					throw new VetoException("Canno't get calendar details, re-try later");
				}
			}

			private void sendConfirmationEmail(final String recipient, final Event event) {
				final IMailSender mailSender = BEANS.get(IMailSender.class);
				final String subject = "ZeroClick Meeting : meeting Confirmation";
				final String content = "Confirmation : " + event.getHtmlLink()
						+ " <br /> <br />Best Regards<br /> ZeroClickMeeting Team";
				try {
					mailSender.sendEmail(recipient, subject, content);
				} catch (final MailException e) {
					throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
				}
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "v");
			}
		}

		@Order(3000)
		public class NextMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.next");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.LongArrowRight;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			private Boolean isWorkflowVisible(final String currentState) {
				Boolean isVisible = Boolean.FALSE;
				if ("ASKED".equals(currentState)) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(Table.this.userCanChooseDate(row)
							&& this.isWorkflowVisible(Table.this.getState(row)) && Table.this.isGuestCurrentUser(row));
					this.setEnabled(EventTablePage.this.isUserCalendarConfigured() && Table.this.isUserTimeZoneValid());
				}
			}

			@Override
			protected void execAction() {
				try {
					Table.this.changeDatesNext();
					Table.this.reloadMenus(Table.this.getSelectedRow());
				} catch (final IOException e) {
					throw new VetoException("Canno't get calendar details, re-try later");
				}
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.RIGHT);
			}
		}

		private class EventFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent e) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == e.getType() && e.getForm().isFormStored()) {
					EventTablePage.this.reloadPage();
				}
			}
		}

	}
}
