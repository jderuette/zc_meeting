package org.zeroclick.meeting.server.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.AbstractFormDataCache;
import org.zeroclick.common.AbstractPageDataDataCache;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.api.ApiModifiedNotification;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddEmailToApi;
import org.zeroclick.meeting.server.sql.migrate.data.PatchManageMicrosoftCalendars;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.CreateApiPermission;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.ReadApiPermission;
import org.zeroclick.meeting.shared.calendar.UpdateApiPermission;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

public class ApiService extends AbstractCommonService implements IApiService {

	private static final Logger LOG = LoggerFactory.getLogger(ApiService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	private final AbstractFormDataCache<Long, ApiFormData> dataCache = new AbstractFormDataCache<Long, ApiFormData>() {
		@Override
		public ApiFormData loadForCache(final Long apiCredentailId) {
			final ApiFormData apiFormData = new ApiFormData();
			apiFormData.setApiCredentialId(apiCredentailId);
			return ApiService.this.loadForCache(apiFormData);
		}
	};

	private final AbstractPageDataDataCache<Long, ApiTablePageData> dataCacheByUserId = new AbstractPageDataDataCache<Long, ApiTablePageData>() {
		@Override
		public ApiTablePageData loadForCache(final Long userId) {
			return ApiService.this.loadForCacheByUserId(userId);
		}
	};

	private ICache<Long, ApiFormData> getDataCache() {
		return this.dataCache.getCache();
	}

	private ICache<Long, ApiTablePageData> getDataCacheByUser() {
		return this.dataCacheByUserId.getCache();
	}

	protected ApiFormData loadForCache(final ApiFormData apiFormData) {
		final ApiFormData result = new ApiFormData();
		result.setApiCredentialId(apiFormData.getApiCredentialId());
		SQL.selectInto(
				SQLs.OAUHTCREDENTIAL_SELECT + SQLs.OAUHTCREDENTIAL_FILTER_OAUTH_ID + SQLs.OAUHTCREDENTIAL_SELECT_INTO,
				result);

		if (null == result.getProviderData()) {
			// force load BLOB data
			final Object[][] apiProvierData = SQL.select(
					SQLs.OAUHTCREDENTIAL_SELECT_PROVIDER_DATA_ONLY + SQLs.OAUHTCREDENTIAL_FILTER_OAUTH_ID,
					new NVPair("apiCredentialId", apiFormData.getApiCredentialId()));
			if (apiProvierData.length == 1) {
				result.setProviderData((byte[]) apiProvierData[0][0]);
			}
		}
		return result;
	}

	protected ApiTablePageData loadForCacheByUserId(final Long userId) {
		final ApiTablePageData pageData = new ApiTablePageData();

		final StringBuilder sql = new StringBuilder();
		sql.append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT);

		if (null != userId) {
			sql.append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT_FILTER_USER);
		}

		sql.append(SQLs.OAUHTCREDENTIAL_PAGE_DATA_SELECT_INTO);
		SQL.selectInto(sql.toString(), new NVPair("page", pageData), new NVPair("currentUser", userId));

