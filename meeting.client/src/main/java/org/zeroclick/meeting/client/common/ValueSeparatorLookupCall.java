package org.zeroclick.meeting.client.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class ValueSeparatorLookupCall extends LocalLookupCall<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<LookupRow<String>> execCreateLookupRows() {
		final List<LookupRow<String>> rows = new ArrayList<>();

		rows.add(new LookupRow<>(";", ";"));
		rows.add(new LookupRow<>(",", ","));
		rows.add(new LookupRow<>("\n", TEXTS.get("zc.meeting.event.importEmails.separator.OneByLine")));
		/*
		 * rows.add(new LookupRow<>("\r\n",
		 * TEXTS.get("zc.meeting.event.importEmails.separator.windowsNewLine")))
		 * ; rows.add(new LookupRow<>("\r",
		 * TEXTS.get("zc.meeting.event.importEmails.separator.linuxNewLine")));
		 * rows.add(new LookupRow<>("\n",
		 * TEXTS.get("zc.meeting.event.importEmails.separator.macNewLine")));
		 */

		return rows;
	}

	public ILookupRow<String> getDataById(final String searchedId) {
		final List<? extends ILookupRow<String>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<String>> durationIt = datas.iterator();
		while (durationIt.hasNext()) {
			final ILookupRow<String> data = durationIt.next();
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
