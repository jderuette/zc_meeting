package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.AddEventToCalendarField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.CalendarConfigurationIdField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ExternalIdField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.MainField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.NameField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.OAuthCredentialIdField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.OkButton;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ProcessField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ProcessFreeEventField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ProcessFullDayEventField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ProcessNotRegistredOnEventField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ProviderField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.ReadOnlyField;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationForm.MainBox.UserIdField;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission;

@FormData(value = CalendarConfigurationFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class CalendarConfigurationForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.calendar.configuration");
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

	public CalendarConfigurationIdField getCalendarConfigurationIdField() {
		return this.getFieldByClass(CalendarConfigurationIdField.class);
	}

	public ExternalIdField getExternalIdField() {
		return this.getFieldByClass(ExternalIdField.class);
	}

	public ProcessFullDayEventField getProcessFullDayEventField() {
		return this.getFieldByClass(ProcessFullDayEventField.class);
	}

	public ProcessFreeEventField getProcessFreeEventField() {
		return this.getFieldByClass(ProcessFreeEventField.class);
	}

	public ProcessNotRegistredOnEventField getProcessNotRegistredOnEventField() {
		return this.getFieldByClass(ProcessNotRegistredOnEventField.class);
	}

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public MainField getMainField() {
		return this.getFieldByClass(MainField.class);
	}

	public ProcessField getProcessField() {
		return this.getFieldByClass(ProcessField.class);
	}

	public AddEventToCalendarField getAddEventToCalendarField() {
		return this.getFieldByClass(AddEventToCalendarField.class);
	}

	public ReadOnlyField getReadOnlyField() {
		return this.getFieldByClass(ReadOnlyField.class);
	}

	public ProviderField getProviderField() {
		return this.getFieldByClass(ProviderField.class);
	}

	public NameField getNameField() {
		return this.getFieldByClass(NameField.class);
	}

	public OAuthCredentialIdField getOAuthCredentialIdField() {
		return this.getFieldByClass(OAuthCredentialIdField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Override
	protected void execInitForm() {
		this.getAddEventToCalendarField().setValue(!this.getReadOnlyField().getValue());
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class CalendarConfigurationIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.id");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(2000)
		public class ExternalIdField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.externalId");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 150;
			}
		}

		@Order(3000)
		public class NameField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.name");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(4000)
		public class ReadOnlyField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.readOnly");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

		}

		@Order(5000)
		public class MainField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.main");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}
		}

		@Order(6000)
		public class ProcessField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.processThisCalendar");
			}
		}

		@Order(7000)
		public class AddEventToCalendarField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.addEventToThisCalendar");
			}
		}

		@Order(8000)
		public class ProcessFullDayEventField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.processFullDayEvent");
			}
		}

		@Order(9000)
		public class ProcessFreeEventField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.processFreeEvent");
			}
		}

		@Order(10000)
		public class ProcessNotRegistredOnEventField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.processNotRegisteredOnEvent");
			}
		}

		@Order(11000)
		public class OAuthCredentialIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.OAuthCredentialId");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(12000)
		public class ProviderField extends AbstractSmartField<Long> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.api.provider");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected Class<? extends ICodeType<Long, Long>> getConfiguredCodeType() {
				return ProviderCodeType.class;
			}

			@Override
			public String toString() {
				final ToStringBuilder sbuilder = new ToStringBuilder(this);
				sbuilder.attr(this.getValue());
				return sbuilder.toString();
			}
		}

		@Order(13000)
		public class UserIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.calendar.userId");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
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
			final ICalendarConfigurationService service = BEANS.get(ICalendarConfigurationService.class);
			CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
			CalendarConfigurationForm.this.exportFormData(formData);
			formData = service.load(formData);
			CalendarConfigurationForm.this.importFormData(formData);

			CalendarConfigurationForm.this.setEnabledPermission(
					new UpdateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()));
		}

		@Override
		protected void execStore() {
			final ICalendarConfigurationService service = BEANS.get(ICalendarConfigurationService.class);
			final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
			CalendarConfigurationForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final ICalendarConfigurationService service = BEANS.get(ICalendarConfigurationService.class);
			CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
			CalendarConfigurationForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			CalendarConfigurationForm.this.importFormData(formData);

			CalendarConfigurationForm.this.setEnabledPermission(
					new CreateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()));
		}

		@Override
		protected void execStore() {
			final ICalendarConfigurationService service = BEANS.get(ICalendarConfigurationService.class);
			final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
			CalendarConfigurationForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
