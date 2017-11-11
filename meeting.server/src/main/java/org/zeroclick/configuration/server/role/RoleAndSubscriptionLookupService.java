package org.zeroclick.configuration.server.role;

import java.util.ArrayList;
import java.util.List;

import org.zeroclick.configuration.shared.role.IRoleAndSubscriptionLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;

public class RoleAndSubscriptionLookupService extends AbstractCombinedMultiSqlLookupService<Long>
		implements IRoleAndSubscriptionLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		sqls.add(new SqlLookupConfiguration(SQLs.ROLE_LOOKUP_WITHOUT_SUBSCRIPTION).setAutoFirstWildcard(Boolean.TRUE)
				.setTranslatedColumn(1));
		sqls.add(new SqlLookupConfiguration(SQLs.ROLE_LOOKUP_SUBSCRIPTION).setAutoFirstWildcard(Boolean.TRUE)
				.setTranslatedColumn(1));
		return sqls;
	}

	@Override
	protected Boolean getConfiguredRemoveDuplicate() {
		return Boolean.TRUE;
	}
}
