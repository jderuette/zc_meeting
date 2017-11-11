package org.zeroclick.meeting.client.common;

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

		rows.add(new LookupRow<>(15, TEXTS.get("zc.meeting.duration.15")));
		rows.add(new LookupRow<>(30, TEXTS.get("zc.meeting.duration.30")));
		rows.add(new LookupRow<>(45, TEXTS.get("zc.meeting.duration.45")));
		rows.add(new LookupRow<>(60, TEXTS.get("zc.meeting.duration.60")));
		rows.add(new LookupRow<>(120, TEXTS.get("zc.meeting.duration.120")));

		return rows;
	}

	public ILookupRow<Integer> getDataById(final Integer searchedId) {
		final List<? extends ILookupRow<Integer>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<Integer>> durationIt = datas.iterator();
		while (durationIt.hasNext()) {
			final ILookupRow<Integer> data = durationIt.next();
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
