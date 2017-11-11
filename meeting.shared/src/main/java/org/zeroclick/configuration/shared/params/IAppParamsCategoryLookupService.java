package org.zeroclick.configuration.shared.params;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IAppParamsCategoryLookupService extends ILookupService<String> {
}
