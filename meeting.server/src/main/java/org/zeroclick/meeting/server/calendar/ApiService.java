package org.zeroclick.meeting.server.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.api.ApiModifiedNotification;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddEmailToApi;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CreateApiPermission;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.ReadApiPermission;
import org.zeroclick.meeting.shared.calendar.UpdateApiPermission;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class ApiService extends AbstractCommonService implements IApiService {

	private static final Logger LOG = LoggerFactory.getLogger(ApiService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public ApiTablePageData getApiTableData(final SearchFilter filter) {
		final ApiTablePageData pageData = new ApiTablePageData();

		Long userId = null;

		if (null != filter && null != filter.getFormData() && null != filter.getFormData().getPropertyById("userId")) {
			userId = (Long) filter.getFormData().getPropertyById("userId").getValue();
		}

		final StringBuilder sql = new StringBuilder();
		sql.append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT);

		if (null == userId && ACCESS.getLevel(new ReadApiPermission((Long) null)) != ReadApiPermission.LEVEL_ALL) {
			userId = super.userHelper.getCurrentUserId();
		}

		if (null != userId) {
			sql.append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT_FILTER_USER);
		}

		sql.append(SQLs.OAUHTCREDENTIAL_PAGE_DATA_SELECT_INTO);
		SQL.selectInto(sql.toString(), new NVPair("page", pageData), new NVPair("currentUser", userId));

		if (pageData.getRowCount() > 0) {
			// Local cache to avoid multiple validation of same apiCredentialId
			final Map<Long, Boolean> alreadyCheckReadAcces = new HashMap<>();
			// Post check permission base on OAuthId
			for (final ApiTableRowData row : pageData.getRows()) {
				if (null == alreadyCheckReadAcces.get(row.getApiCredentialId())) {
					final Boolean canRead = ACCESS.check(new ReadApiPermission(row.getApiCredentialId()));
					alreadyCheckReadAcces.put(row.getApiCredentialId(), canRead);
				}
				if (!alreadyCheckReadAcces.get(row.getApiCredentialId())) {
					LOG.warn("User : " + super.userHelper.getCurrentUserId() + " try to access UserApi : "
							+ row.getApiCredentialId() + " belonging to user : " + row.getUserId()
							+ " but hasen't acces. Silently removing this (api) row");
					pageData.removeRow(row);
				}
			}
		}

		return pageData;
	}

	@Override
	public ApiTablePageData getApis(final Long userId) {
		final SearchFilter filter = new SearchFilter();
		final ApiFormData apiSearchFilterForm = new ApiFormData();
		apiSearchFilterForm.setUserId(userId);
		filter.setFormData(apiSearchFilterForm);

		return this.getApiTableData(filter);
	}

	@Override
	public ApiFormData prepareCreate(final ApiFormData formData) {
		super.checkPermission(new CreateApiPermission());
		LOG.warn("PrepareCreate for Api");
		return this.store(formData);
	}

	@Override
	public ApiFormData create(final ApiFormData formData) {
		return this.create(formData, Boolean.TRUE);
	}

	@Override
	public ApiFormData create(final ApiFormData formData, final Boolean sendNotification) {
		super.checkPermission(new CreateApiPermission());

		Boolean isNew = Boolean.FALSE;

		final Object[][] duplicateApiByEmailAccount = SQL.select(SQLs.OAUHTCREDENTIAL_SELECT_BY_ACCOUNT_EMAIL,
				formData);
		if (null == duplicateApiByEmailAccount || duplicateApiByEmailAccount.length == 0) {
			LOG.info("Creating new API in DB for user : " + formData.getUserIdProperty().getValue()
					+ " with account email : " + formData.getAccountEmail().getValue() + " for provider : "
					+ formData.getProvider().getValue());
			isNew = Boolean.TRUE;
			// add a unique id if necessary
			if (null == formData.getApiCredentialId()) {
				formData.setApiCredentialId(SQL.getSequenceNextval("OAUHTCREDENTIAL_ID_SEQ"));
			}
			SQL.insert(SQLs.OAUHTCREDENTIAL_INSERT, formData);
		} else {
			LOG.warn("Duplicate API id found for user : " + formData.getUserId() + "  with account's email : "
					+ formData.getAccountEmail().getValue() + " Not created only perform umpdate");
		}

		final ApiFormData apiFormCreated = this.store(formData, sendNotification || !isNew);

		if (sendNotification && isNew) {
			this.sendCreatedNotifications(apiFormCreated);
		}

		return apiFormCreated;
	}

	private void sendCreatedNotifications(final ApiFormData formData) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acs.getUserNotificationIds(formData.getUserId()),
				new ApiCreatedNotification(formData));
	}

	private void sendDeletedNotifications(final ApiFormData formData) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acs.getUserNotificationIds(formData.getUserId()),
				new ApiDeletedNotification(formData));
	}

	private void sendMoifiedNotifications(final ApiFormData formData) {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acs.getUserNotificationIds(formData.getUserId()),
				new ApiModifiedNotification(formData));
	}

	@Override
	public ApiFormData load(final ApiFormData formData) {
		return this.load(formData, Boolean.TRUE);
	}

	private ApiFormData load(final ApiFormData formData, final Boolean loadProviderData) {
		final Long userId = formData.getUserIdProperty().getValue();
		Long oAuthId = formData.getApiCredentialIdProperty().getValue();
		final String accesToken = formData.getAccessToken().getValue();
		final String accountsEmail = formData.getAccountEmail().getValue();

		LOG.debug("Loading credential by ID : " + oAuthId + ", and UserId : " + userId + " and accesToken : "
				+ accesToken);

		if (null == oAuthId && null != accesToken && !accesToken.isEmpty()) {
			LOG.debug("No API ID but an accessToken, using accessToken to load api Data");
			oAuthId = this.getApiIdByAccessToken(userId, accesToken);
		}

		if (null == oAuthId && null != accountsEmail && !accountsEmail.isEmpty()) {
			LOG.debug("No API ID but an account's email found, using accountsEmail to load api Data");
			oAuthId = this.getApiIdByAccountEmail(userId, accountsEmail);
		}

		// if (null == oAuthId) {
		// oAuthId = this.getApiId(userId);
		// }
		if (null == oAuthId) {
			LOG.debug("No API ID found for userId : " + userId);
			return formData;
		} else {
			formData.setApiCredentialId(oAuthId);
		}

		LOG.debug("Loading credential by ID : " + oAuthId);

		if (!ACCESS.check(new ReadApiPermission(oAuthId))) {
			final Long currentUserId = super.userHelper.getCurrentUserId();
			LOG.error("User :" + currentUserId + " (id : " + currentUserId + " try to load Api Data with Id : "
					+ oAuthId + " (user : " + userId + ") wich belong to User " + userId
					+ " But haven't 'ALL'/'RELATED' read permission");

			super.throwAuthorizationFailed();
		}

		SQL.selectInto(
				SQLs.OAUHTCREDENTIAL_SELECT + SQLs.OAUHTCREDENTIAL_FILTER_OAUTH_ID + SQLs.OAUHTCREDENTIAL_SELECT_INTO,
				formData);

		if (loadProviderData && null == formData.getProviderData()) {
			// force load BLOB data
			final Object[][] apiProvierData = SQL.select(
					SQLs.OAUHTCREDENTIAL_SELECT_PROVIDER_DATA_ONLY + SQLs.OAUHTCREDENTIAL_FILTER_OAUTH_ID,
					new NVPair("apiCredentialId", oAuthId));
			if (apiProvierData.length == 1) {
				formData.setProviderData((byte[]) apiProvierData[0][0]);
			}
		}

		return formData;
	}

	private ApiFormData loadByAccountsEmail(final Long userId, final String accountEmail) {
		final ApiFormData input = new ApiFormData();
		input.setUserId(userId);
		input.getAccountEmail().setValue(accountEmail);
		return this.load(input, Boolean.FALSE);
	}

	@Override
	public ApiFormData load(final Long apiId) {
		final ApiFormData input = new ApiFormData();
		input.setApiCredentialId(apiId);

		return this.load(input, Boolean.FALSE);
	}

	@Override
	public ApiFormData store(final ApiFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	/**
	 * Store accont's email to the API. This method is call at the end of new
	 * API configuration. If an api already exist in DB form this user and this
	 * account's email, this api is refreshed.
	 *
	 * /!\ the original api created this the original ID, is deleted, and should
	 * be discarded in UserCache (credential Cache)
	 *
	 * @param formData
	 * @return
	 */
	@Override
	public ApiFormData storeAccountEmail(final ApiFormData newDataForm, final Long userId, final String accountEmail) {
		ApiFormData updatedData = null;
		// if already an API with this Email for this user
		final ApiFormData exstingCredentialId = this.loadByAccountsEmail(userId, accountEmail);

		if (null != exstingCredentialId && null != exstingCredentialId.getApiCredentialId()
				&& !exstingCredentialId.getApiCredentialId().equals(newDataForm.getApiCredentialId())) {
			// User try to add an already existing API

			// ==> refresh the old one with the new Data form provider
			// (accessToken, refreshToken, ....)
			exstingCredentialId.setProviderData(newDataForm.getProviderData());
			exstingCredentialId.setRepositoryId(newDataForm.getRepositoryId());
			exstingCredentialId.setUserId(newDataForm.getUserId());
			exstingCredentialId.getAccessToken().setValue(newDataForm.getAccessToken().getValue());
			exstingCredentialId.getExpirationTimeMilliseconds()
					.setValue(newDataForm.getExpirationTimeMilliseconds().getValue());
			exstingCredentialId.getProvider().setValue(newDataForm.getProvider().getValue());

			// ==> Only update existing API
			updatedData = this.store(exstingCredentialId, Boolean.TRUE, Boolean.FALSE);

			// ==> Remove the "new duplicate" credential
			this.delete(newDataForm);
		} else {
			final ApiFormData apiDataAfterCredentialStored = this.load(newDataForm.getApiCredentialId());

			apiDataAfterCredentialStored.getAccountEmail().setValue(accountEmail);
			updatedData = this.store(apiDataAfterCredentialStored, Boolean.TRUE, Boolean.TRUE);
		}

		return updatedData;
	}

	private ApiFormData store(final ApiFormData formData, final Boolean sendNotifications) {
		return this.store(formData, sendNotifications, Boolean.FALSE);
	}

	private ApiFormData store(final ApiFormData formData, final Boolean sendNotifications,
			final Boolean useCreatedNotification) {
		super.checkPermission(new UpdateApiPermission(formData.getApiCredentialId()));

		LOG.debug("Storing API (" + formData.getApiCredentialId() + ") in DB for user : "
				+ formData.getUserIdProperty().getValue() + " for provider : " + formData.getProvider().getValue());

		if (this.isAfterCreateAddAccountEmailPatch()) {
			SQL.update(SQLs.OAUHTCREDENTIAL_UPDATE, formData);
		} else {
			SQL.update(SQLs.OAUHTCREDENTIAL_UPDATE_WITHOUT_ACCOUNT_EMAIL, formData);
		}

		if (sendNotifications) {
			if (useCreatedNotification) {
				this.sendCreatedNotifications(formData);
			} else {
				this.sendMoifiedNotifications(formData);
			}
		}

		return formData;
	}

	@Override
	public void delete(final ApiFormData formData) {
		super.checkPermission(new DeleteApiPermission(formData.getApiCredentialId()));

		LOG.debug("Deleting API in DB for api_id : " + formData.getApiCredentialId() + "(user : "
				+ formData.getUserIdProperty().getValue() + ") for provider : " + formData.getProvider().getValue());

		final ApiFormData dataBeforeDeletion = this.load(formData);

		// delete related table data to maintain FK constraints
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);
		calendarConfigurationService.deleteByApiId(formData.getApiCredentialId());

		SQL.delete(SQLs.OAUHTCREDENTIAL_DELETE, dataBeforeDeletion);

		// the formData BEFORE deletion
		this.sendDeletedNotifications(dataBeforeDeletion);
	}

	// private void delete(final Long apiCredentialId) {
	// final ApiFormData input = new ApiFormData();
	//
	// input.setApiCredentialId(apiCredentialId);
	//
	// this.delete(input);
	// }

	private Long getApiId(final Long userId) {
		// Warning permission check at the end to allow "attendee" to check
		// "host" calendar
		LOG.debug("Searching API Id for user : " + userId);

		final ApiFormData formData = new ApiFormData();
		formData.setUserId(userId);
		SQL.selectInto(SQLs.OAUHTCREDENTIAL_SELECT_API_ID + SQLs.OAUHTCREDENTIAL_FILTER_USER_ID
				+ SQLs.OAUHTCREDENTIAL_SELECT_INTO_API_ID, formData);

		if (null != formData.getApiCredentialId()
				&& !ACCESS.check(new ReadApiPermission(formData.getApiCredentialId()))) {
			super.throwAuthorizationFailed();
		}
		return formData.getApiCredentialId();
	}

	@Override
	public Long getApiIdByAccessToken(final Long userId, final String accessToken) {
		LOG.debug("Searching API Id  : " + userId + " with Acces Token : " + accessToken);
		final ApiFormData formData = new ApiFormData();
		formData.setUserId(userId);
		formData.getAccessToken().setValue(accessToken);
		SQL.selectInto(SQLs.OAUHTCREDENTIAL_SELECT_API_ID + SQLs.OAUHTCREDENTIAL_FILTER_USER_ID
				+ SQLs.OAUHTCREDENTIAL_FILTER_ACESS_TOKEN + SQLs.OAUHTCREDENTIAL_SELECT_INTO_API_ID, formData);

		if (null != formData.getApiCredentialId()
				&& !ACCESS.check(new ReadApiPermission(formData.getApiCredentialId()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getApiCredentialId()) {
			LOG.warn("No API Id in DataBase for user :" + userId + " and accesToken : " + accessToken);
		} else {
			LOG.debug("API id : " + formData.getApiCredentialId() + " for user :" + userId + " and accesToken : "
					+ accessToken);
		}
		return formData.getApiCredentialId();
	}

	private Long getApiIdByAccountEmail(final Long userId, final String accountsEmail) {
		LOG.debug("Searching API Id  : " + userId + " with account's email : " + accountsEmail);
		final ApiFormData formData = new ApiFormData();
		formData.setUserId(userId);
		formData.getAccountEmail().setValue(accountsEmail);
		SQL.selectInto(
				SQLs.OAUHTCREDENTIAL_SELECT_API_ID + SQLs.OAUHTCREDENTIAL_FILTER_USER_ID
						+ SQLs.OAUHTCREDENTIAL_FILTER_ACCOUNTS_EMAIL + SQLs.OAUHTCREDENTIAL_SELECT_INTO_API_ID,
				formData);

		if (null != formData.getApiCredentialId()
				&& !ACCESS.check(new ReadApiPermission(formData.getApiCredentialId()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getApiCredentialId()) {
			LOG.warn("No API Id in DataBase for user :" + userId + " and account's email : " + accountsEmail);
		} else {
			LOG.debug("API id : " + formData.getApiCredentialId() + " for user :" + userId + " and account's email : "
					+ accountsEmail);
		}
		return formData.getApiCredentialId();
	}

	@Override
	public Set<String> getAllUserId() {
		// FIXME Djer13 ByPassing userPermission security. Is it required ?
		// if (!ACCESS.check(new ReadApiPermission())) {
		// throw new VetoException(TEXTS.get("AuthorizationFailed"));
		// }

		final Set<String> result = new HashSet<>();
		LOG.debug("Loading all userId");

		final Object[][] queryResult = SQL.select(SQLs.OAUHTCREDENTIAL_SELECT_ALL_USER_IDS, new ApiFormData());

		for (int i = 0; i < queryResult.length; i++) {
			result.add((String) queryResult[i][0]);
		}
		return result;
	}

	@Override
	public Collection<ApiFormData> loadGoogleData() {
		// FIXME Djer13 ByPassing userPermission security. Is it required ?
		// if (!ACCESS.check(new ReadApiPermission())) {
		// throw new VetoException(TEXTS.get("AuthorizationFailed"));
		// }
		final List<ApiFormData> result = new ArrayList<>();
		LOG.debug("Loading all datas (only Binary Google Data)");

		SQL.selectInto(SQLs.OAUHTCREDENTIAL_SELECT_GOOGLE_DATA + SQLs.OAUHTCREDENTIAL_SELECT_INTO, result);
		return result;
	}

	public Long getOwner(final Long apiCredentialId) {
		final ApiFormData formData = new ApiFormData();
		formData.setApiCredentialId(apiCredentialId);
		SQL.selectInto(SQLs.OAUHTCREDENTIAL_SELECT_OWNER, formData);

		return formData.getUserId();
	}

	private Boolean isAfterCreateAddAccountEmailPatch() {
		return DatabaseHelper.get().isColumnExists(PatchAddEmailToApi.PATCHED_TABLE,
				PatchAddEmailToApi.PATCHED_ADDED_COLUMN);
	}

	@Override
	public boolean isOwn(final Long apiCredentialId) {
		final Long apiCredentialOwner = this.getOwner(apiCredentialId);

		if (null == apiCredentialOwner) {
			LOG.error("ApiCrentialId " + apiCredentialId + " as NO owner (user_id)");
			return false;
		} else if (apiCredentialOwner.equals(super.userHelper.getCurrentUserId())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isRelated(final Long apiCredentialId) {
		final Long apiCredentialOwner = this.getOwner(apiCredentialId);

		final IEventService eventService = BEANS.get(IEventService.class);

		final Map<Long, Integer> pendingUsers = eventService.getUsersWithPendingMeeting();

		return pendingUsers.containsKey(apiCredentialOwner) ? pendingUsers.get(apiCredentialOwner) > 0 : Boolean.FALSE;
	}
}
