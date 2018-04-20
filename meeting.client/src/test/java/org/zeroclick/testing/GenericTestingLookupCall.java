package org.zeroclick.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;

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

/**
 * @author djer strongly inspired by @see TestingLookupService
 * @param <T>
 */
public class GenericTestingLookupCall<T> implements ILookupService<T> {

	private List<ILookupRow<T>> rows = new ArrayList<>();

	public GenericTestingLookupCall() {
	}

	public List<ILookupRow<T>> getRows() {
		return CollectionUtility.arrayList(this.rows);
	}

	public void setRows(final List<ILookupRow<T>> rows) {
		this.rows = CollectionUtility.arrayList(rows);
	}

	@Override
	public List<ILookupRow<T>> getDataByKey(final ILookupCall<T> call) {
		final List<ILookupRow<T>> list = new ArrayList<>();
		final Object key = call.getKey();
		if (key != null) {
			for (final ILookupRow<T> row : this.getRows()) {
				if (key.equals(row.getKey())) {
					list.add(row);
				}
			}
		}
		return list;
	}

	@Override
	public List<ILookupRow<T>> getDataByRec(final ILookupCall<T> call) {
		final List<ILookupRow<T>> list = new ArrayList<>();
		final Object parentKey = call.getRec();
		if (parentKey == null) {
			for (final ILookupRow<T> row : this.getRows()) {
				if (row.getParentKey() == null) {
					list.add(row);
				}
			}
		} else {
			for (final ILookupRow<T> row : this.getRows()) {
				if (row.getParentKey() == parentKey) {
					list.add(row);
				}
			}
		}
		return list;
	}

	@Override
	public List<ILookupRow<T>> getDataByText(final ILookupCall<T> call) {
		final List<ILookupRow<T>> list = new ArrayList<>();
		final Pattern p = createLowerCaseSearchPattern(call.getText());
		for (final ILookupRow<T> row : this.getRows()) {
			if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
				list.add(row);
			}
		}
		return list;
	}

	@Override
	public List<ILookupRow<T>> getDataByAll(final ILookupCall<T> call) {
		final List<ILookupRow<T>> list = new ArrayList<>();
		final Pattern p = createLowerCaseSearchPattern(call.getAll());
		for (final ILookupRow<T> row : this.getRows()) {
			if (row.getText() != null && p.matcher(row.getText().toLowerCase()).matches()) {
				list.add(row);
			}
		}
		return list;
	}

	public static Pattern createLowerCaseSearchPattern(String s) {
		if (s == null) {
			s = "";
		}
		s = s.toLowerCase();
		if (s.indexOf('*') < 0) {
			s = s + "*";
		}
		return Pattern.compile(StringUtility.toRegExPattern(s), Pattern.DOTALL);
	}
}
