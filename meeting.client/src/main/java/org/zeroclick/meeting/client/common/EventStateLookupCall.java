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
package org.zeroclick.meeting.client.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.meeting.shared.Icons;

/**
 * @author djer
 *
 */
public class EventStateLookupCall extends LocalLookupCall<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<ILookupRow<String>> execCreateLookupRows() {
		final List<ILookupRow<String>> rows = new ArrayList<>();

		rows.add(new LookupRow<>("ASKED", TEXTS.get("zc.meeting.state.asked")).withIconId(Icons.Clock));
		rows.add(new LookupRow<>("ACCEPTED", TEXTS.get("zc.meeting.state.accepted")).withIconId(Icons.Checked)
				.withBackgroundColor("#4b8e34"));
		rows.add(new LookupRow<>("REFUSED", TEXTS.get("zc.meeting.state.refused")).withIconId(Icons.Remove)
				.withBackgroundColor("#db4a15"));

		return rows;
	}

	public ILookupRow<String> getDataById(final String searchedId) {
		final List<? extends ILookupRow<String>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<String>> eventStateIt = datas.iterator();
		while (eventStateIt.hasNext()) {
			final ILookupRow<String> data = eventStateIt.next();
			if (data.getKey().equals(searchedId)) {
				return data; // early break
			}
		}
		return null;
	}

	public String getText(final String searchedId) {
		String label = "";
		final ILookupRow<String> data = this.getDataById(searchedId);
		if (null != data) {
			label = data.getText();
		}
		return label;
	}

}
