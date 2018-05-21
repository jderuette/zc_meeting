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
package org.zeroclick.ui.form.columns.userid;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.shared.user.UserLookupCall;

/**
 * @author djer
 *
 */
public abstract class AbstractUserIdColumn extends AbstractSmartColumn<Long> {
	@Override
	protected String getConfiguredHeaderText() {
		return TEXTS.get("zc.meeting.event.involevment.userId");
	}

	@Override
	protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
		return UserLookupCall.class;
	}

	@Override
	protected void execDecorateCell(final Cell cell, final ITableRow row) {
		super.execDecorateCell(cell, row);

		final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);
		final Object cellValue = cell.getValue();

		if (null != cellValue) {
			final Long userId = (Long) cellValue;
			if (appUserHelper.isMySelf(userId)) {
				cell.setText(TEXTS.get("zc.common.me"));
				// this.updateDisplayText(row, cell);
			}
		}
	}

	@Override
	protected int getConfiguredWidth() {
		return 200;
	}
}