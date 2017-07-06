package org.zeroclick.meeting.server.event;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.meeting.shared.event.KnowEmailLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class KnowEmailLookupCallTest {

	protected KnowEmailLookupCall createLookupCall() {
		return new KnowEmailLookupCall();
	}

	@Test
	public void testLookupByAll() {
		KnowEmailLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		KnowEmailLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		KnowEmailLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
