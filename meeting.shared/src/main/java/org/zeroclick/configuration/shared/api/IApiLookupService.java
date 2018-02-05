package org.zeroclick.configuration.shared.api;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IApiLookupService extends ILookupService<Long> {
}
