package org.zeroclick.meeting.shared.calendar;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.api.ApiTablePageData;

@TunnelToServer
public interface IApiService extends IService {

	ApiTablePageData getApiTableData(SearchFilter filter);

	ApiFormData prepareCreate(ApiFormData formData);

	ApiFormData create(ApiFormData formData);

	ApiFormData load(ApiFormData formData);

	ApiFormData store(ApiFormData formData);

	void delete(ApiFormData formData);

	Set<String> getAllUserId();

	Collection<ApiFormData> loadGoogleData();

	boolean isOwn(Long apiCredentialId);

	boolean isRelated(Long apiCredentialId);
}
