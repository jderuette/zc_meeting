package org.zeroclick.meeting.client.calendar;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationTablePage.Table;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.security.AccessControlService;

@Data(CalendarConfigurationTablePageData.class)
public class CalendarConfigurationTablePage extends AbstractCalendarConfigurationTablePage<Table> {

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ICalendarConfigurationService.class).getCalendarConfigurationTableData(filter,
				Boolean.FALSE));
	}

	@Order(1000)
	public class AutoImportMyCalendarsMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.calendar.autoImport");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection,
					TreeMenuType.EmptySpace);
		}

		@Override
		protected void execAction() {
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

			if (!googleHelper.isCalendarConfigured()) {
				googleHelper.askToAddApi(BEANS.get(AccessControlService.class).getZeroClickUserIdOfCurrentSubject());
			}
			if (!googleHelper.isCalendarConfigured()) {
				// User really won't provide required data
				return; // early Break
			}
			final Map<String, AbstractCalendarConfigurationTableRowData> calendars = googleHelper.getCalendars();

			final ICalendarConfigurationService calendarConfigurationService = BEANS
					.get(ICalendarConfigurationService.class);
			calendarConfigurationService.autoConfigure(calendars);

			CalendarConfigurationTablePage.this.execLoadData(null);
		}
	}

	public class Table extends AbstractCalendarConfigurationTablePage<Table>.Table {
	}
}
