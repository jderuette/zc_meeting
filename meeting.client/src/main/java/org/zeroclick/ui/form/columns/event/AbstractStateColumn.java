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
package org.zeroclick.ui.form.columns.event;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.zeroclick.meeting.shared.event.EventStateCodeType;

/**
 * @author djer
 *
 */
public abstract class AbstractStateColumn extends AbstractSmartColumn<String> {
	// In User context, so texts are translated
	final EventStateCodeType eventStateCodes = new EventStateCodeType();

	@Override
	protected String getConfiguredHeaderText() {
		return TEXTS.get("zc.meeting.event.state");
	}

	@Override
	protected int getConfiguredWidth() {
		return 100;
	}

	@Override
	protected boolean getConfiguredVisible() {
		return Boolean.FALSE;
	}

	@Override
	protected void execDecorateCell(final Cell cell, final ITableRow row) {
		super.execDecorateCell(cell, row);

		final Object stateValue = cell.getValue();

		if (null != stateValue) {
			final String stateColumnValue = (String) stateValue;

			final ICode<String> currentStateCode = this.eventStateCodes.getCode(stateColumnValue);
			if (null != currentStateCode) {
				cell.setIconId(currentStateCode.getIconId());
				cell.setBackgroundColor(currentStateCode.getBackgroundColor());
				cell.setForegroundColor(currentStateCode.getForegroundColor());
				cell.setText(currentStateCode.getText());
			}
		}
	}

	@Override
	protected Class<? extends ICodeType<Long, String>> getConfiguredCodeType() {
		return EventStateCodeType.class;
	}
}