package org.zeroclick.meeting.shared.event;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface IEventAskedService extends IService {

	EventAskedTablePageData getEventAskedTableData(SearchFilter filter);
}
