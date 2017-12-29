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
package org.zeroclick.meeting.ui.html.json.form.fields;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.zeroclick.ui.form.fields.datefield.AbstractZeroClickUTCDateField;

/**
 * @author djer
 *
 */
@Order(10)
// @ClassId("5e26128b-2d65-4ed7-88cc-c23a466b6e88")
@FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
public class ZeroClickUTCDateFiled extends AbstractZeroClickUTCDateField implements IComposerValueField {

	@Override
	public void addValueChangeListenerToTarget(final PropertyChangeListener listener) {
		this.addPropertyChangeListener(listener);
	}

	@Override
	public void removeValueChangeListenerFromTarget(final PropertyChangeListener listener) {
		this.removePropertyChangeListener(listener);
	}

	@Override
	public void setSelectionContext(final IDataModelAttribute attribute, final int dataType,
			final IDataModelAttributeOp op, final List values) {
		try {
			@SuppressWarnings("unchecked")
			final Object firstElement = CollectionUtility.firstElement(values);
			if (firstElement instanceof UTCDate) {
				this.setValue((UTCDate) firstElement);
			} else {
				this.setValue(null);
			}
		} catch (final Exception e) {
			// nop
			this.setValue(null);
		}
	}

	@Override
	public void clearSelectionContext() {
		this.setValue(null);
	}

	@Override
	public List<Object> getValues() {
		if (this.getValue() != null) {
			return Collections.singletonList((Object) this.getValue());
		} else {
			return null;
		}
	}

	@Override
	public List<String> getTexts() {
		return CollectionUtility.arrayList(this.getDisplayText());
	}
}
