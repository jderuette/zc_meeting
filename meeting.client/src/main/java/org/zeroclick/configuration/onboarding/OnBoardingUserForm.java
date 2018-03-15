package org.zeroclick.configuration.onboarding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.api.AbstractApiTable;
import org.zeroclick.configuration.client.slot.SlotsForm;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.AddCalendarField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.ApisTableField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.EditSlotConfigButton;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.LanguageField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.LoginField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.OkButton;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.TimeZoneField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.service.CalendarService;

@FormData(value = OnBoardingUserFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class OnBoardingUserForm extends AbstractForm {

	private PropertyChangeListener apiEmptynessChangeListener;

	private static final String COLOR_WARNING_BACKGROUND = "FF9D00";

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.common.onBoarding.user");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	private void initFormAfterLoad() {
		this.getLanguageField().setDefaultLanguage();
		this.getApisTableField().setVisible(this.getApisTableField().getTable().getRowCount() > 0);
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

	public AddCalendarField getAddCalendarField() {
		return this.getFieldByClass(AddCalendarField.class);
	}

	public LanguageField getLanguageField() {
		return this.getFieldByClass(LanguageField.class);
	}

	public ApisTableField getApisTableField() {
		return this.getFieldByClass(ApisTableField.class);
	}

	// public SlotConfigField getSlotConfigField() {
	// return this.getFieldByClass(SlotConfigField.class);
	// }

	public EditSlotConfigButton getEditSlotConfigButton() {
		return this.getFieldByClass(EditSlotConfigButton.class);
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
		public class TimeZoneField extends org.zeroclick.ui.form.fields.timezonefield.TimeZoneField {
		}

		@Order(3000)
		public class LoginField extends org.zeroclick.ui.form.fields.loginfield.LoginField {
		}

		@Order(3500)
		public class LanguageField extends org.zeroclick.ui.form.fields.languagefield.LanguageField {

		}

		@Order(4000)
		public class EditSlotConfigButton extends AbstractButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.slot.configuration");
			}

			@Override
			protected boolean getConfiguredProcessButton() {
				return false;
			}

			@Override
			protected String getConfiguredTooltipText() {
				return TEXTS.get("zc.meeting.slot.configuration.description.short");
			}

			@Override
			protected void execClickAction() {
				final SlotsForm slotsForm = new SlotsForm();
				slotsForm.startModify();
			}
		}

		@Order(5000)
		public class AddCalendarField extends AbstractHtmlField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.addCalendar");
			}

			@Override
			protected String getConfiguredTooltipText() {
				return TEXTS.get("zc.meeting.addCalendar.tooltips");
			}

			@Override
			protected void execInitField() {
				super.execInitField();
				this.setHtmlEnabled(Boolean.TRUE);
				this.setValue(BEANS.get(GoogleApiHelper.class).getAddGoogleLink());
			}
		}

		@Order(6000)
		public class ApisTableField extends AbstractTableField<AbstractApiTable> {

			@Override
			protected boolean getConfiguredLabelVisible() {
				return false;
			}

			@Override
			protected int getConfiguredGridH() {
				return 3;
			}

			public class Table extends AbstractApiTable {

			}

		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.onboarding.shouldAddGoogle");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.TRUE;
			}

			@Override
			protected String getConfiguredBackgroundColor() {
				return COLOR_WARNING_BACKGROUND;
			}

			@Override
			protected void execInitField() {
				super.execInitField();
				if (BEANS.get(CalendarService.class).isCalendarConfigured()) {
					this.setActive();
				} else {
					this.setInactive();
				}
			}

			@Override
			protected boolean execIsSaveNeeded() {
				// to force form save even if no modification done in Fields
				return Boolean.TRUE;
			}

			public void setActive() {
				this.setLabel(TEXTS.get("SaveButton"));
				this.setEnabled(Boolean.TRUE);
				this.setBackgroundColor(null);// default color
				// OnBoardingUserForm.this.getAddCalendarField().setVisible(Boolean.FALSE);
			}

			public void setInactive() {
				this.setLabel(this.getConfiguredLabel());
				this.setEnabled(this.getConfiguredEnabled());
				this.setBackgroundColor(COLOR_WARNING_BACKGROUND);
				// OnBoardingUserForm.this.getAddCalendarField().setVisible(Boolean.TRUE);
			}
		}
	}

	@Override
	protected void execInitForm() {

		this.apiEmptynessChangeListener = new EmptynessPropertyChangeListener();
		this.getApisTableField().addPropertyChangeListener("empty", this.apiEmptynessChangeListener);
	}

	@Override
	protected void execDisposeForm() {

		this.getApisTableField().removePropertyChangeListener("empty", this.apiEmptynessChangeListener);
	}

	private class EmptynessPropertyChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if ("empty".equals(evt.getPropertyName())) {
				final Boolean isEmpty = (Boolean) evt.getNewValue();
				if (isEmpty) { // table became empty
					OnBoardingUserForm.this.getOkButton().setInactive();
				} else {
					OnBoardingUserForm.this.getOkButton().setActive();
				}
				OnBoardingUserForm.this.getApisTableField().setVisible(!isEmpty);
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

			OnBoardingUserForm.this.initFormAfterLoad();

			OnBoardingUserForm.this.setEnabledPermission(new UpdateUserPermission(formData.getUserId().getValue()));
		}

		@Override
		protected void execStore() {
			final IUserService service = BEANS.get(IUserService.class);
			final OnBoardingUserFormData formData = new OnBoardingUserFormData();
			OnBoardingUserForm.this.exportFormData(formData);

			service.store(formData);

			if (OnBoardingUserForm.this.getLanguageField().getValueChanged()) {
				if (OnBoardingUserForm.this.isSaveNeeded()) {
					// Try to avoid popup "save unsaved forms"
					OnBoardingUserForm.this.markSaved();
					OnBoardingUserForm.this.setAskIfNeedSave(Boolean.FALSE);
				}
				OnBoardingUserForm.this.getLanguageField().askToReloadSession();
			}
		}
	}
}
