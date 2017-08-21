package org.zeroclick.meeting.client.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class SlotLookupCall extends LocalLookupCall<Integer> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<LookupRow<Integer>> execCreateLookupRows() {
		final List<LookupRow<Integer>> rows = new ArrayList<>();

		rows.add(new LookupRow<>(1, TEXTS.get("zc.meeting.slot.1")));
		rows.add(new LookupRow<>(2, TEXTS.get("zc.meeting.slot.2")));
		rows.add(new LookupRow<>(3, TEXTS.get("zc.meeting.slot.3")));
		rows.add(new LookupRow<>(4, TEXTS.get("zc.meeting.slot.4")));

		return rows;
	}

	public ILookupRow<Integer> getDataById(final Integer searchedId) {
		final List<? extends ILookupRow<Integer>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<Integer>> slotIt = datas.iterator();
		while (slotIt.hasNext()) {
			final ILookupRow<Integer> data = slotIt.next();
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
