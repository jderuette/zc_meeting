package org.zeroclick.configuration.client.slot;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.client.slot.SlotAdminTablePage.Table;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotAdminTablePageData;
import org.zeroclick.meeting.shared.Icons;

@Data(SlotAdminTablePageData.class)
public class SlotAdminTablePage extends AbstractSlotTablePage<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot.admin");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.ExclamationMark;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ISlotService.class).getDayDurationAdminTableData(filter));
	}

	public class Table extends AbstractSlotTablePage<Table>.Table {

		@Override
		protected void execInitTable() {
			super.execInitTable();
			this.getDayDurationIdColumn().setVisible(Boolean.TRUE);

			this.getUserIdColumn().setVisible(Boolean.TRUE);
			this.getUserIdColumn().setInitialAlwaysIncludeSortAtBegin(Boolean.FALSE);

			this.getSlotIdColumn().setVisible(Boolean.TRUE);
			this.getSlotIdColumn().setInitialAlwaysIncludeSortAtBegin(Boolean.FALSE);

			this.getOrderInSlotColumn().setVisible(Boolean.TRUE);
			this.getOrderInSlotColumn().setInitialAlwaysIncludeSortAtBegin(Boolean.FALSE);
		}

		@Override
		protected boolean getConfiguredHeaderEnabled() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredSortEnabled() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredTableStatusVisible() {
			return Boolean.TRUE;
		}
	}
}
