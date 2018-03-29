package org.zeroclick.meeting.shared.event;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@TunnelToServer
public interface IEventService extends IService {

	EventTablePageData getEventTableData(SearchFilter filter);

	AbstractTablePageData getEventAskedTableData(SearchFilter filter);

	AbstractTablePageData getEventProcessedTableData(SearchFilter filter);

	AbstractTablePageData getEventAdminTableData(SearchFilter filter);

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

	/**
	 * Get pending events for a specific userId
	 *
	 * @param forUserId
	 *            : userId to get pending meeting
	 *
	 * @return a Map<UserId, NbEvent>
	 */
	Map<Long, Integer> getUsersWithPendingMeeting(Long forUserId);

	Set<String> getKnowEmail(ILookupCall<String> call);

	Set<String> getKnowEmailByKey(ILookupCall<String> call);

	/**
	 *
	 * @param state
	 * @return UserId => nbEvent
	 */
	Map<Long, Integer> getNbEventsByUser(String state);

	/**
	 *
	 * @param state
	 * @param onlyAsOrganizer
	 * @return UserId => nbEvent
	 */
	Map<Long, Integer> getNbEventsByUser(String state, Boolean onlyAsOrganizer);

	/**
	 *
	 * @param state
	 * @param onlyAsOrganizer
	 * @param forUserId
	 *            : userId to get pending meeting
	 * @return UserId => nbEvent
	 */
	public Map<Long, Integer> getNbEventsByUser(final String state, final Boolean onlyAsOrganizer, Long forUserId);

	/**
	 * Used for data migration
	 */
	void migrateDurationlookupToCodeType();

}
