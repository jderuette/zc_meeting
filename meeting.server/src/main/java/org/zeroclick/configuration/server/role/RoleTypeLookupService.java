package org.zeroclick.configuration.server.role;

import java.util.ArrayList;
import java.util.List;

import org.zeroclick.configuration.shared.role.IRoleTypeLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;

public class RoleTypeLookupService extends AbstractCombinedMultiSqlLookupService<String>
		implements IRoleTypeLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		sqls.add(new SqlLookupConfiguration(SQLs.ROLE_SELECT_TYPE_FOR_SMART_FIELD + SQLs.ROLE_SELECT_FILTER_LOOKUP_TYPE)
				.setAutoFirstWildcard(Boolean.TRUE));
		return sqls;
	}
}
