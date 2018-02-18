package org.zeroclick.configuration.server.slot;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.BeanArrayHolder;
import org.eclipse.scout.rt.platform.holders.IBeanArrayHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.DayDurationModifiedNotification;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.configuration.shared.slot.SlotsFormData;
import org.zeroclick.configuration.shared.slot.SlotsFormData.SlotsTable.SlotsTableRowData;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchSlotTable;

public class SlotService extends AbstractCommonService implements ISlotService {

	private static final Logger LOG = LoggerFactory.getLogger(SlotService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	private String getSlotCode(final Long slotId) {
		super.checkPermission(new ReadSlotPermission(slotId));
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
		super.checkPermission(new ReadSlotPermission(formData.getSlotId()));

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
	public DayDurationFormData store(final DayDurationFormData formData) {
		super.checkPermission(new UpdateSlotPermission());
		SQL.update(SQLs.DAY_DURATION_UPDATE, formData);

		this.sendModifiedNotifications(formData);

		return formData;
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public SlotsFormData store(final SlotsFormData formData) {
		final SlotsTableRowData[] rows = formData.getSlotsTable().getRows();

		for (final SlotsTableRowData row : rows) {
			this.checkPermission(new UpdateSlotPermission());
			SQL.insert(SQLs.DAY_DURATION_UPDATE, row);
		}

		this.sendModifiedNotifications(formData);

		return formData;
	}

	private void sendModifiedNotifications(final DayDurationFormData formData) {
		this.sendModifiedNotifications(formData.getUserId(), formData.getSlotId(), formData);
	}

	private void sendModifiedNotifications(final Long userId, final Long slotId, final DayDurationFormData formData) {
		final String sltoCode = this.getSlotCode(slotId);
		final Set<String> notifiedUsers = this.buildNotifiedUsers(userId, Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new DayDurationModifiedNotification(formData, sltoCode));
	}

	private void sendModifiedNotifications(final SlotsFormData formData) {
		if (formData.getSlotsTable().getRowCount() > 0) {
			for (final SlotsTableRowData row : formData.getSlotsTable().getRows()) {
				this.sendModifiedNotifications(row.getUserId(), row.getSlotId(), this.toDayDurationForm(row));
			}
		} else {
			LOG.warn(
					"Cannot send user CalendarsConfigurationModifiedNotification because no User ID (no calendars modified)");
		}
	}

	private DayDurationFormData toDayDurationForm(final SlotsTableRowData row) {
		final DayDurationFormData dayDurationFormData = new DayDurationFormData();
		dayDurationFormData.setDayDurationId(row.getDayDurationId());
		dayDurationFormData.setName(row.getName());
		dayDurationFormData.setSlotCode(String.valueOf(row.getSlot()));
		dayDurationFormData.setSlotId(row.getSlotId());
		dayDurationFormData.setUserId(row.getUserId());
		dayDurationFormData.getMonday().setValue(row.getMonday());
		dayDurationFormData.getTuesday().setValue(row.getTuesday());
		dayDurationFormData.getWednesday().setValue(row.getWednesday());
		dayDurationFormData.getTuesday().setValue(row.getTuesday());
		dayDurationFormData.getFriday().setValue(row.getFriday());
		dayDurationFormData.getSaturday().setValue(row.getSaturday());
		dayDurationFormData.getSunday().setValue(row.getSunday());
		dayDurationFormData.getWeeklyPerpetual().setValue(row.getWednesday());
		dayDurationFormData.getOrderInSlot().setValue(Long.valueOf(row.getOrderInSlot()));
		return dayDurationFormData;
	}

	private SlotTablePageData getSlotData(final SearchFilter filter, final Boolean displayAllForAdmin) {
		// No permission check, not admin user automatically see is own data
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
		super.checkPermission(new ReadSlotPermission(slotId));

		final String sql = SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE
				+ SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID + SQLs.DAY_DURATION_SELECT_ORDER;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}

	@Override
	public Object[][] getDayDurationsLight(final Long slotId) {
		// Access on SLot implies access on DayDuration
		super.checkPermission(new ReadSlotPermission(slotId));

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

	private String buildDayDurationsSelect(final Long userId, final Boolean addInto, final String intoPrefix) {
		final StringBuilder sql = new StringBuilder(128);
		sql.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.DAY_DURATION_SELECT_SLOT_USER_ID)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND).append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_NAME);
		if (null != userId) {
			sql.append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_USER_ID);
		}
		sql.append(SQLs.DAY_DURATION_SELECT_ORDER);

		if (addInto) {
			if (null == intoPrefix) {
				sql.append(SQLs.DAY_DURATION_SELECT_INTO).append(SQLs.DAY_DURATION_SELECT_INTO_SLOT_USER_ID);
			} else {
				sql.append(this.addResultPrefix(SQLs.DAY_DURATION_SELECT_INTO, intoPrefix))
						.append(this.addResultPrefix(SQLs.DAY_DURATION_SELECT_INTO_SLOT_USER_ID, intoPrefix));
			}
		}

		return sql.toString();
	}

	private String addResultPrefix(final String sql, final String prefix) {
		return sql.replaceAll("\\:\\{", Matcher.quoteReplacement(":{" + prefix + "."));
	}

	@Override
	public List<DayDurationFormData> getDayDurations(final String slotName, final Long userId) {
		final Long slotId = this.getSlotId(slotName, userId);
		if (null == slotId) {
			return null; // early Break, Default Slot config should be return
		}

		if (ACCESS.getLevel(new ReadSlotPermission(slotId)) < ReadSlotPermission.LEVEL_INVOLVED) {
			super.throwAuthorizationFailed();
		}

		final IBeanArrayHolder<DayDurationFormData> results = new BeanArrayHolder<>(DayDurationFormData.class);

		final String sql = this.buildDayDurationsSelect(userId, Boolean.TRUE, "results");
		SQL.selectInto(sql, new NVPair("userId", userId), new NVPair("slotName", slotName),
				new NVPair("results", results));
		return null == results ? null : CollectionUtility.arrayList(results.getBeans((BeanArrayHolder.State[]) null));
	}

