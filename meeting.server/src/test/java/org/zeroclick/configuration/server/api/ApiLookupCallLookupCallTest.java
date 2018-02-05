package org.zeroclick.configuration.server.api;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.api.ApiLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class ApiLookupCallLookupCallTest {

	protected ApiLookupCall createLookupCall() {
		return new ApiLookupCall();
	}

	@Test
	public void testLookupByAll() {
		ApiLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		ApiLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		ApiLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<Long>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
