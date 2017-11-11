package org.zeroclick.configuration.server.role;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.role.RoleAndSubscriptionLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class RoleAndSubscriptionLookupCallTest {

	protected RoleAndSubscriptionLookupCall createLookupCall() {
		return new RoleAndSubscriptionLookupCall();
	}

	@Test
	public void testLookupByAll() {
		RoleAndSubscriptionLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		RoleAndSubscriptionLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		RoleAndSubscriptionLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
