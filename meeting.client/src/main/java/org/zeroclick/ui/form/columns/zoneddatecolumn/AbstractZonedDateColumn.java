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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;

/**
 * @author djer
 *
 */
public abstract class AbstractZonedDateColumn extends AbstractDateColumn {

	private DateHelper getDateHelper() {
		return BEANS.get(DateHelper.class);
	}

	private AppUserHelper getAppUserHelper() {
		return BEANS.get(AppUserHelper.class);
	}

	@Override
	protected abstract String getConfiguredHeaderText();

	public ZonedDateTime getZonedValue(final int rowIndex) {
		return this.getDateHelper().getZonedValue(this.getAppUserHelper().getCurrentUserTimeZone(),
				this.getValue(rowIndex));
	}

	public ZonedDateTime getSelectedZonedValue() {
		return this.getSelectedZonedValue(this.getAppUserHelper().getCurrentUserTimeZone());
	}

	public ZonedDateTime getSelectedZonedValue(final ZoneId userZoneId) {
		final Date currentDate = super.getSelectedValue();
		return this.getDateHelper().getZonedValue(userZoneId, currentDate);
	}

	/**
	 * @deprecated use getSelectedZonedValue instead
	 * @return
	 */
	@Deprecated
	public LocalDateTime getSelectedLocalValue() {
		final Date currentValue = super.getSelectedValue();
		LocalDateTime retvalue = null;
		if (null != currentValue) {
			retvalue = this.getDateHelper().toLocalDateTime(currentValue);
		}
		return retvalue;
	}

	public void setValue(final int rowIndex, final ZonedDateTime rawValue) {
		if (null != rawValue) {
			this.setValue(this.getTable().getRow(rowIndex), this.getDateHelper().toDate(rawValue));
		}
	}

	public void setValue(final ITableRow row, final ZonedDateTime rawValue) {
		if (null != rawValue && null != row) {
			this.setValue(this.getTable().getRow(row.getRowIndex()), this.getDateHelper().toDate(rawValue));
		}
	}

	/**
	 * @deprecated use setValue(int, ZonedDateTime) instead
	 * @return
	 */
	@Deprecated
	public void setValue(final int rowIndex, final LocalDateTime rawValue) {
		if (null != rawValue) {
			this.setValue(this.getTable().getRow(rowIndex), this.getDateHelper().toDate(rawValue));
		}
	}

	/**
	 * @deprecated use setValue(ITableRow, ZonedDateTime) instead
	 * @return
	 */
	@Deprecated
	public void setValue(final ITableRow row, final LocalDateTime rawValue) {
		if (null != rawValue && null != row) {
			this.setValue(this.getTable().getRow(row.getRowIndex()), this.getDateHelper().toDate(rawValue));
		}
	}

	@Override
	protected boolean getConfiguredHasTime() {
		return true;
	}

	@Override
	protected int getConfiguredWidth() {
		return 140;
	}
}
