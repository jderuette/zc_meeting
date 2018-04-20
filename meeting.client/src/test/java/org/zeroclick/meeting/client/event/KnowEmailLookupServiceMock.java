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
package org.zeroclick.meeting.client.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.meeting.shared.event.IKnowEmailLookupService;
import org.zeroclick.testing.GenericTestingLookupCall;

/**
 * @author djer
 *
 */
public class KnowEmailLookupServiceMock extends GenericTestingLookupCall<String> implements IKnowEmailLookupService {

	public KnowEmailLookupServiceMock() {
		this.loadDefaultValues();
	}

	private void loadDefaultValues() {
		final List<ILookupRow<String>> defaultRows = new ArrayList<>();
		final ILookupRow<String> row1 = new LookupRow<>("UnitTestKnown1@someProvider42.org",
				"UnitTestKnown1@someProvider42.org");
		defaultRows.add(row1);

		final ILookupRow<String> row2 = new LookupRow<>("UnitTestKnown2@someProvider42.org",
				"UnitTestKnown2@someProvider42.org");
		defaultRows.add(row2);

		final ILookupRow<String> row3 = new LookupRow<>("UnitTestKnown3@someProvider42.org",
				"UnitTestKnown3@someProvider42.org");
		defaultRows.add(row3);

		this.setRows(defaultRows);
	}
}
