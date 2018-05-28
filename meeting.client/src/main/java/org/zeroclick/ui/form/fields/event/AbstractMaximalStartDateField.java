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
public abstract class AbstractMaximalStartDateField extends AbstractDateField {

	private AbstractMinimalStartDateField minimalStartDateField;

	protected AbstractMinimalStartDateField getMinimalStartDateField() {
		return this.minimalStartDateField;
	}

	private void setMinimalStartDateField(final AbstractMinimalStartDateField minimalStartDateField) {
		this.minimalStartDateField = minimalStartDateField;
	}

	@Override
	protected void execInitField() {
		this.setMinimalStartDateField(this.getConfiguredMinimalStartDateField());
		super.execInitField();
	}

	protected AbstractMinimalStartDateField getConfiguredMinimalStartDateField() {
		return null;
	}

	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.meeting.maximalStartDate");
	}

	@Override
	protected boolean getConfiguredHasTime() {
		return Boolean.TRUE;
	}

	@Override
	protected Date getConfiguredAutoDate() {
		final Date now = new Date();
		now.setHours(23);
		now.setMinutes(59);
		now.setSeconds(59);
		return now;
	}

	public void validateCurrentValue() {
		if (null != this.minimalStartDateField) {
			this.validateValue(this.getValue());
		}
	}

	private void validateValue(final Date rawValue) {
		if (null != rawValue && null != this.getMinimalStartDateField()
				&& null != this.getMinimalStartDateField().getValue()
				&& rawValue.before(this.getMinimalStartDateField().getValue())) {
			throw new VetoException(TEXTS.get("zc.meeting.maximalStartDate.afterMinimalDate"));
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
		if (null != this.getMinimalStartDateField()) {
			this.getMinimalStartDateField().validateCurrentValue();
		}
		return super.execValidateValue(rawValue);
	}
}
