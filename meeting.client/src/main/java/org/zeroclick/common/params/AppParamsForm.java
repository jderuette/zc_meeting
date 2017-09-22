package org.zeroclick.common.params;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.common.params.AppParamsForm.MainBox.CancelButton;
import org.zeroclick.common.params.AppParamsForm.MainBox.CategoryField;
import org.zeroclick.common.params.AppParamsForm.MainBox.KeyField;
import org.zeroclick.common.params.AppParamsForm.MainBox.OkButton;
import org.zeroclick.common.params.AppParamsForm.MainBox.ParamIdField;
import org.zeroclick.common.params.AppParamsForm.MainBox.ValueField;
import org.zeroclick.configuration.shared.params.AppParamsCategoryLookupCall;
import org.zeroclick.configuration.shared.params.CreateAppParamsPermission;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.params.UpdateAppParamsPermission;

@FormData(value = AppParamsFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class AppParamsForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.params");
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

	public ParamIdField getParamIdField() {
		return this.getFieldByClass(ParamIdField.class);
	}

	public KeyField getKeyField() {
		return this.getFieldByClass(KeyField.class);
	}

	public CategoryField getCategoryField() {
		return this.getFieldByClass(CategoryField.class);
	}

	public ValueField getValueField() {
		return this.getFieldByClass(ValueField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class ParamIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.params.id");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
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
		public class KeyField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.params.key");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(3000)
		public class ValueField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.params.value");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}
		}

		@Order(4000)
		public class CategoryField extends AbstractProposalField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.params.category");
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return AppParamsCategoryLookupCall.class;
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
			final IAppParamsService service = BEANS.get(IAppParamsService.class);
			AppParamsFormData formData = new AppParamsFormData();
			AppParamsForm.this.exportFormData(formData);
			formData = service.load(formData);
			AppParamsForm.this.importFormData(formData);

			AppParamsForm.this.setEnabledPermission(new UpdateAppParamsPermission());
		}

		@Override
		protected void execStore() {
			final IAppParamsService service = BEANS.get(IAppParamsService.class);
			final AppParamsFormData formData = new AppParamsFormData();
			AppParamsForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IAppParamsService service = BEANS.get(IAppParamsService.class);
			AppParamsFormData formData = new AppParamsFormData();
			AppParamsForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			AppParamsForm.this.importFormData(formData);

			AppParamsForm.this.setEnabledPermission(new CreateAppParamsPermission());
		}

		@Override
		protected void execStore() {
			final IAppParamsService service = BEANS.get(IAppParamsService.class);
			final AppParamsFormData formData = new AppParamsFormData();
			AppParamsForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