		return pageData;
	}

	@Override
	public ApiTablePageData getApiTableData(final SearchFilter filter) {
		return this.getApiTableData(filter, false);
	}

	private ApiTablePageData getApiTableData(final SearchFilter filter, final boolean displayAllForAdmin) {
		Long userId = null;

		if (null != filter && null != filter.getFormData() && null != filter.getFormData().getPropertyById("userId")) {
			userId = (Long) filter.getFormData().getPropertyById("userId").getValue();
		}

		final Boolean isAdmin = ACCESS.getLevel(new ReadApiPermission((Long) null)) == ReadApiPermission.LEVEL_ALL;
		final Boolean standardUserCanAccesUserId = ACCESS
				.getLevel(new ReadApiPermission(userId)) >= ReadApiPermission.LEVEL_RELATED;

		if (isAdmin) {
			if (null == userId && !displayAllForAdmin) {
				userId = super.userHelper.getCurrentUserId();
			}
		} else if (!standardUserCanAccesUserId) {
			userId = super.userHelper.getCurrentUserId();
		}

		if (!isAdmin && !displayAllForAdmin && null == userId) {
			LOG.warn("Trying to get APIs of null userId from a non admin or not listing ALL API (as admin) ");
			userId = super.userHelper.getCurrentUserId();
		}

		ApiTablePageData apis = null;

		if (null == userId) {
			// load for admin. Cached does not handle null "key"
			apis = this.loadForCacheByUserId(null);
		} else {
			apis = this.getDataCacheByUser().get(userId);
		}

		if (apis.getRowCount() > 0) {
			// Local cache to avoid multiple validation of same apiCredentialId
			final Map<Long, Boolean> alreadyCheckReadAcces = new HashMap<>();
			// Post check permission base on OAuthId
			for (final ApiTableRowData row : apis.getRows()) {
				if (null == alreadyCheckReadAcces.get(row.getApiCredentialId())) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final Boolean canRead = ACCESS.check(new ReadApiPermission(row.getApiCredentialId()));
					alreadyCheckReadAcces.put(row.getApiCredentialId(), canRead);
				}
				if (!alreadyCheckReadAcces.get(row.getApiCredentialId())) {
					LOG.warn("User : " + super.userHelper.getCurrentUserId() + " try to access UserApi : "
							+ row.getApiCredentialId() + " belonging to user : " + row.getUserId()
							+ " but hasen't acces. Silently removing this (api) row");
					apis.removeRow(row);
				}
			}
		}

		return apis;
	}

	@Override
	public ApiTablePageData getApiTableData(final boolean displayAllForAdmin) {
		return this.getApiTableData(null, displayAllForAdmin);
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
	public ApiTableRowData getApi(final Long apiCredentialId) {
		if (null == apiCredentialId) {
			throw new VetoException("Api Crendetail ID is required");
		}
		ApiTableRowData apiData = null;
		final ApiTablePageData pageData = new ApiTablePageData();

		final StringBuilder sql = new StringBuilder();
		sql.append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT).append(SQLs.OAUHTCREDENTIAL_PAGE_SELECT_FILTER_API_CREDENTIAL_ID)
				.append(SQLs.OAUHTCREDENTIAL_PAGE_DATA_SELECT_INTO);
		SQL.selectInto(sql.toString(), new NVPair("page", pageData), new NVPair("apiCredentialId", apiCredentialId));

		if (pageData.getRowCount() == 1) {
			apiData = pageData.getRows()[0];
		} else {
			LOG.error("No Api Data with apiCredentailID : " + apiCredentialId);
		}

		return apiData;
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

		final Long existingApiByEmailAccount = this.getApiIdByAccountEmail(formData.getUserId(),
				formData.getAccountEmail().getValue());

		if (null == existingApiByEmailAccount) {
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
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acsHelper.getUserNotificationIds(formData.getUserId()),
				new ApiCreatedNotification(formData));
	}

	private void sendDeletedNotifications(final ApiFormData formData) {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acsHelper.getUserNotificationIds(formData.getUserId()),
				new ApiDeletedNotification(formData));
	}

	private void sendMoifiedNotifications(final ApiFormData formData) {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(acsHelper.getUserNotificationIds(formData.getUserId()),
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

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Loading credential by ID : ").append(oAuthId).append(", and UserId : ")
					.append(userId).append(" and accesToken : ").append(accesToken).toString());
		}

		if (null == oAuthId && null != accesToken && !accesToken.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No API ID but an accessToken, using accessToken to load api Data");
			}
			oAuthId = this.getApiIdByAccessToken(userId, accesToken);
		}

		if (null == oAuthId && null != accountsEmail && !accountsEmail.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No API ID but an account's email found, using accountsEmail to load api Data");
			}
			oAuthId = this.getApiIdByAccountEmail(userId, accountsEmail);
		}

		if (null == oAuthId) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuffer().append("No API ID found for userId : ").append(userId).toString());
			}
			return formData;
		} else {
			formData.setApiCredentialId(oAuthId);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Loading credential by ID : ").append(oAuthId).toString());
		}

		if (!ACCESS.check(new ReadApiPermission(oAuthId))) {
			final Long currentUserId = super.userHelper.getCurrentUserId();
			LOG.error(new StringBuffer().append("User : ").append(currentUserId).append(" (id : ").append(currentUserId)
					.append(") try to load Api Data with Id : ").append(oAuthId).append(" (user : ").append(userId)
					.append(") wich belong to User ").append(userId)
					.append(" But haven't 'ALL'/'RELATED' read permission").toString());

			super.throwAuthorizationFailed();
		}

		ApiFormData cachedData = this.getDataCache().get(formData.getApiCredentialId());
		if (null == cachedData) {
			// avoid NPE
			cachedData = formData;
		}

		return cachedData;
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
	 * /!\ the original api created with the original ID, is deleted, and should
	 * be discarded in UserCache (credential Cache)
	 *
	 * @param formData
	 * @return
	 */
	@Override
	public ApiFormData storeAccountEmail(final ApiFormData newDataForm, final String accountEmail) {
		ApiFormData updatedData = null;
		// if already an API with this Email for this user
		final ApiFormData exstingCredentialId = this.loadByAccountsEmail(newDataForm.getUserId(), accountEmail);

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
			exstingCredentialId.getAccountEmail().setValue(accountEmail);

			// ==> Only update existing API
			updatedData = this.store(exstingCredentialId, Boolean.TRUE, Boolean.FALSE);

			// ==> Remove the "new duplicate" credential (created during "init"
			// of googleStore.savecCredential
			this.delete(newDataForm);
		} else {
			final ApiFormData existingCredentialStored = this.load(newDataForm.getApiCredentialId());

			existingCredentialStored.getAccountEmail().setValue(accountEmail);
			updatedData = this.store(existingCredentialStored, Boolean.TRUE, Boolean.TRUE);
		}

		return updatedData;
	}

	private ApiFormData store(final ApiFormData formData, final Boolean sendNotifications) {
		return this.store(formData, sendNotifications, Boolean.FALSE);
	}

	private ApiFormData store(final ApiFormData formData, final Boolean sendNotifications,
			final Boolean useCreatedNotification) {
		super.checkPermission(new UpdateApiPermission(formData.getApiCredentialId()));
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Storing API (").append(formData.getApiCredentialId())
					.append(") in DB for user : ").append(formData.getUserIdProperty().getValue())
					.append(" for provider : ").append(formData.getProvider().getValue()).toString());
		}

		if (this.isAfterCreateAddAccountEmailPatch()) {
			if (this.isAfterCreatAddTenantId()) {
				SQL.update(SQLs.OAUHTCREDENTIAL_UPDATE_WITH_TENANT_ID, formData);
			} else {
				SQL.update(SQLs.OAUHTCREDENTIAL_UPDATE, formData);
			}
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

		this.clearCaches(formData);

		return formData;
	}

	@Override
	public void delete(final ApiFormData formData) {
		super.checkPermission(new DeleteApiPermission(formData.getApiCredentialId()));
		if (LOG.isDebugEnabled()) {
			LOG.debug(
					new StringBuffer().append("Deleting API in DB for api_id : ").append(formData.getApiCredentialId())
							.append("(user : ").append(formData.getUserIdProperty().getValue())
							.append(") for provider : ").append(formData.getProvider().getValue()).toString());
		}

		final ApiFormData dataBeforeDeletion = this.load(formData);

		// delete related table data to maintain FK constraints
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);
		calendarConfigurationService.deleteByApiId(formData.getApiCredentialId());

		SQL.delete(SQLs.OAUHTCREDENTIAL_DELETE, dataBeforeDeletion);

		// the formData BEFORE deletion
		this.sendDeletedNotifications(dataBeforeDeletion);

		this.clearCaches(formData);
	}

	@Override
	public Long getApiIdByAccessToken(final Long userId, final String accessToken) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Searching API Id  : ").append(userId).append(" with Acces Token : ")
					.append(accessToken).toString());
		}

		final ApiTablePageData cachedData = this.getDataCacheByUser().get(userId);

		ApiTableRowData matchingApi = null;
		if (null != cachedData && cachedData.getRowCount() > 0) {
			for (final ApiTableRowData cachedApi : cachedData.getRows()) {
				if (cachedApi.getAccessToken().equals(accessToken)) {
					matchingApi = cachedApi;
					break;
				}
			}
		}

		this.checkApiAcces(matchingApi);

		return matchingApi.getApiCredentialId();
	}

	private Long getApiIdByAccountEmail(final Long userId, final String accountsEmail) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append("Searching API Id  : ").append(userId)
					.append(" with account's email : ").append(accountsEmail).toString());
		}

		final ApiTablePageData cachedData = this.getDataCacheByUser().get(userId);

		ApiTableRowData matchingApi = null;
		if (null != cachedData && cachedData.getRowCount() > 0) {
			for (final ApiTableRowData cachedApi : cachedData.getRows()) {
				if (null != cachedApi.getAccountEmail() && cachedApi.getAccountEmail().equals(accountsEmail)) {
					matchingApi = cachedApi;
					break;
				}
			}
		}

		if (null == matchingApi) {
			// No api for this user
			LOG.info(new StringBuilder().append("No Api for user : ").append(userId).append(" with email : ")
					.append(accountsEmail).append(" in ").append(cachedData.getRowCount()).append(" api forthis user")
					.toString());
			return null; // early break;
		}
		this.checkApiAcces(matchingApi);

		return matchingApi.getApiCredentialId();
	}

	private void checkApiAcces(final ApiTableRowData apiRow) {
		if (null != apiRow.getApiCredentialId() && !ACCESS.check(new ReadApiPermission(apiRow.getApiCredentialId()))) {
			super.throwAuthorizationFailed();
		}
		if (null == apiRow.getApiCredentialId()) {
			LOG.warn(new StringBuffer().append("No API Id in DataBase for user :").append(apiRow.getUserId())
					.append(" and account's email : ").append(apiRow.getAccountEmail()).toString());
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuffer().append("API id : ").append(apiRow.getApiCredentialId())
						.append(" for user :").append(apiRow.getUserId()).append(" and account's email : ")
						.append(apiRow.getAccountEmail()).toString());
			}
		}
	}

	private void clearCaches(final ApiFormData formData) {
		this.dataCache.clearCache(formData.getApiCredentialId());
		this.dataCacheByUserId.clearCache(formData.getUserId());
	}

	@Override
	public Set<String> getAllUserId() {
		// FIXME Djer13 ByPassing userPermission security. Is it required ?
		// if (!ACCESS.check(new ReadApiPermission())) {
		// throw new VetoException(TEXTS.get("AuthorizationFailed"));
		// }

		final Set<String> result = new HashSet<>();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading all userId");
		}

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
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading all datas (only Binary Google Data)");
		}

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

	private boolean isAfterCreatAddTenantId() {
		return DatabaseHelper.get().isColumnExists(PatchManageMicrosoftCalendars.PATCHED_TABLE,
				PatchManageMicrosoftCalendars.PATCHED_ADDED_COLUMN_TENANT_ID);
	}

	@Override
	public boolean isOwn(final Long apiCredentialId) {
		final Long apiCredentialOwner = this.getOwner(apiCredentialId);

		if (null == apiCredentialOwner) {
			LOG.error(new StringBuffer().append("ApiCrentialId ").append(apiCredentialId)
					.append(" as NO owner (user_id)").toString());
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
