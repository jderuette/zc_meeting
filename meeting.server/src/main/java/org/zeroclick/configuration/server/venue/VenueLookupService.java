package org.zeroclick.configuration.server.venue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.zeroclick.configuration.shared.venue.IVenueLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class VenueLookupService extends AbstractCombinedMultiSqlLookupService<String> implements IVenueLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();
		sqls.add(new SqlLookupConfiguration(SQLs.PARAMS_SELECT_FOR_SMART_FIELD + SQLs.PARAMS_SELECT_FILTER_CATEGORY
				+ SQLs.PARAMS_SELECT_FILTER_LOOKUP, 1, new NVPair("category", "venue"))
						.setAutoFirstWildcard(Boolean.TRUE));
		sqls.add(new SqlLookupConfiguration(SQLs.EVENT_SELECT_VENUE_LOOKUP, new NVPair("currentUser", currentUserId))
				.setAutoFirstWildcard(Boolean.TRUE));
		return sqls;
	}

	@Override
	protected Boolean getConfiguredRemoveDuplicate() {
		return Boolean.TRUE;
	}
}
