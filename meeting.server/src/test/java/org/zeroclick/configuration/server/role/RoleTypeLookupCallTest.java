package org.zeroclick.configuration.server.role;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.role.RoleTypeLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class RoleTypeLookupCallTest {

	protected RoleTypeLookupCall createLookupCall() {
		return new RoleTypeLookupCall();
	}

	@Test
	public void testLookupByAll() {
		RoleTypeLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		RoleTypeLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		RoleTypeLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
