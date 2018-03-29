package org.zeroclick.configuration.server.slot;

import java.util.ArrayList;
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
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractBulkSqlDataCache;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.AbstractFormDataCache;
import org.zeroclick.common.AbstractListFormDataCache;
import org.zeroclick.common.AbstractPageDataDataCache;
import org.zeroclick.common.BulkSqlData;
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

	private final AbstractFormDataCache<Long, DayDurationFormData> dataCacheDayDuration = new AbstractFormDataCache<Long, DayDurationFormData>() {
		@Override
		public DayDurationFormData loadForCache(final Long dayDurationId) {
			final DayDurationFormData dayDurationFormData = new DayDurationFormData();
			dayDurationFormData.setDayDurationId(dayDurationId);
			return SlotService.this.loadForCacheDayDuration(dayDurationFormData);
		}
	};

	private final AbstractBulkSqlDataCache<Long, BulkSqlData> dataCacheSlotsByUserId = new AbstractBulkSqlDataCache<Long, BulkSqlData>() {
		@Override
		public BulkSqlData loadForCache(final Long userId) {
			return SlotService.this.loadForCacheSlotByUserId(userId);
		}
	};

	private final AbstractListFormDataCache<Long, List<DayDurationFormData>> dataCacheDayDurationByUserId = new AbstractListFormDataCache<Long, List<DayDurationFormData>>() {
		@Override
		public List<DayDurationFormData> loadForCache(final Long userId) {
			return SlotService.this.loadForCacheDayDurationByUserId(userId);
		}
	};

	private final AbstractPageDataDataCache<Long, SlotTablePageData> dataCacheSlotTableDataDayDurationByUserId = new AbstractPageDataDataCache<Long, SlotTablePageData>() {
		@Override
		public SlotTablePageData loadForCache(final Long userId) {
			return SlotService.this.loadForCacheSlotTableDayDurationByUserId(userId);
		}
	};

	private ICache<Long, DayDurationFormData> getDataCacheDayDuration() {
		return this.dataCacheDayDuration.getCache();
	}

	private ICache<Long, BulkSqlData> getDataCacheSlotByUserId() {
		return this.dataCacheSlotsByUserId.getCache();
	}

	private ICache<Long, List<DayDurationFormData>> getDataCacheDayDurationByUserId() {
		return this.dataCacheDayDurationByUserId.getCache();
	}

	private ICache<Long, SlotTablePageData> getDataCacheSlotTableDataDayDurationByUserId() {
		return this.dataCacheSlotTableDataDayDurationByUserId.getCache();
	}

	protected DayDurationFormData loadForCacheDayDuration(final DayDurationFormData dayDurationFormData) {
		final StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.SLOT_SELECT_FILEDS)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND).append(SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID)
				.append(SQLs.DAY_DURATION_SELECT_INTO);
		SQL.selectInto(sqlBuilder.toString(), dayDurationFormData,
				new NVPair("dayDurationId", dayDurationFormData.getDayDurationId()));

		return dayDurationFormData;
	}

	protected BulkSqlData loadForCacheSlotByUserId(final Long userId) {
		final String sql = SQLs.SLOT_PAGE_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID;
		final Object[][] results = SQL.select(sql, new NVPair("currentUser", userId));

		return new BulkSqlData(results);
	}

	protected List<DayDurationFormData> loadForCacheDayDurationByUserId(final Long userId) {
		final IBeanArrayHolder<DayDurationFormData> results = new BeanArrayHolder<>(DayDurationFormData.class);

		final StringBuilder sql = new StringBuilder(128);
		sql.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.DAY_DURATION_SELECT_SLOT_USER_ID)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND);
		// .append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_NAME);
		if (null != userId) {
			sql.append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_USER_ID);
		}
		sql.append(SQLs.DAY_DURATION_SELECT_ORDER)
				.append(this.addResultPrefix(SQLs.DAY_DURATION_SELECT_INTO, "results"))
				.append(this.addResultPrefix(SQLs.DAY_DURATION_SELECT_INTO_SLOT_USER_ID, "results"));

		SQL.selectInto(sql.toString(), new NVPair("userId", userId), new NVPair("results", results));

		return null == results ? null : CollectionUtility.arrayList(results.getBeans((BeanArrayHolder.State[]) null));
	}

	protected SlotTablePageData loadForCacheSlotTableDayDurationByUserId(final Long userId) {
		final SlotTablePageData pageData = new SlotTablePageData();

		final StringBuilder sql = new StringBuilder();
		sql.append(SQLs.DAY_DURATION_PAGE_SELECT);
		if (null != userId) {
			sql.append(SQLs.SLOT_SELECT_FILTER_USER_ID);
		}
		sql.append(SQLs.DAY_DURATION_PAGE_SELECT_INTO);

		SQL.selectInto(sql.toString(), new NVPair("page", pageData), new NVPair("currentUser", userId));

		return pageData;
	}

	private String addResultPrefix(final String sql, final String prefix) {
		return sql.replaceAll("\\:\\{", Matcher.quoteReplacement(":{" + prefix + "."));
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

		DayDurationFormData cachedData = this.getDataCacheDayDuration().get(formData.getDayDurationId());
		if (null == cachedData) {
			// avoid NPE
			cachedData = formData;
		}

		return cachedData;
	}

	@Override
	public DayDurationFormData store(final DayDurationFormData formData) {
		super.checkPermission(new UpdateSlotPermission());
		SQL.update(SQLs.DAY_DURATION_UPDATE, formData);

		this.sendModifiedNotifications(formData);

		this.clearCache(formData.getDayDurationId(), formData.getUserId());

		return formData;
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public SlotsFormData store(final SlotsFormData formData) {
		final SlotsTableRowData[] rows = formData.getSlotsTable().getRows();

		for (final SlotsTableRowData row : rows) {
			this.checkPermission(new UpdateSlotPermission());
			SQL.insert(SQLs.DAY_DURATION_UPDATE, row);

			this.clearCache(row.getDayDurationId(), row.getUserId());
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
		SlotTablePageData result = new SlotTablePageData();
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadSlotPermission((Long) null)) != ReadSlotPermission.LEVEL_ALL) {

			final SlotTablePageData cachedData = this.getDataCacheSlotTableDataDayDurationByUserId()
					.get(super.userHelper.getCurrentUserId());
			if (null != cachedData) {
				// TODO Djer permission check on each Slot, for the reader
				// (userId)
				result = cachedData;
			}
		} else {
			// ICache.get(Null) does not work, manual load for admins
			result = this.loadForCacheSlotTableDayDurationByUserId(null);
		}

		return result;
	}

	@Override
	public SlotTablePageData getDayDurationTableData(final SearchFilter filter) {
		return this.getDayDurationData(filter, Boolean.FALSE);
	}

	@Override
	public SlotTablePageData getDayDurationAdminTableData(final SearchFilter filter) {
		return this.getDayDurationData(filter, Boolean.TRUE);
	}

	private Long getSlotId(final String slotName, final Long userId) {
		// No permission check here to allow involved user to get ID of a slot
		// by name (during day, lunch, ...)
		Long slotId = null;

		final BulkSqlData cachedData = this.getDataCacheSlotByUserId().get(userId);

		if (null != cachedData && null != cachedData.getData() && cachedData.getData().length > 0) {
			// filter by Slot Name
			for (final Object[] slotRow : cachedData.getData()) {
				if (null != slotRow && null != slotRow[1] && slotName.equals(slotRow[1])) {
					slotId = (Long) slotRow[0];
					break;
				}
			}
		}

		return slotId;
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

		List<DayDurationFormData> dayDurationsPageData = null;

		if (null != userId) {
			dayDurationsPageData = this.getDataCacheDayDurationByUserId().get(userId);
		} else {
			// Null does not work as Key for cache, manual load
			dayDurationsPageData = this.loadForCacheDayDurationByUserId(null);
		}

		final List<DayDurationFormData> dayDurations = new ArrayList<>();

		if (null != dayDurationsPageData && dayDurationsPageData.size() > 0) {
			// extract valid slot by SlotId
			for (final DayDurationFormData dayDuration : dayDurationsPageData) {
				if (slotId.equals(dayDuration.getSlotId())) {
					dayDurations.add(dayDuration);
				}
			}
		}

		return dayDurations;
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

	private void clearCache(final Long dayDurationId, final Long userId) {
		this.dataCacheDayDuration.clearCache(dayDurationId);
		this.dataCacheDayDurationByUserId.clearCache(userId);
		this.dataCacheSlotsByUserId.clearCache(userId);
		this.dataCacheSlotTableDataDayDurationByUserId.clearCache(userId);
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
