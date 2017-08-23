package org.zeroclick.meeting.client.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.client.api.ApiCreatedNotificationHandler;
import org.zeroclick.configuration.client.user.UserModifiedNotificationHandler;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.common.CallTrackerService;
import org.zeroclick.meeting.client.common.DurationLookupCall;
import org.zeroclick.meeting.client.common.EventStateLookupCall;
import org.zeroclick.meeting.client.common.SlotLookupCall;
import org.zeroclick.meeting.client.event.EventTablePage.Table.NewMenu;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.ReadEventExtendedPropsPermission;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.eventb.AbstractEventsTablePageData;

import com.google.api.services.calendar.model.Event;

@Data(AbstractEventsTablePageData.class)
public abstract class AbstractEventsTablePage<T extends AbstractEventsTablePage<T>.Table>
		extends AbstractPageWithTable<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEventsTablePage.class);

	final private Integer maxTry = 20;
	final protected CallTrackerService<Long> callTracker = new CallTrackerService<>(this.maxTry, Duration.ofMinutes(3),
			"Get calendar Events");

	private DateHelper dateHelper;
	private AppUserHelper appUserHelper;

	// TODO Djer13 is caching here smart ?
	private final Map<Long, String> cachedUserTimeZone = new HashMap<>();

	protected boolean isUserCalendarConfigured() {
		return GoogleApiHelper.get().isCalendarConfigured();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.events");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected boolean getConfiguredTableStatusVisible() {
		return Boolean.FALSE;
	}

	@Override
	protected void execInitPage() {
		if (null == this.dateHelper) {
			this.dateHelper = DateHelper.get();
		}
		if (null == this.appUserHelper) {
			this.appUserHelper = AppUserHelper.get();
		}
	}

	/**
	 * Allow subClass to defined if they want to handle notification for this
	 * kind of event
	 *
	 * @param formdata
	 * @return true if this (sub) tablePage want handle the event
	 */
	protected Boolean canHandleNew(final AbstractEventNotification notification) {
		return Boolean.FALSE;
	}

	/**
	 * Allow subClass to defined if they want to handle notification for this
	 * kind of event
	 *
	 * @param formdata
	 * @param previousStateRow
	 * @return true if this (sub) tablePage want handle the event
	 */
	protected Boolean canHandleModified(final AbstractEventNotification notification) {
		return Boolean.FALSE;
	}

	/**
	 * Propagate new Events to child page if implemented.Default do Nothing
	 *
	 * @param formData
	 */
	protected void onNewEvent(final EventFormData formData) {

	}

	/**
	 * Propagate modified Events to child page if implemented.Default do Nothing
	 *
	 * @param formData
	 */
	protected void onModifiedEvent(final EventFormData formData, final String previousStateRow) {

	}

	/**
	 * Is row event (formData) held by CurrentUser ?
	 *
	 * @param row
	 * @return
	 */
	protected Boolean isHeldByCurrentUser(final EventFormData formData) {
		return this.isOrganizer(formData.getOrganizer().getValue());
	}

	protected Boolean isOrganizer(final Long userId) {
		final Long currentUser = this.getAppUserHelper().getCurrentUserId();

		return currentUser.equals(userId);
	}

	/**
	 * Search for Locale Key
	 *
	 * @param formData
	 *            event Data to search for the key (mostly "state" is used)
	 * @param isSender
	 *            does this message for user who modify the event (sender) or
	 *            the other (receiver) ?
	 * @return the key to use {@link TEXTS.get()}. <br />
	 *         Samples : <br/>
	 *         zc.meeting.notification.modifiedEvent.send (default for user who
	 *         modify the event)<br />
	 *         zc.meeting.notification.modifiedEvent.receive (default for user
	 *         who is informed of the modification)<br />
	 *         zc.meeting.notification.modifiedEvent.send.asked<br />
	 *         zc.meeting.notification.modifiedEvent.send.refused<br />
	 */
	protected String getDesktopNotificationModifiedEventKey(final EventFormData formData) {
		final Boolean isSender = this.isCurerntUserActor(formData);
		return this.getDesktopNotificationModifiedEventKey(formData, isSender);
	}

	/**
	 * @see getDesktopNotificationModifiedEventKey(EventFormData)
	 *
	 * @param formData
	 * @param isSender
	 *            does this message for user who modify the event (sender) or
	 *            the other (receiver) ?
	 * @return
	 */
	protected String getDesktopNotificationModifiedEventKey(final EventFormData formData, final Boolean isSender) {
		final String currentState = formData.getState().getValue().toLowerCase();
		final StringBuilder builder = new StringBuilder(60);

		builder.append("zc.meeting.notification.modifiedEvent");
		if (isSender) {
			builder.append(".send");
		} else {
			builder.append(".receive");
		}

		if (null != currentState && !currentState.isEmpty()) {
			builder.append('.');
			builder.append(currentState);
		}

		return builder.toString();
	}

	protected String[] buildValuesForLocaleMessages(final EventFormData formData) {
		final List<String> values = new ArrayList<>();
		final ZoneId userZoneId = this.getAppUserHelper().getCurrentUserTimeZone();

		final String actor = this.getActorEmail(formData);
		final String receiver = this.getReceiverEmail(formData);

		values.add(actor); // 0
		final String stateText = TEXTS.get("zc.meeting.state." + formData.getState().getValue().toLowerCase());
		values.add(stateText.toLowerCase()); // 1

		values.add(formData.getSubject().getValue());// 2

		final String slotText = TEXTS.get("zc.meeting.slot." + formData.getSlot().getValue());
		values.add(slotText.toLowerCase());// 3

		final String durationText = TEXTS.get("zc.meeting.duration." + formData.getDuration().getValue());
		values.add(durationText.toLowerCase());// 4

		String startDate = null;
		if (null != formData.getStartDate().getValue()) {
			startDate = this.getDateHelper().format(formData.getStartDate().getValue(), userZoneId);
		}
		values.add(startDate);// 5

		String endDate = null;
		if (null != formData.getEndDate().getValue()) {
			endDate = this.getDateHelper().format(formData.getEndDate().getValue(), userZoneId);
		}
		values.add(endDate);// 6
		values.add(formData.getReason().getValue());// 7
		values.add(receiver);// 8

		if (null != formData.getStartDate().getValue()) {
			values.add(DateHelper.get().getRelativeDay(formData.getStartDate().getValue(), userZoneId));// 9
			values.add(DateHelper.get().formatHours(formData.getStartDate().getValue(), userZoneId));// 10
		}

		if (null != formData.getEndDate().getValue()) {
			values.add(DateHelper.get().formatHours(formData.getEndDate().getValue(), userZoneId));// 11
		}

		return CollectionUtility.toArray(values, String.class);
	}

	protected String[] buildValuesForLocaleMessages(final EventFormData formData, final Event externalEvent) {
		final List<String> values = CollectionUtility.arrayList(this.buildValuesForLocaleMessages(formData));

		values.add(externalEvent.getHtmlLink()); // 12

		return CollectionUtility.toArray(values, String.class);
	}

	protected String[] buildValuesForLocaleMessages(final EventFormData formData, final Event externalEvent,
			final String... otherParams) {
		final List<String> values = CollectionUtility.arrayList(this.buildValuesForLocaleMessages(formData));

		values.add(externalEvent.getHtmlLink()); // 12

		for (final String param : otherParams) {
			values.add(param);
		}

		return CollectionUtility.toArray(values, String.class);
	}

	protected Boolean isCurerntUserActor(final EventFormData formData) {
		final Long lastModifierUserId = formData.getLastModifier();
		if (null == lastModifierUserId) {
			return Boolean.FALSE;
		}

		final Long currentUserId = this.getAppUserHelper().getCurrentUserId();
		return lastModifierUserId.equals(currentUserId);
	}

	protected String getActorEmail(final EventFormData formData) {
		String actor = this.extractEmail(formData, Boolean.TRUE);

		if (null == actor) {
			LOG.warn("No LastModifier in event : " + formData.getEventId()
					+ " Falling back to using holder to determine actor");
			if (this.isHeldByCurrentUser(formData)) {
				actor = formData.getOrganizerEmail().getValue();
			} else {
				actor = formData.getEmail().getValue();
			}
		}
		return actor;
	}

	protected String getReceiverEmail(final EventFormData formData) {
		String receiver = this.extractEmail(formData, Boolean.FALSE);

		if (null == receiver) {
			LOG.warn("No LastModifier in event : " + formData.getEventId()
					+ " Falling back to using holder to determine receiver");
			if (this.isHeldByCurrentUser(formData)) {
				receiver = formData.getEmail().getValue();
			} else {
				receiver = formData.getOrganizerEmail().getValue();
			}
		}
		return receiver;
	}

	protected String extractEmail(final EventFormData formData, final Boolean actor) {
		String actorEmail = null;
		String receiverEmail = null;
		String userEmail;
		final Long lastModifierUserId = formData.getLastModifier();
		if (null != lastModifierUserId) {
			if (formData.getOrganizer().getValue().equals(lastModifierUserId)) {
				actorEmail = formData.getOrganizerEmail().getValue();
				receiverEmail = formData.getEmail().getValue();
				LOG.debug("Last modifier for event : " + formData.getEventId() + " is user : " + lastModifierUserId
						+ ", and the actor is the organizer. " + actorEmail);
			} else if (formData.getGuestId().getValue().equals(lastModifierUserId)) {
				actorEmail = formData.getEmail().getValue();
				receiverEmail = formData.getOrganizerEmail().getValue();
				LOG.debug("Last modifier for event : " + formData.getEventId() + " is user : " + lastModifierUserId
						+ ", and the actor is the attendee : " + receiverEmail);
			} else {
				LOG.debug("Last modifier for event : " + formData.getEventId() + " is user : " + lastModifierUserId
						+ ", and actor is neither orgnizer nor attendee user mail return is null");
			}
		}
		if (actor) {
			userEmail = actorEmail;
		} else {
			userEmail = receiverEmail;
		}
		return userEmail;
	}

	public class Table extends AbstractTable {

		protected INotificationListener<EventCreatedNotification> eventCreatedListener;
		protected INotificationListener<EventModifiedNotification> eventModifiedListener;
		protected INotificationListener<ApiCreatedNotification> apiCreatedListener;
		protected INotificationListener<UserModifiedNotification> userModifiedListener;

		@Override
		protected boolean getConfiguredHeaderEnabled() {
			return Boolean.FALSE;
		}

		@Override
		protected boolean getConfiguredSortEnabled() {
			return Boolean.FALSE;
		}

		@Override
		protected boolean getConfiguredTableStatusVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected boolean getConfiguredAutoResizeColumns() {
			return Boolean.TRUE;
		}

		@Override
		protected void execInitTable() {
			super.execInitTable();
			this.setTableStatusVisible(Boolean.FALSE);
		}

		@Override
		protected void initConfig() {
			super.initConfig();
			this.setRowIconVisible(Boolean.FALSE);
			this.getEventIdColumn().setVisiblePermission(new ReadEventExtendedPropsPermission());
			this.getExternalIdOrganizerColumn().setVisiblePermission(new ReadEventExtendedPropsPermission());
			this.getExternalIdRecipientColumn().setVisiblePermission(new ReadEventExtendedPropsPermission());

			final EventCreatedNotificationHandler eventCreatedNotificationHandler = BEANS
					.get(EventCreatedNotificationHandler.class);
			eventCreatedNotificationHandler.addListener(this.createEventCreatedListener());

			final EventModifiedNotificationHandler eventModifiedNotificationHandler = BEANS
					.get(EventModifiedNotificationHandler.class);
			eventModifiedNotificationHandler.addListener(this.createEventModifiedListener());

			final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
					.get(ApiCreatedNotificationHandler.class);
			apiCreatedNotificationHandler.addListener(this.createApiCreatedListener());

			final UserModifiedNotificationHandler userModifiedNotificationHandler = BEANS
					.get(UserModifiedNotificationHandler.class);
			userModifiedNotificationHandler.addListener(this.createUserModifiedListener());

		}

		protected INotificationListener<EventCreatedNotification> createEventCreatedListener() {
			this.eventCreatedListener = new INotificationListener<EventCreatedNotification>() {
				@Override
				public void handleNotification(final EventCreatedNotification notification) {

					if (!AbstractEventsTablePage.this.canHandleNew(notification)) {
						return; // Early Break
					}

					final EventFormData eventForm = notification.getEventForm();
					LOG.debug("New event prepare to add to table (in " + Table.this.getTitle() + ") for event Id : "
							+ eventForm.getEventId());
					try {
						AbstractEventsTablePage.this.getTable()
								.addRow(AbstractEventsTablePage.this.getTable().createTableRowFromForm(eventForm));
						AbstractEventsTablePage.this.getTable().applyRowFilters();

						// ClientSession.get().getDesktop().refreshPages(EventTablePage.class);

						AbstractEventsTablePage.this.onNewEvent(eventForm);

						final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
						notificationHelper.addProccessedNotification(
								AbstractEventsTablePage.this.getDesktopNotificationModifiedEventKey(eventForm),
								AbstractEventsTablePage.this.buildValuesForLocaleMessages(eventForm));

						// final Desktop desktop = (Desktop)
						// ClientSession.get().getDesktop();
						// desktop.addNotification(IStatus.OK, 0l, Boolean.TRUE,
						// AbstractEventsTablePage.this.getDesktopNotificationModifiedEventKey(eventForm),
						// AbstractEventsTablePage.this.buildValuesForLocaleMessages(eventForm));
					} catch (final RuntimeException e) {
						LOG.error("Could not add new event. (" + Table.this.getTitle() + ")", e);
					}
				}
			};
			return this.eventCreatedListener;
		}

		protected INotificationListener<EventModifiedNotification> createEventModifiedListener() {
			this.eventModifiedListener = new INotificationListener<EventModifiedNotification>() {
				@Override
				public void handleNotification(final EventModifiedNotification notification) {
					final EventFormData eventForm = notification.getEventForm();
					if (!AbstractEventsTablePage.this.canHandleModified(notification)) {
						// remove row if exists
						final ITableRow row = AbstractEventsTablePage.this.getTable().getRow(eventForm.getEventId());
						if (null != row) {
							LOG.debug("Modified event prepare to remove table row (in " + Table.this.getTitle()
									+ ") for event Id : " + eventForm.getEventId());
							AbstractEventsTablePage.this.getTable().deleteRow(row);

							// TODO Djer13 not really a "modified" event, just a
							// row removed
							AbstractEventsTablePage.this.onModifiedEvent(eventForm, eventForm.getPreviousState());
						}
						return; // early break
					}

					try {
						ITableRow row = AbstractEventsTablePage.this.getTable().getRow(eventForm.getEventId());
						if (null == row) {
							row = AbstractEventsTablePage.this.getTable()
									.addRow(AbstractEventsTablePage.this.getTable().createTableRowFromForm(eventForm));
						}
						if (null != row) {
							LOG.debug("Modified event prepare to modify table row (in " + Table.this.getTitle()
									+ ") for event Id : " + eventForm.getEventId());
							// if row is null, this table instance should not
							// handle this event. We can safely ignore.
							// TODO Djer13 perf : avoid handling notification in
							// child xxxEventTablePage if this event is not the
							// current Table
							final String previousStateRow = eventForm.getPreviousState();

							Table.this.updateTableRowFromForm(row, eventForm);

							AbstractEventsTablePage.this.getTable().applyRowFilters();

							AbstractEventsTablePage.this.onModifiedEvent(eventForm, previousStateRow);

							final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
							notificationHelper.addProccessedNotification(
									AbstractEventsTablePage.this.getDesktopNotificationModifiedEventKey(eventForm),
									AbstractEventsTablePage.this.buildValuesForLocaleMessages(eventForm));
							//
							// final Desktop desktop = (Desktop)
							// ClientSession.get().getDesktop();
							// desktop.addNotification(IStatus.OK, 0l,
							// Boolean.TRUE,
							// AbstractEventsTablePage.this.getDesktopNotificationModifiedEventKey(eventForm),
							// AbstractEventsTablePage.this.buildValuesForLocaleMessages(eventForm));

						} else {
							LOG.debug("Modified event ignored because it's not a current table row (in "
									+ Table.this.getTitle() + ") for event Id : " + eventForm.getEventId());
						}

					} catch (final RuntimeException e) {
						LOG.error("Could not update event. (" + Table.this.getTitle() + ") for event Id : "
								+ eventForm.getEventId(), e);
					}
				}
			};
			return this.eventModifiedListener;
		}

		private INotificationListener<ApiCreatedNotification> createApiCreatedListener() {
			this.apiCreatedListener = new INotificationListener<ApiCreatedNotification>() {
				@Override
				public void handleNotification(final ApiCreatedNotification notification) {
					try {
						final ApiFormData eventForm = notification.getApiForm();
						LOG.debug("Created Api prepare to modify menus (" + this.getClass().getName() + ") : "
								+ eventForm.getUserId());
						Table.this.reloadMenus();
						// calculate start/end meeting
						LOG.debug("Created Api prepare to calculate start/end date (" + this.getClass().getName()
								+ ") : " + eventForm.getUserId());
						final List<ITableRow> rows = Table.this.getRows();
						for (final ITableRow row : rows) {
							Table.this.autoFillDates(row);
						}
					} catch (final RuntimeException e) {
						LOG.error("Could not handle new api. (" + Table.this.getTitle() + ")", e);
					}
				}
			};

			return this.apiCreatedListener;
		}

		private INotificationListener<UserModifiedNotification> createUserModifiedListener() {
			this.userModifiedListener = new INotificationListener<UserModifiedNotification>() {
				@Override
				public void handleNotification(final UserModifiedNotification notification) {
					try {
						final UserFormData userForm = notification.getUserForm();
						LOG.debug("User modified prepare to reset cached TimeZone (" + Table.this.getTitle() + ") : "
								+ userForm.getUserId());
						final List<ITableRow> rows = Table.this.getRows();
						for (final ITableRow row : rows) {
							Table.this.autoFillDates(row);
						}
					} catch (final RuntimeException e) {
						LOG.error("Could not handle modified User. (" + Table.this.getTitle() + ")", e);
					}
				}
			};

			return this.userModifiedListener;
		}

		protected void autoFillDates(final ITableRow row) {
			// TODO Auto-generated method stub
		}

		protected Boolean isTimeZoneValid(final Long userId) {
			Boolean timeZoneValid = Boolean.FALSE;

			if (!AbstractEventsTablePage.this.cachedUserTimeZone.containsKey(userId)) {
				final IUserService userService = BEANS.get(IUserService.class);
				final String currentUserTimeZone = userService.getUserTimeZone(userId);

				if (null == currentUserTimeZone) {
					LOG.warn("User " + userId + " hasen't set is timezone !");
				} else {
					AbstractEventsTablePage.this.cachedUserTimeZone.put(userId, currentUserTimeZone);
				}
			}

			timeZoneValid = null != AbstractEventsTablePage.this.cachedUserTimeZone.get(userId);

			return timeZoneValid;
		}

		protected Boolean isUserTimeZoneValid() {
			final Long currentUserId = AbstractEventsTablePage.this.getAppUserHelper().getCurrentUserId();

			return this.isTimeZoneValid(currentUserId);
		}

		public void resetCacheUserTimeZone(final Long userId) {
			AbstractEventsTablePage.this.cachedUserTimeZone.remove(userId);
		}

		protected Boolean canAutofillDates(final ITableRow row) {
			final Long hostId = this.getOrganizerColumn().getValue(row.getRowIndex());
			final Long attendeeId = this.getGuestIdColumn().getValue(row.getRowIndex());
			final Boolean startDateEmpty = null == this.getStartDateColumn().getValue(row.getRowIndex());
			final Boolean endDateEmpty = null == this.getEndDateColumn().getValue(row.getRowIndex());
			final String rowState = this.getStateColumn().getValue(row.getRowIndex());
			return null != row && "ASKED".equals(rowState) && startDateEmpty && endDateEmpty
					&& GoogleApiHelper.get().isCalendarConfigured(hostId)
					&& GoogleApiHelper.get().isCalendarConfigured(attendeeId) && this.isTimeZoneValid(attendeeId)
					&& this.isTimeZoneValid(hostId) && this.isGuestCurrentUser(row);
		}

		@Override
		protected void execDisposeTable() {
			final EventCreatedNotificationHandler eventCreatedNotificationHandler = BEANS
					.get(EventCreatedNotificationHandler.class);
			eventCreatedNotificationHandler.removeListener(this.eventCreatedListener);
			final EventModifiedNotificationHandler eventModifiedNotificationHandler = BEANS
					.get(EventModifiedNotificationHandler.class);
			eventModifiedNotificationHandler.removeListener(this.eventModifiedListener);

			final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
					.get(ApiCreatedNotificationHandler.class);
			apiCreatedNotificationHandler.removeListener(this.apiCreatedListener);

			super.execDisposeTable();
		}

		protected ITableRow getRow(final Long eventId) {
			final List<ITableRow> currentRows = this.getRows();
			for (final ITableRow aRow : currentRows) {
				if (eventId.equals(aRow.getCell(this.getEventIdColumn()).getValue())) {
					return aRow;
				}
			}

			return null;
		}

		@Override
		protected void execDecorateRow(final ITableRow row) {

			if (this.isHeldByCurrentUser(row)) {
				// row.setIconId(Icons.AngleDoubleLeft);
				this.getOrganizerEmailColumn().updateDisplayText(row, TEXTS.get("zc.common.me"));
			}

			if (this.isGuestCurrentUser(row)) {
				// row.setIconId(Icons.AngleDoubleRight);
				this.getEmailColumn().updateDisplayText(row, TEXTS.get("zc.common.me"));
			}

			final ZonedDateTime currentStartDate = this.getStartDateColumn().getZonedValue(row.getRowIndex());
			this.getStartDateColumn().updateDisplayText(row,
					AbstractEventsTablePage.this.getDateHelper().toUserDate(currentStartDate));

			final ZonedDateTime currentEndDate = this.getEndDateColumn().getZonedValue(row.getRowIndex());
			this.getEndDateColumn().updateDisplayText(row,
					AbstractEventsTablePage.this.getDateHelper().toUserDate(currentEndDate));

		}

		private List<Object> getListFromForm(final EventFormData formData) {
			// EventIdColumn[Id],
			// OrganizerColumn[Host],
			// OrganizerEmailColumn[Host],
			// SubjectField [Subject ]
			// GuestIdColumn[AttendeeId],
			// EmailColumn[Attendee],
			// DurationColumn[Duration],
			// StateColumn[State],
			// StartDateColumn[Start],
			// EndDateColumn[End],
			// SlotColumn[Slot],
			// ExternalIdOrganizerColumn[External Id],
			// ExternalIdRecipientColumn[External Id]
			// reasonColumn [reason]
			final List<Object> datas = new ArrayList<>();
			datas.add(formData.getEventId());
			datas.add(formData.getOrganizer().getValue());
			datas.add(formData.getOrganizerEmail().getValue());
			datas.add(formData.getGuestId().getValue());
			datas.add(formData.getEmail().getValue());
			datas.add(formData.getSubject().getValue());
			datas.add(formData.getSlot().getValue());
			datas.add(formData.getDuration().getValue());
			datas.add(formData.getState().getValue());
			datas.add(formData.getStartDate().getValue());
			datas.add(formData.getEndDate().getValue());
			datas.add(formData.getExternalIdOrganizer());
			datas.add(formData.getExternalIdRecipient());
			datas.add(formData.getReason().getValue());
			return datas;
		}

		protected ITableRow createTableRowFromForm(final EventFormData formData) {
			return new TableRow(this.getColumnSet(), this.getListFromForm(formData));
		}

		protected void updateTableRowFromForm(final ITableRow row, final EventFormData formData) {
			if (null != row) {
				final List<Object> datas = this.getListFromForm(formData);
				for (int i = 0; i < datas.size(); i++) {
					final Object propertyFormData = datas.get(i);
					final ICell cell = row.getCell(i);
					if (propertyFormData != cell) {
						// TODO enable validation ??
						// row.getTable().getColumns().get(i).setValue(row,
						// propertyFormData);
						row.setCellValue(i, propertyFormData);
					}
				}
			}
		}

		protected ITableRow getOwnerAsTableRow(final Object newOwnerValue) {
			if (newOwnerValue instanceof List) {
				final Object firstOwner = ((List) newOwnerValue).get(0);
				if (firstOwner instanceof ITableRow) {
					return (ITableRow) firstOwner;
				}
			}
			return null;
		}

		/**
		 * Is row Held by CurrentUser ?
		 *
		 * @param row
		 * @return
		 */
		protected Boolean isHeldByCurrentUser(final ITableRow row) {
			return AbstractEventsTablePage.this.isOrganizer(this.getOrganizerColumn().getValue(row.getRowIndex()));
		}

		/**
		 * Is current user Guest for row ?
		 *
		 * @param row
		 * @return
		 */
		protected Boolean isGuestCurrentUser(final ITableRow row) {
			final String rowEmail = this.getEmailColumn().getValue(row.getRowIndex());

			return this.isGuest(rowEmail);
		}

		protected Boolean isGuest(final String email) {
			final IUserService userService = BEANS.get(IUserService.class);
			final UserFormData userDetails = userService.getCurrentUserDetails();

			final String currentUserEmail = userDetails.getEmail().getValue();

			return currentUserEmail.equals(email);
		}

		protected String getState(final ITableRow row) {
			return Table.this.getStateColumn().getValue(row.getRowIndex());
		}

		protected Boolean userCanUpdate(final ITableRow row) {
			final Long currentEventId = Table.this.getEventIdColumn().getValue(row.getRowIndex());

			return ACCESS.check(new UpdateEventPermission(currentEventId));
		}

		protected Boolean userCanChooseDate(final ITableRow row) {
			// TODO Djer13 create a specific "ChooseDateEventPermisison" ?
			return this.userCanUpdate(row);
		}

		protected Boolean userCanAccept(final ITableRow row) {
			// TODO Djer13 create a specific "AcceptEventPermisison" ?
			return this.userCanUpdate(row);
		}

		protected Boolean userCanReject(final ITableRow row) {
			// TODO Djer13 create a specific "RejectEventPermisison" ?
			return this.userCanUpdate(row);
		}

		protected Boolean userCanCancel(final ITableRow row) {
			// TODO Djer13 create a specific "CancelEventPermisison" ?
			return this.userCanUpdate(row);
		}

		protected List<String> getCurrentUserEmails() {
			final List<String> emails = new ArrayList<>();
			final IUserService userService = BEANS.get(IUserService.class);
			final UserFormData userDetails = userService.getCurrentUserDetails();

			emails.add(userDetails.getEmail().getValue());

			return emails;
		}

		@Override
		protected void execRowClick(final ITableRow row, final MouseButton mouseButton) {
			this.reloadMenus(row);
		}

		/**
		 * Allow a "refresh" of menu even if no "owner" has changed
		 *
		 * @param row
		 *            the selected row (owner) may be null;
		 */
		protected void reloadMenus(final ITableRow row) {
			final List<IMenu> menus = this.getMenus();
			for (final IMenu menu : menus) {
				// menu.initAction();
				menu.handleOwnerValueChanged(row);
				// if (menu instanceof
				// AbstractEventsTablePage.Table.AbstractRowAwareMenu) {
				// @SuppressWarnings("unchecked")
				// final AbstractRowAwareMenu menuStateAware =
				// (AbstractRowAwareMenu) menu;
				// menuStateAware.handleRowChanged(row);
				// }
			}
		}

		protected void reloadMenus() {
			final ITableRow currentRow = this.getSelectedRow();
			if (null != currentRow) {
				this.reloadMenus(currentRow);
			} else {
				final IMenu newEventMenu = this.getMenuByClass(NewMenu.class);
				if (null != newEventMenu) {
					newEventMenu.setVisible(Boolean.TRUE);
				}
			}
		}

		/**
		 * save the current selected row to the database
		 *
		 * @return the saved event Datas
		 */
		protected EventFormData saveEventCurrentRow() {
			final EventFormData formData = this.saveEvent(this.getSelectedRow());
			this.reloadMenus(this.getSelectedRow());

			return formData;
		}

		private EventFormData saveEvent(final ITableRow selectedRow) {
			EventFormData eventFormData = new EventFormData();
			final Integer rowIndex = selectedRow.getRowIndex();
			final IEventService service = BEANS.get(IEventService.class);

			eventFormData.setEventId(this.getEventIdColumn().getValue(rowIndex));

			eventFormData = service.load(eventFormData);

			eventFormData.getStartDate().setValue(this.getStartDateColumn().getValue(rowIndex));
			eventFormData.getEndDate().setValue(this.getEndDateColumn().getValue(rowIndex));
			eventFormData.setExternalIdRecipient(this.getExternalIdRecipientColumn().getValue(rowIndex));
			eventFormData.setExternalIdOrganizer(this.getExternalIdOrganizerColumn().getValue(rowIndex));
			eventFormData.getState().setValue(this.getStateColumn().getValue(rowIndex));

			return service.store(eventFormData);

		}

		public abstract class AbstractRowAwareMenu extends AbstractMenu {
			public abstract void handleRowChanged(final ITableRow row);
		}

		@Order(3000)
		public class RefuseMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.refuse");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.ExclamationMark;
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

				Boolean iSVisible = Boolean.FALSE;
				if ("ASKED".equals(currentState) && Table.this.isGuestCurrentUser(Table.this.getSelectedRow())) {
					iSVisible = Boolean.TRUE;
				} else if ("ACCEPTED".equals(currentState)) {
					iSVisible = Boolean.TRUE;
				}
				return iSVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(this.isWorkflowVisible(Table.this.getState(row)) && Table.this.userCanReject(row));
					this.setEnabled(AbstractEventsTablePage.this.isUserCalendarConfigured());
					// TODO Djer13 hide if date.start and date.end are in the
					// past ?
				}
			}

			@Override
			protected void execAction() {
				final RejectEventForm form = new RejectEventForm();
				final Long currentEventId = Table.this.getEventIdColumn().getSelectedValue();
				form.setEventId(currentEventId);
				// form.addFormListener(new EventFormListener());
				form.setEnabledPermission(new UpdateEventPermission(currentEventId));
				// start the form using its modify handler
				form.startReject(Table.this.isHeldByCurrentUser(Table.this.getSelectedRow()));
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "r");
			}

		}

		@Order(4000)
		public class CancelMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("Cancel");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.ExclamationMark;
			}

			@Override
			protected boolean getConfiguredVisible() {
				// to avoid button blink
				return Boolean.FALSE;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			private Boolean isWorkflowVisible(final String currentState) {
				Boolean isVisible = Boolean.FALSE;
				if ("ASKED".equals(currentState) && Table.this.isHeldByCurrentUser(Table.this.getSelectedRow())) {
					isVisible = Boolean.TRUE;
				}
				return isVisible;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final ITableRow row = Table.this.getOwnerAsTableRow(newOwnerValue);
				if (null != row) {
					this.setVisible(this.isWorkflowVisible(Table.this.getState(row)) && Table.this.userCanCancel(row));
					this.setEnabled(AbstractEventsTablePage.this.isUserCalendarConfigured());
					// TODO Djer13 hide if date.start and date.end are in the
					// past ?
				}
			}

			@Override
			protected void execAction() {
				final RejectEventForm form = new RejectEventForm();
				final Long currentEventId = Table.this.getEventIdColumn().getSelectedValue();
				form.setEventId(currentEventId);
				// form.addFormListener(new EventFormListener());
				form.setEnabledPermission(new UpdateEventPermission(currentEventId));
				// start the form using its modify handler
				form.startCancel(Table.this.isHeldByCurrentUser(Table.this.getSelectedRow()));
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "c");
			}
		}

		/**
		 * @deprecated use toZonedDateTime(...) instead
		 * @param date
		 * @return
		 */
		@Deprecated
		protected LocalDateTime toLocalDateTime(final Date date) {
			LocalDateTime localDateTime = null;
			if (null != date) {
				localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
			}

			return localDateTime;
		}

		/**
		 * deprecated use toDate(ZonedDateTime) instead
		 *
		 * @deprecated
		 * @param localDateTime
		 * @return
		 */
		@Deprecated
		protected Date toDate(final LocalDateTime localDateTime) {
			return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}

		public AbstractEventsTablePage<?>.Table.DurationColumn getDurationColumn() {
			return this.getColumnSet().getColumnByClass(DurationColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.EmailColumn getEmailColumn() {
			return this.getColumnSet().getColumnByClass(EmailColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.StartDateColumn getStartDateColumn() {
			return this.getColumnSet().getColumnByClass(StartDateColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.EndDateColumn getEndDateColumn() {
			return this.getColumnSet().getColumnByClass(EndDateColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.StateColumn getStateColumn() {
			return this.getColumnSet().getColumnByClass(StateColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.ExternalIdOrganizerColumn getExternalIdOrganizerColumn() {
			return this.getColumnSet().getColumnByClass(ExternalIdOrganizerColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.ExternalIdRecipientColumn getExternalIdRecipientColumn() {
			return this.getColumnSet().getColumnByClass(ExternalIdRecipientColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.OrganizerEmailColumn getOrganizerEmailColumn() {
			return this.getColumnSet().getColumnByClass(OrganizerEmailColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.GuestIdColumn getGuestIdColumn() {
			return this.getColumnSet().getColumnByClass(GuestIdColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.SubjectColumn getSubjectColumn() {
			return this.getColumnSet().getColumnByClass(SubjectColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.ReasonColumn getReasonColumn() {
			return this.getColumnSet().getColumnByClass(ReasonColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.OrganizerColumn getOrganizerColumn() {
			return this.getColumnSet().getColumnByClass(OrganizerColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.SlotColumn getSlotColumn() {
			return this.getColumnSet().getColumnByClass(SlotColumn.class);
		}

		public AbstractEventsTablePage<?>.Table.EventIdColumn getEventIdColumn() {
			return this.getColumnSet().getColumnByClass(EventIdColumn.class);
		}

		@Order(1)
		public class EventIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.common.id");
			}

			@Override
			protected boolean getConfiguredDisplayable() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredPrimaryKey() {
				return true;
			}
		}

		@Order(25)
		public class OrganizerColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.hostId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}
		}

		@Order(30)
		public class OrganizerEmailColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(40)
		public class GuestIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.attendeeId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}
		}

		@Order(50)
		public class EmailColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.attendee");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(60)
		public class SubjectColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.subject");
			}

			@Override
			protected int getConfiguredWidth() {
				return 256;
			}
		}

		@Order(100)
		public class SlotColumn extends AbstractSmartColumn<Integer> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.slot");
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return SlotLookupCall.class;
			}
		}

		@Order(200)
		public class DurationColumn extends AbstractSmartColumn<Integer> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.duration");
			}

			@Override
			protected int getConfiguredWidth() {
				return 96;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return DurationLookupCall.class;
			}
		}

		@Order(1100)
		public class StateColumn extends AbstractSmartColumn<String> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.state");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected void execDecorateCell(final Cell cell, final ITableRow row) {
				super.execDecorateCell(cell, row);

				final String stateColumnValue = (String) cell.getValue();

				// TODO Djer13 optimization, useful to create a new lookup for
				// each cell ?
				final EventStateLookupCall stateLookUpCall = new EventStateLookupCall();
				final ILookupRow<String> stateLookupValue = stateLookUpCall.getDataById(stateColumnValue);

				cell.setIconId(stateLookupValue.getIconId());
				cell.setBackgroundColor(stateLookupValue.getBackgroundColor());
				cell.setForegroundColor(stateLookupValue.getForegroundColor());

			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return EventStateLookupCall.class;
			}
		}

		@Order(2000)
		public class StartDateColumn extends AbstractDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.start");
			}

			public ZonedDateTime getZonedValue(final int rowIndex) {
				return AbstractEventsTablePage.this.getDateHelper().getZonedValue(
						AbstractEventsTablePage.this.getAppUserHelper().getCurrentUserTimeZone(),
						this.getValue(rowIndex));
			}

			public ZonedDateTime getSelectedZonedValue() {
				return this.getSelectedZonedValue(
						AbstractEventsTablePage.this.getAppUserHelper().getCurrentUserTimeZone());
			}

			public ZonedDateTime getSelectedZonedValue(final ZoneId userZoneId) {
				final Date currentDate = super.getSelectedValue();
				return AbstractEventsTablePage.this.getDateHelper().getZonedValue(userZoneId, currentDate);
			}

			/**
			 * @deprecated use getSelectedZonedValue instead
			 * @return
			 */
			@Deprecated
			public LocalDateTime getSelectedLocalValue() {
				final Date currentValue = super.getSelectedValue();
				LocalDateTime retvalue = null;
				if (null != currentValue) {
					retvalue = Table.this.toLocalDateTime(currentValue);
				}
				return retvalue;
			}

			public void setValue(final int rowIndex, final ZonedDateTime rawValue) {
				if (null != rawValue) {
					this.setValue(this.getTable().getRow(rowIndex),
							AbstractEventsTablePage.this.getDateHelper().toDate(rawValue));
				}
			}

			public void setValue(final ITableRow row, final ZonedDateTime rawValue) {
				if (null != rawValue && null != row) {
					this.setValue(this.getTable().getRow(row.getRowIndex()),
							AbstractEventsTablePage.this.getDateHelper().toDate(rawValue));
				}
			}

			/**
			 * @deprecated use setValue(int, ZonedDateTime) instead
			 * @return
			 */
			@Deprecated
			public void setValue(final int rowIndex, final LocalDateTime rawValue) {
				if (null != rawValue) {
					this.setValue(this.getTable().getRow(rowIndex), Table.this.toDate(rawValue));
				}
			}

			/**
			 * @deprecated use setValue(ITableRow, ZonedDateTime) instead
			 * @return
			 */
			@Deprecated
			public void setValue(final ITableRow row, final LocalDateTime rawValue) {
				if (null != rawValue && null != row) {
					this.setValue(this.getTable().getRow(row.getRowIndex()), Table.this.toDate(rawValue));
				}
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return true;
			}

			// @Override
			// protected boolean getConfiguredEditable() {
			// return false;
			// }

			@Override
			protected int getConfiguredWidth() {
				return 140;
			}
		}

		@Order(3000)
		public class EndDateColumn extends AbstractDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.end");
			}

			public ZonedDateTime getZonedValue(final int rowIndex) {
				return AbstractEventsTablePage.this.getDateHelper().getZonedValue(
						AbstractEventsTablePage.this.getAppUserHelper().getCurrentUserTimeZone(),
						this.getValue(rowIndex));
			}

			public ZonedDateTime getSelectedZonedValue() {
				return this.getSelectedZonedValue(
						AbstractEventsTablePage.this.getAppUserHelper().getCurrentUserTimeZone());
			}

			public ZonedDateTime getSelectedZonedValue(final ZoneId userZoneId) {
				final Date currentDate = super.getSelectedValue();
				return AbstractEventsTablePage.this.getDateHelper().getZonedValue(userZoneId, currentDate);
			}

			/**
			 * @deprecated use getSelectedZonedValue instead
			 * @return
			 */
			@Deprecated
			public LocalDateTime getSelectedLocalValue() {
				final Date currentValue = super.getSelectedValue();
				LocalDateTime retvalue = null;
				if (null != currentValue) {
					retvalue = Table.this.toLocalDateTime(currentValue);
				}
				return retvalue;
			}

			public void setValue(final int rowIndex, final ZonedDateTime rawValue) {
				if (null != rawValue) {
					this.setValue(this.getTable().getRow(rowIndex),
							AbstractEventsTablePage.this.getDateHelper().toDate(rawValue));
				}
			}

			public void setValue(final ITableRow row, final ZonedDateTime rawValue) {
				if (null != rawValue && null != row) {
					this.setValue(this.getTable().getRow(row.getRowIndex()),
							AbstractEventsTablePage.this.getDateHelper().toDate(rawValue));
				}
			}

			/**
			 * @deprecated use setValue(int, ZonedDateTime) instead
			 * @return
			 */
			@Deprecated
			public void setValue(final int rowIndex, final LocalDateTime rawValue) {
				if (null != rawValue) {
					this.setValue(this.getTable().getRow(rowIndex), Table.this.toDate(rawValue));
				}
			}

			/**
			 * @deprecated use setValue(ITableRow, ZonedDateTime) instead
			 * @return
			 */
			@Deprecated
			public void setValue(final ITableRow row, final LocalDateTime rawValue) {
				if (null != rawValue && null != row) {
					this.setValue(this.getTable().getRow(row.getRowIndex()), Table.this.toDate(rawValue));
				}
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return true;
			}

			// @Override
			// protected boolean getConfiguredEditable() {
			// return false;
			// }

			@Override
			protected int getConfiguredWidth() {
				return 140;
			}
		}

		@Order(4500)
		public class ExternalIdOrganizerColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.externalId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(5000)
		public class ExternalIdRecipientColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.externalId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(6000)
		public class ReasonColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.rejectReason");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		// StartFrom
	}

	protected DateHelper getDateHelper() {
		return this.dateHelper;
	}

	protected AppUserHelper getAppUserHelper() {
		return this.appUserHelper;
	}

}
