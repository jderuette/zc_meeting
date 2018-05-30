package org.zeroclick.meeting.shared.event.involevment;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@TunnelToServer
public interface IInvolvementService extends IService {

	InvolvementTablePageData getInvolevmentTableData(SearchFilter filter);

	InvolvementFormData prepareCreate(InvolvementFormData formData);

	InvolvementFormData create(InvolvementFormData formData);

	InvolvementFormData load(InvolvementFormData formData);

	InvolvementFormData store(InvolvementFormData formData);

	Boolean isGuest(Long userId);

	void updateStatusAccepted(Long eventId, Long userId);

	void updateStatusRefused(Long eventId, Long userId, String reason);

	InvolvementFormData getOrganizer(Long eventId);

	InvolvementTablePageData getParticipants(Long eventId);
}
