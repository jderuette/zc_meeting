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

	DayDurationFormData store(DayDurationFormData formData);

	DayDurationFormData load(DayDurationFormData formData);

	Object[][] getSlots();

	Object[][] getDayDurations(Long slotId);

	Object[][] getDayDurationsLight(Long slotId);
}
