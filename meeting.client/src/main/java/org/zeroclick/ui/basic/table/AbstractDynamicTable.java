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
package org.zeroclick.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @author djer
 *
 */
public abstract class AbstractDynamicTable<F extends IForm> extends AbstractTable {

	/**
	 * Get a list of value (to add to a row) from a form. Order must be the same
	 * as Column Order
	 *
	 * @param formData
	 * @return
	 */
	// FIXME Djer13 avoid coupling between column order and data extraction
	// order
	protected abstract List<Object> getListFromForm(final F formData);

	/**
	 * Extract an ID from a FormData @see getIdFromRow(ITableRow)
	 *
	 * @param formData
	 * @return
	 */
	protected abstract Object getIdFromForm(final F formData);

	/**
	 * Extract an Id from a row.<br/>
	 * Typically : row.getCell(this.**getDocumentIdColumn()**).getValue()
	 *
	 * @param row
	 *            the to extract ID from
	 * @return an Id (often a long, but for composite Id may be a String (like :
	 *         id1-id2-id3 ex : 1-5-20170101T15:30:00)
	 */
	protected abstract Object getIdFromRow(ITableRow row);

	/**
	 * Create a new row and add it to the current table
	 *
	 * @param formData
	 * @return
	 */
	protected ITableRow createTableRowFromForm(final F formData) {
		final ITableRow row = new TableRow(this.getColumnSet(), this.getListFromForm(formData));
		this.addRow(row);
		return row;
	}

	/**
	 * Modify the existing Row corresponding to the formData
	 *
	 * @param formData
	 * @return the modified tableRow (or null if no row found to update)
	 */
	protected ITableRow updateTableRowFromForm(final F formData) {
		final ITableRow row = this.getRow(formData);
		if (null != row) {
			final List<Object> datas = this.getListFromForm(formData);
			for (int i = 0; i < datas.size(); i++) {
				final Object propertyFormData = datas.get(i);
				final ICell cell = row.getCell(i);
				if (propertyFormData != cell) {
					// TODO enable validation ??
					// row.getTable().getColumns().get(i).setValue(row,
					// propertyFormData);
					row.setCellValue(i, propertyFormData);
				}
			}
		}
		return row;
	}

	/**
	 * Delete the row corresponding to the formData (if exists)
	 *
	 * @param formData
	 */
	protected void deleteTableRowFromForm(final F formData) {
		final ITableRow row = this.getRow(formData);
		if (null != row) {
			this.deleteRow(row);
		}
	}

	/**
	 * Search for the row with the same ID as the form.
	 *
	 * @param formData
	 * @return a Row or null if no row match. If multiple row with this ID
	 *         (should not append) return the first one.
	 */
	protected ITableRow getRow(final F formData) {
		final Object formId = this.getIdFromForm(formData);
		final List<ITableRow> currentRows = this.getRows();
		for (final ITableRow aRow : currentRows) {
			if (formId.equals(this.getIdFromRow(aRow))) {
				return aRow;
			}
		}

		return null;
	}

}
