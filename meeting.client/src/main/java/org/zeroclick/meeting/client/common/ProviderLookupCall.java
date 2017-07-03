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

/**
 * @author djer
 *
 */
public class ProviderLookupCall extends LocalLookupCall<Integer> {
	private static final long serialVersionUID = 1L;

	@Override
	protected List<LookupRow<Integer>> execCreateLookupRows() {
		final List<LookupRow<Integer>> rows = new ArrayList<>();

		rows.add(new LookupRow<>(1, TEXTS.get("zc.api.provider.google")));
		rows.add(new LookupRow<>(2, TEXTS.get("zc.api.provider.testProvider")));

		return rows;
	}

	public ILookupRow<Integer> getDataById(final Integer searchedId) {
		final List<? extends ILookupRow<Integer>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<Integer>> it = datas.iterator();
		while (it.hasNext()) {
			final ILookupRow<Integer> data = it.next();
			if (data.getKey().equals(searchedId)) {
				return data; // early break
			}
		}
		return null;
	}

	public String getText(final Integer searchedId) {
		String label = "";
		final ILookupRow<Integer> data = this.getDataById(searchedId);
		if (null != data) {
			label = data.getText();
		}
		return label;
	}
}
