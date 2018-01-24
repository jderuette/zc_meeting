package org.zeroclick.configuration.onboarding;

import java.util.Map;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.client.api.ApiCreatedNotificationHandler;
import org.zeroclick.configuration.client.api.ApiDeletedNotificationHandler;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.AddCalendarField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.LanguageField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.LoginField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.OkButton;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.TimeZoneField;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm.MainBox.UserIdField;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

@FormData(value = OnBoardingUserFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class OnBoardingUserForm extends AbstractForm {

	private static final Logger LOG = LoggerFactory.getLogger(OnBoardingUserForm.class);

	private INotificationListener<ApiCreatedNotification> apiCreatedListener;
	private INotificationListener<ApiDeletedNotification> apiDeletedListener;

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
				if (BEANS.get(GoogleApiHelper.class).isCalendarConfigured()) {
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
				OnBoardingUserForm.this.getAddCalendarField().setVisible(Boolean.FALSE);
			}

			public void setInactive() {
				this.setLabel(this.getConfiguredLabel());
				this.setEnabled(this.getConfiguredEnabled());
				this.setBackgroundColor(COLOR_WARNING_BACKGROUND);
				OnBoardingUserForm.this.getAddCalendarField().setVisible(Boolean.TRUE);
			}
		}
	}

	@Override
	protected void execInitForm() {
		final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
				.get(ApiCreatedNotificationHandler.class);
		apiCreatedNotificationHandler.addListener(this.createApiCreatedListener());

		final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
				.get(ApiDeletedNotificationHandler.class);
		apiDeletedNotificationHandler.addListener(this.createApiDeletedListener());
	}

	@Override
	protected void execDisposeForm() {
		final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
				.get(ApiCreatedNotificationHandler.class);
		apiCreatedNotificationHandler.removeListener(this.apiCreatedListener);

		final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
				.get(ApiDeletedNotificationHandler.class);
		apiDeletedNotificationHandler.removeListener(this.apiDeletedListener);
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

	private INotificationListener<ApiCreatedNotification> createApiCreatedListener() {
		this.apiCreatedListener = new INotificationListener<ApiCreatedNotification>() {
			@Override
			public void handleNotification(final ApiCreatedNotification notification) {
				try {
					final ApiFormData eventForm = notification.getApiForm();
					LOG.debug("Created Api prepare to modify OnBoardingForm state : " + eventForm.getUserId());
					OnBoardingUserForm.this.getOkButton().setActive();

					// auto import calendar
					final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
					final Map<String, AbstractCalendarConfigurationTableRowData> calendars = googleHelper
							.getCalendars();

					final ICalendarConfigurationService calendarConfigurationService = BEANS
							.get(ICalendarConfigurationService.class);
					calendarConfigurationService.autoConfigure(calendars);

				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiCreatedListener;
	}

	private INotificationListener<ApiDeletedNotification> createApiDeletedListener() {
		this.apiDeletedListener = new INotificationListener<ApiDeletedNotification>() {
			@Override
			public void handleNotification(final ApiDeletedNotification notification) {
				try {
					final ApiFormData eventForm = notification.getApiForm();
					LOG.debug("Deleted Api prepare to modify OnBoardingForm state : " + eventForm.getUserId());
					OnBoardingUserForm.this.getOkButton().setInactive();
				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiDeletedListener;
	}
}
