package org.zeroclick.configuration.client.slot;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.configuration.shared.slot.AbstractSlotTablePageData;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;

@Data(AbstractSlotTablePageData.class)
public abstract class AbstractSlotTablePage<T extends AbstractSlotTablePage<T>.Table> extends AbstractPageWithTable<T> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		// AbstractSlotTable is autoLoaded
		// this.importPageData(BEANS.get(ISlotService.class).getDayDurationTableData(filter));
		// this.getTable().sort();
	}

	@Override
	protected void execInitPage() {
		super.execInitPage();
		// this.setDetailForm(new DayDurationForm());
		// this.setDetailFormVisible(Boolean.TRUE);
		// this.getTable().sort();
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected IPage<?> execCreateChildPage(final ITableRow row) {
		// TODO Auto-generated method stub
		return super.execCreateChildPage(row);
	}

	protected Boolean isSlotAdmin() {
		final int currentUserSlotLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		return currentUserSlotLevel == ReadSlotPermission.LEVEL_ALL;
	}

	public class Table extends AbstractSlotTable {

	}
}
