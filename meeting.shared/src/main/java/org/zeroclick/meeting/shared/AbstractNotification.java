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
package org.zeroclick.meeting.shared;

import java.io.Serializable;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/**
 * @author djer
 *
 */
public abstract class AbstractNotification<V extends AbstractFormData> implements Serializable {

	private static final long serialVersionUID = 7996107908284379078L;

	private final V formData;

	public AbstractNotification(final V newData) {
		this.formData = newData;
	}

	public V getFormData() {
		return this.formData;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(100);
		builder.append(this.getClass()).append(" [FormData=").append(this.getFormData()).append(']');
		return builder.toString();
	}

}
