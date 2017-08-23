package org.zeroclick.configuration.shared.user;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface ILanguageLookupService extends ILookupService<String> {
}
