package org.oneclick.configuration.shared.api;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IProviderLookupService extends ILookupService<Integer> {
}
