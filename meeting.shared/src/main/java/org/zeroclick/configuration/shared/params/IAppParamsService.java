package org.zeroclick.configuration.shared.params;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.common.params.AppParamsFormData;
import org.zeroclick.common.params.AppParamsTablePageData;

@TunnelToServer
public interface IAppParamsService extends IService {

	public static final String KEY_DATA_VERSION = "dataVersion";
	public static final String APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX = "callTracker.events.maxSuccesiveCall";
	public static final String APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION = "callTracker.events.ttl";
	public static final String APP_PARAM_KEY_TOS_URL = "tos.url";

	AppParamsTablePageData getAppParamsTableData(SearchFilter filter);

	void create(String key, String value);

	void create(String key, String value, String category);

	String getValue(String key);

	Boolean isKeyExists(String key);

	void store(String key, String value);

	void delete(String key);

	AppParamsFormData prepareCreate(AppParamsFormData formData);

	AppParamsFormData create(AppParamsFormData formData);

	AppParamsFormData load(AppParamsFormData formData);

	AppParamsFormData store(AppParamsFormData formData);
}
