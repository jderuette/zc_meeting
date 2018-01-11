package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;

@Data(CalendarConfigurationTablePageData.class)
public class CalendarConfigurationTablePage extends AbstractCalendarConfigurationTablePage<Table> {

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		// Data loaded by the Table itself
		// this.importPageData(
		// BEANS.get(ICalendarConfigurationService.class).getCalendarConfigurationTableData(filter,
		// Boolean.TRUE));
	}

	public class Table extends AbstractCalendarConfigurationTablePage<Table>.Table {

		@Override
		protected boolean getConfiguredDisplayAllUsers() {
			return true;
		}
	}
}
