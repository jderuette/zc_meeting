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
package org.zeroclick.meeting.client.api;

import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.meeting.client.api.microsoft.service.CalendarService;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;

/**
 * @author djer
 * @param <W>
 *
 */
public class ApiCalendar<T, V> {
	final T calendar;
	final V credential;
	final ApiTableRowData metaData;

	public ApiCalendar(final T calendar, final ApiCredential<V> apiCredential) {
		super();
		this.calendar = calendar;
		this.credential = apiCredential.getCredential();
		this.metaData = apiCredential.getMetaData();
	}

	public ApiCalendar(final CalendarService mCalendarService, final ApiCredential<String> apiCredential) {
		super();
		this.calendar = (T) mCalendarService;
		this.credential = (V) apiCredential.getCredential();
		this.metaData = apiCredential.getMetaData();
	}

	public ApiCalendar(final Calendar gcalendarService, final ApiCredential<Credential> apiCredential) {
		super();
		this.calendar = (T) gcalendarService;
		this.credential = (V) apiCredential.getCredential();
		this.metaData = apiCredential.getMetaData();
	}

	public T getCalendar() {
		return this.calendar;
	}

	public V getCredential() {
		return this.credential;
	}

	public ApiTableRowData getMetaData() {
		return this.metaData;
	}
}
