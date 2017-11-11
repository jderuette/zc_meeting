package org.zeroclick.configuration.shared.slot;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface ISlotService extends IService {

	SlotFormData prepareCreate(SlotFormData formData);

	SlotFormData create(SlotFormData formData);

	SlotFormData load(SlotFormData formData);

	SlotFormData store(SlotFormData formData);

	SlotTablePageData getSlotTableData(SearchFilter filter);

	SlotTablePageData getDayDurationTableData(SearchFilter filter);

	SlotTablePageData getDayDurationAdminTableData(SearchFilter filter);

	DayDurationFormData store(DayDurationFormData formData);

	DayDurationFormData load(DayDurationFormData formData);

	Object[][] getSlots();

	Object[][] getDayDurations(Long slotId);

	Object[][] getDayDurationsLight(Long slotId);

	/**
	 * retrieve CURRENT USER list of daySuration for the Slot name
	 *
	 * @param slotName
	 * @return
	 */
	Object[][] getDayDurations(String slotName);

	Object[][] getDayDurations(String slotName, Long userId);

	boolean isOwn(Long slotId);

	boolean isInvolved(Long slotId);

	void createDefaultSlot(Long userId);

	/**
	 * For data migration when slot Code column is added to model
	 */
	void addDefaultCodeToExistingSlot();

	/**
	 * Update all Day Duration in the slot "slotName" having the requiredStart
	 * AND the requiredEnd to the new newStart AND newEnd
	 *
	 * @param slotName
	 * @param requiredStart
	 * @param requiredEnd
	 * @param newStart
	 * @param newEnd
	 */
	void updateDayDurationsByTemplate(String slotName, String requiredStart, String requiredEnd, String newStart,
			String newEnd);
}
