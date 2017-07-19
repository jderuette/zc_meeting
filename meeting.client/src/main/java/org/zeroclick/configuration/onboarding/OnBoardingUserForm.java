package org.zeroclick.configuration.onboarding;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.LoginField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.OkButton;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.TimeZoneField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;

@FormData(value = OnBoardingUserFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class OnBoardingUserForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.common.onBoarding.user");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public LoginField getLoginField() {
		return this.getFieldByClass(LoginField.class);
	}

	public TimeZoneField getTimeZoneField() {
		return this.getFieldByClass(TimeZoneField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
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
		public class UserIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.userId");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}
		}

		@Order(2000)
		public class LoginField extends org.zeroclick.ui.form.fields.loginfield.LoginField {
		}

		@Order(3000)
		public class TimeZoneField extends org.zeroclick.ui.form.fields.timezonefield.TimeZoneField {
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.common.updateAction");
			}
		}

	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);
			OnBoardingUserFormData formData = new OnBoardingUserFormData();
			OnBoardingUserForm.this.exportFormData(formData);
			formData = service.load(formData);
			OnBoardingUserForm.this.importFormData(formData);

			OnBoardingUserForm.this.setEnabledPermission(new UpdateUserPermission(formData.getUserId().getValue()));
		}

		@Override
		protected void execStore() {
			final IUserService service = BEANS.get(IUserService.class);
			final OnBoardingUserFormData formData = new OnBoardingUserFormData();
			OnBoardingUserForm.this.exportFormData(formData);

			service.store(formData);
		}
	}
}
