package org.zeroclick.meeting.shared.event.externalevent;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface IExternalEventService extends IService {

	ExternalEventTablePageData getExternalEventTableData(SearchFilter filter);

	ExternalEventFormData prepareCreate(ExternalEventFormData formData);

	ExternalEventFormData create(ExternalEventFormData formData);

	ExternalEventFormData load(ExternalEventFormData formData);

	ExternalEventFormData store(ExternalEventFormData formData);
}
