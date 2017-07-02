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
package org.oneclick.configuration.shared.api;

import java.io.Serializable;

import org.oneclick.meeting.shared.calendar.ApiFormData;

/**
 * @author djer
 *
 */
public abstract class AbstractApiNotification implements Serializable {

	private static final long serialVersionUID = 7138478047755190548L;

	private final ApiFormData apiForm;

	public AbstractApiNotification(final ApiFormData apiForm) {
		this.apiForm = apiForm;
	}

	public ApiFormData getApiForm() {
		return this.apiForm;
	}
}
