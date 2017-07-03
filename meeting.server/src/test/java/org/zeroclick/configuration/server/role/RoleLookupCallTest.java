package org.oneclick.configuration.server.role;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.runner.RunWith;
import org.oneclick.configuration.shared.role.RoleLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class RoleLookupCallTest {

	protected RoleLookupCall createLookupCall() {
		return new RoleLookupCall();
	}

	// TODO [djer] add test cases
}
