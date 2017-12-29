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
package org.zeroclick.ui.form.columns.zoneddatecolumn;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 * @author djer
 *
 */
public abstract class AbstractZonedTimeColumn extends AbstractZonedDateColumn {

	@Override
	protected void execDecorateCell(final Cell cell, final ITableRow row) {
		if (null != cell.getValue()) {
			final Date utcDate = (Date) cell.getValue();
			cell.setText(this.getDateHelper().formatHours(utcDate, this.getAppUserHelper().getCurrentUserTimeZone()));
		}
	}

	@Override
	protected boolean getConfiguredHasTime() {
		return true;
	}

	@Override
	protected boolean getConfiguredHasDate() {
		return false;
	}

	@Override
	protected int getConfiguredWidth() {
		return 140;
	}
}
