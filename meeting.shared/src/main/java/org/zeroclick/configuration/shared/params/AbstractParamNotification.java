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
package org.zeroclick.configuration.shared.params;

import java.io.Serializable;

import org.zeroclick.common.params.AppParamsFormData;

/**
 * @author djer
 *
 */
public abstract class AbstractParamNotification implements Serializable {

	private static final long serialVersionUID = -2546211635305638271L;

	private final AppParamsFormData paramForm;

	public AbstractParamNotification(final AppParamsFormData paramForm) {
		this.paramForm = paramForm;
	}

	public AppParamsFormData getParamForm() {
		return this.paramForm;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(50);
		builder.append("ParamNotification [eventForm=").append(this.paramForm).append(']');
		return builder.toString();
	}

}
