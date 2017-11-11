package org.zeroclick.meeting.server.calendar;

import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.common.CommonService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.event.ReadEventPermission;

public class CalendarConfigurationService extends CommonService implements ICalendarConfigurationService {

	@Override
	public CalendarConfigurationTablePageData getCalendarConfigurationTableData(final SearchFilter filter,
			final Boolean displayAllForAdmin) {
		final CalendarConfigurationTablePageData pageData = new CalendarConfigurationTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		// FIXME Djer13 use AgendaConfig permissions instead of event
		if (!displayAllForAdmin
				|| ACCESS.getLevel(new ReadEventPermission((Long) null)) != ReadEventPermission.LEVEL_ALL) {
			ownerFilter = SQLs.AGENDA_CONFIG_PAGE_SELECT_FILTER_USER;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.AGENDA_CONFIG_PAGE_SELECT + ownerFilter + SQLs.AGENDA_CONFIG_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));
		return pageData;
	}
}
