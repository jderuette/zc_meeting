package org.zeroclick.configuration.client.slot;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.client.slot.SlotTablePage.Table;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;

@Data(SlotTablePageData.class)
public class SlotTablePage extends AbstractPageWithTable<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ISlotService.class).getSlotTableData(filter));
	}

	@Override
	protected void execInitPage() {
		super.execInitPage();
		this.setDetailForm(new SlotForm());
		this.setDetailFormVisible(Boolean.TRUE);
	}

	public class Table extends AbstractTable {

		public SlotIdColumn getSlotIdColumn() {
			return this.getColumnSet().getColumnByClass(SlotIdColumn.class);
		}

		public NameColumn getNameColumn() {
			return this.getColumnSet().getColumnByClass(NameColumn.class);
		}

		public UserIdColumn getUserIdColumn() {
			return this.getColumnSet().getColumnByClass(UserIdColumn.class);
		}

		public isDefaultColumn getisDefaultColumn() {
			return this.getColumnSet().getColumnByClass(isDefaultColumn.class);
		}

		@Order(1000)
		public class SlotIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.slot.id");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class UserIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.slot.id");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(3000)
		public class NameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.slot.name");
			}

			@Override
			protected void execDecorateCell(final Cell cell, final ITableRow row) {
				super.execDecorateCell(cell, row);
				cell.setText(TEXTS.get(cell.getText()));
			}

			@Override
			protected int getConfiguredWidth() {
				return 200;
			}
		}

		@Order(4000)
		public class isDefaultColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.slot.isDefault");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

	}
}
