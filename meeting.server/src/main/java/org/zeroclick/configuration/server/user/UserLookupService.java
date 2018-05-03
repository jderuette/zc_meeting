package org.zeroclick.configuration.server.user;

import java.util.ArrayList;
import java.util.List;

import org.zeroclick.configuration.shared.user.IUserLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;

public class UserLookupService extends AbstractCombinedMultiSqlLookupService<Long> implements IUserLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		sqls.add(new SqlLookupConfiguration(SQLs.USER_SELECT_LOOKUP).setAutoFirstWildcard(Boolean.TRUE));
		return sqls;
	}

	// @Override
	// protected Boolean getConfiguredRemoveDuplicate() {
	// return Boolean.TRUE;
	// }
}
