package org.zeroclick.meeting.server.event;

import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.shared.event.EventAskedTablePageData;
import org.zeroclick.meeting.shared.event.IEventAskedService;

public class EventAskedService implements IEventAskedService {

	@Override
	public EventAskedTablePageData getEventAskedTableData(SearchFilter filter) {
		EventAskedTablePageData pageData = new EventAskedTablePageData();
		// TODO [djer] fill pageData.
		return pageData;
	}
}
