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
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper.SubscriptionHelperData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.EventTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class EventService extends CommonService implements IEventService {

	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

	private EventTablePageData getEvents(final SearchFilter filter, final String eventStatusCriteria,
			final Boolean displayAllForAdmin) {
		final EventTablePageData pageData = new EventTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			ownerFilter = SQLs.EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.EVENT_PAGE_SELECT + ownerFilter + eventStatusCriteria
				+ SQLs.EVENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId),
				new NVPair("currentUserEmail", this.getCurrentUserEmail()));

		return pageData;
	}

	@Override
	public EventTablePageData getEventTableData(final SearchFilter filter) {
		return this.getEvents(filter, " AND state = 'ASKED' AND guest_id=:currentUser", Boolean.FALSE);
	}

	@Override
	public AbstractTablePageData getEventAskedTableData(final SearchFilter filter) {
		return this.getEvents(filter, " AND state = 'ASKED' AND guest_id!=:currentUser", Boolean.FALSE);
	}

	@Override
	public AbstractTablePageData getEventProcessedTableData(final SearchFilter filter) {
		return this.getEvents(filter, " AND state <> 'ASKED'", Boolean.FALSE);
	}

	@Override
	public AbstractTablePageData getEventAdminTableData(final SearchFilter filter) {
		return this.getEvents(filter, "", Boolean.TRUE);
	}

	@Override
	public Map<Long, Integer> getNbEventsByUser(final String state) {
		final Map<Long, Integer> users = new HashMap<>();

		final Long currentUser = super.userHelper.getCurrentUserId();
		LOG.debug("Loading pending meeting users with : " + currentUser);

		final Object[][] pendingOrganizer = this.getEventsByUser(SQLs.EVENT_SELECT_USERS_EVENT_GUEST, state,
				currentUser);
		final Object[][] pendingAttendee = this.getEventsByUser(SQLs.EVENT_SELECT_USERS_EVENT_HOST, state, currentUser);

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

		LOG.debug("List of pending meeting users with : " + currentUser + " : " + users);
		return users;
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
	public Map<Long, Integer> getUsersWithPendingMeeting() {
		return this.getNbEventsByUser(null);
	}

	@Override
	public EventFormData prepareCreate(final EventFormData formData) {
		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);
		final SubscriptionHelperData subscriptionData = subHelper.canCreateEvent();
		if (!subscriptionData.isAccessAllowed()) {
			super.throwAuthorizationFailed();
		}
		LOG.debug("PrepareCreate for Event");

		// TODO Djer move EventStateLookupCall to shared part ?
		formData.getState().setValue("ASKED");
		// TODO Djer move DurationLookupCall to shared Part ?
		formData.getDuration().setValue(30);
		// TODO Djer move SlotLookup to shared Part ?
		formData.getSlot().setValue(1);

		formData.getOrganizer().setValue(super.userHelper.getCurrentUserId());

		return formData;
	}

	@Override
	public EventFormData create(final EventFormData formData) {
		final SubscriptionHelper subHelper = BEANS.get(SubscriptionHelper.class);
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

		final Set<String> notifiedUsers = this.buildNotifiedUsers(storedData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new EventCreatedNotification(storedData));

		return storedData;
	}

	private Set<String> buildNotifiedUsers(final EventFormData formData) {
		// Notify Users for EventTable update
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		final Set<String> notifiedUsers = new HashSet<>();
		if (null != formData.getGuestId().getValue()) {
			notifiedUsers.addAll(acs.getUserNotificationIds(formData.getGuestId().getValue()));
		}
		if (null != formData.getOrganizer().getValue()) {
			notifiedUsers.addAll(acs.getUserNotificationIds(formData.getOrganizer().getValue()));
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
		if (!ACCESS.check(new ReadEventPermission(formData.getEventId()))) {
			super.throwAuthorizationFailed();
		}
		SQL.selectInto(SQLs.EVENT_SELECT, formData);
		return formData;
	}

	@Override
	public RejectEventFormData load(final RejectEventFormData formData) {
		if (!ACCESS.check(new ReadEventPermission(formData.getEventId()))) {
			super.throwAuthorizationFailed();
		}
		SQL.selectInto(SQLs.EVENT_SELECT_REJECT, formData);
		return formData;
	}

	private EventFormData store(final EventFormData formData, final Boolean duringCreate) {
		if (!ACCESS.check(new UpdateEventPermission(formData.getEventId()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getLastModifier()) {
			formData.setLastModifier(super.userHelper.getCurrentUserId());
		}
		SQL.update(SQLs.EVENT_UPDATE, formData);

		if (!duringCreate) {

			this.sendModifiedNotifications(formData);
		}
		return formData;
	}

	@Override
	public EventFormData store(final EventFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	@Override
	public EventFormData storeNewState(final RejectEventFormData formData) {
		if (!ACCESS.check(new UpdateEventPermission(formData.getEventId()))) {
			super.throwAuthorizationFailed();
		}
		SQL.update(SQLs.EVENT_UPDATE_STATE, formData);

		// reload the **full** event data after update
		final EventFormData eventFormData = new EventFormData();
		eventFormData.setEventId(formData.getEventId());
		eventFormData.setLastModifier(super.userHelper.getCurrentUserId());
		this.load(eventFormData);

		eventFormData.setPreviousState(formData.getState());

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
			LOG.error("Event " + eventId + " as NO owner (organizer)");
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
			LOG.error("Event " + eventId + " as NO recipient (email)");
			isRecipient = Boolean.FALSE;
		} else if (eventRecipient.equals(this.getCurrentUserEmail())) {
			isRecipient = Boolean.TRUE;
		}
		return isRecipient;

	}

	private String getCurrentUserEmail() {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();

		return userDetails.getEmail().getValue();
	}
}
