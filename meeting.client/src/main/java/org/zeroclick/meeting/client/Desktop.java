package org.zeroclick.meeting.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
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
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.client.ConfigurationOutline;
import org.zeroclick.configuration.client.administration.AdministrationOutline;
import org.zeroclick.configuration.client.api.ApiCreatedNotificationHandler;
import org.zeroclick.configuration.client.api.ApiDeletedNotificationHandler;
import org.zeroclick.configuration.client.user.UserForm;
import org.zeroclick.configuration.client.user.UserModifiedNotificationHandler;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.role.ReadPermissionPermission;
import org.zeroclick.configuration.shared.role.ReadRolePermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.client.meeting.MeetingOutline;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CreateApiPermission;
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

		final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
				.get(ApiCreatedNotificationHandler.class);
		apiCreatedNotificationHandler.addListener(this.createApiCreatedListener());

		final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
				.get(ApiDeletedNotificationHandler.class);
		apiDeletedNotificationHandler.addListener(this.createApiDeletedListener());

		final UserModifiedNotificationHandler userModifiedNotificationHandler = BEANS
				.get(UserModifiedNotificationHandler.class);
		userModifiedNotificationHandler.addListener(this.createUserModifiedListener());

		this.checkAndUpdateRequiredDatas();
	}

	private void refreshAllPages() {
		for (final IOutline outline : this.getAvailableOutlines()) {
			outline.refreshPages(IPage.class);
		}
	}

	private void checkAndUpdateRequiredDatas() {
		// default user timezone
		final IUserService userService = BEANS.get(IUserService.class);
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();
		final String currentUserTimeZone = userService.getUserTimeZone(currentUserId);
		if (null == currentUserTimeZone || !GoogleApiHelper.get().isCalendarConfigured()) {
			final OnBoardingUserForm form = new OnBoardingUserForm();
			form.getUserIdField().setValue(currentUserId);
			form.setEnabledPermission(new UpdateUserPermission(currentUserId));
			form.startModify();
		}
	}

	@Override
	protected void execClosing() {
		final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
				.get(ApiCreatedNotificationHandler.class);
		apiCreatedNotificationHandler.removeListener(this.apiCreatedListener);

		final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
				.get(ApiDeletedNotificationHandler.class);
		apiDeletedNotificationHandler.removeListener(this.apiDeletedListener);

		final UserModifiedNotificationHandler userModifiedNotificationHandler = BEANS
				.get(UserModifiedNotificationHandler.class);
		userModifiedNotificationHandler.removeListener(this.userModifiedListener);
	}

	protected void selectFirstVisibleOutline() {
		for (final IOutline outline : this.getAvailableOutlines()) {
			if (outline.isEnabled() && outline.isVisible()) {
				this.setOutline(outline);
				break;
			}
		}
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
				LOG.debug("Adding desktop notification : " + status.getMessage());
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
					final ApiFormData eventForm = notification.getApiForm();
					LOG.debug("Created Api prepare to modify desktop menus (" + this.getClass().getName() + ") : "
							+ eventForm.getUserId());
					Desktop.this.getMenu(AddGoogleCalendarMenu.class).setVisible(Boolean.FALSE);
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
					LOG.debug("Deleted Api prepare to modify desktop menus (" + this.getClass().getName() + ") : "
							+ eventForm.getUserId());
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
					final UserFormData userForm = notification.getUserForm();
					LOG.debug("User modified prepare to update locale (" + this.getClass().getName() + ") : "
							+ userForm.getUserId());

					final Locale currentLocale = NlsLocale.get();

					if (null == currentLocale
							|| !currentLocale.getLanguage().equals(userForm.getLanguage().getValue())) {
						ClientSession.get().setLocale(new Locale(userForm.getLanguage().getValue()));
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
			this.setVisible(!GoogleApiHelper.get().isCalendarConfigured());
		}
	}

	@Order(3000)
	public class UserMenu extends AbstractMenu {

		@Override
		protected String getConfiguredIconId() {
			return AbstractIcons.Person;
		}

		@Order(31000)
		public class WhoAmIMenuMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				final IUserService userService = BEANS.get(IUserService.class);
				final UserFormData userDetails = userService.getCurrentUserDetails();
				return TEXTS.get("zc.user.logedAs", userDetails.getEmail().getValue());
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

		@Order(32000)
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

		@Order(33000)
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
