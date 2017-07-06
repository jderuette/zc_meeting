package org.zeroclick.meeting.shared.event;

import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class KnowEmailLookupCall extends LookupCall<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends ILookupService<String>> getConfiguredService() {
		return IKnowEmailLookupService.class;
	}
}
