package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface ICalendarConfigurationService extends IService {

	CalendarConfigurationTablePageData getCalendarConfigurationTableData(SearchFilter filter,
			Boolean displayAllForAdmin);
}
