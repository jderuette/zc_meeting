package org.zeroclick.meeting.client.event.involevment;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.shared.user.UserLookupCall;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.EventIdField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.ExternalEventIdField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.InvitedByField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.ReasonField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.RoleField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.StateField;
import org.zeroclick.meeting.client.event.involevment.InvolvementForm.MainBox.UserIdField;
import org.zeroclick.meeting.shared.event.involevment.EventRoleCodeType;
import org.zeroclick.meeting.shared.event.involevment.InvolvmentStateCodeType;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;

@FormData(value = InvolvementFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class InvolvementForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.involevment");
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

	public EventIdField getEventIdField() {
		return this.getFieldByClass(EventIdField.class);
	}

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public StateField getStateField() {
		return this.getFieldByClass(StateField.class);
	}

	public RoleField getRoleField() {
		return this.getFieldByClass(RoleField.class);
	}

	public ReasonField getReasonField() {
		return this.getFieldByClass(ReasonField.class);
	}

	public ExternalEventIdField getExternalEventIdField() {
		return this.getFieldByClass(ExternalEventIdField.class);
	}

	public InvitedByField getInvitedByField() {
		return this.getFieldByClass(InvitedByField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class EventIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.eventId");
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
		public class UserIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.userId");
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

		@Order(3000)
		public class RoleField extends AbstractSmartField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.role");
			}

			@Override
			protected Class<? extends ICodeType<?, String>> getConfiguredCodeType() {
				return EventRoleCodeType.class;
			}

		}

		@Order(4000)
		public class StateField extends AbstractSmartField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.state");
			}

			@Override
			protected Class<? extends ICodeType<?, String>> getConfiguredCodeType() {
				return InvolvmentStateCodeType.class;
			}
		}

		@Order(5000)
		public class ReasonField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.rejectReason");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}
		}

		@Order(6000)
		public class ExternalEventIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.externalEventId");
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

		@Order(7000)
		public class InvitedByField extends AbstractSmartField<Long> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.involevment.invitedBy");
			}

			@Override
			protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
				return UserLookupCall.class;
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
			final IInvolvementService service = BEANS.get(IInvolvementService.class);
			InvolvementFormData formData = new InvolvementFormData();
			InvolvementForm.this.exportFormData(formData);
			formData = service.load(formData);
			InvolvementForm.this.importFormData(formData);
		}

		@Override
		protected void execStore() {
			final IInvolvementService service = BEANS.get(IInvolvementService.class);
			final InvolvementFormData formData = new InvolvementFormData();
			InvolvementForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IInvolvementService service = BEANS.get(IInvolvementService.class);
			InvolvementFormData formData = new InvolvementFormData();
			InvolvementForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			InvolvementForm.this.importFormData(formData);
		}

		@Override
		protected void execStore() {
			final IInvolvementService service = BEANS.get(IInvolvementService.class);
			final InvolvementFormData formData = new InvolvementFormData();
			InvolvementForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
