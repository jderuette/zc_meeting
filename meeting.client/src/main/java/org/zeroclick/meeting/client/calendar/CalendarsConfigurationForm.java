package org.zeroclick.meeting.client.calendar;

import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.common.ui.form.IPageForm;
import org.zeroclick.meeting.client.calendar.CalendarsConfigurationForm.MainBox.CalendarConfigTableField;
import org.zeroclick.meeting.client.calendar.CalendarsConfigurationForm.MainBox.CalendarDescriptionField;
import org.zeroclick.meeting.client.calendar.CalendarsConfigurationForm.MainBox.CloseButton;
import org.zeroclick.meeting.client.calendar.CalendarsConfigurationForm.MainBox.SaveButton;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

@FormData(value = CalendarsConfigurationFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class CalendarsConfigurationForm extends AbstractForm implements IPageForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.configuration");
	}

	@Override
	protected boolean getConfiguredMaximizeEnabled() {
		return true;
	}

	@Override
	public void startPageForm() {
		this.startInternal(new PageFormHandler());
	}

	@Override
	public AbstractCloseButton getCloseButton() {
		return this.getFieldByClass(CloseButton.class);
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public CalendarConfigTableField getCalendarConfigTableField() {
		return this.getFieldByClass(CalendarConfigTableField.class);
	}

	public CalendarDescriptionField getCalendarDescriptionField() {
		return this.getFieldByClass(CalendarDescriptionField.class);
	}

	public SaveButton getSaveButton() {
		return this.getFieldByClass(SaveButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		@FormData(sdkCommand = FormData.SdkCommand.IGNORE)
		public class CalendarDescriptionField extends AbstractHtmlField {
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
				return 6;
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
		public class CalendarConfigTableField extends AbstractTableField<AbstractCalendarConfigurationTable> {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.configuration");
			}

			@Override
			protected int getConfiguredLabelPosition() {
				return LABEL_POSITION_TOP;
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
				return 5;
			}

			@Override
			protected double getConfiguredGridWeightY() {
				return 0.9;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return true;
			}

			public class Table extends AbstractCalendarConfigurationTable {

			}
		}

		@Order(50)
		public class CloseButton extends AbstractCloseButton {
		}

		@Order(100000)
		public class SaveButton extends AbstractOkButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			CalendarsConfigurationForm.this.getCalendarDescriptionField()
					.setValue(TEXTS.get("zc.meeting.calendar.configuration.description"));
		}

		@Override
		protected boolean execValidate() {
			return CalendarsConfigurationForm.this.getCalendarConfigTableField().getTable().canStore();
		}

		@Override
		protected void execStore() {
			final CalendarsConfigurationFormData formData = new CalendarsConfigurationFormData();
			CalendarsConfigurationForm.this.exportFormData(formData);

			final List<ITableRow> modifiedRows = CalendarsConfigurationForm.this.getCalendarConfigTableField()
					.getTable().getUpdatedRows();
			if (modifiedRows.size() > 0) {
				final ICalendarConfigurationService calendarConfigurationService = BEANS
						.get(ICalendarConfigurationService.class);
				// remove unmodified rows, to avoid useless work on server side
				final CalendarConfigTableRowData[] exportedRows = formData.getCalendarConfigTable().getRows();
				for (final CalendarConfigTableRowData row : exportedRows) {
					if (!this.isModified(row, modifiedRows)) {
						formData.getCalendarConfigTable().removeRow(row);
					}
				}

				calendarConfigurationService.store(formData);
			}
		}

		private Boolean isModified(final CalendarConfigTableRowData row, final List<ITableRow> modifiedRows) {
			final Long calendarConfigurationId = row.getCalendarConfigurationId();
			return this.contains(calendarConfigurationId, modifiedRows);
		}

		private Boolean contains(final Long calendarConfigurationId, final List<ITableRow> modifiedRows) {
			for (final ITableRow row : modifiedRows) {
				if (CalendarsConfigurationForm.this.getCalendarConfigTableField().getTable()
						.getCalendarConfigurationIdColumn().getValue(row) == calendarConfigurationId) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
	}

	public class PageFormHandler extends AbstractFormHandler {
		@Override
		protected void execLoad() {
			CalendarsConfigurationForm.this.getCalendarDescriptionField()
					.setValue(TEXTS.get("zc.meeting.calendar.configuration.description"));
		}
	}
}
