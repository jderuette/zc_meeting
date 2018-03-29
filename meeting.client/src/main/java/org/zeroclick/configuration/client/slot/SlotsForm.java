package org.zeroclick.configuration.client.slot;

import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.slot.SlotsForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.slot.SlotsForm.MainBox.OkButton;
import org.zeroclick.configuration.client.slot.SlotsForm.MainBox.SlotsDescriptionField;
import org.zeroclick.configuration.client.slot.SlotsForm.MainBox.SlotsTableField;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotsFormData;
import org.zeroclick.configuration.shared.slot.SlotsFormData.SlotsTable.SlotsTableRowData;

@FormData(value = SlotsFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class SlotsForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot.configuration");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public SlotsDescriptionField getSlotsDescriptionField() {
		return this.getFieldByClass(SlotsDescriptionField.class);
	}

	public SlotsTableField getSlotsTableField() {
		return this.getFieldByClass(SlotsTableField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		@FormData(sdkCommand = FormData.SdkCommand.IGNORE)
		public class SlotsDescriptionField extends AbstractHtmlField {
			@Override
			protected String getConfiguredLabel() {
				return null;
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected int getConfiguredGridH() {
				return 3;
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected boolean getConfiguredFocusable() {
				return false;
			}

			@Override
			protected double getConfiguredGridWeightY() {
				return 0.3;
			}
		}

		@Order(2000)
		public class SlotsTableField extends AbstractTableField<SlotsTableField.Table> {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.slot.configuration");
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected int getConfiguredGridH() {
				return 6;
			}

			@Override
			protected double getConfiguredGridWeightY() {
				return 0.9;
			}

			public class Table extends AbstractSlotTable {

			}

		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {

		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}

		@Order(102000)
		public class CloseButton extends AbstractCloseButton {
			@Override
			protected boolean getConfiguredVisible() {
				return false;
			}
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {

			SlotsForm.this.getSlotsDescriptionField().setValue(TEXTS.get("zc.meeting.slot.configuration.description"));
		}

		@Override
		protected void execStore() {

			final SlotsFormData formData = new SlotsFormData();
			SlotsForm.this.exportFormData(formData);

			final List<ITableRow> modifiedRows = SlotsForm.this.getSlotsTableField().getTable().getUpdatedRows();
			if (modifiedRows.size() > 0) {
				final ISlotService slotService = BEANS.get(ISlotService.class);
				// remove unmodified rows, to avoid useless work on server side
				final SlotsTableRowData[] exportedRows = formData.getSlotsTable().getRows();
				for (final SlotsTableRowData row : exportedRows) {
					if (!this.isModified(row, modifiedRows)) {
						formData.getSlotsTable().removeRow(row);
					}
				}
				slotService.store(formData);
			}
		}

		private Boolean isModified(final SlotsTableRowData row, final List<ITableRow> modifiedRows) {
			final Long dayDurationId = row.getDayDurationId();
			return this.contains(dayDurationId, modifiedRows);
		}

		private Boolean contains(final Long dayDurationId, final List<ITableRow> modifiedRows) {
			for (final ITableRow row : modifiedRows) {
				if (SlotsForm.this.getSlotsTableField().getTable().getDayDurationIdColumn()
						.getValue(row) == dayDurationId) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
	}
}
