package org.zeroclick.configuration.shared.api;

import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

public class ApiLookupCall extends LookupCall<Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends ILookupService<Long>> getConfiguredService() {
		return IApiLookupService.class;
	}
}
