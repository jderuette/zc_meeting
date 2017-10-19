package org.zeroclick.configuration.server.slot;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.configuration.shared.slot.CreateSlotPermission;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.DayDurationModifiedNotification;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.configuration.shared.slot.SlotFormData;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchSlotTable;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class SlotService extends CommonService implements ISlotService {

	private static final Logger LOG = LoggerFactory.getLogger(SlotService.class);

	@Override
	public SlotFormData prepareCreate(final SlotFormData formData) {
		if (!ACCESS.check(new CreateSlotPermission())) {
			super.throwAuthorizationFailed();
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public SlotFormData create(final SlotFormData formData) {
		if (!ACCESS.check(new CreateSlotPermission())) {
			super.throwAuthorizationFailed();
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public SlotFormData load(final SlotFormData formData) {
		// Filter in select (currentUser) check if user can at least read own
		if (ACCESS.getLevel(new ReadSlotPermission((Long) null)) >= ReadSlotPermission.LEVEL_OWN) {
			super.throwAuthorizationFailed();
		}

		SQL.selectInto(SQLs.SLOT_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID + SQLs.SLOT_PAGE_SELECT_INTO, formData,
				new NVPair("node", formData), new NVPair("currentUser", this.userHelper.getCurrentUserId()));
		return formData;
	}

	private String getSlotCode(final Long slotId) {
		if (!ACCESS.check(new ReadSlotPermission(slotId))) {
			super.throwAuthorizationFailed();
		}
		String slotCode = null;

		final Object[][] result = SQL.select(SQLs.SLOT_SELECT + SQLs.SLOT_SELECT_FILTER_SLOT_ID,
				new NVPair("slotId", slotId));

		if (null != result && result.length != 0 && null != result[0] && result[0].length >= 1) {
			final String slotName = (String) result[0][1];
			if (null != slotName) {
				slotCode = slotName.substring(slotName.lastIndexOf('.') + 1);
			}
		}
		return slotCode;
	}

	@Override
	public DayDurationFormData load(final DayDurationFormData formData) {
		// Access on Slot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(formData.getSlotId()))) {
			super.throwAuthorizationFailed();
		}

		final StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.SLOT_SELECT_FILEDS)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND).append(SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID)
				.append(SQLs.DAY_DURATION_SELECT_INTO);

		// SQL.selectInto(
		// SQLs.DAY_DURATION_SELECT +
		// SQLs.DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE
		// + SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID +
		// SQLs.DAY_DURATION_SELECT_INTO,
		// formData, new NVPair("dayDurationId", formData.getDayDurationId()));

		SQL.selectInto(sqlBuilder.toString(), formData, new NVPair("dayDurationId", formData.getDayDurationId()));
		return formData;
	}

	@Override
	public SlotFormData store(final SlotFormData formData) {
		if (!ACCESS.check(new UpdateSlotPermission())) {
			super.throwAuthorizationFailed();
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public DayDurationFormData store(final DayDurationFormData formData) {
		if (!ACCESS.check(new UpdateSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		SQL.update(SQLs.DAY_DURATION_UPDATE, formData);

		this.sendModifiedNotifications(formData);

		return formData;
	}

	private Set<String> buildNotifiedUsers(final DayDurationFormData formData) {
		// Notify Users for DayDuration update
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		final Set<String> notifiedUsers = new HashSet<>();
		if (null != formData.getUserId()) {
			notifiedUsers.addAll(acs.getUserNotificationIds(formData.getUserId()));
		}

		// inform all attendee with pending event
		// TODO Djer13 only event with "current" slotId ?
		final Set<Long> pendingUsers = this.getUserWithPendingEvent();
		if (null != pendingUsers) {
			for (final Long userId : pendingUsers) {
				notifiedUsers.addAll(acs.getUserNotificationIds(userId));
			}
		}
		return notifiedUsers;
	}

	private void sendModifiedNotifications(final DayDurationFormData formData) {
		final String sltoCode = this.getSlotCode(formData.getSlotId());
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new DayDurationModifiedNotification(formData, sltoCode));
	}

	private SlotTablePageData getSlotData(final SearchFilter filter, final Boolean displayAllForAdmin) {
		final SlotTablePageData pageData = new SlotTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadSlotPermission((Long) null)) != ReadSlotPermission.LEVEL_ALL) {
			ownerFilter = SQLs.SLOT_SELECT_FILTER_USER_ID;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.SLOT_PAGE_SELECT + ownerFilter + SQLs.SLOT_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public SlotTablePageData getSlotTableData(final SearchFilter filter) {
		return this.getSlotData(filter, Boolean.FALSE);
	}

	@Override
	public Object[][] getSlots() {
		// Filter in select (currentUser) check if user can at least read own
		if (ACCESS.getLevel(new ReadSlotPermission((Long) null)) < ReadSlotPermission.LEVEL_OWN) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.SLOT_PAGE_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID;

		final Object[][] results = SQL.select(sql, new NVPair("currentUser", super.userHelper.getCurrentUserId()));

		return results;
	}

	private SlotTablePageData getDayDurationData(final SearchFilter filter, final Boolean displayAllForAdmin) {
		final SlotTablePageData pageData = new SlotTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadSlotPermission((Long) null)) != ReadSlotPermission.LEVEL_ALL) {
			ownerFilter = SQLs.SLOT_SELECT_FILTER_USER_ID;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.DAY_DURATION_PAGE_SELECT + ownerFilter + SQLs.DAY_DURATION_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public SlotTablePageData getDayDurationTableData(final SearchFilter filter) {
		return this.getDayDurationData(filter, Boolean.FALSE);
	}

	@Override
	public SlotTablePageData getDayDurationAdminTableData(final SearchFilter filter) {
		return this.getDayDurationData(filter, Boolean.TRUE);
	}

	@Override
	public Object[][] getDayDurations(final Long slotId) {
		// Access on SLot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(slotId))) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE
				+ SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID + SQLs.DAY_DURATION_SELECT_ORDER;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}

	@Override
	public Object[][] getDayDurationsLight(final Long slotId) {
		// Access on SLot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(slotId))) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.DAY_DURATION_SELECT_LIGHT + SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID
				+ SQLs.DAY_DURATION_SELECT_ORDER;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}

	private Long getSlotId(final String slotName, final Long userId) {
		// No permission check here to allow involved user to get ID of a slot
		// by name (during day, lunch, ...)
		Long slotId = null;
		final String sql = SQLs.SLOT_SELECT_ID_BY_NAME;

		final Object[][] results = SQL.select(sql, new NVPair("slotName", slotName), new NVPair("userId", userId));

		if (null != results && results.length != 0 && results[0] != null && results[0].length != 0) {
			slotId = (Long) results[0][0];
		}
		return slotId;
	}

	@Override
	public Object[][] getDayDurations(final String slotName) {
		return this.getDayDurations(slotName, super.userHelper.getCurrentUserId());
	}

	@Override
	public Object[][] getDayDurations(final String slotName, final Long userId) {
		// get Slot Id (if user has a specific configuration)
		final Long slotId = this.getSlotId(slotName, userId);
		if (null == slotId) {
			return null; // early Break, Default Slot config should be return
		}

		if (ACCESS.getLevel(new ReadSlotPermission(slotId)) < ReadSlotPermission.LEVEL_INVOLVED) {
			super.throwAuthorizationFailed();
		}

		final String sql = this.buildDayDurationsSelect(userId, Boolean.FALSE);

		final Object[][] results = SQL.select(sql, new NVPair("slotName", slotName), new NVPair("userId", userId));

		return results;
	}

	private String buildDayDurationsSelect(final Long userId, final Boolean addInto) {
		final StringBuilder sql = new StringBuilder(128);
		sql.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.SLOT_SELECT_FILEDS)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND).append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_NAME);
		if (null != userId) {
			sql.append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_USER_ID);
		}
		sql.append(SQLs.DAY_DURATION_SELECT_ORDER);

		if (addInto) {
			sql.append(SQLs.DAY_DURATION_SELECT_INTO);
		}

		return sql.toString();
	}

	private List<DayDurationFormData> getDayDurationsForAllUsers(final String slotName) {
		if (ACCESS.getLevel(new ReadSlotPermission((Long) null)) != ReadSlotPermission.LEVEL_ALL) {
			super.throwAuthorizationFailed();
		}
		final List<DayDurationFormData> results = new ArrayList<>();

		final String sql = this.buildDayDurationsSelect(null, Boolean.TRUE);

		SQL.selectInto(sql, new NVPair("slotName", slotName), new NVPair("", results));

		return results;

	}

	private Long getOwner(final Long slotId) {
		Long slotUserId = Long.valueOf(-1l);
		final Object[][] result = SQL.select(SQLs.SLOT_SELECT_OWNER, new NVPair("slotId", slotId));
		if (null != result && result.length > 0) {
			slotUserId = (Long) result[0][0];
		}

		return slotUserId;
	}

	@Override
	public boolean isOwn(final Long slotId) {
		Boolean isOwn = Boolean.FALSE;
		final Long currentUserId = super.userHelper.getCurrentUserId();

		final Long slotOwner = this.getOwner(slotId);

		if (null == slotOwner) {
			LOG.error("Slot " + slotId + " as NO owner (userId)");
			isOwn = Boolean.FALSE;
		} else if (slotOwner.equals(currentUserId)) {
			isOwn = Boolean.TRUE;
		}

		return isOwn;
	}

	@Override
	public boolean isInvolved(final Long slotId) {
		final Long slotOwner = this.getOwner(slotId);

		final Set<Long> pendingUsers = this.getUserWithPendingEvent();

		return pendingUsers.contains(slotOwner);
	}

	private Set<Long> getUserWithPendingEvent() {
		Set<Long> pendingMeetingUser = new HashSet<>();

		final IEventService eventService = BEANS.get(IEventService.class);
		final Map<Long, Integer> pendingUsers = eventService.getUsersWithPendingMeeting();

		if (null != pendingUsers) {
			pendingMeetingUser = pendingUsers.keySet();
		}

		return pendingMeetingUser;

	}

	@Override
	public void createDefaultSlot(final Long userId) {
		Long slotId = this.createSlot("zc.meeting.slot.1", userId, 1);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_MORNING, slotId));
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_AFTERNOON, slotId));

		slotId = this.createSlot("zc.meeting.slot.2", userId, 2);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_LUNCH, slotId));

		slotId = this.createSlot("zc.meeting.slot.3", userId, 3);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_EVENING, slotId));

		slotId = this.createSlot("zc.meeting.slot.4", userId, 4);
		SQL.insert(SQLs.DAY_DURATION_INSERT_SAMPLE + this.forSlot(SQLs.DAY_DURATION_VALUES_WWEEKEND, slotId));
	}

	@Override
	public void updateDayDurationsByTemplate(final String slotName, final String requiredStart,
			final String requiredEnd, final String newStart, final String newEnd) {
		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		final List<DayDurationFormData> evenningDayDuration = this.getDayDurationsForAllUsers(slotName);

		if (null != evenningDayDuration && evenningDayDuration.size() > 0) {
			final Iterator<DayDurationFormData> itWorkDays = evenningDayDuration.iterator();

			final Date newValidStartEvenning = dateHelper
					.toDate(ZonedDateTime.parse(newStart, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
			final Date newValidEndEvenning = dateHelper
					.toDate(ZonedDateTime.parse(newEnd, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));

			while (itWorkDays.hasNext()) {
				final DayDurationFormData anEveningDayDuration = itWorkDays.next();
				if (anEveningDayDuration.getSlotStart().getValue().equals(requiredStart)
						&& anEveningDayDuration.getSlotEnd().getValue().equals(requiredEnd)) {
					anEveningDayDuration.getSlotStart().setValue(newValidStartEvenning);
					anEveningDayDuration.getSlotEnd().setValue(newValidEndEvenning);
					this.store(anEveningDayDuration);
				}
			}
		}

	}

	/**
	 * Create a new Slot and return the Slot Id
	 *
	 * @param slotName
	 * @param userId
	 * @return
	 */
	private Long createSlot(final String slotName, final Long userId, final Integer slotCode) {
		// TODO Djer permission check ? (should be against permissions on (new)
		// userId
		final Long slotId = DatabaseHelper.get().getNextVal(PatchSlotTable.SLOT_ID_SEQ);
		SQL.insert(SQLs.SLOT_INSERT_SAMPLE
				+ this.forSlot(SQLs.SLOT_VALUES_GENERIC, slotId).replace("__slotCode__", String.valueOf(slotCode))
						.replace("__slotName__", slotName).replace("__userId__", String.valueOf(userId)));
		return slotId;
	}

	private String forSlot(final String sql, final Long slotId) {
		return sql.replace("__slotId__", String.valueOf(slotId));
	}

	@Override
	public void addDefaultCodeToExistingSlot() {
		final String sql = SQLs.SLOT_PAGE_SELECT;

		final Object[][] slots = SQL.select(sql);

		if (null != slots && slots.length > 0) {
			for (int row = 0; row < slots.length; row++) {
				LOG.info("Adding default Slot code for slot");
				final Object[] slot = slots[row];
				final String slotName = (String) slot[1];
				final Long slotId = (Long) slot[0];
				final String slotCodeExtracted = slotName.substring(slotName.lastIndexOf('.') + 1, slotName.length());
				final Integer slotCode = Integer.valueOf(slotCodeExtracted);
				SQL.update(SQLs.SLOT_UPDATE_CODE, new NVPair("slotCode", slotCode), new NVPair("slotId", slotId));
			}
		}
	}

}
