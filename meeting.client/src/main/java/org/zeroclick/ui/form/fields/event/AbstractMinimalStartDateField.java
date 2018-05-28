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
package org.zeroclick.ui.form.fields.event;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @author djer
 *
 */
public abstract class AbstractMinimalStartDateField extends AbstractDateField {

	private AbstractMaximalStartDateField maximalStartDateField;

	protected AbstractMaximalStartDateField getMaximalStartDateField() {
		return this.maximalStartDateField;
	}

	private void setMaximalStartDateField(final AbstractMaximalStartDateField maximalStartDateField) {
		this.maximalStartDateField = maximalStartDateField;
	}

	protected AbstractMaximalStartDateField getConfiguredMaximalStartDateField() {
		return null;
	}

	@Override
	protected void execInitField() {
		this.setMaximalStartDateField(this.getConfiguredMaximalStartDateField());
		super.execInitField();
	}

	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.meeting.minimalStartDate");
	}

	@Override
	protected boolean getConfiguredHasTime() {
		return Boolean.TRUE;
	}

	@Override
	protected Date getConfiguredAutoDate() {
		final Date now = new Date();
		now.setHours(0);
		now.setMinutes(0);
		now.setSeconds(0);
		return now;
	}

	public void validateCurrentValue() {
		if (null != this.maximalStartDateField) {
			this.validateValue(this.getValue());
		}
	}

	private void validateValue(final Date rawValue) {
		if (null != rawValue && null != this.getMaximalStartDateField()
				&& null != this.getMaximalStartDateField().getValue()
				&& rawValue.after(this.getMaximalStartDateField().getValue())) {
			throw new VetoException(TEXTS.get("zc.meeting.minimalStartDate.afterMaximalDate"));
		}
		this.clearErrorStatus();
	}

	protected void validateWithOtherFields(final Date rawValue) {
		// Default do nothing
	}

	@Override
	protected Date execValidateValue(final Date rawValue) {
		this.validateValue(rawValue);
		this.validateWithOtherFields(rawValue);
		if (null != this.getMaximalStartDateField()) {
			this.getMaximalStartDateField().validateCurrentValue();
		}
		return super.execValidateValue(rawValue);
	}
}
