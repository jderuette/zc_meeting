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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.user.AppUserHelper;

/**
 * @author djer
 *
 */
public abstract class AbstractZonedDateField extends AbstractDateField {

	protected DateHelper getDateHelper() {
		return BEANS.get(DateHelper.class);
	}

	protected AppUserHelper getAppUserHelper() {
		return BEANS.get(AppUserHelper.class);
	}

	@Override
	protected boolean getConfiguredHasTime() {
		return false;
	}

	public ZonedDateTime getZonedValue() {
		return this.getDateHelper().getZonedValue(this.getAppUserHelper().getCurrentUserTimeZone(), this.getValue());
	}

	public ZonedDateTime getZonedValue(final ZoneId userZoneId) {
		final Date currentDate = super.getValue();
		return this.getDateHelper().getZonedValue(userZoneId, currentDate);
	}

	public void setValue(final ZonedDateTime rawValue) {
		if (null != rawValue) {
			this.setValue(this.getDateHelper().toDate(rawValue));
		}
	}

	public void prepapreStore() {
		final Date usertDate = this.getValue();
		if (null != usertDate && this.isSaveNeeded()) {
			this.setValue(this.getDateHelper().toUtcDate(usertDate, this.getAppUserHelper().getCurrentUserTimeZone()));
		}
	}

	@Override
	protected void execChangedValue() {
		if (null != this.getValue()) {
			this.setDisplayText(this.getDisplayText());
		}
	}

	@Override
	public String getDisplayText() {
		String formatedDateHours = "";
		if (null != this.getValue()) {
			final DateHelper dateHelper = BEANS.get(DateHelper.class);
			formatedDateHours = dateHelper.formatForUi(this.getValue(),
					this.getAppUserHelper().getCurrentUserTimeZone());
		}
		return formatedDateHours;
	}

}
