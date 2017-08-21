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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jdbc.ISqlService;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.server.jdbc.lookup.AbstractSqlLookupService;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link AbstractSqlLookupService}.
 * <ul>
 * <li>Allow multiple query to be run to build the List of ILookupRow<T>
 * ({@link #getConfiguredSqlSelects()})</li>
 * <li>Allow to remove duplicate entries
 * ({@link #getConfiguredRemoveDuplicate()})</li>
 * <li>Allow to use **one** custom BindBase in all your queries
 * ({@link #getConfiguredBindBase()})</li>
 * <ul>
 *
 * @author djer
 *
 */

public abstract class AbstractCombinedMultiSqlLookupService<T> extends AbstractSqlLookupService<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCombinedMultiSqlLookupService.class);

	/**
	 * Sqls SELECT statement
	 */
	@ConfigProperty(ConfigProperty.SQL)
	@Order(12)
	protected List<String> getConfiguredSqlSelects() {
		return null;
	}

	/**
	 * ONE other bindBase to pass to the query
	 *
	 * @return
	 */
	@Order(15)
	protected Object getConfiguredBindBase() {
		return null;
	}

	/**
	 * Remove the duplicate between each Queries. for performance it's
	 * recommended to use "DISTINCT" in each queries to limit Work.
	 *
	 * @return
	 */
	@ConfigProperty(ConfigProperty.BOOLEAN)
	@Order(17)
	protected Boolean getConfiguredRemoveDuplicate() {
		return Boolean.FALSE;
	}

	/**
	 * Comparator to identify duplicate Row. Default use key only.
	 *
	 * @return
	 */
	@Order(19)
	protected Comparator<? super ILookupRow<T>> getConfiguredComparator() {
		return new Comparator<ILookupRow<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(final ILookupRow<T> row1, final ILookupRow<T> row2) {
				return ((Comparable<T>) row1.getKey()).compareTo(row2.getKey());
			}
		};
	}

	@Override
	public List<ILookupRow<T>> getDataByKey(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<String> sqls = this.getConfiguredSqlSelects();
		for (final String sql : sqls) {
			combinedRows.addAll(this.execLoadLookupRows(sql, this.filterSqlByKey(sql), call));
		}

		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByText(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<String> sqls = this.getConfiguredSqlSelects();
		for (final String sql : sqls) {
			// change wildcards in text to db specific wildcards
			if (call.getText() != null) {
				final String text = call.getText();
				final String sqlWildcard = BEANS.get(ISqlService.class).getSqlStyle().getLikeWildcard();
				call.setText(text.replace(call.getWildcard(), sqlWildcard));
			}
			combinedRows.addAll(this.execLoadLookupRows(sql, this.filterSqlByText(sql), call));
		}
		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByAll(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<String> sqls = this.getConfiguredSqlSelects();
		for (final String sql : sqls) {
			if (containsRefusingAllTag(sql)) {
				throw new VetoException(ScoutTexts.get("SearchTextIsTooGeneral"));
			}
			combinedRows.addAll(this.execLoadLookupRows(sql, this.filterSqlByAll(sql), call));
		}
		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByRec(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<String> sqls = this.getConfiguredSqlSelects();
		for (final String sql : sqls) {
			combinedRows.addAll(this.execLoadLookupRows(sql, this.filterSqlByRec(sql), call));
		}
		return this.handlePostQueries(combinedRows);
	}

	/**
	 * This method is called on server side to load lookup rows.
	 */
	@ConfigOperation
	@Order(10)
	@Override
	protected List<ILookupRow<T>> execLoadLookupRows(final String originalSql, final String preprocessedSql,
			final ILookupCall<T> call) {
		final Object bindBase = this.getConfiguredBindBase();
		final Object[][] data = SQL.selectLimited(preprocessedSql, call.getMaxRowCount(), call, bindBase);
		if (this.getConfiguredSortColumn() >= 0) {
			sortData(data, this.getConfiguredSortColumn());
		}
		try {
			Class<?> genericsParameterClass = Object.class;
			try {
				genericsParameterClass = TypeCastUtility.getGenericsParameterClass(this.getClass(),
						ILookupService.class);
			} catch (final IllegalArgumentException e) {
				LOG.warn("Unable to calculate type parameters for lookup service '" + this.getClass().getName()
						+ "'. No key type validation will be performed.");
			}
			return createLookupRowArray(data, call, genericsParameterClass);
		} catch (final IllegalArgumentException e) {
			throw new ProcessingException(
					"Unable to load lookup rows for lookup service '" + this.getClass().getName() + "'.", e);
		}
	}

	protected List<ILookupRow<T>> handlePostQueries(final List<ILookupRow<T>> rows) {
		if (this.getConfiguredRemoveDuplicate()) {
			return this.removeDuplicate(rows);
		} else {
			return rows;
		}
	}

	protected List<ILookupRow<T>> removeDuplicate(final List<ILookupRow<T>> rows) {
		final TreeSet<ILookupRow<T>> uniquesRows = new TreeSet<>(this.getConfiguredComparator());
		uniquesRows.addAll(rows);
		return new ArrayList<>(uniquesRows);
	}

}
