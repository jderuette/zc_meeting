package org.zeroclick.configuration.client.api;

import java.io.IOException;

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
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.AccessTokenField;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.AccountEmailField;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.ExpirationTimeMillisecondsField;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.OkButton;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.ProviderField;
import org.zeroclick.configuration.client.api.ApiForm.MainBox.RefreshTokenField;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CreateApiPermission;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.ui.form.fields.emailfield.EmailField;

@FormData(value = ApiFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ApiForm extends AbstractForm {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractForm.class);

	private Long apiCredentialId;
	private Long userId;
	private String repositoryId;
	private byte[] providerData;

	private Boolean deleteAction;

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
		return null == this.providerData ? null : this.providerData.clone();
	}

	@FormData
	public void setProviderData(final byte[] providerData) {
		this.providerData = null == providerData ? null : providerData.clone();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.api.oAuthCredential");
	}

	public void startModify() {
		this.deleteAction = Boolean.FALSE;
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.deleteAction = Boolean.FALSE;
		this.startInternal(new NewHandler());
	}

	public void startDelete() {
		this.deleteAction = Boolean.TRUE;
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

	public AccountEmailField getAccountEmailField() {
		return this.getFieldByClass(AccountEmailField.class);
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
				return TEXTS.get("zc.api.accessToken");
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
				return TEXTS.get("zc.api.expirationTimeMilliseconds");
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
				return TEXTS.get("zc.api.refreshToken");
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
		public class ProviderField extends AbstractSmartField<Long> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.api.provider");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
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

		@Order(5000)
		public class AccountEmailField extends EmailField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.api.accountEmail");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {

			@Override
			protected boolean execIsSaveNeeded() {
				// to force form save even if no modification done in Fields
				if (ApiForm.this.deleteAction) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
				}
			}
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
			formData.setApiCredentialId(ApiForm.this.getApiCredentialId());
			formData.setUserId(ApiForm.this.getUserId());
			formData = service.load(formData);
			ApiForm.this.importFormData(formData);

			final int userPermissionLevel = ACCESS.getLevel(new DeleteApiPermission((Long) null));

			ApiForm.this.setVisibleGranted(userPermissionLevel > DeleteApiPermission.LEVEL_OWN);
			ApiForm.this.setEnabledGranted(userPermissionLevel > DeleteApiPermission.LEVEL_OWN);

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
			formData.setApiCredentialId(ApiForm.this.getApiCredentialId());
			formData.setUserId(ApiForm.this.getUserId());
			formData = service.load(formData);
			ApiForm.this.importFormData(formData);
			final int userPermissionLevel = ACCESS.getLevel(new DeleteApiPermission((Long) null));

			ApiForm.this.setVisibleGranted(userPermissionLevel > DeleteApiPermission.LEVEL_OWN);
			ApiForm.this.setEnabledGranted(userPermissionLevel > DeleteApiPermission.LEVEL_OWN);
		}

		@Override
		protected void execStore() {
			final IApiService service = BEANS.get(IApiService.class);
			final ApiFormData formData = new ApiFormData();
			ApiForm.this.exportFormData(formData);
			formData.setApiCredentialId(ApiForm.this.getApiCredentialId());
			formData.setUserId(ApiForm.this.getUserId());

			// use GoolgleApiHelper to clean cache for this user
			try {
				BEANS.get(GoogleApiHelper.class).removeCredential(ApiForm.this.getApiCredentialId());
			} catch (final IOException e) {
				LOG.error("Error while trying to delete User (Google) Api credential", e);
			}

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
