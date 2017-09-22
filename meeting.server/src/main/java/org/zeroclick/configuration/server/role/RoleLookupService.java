package org.zeroclick.configuration.server.role;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.rt.server.jdbc.lookup.AbstractSqlLookupService;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.zeroclick.configuration.shared.role.IRoleLookupService;
import org.zeroclick.meeting.server.sql.SQLs;

public class RoleLookupService extends AbstractSqlLookupService<Long> implements IRoleLookupService {

	@Override
	protected String getConfiguredSqlSelect() {
		return SQLs.ROLE_LOOKUP;
	}

	@Override
	public List<ILookupRow<Long>> getDataByAll(final ILookupCall<Long> call) {
		final List<ILookupRow<Long>> rawData = super.getDataByAll(call);

		if (null != rawData && rawData.size() > 0) {
			final Iterator<ILookupRow<Long>> itRows = rawData.iterator();

			while (itRows.hasNext()) {
				final ILookupRow<Long> row = itRows.next();
				row.withText(TEXTS.getWithFallback(row.getText(), row.getText()));
			}
		}
		// TODO Djer create a new list instead of modifying one create by parent
		// method ?
		return rawData;
	}
}
