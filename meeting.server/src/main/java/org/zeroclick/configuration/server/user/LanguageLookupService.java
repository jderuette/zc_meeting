package org.zeroclick.configuration.server.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.server.services.lookup.AbstractLookupService;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.zeroclick.configuration.shared.user.ILanguageLookupService;

public class LanguageLookupService extends AbstractLookupService<String> implements ILanguageLookupService {

	protected List<LookupRow<String>> createLookupRows() {
		final List<LookupRow<String>> rows = new ArrayList<>();

		rows.add(new LookupRow<>("fr", TEXTS.get("zc.user.language.fr")));
		rows.add(new LookupRow<>("en", TEXTS.get("zc.user.language.en")));

		return rows;
	}

	@Override
	public List<? extends ILookupRow<String>> getDataByKey(final ILookupCall<String> call) {
		final List<LookupRow<String>> rows = this.createLookupRows();
		final Iterator<LookupRow<String>> itRows = rows.iterator();

		while (itRows.hasNext()) {
			final LookupRow<String> row = itRows.next();

			if (null != call.getKey() && !row.getKey().contains(call.getKey())) {
				itRows.remove();
			}
		}
		return rows;
	}

	@Override
	public List<? extends ILookupRow<String>> getDataByRec(final ILookupCall<String> call) {
		// TODO [djer] is rec = all ? .
		return this.getDataByAll(call);
	}

	@Override
	public List<? extends ILookupRow<String>> getDataByText(final ILookupCall<String> call) {
		final List<LookupRow<String>> rows = this.createLookupRows();
		final Iterator<LookupRow<String>> itRows = rows.iterator();

		while (itRows.hasNext()) {
			final LookupRow<String> row = itRows.next();

			if (null != call.getText() && !row.getText().contains(call.getText())) {
				itRows.remove();
			}
		}
		return rows;
	}

	@Override
	public List<? extends ILookupRow<String>> getDataByAll(final ILookupCall<String> call) {
		final List<LookupRow<String>> rows = this.createLookupRows();
		final Iterator<LookupRow<String>> itRows = rows.iterator();

		while (itRows.hasNext()) {
			final LookupRow<String> row = itRows.next();

			if (null != call.getKey() && !row.getKey().contains(call.getKey()) && null != call.getText()
					&& !row.getText().contains(call.getText())) {
				itRows.remove();
			}
		}
		return rows;
	}
}
