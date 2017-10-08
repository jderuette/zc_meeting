package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class RoleAndSubscriptionLookupCall extends LookupCall<Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends ILookupService<Long>> getConfiguredService() {
		return IRoleAndSubscriptionLookupService.class;
	}
}
