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
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jdbc.ISqlService;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.server.jdbc.lookup.AbstractSqlLookupService;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
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
 * To get higher performance when searching by key, put SQL in order to maximize
 * first request get result/lower cost
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
	protected List<SqlLookupConfiguration> getConfiguredSqlSelects() {
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

	@SuppressWarnings("unchecked")
	@Override
	public List<ILookupRow<T>> getDataByKey(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<SqlLookupConfiguration> sqls = this.getConfiguredSqlSelects();
		for (final SqlLookupConfiguration sqlConfig : sqls) {
			// If there a re translation, the "real" key will be missed. Each
			// translated value will be considered as a new value (instead of a
			// translation of the same key)
			// if (sqlConfig.getTranslatedColumn() >= 0) {
			// final Map<String, String> allTranslations =
			// ScoutTexts.getInstance().getTextMap(NlsLocale.get());
			// if (allTranslations.containsValue(call.getKey())) {
			// final Iterator<String> itKeys =
			// allTranslations.keySet().iterator();
			// while (itKeys.hasNext()) {
			// final String key = itKeys.next();
			// final String value = allTranslations.get(key);
			// if (value.equals(call.getKey())) {
			// call.setKey((T) key);
			// break;
			// }
			// }
			// }
			// }
			this.configureCall(call, sqlConfig);

			final List<ILookupRow<T>> data = this.execLoadLookupRows(sqlConfig, this.filterSqlByKey(sqlConfig.getSql()),
					call);
			combinedRows.addAll(data);
			// By key we stop when we found one
			if (null != data && !data.isEmpty()) {
				break;
			}
		}

		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByText(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<SqlLookupConfiguration> sqls = this.getConfiguredSqlSelects();
		for (final SqlLookupConfiguration sqlConfig : sqls) {
			this.configureCall(call, sqlConfig);
			// change wildcards in text to db specific wildcards
			if (call.getText() != null) {
				final String text = call.getText();
				final String sqlWildcard = BEANS.get(ISqlService.class).getSqlStyle().getLikeWildcard();
				call.setText(text.replace(call.getWildcard(), sqlWildcard));
			}
			combinedRows.addAll(this.execLoadLookupRows(sqlConfig, this.filterSqlByText(sqlConfig.getSql()), call));
		}
		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByAll(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<SqlLookupConfiguration> sqls = this.getConfiguredSqlSelects();
		for (final SqlLookupConfiguration sqlConfig : sqls) {
			this.configureCall(call, sqlConfig);
			if (containsRefusingAllTag(sqlConfig.getSql())) {
				throw new VetoException(ScoutTexts.get("SearchTextIsTooGeneral"));
			}
			combinedRows.addAll(this.execLoadLookupRows(sqlConfig, this.filterSqlByAll(sqlConfig.getSql()), call));
		}
		return this.handlePostQueries(combinedRows);
	}

	@Override
	public List<ILookupRow<T>> getDataByRec(final ILookupCall<T> call) {
		final List<ILookupRow<T>> combinedRows = new ArrayList<>();
		final List<SqlLookupConfiguration> sqls = this.getConfiguredSqlSelects();
		for (final SqlLookupConfiguration sqlConfig : sqls) {
			this.configureCall(call, sqlConfig);
			combinedRows.addAll(this.execLoadLookupRows(sqlConfig, this.filterSqlByRec(sqlConfig.getSql()), call));
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
		final SqlLookupConfiguration sqlConfig = new SqlLookupConfiguration(originalSql);
		this.configureCall(call, sqlConfig);
		return this.execLoadLookupRows(sqlConfig, preprocessedSql, call);
	}

	/**
	 * This method is called on server side to load lookup rows.
	 */
	@ConfigOperation
	@Order(10)
	protected List<ILookupRow<T>> execLoadLookupRows(final SqlLookupConfiguration sqlConfig,
			final String preprocessedSql, final ILookupCall<T> call) {
		final Object bindBase = sqlConfig.getBindBase();
		String cleanedPreproccesseSql = preprocessedSql;
		Object[][] filteredData;
		if (sqlConfig.getTranslatedColumn() >= 0) {
			// remove the <text> filter, else will be applied on NLS key
			cleanedPreproccesseSql = preprocessedSql.replaceAll(" AND \\S+ LIKE :text", "");
		}
		if (!sqlConfig.isCaseSensitive()) {
			cleanedPreproccesseSql = cleanedPreproccesseSql.replaceAll("(AND )(\\S+)( LIKE :text)", " $1 lower($2) $3");
			// cleanedPreproccesseSql = cleanedPreproccesseSql.replaceAll("(AND
			// )(\\S+)(=:key)", "$1 lower($2) $3");
		}
		final Object[][] data = SQL.selectLimited(cleanedPreproccesseSql, call.getMaxRowCount(), call, bindBase);
		if (this.getConfiguredSortColumn() >= 0) {
			sortData(data, this.getConfiguredSortColumn());
		}

		if (sqlConfig.getTranslatedColumn() >= 0) {
			this.applyTranslation(data, sqlConfig);
			// apply filter on *translated* data, filter "<text>.... </text>
			// should no be in original SQL
			filteredData = this.filterTranslatedColumn(data, call, sqlConfig);
		} else {
			filteredData = data;
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
			return createLookupRowArray(filteredData, call, genericsParameterClass);
		} catch (final IllegalArgumentException e) {
			throw new ProcessingException(
					"Unable to load lookup rows for lookup service '" + this.getClass().getName() + "'.", e);
		}
	}

	protected void applyTranslation(final Object[][] data, final SqlLookupConfiguration sqlConfig) {
		if (null != data && data.length > 0) {
			for (final Object[] row : data) {
				if (null != row && null != row[sqlConfig.getTranslatedColumn()]) {
					final String translated = TEXTS.get((String) row[sqlConfig.getTranslatedColumn()],
							(String) row[sqlConfig.getTranslatedColumn()]);
					if (null != translated) {
						row[sqlConfig.getTranslatedColumn()] = translated;
					}
				}
			}
		}
	}

	protected Object[][] filterTranslatedColumn(final Object[][] data, final ILookupCall<T> call,
			final SqlLookupConfiguration sqlConfig) {
		String searchedText = call.getText();
		final List<Object[]> filteredRows = new ArrayList<>();
		Object[][] filteredRowRet;
		if (null != searchedText && null != data && data.length > 0) {
			// remove "SQL" wildcard and replace * by .* (regexp)
			searchedText = searchedText.replaceAll("\\*", ".*").replaceAll("%", ".*");
			final int j = 0;
			for (int i = 0; i < data.length; i++) {
				final Object[] row = data[i];
				if (null != row && null != row[sqlConfig.getTranslatedColumn()]) {
					final String translated = (String) row[sqlConfig.getTranslatedColumn()];
					String translatedSearched = translated;
					if (!sqlConfig.isCaseSensitive()) {
						translatedSearched = translated.toLowerCase(NlsLocale.get());
					}
					if (null == translated || searchedText.isEmpty()
							|| translatedSearched.matches(searchedText.toLowerCase(NlsLocale.get()))) {
						filteredRows.add(row.clone());
					}
				}
			}
			filteredRowRet = CollectionUtility.toArray(filteredRows, Object[].class);

		} else {
			// no filters return a data copy
			filteredRowRet = data.clone();
		}

		return filteredRowRet;
	}

	protected void configureCall(final ILookupCall<T> call, final SqlLookupConfiguration sqlConfig) {
		if (null != call.getText() && !call.getText().isEmpty()) {
			if (Boolean.FALSE == sqlConfig.isCaseSensitive()) {
				call.setText(call.getText().toLowerCase(NlsLocale.get()));
			}
			if (sqlConfig.isAutoFirstWildcard() && !call.getText().startsWith(call.getWildcard())) {
				call.setText(call.getWildcard() + call.getText());
			}
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
