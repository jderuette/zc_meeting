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
package org.zeroclick.common.shared.notification;

import java.io.Serializable;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/**
 * @author djer
 *
 */
public abstract class AbstractNotication<T extends AbstractFormData> implements Serializable {

	private final T formData;

	public AbstractNotication(final T formData) {
		this.formData = formData;
	}

	public T getFormData() {
		return this.formData;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(50);
		builder.append(this.getClass().getSimpleName()).append("[FormData=").append(this.formData).append(']');
		return builder.toString();
	}

}
