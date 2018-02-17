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
package org.zeroclick.ui.action.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @author djer
 *
 */
public abstract class AbstractCopyMenu extends AbstractMenu {

	@Override
	protected String getConfiguredText() {
		return TEXTS.get("zc.user.copy");
	}

	@Override
	protected Set<? extends IMenuType> getConfiguredMenuTypes() {
		return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
	}

	/**
	 * Separator used between each values (columns)
	 *
	 * @return
	 */
	protected String getConfiguredValueSeparator() {
		return ";";
	}

	/**
	 * Separator used between lines (row)
	 *
	 * @return
	 */
	protected String getConfiguredLineSeparator() {
		return "\r\n";
	}

	/**
	 * Columns to add in the clipBoard. <br/>
	 * <b>default</b> all table columns
	 *
	 * @return the list of column to add to clipBoard
	 */
	protected List<IColumn<?>> getConfiguredCopiedColumns() {
		return new ArrayList<>();
	}

	/**
	 * Table providing datas
	 *
	 * @return
	 */
	protected AbstractTable getConfiguredTable() {
		return null;
	}

	/**
	 * Add the default Ctr+C to this menu ?
	 *
	 * @return
	 */
	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredDefaultKeyStroke() {
		return false;
	}

	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredAddHeader() {
		return true;
	}

	@Override
	protected String getConfiguredKeyStroke() {
		String keyStroke = null;
		if (this.getConfiguredDefaultKeyStroke()) {
			keyStroke = combineKeyStrokes(IKeyStroke.CONTROL, "c");
		}
		return keyStroke;
	}

	@Override
	protected void execAction() {

		if (null == this.getConfiguredTable()) {
			throw new VetoException("Error, no table selected");
		}

		final AbstractTable table = this.getConfiguredTable();

		if (table.getSelectedRowCount() == 0) {
			throw new VetoException("You must select at least one row");
		}

		final String valueSeparator = this.getConfiguredValueSeparator();
		final String lineSeparator = this.getConfiguredLineSeparator();
		List<IColumn<?>> copiedColumns = this.getConfiguredCopiedColumns();
		final Boolean addHeaders = this.getConfiguredAddHeader();

		if (null == this.getConfiguredCopiedColumns() || this.getConfiguredCopiedColumns().size() == 0) {
			copiedColumns = table.getColumns();
		}

		final List<ITableRow> selectedRows = table.getSelectedRows();

		final StringBuilder copiedData = new StringBuilder(512);

		if (addHeaders) {
			for (final IColumn<?> column : copiedColumns) {
				copiedData.append(column.getHeaderCell().getText()).append(valueSeparator);
			}
			// remove last value separator
			copiedData.deleteCharAt(copiedData.lastIndexOf(valueSeparator));
			copiedData.append(lineSeparator);
		}

		for (final ITableRow row : selectedRows) {
			for (final IColumn<?> column : copiedColumns) {
				copiedData.append(column.getValue(row)).append(valueSeparator);
			}
			// remove last value separator
			copiedData.deleteCharAt(copiedData.lastIndexOf(valueSeparator));
			copiedData.append(lineSeparator);
		}

		// remove last lineSeparator
		copiedData.deleteCharAt(copiedData.lastIndexOf(lineSeparator));

		BEANS.get(IClipboardService.class).setTextContents(copiedData.toString());
	}

}
