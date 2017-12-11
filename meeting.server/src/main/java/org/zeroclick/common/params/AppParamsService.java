package org.zeroclick.common.params;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.configuration.shared.params.CreateAppParamsPermission;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.params.ParamCreatedNotification;
import org.zeroclick.configuration.shared.params.ParamModifiedNotification;
import org.zeroclick.configuration.shared.params.ReadAppParamsPermission;
import org.zeroclick.configuration.shared.params.UpdateAppParamsPermission;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateParamsTable;

public class AppParamsService extends CommonService implements IAppParamsService {

	private static final Logger LOG = LoggerFactory.getLogger(AppParamsService.class);

	@Override
	public AppParamsTablePageData getAppParamsTableData(final SearchFilter filter) {
		final AppParamsTablePageData pageData = new AppParamsTablePageData();
		SQL.selectInto(SQLs.PARAMS_PAGE_SELECT + SQLs.PARAMS_PAGE_DATA_SELECT_INTO, new NVPair("page", pageData));
		return pageData;
	}

	@Override
	public void create(final String key, final String value) {
		LOG.debug("Creating app_params with key : " + key + " and value : " + value);

		final Long paramId = this.getNextId();
		SQL.update(SQLs.PARAMS_INSERT, new NVPair("paramId", paramId), new NVPair("key", key),
				new NVPair("value", value));

		SQL.update(SQLs.PARAMS_UPDATE, new NVPair("paramId", paramId), new NVPair("key", key),
				new NVPair("value", value));
	}

	@Override
	public void create(final String key, final String value, final String category) {
		LOG.debug("Creating app_params with key : " + key + " and value : " + value + "(category : " + category + ")");
		final Long paramId = this.getNextId();

		SQL.update(SQLs.PARAMS_INSERT, new NVPair("paramId", paramId));

		SQL.update(SQLs.PARAMS_UPDATE_WITH_CATEGORY, new NVPair("paramId", paramId), new NVPair("key", key),
				new NVPair("value", value), new NVPair("category", category));
	}

	@Override
	public String getValue(final String key) {
		final Object value = this.getData(key, 2);
		return null == value ? null : (String) value;
	}

	protected Long getId(final String key) {
		final Object paramId = this.getData(key, 0);
		return null == paramId ? null : (Long) paramId;
	}

	@Override
	public void store(final String key, final String value) {
		LOG.debug("Storing app_params with key : " + key + " and value : " + value);

		final Long existingId = this.getId(key);
		if (null == existingId) {
			this.create(key, value);
		} else {
			LOG.debug("Updatting app_params with id : " + existingId + " with key : " + key + " and value : " + value);
			SQL.update(SQLs.PARAMS_UPDATE, new NVPair("key", key), new NVPair("value", value));
		}
	}

	protected Object getData(final String key, final Integer columnNumber) {
		LOG.debug("Searching app_params for key : " + key);
		Object paramValue = null;
		final Object[][] datas = SQL.select(SQLs.PARAMS_SELECT + SQLs.PARAMS_SELECT_FILTER_KEY, new NVPair("key", key));

		if (null != datas && datas.length == 1) {
			paramValue = datas[0][columnNumber];
		} else if (null != datas && datas.length > 1) {
			LOG.warn("Multiple values for key : " + key + ". Returning the first one");
			paramValue = datas[0][columnNumber];
		} else {
			LOG.warn("No value for key : " + key + ".");
		}

		return paramValue;
	}

	@Override
	public void delete(final String key) {
		LOG.debug("Deleting app_params key : " + key);
		SQL.update(SQLs.PARAMS_DELETE, new NVPair("key", key));
	}

	protected Long getNextId() {
		return SQL.getSequenceNextval(PatchCreateParamsTable.APP_PARAMS_ID_SEQ);
	}

	@Override
	public AppParamsFormData prepareCreate(final AppParamsFormData formData) {
		if (!ACCESS.check(new CreateAppParamsPermission())) {
			super.throwAuthorizationFailed();
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public AppParamsFormData create(final AppParamsFormData formData) {
		if (!ACCESS.check(new CreateAppParamsPermission())) {
			super.throwAuthorizationFailed();
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
	public AppParamsFormData load(final AppParamsFormData formData) {
		if (!ACCESS.check(new ReadAppParamsPermission())) {
			super.throwAuthorizationFailed();
		}
		SQL.selectInto(SQLs.PARAMS_SELECT_WITH_CATEGORY + SQLs.PARAMS_SELECT_FILTER_ID + SQLs.PARAMS_SELECT_INTO,
				formData);
		return formData;
	}

	protected AppParamsFormData store(final AppParamsFormData formData, final Boolean forCreation) {
		if (!ACCESS.check(new UpdateAppParamsPermission())) {
			super.throwAuthorizationFailed();
		}
		SQL.update(SQLs.PARAMS_UPDATE_WITH_CATEGORY, formData);

		if (!forCreation) {
			this.sendModifiedNotifications(formData);
		}
		return formData;
	}

	@Override
	public AppParamsFormData store(final AppParamsFormData formData) {
		if (!ACCESS.check(new UpdateAppParamsPermission())) {
			super.throwAuthorizationFailed();
		}
		this.store(formData, Boolean.FALSE);

		return formData;

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
