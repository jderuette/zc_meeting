package org.zeroclick.configuration.client.user;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.user.ViewCpsForm.MainBox.CloseButton;
import org.zeroclick.configuration.client.user.ViewCpsForm.MainBox.ContractsField;
import org.zeroclick.configuration.client.user.ViewCpsForm.MainBox.CpsTextField;
import org.zeroclick.configuration.client.user.ViewCpsForm.MainBox.SubscriptionIdField;
import org.zeroclick.configuration.client.user.ViewCpsForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ValidateCpsFormData;
import org.zeroclick.ui.form.fields.datefield.AbstractZonedDateField;

public class ViewCpsForm extends AbstractForm {

	private Long subscriptionId;

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.role.subscription.cps.view");
	}

	public void startNew(final Long subscriptionId) {
		this.subscriptionId = subscriptionId;
		this.startInternal(new NewHandler());
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public ContractsField getContractsField() {
		return this.getFieldByClass(ContractsField.class);
	}

	public SubscriptionIdField getSubscriptionIdField() {
		return this.getFieldByClass(SubscriptionIdField.class);
	}

	public CpsTextField getCpsTextField() {
		return this.getFieldByClass(CpsTextField.class);
	}

	public UserIdField getUserIdField() {
		return this.getFieldByClass(UserIdField.class);
	}

	public CloseButton getCloseButton() {
		return this.getFieldByClass(CloseButton.class);
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
		public class AcceptedCpsDateField extends AbstractZonedDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.acceptedCpsDate");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return false;
			}

			@Override
			protected boolean getConfiguredHasDate() {
				return true;
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return true;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return false;
			}
		}

		@Order(3000)
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

		@Order(4000)
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
			protected boolean getConfiguredVisible() {
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

		@Order(5000)
		public class ContractsField extends AbstractHtmlField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.subscription.contracts");
			}

			@Override
			protected void execInitField() {
				super.execInitField();
				this.setHtmlEnabled(Boolean.TRUE);
				final IAppParamsService appParamService = BEANS.get(IAppParamsService.class);
				final String url = appParamService.getValue(IAppParamsService.APP_PARAM_KEY_TOS_URL);

				if (null == url || url.isEmpty()) {
					throw new VetoException("No Term Of Use to display");
				}

				this.setValue(MainBox.this.buildLink(TEXTS.get("zc.user.role.subscription.tos.label"), url));
			}
		}

		public String buildLink(final String label, final String url) {
			final StringBuilder builder = new StringBuilder(64);
			builder.append("<a href='").append(url).append("' target='_blank'>").append(label).append("</a>");

			return builder.toString();
		}

		@Order(100000)
		public class CloseButton extends AbstractCloseButton {
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IUserService service = BEANS.get(IUserService.class);

			ViewCpsForm.this.getSubscriptionIdField().setValue(ViewCpsForm.this.subscriptionId);
			ValidateCpsFormData formData = new ValidateCpsFormData();

			ViewCpsForm.this.exportFormData(formData);
			formData = service.getActiveSubscriptionDetails(ViewCpsForm.this.getUserIdField().getValue());
			ViewCpsForm.this.importFormData(formData);

			ViewCpsForm.this
					.setEnabledPermission(new ReadAssignSubscriptionToUserPermission(formData.getUserId().getValue()));

			ViewCpsForm.this.loadCpsText(ViewCpsForm.this.getSubscriptionIdField().getValue());
		}

		@Override
		protected void execStore() {
			// Do Nothing
		}
	}
}
