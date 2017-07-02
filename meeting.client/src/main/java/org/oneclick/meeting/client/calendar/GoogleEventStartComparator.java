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
package org.oneclick.meeting.client.calendar;

import java.util.Comparator;

import com.google.api.services.calendar.model.Event;

/**
 * @author djer13
 *
 *         Allow ordering of Google events from multiple calendars. Order is
 *         based on the start date of each events.
 *
 */
public class GoogleEventStartComparator implements Comparator<Event> {

	@Override
	public int compare(final Event e1, final Event e2) {
		Long e1StartValue;
		Long e2StartValue;
		if (null == e1.getStart().getDateTime()) {
			e1StartValue = e1.getStart().getDate().getValue();
		} else {
			e1StartValue = e1.getStart().getDateTime().getValue();
		}
		if (null == e2.getStart().getDateTime()) {
			e2StartValue = e2.getStart().getDate().getValue();
		} else {
			e2StartValue = e2.getStart().getDateTime().getValue();
		}

		if (e1StartValue < e2StartValue) {
			return -1;
		} else if (e1StartValue == e2StartValue) {
			return 0;
		}
		return 1;
	}

	/*
	 * compares its two arguments for order. Returns a negative integer (first
	 * less than), zero (first equals second), a positive integer (first greater
	 * than)
	 */

}
