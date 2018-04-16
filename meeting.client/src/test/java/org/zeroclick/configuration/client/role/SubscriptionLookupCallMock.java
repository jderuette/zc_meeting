/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.configuration.client.role;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.configuration.shared.role.ISubscriptionLookupService;
import org.zeroclick.testing.GenericTestingLookupCall;

/**
 * @author djer
 *
 */
public class SubscriptionLookupCallMock extends GenericTestingLookupCall<Long> implements ISubscriptionLookupService {

	public SubscriptionLookupCallMock() {
		this.loadDefaultValues();
	}

	private void loadDefaultValues() {
		final List<ILookupRow<Long>> defaultRows = new ArrayList<>();
		final ILookupRow<Long> row1 = new LookupRow<>(3L, "Free");
		defaultRows.add(row1);

		final ILookupRow<Long> row2 = new LookupRow<>(4L, "Pro");
		defaultRows.add(row2);

		final ILookupRow<Long> row3 = new LookupRow<>(5L, "Business");
		defaultRows.add(row3);

		this.setRows(defaultRows);

	}
}
