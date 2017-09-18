package org.zeroclick.configuration.client.slot;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.client.slot.SlotTablePage.Table;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.security.AccessControlService;

@Data(SlotTablePageData.class)
public class SlotTablePage extends AbstractSlotTablePage<Table> {

	private static final Logger LOG = LoggerFactory.getLogger(SlotTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot.config");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ISlotService.class).getDayDurationTableData(filter));
		this.getTable().sort();
	}

	@Override
	protected void execInitPage() {
		super.execInitPage();
		// this.setDetailForm(new DayDurationForm());
		// this.setDetailFormVisible(Boolean.TRUE);
		this.getTable().sort();
	}

	@Order(1000)
	public class EditMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.dayDuration.edit");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection);
		}

		@Override
		protected String getConfiguredIconId() {
			return Icons.Pencil;
		}

		@Override
		protected void execAction() {

			final List<ITableRow> selectedRows = SlotTablePage.this.getTable().getSelectedRows();
			if (null != selectedRows && !selectedRows.isEmpty()) {
				for (final ITableRow row : selectedRows) {
					if (null != row) {
						SlotTablePage.this.loadDayDurationForm(row);
					}
				}
			}

		}
	}

	protected void loadDayDurationForm(final ITableRow row) {
		final Integer slotCode = this.getTable().getSlotColumn().getValue(row.getRowIndex());
		final String dayDurationName = this.getTable().getNameColumn().buildDisplayValue(row);

		final List<IForm> forms = IDesktop.CURRENT.get().getForms(IDesktop.CURRENT.get().getOutline());

		final StringBuilder slotNameBuilder = new StringBuilder();
		slotNameBuilder.append(TEXTS.get("zc.meeting.slot." + slotCode)).append(" - ").append(dayDurationName);

		final String slotName = slotNameBuilder.toString();

		for (final IForm form : forms) {
			if (null != slotName && slotName.equals(form.getSubTitle())) {
				form.activate();
				// early break;
				return;
			}
		}

		final Long dayDurationId = this.getTable().getDayDurationIdColumn().getValue(row.getRowIndex());
		final Long slotId = this.getTable().getSlotIdColumn().getValue(row.getRowIndex());

		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final DayDurationForm dayDurationForm = new DayDurationForm();

		dayDurationForm.setUserId(acs.getZeroClickUserIdOfCurrentSubject());
		dayDurationForm.setSubTitle(slotName);
		dayDurationForm.getMainBox().setGridColumnCountHint(1);
		dayDurationForm.setDayDurationId(dayDurationId);
		dayDurationForm.setSlotId(slotId);
		dayDurationForm.setDisplayParent(IDesktop.CURRENT.get().getOutline());
		dayDurationForm.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
		dayDurationForm.setDisplayViewId(IForm.VIEW_ID_E);

		dayDurationForm.startModify();
	}

	public class Table extends AbstractSlotTablePage<Table>.Table {

	}
}
