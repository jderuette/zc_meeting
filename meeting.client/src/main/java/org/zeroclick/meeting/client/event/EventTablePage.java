package org.zeroclick.meeting.client.event;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.event.EventTablePage.Table;
import org.zeroclick.meeting.service.CalendarService;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.EventStateCodeType;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.ui.action.menu.AbstractValidateMenu;

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
	protected Boolean canHandle(final EventCreatedNotification notification) {
		final EventFormData formData = notification.getFormData();
		return !this.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(EventStateCodeType.WaitingCode.ID, formData.getState().getValue());
	}

	@Override
	protected Boolean canHandle(final EventModifiedNotification notification) {
		final EventFormData formData = notification.getFormData();
		return !this.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(EventStateCodeType.WaitingCode.ID, formData.getState().getValue());
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
						if (LOG.isDebugEnabled()) {
							LOG.debug("No auto-filled dates for event ID : " + eventId + " Adding to processedList");
						}
						EventTablePage.this.addEventProcessedWithoutDates(eventId);
					}
				} catch (final IOException e) {
					LOG.error("Canno't auto calculate start/end meeting for row " + row, e);
				}
			}
		}

		private void changeDatesNext(final ZonedDateTime newStart) throws IOException {
			this.changeDatesNext(Table.this.getSelectedRow(), newStart, Boolean.TRUE);
		}

		private void changeDatesNext() throws IOException {
			this.changeDatesNext(Table.this.getSelectedRow(), Boolean.TRUE);
		}

		protected void changeDatesNext(final ITableRow row, final Boolean askedByUser) throws IOException {
			final ZonedDateTime startDate;

			final Long guestId = this.getCurrentUserId();
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
			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			final CalendarService calendarService = BEANS.get(CalendarService.class);

			final int rowIndex = row.getRowIndex();

			final Long eventId = this.getEventIdColumn().getValue(rowIndex);

			final Long durationId = this.getDurationColumn().getValue(rowIndex);
			final Long slotId = this.getSlotColumn().getValue(rowIndex);
			final Long organizerId = this.getOrganizerColumn().getValue(rowIndex);

			final IUserService userService = BEANS.get(IUserService.class);
			final UserFormData userDetails = userService.getCurrentUserDetails();
			final Long currentUserId = userDetails.getUserId().getValue();

			final ZonedDateTime minimalStart = this.getMinimalStartDateColumn().getZonedValue(rowIndex);
			final ZonedDateTime maximalStart = this.getMaximalStartDateColumn().getZonedValue(rowIndex);

			final ZonedDateTime currentStartDate = this.getStartDateColumn().getZonedValue(rowIndex);

			final DateReturn newPossibleDate = calendarService.searchNextDate(eventId, durationId, slotId, organizerId,
					currentUserId, minimalStart, maximalStart, newStartDate, currentStartDate, askedByUser);

			if (newPossibleDate.isNoAvailableDate()) {
				return;// break new Date search
			} else {
				if (newPossibleDate.isCreated()) {
					// update start and end date
					row.setCellValue(this.getStartDateColumn().getColumnIndex(),
							EventTablePage.this.getDateHelper().toDate(newPossibleDate.getStart()));
					this.getStartDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));
					row.setCellValue(this.getEndDateColumn().getColumnIndex(),
							EventTablePage.this.getDateHelper().toDate(newPossibleDate.getEnd()));
					this.getEndDateColumn().updateDisplayTexts(CollectionUtility.arrayList(row));
				}
			}

			row.setIconId(newPossibleDate.getIcon());
			if (null != newPossibleDate.getMessageKey()) {
				notificationHelper.addProccessedNotification(newPossibleDate.getMessageKey());
			}
		}

		@Order(2000)
		public class NewEventMenu extends AbstractCreatEventMenu {
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
				if (CompareUtility.equals(EventStateCodeType.WaitingCode.ID, currentState)) {
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
					this.setVisible(Table.this.userCanAccept(
							row) /*
									 * && this.isWorkflowVisible(Table.this.
									 * getState(row))
									 */
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
				/*
				 * final Boolean guestAutoAcceptEvent = Boolean.TRUE; final
				 * IEventService eventService = BEANS.get(IEventService.class);
				 * final IInvolvementService involvementService =
				 * BEANS.get(IInvolvementService.class); final
				 * IExternalEventService externalEventService =
				 * BEANS.get(IExternalEventService.class);
				 * 
				 * final NotificationHelper notificationHelper =
				 * BEANS.get(NotificationHelper.class);
				 * notificationHelper.addProcessingNotification(
				 * "zc.meeting.notification.acceptingEvent");
				 * 
				 * final Long eventId =
				 * Table.this.getEventIdColumn().getSelectedValue(); final
				 * IUserService userService = BEANS.get(IUserService.class);
				 * final ZonedDateTime start =
				 * Table.this.getStartDateColumn().getSelectedZonedValue();
				 * final ZonedDateTime end =
				 * Table.this.getEndDateColumn().getSelectedZonedValue(); Long
				 * eventHeldBy =
				 * Table.this.getOrganizerColumn().getSelectedValue(); final
				 * String eventHeldEmail =
				 * Table.this.getOrganizerEmailColumn().getSelectedValue(); Long
				 * eventGuestId = Table.this.getCurrentUserId(); final String
				 * eventGuestEmail = Table.this.getCurrentUserEmails().get(0);
				 * final String subject =
				 * Table.this.getSubjectColumn().getSelectedValue(); final
				 * String venue =
				 * Table.this.getVenueColumn().getSelectedValue(); final String
				 * description = eventService.loadDescription(eventId);
				 * 
				 * try { if (null == start || null == end) {
				 * Table.this.changeDatesNext(); throw new
				 * VetoException(TEXTS.get("zc.meeting.chooseDateFirst")); }
				 * 
				 * final CalendarService calendarService =
				 * BEANS.get(CalendarService.class);
				 * 
				 * final CalendarConfigurationFormData
				 * organizerCalendarToStoreEvent = calendarService
				 * .getUserCreateEventCalendar(eventHeldBy);
				 * 
				 * // Table.this.getStateColumn().setValue(Table.this.
				 * getSelectedRow(), // EventStateCodeType.PlannedCode.ID);
				 * 
				 * if (null == eventHeldBy) { eventHeldBy =
				 * userService.getUserIdByEmail(eventHeldEmail); } // External
				 * event for holder final EventIdentification
				 * externalOrganizerEventId = calendarService.createEvent(start,
				 * end, eventHeldBy, eventGuestEmail, subject, venue,
				 * guestAutoAcceptEvent, description); ExternalEventFormData
				 * orgaExternalEventformData = null; if (null ==
				 * externalOrganizerEventId) { LOG.warn(new
				 * StringBuilder().append("Event not created for user : ").
				 * append(eventHeldBy)
				 * .append(" and he is the organizer ! (subject : ").append(
				 * subject).append(")") .toString()); } else { //
				 * Table.this.getExternalIdOrganizerColumn().setValue(Table.this
				 * .getSelectedRow(), // externalOrganizerEventId.getEventId());
				 * 
				 * // create ExternalId orgaExternalEventformData = new
				 * ExternalEventFormData();
				 * orgaExternalEventformData.getExternalEventId().setValue(
				 * externalOrganizerEventId.getEventId());
				 * orgaExternalEventformData.getExternalCalendarId()
				 * .setValue(externalOrganizerEventId.getCalendarData().
				 * getExternalId().getValue());
				 * orgaExternalEventformData.getApiCredentialId()
				 * .setValue(organizerCalendarToStoreEvent.getOAuthCredentialId(
				 * ).getValue()); orgaExternalEventformData =
				 * externalEventService.create(orgaExternalEventformData);
				 * 
				 * // update orga state InvolvementFormData
				 * orgaInvolvementformData = new InvolvementFormData();
				 * orgaInvolvementformData.getEventId().setValue(eventId);
				 * orgaInvolvementformData.getUserId().setValue(eventHeldBy);
				 * orgaInvolvementformData =
				 * involvementService.load(orgaInvolvementformData);
				 * 
				 * orgaInvolvementformData.getState().setValue(
				 * InvolvmentStateCodeType.AcceptedCode.ID);
				 * orgaInvolvementformData.getExternalEventId()
				 * .setValue(orgaExternalEventformData.getExternalId().getValue(
				 * )); orgaInvolvementformData =
				 * involvementService.store(orgaInvolvementformData);
				 * 
				 * }
				 * 
				 * final String organizerEventExternalHtmlLink = calendarService
				 * .getEventExternalLink(externalOrganizerEventId, eventHeldBy);
				 * 
				 * String guestEventExternalHtmlLink =
				 * organizerEventExternalHtmlLink;
				 * 
				 * Boolean isAttendeeCalendarToStoreEventConfigured =
				 * Boolean.FALSE;
				 * 
				 * final CalendarConfigurationFormData
				 * attendeeCalendarToStoreEvent = calendarService
				 * .getUserCreateEventCalendar(eventGuestId);
				 * 
				 * if (!guestAutoAcceptEvent) { // Only if required to process
				 * "accept" in a other // request if (null == eventGuestId) {
				 * eventGuestId = userService.getUserIdByEmail(eventGuestEmail);
				 * }
				 * 
				 * final EventIdentification externalGuestEvent =
				 * calendarService.acceptCreatedEvent( externalOrganizerEventId,
				 * eventGuestId, eventGuestEmail, eventHeldBy); if (null !=
				 * externalGuestEvent) { //
				 * Table.this.getExternalIdRecipientColumn().setValue(Table.this
				 * .getSelectedRow(), // externalGuestEvent.getEventId());
				 * 
				 * // create guest provide specificExternalId
				 * ExternalEventFormData guestExternalEventformData = new
				 * ExternalEventFormData();
				 * guestExternalEventformData.getExternalEventId().setValue(
				 * externalGuestEvent.getEventId());
				 * guestExternalEventformData.getExternalCalendarId()
				 * .setValue(externalGuestEvent.getCalendarData().getExternalId(
				 * ).getValue());
				 * guestExternalEventformData.getApiCredentialId()
				 * .setValue(attendeeCalendarToStoreEvent.getOAuthCredentialId()
				 * .getValue()); guestExternalEventformData =
				 * externalEventService.create(guestExternalEventformData);
				 * 
				 * // update orga state InvolvementFormData
				 * guestInvolvementformData = new InvolvementFormData();
				 * guestInvolvementformData.getEventId().setValue(eventId);
				 * guestInvolvementformData.getUserId().setValue(eventHeldBy);
				 * guestInvolvementformData =
				 * involvementService.load(guestInvolvementformData);
				 * 
				 * guestInvolvementformData.getState().setValue(
				 * InvolvmentStateCodeType.AcceptedCode.ID);
				 * guestInvolvementformData.getExternalEventId()
				 * .setValue(guestExternalEventformData.getExternalId().getValue
				 * ()); involvementService.store(guestInvolvementformData);
				 * 
				 * guestEventExternalHtmlLink =
				 * calendarService.getEventExternalLink(externalGuestEvent,
				 * eventGuestId); } } else { EventIdentification
				 * externalAttendeeEventId; final IApiService apiService =
				 * BEANS.get(IApiService.class); // if guest use a different
				 * provider as the organizer, // and is autoAccepting events, we
				 * need to create a // specific event (in his provider)
				 * 
				 * final ApiTableRowData organizerCalendarApi = apiService
				 * .getApi(organizerCalendarToStoreEvent.getOAuthCredentialId().
				 * getValue()); final Long organizerProvider =
				 * organizerCalendarApi.getProvider();
				 * 
				 * if (null != attendeeCalendarToStoreEvent) {
				 * isAttendeeCalendarToStoreEventConfigured = Boolean.TRUE;
				 * final ApiTableRowData attendeeProviderCalendarApi =
				 * apiService
				 * .getApi(attendeeCalendarToStoreEvent.getOAuthCredentialId().
				 * getValue()); final Long attendeeProvider =
				 * attendeeProviderCalendarApi.getProvider();
				 * 
				 * final boolean isSameProvider =
				 * organizerProvider.equals(attendeeProvider);
				 * ExternalEventFormData guestExternalEventformData = null; if
				 * (isSameProvider) { externalAttendeeEventId =
				 * externalOrganizerEventId; guestExternalEventformData =
				 * orgaExternalEventformData;
				 * 
				 * } else { externalAttendeeEventId =
				 * calendarService.createEvent(start, end, eventGuestId,
				 * eventHeldEmail, subject, venue, Boolean.TRUE, description);
				 * 
				 * // create guest provider specific ExternalId
				 * guestExternalEventformData = new ExternalEventFormData();
				 * guestExternalEventformData.getExternalEventId()
				 * .setValue(externalAttendeeEventId.getEventId());
				 * guestExternalEventformData.getExternalCalendarId()
				 * .setValue(externalAttendeeEventId.getCalendarData().
				 * getExternalId().getValue());
				 * guestExternalEventformData.getApiCredentialId()
				 * .setValue(attendeeCalendarToStoreEvent.getOAuthCredentialId()
				 * .getValue()); guestExternalEventformData =
				 * externalEventService.create(guestExternalEventformData); }
				 * 
				 * //
				 * Table.this.getExternalIdRecipientColumn().setValue(Table.this
				 * .getSelectedRow(), // externalAttendeeEventId.getEventId());
				 * 
				 * // update guest state InvolvementFormData
				 * guestInvolvementformData = new InvolvementFormData();
				 * guestInvolvementformData.getEventId().setValue(eventId);
				 * guestInvolvementformData.getUserId().setValue(eventGuestId);
				 * guestInvolvementformData =
				 * involvementService.load(guestInvolvementformData);
				 * 
				 * guestInvolvementformData.getState().setValue(
				 * InvolvmentStateCodeType.AcceptedCode.ID);
				 * guestInvolvementformData.getExternalEventId()
				 * .setValue(guestExternalEventformData.getExternalId().getValue
				 * ()); involvementService.store(guestInvolvementformData);
				 * 
				 * } else { notificationHelper.addWarningNotification(
				 * "zc.meeting.calendar.noAddEvent.cannotCreateEvent",
				 * eventHeldEmail); } }
				 * 
				 * // Save at the end to save external IDs ! // /!\ after save
				 * guest may not be able to check the // organizer API (no more
				 * waiting meeting with organizer) // ==> Do all API related
				 * stuff BEFORE saving Event final EventFormData formData =
				 * Table.this.saveEventCurrentRow();
				 * 
				 * this.sendConfirmationEmail(formData,
				 * organizerEventExternalHtmlLink, eventHeldEmail, eventHeldBy,
				 * eventGuestEmail, Boolean.TRUE);
				 * this.sendConfirmationEmail(formData,
				 * guestEventExternalHtmlLink, eventGuestEmail, eventGuestId,
				 * eventHeldEmail, isAttendeeCalendarToStoreEventConfigured);
				 * 
				 * Table.this.resetInvalidatesEvent(start, end);
				 * Table.this.autoFillDates();
				 * 
				 * } catch (final IOException e) {
				 * LOG.error("Error while getting (Google) calendar details",
				 * e); throw new
				 * VetoException("Canno't get calendar details, re-try later",
				 * e); }
				 */
			}

			private void sendConfirmationEmail(final EventFormData formData, final String eventHtmlLink,
					final String recipient, final Long recipientUserId, final String otherParticpantEmail,
					final Boolean isAddCalendarConfigured) {
				final IMailSender mailSender = BEANS.get(IMailSender.class);

				String warningManualManageEvent = "";

				if (!isAddCalendarConfigured) {
					warningManualManageEvent = TextsHelper.get(recipientUserId,
							"zc.meeting.email.event.confirm.requireManualEventManagement");
				}

				final String[] values = EventTablePage.this.getEventMessageHelper().buildValuesForLocaleMessages(
						formData, recipientUserId, eventHtmlLink, otherParticpantEmail, warningManualManageEvent);

				final String subject = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.subject",
						values);
				final String content = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.html", values);

				try {
					mailSender.sendEmail(recipient, subject, content, Boolean.FALSE);
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
				if (CompareUtility.equals(EventStateCodeType.WaitingCode.ID, currentState)) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(Table.this.userCanChooseDate(row)
							&& /*
								 * this.isWorkflowVisible(Table.this.getState(
								 * row )) &&
								 */ Table.this.isGuestCurrentUser(row));
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
				if (CompareUtility.equals(EventStateCodeType.WaitingCode.ID, currentState)) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(Table.this.userCanChooseDate(row)
							&& /*
								 * this.isWorkflowVisible(Table.this.getState(
								 * row )) &&
								 */ Table.this.isGuestCurrentUser(row));
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
					if (CompareUtility.equals(EventStateCodeType.WaitingCode.ID, currentState)) {
						isVisible = Boolean.TRUE;
					}
					return isVisible;
				}

				@Override
				protected void execOwnerValueChanged(final Object newOwnerValue) {
					final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
					if (null != row) {
						this.setVisible(Table.this.userCanChooseDate(row)
								&& /*
									 * this.isWorkflowVisible(Table.this.
									 * getState(row)) &&
									 */ Table.this.isGuestCurrentUser(row));
						// EventTablePage.this.isUserCalendarConfigured() &&
						this.setEnabled(Table.this.isUserTimeZoneValid()
								&& EventTablePage.this.isOrganizerCalendarConfigured(row));
					}
				}

				@Override
				protected void execAction() {
					try {
						final ITableRow row = Table.this.getSelectedRow();
						final Long guestId = Table.this.getCurrentUserId();
						final ZonedDateTime currentStartDate = EventTablePage.this.getDateHelper().getZonedValue(
								EventTablePage.this.getAppUserHelper().getUserZoneId(guestId),
								Table.this.getStartDateColumn().getValue(row.getRowIndex()));
						if (null == currentStartDate) {
							// a simple next because, there is no existing start
							Table.this.changeDatesNext();
						} else {
							final ZonedDateTime newStartDate = currentStartDate.plusDays(1).withHour(0).withMinute(0)
									.withSecond(0).withNano(0);
							Table.this.changeDatesNext(newStartDate);
						}
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
