package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.api.AbstractApiTable;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.ApisTableField;
import org.zeroclick.meeting.client.calendar.AddCalendarForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.calendar.AddCalendarForm.MainBox.OkButton;

public class AddCalendarForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.addCalendar");
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	public AddCalendarField getAddCalendarField() {
		return this.getFieldByClass(AddCalendarField.class);
	}

	public ApisTableField getApisTableField() {
		return this.getFieldByClass(ApisTableField.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridW() {
			return 1;
		}

		@Override
		protected int getConfiguredGridColumnCount() {
			return 1;
		}

		@Order(1000)
		public class AddCalendarField extends org.zeroclick.meeting.client.calendar.AddCalendarField {
		}

		@Order(2000)
		public class ApisTableField extends AbstractTableField<AbstractApiTable> {

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected int getConfiguredGridH() {
				return 3;
			}

			public class Table extends AbstractApiTable {

			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
		}

		@Override
		protected void execStore() {
		}
	}
}
