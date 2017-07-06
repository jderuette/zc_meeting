package org.zeroclick.meeting.shared.event;

import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

@TunnelToServer
public interface IKnowEmailLookupService extends ILookupService<String> {
}
