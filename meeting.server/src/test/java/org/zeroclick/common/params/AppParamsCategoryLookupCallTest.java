package org.zeroclick.common.params;

import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zeroclick.configuration.shared.params.AppParamsCategoryLookupCall;

@RunWithSubject("anonymous")
@RunWith(ServerTestRunner.class)
@RunWithServerSession(AbstractServerSession.class)
public class AppParamsCategoryLookupCallTest {

	protected AppParamsCategoryLookupCall createLookupCall() {
		return new AppParamsCategoryLookupCall();
	}

	@Test
	public void testLookupByAll() {
		final AppParamsCategoryLookupCall call = this.createLookupCall();
		// TODO [djer] fill call
		final List<? extends ILookupRow<String>> data = call.getDataByAll();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByKey() {
		final AppParamsCategoryLookupCall call = this.createLookupCall();
		// TODO [djer] fill call
		final List<? extends ILookupRow<String>> data = call.getDataByKey();
		// TODO [djer] verify data
	}

	@Test
	public void testLookupByText() {
		final AppParamsCategoryLookupCall call = this.createLookupCall();
		// TODO [djer] fill call
		final List<? extends ILookupRow<String>> data = call.getDataByText();
		// TODO [djer] verify data
	}

	// TODO [djer] add test cases
}
