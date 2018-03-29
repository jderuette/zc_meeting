package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationAdminTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;

@Data(CalendarConfigurationTablePageData.class)
public class CalendarConfigurationAdminTablePage extends AbstractCalendarConfigurationTablePage<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.configuration.admin");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		// manual load, to allow correct handle of refresh (F5)
		this.getTable().loadData();
	}

	public class Table extends AbstractCalendarConfigurationTablePage<Table>.Table {

		@Override
		protected boolean getConfiguredDisplayAllUsers() {
			return true;
		}

		@Override
		protected boolean getConfiguredAutoLoad() {
			return false;
		}
	}
}
