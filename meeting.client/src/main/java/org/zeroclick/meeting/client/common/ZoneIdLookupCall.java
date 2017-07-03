package org.zeroclick.meeting.client.common;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

public class ZoneIdLookupCall extends LocalLookupCall<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected List<LookupRow<String>> execCreateLookupRows() {
		final List<LookupRow<String>> rows = new ArrayList<>();
		
		final Set<String> existingZoneids = ZoneId.getAvailableZoneIds();

		for (final String zoneId : existingZoneids) {
			rows.add(new LookupRow<>(zoneId, zoneId));
		}

		return rows;
	}

	public ILookupRow<String> getDataById(final String searchedId) {
		final List<? extends ILookupRow<String>> datas = this.getDataByAll();

		final Iterator<? extends ILookupRow<String>> it = datas.iterator();
		while (it.hasNext()) {
			final ILookupRow<String> data = it.next();
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
