package org.zeroclick.configuration.client.user;

import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.common.security.ScoutServiceCredentialVerifier;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.client.user.UserForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.user.UserForm.MainBox.ConfirmPasswordField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.EmailField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.LanguageField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.LoginField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.OkButton;
import org.zeroclick.configuration.client.user.UserForm.MainBox.PasswordField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.RolesBox;
import org.zeroclick.configuration.client.user.UserForm.MainBox.SendUserInviteEmailField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.SubscriptionBox;
import org.zeroclick.configuration.client.user.UserForm.MainBox.TimeZoneField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.UserDetailBox;
import org.zeroclick.configuration.client.user.UserForm.MainBox.UserDetailBox.SubscriptionsListBox;
import org.zeroclick.configuration.client.user.UserForm.MainBox.UserDetailBox.SubscriptionsListBox.SubscriptionsListTableField;
import org.zeroclick.configuration.client.user.UserForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.role.CreateAssignToRolePermission;
import org.zeroclick.configuration.shared.role.RoleLookupCall;
import org.zeroclick.configuration.shared.role.SubscriptionLookupCall;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.security.AccessControlService;

@FormData(value = UserFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class UserForm extends AbstractForm {

	private static final Logger LOG = LoggerFactory.getLogger(UserForm.class);

	private Boolean forCreation;

	private String hashedPassword;
	private Boolean activeSubscriptionValid;

	/**
	 * Some more advanced field are ignored/replaced
	 */
	private Boolean autofilled;

	private Long invitedBy;

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

	@FormData
	public Long getInvitedBy() {
		return this.invitedBy;
	}

	@FormData
	public void setInvitedBy(final Long invitedBy) {
		this.invitedBy = invitedBy;
	}

	@FormData
	public Boolean getActiveSubscriptionValid() {
		return this.activeSubscriptionValid;
	}

	@FormData
	public void setActiveSubscriptionValid(final Boolean activeSubscriptionValid) {
		this.activeSubscriptionValid = activeSubscriptionValid;
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

	public LanguageField getLanguageField() {
		return this.getFieldByClass(LanguageField.class);
	}

	public SubscriptionBox getSubscriptionBox() {
		return this.getFieldByClass(SubscriptionBox.class);
	}

	public UserDetailBox getSubscriptionsDetailsBox() {
		return this.getFieldByClass(UserDetailBox.class);
	}

	public SubscriptionsListBox getSubscriptionsListBox() {
		return this.getFieldByClass(SubscriptionsListBox.class);
	}

	public SubscriptionsListTableField getSubscriptionsListTableField() {
		return this.getFieldByClass(SubscriptionsListTableField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Override
	public void initForm() {
		super.initForm();
		this.getSendUserInviteEmailField().setVisible(!this.isMyself());
		this.getLanguageField().setMandatory(this.isMyself());
		this.getTimeZoneField().setMandatory(this.isMyself());

		final Boolean isSubscirptionAdmin = this.isUserReadAmin();
		this.getUserIdField().setVisibleGranted(isSubscirptionAdmin);
	}

	private void initFormAfterLoad() {
		this.getPasswordField().setMandatory(this.isPasswordMandatory());
		this.getConfirmPasswordField().setMandatory(this.isPasswordMandatory());
		if (null == this.isAutofilled()) {
			this.setAutofilled(Boolean.FALSE);
		}
		this.getLanguageField().setDefaultLanguage();

	}

	private Boolean isMyself() {
		final Long currentUserId = ((AccessControlService) BEANS.get(IAccessControlService.class))
				.getZeroClickUserIdOfCurrentSubject();
		return null != currentUserId && currentUserId.equals(UserForm.this.getUserIdField().getValue());
	}

	private Boolean isPasswordMandatory() {
		return null == UserForm.this.getHashedPassword();
	}

	private Boolean isUserLoggedWithEmail() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final IUserService userServcie = BEANS.get(IUserService.class);

		final String curentUserId = acs.getUserIdOfCurrentUser();
		final UserFormData userData = userServcie.getCurrentUserDetails();

		return userData.getEmail().getValue().equalsIgnoreCase(curentUserId);
	}

	private Boolean isUserReadAmin() {
		return ACCESS.getLevel(new ReadUserPermission((Long) null)) == ReadUserPermission.LEVEL_ALL;
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
		public class LoginField extends org.zeroclick.ui.form.fields.loginfield.LoginField {
			private Boolean valueChanged = Boolean.FALSE;

			protected Boolean getValueChanged() {
				return this.valueChanged;
			}

			protected void setValueChanged(final Boolean valueChanged) {
				this.valueChanged = valueChanged;
			}

			@Override
			protected void execChangedValue() {
				super.execChangedValue();
				this.setValueChanged(Boolean.TRUE);
			}
		}

		@Order(2000)
		public class EmailField extends org.zeroclick.ui.form.fields.emailfield.EmailField {

			private Boolean valueChanged = Boolean.FALSE;

			protected Boolean getValueChanged() {
				return this.valueChanged;
			}

			protected void setValueChanged(final Boolean valueChanged) {
				this.valueChanged = valueChanged;
			}

			@Override
			protected void execChangedValue() {
				super.execChangedValue();
				this.setValueChanged(Boolean.TRUE);
			}
		}

		@Order(2500)
		public class PasswordField extends org.zeroclick.ui.form.fields.passwordfield.PasswordField {
			@Override
			protected String execValidateValue(final String rawValue) {
				final String superRawValue = super.execValidateValue(rawValue);

				UserForm.this.getConfirmPasswordField().setMandatory(Boolean.TRUE);
				return superRawValue;
			}
		}

		@Order(3000)
		public class ConfirmPasswordField
				extends org.zeroclick.ui.form.fields.confirmpasswordfield.ConfirmPasswordField {
			@Override
			protected String execValidateValue(final String rawValue) {
				final String password = UserForm.this.getPasswordField().getValue();

				super.checkPasswordMatches(password, rawValue);

				return rawValue;
			}
		}

		@Order(3500)
		public class TimeZoneField extends org.zeroclick.ui.form.fields.timezonefield.TimeZoneField {

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

		}

		@Order(3700)
		public class LanguageField extends org.zeroclick.ui.form.fields.languagefield.LanguageField {
			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}
		}

		@Order(4000)
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

		@Order(4250)
		public class SubscriptionBox extends org.zeroclick.configuration.client.user.fields.SubscriptionBox {
			private Boolean valueChanged = Boolean.FALSE;

			protected Boolean getValueChanged() {
				return this.valueChanged;
			}

			protected void setValueChanged(final Boolean valueChanged) {
				this.valueChanged = valueChanged;
			}

			@Override
			protected void execChangedValue() {
				super.execChangedValue();
				Boolean changed;
				if (null == this.getValue() && null == this.getInitValue()) {
					changed = Boolean.FALSE;
				} else if (null != this.getValue() && !this.getValue().equals(this.getInitValue())) {
					changed = Boolean.TRUE;
				} else {
					changed = Boolean.TRUE;
				}
				this.setValueChanged(changed);
			}
		}

		@Order(4500)
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

		@Order(52250)
		public class UserDetailBox extends AbstractTabBox {

			@Order(1000)
			public class SubscriptionsListBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.user.role.subscriptionList");
				}

				@Order(1000)
				public class SubscriptionsListTableField extends AbstractTableField<SubscriptionsListTableField.Table> {
					@Override
					protected String getConfiguredLabel() {
						return "";
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected int getConfiguredGridH() {
						return UserForm.this.isUserReadAmin() ? 6 : 3;
					}

					@Override
					protected void execInitField() {
						super.execInitField();
						final Boolean isUserAdmin = UserForm.this.isUserReadAmin();
						this.getTable().getAcceptedCpsDateColumn().setVisibleGranted(isUserAdmin);
						this.getTable().getAcceptedWithdrawalDateColumn().setVisibleGranted(isUserAdmin);
					}

					public class Table extends AbstractTable {

						@Override
						protected boolean getConfiguredTableStatusVisible() {
							return UserForm.this.isUserReadAmin();
						}

						@Override
						protected boolean getConfiguredHeaderEnabled() {
							return UserForm.this.isUserReadAmin();
						}

						public AcceptedCpsDateColumn getAcceptedCpsDateColumn() {
							return this.getColumnSet().getColumnByClass(AcceptedCpsDateColumn.class);
						}

						public AcceptedWithdrawalDateColumn getAcceptedWithdrawalDateColumn() {
							return this.getColumnSet().getColumnByClass(AcceptedWithdrawalDateColumn.class);
						}

						public SubscriptionIdColumn getSubscriptionIdColumn() {
							return this.getColumnSet().getColumnByClass(SubscriptionIdColumn.class);
						}

						public UserIdColumn getUserIdColumn() {
							return this.getColumnSet().getColumnByClass(UserIdColumn.class);
						}

						public StartDateColumn getStartDateColumn() {
							return this.getColumnSet().getColumnByClass(StartDateColumn.class);
						}

						@Order(1000)
						public class SubscriptionIdColumn extends AbstractSmartColumn<Long> {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.user.role.subscription.name");
							}

							@Override
							protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
								return SubscriptionLookupCall.class;
							}

							@Override
							protected boolean getConfiguredUiSortPossible() {
								return Boolean.TRUE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}

						@Order(2000)
						public class UserIdColumn extends AbstractLongColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.user.role.subscription.userId");
							}

							@Override
							protected boolean getConfiguredVisible() {
								return Boolean.FALSE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}

						@Order(3000)
						public class StartDateColumn extends AbstractDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.user.role.subscription.startDate");
							}

							@Override
							protected int getConfiguredSortIndex() {
								return 1;
							}

							@Override
							protected boolean getConfiguredSortAscending() {
								return Boolean.FALSE;
							}

							@Override
							protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
								return UserForm.this.isUserReadAmin();
							}

							@Override
							protected boolean getConfiguredHasDate() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredHasTime() {
								return Boolean.TRUE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 150;
							}
						}

						@Order(4000)
						public class AcceptedCpsDateColumn extends AbstractDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.user.role.subscription.acceptedCpsDate");
							}

							@Override
							protected boolean getConfiguredHasDate() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredHasTime() {
								return Boolean.TRUE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 150;
							}
						}

						@Order(5000)
						public class AcceptedWithdrawalDateColumn extends AbstractDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.user.role.subscription.acceptedWithdrawalDate");
							}

							@Override
							protected boolean getConfiguredHasDate() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredHasTime() {
								return Boolean.TRUE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 150;
							}
						}

					} // End table
				}// End table Field subscription list
			}// end subscriptions List Box
		}// end details box

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
		protected boolean execValidate() {
			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.user.notification.modifyingUser");
			return UserForm.this.handleSubscriptionModification();
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

			final String currentUserId = ((AccessControlService) BEANS.get(IAccessControlService.class))
					.getUserIdOfCurrentUser();
			// if loggedIn with Email can change login, else can change email.
			// Else user session NEED to be reloaded with new login/email.
			final Boolean userLoginChanged = UserForm.this.getLoginField().getValueChanged();
			final Boolean userLoginNotCurrent = !currentUserId.equals(formData.getLogin().getValue());
			final Boolean emailChanged = UserForm.this.getEmailField().getValueChanged();
			final Boolean userEmailNotCurrent = !currentUserId.equals(formData.getEmail().getValue());
			final Boolean loggedWithEmail = UserForm.this.isUserLoggedWithEmail();

			service.store(formData);

			if (UserForm.this.getSendUserInviteEmailField().getValue()) {
				UserForm.this.sendUserInviteEmail();
			}

			// hard to know if user use login or email to login. If ONE changed,
			// we reset session.
			final Boolean needSessionReload = UserForm.this.isMyself() && userLoginChanged && userLoginNotCurrent
					&& !loggedWithEmail || emailChanged && userEmailNotCurrent && loggedWithEmail;

			if (needSessionReload) {
				MessageBoxes.createOk().withHeader(TEXTS.get("zc.user.session.needReload.title"))
						.withBody(TEXTS.get("zc.user.session.needReload")).withIconId(Icons.ExclamationMark)
						.withSeverity(IStatus.WARNING).withAutoCloseMillis(10000L).show();
				ClientSession.get().stop();
			} else if (UserForm.this.getLanguageField().getValueChanged()) {
				UserForm.this.getLanguageField().askToReloadSession();
			}

			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProccessedNotification("zc.user.notification.modifiedUser",
					UserForm.this.getEmailField().getValue());

		}
	}

	/**
	 * Add the subscription key to the role box. Subscriptions ARE roles, with
	 * different UI
	 */
	protected Boolean handleSubscriptionModification() {
		Boolean subscriptionAndCpsValid = Boolean.TRUE;
		if (UserForm.this.getSubscriptionBox().getValueChanged()) {
			final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
			final Date startDate = appUserHelper.getUserNowInHisTimeZone(this.getUserIdField().getValue());
			final ValidateCpsForm validateCpsForm = new ValidateCpsForm();
			validateCpsForm.getUserIdField().setValue(this.getUserIdField().getValue());
			validateCpsForm.getSubscriptionIdField().setValue(this.getSubscriptionBox().getValue());
			validateCpsForm.getStartDateField().setValue(startDate);
			if (this.isMyself()) {
				validateCpsForm.setModal(Boolean.TRUE);
				validateCpsForm.startNew(this.getSubscriptionBox().getValue());
				validateCpsForm.waitFor();
				if (!validateCpsForm.isFormStored()) {
					this.getSubscriptionBox().resetValue();
					subscriptionAndCpsValid = Boolean.FALSE;
				}
			} else {
				// validateCpsForm.startNewFromAdmin(this.getSubscriptionBox().getValue());
			}
		}
		return subscriptionAndCpsValid;
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);
			UserForm.this.getUserIdField().setVisible(Boolean.FALSE);
			UserForm.this.getUserIdField().setMandatory(Boolean.FALSE);
			UserFormData formData = new UserFormData();
			UserForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			UserForm.this.importFormData(formData);

			UserForm.this.initFormAfterLoad();

			UserForm.this.setEnabledPermission(new CreateUserPermission());
		}

		@Override
		protected boolean execValidate() {
			if (!UserForm.this.isAutofilled()) {
				final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
				notificationHelper.addProcessingNotification("zc.user.notification.creatingUser");
			}
			return Boolean.TRUE;
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

		if (null == formData.getPassword().getValue()) {
			LOG.warn("No Generated Plain Paswword during saving new User. Generating one");
			this.generateAndAddFormPassword();
		}
		formData.setHashedPassword(UserForm.this.hashPassword(formData.getPassword().getValue()));

		if (null == formData.getAutofilled()) {
			formData.setAutofilled(Boolean.FALSE);
		}

		this.handleSubscriptionModification();

		final UserFormData savedData = service.create(formData);

		// Update userId field in this form for later use
		this.getUserIdField().setValue(savedData.getUserId().getValue());

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

		this.sendUserInviteEmail(this, userService.getCurrentUserDetails().getEmail().getValue(), null);
	}

	private void sendUserInviteEmail(final UserForm newUser, final String emailSender, final String meetinSubject) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);

		String subject;
		String messageBody;

		if (this.isAutofilled()) {
			// invite + meeting
			subject = TextsHelper.get(newUser.getUserIdField().getValue(), "zc.meeting.email.invit.subject",
					emailSender);
			messageBody = TextsHelper.get(newUser.getUserIdField().getValue(), "zc.meeting.email.invit.html",
					emailSender, new ApplicationUrlProperty().getValue(), newUser.getEmailField().getValue(),
					newUser.getPasswordField().getValue(), meetinSubject);
		} else {
			// Simple invite without meeting
			subject = TextsHelper.get(newUser.getUserIdField().getValue(), "zc.user.email.invit.withoutMeeting.subject",
					emailSender);
			messageBody = TextsHelper.get(newUser.getUserIdField().getValue(),
					"zc.user.email.invit.withoutMeeting.html", emailSender, new ApplicationUrlProperty().getValue(),
					newUser.getEmailField().getValue(), newUser.getPasswordField().getValue());
		}
		try {
			mailSender.sendEmail(newUser.getEmailField().getValue(), subject, messageBody);

			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.user.notification.invitedUser",
					newUser.getEmailField().getValue());
		} catch (final MailException e) {
			throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
		}
	}

	public UserForm autoFillInviteUser(final String email, final String emailSender, final String meetingSubject) {
		final IUserService userService = BEANS.get(IUserService.class);

		LOG.info("Creating and invite a new User with email : " + email + " by : " + emailSender
				+ " with meetingSubject : " + meetingSubject);

		if (userService.isEmailAlreadyUsed(email)) {
			new VetoException(TEXTS.get("zc.user.emailAlreadyUsed"));
		}

		this.createUser(email, Boolean.TRUE);
		this.save();
		this.sendUserInviteEmail(this, emailSender, meetingSubject);

		return this;
	}

	private UserForm createUser(final String email, final Boolean isAutoFileld) {
		final Long defaultRole = 2l;
		final Long subscriptionFreeId = 3l;

		this.getEmailField().setValue(email.toLowerCase());

		this.generateAndAddFormPassword();

		final Locale currentUserLocal = NlsLocale.get();
		String language = "FR";
		if (null != currentUserLocal && !currentUserLocal.getLanguage().isEmpty()) {
			language = currentUserLocal.getLanguage();
		}

		this.getLanguageField().setValue(language);

		this.getRolesBox().checkKey(defaultRole);
		this.getRolesBox().touch();

		this.getSubscriptionBox().setValue(subscriptionFreeId);

		this.setAutofilled(isAutoFileld);

		super.doSave();

		return this;
	}

	private void generateAndAddFormPassword() {
		String plainRandomPassword = this.generatePasword();

		if (null == plainRandomPassword || "".equals(plainRandomPassword.trim())) {
			LOG.warn("Generated Plain Password is null or empty ! Trying again ...");
			plainRandomPassword = this.generatePasword();
			if (null == plainRandomPassword || "".equals(plainRandomPassword.trim())) {
				LOG.warn("Generated Plain Password is STILL null or empty (last try) !");
				plainRandomPassword = this.generatePasword();
			}
		}

		this.getPasswordField().setValue(plainRandomPassword);
		this.getConfirmPasswordField().setValue(plainRandomPassword);
	}

	private String generatePasword() {
		final byte[] randomBytes = SecurityUtility.createRandomBytes(20);
		LOG.info("Generated auto-createUser (before Base64) : " + randomBytes);
		String plainRandomPassword = Base64Utility.encode(randomBytes);
		LOG.info("Generated auto-createUser (after Base64) : " + plainRandomPassword);
		plainRandomPassword.replaceAll("[iIlL0Oo/=\\+\\\\]", java.util.regex.Matcher.quoteReplacement(""));
		plainRandomPassword = plainRandomPassword.substring(0, 8);

		// TODO Djer13 remove this when possible
		LOG.info("Generated auto-createUser (after substring) partial : " + plainRandomPassword.substring(3, 6));

		return plainRandomPassword;
	}

}
