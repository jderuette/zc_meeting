package org.zeroclick.configuration.server.slot;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.common.CommonService;
import org.zeroclick.configuration.shared.slot.CreateSlotPermission;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.configuration.shared.slot.SlotFormData;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.configuration.shared.slot.UpdateSlotPermission;
import org.zeroclick.meeting.server.sql.SQLs;

public class SlotService extends CommonService implements ISlotService {

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
		if (!ACCESS.check(new ReadSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}

		// SQL.selectInto(
		// "SELECT slot_id, name, user_id FROM SLOT WHERE 1=1" + " AND
		// user_id=:currentUser"
		// + " INTO :{node.slotId}, :{node.name}, :{node.userId}",
		// formData, new NVPair("currentUser",
		// this.userHelper.getCurrentUserId()));

		SQL.selectInto(SQLs.SLOT_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID + SQLs.SLOT_PAGE_SELECT_INTO, formData,
				new NVPair("node", formData), new NVPair("currentUser", this.userHelper.getCurrentUserId()));
		return formData;
	}

	@Override
	public DayDurationFormData load(final DayDurationFormData formData) {
		if (!ACCESS.check(new ReadSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}

		SQL.select(SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID,
				new NVPair("dayDurationId", formData.getDayDurationId()));

		SQL.selectInto(
				SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID
						+ SQLs.DAY_DURATION_SELECT_INTO,
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
		if (!ACCESS.check(new ReadSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}

		final String sql = SQLs.SLOT_PAGE_SELECT + SQLs.SLOT_SELECT_FILTER_USER_ID;

		final Object[][] results = SQL.select(sql, new NVPair("currentUser", super.userHelper.getCurrentUserId()));

		return results;
	}

	@Override
	public Object[][] getDayDurations(final Long slotId) {
		if (!ACCESS.check(new ReadSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}

		final String sql = SQLs.DAY_DURATION_SELECT + SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}

	@Override
	public Object[][] getDayDurationsLight(final Long slotId) {
		if (!ACCESS.check(new ReadSlotPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}

		final String sql = SQLs.DAY_DURATION_SELECT_LIGHT + SQLs.DAY_DURATION_SELECT_FILTER_SLOT_ID;

		final Object[][] results = SQL.select(sql, new NVPair("slotId", slotId));

		return results;
	}
}
