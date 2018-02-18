package org.zeroclick.configuration.shared.slot;

import java.util.List;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface ISlotService extends IService {

	SlotsFormData store(SlotsFormData formData);

	SlotTablePageData getSlotTableData(SearchFilter filter);

	SlotTablePageData getDayDurationTableData(SearchFilter filter);

	SlotTablePageData getDayDurationAdminTableData(SearchFilter filter);

	DayDurationFormData store(DayDurationFormData formData);

	DayDurationFormData load(DayDurationFormData formData);

	Object[][] getSlots();

	Object[][] getDayDurations(Long slotId);

	Object[][] getDayDurationsLight(Long slotId);

	List<DayDurationFormData> getDayDurations(String slotName, Long userId);

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
