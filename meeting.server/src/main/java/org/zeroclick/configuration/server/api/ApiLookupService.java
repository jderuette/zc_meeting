package org.zeroclick.configuration.server.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.zeroclick.comon.text.UserHelper;
import org.zeroclick.configuration.shared.api.IApiLookupService;
import org.zeroclick.meeting.server.sql.AbstractCombinedMultiSqlLookupService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SqlLookupConfiguration;

public class ApiLookupService extends AbstractCombinedMultiSqlLookupService<Long> implements IApiLookupService {

	@Override
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
		final List<SqlLookupConfiguration> configuredSelects = new ArrayList<>();
		final UserHelper userHelper = BEANS.get(UserHelper.class);

		configuredSelects.add(new SqlLookupConfiguration(SQLs.OAUHTCREDENTIAL_LOOKUP,
				new NVPair("userId", userHelper.getCurrentUserId())));

		return configuredSelects;
	}
}
