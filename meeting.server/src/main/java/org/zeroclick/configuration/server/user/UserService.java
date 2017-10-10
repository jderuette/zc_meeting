package org.zeroclick.configuration.server.user;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import org.zeroclick.common.CommonService;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.common.document.DocumentFormData.LinkedRole.LinkedRoleRowData;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.role.CpsAcceptedNotification;
import org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.UpdateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.configuration.shared.user.UserTablePageData;
import org.zeroclick.configuration.shared.user.ValidateCpsFormData;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class UserService extends CommonService implements IUserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	protected static final Charset CHARSET = StandardCharsets.UTF_16;

	public static final Long[] DEFAULT_ROLES_VALUES = new Long[] { 2l };
	public static final Set<Long> DEFAULT_ROLES = new HashSet<>(Arrays.asList(DEFAULT_ROLES_VALUES));
	public static final Long DEFAULT_SUBSCRIPTION = 3l;;

	@Override
	public UserTablePageData getUserTableData(final SearchFilter filter) {
		final UserTablePageData pageData = new UserTablePageData();
		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (ACCESS.getLevel(new ReadUserPermission((Long) null)) != ReadUserPermission.LEVEL_ALL) {
			ownerFilter = SQLs.USER_SELECT_FILTER_ID;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.USER_PAGE_SELECT + ownerFilter + SQLs.USER_PAGE_DATA_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		return pageData;
	}

	@Override
	public UserFormData prepareCreate(final UserFormData formData) {
		if (!ACCESS.check(new CreateUserPermission())) {
			super.throwAuthorizationFailed();
		}
		LOG.debug("PrepareCreate for User");
		return formData;
	}

	@Override
	public UserFormData create(final UserFormData formData) {
		if (formData.getAutofilled()) {
			// TODO Djer13 create specific create level (5) for autofilled
			// created user ?
			LOG.info(
					"Create user autoFilled enabled reseting advanced value (role forced to 'Standard', subscription to 'free')");
			formData.getRolesBox().setValue(DEFAULT_ROLES);
			formData.getSubscriptionBox().setValue(DEFAULT_SUBSCRIPTION);

			formData.setInvitedBy(super.userHelper.getCurrentUserId());
		} else {
			if (!ACCESS.check(new CreateUserPermission())) {
				super.throwAuthorizationFailed();
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
		final UserFormData userCreatedData = this.store(formData, Boolean.TRUE);

		// add default slot configuration for the new User
		final ISlotService slotService = BEANS.get(ISlotService.class);
		slotService.createDefaultSlot(userCreatedData.getUserId().getValue());

		return userCreatedData;
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
			super.throwAuthorizationFailed();
		}
		LOG.debug("Load User with Id :" + formData.getUserId().getValue() + " and email : "
				+ formData.getEmail().getValue() + " (login : " + formData.getLogin().getValue() + ")");

		Long currentSelectedUserId = formData.getUserId().getValue();
		if (ACCESS.getLevel(new ReadUserPermission((Long) null)) != ReadUserPermission.LEVEL_ALL) {
			// if not allowed to read ALL User, force currentUser only

			final Long currenUserId = super.userHelper.getCurrentUserId();

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
		this.loadCurrentSubscription(formData, currentSelectedUserId);
		this.loadSubscriptionsDetails(formData, currentSelectedUserId);

		formData.setActiveSubscriptionValid(this.isActiveSubscriptionValid(currentSelectedUserId, Boolean.FALSE));

		return formData;
	}

	@Override
	public ValidateCpsFormData load(final ValidateCpsFormData formData) {
		if (!ACCESS.check(new ReadAssignSubscriptionToUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}

		// get THIS subscription details
		SQL.selectInto(SQLs.USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS + SQLs.USER_ROLE_FILTER_USER_ID
				+ SQLs.USER_ROLE_FILTER_SUBSCRIPTION_ID + SQLs.USER_ROLE_FILTER_START_DATE
				+ SQLs.USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS_INTO_CPS, formData);

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
		final UserFormData formDataRoles = this.getRoles(currentSelectedUserId);
		formData.getRolesBox().setValue(formDataRoles.getRolesBox().getValue());
	}

	private UserFormData getRoles(final Long userId) {
		return this.getRoles(userId, Boolean.FALSE);
	}

	/**
	 * On initial load of user (during authentication) permission cannot be
	 * checked, because we need to gather those permissions....
	 *
	 * @param userId
	 * @param checkcheckAccess
	 * @return
	 */
	private UserFormData getRoles(final Long userId, final Boolean checkcheckAccess) {
		if (checkcheckAccess && !ACCESS.check(new ReadUserPermission(userId))) {
			this.throwAuthorizationFailed();
		}
		final UserFormData formDataRoles = new UserFormData();
		formDataRoles.getUserId().setValue(userId);
		SQL.selectInto(SQLs.USER_ROLE_SELECT_ROLE_ID + SQLs.USER_ROLE_SELECT_FILTER_USER + SQLs.USER_SELECT_INTO_ROLES,
				formDataRoles);
		return formDataRoles;
	}

	private void loadCurrentSubscription(final UserFormData formData, final Long currentSelectedUserId) {
		final UserFormData formDataRoles = this.getCurrentSubscription(currentSelectedUserId);
		formData.getSubscriptionBox().setValue(formDataRoles.getSubscriptionBox().getValue());
	}

	private UserFormData getCurrentSubscription(final Long userId) {
		return this.getSubscription(userId, Boolean.FALSE);
	}

	private UserFormData getSubscription(final Long userId, final Boolean checkAccess) {
		if (checkAccess && !ACCESS.check(new ReadUserPermission(userId))) {
			this.throwAuthorizationFailed();
		}
		final UserFormData formDataSubscription = new UserFormData();
		formDataSubscription.getUserId().setValue(userId);
		SQL.selectInto(SQLs.USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION + SQLs.USER_SELECT_INTO_SUBSCRIPTION,
				formDataSubscription);
		return formDataSubscription;
	}

	private Object[][] getActiveSubscriptionDetails(final Long userId, final Boolean checkAccess) {
		if (checkAccess && !ACCESS.check(new ReadUserPermission(userId))) {
			this.throwAuthorizationFailed();
		}
		final UserFormData formDataSubscription = new UserFormData();
		formDataSubscription.getUserId().setValue(userId);
		final Object[][] datas = SQL.select(SQLs.USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_DETAILS, formDataSubscription);
		return datas;
	}

	private Boolean isActiveSubscriptionValid(final Long userId, final Boolean checkAccess) {
		final Object[][] activeSubscriptiondatas = this.getActiveSubscriptionDetails(userId, checkAccess);
		Boolean subscriptionValid = Boolean.FALSE;
		if (null != activeSubscriptiondatas && activeSubscriptiondatas.length > 0) {
			final Long subscriptionId = (Long) activeSubscriptiondatas[0][0];

			final IRoleService roleService = BEANS.get(IRoleService.class);
			final DocumentFormData activeDocument = roleService.getActiveDocument(subscriptionId);

			final Long documentId = activeDocument.getDocumentId().getValue();
			LinkedRoleRowData documentRoleData = null;
			if (null != documentId) {
				documentRoleData = roleService.getDocumentMetaData(subscriptionId, documentId);
			}

			// A document exists for the current subscription and has a start
			// (applicable) date
			if (null != documentRoleData && null != documentRoleData.getStartDate()) {
				final Object acceptedCpsDateData = activeSubscriptiondatas[0][3];
				final Object acceptedWithdrawDateData = activeSubscriptiondatas[0][4];

				Date acceptedCpsDate = null;
				if (null != acceptedCpsDateData) {
					acceptedCpsDate = (Date) acceptedCpsDateData;
				}

				Date accepteWithdrawDate = null;
				if (null != acceptedWithdrawDateData) {
					accepteWithdrawDate = (Date) acceptedWithdrawDateData;
				}

				final Boolean acceptedCPSDateValid = null != acceptedCpsDate
						&& !acceptedCpsDate.after(documentRoleData.getStartDate());
				final Boolean accepteWithdrawDateValid = null != accepteWithdrawDate
						&& !accepteWithdrawDate.after(documentRoleData.getStartDate());

				if (subscriptionId == 3l) { // free
					// only CPS
					subscriptionValid = acceptedCPSDateValid;
				} else {
					subscriptionValid = acceptedCPSDateValid && accepteWithdrawDateValid;
				}
			}
		}

		return subscriptionValid;

	}

	private void loadSubscriptionsDetails(final UserFormData formData, final Long currentSelectedUserId) {
		final UserFormData formDataSubscriptionsDetails = this.getSubscriptionsDetails(currentSelectedUserId,
				Boolean.FALSE);
		formData.getSubscriptionsListTable()
				.setRows(formDataSubscriptionsDetails.getSubscriptionsListTable().getRows());
	}

	private UserFormData getSubscriptionsDetails(final Long userId, final Boolean checkAccess) {
		if (checkAccess && !ACCESS.check(new ReadUserPermission(userId))) {
			this.throwAuthorizationFailed();
		}
		final UserFormData formDataSubscriptions = new UserFormData();
		formDataSubscriptions.getUserId().setValue(userId);
		SQL.selectInto(
				SQLs.USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS + SQLs.USER_ROLE_SELECT_FILTER_USER
						+ SQLs.USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS_INTO,
				new NVPair("subscriptionDetails", formDataSubscriptions.getSubscriptionsListTable()),
				new NVPair("userId", userId));
		return formDataSubscriptions;
	}

	private UserFormData loadUserIdByEmail(final UserFormData formData) {
		// No permission check right now, will be done by standard "load" method
		// when the userId of this email will be retrieved
		LOG.debug("Searching userId with email : " + formData.getEmail().getValue());
		SQL.selectInto(SQLs.USER_SELECT_ID_ONLY + SQLs.USER_SELECT_FILTER_EMAIL + SQLs.USER_SELECT_INTO_ID_ONLY,
				formData);
		return formData;
	}

	private UserFormData loadUserIdByLogin(final UserFormData formData) {
		// No permission check right now, will be done by standard "load" method
		// when the userId of this email will be retrieved
		LOG.debug("Searching userId with login : " + formData.getLogin().getValue());
		SQL.selectInto(SQLs.USER_SELECT_ID_ONLY + SQLs.USER_SELECT_FILTER_LOGIN + SQLs.USER_SELECT_INTO_ID_ONLY,
				formData);
		return formData;
	}

	protected UserFormData store(final UserFormData formData, final Boolean isCreation) {
		if (!isCreation && !ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		LOG.debug("Store User with Id :" + formData.getUserId().getValue() + " and email : "
				+ formData.getEmail().getValue() + " (Login : " + formData.getLogin().getValue() + ")");

		SQL.update(SQLs.USER_UPDATE, formData);

		if (null != formData.getHashedPassword()) {
			SQL.update(SQLs.USER_UPDATE_PASSWORD, formData);
		}

		this.updatesRoles(formData);
		// Usefull only for admin, dor other user store(ValidateCpsForm) already
		// do the update
		this.updateSubscriptions(formData);

		// reset permission cache

		this.sendModifiedNotifications(formData);

		return formData;
	}

	/**
	 * Add and remove roles
	 *
	 * @param formData
	 */
	private void updatesRoles(final UserFormData formData) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final UserFormData existingUserRoles = this.getRoles(formData.getUserId().getValue(), Boolean.TRUE);

		final Set<Long> addedRoles = this.getItemsAdded(existingUserRoles.getRolesBox().getValue(),
				formData.getRolesBox().getValue());
		final Set<Long> removedRoles = this.getItemsRemoved(existingUserRoles.getRolesBox().getValue(),
				formData.getRolesBox().getValue());

		// TODO Djer13 : for "standard" roles history is lost. Use something
		// similar to Subscriptions ? (How to add a "role" which discard the old
		// one ?)
		if (!addedRoles.isEmpty()) {
			LOG.info("Adding new roles : " + addedRoles + " for User " + formData.getUserId().getValue());
			SQL.update(SQLs.USER_ROLE_INSERT, new NVPair("userId", formData.getUserId().getValue()),
					new NVPair("rolesBox", addedRoles));
			acs.clearUserCache(this.buildNotifiedUsers(formData));
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No new roles to add for user " + formData.getUserId().getValue());
			}
		}
		if (!removedRoles.isEmpty()) {
			LOG.info("Removing roles : " + removedRoles + " for User " + formData.getUserId().getValue());
			SQL.update(SQLs.USER_ROLE_REMOVE, new NVPair("userId", formData.getUserId().getValue()),
					new NVPair("rolesBox", removedRoles));
			acs.clearUserCache(this.buildNotifiedUsers(formData));
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No roles to remove for user " + formData.getUserId().getValue());
			}
		}
	}

	private void updateSubscriptions(final UserFormData formData) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final UserFormData currentSubscriptionData = this.getSubscription(formData.getUserId().getValue(),
				Boolean.TRUE);
		final Long currentSubscription = currentSubscriptionData.getSubscriptionBox().getValue();
		final Long newSubscription = formData.getSubscriptionBox().getValue();

		if (!currentSubscription.equals(newSubscription)) {
			// Only add the new one, the oldest will be discarded as a more
			// recent exists
			LOG.info("Siwtching user subscription from  : " + currentSubscription + " to " + newSubscription
					+ " for User " + formData.getUserId().getValue());
			this.addSubscriptionAsynch(formData.getUserId().getValue(), newSubscription);

			acs.clearUserCache(this.buildNotifiedUsers(formData));
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No subscription modification detected for user " + formData.getUserId().getValue());
			}
		}
	}

	private void addSubscriptionAsynch(final Long userId, final Long subscriptionId) {
		final Date startDate = new Date();
		SQL.update(SQLs.USER_ROLE_INSERT_WITH_START_DATE, new NVPair("userId", userId),
				new NVPair("rolesBox", subscriptionId), new NVPair("startDate", startDate));

		// empty subscription metadata
		final ValidateCpsFormData validateCpsFakeFormData = new ValidateCpsFormData();
		validateCpsFakeFormData.getUserId().setValue(userId);
		validateCpsFakeFormData.getSubscriptionId().setValue(subscriptionId);
		validateCpsFakeFormData.getStartDate().setValue(startDate);
		validateCpsFakeFormData.getAcceptedCpsDate().setValue(null);
		validateCpsFakeFormData.getAcceptedWithdrawalDate().setValue(null);

		SQL.update(SQLs.USER_ROLE_UPDATE_CPS, validateCpsFakeFormData);
	}

	private Set<Long> getItemsAdded(final Set<Long> existingItems, final Set<Long> newItems) {
		final Set<Long> itemsAdded = new HashSet<>();
		if (null != newItems) {
			itemsAdded.addAll(newItems);
			itemsAdded.removeAll(existingItems);
		}
		return itemsAdded;
	}

	private Set<Long> getItemsRemoved(final Set<Long> existingItems, final Set<Long> newItems) {
		final Set<Long> itemsRemoved = new HashSet<>();
		if (null != existingItems) {
			itemsRemoved.addAll(existingItems);
			itemsRemoved.removeAll(newItems);
		}
		return itemsRemoved;
	}

	@Override
	public UserFormData store(final UserFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	@Override
	public OnBoardingUserFormData store(final OnBoardingUserFormData formData) {
		if (!ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		LOG.debug("Store OnBoarding User datas with Id :" + formData.getUserId().getValue() + " (Login : "
				+ formData.getLogin().getValue() + ")");

		/**
		 * Collect user data BEFORE update, to allow login/email modifications
		 * if done after update "isOwn" on load will fail because, no User row
		 * will have the correct "subject"
		 **/
		UserFormData userFormForNotifications = new UserFormData();
		userFormForNotifications.getUserId().setValue(formData.getUserId().getValue());
		userFormForNotifications = this.load(userFormForNotifications);

		SQL.update(SQLs.USER_UPDATE_ONBOARDING, formData);

		this.sendModifiedNotifications(userFormForNotifications);

		return formData;
	}

	@Override
	public ValidateCpsFormData create(final ValidateCpsFormData formData) {
		if (!ACCESS.check(new CreateAssignSubscriptionToUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}

		SQL.update(SQLs.USER_ROLE_INSERT_WITH_START_DATE, new NVPair("userId", formData.getUserId().getValue()),
				new NVPair("rolesBox", formData.getSubscriptionId().getValue()),
				new NVPair("startDate", formData.getStartDate().getValue()));

		SQL.insert(SQLs.SUBSCRIPTION_INSERT, formData);

		this.store(formData);

		return formData;
	}

	@Override
	public ValidateCpsFormData store(final ValidateCpsFormData formData) {
		if (!ACCESS.check(new UpdateAssignSubscriptionToUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		LOG.debug("Store CPS User datas for user Id :" + formData.getUserId().getValue()
				+ " for subscription (role) id : " + formData.getSubscriptionId().getValue());

		SQL.update(SQLs.USER_ROLE_UPDATE_CPS, formData);

		acs.clearUserCache(this.buildNotifiedUsers(formData));

		this.sendModifiedNotifications(formData);

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

	private Set<String> buildNotifiedUsers(final ValidateCpsFormData formData) {
		// Notify CPS for UserUpdate update

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

	private void sendModifiedNotifications(final ValidateCpsFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers, new CpsAcceptedNotification(formData));
	}

	@Override
	public boolean isOwn(final Long userId) {
		final Long currentUserId = super.userHelper.getCurrentUserId();

		if (null == userId) {
			LOG.error("Cannot check currentUser own with empty or null UserId");
			return false;
		} else if (null == currentUserId) {
			LOG.error("Cannot check currentUser because he's id is null");
			return false;
		} else if (userId.equals(currentUserId)) {
			return true;
		}

		return false;
	}

	@Override
	public UserFormData getCurrentUserDetails() {
		final UserFormData formData = new UserFormData();
		formData.getUserId().setValue(super.userHelper.getCurrentUserId());
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
	public String getUserLanguage(final Long userId) {
		// No permission check to allow organizer get language of guest (for
		// email)
		final UserFormData formData = new UserFormData();
		formData.getUserId().setValue(userId);

		SQL.selectInto(SQLs.USER_SELECT_LANGUAGE + SQLs.USER_SELECT_FILTER_ID + SQLs.USER_SELECT_INTO_LANGUAGE,
				formData, new NVPair("currentUser", userId));

		return formData.getLanguage().getValue();
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
		this.loadUserIdByEmail(formData);
		if (null == formData.getUserId().getValue()) {
			formData.getLogin().setValue(loginOrEmail.toLowerCase());
			this.loadUserIdByLogin(formData);
		}

		return formData.getUserId().getValue();
	}

	@Override
	public Long getUserIdByEmail(final String email) {
		// TODO Djer13 add permission Check ? Warning : may be called by an
		// event Organizer to get the guest UserId (by Email)
		final UserFormData formData = new UserFormData();
		formData.getEmail().setValue(email.toLowerCase());
		this.loadUserIdByEmail(formData);
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

	@Override
	public Set<Long> getAllUserId() {
		// Used for migration, No permission check (only technical data)
		final Set<Long> allUserIds = new HashSet<>();
		final Object[][] result = SQL.select(SQLs.USER_SELECT_ID_ONLY);
		if (null != result && result.length > 0) {
			for (int i = 0; i < result.length; i++) {
				if (null != result[i]) {
					allUserIds.add((Long) result[i][0]);
				}
			}
		}
		return allUserIds;
	}

	@Override
	public void addSubFreeToAllUsers() {
		LOG.info("Adding role 'subscription free' to All existing users");
		final Long roleId = 3l;
		final Set<Long> users = this.getAllUserId();

		if (null != users && users.size() > 0) {
			final Iterator<Long> itUsers = users.iterator();
			while (itUsers.hasNext()) {
				final Long userId = itUsers.next();
				try {
					this.addSubscriptionAsynch(userId, 3l);
				} catch (final Exception ex) {
					LOG.warn("Error while trying to insert Role " + roleId + " to User :" + userId
							+ " continuing to next User", ex);
				}
			}
		}
	}
}
