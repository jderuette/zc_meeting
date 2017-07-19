package org.zeroclick.configuration.server.user;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.configuration.shared.user.UserTablePageData;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class UserService implements IUserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	protected static final Charset CHARSET = StandardCharsets.UTF_16;

	public static final Long[] DEFAULT_ROLES_VALUES = new Long[] { 2l };
	public static final Set<Long> DEFAULT_ROLES = new HashSet<>(Arrays.asList(DEFAULT_ROLES_VALUES));

	@Override
	public UserTablePageData getUserTableData(final SearchFilter filter) {
		final UserTablePageData pageData = new UserTablePageData();
		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (ACCESS.getLevel(new ReadUserPermission((Long) null)) != ReadUserPermission.LEVEL_ALL) {
			ownerFilter = SQLs.USER_SELECT_FILTER_ID;
			final AccessControlService acs = BEANS.get(AccessControlService.class);
			currentConnectedUserId = acs.getZeroClickUserIdOfCurrentSubject();
		}

		final String sql = SQLs.USER_PAGE_SELECT + ownerFilter + SQLs.USER_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public UserFormData prepareCreate(final UserFormData formData) {
		if (!ACCESS.check(new CreateUserPermission())) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		LOG.debug("PrepareCreate for User");
		return formData;
	}

	@Override
	public UserFormData create(final UserFormData formData) {
		if (formData.getAutofilled()) {
			// TODO Djer13 create specific create level (5) for autofilled
			// created user ?
			LOG.info("Create user autoFilled enabled reseting advanced value (role forced to 'Standard')");
			formData.getRolesBox().setValue(DEFAULT_ROLES);
		} else {
			if (!ACCESS.check(new CreateUserPermission())) {
				throw new VetoException(TEXTS.get("AuthorizationFailed"));
			}
		}

		if (null == formData.getUserId().getValue()) {
			formData.getUserId().setValue(SQL.getSequenceNextval("USER_ID_SEQ"));
		}

		if (null != formData.getLogin().getValue()) {
			formData.getLogin().setValue(formData.getLogin().getValue().toLowerCase());
		}
		if (null != formData.getEmail().getValue()) {
			formData.getEmail().setValue(formData.getEmail().getValue().toLowerCase());
		}

		LOG.info("Create User with Id :" + formData.getUserId() + ", Login : " + formData.getLogin().getValue()
				+ " and email : " + formData.getEmail());

		if (null != formData.getLogin().getValue() && this.userAlreadyExists(formData.getLogin().getValue())) {
			LOG.error("Trying to create User with an existing login (lowercase match) : "
					+ formData.getLogin().getValue());
			throw new VetoException(TEXTS.get("zc.user.userAlreadyExists"));
		}

		if (null != formData.getEmail().getValue() && this.isEmailAlreadyUsed(formData.getEmail().getValue())) {
			LOG.error("Trying to create User with an existing email (lowercase match) : "
					+ formData.getEmail().getValue());
			throw new VetoException(TEXTS.get("zc.user.userAlreadyExists"));
		}
		SQL.insert(SQLs.USER_INSERT, formData);
		return this.store(formData, Boolean.TRUE);
	}

	private boolean userAlreadyExists(final String userLogin) {
		LOG.info("Checking it login : " + userLogin + " already Exists");
		final UserFormData input = new UserFormData();
		input.getLogin().setValue(userLogin.toLowerCase());

		final UserFormData existingUser = this.load(input);
		return null != existingUser.getUserId().getValue();
	}

	@Override
	public UserFormData load(final UserFormData formData) {
		if (!ACCESS.check(new ReadUserPermission(formData.getUserId().getValue()))) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		LOG.debug("Load User with Id :" + formData.getUserId().getValue() + " and email : "
				+ formData.getEmail().getValue() + " (login : " + formData.getLogin().getValue() + ")");

		Long currentSelectedUserId = formData.getUserId().getValue();
		if (ACCESS.getLevel(new ReadUserPermission((Long) null)) != ReadUserPermission.LEVEL_ALL) {
			// if not allowed to read ALL User, force currentUser only

			final AccessControlService acs = BEANS.get(AccessControlService.class);
			final Long currenUserId = acs.getZeroClickUserIdOfCurrentSubject();

			if (!currenUserId.equals(currentSelectedUserId)) {
				LOG.warn("User : " + currentSelectedUserId + " not allowed to view : " + currentSelectedUserId
						+ " forcing to userId to " + currenUserId);
				currentSelectedUserId = currenUserId;
				formData.getUserId().setValue(currentSelectedUserId);
			}
		}
		SQL.selectInto(SQLs.USER_SELECT + SQLs.USER_SELECT_FILTER_ID + SQLs.USER_SELECT_INTO, formData,
				new NVPair("currentUser", currentSelectedUserId));

		this.loadRoles(formData, currentSelectedUserId);

		return formData;
	}

	@Override
	public OnBoardingUserFormData load(final OnBoardingUserFormData formData) {
		UserFormData userFormData = new UserFormData();
		userFormData.getUserId().setValue(formData.getUserId().getValue());

		userFormData = this.load(userFormData);

		formData.getLogin().setValue(userFormData.getLogin().getValue());
		formData.getTimeZone().setValue(userFormData.getTimeZone().getValue());

		return formData;
	}

	private void loadRoles(final UserFormData formData, final Long currentSelectedUserId) {
		final UserFormData formDataRoles = new UserFormData();
		SQL.selectInto(SQLs.USER_ROLE_SELECT_ROLES + SQLs.USER_ROLE_SELECT_FILTER_USER + SQLs.USER_SELECT_INTO_ROLE,
				formDataRoles, new NVPair("currentUser", currentSelectedUserId));

		formData.getRolesBox().setValue(formDataRoles.getRolesBox().getValue());
	}

	private UserFormData loadByEmail(final UserFormData formData) {
		// No permission check right now, will be done by standard "load" method
		// when the userId of this email will be retrieved
		LOG.debug("Searching userId with email : " + formData.getEmail().getValue());
		SQL.selectInto(SQLs.USER_SELECT + SQLs.USER_SELECT_FILTER_EMAIL + SQLs.USER_SELECT_INTO, formData);
		this.loadRoles(formData, formData.getUserId().getValue());
		return formData;
	}

	private UserFormData loadByLogin(final UserFormData formData) {
		// No permission check right now, will be done by standard "load" method
		// when the userId of this email will be retrieved
		LOG.debug("Searching userId with login : " + formData.getLogin().getValue());
		SQL.selectInto(SQLs.USER_SELECT + SQLs.USER_SELECT_FILTER_LOGIN + SQLs.USER_SELECT_INTO, formData);
		this.loadRoles(formData, formData.getUserId().getValue());
		return formData;
	}

	protected UserFormData store(final UserFormData formData, final Boolean isCreation) {
		if (!isCreation && !ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		LOG.debug("Store User with Id :" + formData.getUserId().getValue() + " and email : "
				+ formData.getEmail().getValue() + " (Login : " + formData.getLogin().getValue() + ")");

		SQL.update(SQLs.USER_UPDATE, formData);

		if (null != formData.getHashedPassword()) {
			SQL.update(SQLs.USER_UPDATE_PASSWORD, formData);
		}

		// Add and Remove UserRoles

		SQL.update(SQLs.USER_ROLE_REMOVE_ALL, formData);
		SQL.update(SQLs.USER_ROLE_INSERT, formData);

		this.sendModifiedNotifications(formData);

		return formData;
	}

	@Override
	public UserFormData store(final UserFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	@Override
	public OnBoardingUserFormData store(final OnBoardingUserFormData formData) {
		if (!ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			throw new VetoException(TEXTS.get("AuthorizationFailed"));
		}
		LOG.debug("Store OnBoarding User datas with Id :" + formData.getUserId().getValue() + " (Login : "
				+ formData.getLogin().getValue() + ")");

		SQL.update(SQLs.USER_UPDATE_ONBOARDING, formData);

		UserFormData userFormForNotifications = new UserFormData();

		userFormForNotifications.getUserId().setValue(formData.getUserId().getValue());
		userFormForNotifications = this.load(userFormForNotifications);
		this.sendModifiedNotifications(userFormForNotifications);

		return formData;
	}

	private Set<String> buildNotifiedUsers(final UserFormData formData) {
		// Notify Users for UserUpdate update

		final Set<String> notifiedUsers = new HashSet<>();
		if (null != formData.getUserId().getValue()) {
			notifiedUsers.addAll(this.getUserNotificationIds(formData.getUserId().getValue()));
		}
		return notifiedUsers;
	}

	private void sendModifiedNotifications(final UserFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers, new UserModifiedNotification(formData));
	}

	@Override
	public boolean isOwn(final Long userId) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();

		if (null == userId) {
			LOG.error("Cannot check currentUser own an empty or null UserId");
			return false;
		} else if (userId.equals(currentUserId)) {
			return true;
		}

		return false;
	}

	@Override
	public UserFormData getCurrentUserDetails() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		final UserFormData formData = new UserFormData();
		formData.getUserId().setValue(acs.getZeroClickUserIdOfCurrentSubject());
		return this.load(formData);
	}

	@Override
	public String getUserTimeZone(final Long userId) {
		// No permission check to allow guest get timeZone of hosts
		final UserFormData formData = new UserFormData();
		formData.getUserId().setValue(userId);

		SQL.selectInto(SQLs.USER_SELECT_TIME_ZONE + SQLs.USER_SELECT_FILTER_ID + SQLs.USER_SELECT_INTO_TIME_ZONE,
				formData, new NVPair("currentUser", userId));

		return formData.getTimeZone().getValue();
	}

	@Override
	public Set<String> getUserNotificationIds(final Long userId) {
		final Set<String> notificationsIds = new HashSet<>();

		final Object[][] datas = SQL.select(SQLs.USER_SELECT_NOTIFICATION_IDS + SQLs.USER_SELECT_FILTER_ID,
				new NVPair("currentUser", userId));

		for (int row = 0; row < datas.length; row++) {
			for (int col = 0; col < datas[row].length; col++) {
				notificationsIds.add((String) datas[row][col]);
			}
		}

		return notificationsIds;
	}

	@Override
	public UserFormData getPassword(final String username) {
		LOG.debug("Retriving password for User " + username);
		final UserFormData formData = this.loadPassword(username);

		return formData;
	}

	@Override
	public UserFormData getPassword(final UserFormData userPassFormData) {
		LOG.debug("Retriving password for User " + userPassFormData.getLogin().getValue());
		final UserFormData formData = this.loadPassword(userPassFormData.getLogin().getValue());
		return formData;
	}

	private UserFormData loadPassword(final String username) {
		LOG.debug("Retriving password for User " + username);
		UserFormData formData = new UserFormData();
		formData.getLogin().setValue(username.toLowerCase());
		SQL.select(SQLs.USER_SELECT_PASSWORD_FILTER_LOGIN, formData);

		if (null == formData.getPassword().getValue()) {
			LOG.debug("No saved password with Login for user : " + username + ". Trying with email");
			formData = this.loadPasswordByEmail(username);
		}

		return formData;
	}

	private UserFormData loadPasswordByEmail(final String email) {
		LOG.debug("Retriving password with email " + email);
		final UserFormData formData = new UserFormData();
		formData.getEmail().setValue(email);
		SQL.select(SQLs.USER_SELECT_PASSWORD_FILTER_EMAIL, formData);

		if (null == formData.getPassword().getValue()) {
			LOG.debug("No saved password with email for user : " + email);
		}
		return formData;
	}

	@Override
	public Long getUserId(final String loginOrEmail) {
		final UserFormData formData = new UserFormData();
		formData.getEmail().setValue(loginOrEmail.toLowerCase());
		this.loadByEmail(formData);
		if (null == formData.getUserId().getValue()) {
			formData.getLogin().setValue(loginOrEmail.toLowerCase());
			this.loadByLogin(formData);
		}

		return formData.getUserId().getValue();
	}

	@Override
	public Long getUserIdByEmail(final String email) {
		// TODO Djer13 add permission Check ? Warning : may be called by an
		// event Organizer to get the guest UserId (by Email)
		final UserFormData formData = new UserFormData();
		formData.getEmail().setValue(email.toLowerCase());
		this.loadByEmail(formData);
		return formData.getUserId().getValue();
	}

	@Override
	public boolean isEmailAlreadyUsed(final String email) {
		// Check for email AND login to avoid allowing user connect to connect
		// with email witch is email field of an other user and therefore
		// blocking the other user
		return null != this.getUserId(email);
	}

	@Override
	public boolean isLoginAlreadyUsed(final String login) {
		// Check for email AND login to avoid allowing user connect to connect
		// with IS login (with @) (using email address of an other) and
		// therefore blocking the other user
		return null != this.getUserId(login);
	}

}
