package org.zeroclick.meeting.shared.calendar;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;

@TunnelToServer
public interface IApiService extends IService {

	ApiTablePageData getApiTableData(SearchFilter filter);

	ApiTablePageData getApiTableData(boolean displayAllForAdmin);

	ApiTablePageData getApis(Long userId);

	ApiTableRowData getApi(Long apiCredentialId);

	ApiFormData prepareCreate(ApiFormData formData);

	ApiFormData create(ApiFormData formData);

	ApiFormData create(ApiFormData input, Boolean sendNotifications);

	ApiFormData load(ApiFormData formData);

	ApiFormData load(Long apiId);

	ApiFormData store(ApiFormData formData);

	ApiFormData storeAccountEmail(ApiFormData formData, String accountEmail);

	void delete(ApiFormData formData);

	Set<String> getAllUserId();

	Collection<ApiFormData> loadGoogleData();

	boolean isOwn(Long apiCredentialId);

	boolean isRelated(Long apiCredentialId);

	Long getApiIdByAccessToken(Long userId, String accessToken);

}
