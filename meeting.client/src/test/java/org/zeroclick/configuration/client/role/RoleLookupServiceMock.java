package org.zeroclick.configuration.client.role;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.configuration.shared.role.IRoleLookupService;
import org.zeroclick.testing.GenericTestingLookupCall;

/**
 * @author djer A mock Service for UnitTest
 */
public class RoleLookupServiceMock extends GenericTestingLookupCall<Long> implements IRoleLookupService {

	public RoleLookupServiceMock() {
		this.loadDefaultValues();
	}

	private void loadDefaultValues() {
		final List<ILookupRow<Long>> defaultRows = new ArrayList<>();
		final ILookupRow<Long> row1 = new LookupRow<>(1L, "Role1");
		defaultRows.add(row1);

		final ILookupRow<Long> row2 = new LookupRow<>(2L, "Role2");
		defaultRows.add(row2);

		this.setRows(defaultRows);

	}
}
