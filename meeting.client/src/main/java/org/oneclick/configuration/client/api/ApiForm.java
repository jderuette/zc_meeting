package org.oneclick.configuration.client.api;

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
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.oneclick.configuration.client.api.ApiForm.MainBox.AccessTokenField;
import org.oneclick.configuration.client.api.ApiForm.MainBox.CancelButton;
import org.oneclick.configuration.client.api.ApiForm.MainBox.ExpirationTimeMillisecondsField;
import org.oneclick.configuration.client.api.ApiForm.MainBox.OkButton;
import org.oneclick.configuration.client.api.ApiForm.MainBox.ProviderField;
import org.oneclick.configuration.client.api.ApiForm.MainBox.RefreshTokenField;
import org.oneclick.meeting.client.common.ProviderLookupCall;
import org.oneclick.meeting.shared.calendar.ApiFormData;
import org.oneclick.meeting.shared.calendar.CreateApiPermission;
import org.oneclick.meeting.shared.calendar.DeleteApiPermission;
import org.oneclick.meeting.shared.calendar.IApiService;
import org.oneclick.meeting.shared.calendar.UpdateApiPermission;

@FormData(value = ApiFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ApiForm extends AbstractForm {

	private Long apiCredentialId;
	private Long userId;
	private String repositoryId;
	private byte[] providerData;

	@FormData
	public Long getApiCredentialId() {
		return this.apiCredentialId;
	}

	@FormData
	public void setApiCredentialId(final long apiCredentialId) {
		this.apiCredentialId = apiCredentialId;
	}

	@FormData
	public Long getUserId() {
		return this.userId;
	}

	@FormData
	public void setUserId(final Long userId) {
		this.userId = userId;
	}

	@FormData
	public String getRepositoryId() {
		return this.repositoryId;
	}

	@FormData
	public void setRepositoryId(final String repositoryId) {
		this.repositoryId = repositoryId;
	}

	@FormData
	public byte[] getProviderData() {
		return this.providerData;
	}

	@FormData
	public void setProviderData(final byte[] providerData) {
		this.providerData = providerData;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("OAuthCredential");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public void startDelete() {
		this.startInternal(new DeleteHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public AccessTokenField getaccessTokenField() {
		return this.getFieldByClass(AccessTokenField.class);
	}

	public ExpirationTimeMillisecondsField getExpirationTimeMillisecondsField() {
		return this.getFieldByClass(ExpirationTimeMillisecondsField.class);
	}

	public RefreshTokenField getRefreshTokenField() {
		return this.getFieldByClass(RefreshTokenField.class);
	}

	public ProviderField getProviderField() {
		return this.getFieldByClass(ProviderField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class AccessTokenField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("AccessToken");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}

			@Override
			public String toString() {
				final ToStringBuilder sbuilder = new ToStringBuilder(this);
				sbuilder.attr(this.getValue());
				return sbuilder.toString();
			}
		}

		@Order(2000)
		public class ExpirationTimeMillisecondsField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("ExpirationTimeMilliseconds");
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 99999999999999L;
			}

			@Override
			public String toString() {
				final ToStringBuilder sbuilder = new ToStringBuilder(this);
				sbuilder.attr(this.getValue());
				return sbuilder.toString();
			}
		}

		@Order(3000)
		public class RefreshTokenField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("RefreshToken");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			public String toString() {
				final ToStringBuilder sbuilder = new ToStringBuilder(this);
				sbuilder.attr(this.getValue());
				return sbuilder.toString();
			}
		}

		@Order(4000)
		public class ProviderField extends AbstractSmartField<Integer> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Provider");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return ProviderLookupCall.class;
			}

			@Override
			public String toString() {
				final ToStringBuilder sbuilder = new ToStringBuilder(this);
				sbuilder.attr(this.getValue());
				return sbuilder.toString();
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
			final IApiService service = BEANS.get(IApiService.class);
			ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			formData = service.load(formData);
			ApiForm.this.importFormData(formData);

			ApiForm.this.setEnabledPermission(new UpdateApiPermission(ApiForm.this.getApiCredentialId()));
		}

		@Override
		protected void execStore() {
			final IApiService service = BEANS.get(IApiService.class);
			final ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class DeleteHandler extends AbstractFormHandler {
		@Override
		protected void execLoad() {
			final IApiService service = BEANS.get(IApiService.class);
			ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			formData = service.load(formData);
			ApiForm.this.importFormData(formData);

			ApiForm.this.setEnabledPermission(new DeleteApiPermission(ApiForm.this.getApiCredentialId()));
		}

		@Override
		protected void execStore() {
			final IApiService service = BEANS.get(IApiService.class);
			final ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			service.delete(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IApiService service = BEANS.get(IApiService.class);
			ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			ApiForm.this.importFormData(formData);

			ApiForm.this.setEnabledPermission(new CreateApiPermission());
		}

		@Override
		protected void execStore() {
			final IApiService service = BEANS.get(IApiService.class);
			final ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
