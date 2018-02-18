package org.zeroclick.meeting.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.notification.DesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.client.ConfigurationOutline;
import org.zeroclick.configuration.client.administration.AdministrationOutline;
import org.zeroclick.configuration.client.api.ApiCreatedNotificationHandler;
import org.zeroclick.configuration.client.api.ApiDeletedNotificationHandler;
import org.zeroclick.configuration.client.api.ApiModifiedNotificationHandler;
import org.zeroclick.configuration.client.slot.SlotsForm;
import org.zeroclick.configuration.client.user.UserForm;
import org.zeroclick.configuration.client.user.UserModifiedNotificationHandler;
import org.zeroclick.configuration.client.user.ValidateCpsForm;
import org.zeroclick.configuration.client.user.ViewCpsForm;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.api.ApiModifiedNotification;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.configuration.shared.role.ReadPermissionPermission;
import org.zeroclick.configuration.shared.role.ReadRolePermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.meeting.client.calendar.CalendarsConfigurationForm;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.client.meeting.MeetingOutline;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CreateApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.ReadApiPermission;
import org.zeroclick.meeting.shared.event.ReadEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 * <h3>{@link Desktop}</h3>
 *
 * @author djer
 */
public class Desktop extends AbstractDesktop {

	private static final Logger LOG = LoggerFactory.getLogger(Desktop.class);

