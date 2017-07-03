package org.oneclick.meeting.server.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.configuration.shared.user.IUserService;
import org.oneclick.configuration.shared.user.UserFormData;
import org.oneclick.meeting.server.sql.SQLs;
import org.oneclick.meeting.shared.event.CreateEventPermission;
import org.oneclick.meeting.shared.event.EventCreatedNotification;
import org.oneclick.meeting.shared.event.EventFormData;
import org.oneclick.meeting.shared.event.EventModifiedNotification;
import org.oneclick.meeting.shared.event.EventTablePageData;
import org.oneclick.meeting.shared.event.IEventService;
import org.oneclick.meeting.shared.event.ReadEventPermission;
import org.oneclick.meeting.shared.event.RejectEventFormData;
import org.oneclick.meeting.shared.event.UpdateEventPermission;
import org.oneclick.meeting.shared.security.AccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventService implements IEventService {

	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

	private EventTablePageData getEvents(final SearchFilter filter, final String eventStatusCriteria) {
		final EventTablePageData pageData = new EventTablePageData();

		String OwnerFilter = "";
		Long currentConnectedUserId = 0L;
		if (ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			OwnerFilter = SQLs.EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT;
			final AccessControlService acs = BEANS.get(AccessControlService.class);
			currentConnectedUserId = acs.getZeroClickUserIdOfCurrentSubject();
		}

		final String sql = SQLs.EVENT_PAGE_SELECT + OwnerFilter + eventStatusCriteria
				+ SQLs.EVENT_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId),
				new NVPair("currentUserEmail", this.getCurrentUserEmail()));

		return pageData;
	}

	@Override
	public EventTablePageData getEventTableData(final SearchFilter filter) {
		return this.getEvents(filter, " AND state = 'ASKED'");
	}

	@Override
	public AbstractTablePageData getEventProcessedTableData(final SearchFilter filter) {
		return this.getEvents(filter, " AND state <> 'ASKED'");
	}

	@Override
	public List<Long> getUsersWithPendingMeeting() {
		final List<Long> users = new ArrayList<>();

		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUser = acs.getZeroClickUserIdOfCurrentSubject();

		LOG.debug("Loading pending meeting users with : " + currentUser);

		Object[][] pendingOrganizer;
		pendingOrganizer = SQL.select(SQLs.EVENT_SELECT_USERS_PENDING_EVENT_GUEST,
				new NVPair("currentUser", currentUser));

		Object[][] pendingAttendee;
		pendingAttendee = SQL.select(SQLs.EVENT_SELECT_USERS_PENDING_EVENT_HOST,
				new NVPair("currentUser", currentUser));

		if (null != pendingOrganizer) {
			for (int i = 0; i < pendingOrganizer.length; i++) {
				users.add((Long) pendingOrganizer[i][0]);
			}
		}

		if (null != pendingAttendee) {
			for (int i = 0; i < pendingAttendee.length; i++) {
				users.add((Long) pendingAttendee[i][1]);
			}
		}

		LOG.debug("List of pending meeting users with : " + currentUser + " : " + users);
		return users;

	}

	@Override
	public EventFormData prepareCreate(final EventFormData formData) {
		if (!ACCESS.check(new CreateEventPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		LOG.debug("PrepareCreate for Event");

		// TODO Djer move EventStateLookupCall to shared part ?
		formData.getState().setValue("ASKED");
		// TODO Djer move DurationLookupCall to shared Part ?
		formData.getDuration().setValue(30);
		// TODO Djer move SlotLookup to shared Part ?
		formData.getSlot().setValue(1);

		final AccessControlService acs = BEANS.get(AccessControlService.class);
		formData.getOrganizer().setValue(acs.getZeroClickUserIdOfCurrentSubject());

		return formData;
	}

	@Override
	public EventFormData create(final EventFormData formData) {
		if (!ACCESS.check(new CreateEventPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		// add a unique event id if necessary
		if (null == formData.getEventId()) {
			formData.setEventId(new Long(UUID.randomUUID().hashCode()));
		}

		// retrieve guest UserID
		formData.getGuestId().setValue(this.retrieveUserId(formData.getEmail().getValue()));

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
			// final Long guestId =
			// this.retrieveUserId(formData.getEmail().getValue());
			notifiedUsers.addAll(acs.getUserNotificationIds(formData.getGuestId().getValue()));
		}
		if (null != formData.getOrganizer().getValue()) {
			// final Long organizerId =
			// this.retrieveUserId(formData.getOrganizerEmail().getValue());
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
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.selectInto(SQLs.EVENT_SELECT, formData);
		return formData;
	}

	@Override
	public RejectEventFormData load(final RejectEventFormData formData) {
		if (!ACCESS.check(new ReadEventPermission(formData.getEventId()))) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.selectInto(SQLs.EVENT_SELECT_REJECT, formData);
		return formData;
	}

	private EventFormData store(final EventFormData formData, final Boolean duringCreate) {
		if (!ACCESS.check(new UpdateEventPermission(formData.getEventId()))) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
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
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.update(SQLs.EVENT_UPDATE_STATE, formData);

		// reload the **full** event data after update
		final EventFormData eventFormData = new EventFormData();
		eventFormData.setEventId(formData.getEventId());
		this.load(eventFormData);

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

	@Override
	public boolean isOwn(final Long eventId) {
		Boolean isOwn = Boolean.FALSE;
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();

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
