package org.zeroclick.configuration.server.slot;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.configuration.shared.slot.CreateSlotPermission;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.configuration.shared.slot.SlotFormData;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.event.IEventService;

public class SlotService extends CommonService implements ISlotService {

	private static final Logger LOG = LoggerFactory.getLogger(SlotService.class);

	@Override
	public SlotFormData prepareCreate(final SlotFormData formData) {
		if (!ACCESS.check(new CreateSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public SlotFormData create(final SlotFormData formData) {
		if (!ACCESS.check(new CreateSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
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

	@Override
	public DayDurationFormData load(final DayDurationFormData formData) {
		// Access on Slot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(formData.getSlotId()))) {
			super.throwAuthorizationFailed();
		}

		SQL.selectInto(
				SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID
						+ SQLs.DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE + SQLs.DAY_DURATION_SELECT_INTO,
				formData, new NVPair("dayDurationId", formData.getDayDurationId()));
		return formData;
	}

	@Override
	public SlotFormData store(final SlotFormData formData) {
		if (!ACCESS.check(new UpdateSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
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

		return formData;
	}

	@Override
	public SlotTablePageData getSlotTableData(final SearchFilter filter) {
		final SlotTablePageData pageData = new SlotTablePageData();

		final Long currentConnectedUserId = super.userHelper.getCurrentUserId();

		final String sql = SQLs.SLOT_PAGE_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID + SQLs.SLOT_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public Object[][] getSlots() {
		// Filter in select (currentUser) check if user can at least read own
		if (ACCESS.getLevel(new ReadSlotPermission((Long) null)) >= ReadSlotPermission.LEVEL_OWN) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.SLOT_PAGE_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID;

		final Object[][] results = SQL.select(sql, new NVPair("currentUser", super.userHelper.getCurrentUserId()));

		return results;
	}

	@Override
	public Object[][] getDayDurations(final Long slotId) {
		// Access on SLot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(slotId))) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE
				+ SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}

	@Override
	public Object[][] getDayDurationsLight(final Long slotId) {
		// Access on SLot implies access on DayDuration
		if (!ACCESS.check(new ReadSlotPermission(slotId))) {
			super.throwAuthorizationFailed();
		}

		final String sql = SQLs.DAY_DURATION_SELECT_LIGHT + SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID;

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

		final StringBuilder sql = new StringBuilder();
		sql.append(SQLs.DAY_DURATION_SELECT).append(", ").append(SQLs.SLOT_SELECT_FILEDS)
				.append(SQLs.DAY_DURATION_SELECT_FROM).append(SQLs.DAY_DURATION_JOIN_SLOT)
				.append(SQLs.GENERIC_WHERE_FOR_SECURE_AND).append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_NAME)
				.append(SQLs.DAY_DURATION_SELECT_FILTER_SLOT_USER_ID);

		final Object[][] results = SQL.select(sql.toString(), new NVPair("slotName", slotName),
				new NVPair("userId", userId));

		return results;

	}

	private Long getOwner(final Long slotId) {
		final Long slotUserId = Long.valueOf(-1l);
		SQL.selectInto(SQLs.SLOT_SELECT_OWNER, new NVPair("slotId", slotId), new NVPair("userId", slotUserId));

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

		final IEventService eventService = BEANS.get(IEventService.class);
		final Map<Long, Integer> pendingUsers = eventService.getUsersWithPendingMeeting();

		return pendingUsers.containsKey(slotOwner) ? pendingUsers.get(slotOwner) > 0 : Boolean.FALSE;
	}

}