	protected INotificationListener<ApiCreatedNotification> apiCreatedListener;
	private INotificationListener<ApiDeletedNotification> apiDeletedListener;
	private INotificationListener<ApiModifiedNotification> apiModifiedListener;
	private INotificationListener<UserModifiedNotification> userModifiedListener;

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.common.applicationTitle");
	}

	@Override
	protected String getConfiguredLogoId() {
		return Icons.APP_LOGO_64_64;
	}

	@Override
	protected List<Class<? extends IOutline>> getConfiguredOutlines() {
		final List<Class<? extends IOutline>> outlines = new ArrayList<>();
		outlines.add(MeetingOutline.class);
		outlines.add(ConfigurationOutline.class);
		outlines.add(AdministrationOutline.class);

		return outlines;
	}

	@Override
	protected void execGuiAttached() {
		super.execGuiAttached();
		this.selectFirstVisibleOutline();
		if (null != this.getOutline()) {
			this.activateFirstPage();
		}

		final ApiCreatedNotificationHandler apiCreatedNotifHand = BEANS.get(ApiCreatedNotificationHandler.class);
		apiCreatedNotifHand.addListener(this.createApiCreatedListener());

		final ApiDeletedNotificationHandler apiDeletedNotifHand = BEANS.get(ApiDeletedNotificationHandler.class);
		apiDeletedNotifHand.addListener(this.createApiDeletedListener());

		final ApiModifiedNotificationHandler apiModifiedNotifHand = BEANS.get(ApiModifiedNotificationHandler.class);
		apiModifiedNotifHand.addListener(this.createApiModifiedListener());

		final UserModifiedNotificationHandler userModifiedNotifHand = BEANS.get(UserModifiedNotificationHandler.class);
		userModifiedNotifHand.addListener(this.createUserModifiedListener());

		this.checkAndUpdateRequiredDatas();
	}

	private void refreshAllPages() {
		for (final IOutline outline : this.getAvailableOutlines()) {
			outline.refreshPages(IPage.class);
		}
	}

	private void checkAndUpdateRequiredDatas() {
		final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
		final IUserService userService = BEANS.get(IUserService.class);
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();

		this.cleanInvalid1ApiKey(currentUserId);

		// default user timezone
		final String currentUserTimeZone = userService.getUserTimeZone(currentUserId);
		final Boolean isCalendarConfigured = googleHelper.isCalendarConfigured();
		if (null == currentUserTimeZone || !isCalendarConfigured) {
			final OnBoardingUserForm form = new OnBoardingUserForm();
			form.getUserIdField().setValue(currentUserId);
			form.setEnabledPermission(new UpdateUserPermission(currentUserId));
			form.startModify();
			form.waitFor();
		}

		// check CPS validity
		final UserFormData userDetails = userService.getCurrentUserDetails();
		if (!userDetails.getActiveSubscriptionValid()) {
			final ValidateCpsForm validateCpsForm = new ValidateCpsForm();
			validateCpsForm.startReValidate(currentUserId);
			// accepted CPS/withdraw and respective Date are let empty to force
			// User re-check
		}

		if (isCalendarConfigured) {
			this.autoImportCalendars(currentUserId);
		}

	}

	private void cleanInvalid1ApiKey(final Long userId) {
		final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);

		// API connection for multiple account (link to email address, and store
		// by API key instead of user Id key
		final IApiService apiService = BEANS.get(IApiService.class);
		final ApiTablePageData userApis = apiService.getApis(userId);

		if (null != userApis && userApis.getRowCount() > 0) {
			for (final ApiTableRowData userApi : userApis.getRows()) {
				if (StringUtility.isNullOrEmpty(userApi.getAccountEmail())) {
					// User has old API system, so is API is store by UserId
					try {
						LOG.info("Invalid API detected (no email account) id : " + userApi.getApiCredentialId()
								+ " removing it fro user : " + userId);
						googleHelper.removeCredential(userId);
						@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
						final ApiFormData apiFormData = new ApiFormData();
						apiFormData.setApiCredentialId(userApi.getApiCredentialId());
						apiFormData.setUserId(userId);
						apiFormData.getProvider().setValue(ProviderCodeType.GoogleCode.ID);
						apiService.delete(apiFormData);

					} catch (final IOException ioe) {
						LOG.error(
								"Error while removing 'old' (Google) API stored by UserId instead of apiCredentialId for user Id : "
										+ userId,
								ioe);
					}
				}
			}
		}
	}

	private void autoImportCalendars(final long userId) {
		// Calendars configuration in 0Click
		final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);

		final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
		formData.getUserId().setValue(userId);
		final CalendarsConfigurationFormData configuredCalendars = calendarConfigurationService
				.getCalendarConfigurationTableData(false);

		if (null == configuredCalendars
				|| null != configuredCalendars && configuredCalendars.getCalendarConfigTable() != null
						&& configuredCalendars.getCalendarConfigTable().getRowCount() == 0) {
			LOG.info("Auto-importing user Calendars for user : " + userId);

			try {
				googleHelper.autoConfigureCalendars();
			} catch (final Exception ex) {
				LOG.error("Error while importing (Google) calendar for user Id : " + userId, ex);
			}
		}
	}

	@Override
	protected void execClosing() {
		if (null != this.apiCreatedListener) {
			final ApiCreatedNotificationHandler apiCreatedNotifHand = BEANS.get(ApiCreatedNotificationHandler.class);
			apiCreatedNotifHand.removeListener(this.apiCreatedListener);
		}

		if (null != this.apiDeletedListener) {
			final ApiDeletedNotificationHandler apiDeletedNotifHand = BEANS.get(ApiDeletedNotificationHandler.class);
			apiDeletedNotifHand.removeListener(this.apiDeletedListener);
		}

		if (null != this.apiModifiedListener) {
			final ApiModifiedNotificationHandler apiModifiedNotifHand = BEANS.get(ApiModifiedNotificationHandler.class);
			apiModifiedNotifHand.removeListener(this.apiModifiedListener);
		}

		if (null != this.userModifiedListener) {
			final UserModifiedNotificationHandler userModifiedNotifHand = BEANS
					.get(UserModifiedNotificationHandler.class);
			userModifiedNotifHand.removeListener(this.userModifiedListener);
		}
	}

	protected void selectFirstVisibleOutline() {
		for (final IOutline outline : this.getAvailableOutlines()) {
			if (outline.isEnabled() && outline.isVisible()) {
				this.setOutline(outline);
				break;
			}
		}
	}

	protected Boolean isMySelf(final Long userId) {
		final Long currentUser = BEANS.get(AppUserHelper.class).getCurrentUserId();
		return currentUser.equals(userId);
	}

	public void addNotification(final Integer severity, final Long duration, final Boolean closable,
			final String messageKey) {
		final IStatus status = new Status(TEXTS.get(messageKey), severity);
		this.addNotification(status, duration, closable);
	}

	public void addNotification(final Integer severity, final Long duration, final Boolean closable,
			final String messageKey, final String... messageArguments) {
		final IStatus status = new Status(TEXTS.get(messageKey, messageArguments), severity);
		this.addNotification(status, duration, closable);
	}

	private void addNotification(final IStatus status, final Long duration, final Boolean closable) {
		Jobs.schedule(new IRunnable() {

			@Override
			@SuppressWarnings("PMD.SignatureDeclareThrowsException")
			public void run() throws Exception {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder().append("Adding desktop notification : ").append(status.getMessage())
							.append("(").append(status.getClass()).append(")").toString());
				}
				final DesktopNotification desktopNotification = new DesktopNotification(status, duration, closable);
				ClientSession.get().getDesktop().addNotification(desktopNotification);
			}
		}, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("adding desktop notification (in ModelJob)"));
	}

	private INotificationListener<ApiCreatedNotification> createApiCreatedListener() {
		this.apiCreatedListener = new INotificationListener<ApiCreatedNotification>() {
			@Override
			public void handleNotification(final ApiCreatedNotification notification) {
				try {
					final ApiFormData apiForm = notification.getFormData();

					if (Desktop.this.isMySelf(apiForm.getUserId())) {
						LOG.info(new StringBuilder().append("Created Api prepare to autoConfigure Calendar's API id ")
								.append(apiForm.getApiCredentialId()).append(" with email : ")
								.append(apiForm.getAccountEmail().getValue()).toString());
						final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);

						notificationHelper.addProccessedNotification("zc.api.added");
						final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
						googleHelper.autoConfigureCalendars();

						notificationHelper.addProccessedNotification(
								"zc.meeting.calendar.notification.createdCalendarsConfig",
								apiForm.getAccountEmail().getValue());
					}

				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiCreatedListener;
	}

	private INotificationListener<ApiModifiedNotification> createApiModifiedListener() {
		this.apiModifiedListener = new INotificationListener<ApiModifiedNotification>() {
			@Override
			public void handleNotification(final ApiModifiedNotification notification) {
				try {
					final ApiFormData apiForm = notification.getFormData();
					LOG.info(new StringBuilder().append("Modified Api prepare to autoConfigure Calendar's API id ")
							.append(apiForm.getApiCredentialId()).append(" with email : ")
							.append(apiForm.getAccountEmail().getValue()).toString());
					if (Desktop.this.isMySelf(apiForm.getUserId())) {
						final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);

						notificationHelper.addProccessedNotification("zc.api.modified");
						final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
						googleHelper.autoConfigureCalendars();

						notificationHelper.addProccessedNotification(
								"zc.meeting.calendar.notification.modifiedCalendarsConfig",
								apiForm.getAccountEmail().getValue());
					}

				} catch (final RuntimeException e) {
					LOG.error("Could not handle modified api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiModifiedListener;
	}

	private INotificationListener<ApiDeletedNotification> createApiDeletedListener() {
		this.apiDeletedListener = new INotificationListener<ApiDeletedNotification>() {
			@Override
			public void handleNotification(final ApiDeletedNotification notification) {
				try {
					final ApiFormData eventForm = notification.getFormData();
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("Deleted Api prepare to modify desktop menus (")
								.append(this.getClass().getName()).append(") : ").append(eventForm.getUserId())
								.toString().toString());
					}
					Desktop.this.getMenu(AddGoogleCalendarMenu.class).setVisible(Boolean.TRUE);

				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiDeletedListener;
	}

	private INotificationListener<UserModifiedNotification> createUserModifiedListener() {
		this.userModifiedListener = new INotificationListener<UserModifiedNotification>() {
			@Override
			public void handleNotification(final UserModifiedNotification notification) {
				try {
					final UserFormData userForm = notification.getFormData();
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("User modified prepare to update locale (")
								.append(this.getClass().getName()).append(") : ").append(userForm.getUserId())
								.toString());
					}

					final Locale currentLocale = NlsLocale.get();

					if (null != userForm.getLanguage().getValue() && (null == currentLocale
							|| !currentLocale.getLanguage().equals(userForm.getLanguage().getValue()))) {
						ClientSession.get().setLocale(new Locale(userForm.getLanguage().getValue()));

						// IUiSession.get().storePreferredLocaleInCookie(new
						// Locale(userForm.getLanguage().getValue()));
						Desktop.this.refreshAllPages();
					}

				} catch (final RuntimeException e) {
					LOG.error("Could not handle modified User. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.userModifiedListener;
	}

	@Order(2000)
	public class AddGoogleCalendarMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.addGoogleCalendar");
		}

		@Override
		protected String getConfiguredTooltipText() {
			return TEXTS.get("zc.meeting.addCalendar.tooltips");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet();
		}

		@Override
		protected String getConfiguredIconId() {
			return Icons.Calendar;
		}

		@Override
		protected void execAction() {
			ClientSession.get().getDesktop().openUri("/addGoogleCalendar", OpenUriAction.NEW_WINDOW);
		}

		@Override
		protected void execInitAction() {
			super.execInitAction();
			this.setVisiblePermission(new CreateApiPermission());
			// this.setVisible(!BEANS.get(GoogleApiHelper.class).isCalendarConfigured());
		}
	}

	@Order(3000)
	public class UserMenu extends AbstractMenu {

		@Override
		protected String getConfiguredIconId() {
			return AbstractIcons.Person;
		}

		@Order(1000)
		public class WhoAmIMenuMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				final IUserService userService = BEANS.get(IUserService.class);
				final UserFormData userDetails = userService.getCurrentUserDetails();
				return TEXTS.get("zc.user.loggedAs", userDetails.getEmail().getValue());
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
			}
		}

		@Order(2000)
		public class EditMyAccountMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.edit.account");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Pencil;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
				// AccessControlService = clientSide Access Control Service.
				// getUserIdOfCurrentUser() implemented in super abstract
				// parent.
				final Long currentUserId = ((AccessControlService) BEANS.get(IAccessControlService.class))
						.getZeroClickUserIdOfCurrentSubject();
				final UserForm form = new UserForm();
				form.getUserIdField().setValue(currentUserId);
				form.setEnabledPermission(new UpdateUserPermission(currentUserId));
				form.startModify();
			}
		}

		@Order(3000)
		public class EditCalendarConfigurationsMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.calendar.configuration");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Calendar;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
				final CalendarsConfigurationForm configForm = new CalendarsConfigurationForm();
				configForm.startModify();
			}
		}

		@Order(4000)
		public class EditSlotsConfigMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.meeting.slot.configuration");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
				final SlotsForm slotsFrom = new SlotsForm();
				slotsFrom.startModify();
			}
		}

		@Order(5000)
		public class ContractSeparatorMenu extends AbstractMenuSeparator {
			@Override
			protected String getConfiguredText() {
				return null;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}
		}

		@Order(6000)
		public class ViewCpsMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.subscription.contracts");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
				final IUserService userService = BEANS.get(IUserService.class);
				final UserFormData userData = userService.getCurrentUserDetails();
				final ViewCpsForm viewCpsForm = new ViewCpsForm();
				viewCpsForm.getUserIdField().setValue(userData.getUserId().getValue());
				viewCpsForm.startNew(userData.getSubscriptionBox().getValue());
			}
		}

		// @Order(6000)
		// public class ViewTosMenu extends AbstractMenu {
		// @Override
		// protected String getConfiguredText() {
		// return TEXTS.get("zc.user.role.subscription.tos.label");
		// }
		//
		// @Override
		// protected Set<? extends IMenuType> getConfiguredMenuTypes() {
		// return CollectionUtility.hashSet();
		// }
		//
		// @Override
		// protected void execAction() {
		// final IAppParamsService appParamService =
		// BEANS.get(IAppParamsService.class);
		// final String url =
		// appParamService.getValue(IAppParamsService.APP_PARAM_KEY_TOS_URL);
		//
		// if (null == url || url.isEmpty()) {
		// throw new VetoException("No Term Of Use to display");
		// }
		// ClientSession.get().getDesktop().openUri(url,
		// OpenUriAction.NEW_WINDOW);
		// }
		// }

		@Order(7000)
		public class LogoutMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.logout");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet();
			}

			@Override
			protected void execAction() {
				if (ClientSession.get().isStopping()) {
					LOG.warn("Logout already in progress");
				} else {
					ClientSession.get().stop();
				}
			}
		}
	}

	// @Order(4000)
	// public class AppLogoMenu extends AbstractMenu {
	// @Override
	// protected String getConfiguredText() {
	// return "";
	// }
	//
	// @Override
	// protected String getConfiguredIconId() {
	// return Icons.APP_LOGO;
	// }
	//
	// @Override
	// protected Set<? extends IMenuType> getConfiguredMenuTypes() {
	// return CollectionUtility.hashSet();
	// }
	//
	// @Override
	// protected void execAction() {
	// }
	// }

	@Order(1000)
	public class MeetingOutlineViewButton extends AbstractOutlineViewButton {

		public MeetingOutlineViewButton() {
			this(MeetingOutline.class);
		}

		protected MeetingOutlineViewButton(final Class<? extends MeetingOutline> outlineClass) {
			super(Desktop.this, outlineClass);
		}

		@Override
		protected String getConfiguredKeyStroke() {
			return IKeyStroke.F2;
		}

		@Override
		protected void execInitAction() {
			super.execInitAction();
			this.setVisibleGranted(ACCESS.getLevel(new ReadApiPermission((Long) null)) >= ReadApiPermission.LEVEL_OWN);
		}
	}

	@Order(3000)
	public class ConfigurationOutlineViewButton extends AbstractOutlineViewButton {

		public ConfigurationOutlineViewButton() {
			this(ConfigurationOutline.class);
		}

		protected ConfigurationOutlineViewButton(final Class<? extends ConfigurationOutline> outlineClass) {
			super(Desktop.this, outlineClass);
		}

		@Override
		protected DisplayStyle getConfiguredDisplayStyle() {
			return DisplayStyle.MENU;
		}

		@Override
		protected String getConfiguredKeyStroke() {
			return IKeyStroke.F4;
		}

		@Override
		protected void execInitAction() {
			super.execInitAction();
			this.setVisibleGranted(
					ACCESS.getLevel(new ReadEventPermission((Long) null)) >= ReadUserPermission.LEVEL_OWN);
		}
	}

	@Order(10000)
	public class AdministrationOutlineViewButton extends AbstractOutlineViewButton {

		public AdministrationOutlineViewButton() {
			this(AdministrationOutline.class);
		}

		protected AdministrationOutlineViewButton(final Class<? extends AdministrationOutline> outlineClass) {
			super(Desktop.this, outlineClass);
		}

		@Override
		protected DisplayStyle getConfiguredDisplayStyle() {
			return DisplayStyle.MENU;
		}

		@Override
		protected String getConfiguredKeyStroke() {
			return IKeyStroke.F8;
		}

		@Override
		protected void execInitAction() {
			super.execInitAction();
			if (ACCESS.check(new ReadRolePermission()) || ACCESS.check(new ReadPermissionPermission())
					|| ACCESS.getLevel(new ReadUserPermission((Long) null)) >= ReadUserPermission.LEVEL_OWN) {
				this.setEnabledGranted(Boolean.TRUE);
				this.setVisible(Boolean.TRUE);
			} else {
				this.setEnabledGranted(Boolean.FALSE);
				this.setVisible(Boolean.FALSE);
			}
		}
	}

}
