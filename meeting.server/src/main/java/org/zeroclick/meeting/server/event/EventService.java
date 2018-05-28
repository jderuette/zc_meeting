package org.zeroclick.meeting.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.configuration.shared.slot.SlotCodeType;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.event.EventAskedTablePageData;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventFormData.State;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.EventStateCodeType;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.event.involevment.EventRoleCodeType;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;
import org.zeroclick.meeting.shared.event.involevment.InvolvmentStateCodeType;

public class EventService extends AbstractCommonService implements IEventService {

	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	private String createMeFilter(final Boolean displayAllForAdmin) {
		final StringBuilder meFilter = new StringBuilder(64);
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			meFilter.append(SQLs.EVENT_FILTER_INVOLVED_USER);
		}

		return meFilter.toString();
	}

	private String createInvolvmentStateFilter(final Boolean invited, final Boolean asked,
			final Boolean displayAllForAdmin) {
		final StringBuilder stateFilter = new StringBuilder();
		String stateCode = "";

		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			if (invited) {
				stateCode = InvolvmentStateCodeType.AskedCode.ID;
			} else if (asked) {
				stateCode = InvolvmentStateCodeType.ProposedCode.ID;
			}
			stateFilter.append(" AND invo.state = '").append(stateCode).append('\'');
		}

		return stateFilter.toString();
	}

	private String createEventStateFilter() {
		return SQLs.EVENT_FILTER_STATE;
	}

	private EventTablePageData getEvents(final SearchFilter filter, final String ownerFilter,
			final String stateFilter) {
		final EventTablePageData pageData = new EventTablePageData();

		final Long currentConnectedUserId = super.userHelper.getCurrentUserId();

		final String sql = SQLs.EVENT_PAGE_SELECT_ORGANIZED + ownerFilter + stateFilter
				+ SQLs.EVENT_PAGE_DATA_SELECT_INTO_ORGANIZED;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("userId", currentConnectedUserId),
				new NVPair("currentUserEmail", this.getCurrentUserEmail()));

		return pageData;
	}

	private EventTablePageData getEventsOld(final SearchFilter filter, final String eventStatusCriteria,
			final Boolean displayAllForAdmin) {
		final EventTablePageData pageData = new EventTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			ownerFilter = SQLs.EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.EVENT_PAGE_SELECT_OLD + ownerFilter + eventStatusCriteria
				+ SQLs.EVENT_PAGE_DATA_SELECT_INTO_OLD;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId),
				new NVPair("currentUserEmail", this.getCurrentUserEmail()));

		return pageData;
	}

	@Override
	public EventTablePageData getEventTableData(final SearchFilter filter) {
		final Boolean admin = Boolean.FALSE;

		final EventTablePageData pageData = new EventTablePageData();

		String stateFilterValue = null;
		String stateFilter = "";
		if (null != filter && null != filter.getFormData()
				&& null != filter.getFormData().getFieldByClass(State.class)) {
			stateFilterValue = filter.getFormData().getFieldByClass(State.class).getValue();
		}

		final String meFilter = this.createMeFilter(admin);
		if (null != stateFilterValue) {
			stateFilter = this.createEventStateFilter();
		}

		final Long currentConnectedUserId = super.userHelper.getCurrentUserId();
		final String sql = SQLs.EVENT_PAGE_SELECT + meFilter + stateFilter + SQLs.EVENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("userId", currentConnectedUserId),
				new NVPair("eventState", stateFilterValue));

		return pageData;
	}

	@Override
	public EventAskedTablePageData getEventAskedTableData(final SearchFilter filter) {
		final Boolean admin = Boolean.FALSE;

		final String meFilter = this.createMeFilter(admin);
		final String stateFilter = this.createEventStateFilter();

		final EventAskedTablePageData pageData = new EventAskedTablePageData();

		final Long currentConnectedUserId = super.userHelper.getCurrentUserId();
		final String sql = SQLs.EVENT_PAGE_SELECT + meFilter + stateFilter + SQLs.EVENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("userId", currentConnectedUserId),
				new NVPair("eventState", EventStateCodeType.WaitingCode.ID));

		return pageData;
	}

	@Override
	public AbstractTablePageData getEventProcessedTableData(final SearchFilter filter) {
		final Boolean admin = Boolean.FALSE;
		final String meFilter = this.createMeFilter(admin);
		final String stateFilter = this.createEventStateFilter();
		return this.getEvents(filter, meFilter, stateFilter);
	}

	@Override
	public AbstractTablePageData getEventAdminTableData(final SearchFilter filter) {
		final Boolean admin = Boolean.TRUE;
		final String meFilter = this.createMeFilter(admin);
		final String stateFilter = this.createEventStateFilter();
		return this.getEvents(filter, meFilter, stateFilter);
	}

	@Override
	public EventTablePageData getEventAdminTableDataOld(final SearchFilter filter) {
		return this.getEventsOld(filter,
				" AND state = '" + EventStateCodeType.WaitingCode.ID + "' AND guest_id=:currentUser", Boolean.FALSE);
	}

	@Override
	public Map<Long, Integer> getUsersWithPendingMeeting() {
		return this.getNbEventsByUser(null, Boolean.FALSE, null);
	}

	@Override
	public Map<Long, Integer> getUsersWithPendingMeeting(final Long forUserId) {
		return this.getNbEventsByUser(null, Boolean.FALSE, forUserId);
	}

	@Override
	public Map<Long, Integer> getNbEventsByUser(final String state) {
		return this.getNbEventsByUser(state, Boolean.FALSE);
	}

	@Override
	public Map<Long, Integer> getNbEventsByUser(final String state, final Boolean onlyAsOrganizer,
			final Long forUserId) {
		final Map<Long, Integer> users = new HashMap<>();
		final Long currentUser = super.userHelper.getCurrentUserId();
		Long userId = forUserId;

		if (null == userId) {
			userId = currentUser;
		}

		final boolean isMySelf = userId.equals(currentUser);
		if (!isMySelf) {
			this.checkPermission(new ReadEventPermission(userId));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Loading pending meeting users with : ").append(userId)
					.append(" (Only as organizer : ").append(onlyAsOrganizer).append(")").toString());
		}

		if (!onlyAsOrganizer) {
			final Object[][] pendingOrganizer = this.getEventsByUser(SQLs.EVENT_SELECT_USERS_EVENT_GUEST, state,
					userId);
			if (null != pendingOrganizer && pendingOrganizer.length > 0) {
				for (int i = 0; i < pendingOrganizer.length; i++) {
					final Long pendingUserOrganizer = (Long) pendingOrganizer[i][1];
					if (!users.containsKey(pendingUserOrganizer)) {
						users.put(pendingUserOrganizer, 0);
					}
					Integer currentNbEvent = users.get(pendingUserOrganizer);
					users.put(pendingUserOrganizer, ++currentNbEvent);
				}
			}
		}

		final Object[][] pendingAttendee = this.getEventsByUser(SQLs.EVENT_SELECT_USERS_EVENT_HOST, state, userId);

		if (null != pendingAttendee && pendingAttendee.length > 0) {
			for (int i = 0; i < pendingAttendee.length; i++) {
				final Long pendingUserAttendee = (Long) pendingAttendee[i][1];
				if (!users.containsKey(pendingUserAttendee)) {
					users.put(pendingUserAttendee, 0);
				}
				Integer currentNbEvent = users.get(pendingUserAttendee);
				users.put(pendingUserAttendee, ++currentNbEvent);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("List of pending meeting (Only as organizer : ").append(onlyAsOrganizer)
					.append(") users with : ").append(userId).append(" : ").append(users).toString());
		}
		return users;
	}

	@Override
	public Map<Long, Integer> getNbEventsByUser(final String state, final Boolean onlyAsOrganizer) {
		final Long currentUser = super.userHelper.getCurrentUserId();
		return this.getNbEventsByUser(state, onlyAsOrganizer, currentUser);

	}

	private Object[][] getEventsByUser(final String sqlBase, final String state, final Long forUser) {
		if (!forUser.equals(super.userHelper.getCurrentUserId())
				&& ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			this.throwAuthorizationFailed();
		}

		Object[][] eventByUsers;

		final StringBuilder eventByUsersSql = new StringBuilder();
		final List<Object> eventByUsersBindBases = new ArrayList<>();

		eventByUsersSql.append(sqlBase);
		eventByUsersBindBases.add(new NVPair("currentUser", forUser));

		if (null != state) {
			eventByUsersSql.append(SQLs.EVENT_SELECT_FILTER_SATE);
			eventByUsersBindBases.add(new NVPair("state", state));
		}
		eventByUsers = SQL.select(eventByUsersSql.toString(),
				CollectionUtility.toArray(eventByUsersBindBases, Object.class));

		return eventByUsers;
	}

	@Override
	public EventFormData prepareCreate(final EventFormData formData) {
		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);
		final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();
		if (!subscriptionData.isAccessAllowed()) {
			super.throwAuthorizationFailed();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("PrepareCreate for Event");
		}

		formData.getState().setValue(EventStateCodeType.WaitingCode.ID);
		formData.getDuration().setValue(DurationCodeType.HalfHourCode.ID);
		formData.getSlot().setValue(SlotCodeType.DayCode.ID);

		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		formData.getCreatedDate().setValue(dateHelper.nowUtc());

		formData.getOrganizer().setValue(super.userHelper.getCurrentUserId());

		return formData;
	}

	@Override
	public EventFormData create(final EventFormData formData) {
		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);
		final IInvolvementService involevmentService = BEANS.get(IInvolvementService.class);

		final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();
		if (!subscriptionData.isAccessAllowed()) {
			super.throwAuthorizationFailed();
		}
		// add a unique event id if necessary
		if (null == formData.getEventId()) {
			formData.setEventId(Long.valueOf(SQL.getSequenceNextval("EVENT_ID_SEQ")));
		}

		// retrieve guest UserID
		formData.getGuestId().setValue(this.retrieveUserId(formData.getEmail().getValue()));

		formData.setLastModifier(super.userHelper.getCurrentUserId());

		SQL.insert(SQLs.EVENT_INSERT, formData);
		final EventFormData storedData = this.store(formData, Boolean.TRUE);

		// Organizer Involvement
		final InvolvementFormData involevmentFormData = new InvolvementFormData();
		involevmentFormData.getEventId().setValue(storedData.getEventId());
		involevmentFormData.getUserId().setValue(storedData.getOrganizer().getValue());
		involevmentFormData.getRole().setValue(EventRoleCodeType.OrganizerCode.ID);
		involevmentFormData.getState().setValue(InvolvmentStateCodeType.ProposedCode.ID);
		involevmentFormData.getInvitedBy().setValue(storedData.getOrganizer().getValue());
		involevmentService.create(involevmentFormData);

		// Guest Involvement
		final InvolvementFormData guestInvolevmentFormData = new InvolvementFormData();
		guestInvolevmentFormData.getEventId().setValue(storedData.getEventId());
		guestInvolevmentFormData.getUserId().setValue(storedData.getGuestId().getValue());
		guestInvolevmentFormData.getRole().setValue(EventRoleCodeType.RequiredGuestCode.ID);
		guestInvolevmentFormData.getState().setValue(InvolvmentStateCodeType.AskedCode.ID);
		guestInvolevmentFormData.getInvitedBy().setValue(storedData.getOrganizer().getValue());
		involevmentService.create(guestInvolevmentFormData);

		final Set<String> notifiedUsers = this.buildNotifiedUsers(storedData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new EventCreatedNotification(storedData));

		return this.store(storedData, Boolean.TRUE);
	}

	private Set<String> buildNotifiedUsers(final EventFormData formData) {
		// Notify Users for EventTable update
		final Set<String> notifiedUsers = new HashSet<>();
		if (null != formData.getGuestId().getValue()) {
			notifiedUsers.addAll(this.getUserNotificationIds(formData.getGuestId().getValue()));
		}
		if (null != formData.getOrganizer().getValue()) {
			notifiedUsers.addAll(this.getUserNotificationIds(formData.getOrganizer().getValue()));
		}
		return notifiedUsers;
	}

	private void sendModifiedNotifications(final EventFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers, new EventModifiedNotification(formData));
	}

	private Long retrieveUserId(final String email) {
		final IUserService userService = BEANS.get(IUserService.class);
		return userService.getUserIdByEmail(email);
	}

	@Override
	public EventFormData load(final EventFormData formData) {
		super.checkPermission(new ReadEventPermission(formData.getEventId()));
		SQL.selectInto(SQLs.EVENT_SELECT, formData);

		// if (null == formData.getDescription().getValue()) {
		// // force load BLOB data
		// final Object[][] eventDescription = SQL.select(
		// SQLs.EVENT_SELECT_DESCRIPTION_DATA_ONLY + SQLs.EVENT_FILTER_EVENT_ID,
		// new NVPair("eventId", formData.getEventId()));
		// if (eventDescription.length == 1) {
		// formData.getDescription().setValue(String.valueOf(eventDescription[0][0]));
		// }
		// }

		formData.getDescription().setValue(this.byteToStringField(formData.getDescriptionData()));
		return formData;
	}

	@Override
	public EventFormData load(final Long eventId) {
		// permission check done by load(FormData)
		final EventFormData formData = new EventFormData();
		formData.setEventId(eventId);
		return this.load(formData);
	}

	@Override
	public RejectEventFormData load(final RejectEventFormData formData) {
		super.checkPermission(new ReadEventPermission(formData.getEventId()));
		SQL.selectInto(SQLs.EVENT_SELECT_REJECT, formData);
		return formData;
	}

	private EventFormData store(final EventFormData formData, final Boolean duringCreate) {
		super.checkPermission(new UpdateEventPermission(formData.getEventId()));
		if (null == formData.getLastModifier()) {
			formData.setLastModifier(super.userHelper.getCurrentUserId());
		}

		if (null != formData.getDescription().getValue()) {
			formData.setDescriptionData(this.stringFiledToByte(formData.getDescription().getValue()));
		}

		SQL.update(SQLs.EVENT_UPDATE, formData);

		this.updateParticipantsPermissions(formData);

		if (!duringCreate) {
			this.sendModifiedNotifications(formData);
		}
		return formData;
	}

	@Override
	public String loadDescription(final Long eventId) {
		String description = null;
		final Object[][] eventDescription = SQL.select(
				SQLs.EVENT_SELECT_DESCRIPTION_DATA_ONLY + SQLs.EVENT_FILTER_EVENT_ID, new NVPair("eventId", eventId));
		if (eventDescription.length == 1) {
			description = this.byteToStringField((byte[]) eventDescription[0][0]);
		}

		return description;
	}

	@Override
	public EventFormData store(final EventFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	@Override
	public EventFormData storeNewState(final RejectEventFormData formData) {
		final String newState = formData.getState();
		if (EventStateCodeType.CanceledCode.ID.equals(newState)) {
			return this.storeNewState(formData, this.userHelper.getCurrentUserId());
		} else {
			return this.storeNewState(formData, null);
		}
	}

	@Override
	public EventFormData storeNewState(final RejectEventFormData formData, final Long userIdRefusing) {
		super.checkPermission(new UpdateEventPermission(formData.getEventId()));
		SQL.update(SQLs.EVENT_UPDATE_STATE, formData, new NVPair("refusedBy", userIdRefusing));

		// reload the **full** event data after update
		final EventFormData eventFormData = new EventFormData();
		eventFormData.setEventId(formData.getEventId());
		eventFormData.setLastModifier(super.userHelper.getCurrentUserId());
		this.load(eventFormData);

		eventFormData.setPreviousState(formData.getState());

		this.sendModifiedNotifications(eventFormData);

		return eventFormData;
	}

	@Override
	public EventFormData storeNewState(final Long eventId, final String newState) {
		super.checkPermission(new UpdateEventPermission(eventId));

		// Save the currentState
		final EventFormData formData = new EventFormData();
		formData.setEventId(eventId);
		final EventFormData previousStateEvent = this.load(formData);

		SQL.update(SQLs.EVENT_UPDATE_STATE, new NVPair("eventId", eventId), new NVPair("newState", newState));

		// reload the **full** event data after update
		final EventFormData eventFormData = new EventFormData();
		eventFormData.setEventId(eventId);
		this.load(eventFormData);

		eventFormData.setPreviousState(previousStateEvent.getState().getValue());

		this.sendModifiedNotifications(eventFormData);

		return eventFormData;
	}

	private Long getOwner(final Long eventId) {
		final EventFormData formData = new EventFormData();
		formData.setEventId(eventId);
		SQL.selectInto(SQLs.EVENT_SELECT_OWNER, formData);

		return formData.getOrganizer().getValue();
	}

	private String getRecipient(final Long eventId) {
		final EventFormData formData = new EventFormData();
		formData.setEventId(eventId);
		SQL.selectInto(SQLs.EVENT_SELECT_RECIPIENT, formData);

		return formData.getEmail().getValue();
	}

	protected Set<String> getKnowEmail(final ILookupCall<String> call, final Boolean strict) {
		final Set<String> knowEmail = new HashSet<>();
		final Long currentUserId = super.userHelper.getCurrentUserId();

		final Object[][] attendeesData = SQL.select(SQLs.EVENT_SELECT_KNOWN_ATTENDEE_LOOKUP,
				new NVPair("currentUser", currentUserId), new NVPair("call", call));
		for (int row = 0; row < attendeesData.length; row++) {
			knowEmail.add((String) attendeesData[row][0]);
		}

		final Object[][] hostsData = SQL.select(SQLs.EVENT_SELECT_KNOWN_HOST_LOOKUP,
				new NVPair("currentUser", currentUserId), new NVPair("call", call));
		for (int row = 0; row < hostsData.length; row++) {
			knowEmail.add((String) hostsData[row][0]);
		}

		return knowEmail;
	}

	@Override
	public Set<String> getKnowEmail(final ILookupCall<String> call) {
		return this.getKnowEmail(call, Boolean.FALSE);

	}

	@Override
	public Set<String> getKnowEmailByKey(final ILookupCall<String> call) {
		return this.getKnowEmail(call, Boolean.TRUE);
	}

	@Override
	public boolean isOwn(final Long eventId) {
		Boolean isOwn = Boolean.FALSE;
		final Long currentUserId = super.userHelper.getCurrentUserId();

		final Long eventOwner = this.getOwner(eventId);

		if (null == eventOwner) {
			LOG.error(
					new StringBuffer().append("Event ").append(eventId).append(" as NO owner (organizer)").toString());
			isOwn = Boolean.FALSE;
		} else if (eventOwner.equals(currentUserId)) {
			isOwn = Boolean.TRUE;
		}

		return isOwn;
	}

	@Override
	public boolean isRecipient(final Long eventId) {
		Boolean isRecipient = Boolean.FALSE;
		final String eventRecipient = this.getRecipient(eventId);
		if (null == eventRecipient || "".equals(eventRecipient)) {
			LOG.error(
					new StringBuffer().append("Event ").append(eventId).append(" as NO recipient (email)").toString());
			isRecipient = Boolean.FALSE;
		} else if (eventRecipient.equalsIgnoreCase(this.getCurrentUserEmail())) {
			isRecipient = Boolean.TRUE;
		}
		return isRecipient;

	}

	/**
	 * allow "related" permissions to be updated
	 *
	 * @param formData
	 */
	private void updateParticipantsPermissions(final EventFormData formData) {
		final IUserService userService = BEANS.get(IUserService.class);

		userService.clearCache(formData.getOrganizer().getValue());
		userService.clearCache(formData.getGuestId().getValue());
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void migrateDurationlookupToCodeType() {
		LOG.info("Updating Event duration from minutes to IDs");
		final DurationCodeType durationCodeType = new DurationCodeType();
		final List<? extends ICode<Long>> existingCodes = durationCodeType.getCodes(Boolean.FALSE);

		for (final ICode<Long> code : existingCodes) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuffer().append("Updating event with durantion : ").append(code.getValue())
						.append(" with id : ").append(code.getId()).toString());
			}
			final String sql = "UPDATE event set duration=:durationId where duration=:durationminutes";
			SQL.update(sql, new NVPair("durationId", code.getId()),
					new NVPair("durationminutes", code.getValue().intValue()));
		}
	}

}
