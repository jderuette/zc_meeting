package org.zeroclick.meeting.client.event;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.client.user.ValidateCpsForm;
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationEnvProperty;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.calendar.GoogleEventStartComparator;
import org.zeroclick.meeting.client.common.DayDuration;
import org.zeroclick.meeting.client.common.SlotHelper;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.client.event.EventTablePage.Table;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.StateCodeType;
import org.zeroclick.ui.action.menu.AbstractNewMenu;
import org.zeroclick.ui.action.menu.AbstractValidateMenu;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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

		super.getTable().autoFillDates();
	}

	@Override
	protected String getConfiguredTitle() {
		return this.buildTitle();
	}

	protected String buildTitle() {
		return TEXTS.get("zc.meeting.events",
				null == this.getNbEventToProcess() ? "0" : this.getNbEventToProcess().toString());
	}

	@Override
	protected boolean getConfiguredTableStatusVisible() {
		return Boolean.FALSE;
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.LongArrowRight;
	}

	protected void incNbEventToProcess() {
		Integer nbEventToProcess = this.getNbEventToProcess();
		this.setNbEventToProcess(++nbEventToProcess);
		this.refreshTitle();
	}

	protected void decNbEventToProcess() {
		Integer nbEventToProcess = this.getNbEventToProcess();
		this.setNbEventToProcess(--nbEventToProcess);
		this.refreshTitle();
	}

	protected void refreshTitle() {
		final Cell cell = this.getCellForUpdate();
		cell.setText(this.buildTitle());
	}

	@Override
	protected Boolean canHandleNew(final AbstractEventNotification notification) {
		final EventFormData formData = notification.getEventForm();
		return !this.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(StateCodeType.AskedCode.ID, formData.getState().getValue());
	}

	@Override
	protected Boolean canHandleModified(final AbstractEventNotification notification) {
		final EventFormData formData = notification.getEventForm();
		return !this.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(StateCodeType.AskedCode.ID, formData.getState().getValue());
	}

	@Override
	protected Boolean canHandle(final CalendarConfigurationCreatedNotification notification) {
		return Boolean.TRUE;
	}

	@Override
	protected Boolean canHandle(final CalendarConfigurationModifiedNotification notification) {
		return Boolean.TRUE;
	}

	@Override
	protected Boolean canHandle(final CalendarsConfigurationModifiedNotification notification) {
		return Boolean.TRUE;
	}

	@Override
	protected Boolean canHandle(final CalendarsConfigurationCreatedNotification notification) {
		return Boolean.TRUE;
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected boolean getConfiguredRowIconVisible() {
			return true;
		}

		@Override
		protected String getConfiguredDefaultIconId() {
			return Icons.Null;
		}

		@Override
		protected void execInitTable() {
			// this.importFromTableRowBeanData(rowDatas, rowType);
			// this.importPageData(BEANS.get(IEventService.class).getEventTableData(null));
		}

		@Override
		protected void initConfig() {
			super.initConfig();
			// this.addDefaultFilters();
			this.getOrganizerEmailColumn().setVisible(Boolean.TRUE);
			this.getStartDateColumn().setVisible(Boolean.TRUE);
			this.getEndDateColumn().setVisible(Boolean.TRUE);
		}

		@Override
		public void deleteRow(final ITableRow row) {
			super.deleteRow(row);
			EventTablePage.this.decNbEventToProcess();
		}

		@Override
		public ITableRow addRow(final ITableRow newRow, final boolean markAsInserted) {
			final ITableRow insertedRow = super.addRow(newRow, markAsInserted);

			EventTablePage.this.incNbEventToProcess();
			return insertedRow;
		}

		@Override
		protected String getConfiguredTitle() {
			return EventTablePage.this.buildTitle();
		}

		/**
		 * Set the row Tooltip text in a new ModelJob too avoid LOOP
		 *
		 * @param row
		 * @param message
		 */
		protected void setRowTooltip(final ITableRow row, final String message) {
			ModelJobs.schedule(new IRunnable() {

				@Override
				public void run() {
					row.setTooltipText(message);

				}
			}, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
		}

		protected void removeRowToolTips(final ITableRow row) {
			ModelJobs.schedule(new IRunnable() {

				@Override
				public void run() {
					row.setTooltipText(null);

				}
			}, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
		}

		// @Override
		// protected void execContentChanged() {
		// this.autoFillDates();
		// super.execContentChanged();
		// }

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
					this.changeDatesNext(row, Boolean.FALSE);
					final Date calculatedStartDate = this.getStartDateColumn().getValue(row.getRowIndex());
					if (null == calculatedStartDate) {
						final Long eventId = this.getEventIdColumn().getValue(row.getRowIndex());
						LOG.debug("No auto-filled dates for event ID : " + eventId + " Adding to processedList");
						EventTablePage.this.addEventProcessedWithoutDates(eventId);
					}
				} catch (final IOException e) {
					LOG.warn("Canno't auto calculate start/end meeting for row " + row, e);
				}
			}
		}

		private void changeDatesNext(final ZonedDateTime newStart) throws IOException {
			this.changeDatesNext(Table.this.getSelectedRow(), newStart, Boolean.TRUE);
		}

		private void changeDatesNext() throws IOException {
			this.changeDatesNext(Table.this.getSelectedRow(), Boolean.TRUE);
		}

		class DateReturn {
			private ZonedDateTime start;
			private final ZonedDateTime end;
			private final Boolean created;
			private final Boolean loopInDates;
			private Boolean noAvailableDate;

			public DateReturn(final ZonedDateTime recommandedStart) {
				this.start = recommandedStart;
				this.end = null;
				this.created = Boolean.FALSE;
				this.loopInDates = Boolean.FALSE;
				this.noAvailableDate = Boolean.FALSE;
			}

			public DateReturn(final ZonedDateTime recommandedStart, final Boolean loopInDates) {
				this.start = recommandedStart;
				this.end = null;
				this.created = Boolean.FALSE;
				this.loopInDates = loopInDates;
				this.noAvailableDate = Boolean.FALSE;
			}

			public DateReturn(final ZonedDateTime recommandedStart, final Boolean loopInDates,
					final Boolean noAvailableDate) {
				this.start = recommandedStart;
				this.end = null;
				this.created = Boolean.FALSE;
				this.loopInDates = loopInDates;
				this.noAvailableDate = noAvailableDate;
			}

			public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate) {
				this.start = start;
				this.end = calculatedEndDate;
				this.created = Boolean.TRUE;
				this.loopInDates = Boolean.FALSE;
				this.noAvailableDate = Boolean.FALSE;
			}

			public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate,
					final Boolean loopInDates) {
				this.start = start;
				this.end = calculatedEndDate;
				this.created = Boolean.TRUE;
				this.loopInDates = loopInDates;
				this.noAvailableDate = Boolean.FALSE;
			}

			public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate,
					final Boolean loopInDates, final Boolean noAvailableDate) {
				this.start = start;
				this.end = calculatedEndDate;
				this.created = Boolean.TRUE;
				this.loopInDates = loopInDates;
				this.noAvailableDate = noAvailableDate;
			}

			public ZonedDateTime getStart() {
				return this.start;
			}

			public void setStart(final ZonedDateTime start) {
				this.start = start;
			}

			public ZonedDateTime getEnd() {
				return this.end;
			}

			public Boolean isCreated() {
				return this.created;
			}

			public Boolean isLoopInDates() {
				return this.loopInDates;
			}

			public Boolean isNoAvailableDate() {
				return this.noAvailableDate;
			}

			public void setNoAvailableDate(final Boolean noAvailableDate) {
				this.noAvailableDate = noAvailableDate;
			}
		}

		protected void changeDatesNext(final ITableRow row, final Boolean askedByUser) throws IOException {
			final ZonedDateTime startDate;
			final Long guestId = this.getGuestIdColumn().getValue(row.getRowIndex());
			final ZonedDateTime currentStartDate = EventTablePage.this.getDateHelper().getZonedValue(
					EventTablePage.this.getAppUserHelper().getUserZoneId(guestId),
					this.getStartDateColumn().getValue(row.getRowIndex()));

			if (null != currentStartDate) {
				startDate = currentStartDate.plus(Duration.ofMinutes(45));
			} else {
				startDate = EventTablePage.this.getAppUserHelper().getUserNow(guestId);
			}

			this.changeDatesNext(row, startDate, askedByUser);

		}

		protected void changeDatesNext(final ITableRow row, final ZonedDateTime newStartDate, final Boolean askedByUser)
				throws IOException {
			final Long eventId = this.getEventIdColumn().getValue(row.getRowIndex());
			Boolean hasLooped = Boolean.FALSE;

			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			LOG.debug("Changing Next date for Event ID : " + eventId + " with start Date : " + newStartDate);

			final ZonedDateTime nextStartDate = this.addReactionTime(newStartDate);
			DateReturn newPossibleDate;

			newPossibleDate = this.tryChangeDatesNext(nextStartDate, row);

			while (!newPossibleDate.isCreated()) {
				// check if the timeSlot available in Calendars
				// if not, try with the new available start date (in
				// calendars)
				if (EventTablePage.this.callTracker.canIncrementNbCall(eventId)) {
					if (newPossibleDate.isNoAvailableDate()) {
						// reached end of available date (after maximal)
						// and the new proposed date is after the one currently
						// displayed, so now over date are available

						notificationHelper.addErrorNotification("zc.meeting.notification.NoAvailableNextDate");
						row.setIconId(Icons.ExclamationMark);
						return;// break new Date search
					}
					newPossibleDate = this.tryChangeDatesNext(newPossibleDate.getStart(), row);
					if (!hasLooped && newPossibleDate.isLoopInDates()) {
						hasLooped = Boolean.TRUE;
					}
				} else {
					if (askedByUser) {
						final int continueSearch = MessageBoxes.createYesNo()
								.withHeader(TEXTS.get("zc.meeting.event.maxSearchReached.title"))
								.withBody(TEXTS.get("zc.meeting.event.maxSearchReached.message",
										EventTablePage.this.getDateHelper().format(newPossibleDate.getStart())))
								.withYesButtonText(TEXTS.get("YesButton")).withIconId(Icons.ExclamationMark)
								.withSeverity(IStatus.INFO).show();
						if (continueSearch == IMessageBox.YES_OPTION) {
							EventTablePage.this.callTracker.resetNbCall(eventId);
						} else {
							break; // stop search
						}
					} else {
						// throw new
						// VetoException(TEXTS.get("zc.meeting.googleTooManyCall"));
						break; // stop search without prompt, because not a user
								// action
					}
				}
			}
			EventTablePage.this.callTracker.resetNbCall(eventId);

			if (newPossibleDate.isCreated()) {
				// update start and end date
				row.setCellValue(this.getStartDateColumn().getColumnIndex(),
						EventTablePage.this.getDateHelper().toDate(newPossibleDate.getStart()));
				this.getStartDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));
				row.setCellValue(this.getEndDateColumn().getColumnIndex(),
						EventTablePage.this.getDateHelper().toDate(newPossibleDate.getEnd()));
				this.getEndDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));

				if (hasLooped) {
					notificationHelper.addWarningNotification("zc.meeting.notification.newMeetingDateFoundFromBegin");
				} else {
					notificationHelper.addProccessedNotification("zc.meeting.notification.newMeetingDateFound");
				}
				row.setIconId(null);
			}
		}

		private DateReturn tryChangeDatesNext(final ZonedDateTime startDate, final ITableRow row) throws IOException {
			final Integer rowIndex = row.getRowIndex();
			final DurationCodeType durationCodes = BEANS.get(DurationCodeType.class);
			final Integer duration = durationCodes.getCode(this.getDurationColumn().getValue(rowIndex)).getValue()
					.intValue();
			final DateReturn newPossibleDate = this.tryChangeDatesNext(startDate, duration,
					this.getSlotColumn().getValue(rowIndex), this.getOrganizerColumn().getValue(rowIndex),
					this.getGuestIdColumn().getValue(rowIndex),
					this.getMinimalStartDateColumn().getZonedValue(rowIndex),
					this.getMaximalStartDateColumn().getZonedValue(rowIndex),
					this.getStartDateColumn().getZonedValue(rowIndex));

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
			ZonedDateTime minimalStart = ZonedDateTime.now(date.getZone()).plus(Duration.ofMinutes(reactionDelayMin));
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
		protected DateReturn tryChangeDatesNext(ZonedDateTime startDate, final Integer selectEventDuration,
				final Long selectSlotId, final Long organizerUserId, final Long guestUserId,
				final ZonedDateTime minimalStartDate, final ZonedDateTime maximalStartDate,
				final ZonedDateTime currentStartDate) throws IOException {
			LOG.info("Checking to create an event starting at " + startDate);

			Boolean loopInDates = Boolean.FALSE;

			if (null != minimalStartDate && startDate.isBefore(minimalStartDate)) {
				LOG.info("startDate is before the mnimal : " + minimalStartDate);
				startDate = minimalStartDate;
			} else if (null != maximalStartDate && startDate.isAfter(maximalStartDate)) {
				LOG.info("startDate is after the maximal : " + maximalStartDate + " Loop back to the minimal : "
						+ minimalStartDate);
				loopInDates = Boolean.TRUE;
				startDate = minimalStartDate;
			}

			final ZonedDateTime nextEndDate = startDate.plus(Duration.ofMinutes(selectEventDuration));

			// Localized Start and Stop for oragnizer
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

			if (null == proposedDate) {
				// check guest (current connected user) calendars
				final ZonedDateTime calendareRecommendedDate = this.tryCreateEvent(startDate, nextEndDate,
						Duration.ofMinutes(selectEventDuration), guestUserId);
				if (calendareRecommendedDate != null) {
					return new DateReturn(this.addReactionTime(calendareRecommendedDate), loopInDates);
				}
			}

			if (null == proposedDate) {
				// Check organizer calendars
				final ZonedDateTime organizerCalendareRecommendedDate = this.tryCreateEvent(organizerStartDate,
						organizerEndDate, Duration.ofMinutes(selectEventDuration), organizerUserId);
				if (organizerCalendareRecommendedDate != null) {
					return new DateReturn(
							this.addReactionTime(this.atZone(organizerCalendareRecommendedDate, guestUserId)),
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
			return EventTablePage.this.getDateHelper().atZone(date,
					EventTablePage.this.getAppUserHelper().getUserZoneId(userId));
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
			final StringBuilder builder = new StringBuilder(150);
			builder.append("Cheking for (Google) calendars events from : ").append(startDate).append(" to ")
					.append(endDate).append(" for user : ").append(userId);
			LOG.info(builder.toString());

			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

			if (!googleHelper.isCalendarConfigured(userId)) {
				LOG.info("Cannot check user (Google) clendar because no API configured for user : " + userId);
				// new recommended Date null, means "available" in user calendar
				return null; // early break
			}

			final ZoneId userZoneId = EventTablePage.this.getAppUserHelper().getUserZoneId(userId);

			final List<Event> allConcurentEvent = this.getEvents(startDate, endDate, userId);

			// if no exiting event, a new one can be created
			ZonedDateTime recommendedNewDate = null;
			if (allConcurentEvent.isEmpty()) {
				LOG.info(
						"No event found in calendars from : " + startDate + " to " + endDate + " for user : " + userId);
				// Do nothing special, recommendedNewDate = null meaning
				// provided periods is OK
			} else {
				// else try to find a new appropriate new startDate
				if (LOG.isDebugEnabled()) {
					final StringBuilder builderDebug = new StringBuilder(100);
					builderDebug.append(allConcurentEvent.size()).append(" event(s) found in calendars from : ")
							.append(startDate).append(" to ").append(endDate).append(" for user : ").append(userId);
					LOG.debug(builderDebug.toString());
				}
				final List<DayDuration> freeTimes = this.getFreeTime(startDate, endDate, allConcurentEvent, userZoneId);

				if (!freeTimes.isEmpty()) {
					if (LOG.isDebugEnabled()) {
						final StringBuilder builderDebug = new StringBuilder(100);
						builderDebug.append("FreeTime found in calendars from : ").append(startDate).append(" to ")
								.append(endDate).append(" with periods : ").append(freeTimes);
						LOG.debug(builderDebug.toString());
					}
					recommendedNewDate = SlotHelper.get().getNextValidDateTime(freeTimes, startDate, endDate);
					if (null != recommendedNewDate) {
						LOG.info("Recommanding new search from : " + recommendedNewDate + " (cause : " + userId
								+ " has blocking event(s) but freeTime)");
					}
				}
				if (null == recommendedNewDate) {
					if (LOG.isDebugEnabled()) {
						final StringBuilder builderDebug = new StringBuilder(100);
						builderDebug.append("No avilable periods found in freeTime from : ").append(startDate)
								.append(" to ").append(endDate).append(" for user : ").append(userId);
						LOG.debug(builderDebug.toString());
					}
					// The new potential start is the end of the last event
					final Event lastEvent = this.getLastEvent(allConcurentEvent);
					if (LOG.isDebugEnabled()) {
						final StringBuilder builderDebug = new StringBuilder(100);
						builderDebug.append("Last (Google) blocking event ").append(googleHelper.aslog(lastEvent));
						LOG.debug(builderDebug.toString());
					}
					final ZonedDateTime endLastEvent = googleHelper.fromEventDateTime(lastEvent.getEnd());
					// TODO Djer13 is required to add 1 minute ?
					recommendedNewDate = endLastEvent.plus(Duration.ofMinutes(1));
					LOG.info("Recommanding new search from : " + recommendedNewDate + " (cause : " + userId
							+ " whole period blocked by " + allConcurentEvent.size() + " event(s)");
				}
			}

			return recommendedNewDate;
		}

		private String getUserCreateEventCalendar(final Long userId) {
			// final GoogleApiHelper googleHelper =
			// BEANS.get(GoogleApiHelper.class);
			final String calendarId = "primary";

			// if (calendarId.equals("primary") && !(this.getCurrentUserId() ==
			// userId)) {
			// // other users anno't use "primary" because it's refer to there
			// // owned primary calendar.
			// calendarId = googleHelper.getPrimaryToCalendarId(userId);
			// }

			return calendarId;
		}

		private List<Event> getEvents(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long userId)
				throws IOException {
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
			Calendar gCalendarService;

			try {
				gCalendarService = googleHelper.getCalendarService(userId);
			} catch (final UserAccessRequiredException uare) {
				throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
			}

			// getEvent from start to End for each calendar
			final String readEventCalendarId = this.getUserCreateEventCalendar(userId);
			final List<CalendarListEntry> userGCalendars = CollectionUtility
					.arrayList(gCalendarService.calendarList().get(readEventCalendarId).execute());

			final DateTime googledStartDate = googleHelper.toDateTime(startDate);
			final DateTime googledEndDate = googleHelper.toDateTime(endDate);

			final List<Event> allConcurentEvent = new ArrayList<>();
			for (final CalendarListEntry calendar : userGCalendars) {
				final Events events = gCalendarService.events().list(calendar.getId()).setMaxResults(50)
						.setTimeMin(googledStartDate).setTimeMax(googledEndDate).setOrderBy("startTime")
						.setSingleEvents(true).execute();
				allConcurentEvent.addAll(events.getItems());
			}

			final Boolean ignoreFullDayEvents = Boolean.TRUE;

			if (!allConcurentEvent.isEmpty() && ignoreFullDayEvents) {
				final Iterator<Event> itEvents = allConcurentEvent.iterator();
				while (itEvents.hasNext()) {
					final Event event = itEvents.next();
					if (null != event.getStart().getDate()) {
						LOG.debug("FullDay Event removed : " + googleHelper.aslog(event));
						itEvents.remove();
					}
				}
			}

			return allConcurentEvent;
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
				final ZonedDateTime eventZonedStartDate = BEANS.get(GoogleApiHelper.class)
						.fromEventDateTime(event.getStart());
				final DayOfWeek eventLocalStartDateDay = eventZonedStartDate.getDayOfWeek();
				if (eventZonedStartDate.isAfter(startDate)) {
					// freeTime from startDate to the beginning of the event
					freeTime.add(new DayDuration(startDate.toOffsetDateTime().toOffsetTime(),
							eventZonedStartDate.toLocalTime()
									.atOffset(BEANS.get(GoogleApiHelper.class).timeOffset(event.getStart())),
							CollectionUtility.arrayList(eventLocalStartDateDay), Boolean.FALSE));
					if (itEvent.hasNext()) {
						event = itEvent.next();
					}
				} else {
					// freeTime from end of this event to begin of the next (if
					// this event ends before the endDate)
					final ZonedDateTime eventZonedEndDate = BEANS.get(GoogleApiHelper.class)
							.fromEventDateTime(event.getEnd());
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
							nextEventLocalStartDate = BEANS.get(GoogleApiHelper.class)
									.fromEventDateTime(nextEvent.getStart());
							offset = BEANS.get(GoogleApiHelper.class).timeOffset(nextEvent.getStart());
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
			LOG.debug(freeTime.size() + " freeTime periods found");
			return freeTime;
		}

		private Event createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long forUserId,
				final String withEmail, final String subject, final String location) throws IOException {
			return this.createEvent(startDate, endDate, forUserId, withEmail, subject, location, Boolean.FALSE);
		}

		private Event createEvent(final ZonedDateTime startDate, final ZonedDateTime endDate, final Long forUserId,
				final String withEmail, final String subject, final String location,
				final Boolean guestAutoAcceptMeeting) throws IOException {
			LOG.debug("Creating (Google) Event from : " + startDate + " to " + endDate + ", for :" + forUserId
					+ " (attendee :" + withEmail + ", autoAccept? " + guestAutoAcceptMeeting + ")");

			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

			Calendar googleCalendarService;

			try {
				googleCalendarService = googleHelper.getCalendarService(forUserId);
			} catch (final UserAccessRequiredException uare) {
				// throw new
				// VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
				return null; // early break;
			}

			final String EnvDisplay = new ApplicationEnvProperty().displayAsText();
			final String createdEventCalendarId = this.getUserCreateEventCalendar(forUserId);

			final Event newEvent = new Event();
			newEvent.setStart(googleHelper.toEventDateTime(startDate));
			newEvent.setEnd(googleHelper.toEventDateTime(endDate));
			newEvent.setSummary(
					EnvDisplay + " " + subject + TextsHelper.get(forUserId, "zc.common.email.subject.suffix"));
			newEvent.setLocation(TextsHelper.get(forUserId, location));
			newEvent.setDescription(subject);

			final EventAttendee attendeeEmail = new EventAttendee().setEmail(withEmail);
			if (guestAutoAcceptMeeting) {
				attendeeEmail.setResponseStatus("accepted");
			}

			final EventAttendee[] attendees = new EventAttendee[] { attendeeEmail };
			newEvent.setAttendees(Arrays.asList(attendees));

			final Event createdEvent = googleCalendarService.events().insert(createdEventCalendarId, newEvent)
					.execute();

			final StringBuilder sbEventInfo = new StringBuilder(250);
			sbEventInfo.append("(Google) Event created  with id : ").append(createdEvent.getId())
					.append(" in calendar : ").append(createdEventCalendarId).append(" with ")
					.append(createdEvent.getAttendees().size()).append(" attendee(s) ; From ").append(startDate)
					.append(" to ").append(endDate).append(", for : ").append(forUserId).append(" ICalUID : ")
					.append(createdEvent.getICalUID()).append(" link : ").append(createdEvent.getHtmlLink());

			LOG.info(sbEventInfo.toString());

			return createdEvent;
		}

		private Event acceptCreatedEvent(final Event organizerEvent, final String organizerCalendarId,
				final Long userId, final String attendeeEmail, final Long heldByUserId) throws IOException {
			LOG.info("Accepting (Google) Event from (" + organizerEvent.getOrganizer().getEmail()
					+ " organizerCalendarId : " + organizerCalendarId + "), for :" + userId + " with his email : "
					+ attendeeEmail);

			Long googleApiUserId = userId;
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

			if (!googleHelper.isCalendarConfigured(userId)) {
				LOG.info("User : " + userId
						+ " as no calendar configured (assuming no API acces), updating the (Google) Event with the heldBy user Id ("
						+ heldByUserId + ")");
				googleApiUserId = heldByUserId;
			}

			Calendar gCalendarService;

			try {
				gCalendarService = googleHelper.getCalendarService(googleApiUserId);
			} catch (final UserAccessRequiredException uare) {
				throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
			}

			final List<EventAttendee> attendees = organizerEvent.getAttendees();
			for (final EventAttendee eventAttendee : attendees) {
				if (attendeeEmail.equals(eventAttendee.getEmail())) {
					LOG.debug("Updating (Google) event : " + organizerEvent.getSummary()
							+ " as accepted for attendee : " + eventAttendee.getEmail());
					eventAttendee.setResponseStatus("accepted");
				}
			}

			// Update the event
			Event updatedEvent = null;
			try {
				updatedEvent = gCalendarService.events()
						.update(organizerCalendarId, organizerEvent.getId(), organizerEvent).execute();
			} catch (final GoogleJsonResponseException gjre) {
				if (gjre.getStatusCode() == 404) {
					// wait a few and re-try
					LOG.warn("(Google); exception while accepting recently created event (id :" + organizerEvent.getId()
							+ "), re-trying)");
					try {
						Thread.sleep(200);
					} catch (final InterruptedException e) {
						// do nothing
					}
				}

				updatedEvent = gCalendarService.events()
						.update(organizerCalendarId, organizerEvent.getId(), organizerEvent).execute();
			}

			return updatedEvent;
		}

		@Order(2000)
		public class NewEventMenu extends AbstractNewMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.addEvent");
			}

			@Override
			protected void execInitAction() {
				this.setVisibleGranted(
						ACCESS.getLevel(new CreateEventPermission()) >= CreateEventPermission.LEVEL_SUB_FREE);
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.TRUE;
			}

			@Override
			protected void execAction() {
				if (!EventTablePage.this.isUserCalendarConfigured()) {
					final GoogleApiHelper gooleHelper = BEANS.get(GoogleApiHelper.class);
					gooleHelper.askToAddApi(Table.this.getCurrentUserId());
				}

				if (!EventTablePage.this.isUserCalendarConfigured()) {
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
						validateCpsForm.getUserIdField().setValue(Table.this.getCurrentUserId());
						validateCpsForm.setModal(Boolean.TRUE);
						validateCpsForm.startNew();
						validateCpsForm.waitFor();
					} else {
						// Do nothing
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
				form.setVisibleGranted(
						ACCESS.getLevel(new CreateEventPermission()) >= CreateEventPermission.LEVEL_SUB_FREE);
				form.startNew();
			}
		}

		@Order(2100)
		public class SeparatorMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return "";
			}

			@Override
			protected boolean getConfiguredSeparator() {
				return Boolean.TRUE;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}
		}

		@Order(2500)
		public class AcceptEventMenu extends AbstractValidateMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.Accept");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			private Boolean isWorkflowVisible(final String currentState) {
				Boolean isVisible = Boolean.FALSE;
				if (CompareUtility.equals(StateCodeType.AskedCode.ID, currentState)) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				final StringBuffer rowMessages = new StringBuffer();
				if (null != row) {
					final Boolean isOrganizerCalendarConfigured = EventTablePage.this
							.isOrganizerCalendarConfigured(row);
					this.setVisible(Table.this.userCanAccept(row) && this.isWorkflowVisible(Table.this.getState(row))
							&& Table.this.isGuestCurrentUser(row));

					final Boolean hasStartDate = null != Table.this.getStartDateColumn().getValue(row.getRowIndex());
					// && EventTablePage.this.isUserCalendarConfigured()
					this.setEnabled(hasStartDate && isOrganizerCalendarConfigured);

					if (hasStartDate) {
						this.setTooltipText(null);
						// row.setTooltipText(null);
					} else {
						this.setTooltipText(TEXTS.get("zc.meeting.accept.require.startDate"));
						// this.setIconId(Icons.ExclamationMark);
						rowMessages.append(TEXTS.get("zc.meeting.accept.require.startDate"));
					}

					if (isOrganizerCalendarConfigured) {
						this.setTooltipText(null);
						row.setTooltipText(null);
					} else {
						this.setTooltipText(TEXTS.get("zc.meeting.accept.require.OrganizerElectronicCalendar"));
						// this.setIconId(Icons.ExclamationMark);
						rowMessages.append(TEXTS.get("zc.meeting.accept.require.OrganizerElectronicCalendar"));
					}
				}

				if (rowMessages.length() > 0) {
					// Table.this.setRowTooltip(row, rowMessages.toString());
					this.setTooltipText(rowMessages.toString());
				} else {
					// Table.this.removeRowToolTips(row);
					this.setTooltipText(null);
				}
			}

			@Override
			protected void execAction() {
				final Boolean guestAutoAcceptEvent = Boolean.TRUE;

				final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
				notificationHelper.addProcessingNotification("zc.meeting.notification.acceptingEvent");

				final IUserService userService = BEANS.get(IUserService.class);
				final ZonedDateTime start = Table.this.getStartDateColumn().getSelectedZonedValue();
				final ZonedDateTime end = Table.this.getEndDateColumn().getSelectedZonedValue();
				Long eventHeldBy = Table.this.getOrganizerColumn().getSelectedValue();
				final String eventHeldEmail = Table.this.getOrganizerEmailColumn().getSelectedValue();
				Long eventGuest = Table.this.getGuestIdColumn().getSelectedValue();
				final String eventGuestEmail = Table.this.getEmailColumn().getSelectedValue();
				final String subject = Table.this.getSubjectColumn().getSelectedValue();
				final String venue = Table.this.getVenueColumn().getSelectedValue();

				try {
					if (null == start || null == end) {
						Table.this.changeDatesNext();
						throw new VetoException(TEXTS.get("zc.meeting.chooseDateFirst"));
					}

					Table.this.getStateColumn().setValue(Table.this.getSelectedRow(), StateCodeType.AcceptedCode.ID);

					if (null == eventHeldBy) {
						eventHeldBy = userService.getUserIdByEmail(eventHeldEmail);
					}
					// External event for holder
					final Event externalOrganizerEvent = Table.this.createEvent(start, end, eventHeldBy,
							eventGuestEmail, subject, venue, guestAutoAcceptEvent);
					if (null == externalOrganizerEvent) {
						LOG.warn("Event not created for user : " + eventHeldBy
								+ " and he is the organizer ! (subject : " + subject + ")");
					} else {
						Table.this.getExternalIdOrganizerColumn().setValue(Table.this.getSelectedRow(),
								externalOrganizerEvent.getId());
					}

					if (!guestAutoAcceptEvent) {
						// Only if required to process "accept" in a other
						// request
						if (null == eventGuest) {
							eventGuest = userService.getUserIdByEmail(eventGuestEmail);
						}

						final Event externalGuestEvent = Table.this.acceptCreatedEvent(externalOrganizerEvent,
								Table.this.getUserCreateEventCalendar(eventHeldBy), eventGuest, eventGuestEmail,
								eventHeldBy);
						if (null != externalGuestEvent) {
							Table.this.getExternalIdRecipientColumn().setValue(Table.this.getSelectedRow(),
									externalGuestEvent.getId());
						}
					} else {
						Table.this.getExternalIdRecipientColumn().setValue(Table.this.getSelectedRow(),
								externalOrganizerEvent.getId());
					}

					// Save at the end to save external IDs !
					final EventFormData formData = Table.this.saveEventCurrentRow();

					this.sendConfirmationEmail(formData, externalOrganizerEvent, eventHeldEmail, eventHeldBy,
							eventGuestEmail);
					this.sendConfirmationEmail(formData, externalOrganizerEvent, eventGuestEmail, eventGuest,
							eventHeldEmail);

					Table.this.resetInvalidatesEvent(start, end);
					Table.this.autoFillDates();

				} catch (final IOException e) {
					LOG.error("Error while getting (Google) calendar details", e);
					throw new VetoException("Canno't get calendar details, re-try later", e);
				}
			}

			private void sendConfirmationEmail(final EventFormData formData, final Event event, final String recipient,
					final Long recipientUserId, final String otherParticpantEmail) {
				final IMailSender mailSender = BEANS.get(IMailSender.class);

				final String[] values = EventTablePage.this.getEventMessageHelper()
						.buildValuesForLocaleMessages(formData, recipientUserId, event, otherParticpantEmail);

				final String subject = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.subject",
						values);
				final String content = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.html", values);

				try {
					mailSender.sendEmail(recipient, subject, content);
				} catch (final MailException e) {
					throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
				}
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
				if (CompareUtility.equals(StateCodeType.AskedCode.ID, currentState)) {
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
					// EventTablePage.this.isUserCalendarConfigured() &&
					this.setEnabled(
							Table.this.isUserTimeZoneValid() && EventTablePage.this.isOrganizerCalendarConfigured(row));
				}
			}

			@Override
			protected void execAction() {
				try {
					Table.this.changeDatesNext();
					Table.this.reloadMenus(Table.this.getSelectedRow());
				} catch (final IOException e) {
					LOG.error("Error while getting (Google) calendar details", e);
					throw new VetoException("Canno't get calendar details, re-try later", e);
				}
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.RIGHT);
			}

		}

		@Order(4000)
		public class NextChooserMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.nextChooser");
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
				if (CompareUtility.equals(StateCodeType.AskedCode.ID, currentState)) {
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
					// EventTablePage.this.isUserCalendarConfigured() &&
					this.setEnabled(
							Table.this.isUserTimeZoneValid() && EventTablePage.this.isOrganizerCalendarConfigured(row));
				}
			}

			@Override
			protected void execAction() {
				throw new VetoException("Top level Menu : Not implemented (Yet)");
			}

			@Order(1000)
			public class NextDayMenu extends AbstractMenu {
				@Override
				protected String getConfiguredText() {
					return TEXTS.get("zc.meeting.nextDay");
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
					if (CompareUtility.equals(StateCodeType.AskedCode.ID, currentState)) {
						isVisible = Boolean.TRUE;
					}
					return isVisible;
				}

				@Override
				protected void execOwnerValueChanged(final Object newOwnerValue) {
					final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
					if (null != row) {
						this.setVisible(
								Table.this.userCanChooseDate(row) && this.isWorkflowVisible(Table.this.getState(row))
										&& Table.this.isGuestCurrentUser(row));
						// EventTablePage.this.isUserCalendarConfigured() &&
						this.setEnabled(Table.this.isUserTimeZoneValid()
								&& EventTablePage.this.isOrganizerCalendarConfigured(row));
					}
				}

				@Override
				protected void execAction() {
					try {
						final ITableRow row = Table.this.getSelectedRow();
						final Long guestId = Table.this.getGuestIdColumn().getValue(row.getRowIndex());
						final ZonedDateTime currentStartDate = EventTablePage.this.getDateHelper().getZonedValue(
								EventTablePage.this.getAppUserHelper().getUserZoneId(guestId),
								Table.this.getStartDateColumn().getValue(row.getRowIndex()));
						final ZonedDateTime newStartDate = currentStartDate.plusDays(1).withHour(0).withMinute(0)
								.withSecond(0).withNano(0);
						Table.this.changeDatesNext(newStartDate);
						Table.this.reloadMenus(Table.this.getSelectedRow());
					} catch (final IOException e) {
						LOG.error("Error while getting (Google) calendar details", e);
						throw new VetoException("Canno't get calendar details, re-try later", e);
					}
				}

				@Override
				protected String getConfiguredKeyStroke() {
					return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.CONTROL, IKeyStroke.RIGHT);
				}
			}
		}

	}
}
