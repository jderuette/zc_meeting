package org.zeroclick.configuration.shared.venue;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IVenueLookupService extends ILookupService<String> {
}
