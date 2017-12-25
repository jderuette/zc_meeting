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
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.AbstractDataCache;
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.common.document.DocumentFormData.LinkedRole.LinkedRoleRowData;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.role.CpsAcceptedNotification;
import org.zeroclick.configuration.shared.role.CreateAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.ReadAssignSubscriptionToUserPermission;
import org.zeroclick.configuration.shared.role.UpdateAssignToRolePermission;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.ReadUserPermission;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserModifiedNotification;
import org.zeroclick.configuration.shared.user.UserTablePageData;
import org.zeroclick.configuration.shared.user.UserTablePageData.UserTableRowData;
import org.zeroclick.configuration.shared.user.ValidateCpsFormData;
import org.zeroclick.meeting.server.security.ServerAccessControlService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class UserService extends AbstractCommonService implements IUserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	protected static final Charset CHARSET = StandardCharsets.UTF_16;

	private static final String NOTIFICATION_USER = "notification-authenticator";

	public static final Long[] DEFAULT_ROLES_VALUES = new Long[] { 2l };
	public static final Set<Long> DEFAULT_ROLES = new HashSet<>(Arrays.asList(DEFAULT_ROLES_VALUES));
	public static final Long DEFAULT_SUBSCRIPTION = 3l;

	@Override
	protected Logger getLog() {
		return LOG;
	}

	private final AbstractDataCache<Long, UserFormData> dataCache = new AbstractDataCache<Long, UserFormData>() {
		@Override
		public UserFormData loadForCache(final Long key) {
			final UserFormData formData = new UserFormData();
			formData.getUserId().setValue(key);
			return UserService.this.loadForCache(formData);
		}
	};

	private ICache<Long, UserFormData> getDataCache() {
		return this.dataCache.getCache();
	}

	@Override
	public UserTablePageData getUserTableData(final SearchFilter filter) {
		final UserTablePageData pageData = new UserTablePageData();
		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (ACCESS.getLevel(new ReadUserPermission((Long) null)) != ReadUserPermission.LEVEL_ALL) {
			ownerFilter = SQLs.USER_SELECT_FILTER_ID;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.USER_PAGE_SELECT + SQLs.USER_PAGE_ADD_STATS_SELECT + SQLs.USER_FROM
				+ SQLs.USER_PAGE_ADD_STATS + SQLs.GENERIC_WHERE_FOR_SECURE_AND + ownerFilter
				+ SQLs.USER_PAGE_DATA_SELECT_INTO + SQLs.USER_PAGE_ADD_STATS_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));

		this.addNbProcessedEventStat(pageData);

		return pageData;
	}

	@Override
	public UserFormData prepareCreate(final UserFormData formData) {
		super.checkPermission(new CreateUserPermission());
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
			super.checkPermission(new CreateUserPermission());
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

		LOG.info("Create User with Id :" + formData.getUserId().getValue() + ", Login : "
				+ formData.getLogin().getValue() + " and email : " + formData.getEmail().getValue());

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
		LOG.info("Checking if login : " + userLogin + " already Exists");
		final UserFormData input = new UserFormData();
		input.getLogin().setValue(userLogin.toLowerCase());

		final UserFormData existingUser = this.load(input);
		return null != existingUser.getUserId().getValue();
	}

	@Override
	public UserFormData load(final UserFormData formData) {
		super.checkPermission(new ReadUserPermission(formData.getUserId().getValue()));
		UserFormData cachedData = this.getDataCache().get(formData.getUserId().getValue());
		if (null == cachedData) {
			// avoid NPE
			cachedData = formData;
		}
		return cachedData;
	}

	private UserFormData loadForCache(final UserFormData formData) {
		// Permission must be checked outside cache loading !
		LOG.debug("Load User with Id :" + formData.getUserId().getValue() + " and email : "
				+ formData.getEmail().getValue() + " (login : " + formData.getLogin().getValue() + ")");

		final Long currentSelectedUserId = formData.getUserId().getValue();

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
		super.checkPermission(new ReadAssignSubscriptionToUserPermission(formData.getUserId().getValue()));

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

	private void addNbProcessedEventStat(final UserTablePageData pageData) {
		if (pageData.getRowCount() > 0) {
			for (final UserTableRowData row : pageData.getRows()) {
				final Long userId = row.getUserId();
				if (null != userId) {
					SQL.selectInto(SQLs.USER_STATS_NB_PROCESSED_EVENT, row);
				}
			}
		}
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

	@Override
	public ValidateCpsFormData getActiveSubscriptionDetails(final Long userId) {
		return this.getActiveSubscriptionDetails(userId, Boolean.TRUE);
	}

	private ValidateCpsFormData getActiveSubscriptionDetails(final Long userId, final Boolean checkAccess) {
		if (checkAccess && !ACCESS.check(new ReadUserPermission(userId))) {
			this.throwAuthorizationFailed();
		}
		final ValidateCpsFormData formDataSubscription = new ValidateCpsFormData();
		SQL.selectInto(
				SQLs.USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_DETAILS + SQLs.USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS_INTO,
				new NVPair("subscriptionDetails", formDataSubscription), new NVPair("userId", userId));
		return formDataSubscription;
	}

	private Boolean isActiveSubscriptionValid(final Long userId, final Boolean checkAccess) {
		final ValidateCpsFormData activeSubscriptionDatas = this.getActiveSubscriptionDetails(userId, checkAccess);
		Boolean subscriptionValid = Boolean.TRUE;
		if (null != activeSubscriptionDatas && null != activeSubscriptionDatas.getSubscriptionId().getValue()) {
			final Long subscriptionId = activeSubscriptionDatas.getSubscriptionId().getValue();

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

				final Date acceptedCpsDate = activeSubscriptionDatas.getAcceptedCpsDate().getValue();
				final Date accepteWithdrawDate = activeSubscriptionDatas.getAcceptedWithdrawalDate().getValue();

				final Boolean acceptedCPSDateValid = null != acceptedCpsDate
						&& acceptedCpsDate.after(documentRoleData.getStartDate());
				// Withdrawal is required only ONCE for a specific subscription
				final Boolean accepteWithdrawDateValid = null != accepteWithdrawDate;

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

		if (NOTIFICATION_USER.equals(formData.getLogin().getValue())) {
			// default notification user dosen't exist, because it don't need
			// specific permissions.
			return formData; // early Break
		}

		SQL.selectInto(SQLs.USER_SELECT_ID_ONLY + SQLs.USER_SELECT_FILTER_LOGIN + SQLs.USER_SELECT_INTO_ID_ONLY,
				formData);
		if (null == formData.getUserId().getValue()) {
			LOG.warn("No User ID for user with login : " + formData.getLogin().getValue());
		}
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

		this.updatesRoles(formData, !formData.getAutofilled());
		// Usefull only for admin, for other user store(ValidateCpsForm) already
		// do the update
		this.updateSubscriptions(formData, !formData.getAutofilled());

		// reset permission cache
		this.sendModifiedNotifications(formData);
		// reset local user cache
		this.dataCache.clearCache(formData.getUserId().getValue());

		return formData;
	}

	/**
	 * Add and remove roles
	 *
	 * @param formData
	 */
	private void updatesRoles(final UserFormData formData, final Boolean checkAccess) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final UserFormData existingUserRoles = this.getRoles(formData.getUserId().getValue(), checkAccess);

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

	private void updateSubscriptions(final UserFormData formData, final Boolean checkAccess) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final UserFormData currentSubscriptionData = this.getSubscription(formData.getUserId().getValue(), checkAccess);
		final Long currentSubscription = currentSubscriptionData.getSubscriptionBox().getValue();
		final Long newSubscription = formData.getSubscriptionBox().getValue();

		if (null == currentSubscription || !currentSubscription.equals(newSubscription)) {
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

		SQL.update(SQLs.SUBSCRIPTION_INSERT, validateCpsFakeFormData);
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
	public void delete(final UserFormData formData) {
		super.checkPermission(new UpdateUserPermission(formData.getUserId().getValue()));
		final IRoleService roleService = BEANS.get(IRoleService.class);
		Long userId = null;
		if (null == formData.getUserId().getValue() && null != formData.getLogin().getValue()) {
			userId = this.getUserId(formData.getLogin().getValue());
		}
		if (null == userId && null != formData.getEmail().getValue()) {
			userId = this.getUserId(formData.getEmail().getValue());
		}
		if (null == formData.getUserId().getValue()) {
			formData.getUserId().setValue(userId);
		}

		if (!ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}

		LOG.info("Deleting User by Id : " + userId + " (asked email : " + formData.getEmail().getValue() + ", login : "
				+ formData.getLogin().getValue() + ")");

		roleService.deleteSubscriptionMetaDataByUser(userId);
		this.deleteUserRoleByUser(formData);
		this.changeInvitedByByUserId(formData);
		SQL.insert(SQLs.USER_DELETE, formData);

		// reset local user cache
		this.dataCache.clearCache(formData.getUserId().getValue());

	}

	private void deleteUserRoleByUser(final UserFormData formData) {
		super.checkPermission(new UpdateAssignToRolePermission());
		LOG.info("Deleting Link between Role and User by user Id : " + formData.getUserId().getValue());
		SQL.insert(SQLs.USER_ROLE_REMOVE_BY_USER, formData);
	}

	private void changeInvitedByByUserId(final UserFormData formData) {
		if (!ACCESS.check(new UpdateUserPermission(formData.getUserId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		final long defaultInvitedBy = 1l;
		LOG.info("Changing 'invited_by' for user : " + formData.getUserId().getValue() + " to the new User : "
				+ defaultInvitedBy);
		SQL.insert(SQLs.USER_UPDATE_INVITED_BY_BY_USER, formData, new NVPair("invitedBy", defaultInvitedBy));
		// reset local user cache for all User as many user may be affected by
		// operation
		this.dataCache.clearCache();
	}

	@Override
	public OnBoardingUserFormData store(final OnBoardingUserFormData formData) {
		super.checkPermission(new UpdateUserPermission(formData.getUserId().getValue()));
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

		this.dataCache.clearCache(formData.getUserId().getValue());

		return formData;
	}

	@Override
	public ValidateCpsFormData create(final ValidateCpsFormData formData) {
		super.checkPermission(new CreateAssignSubscriptionToUserPermission(formData.getUserId().getValue()));

		SQL.update(SQLs.USER_ROLE_INSERT_WITH_START_DATE, new NVPair("userId", formData.getUserId().getValue()),
				new NVPair("rolesBox", formData.getSubscriptionId().getValue()),
				new NVPair("startDate", formData.getStartDate().getValue()));

		SQL.insert(SQLs.SUBSCRIPTION_INSERT, formData);

		this.store(formData);

		return formData;
	}

	@Override
	public ValidateCpsFormData store(final ValidateCpsFormData formData) {
		super.checkPermission(new CreateAssignSubscriptionToUserPermission(formData.getUserId().getValue()));
		final AccessControlService acs = BEANS.get(AccessControlService.class);

		LOG.debug("Store CPS User datas for user Id :" + formData.getUserId().getValue()
				+ " for subscription (role) id : " + formData.getSubscriptionId().getValue() + " start at : "
				+ formData.getStartDate().getValue());

		SQL.update(SQLs.USER_ROLE_UPDATE_CPS, formData);

		acs.clearUserCache(this.buildNotifiedUsers(formData));

		this.sendModifiedNotifications(formData);

		this.dataCache.clearCache(formData.getUserId().getValue());

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
		UserFormData formData = new UserFormData();
		formData.getUserId().setValue(userId);

		formData = this.load(formData);

		return formData.getTimeZone().getValue();
	}

	@Override
	public String getUserLanguage(final Long userId) {
		// No permission check to allow organizer get language of guest (for
		// email)
		UserFormData formData = new UserFormData();
		formData.getUserId().setValue(userId);

		formData = this.load(formData);

		return formData.getLanguage().getValue();
	}

	@Override
	public Set<String> getUserNotificationIds(final Long userId) {
		final Set<String> notificationsIds = new HashSet<>();
		UserFormData formData = new UserFormData();
		formData.getUserId().setValue(userId);

		formData = this.load(formData);

		if (null != formData.getLogin().getValue() && !formData.getLogin().getValue().isEmpty()) {
			notificationsIds.add(formData.getLogin().getValue());
		}
		if (null != formData.getEmail().getValue() && !formData.getEmail().getValue().isEmpty()) {
			notificationsIds.add(formData.getEmail().getValue());
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
		formData.getEmail().setValue(email.toLowerCase());
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
	public UserFormData loggedIn(final UserFormData userData) {
		// No permission check, because used during login
		final Date now = new Date();
		LOG.info("User " + userData.getEmail() + " (login : " + userData.getLogin()
				+ ") juste logged in, updating stats data");
		SQL.insert(SQLs.USER_UPDATE_LAST_LOGIN, userData, new NVPair("currentDate", now));

		this.dataCache.clearCache(userData.getUserId().getValue());
		return userData;
	}

	@Override
	public void clearCache(final Long userId) {
		LOG.info("Clear cache (and server/UI permisison cache) for user ID : " + userId);
		this.dataCache.clearCache(userId);

		final Set<String> userCacheKey = this.getUserNotificationIds(userId);
		BEANS.get(ServerAccessControlService.class).clearCacheOfUsersIds(userCacheKey);
		// BEANS.get(AccessControlService.class).clearUserCache(userCacheKey);
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
					this.dataCache.clearCache(userId);
				} catch (final Exception ex) {
					LOG.warn("Error while trying to insert Role " + roleId + " to User :" + userId
							+ " continuing to next User", ex);
				}
			}
		}
	}
}
