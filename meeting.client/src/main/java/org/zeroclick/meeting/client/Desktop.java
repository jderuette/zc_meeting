package org.zeroclick.meeting.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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
import org.zeroclick.configuration.client.user.UserForm;
import org.zeroclick.configuration.onboarding.OnBoardingUserForm;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.role.ReadPermissionPermission;
import org.zeroclick.configuration.shared.role.ReadRolePermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
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

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.common.applicationTitle");
	}

	@Override
	protected String getConfiguredLogoId() {
		return null;
		// return Icons.AppLogo;
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

		this.checkAndUpdateRequiredDatas();
	}

	private void checkAndUpdateRequiredDatas() {
		// default user timezone
		final IUserService userService = BEANS.get(IUserService.class);
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();
		final String currentUserTimeZone = userService.getUserTimeZone(currentUserId);
		if (null == currentUserTimeZone) {
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
		apiCreatedNotificationHandler.removeListener(this.createApiCreatedListener());
	}

	protected void selectFirstVisibleOutline() {
		for (final IOutline outline : this.getAvailableOutlines()) {
			if (outline.isEnabled() && outline.isVisible()) {
				this.setOutline(outline);
				break;
			}
		}
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
