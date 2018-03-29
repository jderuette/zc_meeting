package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData;

@Data(AbstractCalendarConfigurationTablePageData.class)
public abstract class AbstractCalendarConfigurationTablePage<T extends AbstractCalendarConfigurationTablePage<T>.Table>
		extends AbstractPageWithTable<T> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.configuration");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	public class Table extends AbstractCalendarConfigurationTable {

	}

}
