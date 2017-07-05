package org.zeroclick.meeting.shared.event;

import java.util.Map;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface IEventService extends IService {

	EventTablePageData getEventTableData(SearchFilter filter);

	AbstractTablePageData getEventProcessedTableData(SearchFilter filter);

	EventFormData prepareCreate(EventFormData formData);

	EventFormData create(EventFormData formData);

	EventFormData load(EventFormData formData);

	RejectEventFormData load(RejectEventFormData formData);

	EventFormData store(EventFormData formData);

	boolean isOwn(Long eventId);

	boolean isRecipient(Long eventId);

	EventFormData storeNewState(RejectEventFormData formData);

	/**
	 * Get pending events for current connected user
	 * 
	 * @return a Map<UserId, NbEvent>
	 */
	Map<Long, Integer> getUsersWithPendingMeeting();

}
