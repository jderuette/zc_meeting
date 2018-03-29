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
package org.zeroclick.ui.form.fields.datefield;

import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;

/**
 * @author djer
 *
 */
public abstract class AbstractZonedTimeField extends AbstractZonedDateField {

	@Override
	protected boolean getConfiguredHasTime() {
		return true;
	}

	@Override
	protected boolean getConfiguredHasDate() {
		return false;
	}

	@Override
	public String getDisplayText() {
		String formatedHours = "";
		if (null != this.getValue()) {
			final DateHelper dateHelper = BEANS.get(DateHelper.class);
			final AppUserHelper appUserHelper = BEANS.get(AppUserHelper.class);

			formatedHours = dateHelper.formatHoursForUi(this.getValue(), appUserHelper.getCurrentUserTimeZone());
		}
		return formatedHours;
	}

}
