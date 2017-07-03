package org.zeroclick.configuration.client.user;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.common.security.ScoutServiceCredentialVerifier;
import org.zeroclick.configuration.client.user.UserForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.user.UserForm.MainBox.ConfirmPasswordField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.EmailField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.LoginField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.OkButton;
import org.zeroclick.configuration.client.user.UserForm.MainBox.PasswordField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.RolesBox;
import org.zeroclick.configuration.client.user.UserForm.MainBox.SendUserInviteEmailField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.TimeZoneField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.role.CreateAssignToRolePermission;
import org.zeroclick.configuration.shared.role.RoleLookupCall;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.common.ZoneIdLookupCall;
import org.zeroclick.meeting.shared.security.AccessControlService;

@FormData(value = UserFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class UserForm extends AbstractForm {

	private static final Logger LOG = LoggerFactory.getLogger(UserForm.class);

	private Boolean forCreation;

	private String hashedPassword;

	/**
	 * Some more advanced field are ignored/replaced
	 */
	private Boolean autofilled;

	@FormData
	public String getHashedPassword() {
		return this.hashedPassword;
	}

	@FormData
	public void setHashedPassword(final String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	@FormData
	public Boolean isAutofilled() {
		return this.autofilled;
	}

	@FormData
	public Boolean getAutofilled() {
		return this.isAutofilled();
	}

	@FormData
	public void setAutofilled(final Boolean autofilled) {
		this.autofilled = autofilled;
	}

	public UserForm() {
		super();
		this.forCreation = Boolean.FALSE;
	}

	public UserForm(final Boolean forCreation) {
		super();
		this.forCreation = forCreation;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.user");
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return Boolean.FALSE;
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

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public EmailField getEmailField() {
		return this.getFieldByClass(EmailField.class);
	}

	public RolesBox getRolesBox() {
		return this.getFieldByClass(RolesBox.class);
	}

	public PasswordField getPasswordField() {
		return this.getFieldByClass(PasswordField.class);
	}

	public ConfirmPasswordField getConfirmPasswordField() {
		return this.getFieldByClass(ConfirmPasswordField.class);
	}

	public SendUserInviteEmailField getSendUserInviteEmailField() {
		return this.getFieldByClass(SendUserInviteEmailField.class);
	}

	public TimeZoneField getTimeZoneField() {
		return this.getFieldByClass(TimeZoneField.class);
	}

	public LoginField getLoginField() {
		return this.getFieldByClass(LoginField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Override
	public void initForm() {
		super.initForm();
		this.getSendUserInviteEmailField().setVisible(!this.isMyself());
	}

	private void initFormAfterLoad() {
		this.getPasswordField().setMandatory(this.isPasswordMandatory());
		this.getConfirmPasswordField().setMandatory(this.isPasswordMandatory());
	}

	private Boolean isMyself() {
		final Long currentUserId = ((AccessControlService) BEANS.get(IAccessControlService.class))
				.getZeroClickUserIdOfCurrentSubject();
		return currentUserId.equals(UserForm.this.getUserIdField().getValue());
	}

	private Boolean isPasswordMandatory() {
		return null == UserForm.this.getHashedPassword();
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class UserIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.userId");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.TRUE;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}
		}

		@Order(1500)
		public class LoginField extends AbstractStringField {
			private static final String LOGIN_PATTERN = "^[_A-Za-z0-9-_-].*$";
			private String initialValue = null;

			// private Boolean modified = Boolean.FALSE;
			//
			// protected Boolean isModified() {
			// return this.modified;
			// }
			//
			// protected void setModified(final Boolean modified) {
			// this.modified = modified;
			// }

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Login");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 64;
			}

			// @Override
			// protected void execInitField() {
			// this.addPropertyChangeListener(new PropertyChangeListener() {
			//
			// @Override
			// public void propertyChange(final PropertyChangeEvent evt) {
			// if (null != evt.getNewValue() && !"".equals(evt.getNewValue())
			// && !evt.getNewValue().equals(evt.getOldValue())) {
			// LoginField.this.setModified(Boolean.TRUE);
			// }
			// }
			// });
			// }

			@Override
			protected String execValidateValue(final String rawValue) {
				final IUserService userService = BEANS.get(IUserService.class);
				Boolean modified = Boolean.FALSE;

				if (null == this.initialValue) {
					this.initialValue = rawValue;
				}

				if (null != rawValue && !rawValue.equals(this.initialValue)) {
					modified = Boolean.TRUE;
				}

				if (null != rawValue && modified) {
					if (rawValue.length() < 4) {
						throw new VetoException(TEXTS.get("zc.login.tooShort"));
					}
					if (!Pattern.matches(LOGIN_PATTERN, rawValue)) {
						throw new VetoException(TEXTS.get("zc.login.invalid"));
					}

					if (userService.isLoginAlreadyUsed(rawValue)) {
						throw new VetoException(TEXTS.get("zc.login.invalid"));
					}
				}
				return rawValue;
			}
		}

		@Order(2000)
		public class EmailField extends AbstractStringField {
			private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.common.email");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (rawValue != null && !Pattern.matches(EMAIL_PATTERN, rawValue)) {
					throw new VetoException(TEXTS.get("zc.common.badEmailAddress"));
				}
				return rawValue;
			}
		}

		@Order(2500)
		public class PasswordField extends AbstractStringField {
			private static final String NUMBER_PATTERN = "^.*[0-9]+.*$";
			private static final String LETTER_PATTERN = "^.*[A-Za-z]+.*";

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Password");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredInputMasked() {
				return Boolean.TRUE;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (null != rawValue) {
					if (rawValue.length() < 6) {
						throw new VetoException(TEXTS.get("zc.user.passwordTooShort"));
					}
					if (!Pattern.matches(NUMBER_PATTERN, rawValue)) {
						throw new VetoException(TEXTS.get("zc.user.passwordMustContainNumber"));
					}
					if (!Pattern.matches(LETTER_PATTERN, rawValue)) {
						throw new VetoException(TEXTS.get("zc.user.passwordMustContainLetter"));
					}
				}
				UserForm.this.getConfirmPasswordField().setMandatory(Boolean.TRUE);
				return rawValue;
			}
		}

		@Order(2600)
		public class ConfirmPasswordField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.confirmPassword");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredInputMasked() {
				return Boolean.TRUE;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				final String password = UserForm.this.getPasswordField().getValue();
				if (rawValue != null && !rawValue.equals(password)) {
					throw new VetoException(TEXTS.get("zc.user.passwordsDontMatch"));
				}
				return rawValue;
			}
		}

		@Order(2800)
		public class TimeZoneField extends AbstractSmartField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.timezone");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return ZoneIdLookupCall.class;
			}
		}

		@Order(3000)
		public class RolesBox extends AbstractListBox<Long> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.roles");
			}

			@Override
			protected int getConfiguredGridH() {
				return 4;
			}

			@Override
			protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
				return RoleLookupCall.class;
			}

			@Override
			protected void execInitField() {
				super.execInitField();
				this.setVisiblePermission(new CreateAssignToRolePermission());
			}
		}

		@Order(4000)
		public class SendUserInviteEmailField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.sendUserInviteEmail");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
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
			final IUserService service = BEANS.get(IUserService.class);
			UserFormData formData = new UserFormData();
			UserForm.this.exportFormData(formData);
			formData = service.load(formData);
			UserForm.this.importFormData(formData);

			UserForm.this.initFormAfterLoad();

			UserForm.this.setEnabledPermission(new UpdateUserPermission(formData.getUserId().getValue()));
		}

		@Override
		protected void execStore() {
			final IUserService service = BEANS.get(IUserService.class);
			final UserFormData formData = new UserFormData();
			UserForm.this.exportFormData(formData);
			final String modifiedPassword = formData.getPassword().getValue();
			if (null != modifiedPassword) {
				formData.setHashedPassword(UserForm.this.hashPassword(modifiedPassword));
			}
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);
			UserForm.this.getUserIdField().setEnabled(Boolean.TRUE);
			UserFormData formData = new UserFormData();
			UserForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			UserForm.this.importFormData(formData);

			UserForm.this.initFormAfterLoad();

			UserForm.this.setEnabledPermission(new CreateUserPermission());
		}

		@Override
		protected void execStore() {
			UserForm.this.save();
		}
	}

	private void save() {
		final IUserService service = BEANS.get(IUserService.class);
		final UserFormData formData = new UserFormData();
		UserForm.this.exportFormData(formData);
		formData.setHashedPassword(UserForm.this.hashPassword(formData.getPassword().getValue()));

		if (null == formData.getAutofilled()) {
			formData.setAutofilled(Boolean.FALSE);
		}
		service.create(formData);

		if (UserForm.this.getSendUserInviteEmailField().getValue()) {
			UserForm.this.sendUserInviteEmail();
		}
	}

	protected Boolean isForCreation() {
		return this.forCreation;
	}

	protected void setForCreation(final Boolean forCreation) {
		this.forCreation = forCreation;
	}

	public String hashPassword(final String plainPassword) {
		// TODO Djer13 avoid DIRECT dependence with
		// ScoutServiceCredentialVerifier. Use formBase.getCredentialVeirifer()
		// ?
		final ScoutServiceCredentialVerifier service = BEANS.get(ScoutServiceCredentialVerifier.class);
		return service.generatePassword(plainPassword);
	}

	private void sendUserInviteEmail() {
		final IUserService userService = BEANS.get(IUserService.class);

		this.sendUserInviteEmail(this, userService.getCurrentUserDetails().getEmail().getValue());
	}

	private void sendUserInviteEmail(final UserForm newUser, final String emailSender) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);

		String subject;
		String messageBody;

		if (this.isAutofilled()) {
			// invite + meeting
			subject = TEXTS.get("zc.meeting.email.invit.subject", emailSender);
			messageBody = TEXTS.get("zc.meeting.email.invit.html", emailSender, new ApplicationUrlProperty().getValue(),
					newUser.getEmailField().getValue(), newUser.getPasswordField().getValue());
		} else {
			// Simple invite without meeting
			subject = TEXTS.get("zc.user.email.invit.withoutMeeting.subject", emailSender);
			messageBody = TEXTS.get("zc.user.email.invit.withoutMeeting.html", emailSender,
					new ApplicationUrlProperty().getValue(), newUser.getEmailField().getValue(),
					newUser.getPasswordField().getValue());
		}
		try {
			mailSender.sendEmail(newUser.getEmailField().getValue(), subject, messageBody);
		} catch (final MailException e) {
			throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
		}
	}

	public UserForm autoFillInviteUser(final String email, final String emailSender) {
		final IUserService userService = BEANS.get(IUserService.class);

		LOG.info("Creating and invite a new User with email : " + email + " by : " + emailSender);

		if (userService.isEmailAlreadyUsed(email)) {
			new VetoException(TEXTS.get("zc.user.emailAlreadyUsed"));
		}

		this.createUser(email, Boolean.TRUE);
		this.save();
		this.sendUserInviteEmail(this, emailSender);

		return this;
	}

	private UserForm createUser(final String email, final Boolean isAutoFileld) {
		final Long defaultRole = 2l;

		// final String userId = email.substring(0, email.indexOf('@'));
		this.getEmailField().setValue(email);
		// this.getUserIdField().setValue(userId);

		final byte[] randomBytes = SecurityUtility.createRandomBytes(20);
		String plainRandomPassword = Base64Utility.encode(randomBytes);
		plainRandomPassword.replaceAll("[iI0O/=\\\\]", java.util.regex.Matcher.quoteReplacement(""));
		plainRandomPassword = plainRandomPassword.substring(0, 8);
		this.getPasswordField().setValue(plainRandomPassword);
		this.getConfirmPasswordField().setValue(plainRandomPassword);

		this.getRolesBox().checkKey(defaultRole);
		this.getRolesBox().touch();

		this.setAutofilled(isAutoFileld);

		super.doSave();

		return this;
	}

}
