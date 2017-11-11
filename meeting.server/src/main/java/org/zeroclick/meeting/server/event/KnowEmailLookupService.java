package org.zeroclick.meeting.server.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;
import org.zeroclick.meeting.shared.event.IKnowEmailLookupService;
import org.zeroclick.meeting.shared.security.AccessControlService;

public class KnowEmailLookupService extends AbstractCombinedMultiSqlLookupService<String>
		implements IKnowEmailLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();
		sqls.add(new SqlLookupConfiguration(SQLs.EVENT_SELECT_KNOWN_ATTENDEE_LOOKUP,
				new NVPair("currentUser", currentUserId)));
		sqls.add(new SqlLookupConfiguration(SQLs.EVENT_SELECT_KNOWN_HOST_LOOKUP,
				new NVPair("currentUser", currentUserId)));
		return sqls;
	}

	@Override
	protected Boolean getConfiguredRemoveDuplicate() {
		return Boolean.TRUE;
	}
}