	private List<DayDurationFormData> getDayDurationsForAllUsers(final String slotName) {
		return this.getDayDurations(slotName, null);
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

	@Override
	public void createDefaultSlot(final Long userId) {
		LOG.info("Creating default Slot and DayDuration configuration for user ID : " + userId);
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
		final List<DayDurationFormData> dayDurations = this.getDayDurationsForAllUsers(slotName);

		if (null != dayDurations && dayDurations.size() > 0) {
			final Iterator<DayDurationFormData> itDayDurations = dayDurations.iterator();

			final UTCDate newValidStartEvenning = dateHelper.toScoutUTCDate(dateHelper.dateTimeFromHour(newStart));
			final UTCDate newValidEndEvenning = dateHelper.toScoutUTCDate(dateHelper.dateTimeFromHour(newEnd));

			final Date requiredStartDate = dateHelper.toDate(dateHelper.dateTimeFromHour(requiredStart));
			final Date requiredEndDate = dateHelper.toDate(dateHelper.dateTimeFromHour(requiredEnd));

			while (itDayDurations.hasNext()) {
				final DayDurationFormData aDayDuration = itDayDurations.next();
				if (aDayDuration.getSlotStart().getValue().equals(requiredStartDate)
						&& aDayDuration.getSlotEnd().getValue().equals(requiredEndDate)) {
					aDayDuration.getSlotStart().setValue(newValidStartEvenning);
					aDayDuration.getSlotEnd().setValue(newValidEndEvenning);
					this.store(aDayDuration);
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

		if (DatabaseHelper.get().isColumnExists("SLOT", "slot_code")) {
			SQL.insert(SQLs.SLOT_INSERT_SAMPLE_WITH_CODE + this.forSlot(SQLs.SLOT_VALUES_GENERIC_WITH_CODE, slotId)
					.replace("__slotCode__", String.valueOf(slotCode)).replace("__slotName__", slotName)
					.replace("__userId__", String.valueOf(userId)));
		} else {
			SQL.insert(SQLs.SLOT_INSERT_SAMPLE + this.forSlot(SQLs.SLOT_VALUES_GENERIC, slotId)
					.replace("__slotName__", slotName).replace("__userId__", String.valueOf(userId)));
		}
		return slotId;
	}

	private String forSlot(final String sql, final Long slotId) {
		return sql.replace("__slotId__", String.valueOf(slotId));
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void addDefaultCodeToExistingSlot() {
		// "isCurrentUserSuperUser" Ugly but required to allow datamigration
		// BEFORE super userCreated
		if (this.isCurrentUserSuperUser()
				|| ACCESS.getLevel(new ReadSlotPermission((Long) null)) == ReadSlotPermission.LEVEL_ALL) {

			final String sql = SQLs.SLOT_PAGE_SELECT;

			final Object[][] slots = SQL.select(sql);

			if (null != slots && slots.length > 0) {
				for (int row = 0; row < slots.length; row++) {

					final Object[] slot = slots[row];
					final String slotName = (String) slot[1];
					final Long slotId = (Long) slot[0];
					LOG.info("Adding default Slot code for slot : " + slotName + " (" + slotId + ")");
					final String slotCodeExtracted = slotName.substring(slotName.lastIndexOf('.') + 1,
							slotName.length());
					final Integer slotCode = Integer.valueOf(slotCodeExtracted);
					SQL.update(SQLs.SLOT_UPDATE_CODE, new NVPair("slotCode", slotCode), new NVPair("slotId", slotId));
				}
			}
		} else {
			LOG.error("A Non Slot admin user try to update all Slot Codes ! ");
		}
	}

	// @Override
	// public void migrateDayDurationTimeToUtc(final String slotName) {
	// final DateHelper dateHelper = BEANS.get(DateHelper.class);
	// final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
	// final List<DayDurationFormData> dayDurations =
	// this.getDayDurationsForAllUsers(slotName);
	//
	// if (null != dayDurations && dayDurations.size() > 0) {
	// final Iterator<DayDurationFormData> itDayDurations =
	// dayDurations.iterator();
	//
	// while (itDayDurations.hasNext()) {
	// final DayDurationFormData aDayDuration = itDayDurations.next();
	//
	// final Date curentStart = aDayDuration.getSlotStart().getValue();
	// final Date curentEnd = aDayDuration.getSlotEnd().getValue();
	//
	// final ZoneId userZoneId =
	// appUserHelper.getUserZoneId(aDayDuration.getUserId());
	//
	// final Date utcStartDate =
	// dateHelper.fromUtcDate(dateHelper.hoursToDate(curentStart), userZoneId);
	// final Date utcEndDate =
	// dateHelper.fromUtcDate(dateHelper.hoursToDate(curentEnd), userZoneId);
	//
	// LOG.info("Migrating DayDuration Time to UTC for User : " +
	// aDayDuration.getUserId() + ". Start from "
	// + curentStart + "(" + userZoneId + " ?) to " + utcStartDate + ", End from
	// " + curentEnd + "("
	// + userZoneId + " ?) to " + utcEndDate);
	//
	// aDayDuration.getSlotStart().setValue(utcStartDate);
	// aDayDuration.getSlotEnd().setValue(utcEndDate);
	// this.store(aDayDuration);
	// }
	// }
	// }

}
