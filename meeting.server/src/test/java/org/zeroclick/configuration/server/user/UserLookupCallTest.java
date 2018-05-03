package org.zeroclick.configuration.server.user;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.user.UserLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class UserLookupCallTest {

	protected UserLookupCall createLookupCall() {
		return new UserLookupCall();
	}

	@Test
	public void testLookupByAll() {
		UserLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		UserLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		UserLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
