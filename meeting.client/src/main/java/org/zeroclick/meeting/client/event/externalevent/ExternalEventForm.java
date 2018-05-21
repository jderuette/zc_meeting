package org.zeroclick.meeting.client.event.externalevent;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.ApiCredentialIdField;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.ExternalCalendarIdField;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.ExternalEventIdField;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.ExternalIdField;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventForm.MainBox.OkButton;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;

@FormData(value = ExternalEventFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ExternalEventForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.externalEvent");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
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

	public ExternalIdField getExternalIdField() {
		return this.getFieldByClass(ExternalIdField.class);
	}

	public ExternalEventIdField getExternalEventIdField() {
		return this.getFieldByClass(ExternalEventIdField.class);
	}

	public ExternalCalendarIdField getExternalCalendarIdField() {
		return this.getFieldByClass(ExternalCalendarIdField.class);
	}

	public ApiCredentialIdField getApiCredentialIdField() {
		return this.getFieldByClass(ApiCredentialIdField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class ExternalIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalId");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return -999999999999L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(2000)
		public class ExternalEventIdField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalEventId");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(3000)
		public class ExternalCalendarIdField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalCalendarId");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(4000)
		public class ApiCredentialIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.externalEvent.apiCredentialId");
			}

			@Override
			protected Long getConfiguredMinValue() {
				return -999999999999L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IExternalEventService service = BEANS.get(IExternalEventService.class);
			ExternalEventFormData formData = new ExternalEventFormData();
			ExternalEventForm.this.exportFormData(formData);
			formData = service.load(formData);
			ExternalEventForm.this.importFormData(formData);
		}

		@Override
		protected void execStore() {
			final IExternalEventService service = BEANS.get(IExternalEventService.class);
			final ExternalEventFormData formData = new ExternalEventFormData();
			ExternalEventForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IExternalEventService service = BEANS.get(IExternalEventService.class);
			ExternalEventFormData formData = new ExternalEventFormData();
			ExternalEventForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			ExternalEventForm.this.importFormData(formData);
		}

		@Override
		protected void execStore() {
			final IExternalEventService service = BEANS.get(IExternalEventService.class);
			final ExternalEventFormData formData = new ExternalEventFormData();
			ExternalEventForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
