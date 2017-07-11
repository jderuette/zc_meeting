/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.server.sql;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.jdbc.postgresql.AbstractPostgreSqlService;
import org.zeroclick.meeting.server.sql.DatabaseProperties.JdbcMappingNameProperty;

/**
 * @author djer
 *
 */
@Order(1940)
public class PostgresSqlService extends AbstractPostgreSqlService {

	@Override
	protected String getConfiguredJdbcMappingName() {
		return CONFIG.getPropertyValue(JdbcMappingNameProperty.class);
	}
}
