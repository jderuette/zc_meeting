package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IRoleTypeLookupService extends ILookupService<String> {

	public static final String TYPE_BUSINESS = "business";
	public static final String TYPE_SUBSCRIPTION = "subscription";

}
