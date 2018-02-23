package org.zeroclick.common.params;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.AbstractFormDataCache;
import org.zeroclick.configuration.shared.params.CreateAppParamsPermission;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.params.ParamCreatedNotification;
import org.zeroclick.configuration.shared.params.ParamModifiedNotification;
import org.zeroclick.configuration.shared.params.UpdateAppParamsPermission;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateParamsTable;

public class AppParamsService extends AbstractCommonService implements IAppParamsService {

	private static final Logger LOG = LoggerFactory.getLogger(AppParamsService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	private final AbstractFormDataCache<Long, AppParamsFormData> dataCache = new AbstractFormDataCache<Long, AppParamsFormData>() {
		@Override
		public AppParamsFormData loadForCache(final Long paramId) {
			final AppParamsFormData appParamsFormData = new AppParamsFormData();
			appParamsFormData.getParamId().setValue(paramId);
			return AppParamsService.this.loadForCache(appParamsFormData);
		}
	};

	private final AbstractFormDataCache<String, AppParamsFormData> dataCacheByKey = new AbstractFormDataCache<String, AppParamsFormData>() {
		@Override
		public AppParamsFormData loadForCache(final String key) {
			final AppParamsFormData appParamsFormData = new AppParamsFormData();
			appParamsFormData.getKey().setValue(key);
			return AppParamsService.this.loadForCacheByKey(appParamsFormData);
		}
	};

	private ICache<Long, AppParamsFormData> getDataCache() {
		return this.dataCache.getCache();
	}

	private ICache<String, AppParamsFormData> getDataCacheByKey() {
		return this.dataCacheByKey.getCache();
	}

	private AppParamsFormData loadForCache(final AppParamsFormData formData) {
		// No permission check, public Data
		SQL.selectInto(
				SQLs.PARAMS_SELECT_WITH_CATEGORY + SQLs.PARAMS_SELECT_FILTER_ID + SQLs.PARAMS_SELECT_INTO_WITH_CATEGORY,
				formData);
		return formData;
	}

	private AppParamsFormData loadForCacheByKey(final AppParamsFormData formData) {
		// No permission check, public Data
		SQL.selectInto(SQLs.PARAMS_SELECT + SQLs.PARAMS_SELECT_FILTER_KEY + SQLs.PARAMS_SELECT_INTO, formData);
		return formData;
	}

	@Override
	public AppParamsTablePageData getAppParamsTableData(final SearchFilter filter) {
		final AppParamsTablePageData pageData = new AppParamsTablePageData();
		SQL.selectInto(SQLs.PARAMS_PAGE_SELECT + SQLs.PARAMS_PAGE_DATA_SELECT_INTO, new NVPair("page", pageData));
		return pageData;
	}

	@Override
	public AppParamsFormData prepareCreate(final AppParamsFormData formData) {
		super.checkPermission(new CreateAppParamsPermission());
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public AppParamsFormData create(final AppParamsFormData formData) {
		super.checkPermission(new CreateAppParamsPermission());
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Creating app_params with key : ").append(formData.getKey().getValue())
					.append(" and value : ").append(formData.getValue().getValue()).append("(category : ")
					.append(formData.getCategory().getValue()).append(")").toString());
		}
		// add a unique param id if necessary
		if (null == formData.getParamId().getValue()) {
			formData.getParamId().setValue(this.getNextId());
		}

		SQL.insert(SQLs.PARAMS_INSERT, formData);
		final AppParamsFormData storedData = this.store(formData, Boolean.TRUE);

		// final Set<String> notifiedUsers =
		// this.buildNotifiedUsers(storedData);
		// BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
		// new ParamCreatedNotification(storedData));

		BEANS.get(ClientNotificationRegistry.class).putForAllNodes(new ParamCreatedNotification(storedData));

		return storedData;
	}

	@Override
	public void create(final String key, final String value) {
		// permission checked by method with category parameter
		this.create(key, value, null);
	}

