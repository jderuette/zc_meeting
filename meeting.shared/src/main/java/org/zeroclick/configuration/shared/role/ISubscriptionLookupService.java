package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface ISubscriptionLookupService extends ILookupService<Long> {
}
