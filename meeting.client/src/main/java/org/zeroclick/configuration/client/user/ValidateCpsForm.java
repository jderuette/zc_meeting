package org.zeroclick.configuration.client.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AcceptConditionBox;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AcceptConditionBox.AcceptCpsField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AcceptConditionBox.AcceptWithdrawalField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AcceptConditionBox.AcceptedCpsDateField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AcceptConditionBox.AcceptedWithdrawalDateField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.BottomBox.AddSubscriptionField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.CpsTextField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.OkButton;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.StartDateField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.SubscriptionIdField;
import org.zeroclick.configuration.client.user.ValidateCpsForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.ValidateCpsFormData;

@FormData(value = ValidateCpsFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ValidateCpsForm extends AbstractForm {

	private Long defaultSubscriptionIdValue;
	private Boolean isSubscriptionPaymentValid;

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.role.subscription.validateCPS");
	}

	public void startNew(final Long defaultSubscriptionIdValue) {
		this.defaultSubscriptionIdValue = defaultSubscriptionIdValue;
		this.startInternal(new NewHandler());
	}

	public void startNew() {
		this.startNew(4l);
	}

	public void startModify(final Long defaultSubscriptionIdValue) {
		this.defaultSubscriptionIdValue = defaultSubscriptionIdValue;
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startReValidate(final Long userId) {
		this.defaultSubscriptionIdValue = 4l;
		this.getUserIdField().setValue(userId);
		this.startInternalExclusive(new ModifyHandler(Boolean.TRUE));
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public CpsTextField getCpsTextField() {
		return this.getFieldByClass(CpsTextField.class);
	}

	public AcceptWithdrawalField getAcceptWithdrawalField() {
		return this.getFieldByClass(AcceptWithdrawalField.class);
	}

	public AcceptCpsField getAcceptCpsField() {
		return this.getFieldByClass(AcceptCpsField.class);
	}

	public SubscriptionIdField getSubscriptionIdField() {
		return this.getFieldByClass(SubscriptionIdField.class);
	}

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public StartDateField getStartDateField() {
		return this.getFieldByClass(StartDateField.class);
	}

	public AcceptedCpsDateField getAcceptedCpsDateField() {
		return this.getFieldByClass(AcceptedCpsDateField.class);
	}

	public AcceptedWithdrawalDateField getAcceptedWithdrawalDateField() {
		return this.getFieldByClass(AcceptedWithdrawalDateField.class);
	}

	public AddSubscriptionField getAddSubscriptionField() {
		return this.getFieldByClass(AddSubscriptionField.class);
	}

	public AcceptConditionBox getAcceptConditionBox() {
		return this.getFieldByClass(AcceptConditionBox.class);
	}

	public BottomBox getBottomBox() {
		return this.getFieldByClass(BottomBox.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	private Boolean isSubscriptionAdmin() {
		return ACCESS.getLevel(new UpdateAssignSubscriptionToUserPermission(
				(Long) null)) == UpdateAssignSubscriptionToUserPermission.LEVEL_ALL;
	}

	@Override
	public void initForm() {
		super.initForm();
		final Boolean isSubscriptionAdmin = this.isSubscriptionAdmin();
		this.getUserIdField().setVisibleGranted(isSubscriptionAdmin);
		// this.getUserIdField().setEnabledGranted(isSubscriptionAdmin);

		// this.getStartDateField().setVisibleGranted(isSubscriptionAdmin);
		this.getStartDateField().setEnabledGranted(isSubscriptionAdmin);

		this.getAcceptedCpsDateField().setVisibleGranted(isSubscriptionAdmin);
		this.getAcceptedWithdrawalDateField().setVisibleGranted(isSubscriptionAdmin);
	}

	private void initFormAfterLoad() {

		if (null == this.getStartDateField().getValue()) {
			this.getStartDateField().setValue(this.getNowUserDate());
		}

		if (null == this.getSubscriptionIdField().getValue()) {
			this.getSubscriptionIdField().setValue(this.defaultSubscriptionIdValue);
		}

		if (this.getSubscriptionIdField().getValue() == 3l) { // free
			this.getAcceptedWithdrawalDateField().setMandatory(Boolean.FALSE);
			this.getAcceptedWithdrawalDateField().setVisible(Boolean.FALSE);

			this.getAcceptWithdrawalField().setMandatory(Boolean.FALSE);
			this.getAcceptWithdrawalField().setVisible(Boolean.FALSE);
		} else {
			// if only CPS (re) validation, no (Zoho) subscription links
			this.getAcceptWithdrawalField().setMandatory(!this.isSubscriptionPaymentValid());
			this.getAcceptWithdrawalField().setVisible(!this.isSubscriptionPaymentValid());
		}
		this.loadCpsText(this.getSubscriptionIdField().getValue());

		this.isSubscriptionPaymentValid = ValidateCpsForm.this.isSubscriptionPaymentValid();

	}

	private Boolean isSubscriptionPaymentValid() {
		return null != this.getAcceptedWithdrawalDateField().getValue();
	}

	private Date getNowUserDate() {
		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		return appUserHelper.getUserNowInHisTimeZone(this.getUserIdField().getValue());
	}

	private void loadCpsText(final Long subscriptionId) {
		final SubscriptionHelper subscriptionHelper = BEANS.get(SubscriptionHelper.class);
		final String cpsText = subscriptionHelper.getCpsText(subscriptionId);
		if (null == cpsText || "".equals(cpsText)) {
			this.getCpsTextField().setVisible(Boolean.FALSE);
		} else {
			this.getCpsTextField().setValue(cpsText);
		}
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class SubscriptionIdField extends org.zeroclick.configuration.client.user.fields.SubscriptionBox {
			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}
		}

		@Order(2000)
		public class CpsTextField extends AbstractHtmlField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.cpsLabel");
			}

			@Override
			protected int getConfiguredGridW() {
				return 3;
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredScrollBarEnabled() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredGridH() {
				return 12;
			}
		}

		@Order(3000)
		public class BottomBox extends AbstractGroupBox {
			@Override
			protected String getConfiguredLabel() {
				return "";
			}

			@Override
			protected boolean getConfiguredLabelVisible() {
				return Boolean.FALSE;
			}

			@Order(1000)
			public class AcceptConditionBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return "";
				}

				@Override
				protected boolean getConfiguredLabelVisible() {
					return Boolean.FALSE;
				}

				@Override
				protected int getConfiguredGridW() {
					return 1;
				}

				@Override
				protected int getConfiguredGridColumnCount() {
					return 1;
				}

				@Order(1000)
				public class AcceptCpsField extends AbstractBooleanField {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.user.role.subscription.acceptCps");
					}

					@Override
					protected void execChangedValue() {
						if (this.getValue()) {
							ValidateCpsForm.this.getAcceptedCpsDateField()
									.setValue(ValidateCpsForm.this.getNowUserDate());
							if (ValidateCpsForm.this.getAcceptWithdrawalField().isVisible()) {
								if (ValidateCpsForm.this.getAcceptWithdrawalField().getValue()) {
									ValidateCpsForm.this.getAddSubscriptionField().activateIfRequired();
								}
							} else {
								ValidateCpsForm.this.getAddSubscriptionField().activateIfRequired();
							}
						} else {
							ValidateCpsForm.this.getAcceptedCpsDateField().setValue(null);
							ValidateCpsForm.this.getAddSubscriptionField().setInactive();
						}
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}
				}

				@Order(2000)
				public class AcceptedCpsDateField extends AbstractDateField {
					@Override
					protected String getConfiguredLabel() {
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
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}
				}

				@Order(3000)
				public class AcceptWithdrawalField extends AbstractBooleanField {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.user.role.subscription.acceptWithdrawal");
					}

					@Override
					protected void execChangedValue() {
						if (this.getValue()) {
							ValidateCpsForm.this.getAcceptedWithdrawalDateField()
									.setValue(ValidateCpsForm.this.getNowUserDate());
							if (ValidateCpsForm.this.getAcceptCpsField().isVisible()) {
								if (ValidateCpsForm.this.getAcceptCpsField().getValue()) {
									ValidateCpsForm.this.getAddSubscriptionField().activateIfRequired();
								}
							} else {
								ValidateCpsForm.this.getAddSubscriptionField().activateIfRequired();
							}
						} else {
							ValidateCpsForm.this.getAcceptedWithdrawalDateField().setValue(null);
							ValidateCpsForm.this.getAddSubscriptionField().setInactive();
						}
					}

					@Override
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}
				}

				@Order(4000)
				public class AcceptedWithdrawalDateField extends AbstractDateField {
					@Override
					protected String getConfiguredLabel() {
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
					protected boolean getConfiguredMandatory() {
						return Boolean.TRUE;
					}
				}

			}

			@Order(4000)
			public class AddSubscriptionField extends AbstractHtmlField {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.user.role.subscription.subscribe");
				}

				@Override
				protected void execInitField() {
					super.execInitField();
					this.setInactive();
				}

				@Override
				protected String getConfiguredLabelBackgroundColor() {
					return "AABB66";
				}

				public void activateIfRequired() {
					// TODO Djer enable OK button *after* user click on link
					ValidateCpsForm.this.getOkButton().setActive();
					// ValidateCpsForm.this.getOkButton().setInactive(TEXTS.get("zc.user.role.subscription.mustChooseSubscription"));
					// visible only if a payment URL exist for this subscription
					final SubscriptionHelper subscriptionHelper = BEANS.get(SubscriptionHelper.class);
					final Long subscriptionId = ValidateCpsForm.this.getSubscriptionIdField().getValue();
					final String url = subscriptionHelper.getSubscriptionPaymentURL(subscriptionId);
					this.setValue(url);

					if (!ValidateCpsForm.this.isSubscriptionPaymentValid) {
						if (null != url) {
							// payment required if payment URL configured
							this.setVisible(Boolean.TRUE);
							ValidateCpsForm.this.getOkButton().setPayementRequired();
						}
					}
				}

				public void setInactive() {
					ValidateCpsForm.this.getOkButton().setInactive();
					this.setVisible(Boolean.FALSE);
				}
			}
		}

		@Order(5000)
		public class UserIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.userId");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
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

		@Order(6000)
		public class StartDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.startDate");
			}

			@Override
			protected boolean getConfiguredHasDate() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return Boolean.TRUE;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.mustAcceptCps");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			public void setActive() {
				this.setLabel(TEXTS.get("OkButton"));
				this.setEnabled(Boolean.TRUE);
				ValidateCpsForm.this.getAddSubscriptionField().setVisible(Boolean.FALSE);
				this.setBackgroundColor(this.getConfiguredBackgroundColor());
			}

			public void setInactive(final String reason) {
				this.setLabel(reason);
				this.setEnabled(this.getConfiguredEnabled());
				ValidateCpsForm.this.getAddSubscriptionField().setVisible(Boolean.TRUE);
			}

			public void setInactive() {
				this.setInactive(this.getConfiguredLabel());
			}

			public void setPayementRequired() {
				this.setLabel(TEXTS.get("zc.user.role.subscription.mustFillPayment"));
				this.setBackgroundColor("AAAAA");
			}
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		Boolean validationRequired;

		public ModifyHandler() {
			super();
			this.validationRequired = Boolean.FALSE;
		}

		public ModifyHandler(final Boolean forceValidation) {
			super();
			this.validationRequired = forceValidation;
		}

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);
			ValidateCpsFormData formData = null;
			if (this.validationRequired) {
				// assume this a re-validation
				formData = service.getActiveSubscriptionDetails(ValidateCpsForm.this.getUserIdField().getValue());
				ValidateCpsForm.this.getCancelButton().setVisible(Boolean.FALSE);
			} else {
				formData = new ValidateCpsFormData();
				ValidateCpsForm.this.exportFormData(formData);
				formData = service.load(formData);
			}
			ValidateCpsForm.this.importFormData(formData);

			ValidateCpsForm.this.setEnabledPermission(
					new UpdateAssignSubscriptionToUserPermission(formData.getUserId().getValue()));

			ValidateCpsForm.this.initFormAfterLoad();
		}

		@Override
		protected void execStore() {
			final IUserService service = BEANS.get(IUserService.class);
			final ValidateCpsFormData formData = new ValidateCpsFormData();
			ValidateCpsForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);
			ValidateCpsFormData formData = new ValidateCpsFormData();

			ValidateCpsForm.this.exportFormData(formData);
			formData = service.load(formData);
			ValidateCpsForm.this.importFormData(formData);

			ValidateCpsForm.this.setEnabledPermission(
					new CreateAssignSubscriptionToUserPermission(formData.getUserId().getValue()));

			ValidateCpsForm.this.initFormAfterLoad();
		}

		@Override
		protected void execStore() {
			final IUserService service = BEANS.get(IUserService.class);
			final ValidateCpsFormData formData = new ValidateCpsFormData();
			ValidateCpsForm.this.exportFormData(formData);

			final ValidateCpsFormData savedData = service.create(formData);

			// send admin Email to check payement
			this.informAdminNewCpsValidated(savedData);
		}

		private void informAdminNewCpsValidated(final ValidateCpsFormData savedData) {
			final IMailSender mailSender = BEANS.get(IMailSender.class);
			final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);
			final IUserService userService = BEANS.get(IUserService.class);

			final String recipient = appParamsService.getValue("subInfoEmail");

			final Locale adminLocale = new Locale("FR");

			final UserFormData currentUserDetails = userService.getCurrentUserDetails();
			final String userEmail = currentUserDetails.getEmail().getValue();

			final List<String> values = new ArrayList<>();
			values.add(savedData.getUserId().getValue() + " (" + userEmail + ")");
			values.add(savedData.getSubscriptionId().getValue() + " ("
					+ ValidateCpsForm.this.getSubscriptionIdField().getDisplayText() + ")");

			final String[] valuesArray = CollectionUtility.toArray(values, String.class);

			final String subject = TEXTS.get(adminLocale, "zc.user.role.subscription.email.cpsValidated.subject",
					valuesArray);
			final String content = TEXTS.get(adminLocale, "zc.user.role.subscription.email.cpsValidated.html",
					valuesArray);

			try {
				mailSender.sendEmail(recipient, subject, content);
			} catch (final MailException e) {
				throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
			}
		}
	}

}