	@Override
	public void create(final String key, final String value, final String category) {
		// permission check done by create method
		final AppParamsFormData formData = new AppParamsFormData();
		formData.getKey().setValue(key);
		formData.getValue().setValue(value);
		formData.getCategory().setValue(category);

		this.create(formData);
	}

	@Override
	public AppParamsFormData load(final AppParamsFormData formData) {
		AppParamsFormData cachedData = this.getDataCache().get(formData.getParamId().getValue());
		if (null == cachedData) {
			// avoid NPE
			cachedData = formData;
		}
		return cachedData;
	}

	private AppParamsFormData load(final String key) {
		// No permission check, public Data
		AppParamsFormData cachedData = this.getDataCacheByKey().get(key);
		if (null == cachedData) {
			// avoid NPE
			cachedData = new AppParamsFormData();
		}
		return cachedData;
	}

	@Override
	public String getValue(final String key) {
		// No permission check, public Data
		String value = null;
		final AppParamsFormData cachedData = this.load(key);
		value = null == cachedData.getValue().getValue() ? null : (String) cachedData.getValue().getValue();
		return value;
	}

	protected Long getId(final String key) {
		// No permission check, public Data
		final AppParamsFormData cachedData = this.load(key);
		return cachedData.getParamId().getValue();
	}

	@Override
	public Boolean isKeyExists(final String key) {
		// No permission check, public Data
		final AppParamsFormData cachedData = this.load(key);
		final Boolean found = null != cachedData.getParamId().getValue();

		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("App_params : ").append(key + " found ? ").append(found).toString());
		}
		return found;
	}

	protected AppParamsFormData store(final AppParamsFormData formData, final Boolean forCreation) {
		super.checkPermission(new UpdateAppParamsPermission());
		SQL.update(SQLs.PARAMS_UPDATE_WITH_CATEGORY, formData);

		if (!forCreation) {
			this.sendModifiedNotifications(formData);
		}

		this.clearCaches(formData);

		return formData;
	}

	@Override
	public AppParamsFormData store(final AppParamsFormData formData) {
		// permission check done by store method
		return this.store(formData, Boolean.FALSE);
	}

	@Override
	public void store(final String key, final String value) {
		// permission check done by store method
		final AppParamsFormData formData = new AppParamsFormData();
		formData.getKey().setValue(key);
		formData.getValue().setValue(value);

		this.store(formData);
	}

	@Override
	public void delete(final String key) {
		super.checkPermission(new UpdateAppParamsPermission());
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder().append("Deleting app_params key : ").append(key).toString());
		}
		final AppParamsFormData appParamsFormData = this.load(key);

		SQL.update(SQLs.PARAMS_DELETE, appParamsFormData);
		this.clearCaches(appParamsFormData);
	}

	protected Long getNextId() {
		return SQL.getSequenceNextval(PatchCreateParamsTable.APP_PARAMS_ID_SEQ);
	}

	private void clearCaches(final AppParamsFormData formData) {
		this.dataCache.clearCache(formData.getParamId().getValue());
		this.dataCacheByKey.clearCache(formData.getKey().getValue());
	}

	// private Set<String> buildNotifiedUsers(final AppParamsFormData formData)
	// {
	// // Notify Users for EventTable update
	// // TODO Djer13 notify ALL Users ? (as this modified/created param may
	// // impact them)
	// final AccessControlService acs = BEANS.get(AccessControlService.class);
	//
	// final Set<String> notifiedUsers = new HashSet<>();
	// notifiedUsers.addAll(acs.getUserNotificationIds(acs.getZeroClickUserIdOfCurrentSubject()));
	// return notifiedUsers;
	// }

	private void sendModifiedNotifications(final AppParamsFormData formData) {
		// final Set<String> notifiedUsers = this.buildNotifiedUsers(formData);
		// BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
		// new ParamModifiedNotification(formData));

		BEANS.get(ClientNotificationRegistry.class).putForAllSessions(new ParamModifiedNotification(formData));
	}

}
