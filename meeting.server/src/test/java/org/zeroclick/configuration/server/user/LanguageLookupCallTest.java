package org.zeroclick.configuration.server.user;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.user.LanguageLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class LanguageLookupCallTest {

	protected LanguageLookupCall createLookupCall() {
		return new LanguageLookupCall();
	}

	@Test
	public void testLookupByAll() {
		LanguageLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		LanguageLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		LanguageLookupCall call = createLookupCall();
		// TODO [djer] fill call
		List<? extends ILookupRow<String>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
