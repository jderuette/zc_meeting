package org.zeroclick.configuration.client.slot;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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

		@Order(1000)
		public class UpdateAllDefaultCodeMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.slot.updateAllSlotCode");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
						TableMenuType.EmptySpace);
			}

			@Override
			protected void execAction() {
				final ISlotService slotService = BEANS.get(ISlotService.class);
				slotService.addDefaultCodeToExistingSlot();
			}
		}

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
