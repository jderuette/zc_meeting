package org.zeroclick.configuration.shared.api;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.api.ApiTablePageData;

@TunnelToServer
public interface IApiOldService extends IService {

	ApiTablePageData getApiTableData(SearchFilter filter);
}
