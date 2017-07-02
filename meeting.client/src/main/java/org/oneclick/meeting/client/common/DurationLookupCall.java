package org.oneclick.meeting.client.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class DurationLookupCall extends LocalLookupCall<Integer> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<LookupRow<Integer>> execCreateLookupRows() {
		final List<LookupRow<Integer>> rows = new ArrayList<>();

		rows.add(new LookupRow<>(15, TEXTS.get("15Min")));
		rows.add(new LookupRow<>(30, TEXTS.get("30Min")));
		rows.add(new LookupRow<>(60, TEXTS.get("1Hour")));
		rows.add(new LookupRow<>(120, TEXTS.get("2Hours")));

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
