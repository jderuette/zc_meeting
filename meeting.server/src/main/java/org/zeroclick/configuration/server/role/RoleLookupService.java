package org.zeroclick.configuration.server.role;

import org.eclipse.scout.rt.server.jdbc.lookup.AbstractSqlLookupService;
import org.zeroclick.configuration.shared.role.IRoleLookupService;
import org.zeroclick.meeting.server.sql.SQLs;

public class RoleLookupService extends AbstractSqlLookupService<Long> implements IRoleLookupService {

	@Override
	protected String getConfiguredSqlSelect() {
		return SQLs.ROLE_LOOKUP;
	}
}
