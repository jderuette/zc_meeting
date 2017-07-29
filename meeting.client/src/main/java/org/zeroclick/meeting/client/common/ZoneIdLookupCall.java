package org.zeroclick.meeting.client.common;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.meeting.client.ClientSession;

public class ZoneIdLookupCall extends LocalLookupCall<String> {

	private static final long serialVersionUID = 1L;

	private final Boolean displayFullText = Boolean.TRUE;
	private final Boolean displayCustomText = Boolean.TRUE;

	@Override
	protected List<LookupRow<String>> execCreateLookupRows() {
		// TODO Djer : cache rows because they not change during runtime.
		final List<LookupRow<String>> rows = new ArrayList<>();

		final Set<String> existingZoneids = ZoneId.getAvailableZoneIds();

		for (final String zoneId : existingZoneids) {
			@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
			final StringBuilder builder = new StringBuilder();
			if (this.displayFullText) {
				Locale currentUserLocale = ClientSession.get().getLocale();
				if (null == currentUserLocale) {
					currentUserLocale = Locale.ENGLISH;
				}

				final String zoneDisplayName = ZoneId.of(zoneId).getDisplayName(TextStyle.FULL_STANDALONE,
						currentUserLocale);

				builder.append(zoneId).append(" (").append(zoneDisplayName).append(')');
			}
			if (this.displayCustomText) {
				if (zoneId.equals("Europe/Paris")) {
					builder.append(" France");
				}
			}

			rows.add(new LookupRow<>(zoneId, builder.toString()));
		}

		return rows;
	}

	@Override
	protected Pattern createSearchPattern(String s) {
		if (s != null && !s.startsWith(this.getWildcard())) {
			s = this.getWildcard() + s;
		}
		return super.createSearchPattern(s);
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
