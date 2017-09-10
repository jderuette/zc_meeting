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
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;

/**
 * @author djer
 *
 */
public class SqlLookupConfiguration {

	private String sql;
	private Integer translatedColumn;
	private Object bindBase;
	private Boolean caseSensitive;
	private Boolean autoFirstWildcard;

	public SqlLookupConfiguration() {
		super();
		// no default values
	}

	public SqlLookupConfiguration(final String sql, final Integer translatedColumn, final Object bindBase) {
		super();
		this.sql = sql;
		this.translatedColumn = translatedColumn;
		this.bindBase = bindBase;
		this.caseSensitive = Boolean.FALSE;
		this.autoFirstWildcard = Boolean.FALSE;
	}

	public SqlLookupConfiguration(final String sql, final Object bindBase) {
		super();
		this.sql = sql;
		this.translatedColumn = -1;
		this.bindBase = bindBase;
		this.caseSensitive = Boolean.FALSE;
		this.autoFirstWildcard = Boolean.FALSE;
	}

	public SqlLookupConfiguration(final String sql, final Integer translatedColumn) {
		super();
		this.sql = sql;
		this.translatedColumn = translatedColumn;
		this.caseSensitive = Boolean.FALSE;
		this.autoFirstWildcard = Boolean.FALSE;
	}

	public SqlLookupConfiguration(final String sql) {
		super();
		this.sql = sql;
		this.translatedColumn = -1;
		this.caseSensitive = Boolean.FALSE;
		this.autoFirstWildcard = Boolean.FALSE;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(200);
		builder.append("SqlLookupConfiguration [sql=").append(this.sql).append(", translatedColumn=")
				.append(this.translatedColumn).append(", bindBase=").append(this.bindBase).append(", caseSensitive=")
				.append(this.caseSensitive).append(", autoFirstWildcard=").append(this.autoFirstWildcard).append("]");
		return builder.toString();
	}

	public String getSql() {
		return this.sql;
	}

	/**
	 * SqlSELECT statement
	 */
	@ConfigProperty(ConfigProperty.SQL)
	@Order(10)
	public SqlLookupConfiguration setSql(final String sql) {
		this.sql = sql;
		return this;
	}

	public Integer getTranslatedColumn() {
		return this.translatedColumn;
	}

	/**
	 * Column in result set to send to NLS
	 */
	@ConfigProperty(ConfigProperty.INTEGER)
	@Order(20)
	public SqlLookupConfiguration setTranslatedColumn(final Integer translatedColumn) {
		this.translatedColumn = translatedColumn;
		return this;
	}

	public Object getBindBase() {
		return this.bindBase;
	}

	/**
	 * Bind base for this SQL request
	 */
	@ConfigProperty(ConfigProperty.OBJECT)
	@Order(30)
	public SqlLookupConfiguration setBindBase(final Object bindBase) {
		this.bindBase = bindBase;
		return this;
	}

	public Boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	/**
	 * Is search case sensitive ? (default FALSE)
	 */
	@ConfigProperty(ConfigProperty.BOOLEAN)
	@Order(40)
	public SqlLookupConfiguration setCaseSensitive(final Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		return this;
	}

	public Boolean isAutoFirstWildcard() {
		return this.autoFirstWildcard;
	}

	/**
	 * Automatically add a wildcard at the beginning ? (default FALSE)
	 */
	@ConfigProperty(ConfigProperty.BOOLEAN)
	@Order(50)
	public SqlLookupConfiguration setAutoFirstWildcard(final Boolean autoFirstWilcard) {
		this.autoFirstWildcard = autoFirstWilcard;
		return this;
	}

}
