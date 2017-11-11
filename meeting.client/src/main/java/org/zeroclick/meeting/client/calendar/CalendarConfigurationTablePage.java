package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationTablePage.Table;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

@Data(CalendarConfigurationTablePageData.class)
public class CalendarConfigurationTablePage extends AbstractCalendarConfigurationTablePage<Table> {

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ICalendarConfigurationService.class).getCalendarConfigurationTableData(filter,
				Boolean.FALSE));
	}

	public class Table extends AbstractCalendarConfigurationTablePage<Table>.Table {
	}
}
