package org.zeroclick.common.params;

import java.util.ArrayList;
import java.util.List;

import org.zeroclick.configuration.shared.params.IAppParamsCategoryLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;

public class AppParamsCategoryLookupService extends AbstractCombinedMultiSqlLookupService<String>
		implements IAppParamsCategoryLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> sqls = new ArrayList<>();
		sqls.add(new SqlLookupConfiguration(
				SQLs.PARAMS_SELECT_CATEGORY_FOR_SMART_FIELD + SQLs.PARAMS_SELECT_FILTER_LOOKUP_CATEGORY)
						.setAutoFirstWildcard(Boolean.TRUE));
		return sqls;
	}
}
