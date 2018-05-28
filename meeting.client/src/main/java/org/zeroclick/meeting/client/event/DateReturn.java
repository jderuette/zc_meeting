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
package org.zeroclick.meeting.client.event;

import java.time.ZonedDateTime;

/**
 * @author djer
 *
 */
public class DateReturn {
	private ZonedDateTime start;
	private final ZonedDateTime end;
	private final Boolean created;
	private final Boolean loopInDates;
	private Boolean noAvailableDate;

	// to help informing user
	private String messageKey;
	private String icon;

	public DateReturn(final ZonedDateTime recommandedStart) {
		this.start = recommandedStart;
		this.end = null;
		this.created = Boolean.FALSE;
		this.loopInDates = Boolean.FALSE;
		this.noAvailableDate = Boolean.FALSE;
	}

	public DateReturn(final ZonedDateTime recommandedStart, final Boolean loopInDates) {
		this.start = recommandedStart;
		this.end = null;
		this.created = Boolean.FALSE;
		this.loopInDates = loopInDates;
		this.noAvailableDate = Boolean.FALSE;
	}

	public DateReturn(final ZonedDateTime recommandedStart, final Boolean loopInDates, final Boolean noAvailableDate) {
		this.start = recommandedStart;
		this.end = null;
		this.created = Boolean.FALSE;
		this.loopInDates = loopInDates;
		this.noAvailableDate = noAvailableDate;
	}

	public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate) {
		this.start = start;
		this.end = calculatedEndDate;
		this.created = Boolean.TRUE;
		this.loopInDates = Boolean.FALSE;
		this.noAvailableDate = Boolean.FALSE;
	}

	public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate, final Boolean loopInDates) {
		this.start = start;
		this.end = calculatedEndDate;
		this.created = Boolean.TRUE;
		this.loopInDates = loopInDates;
		this.noAvailableDate = Boolean.FALSE;
	}

	public DateReturn(final ZonedDateTime start, final ZonedDateTime calculatedEndDate, final Boolean loopInDates,
			final Boolean noAvailableDate) {
		this.start = start;
		this.end = calculatedEndDate;
		this.created = Boolean.TRUE;
		this.loopInDates = loopInDates;
		this.noAvailableDate = noAvailableDate;
	}

	public ZonedDateTime getStart() {
		return this.start;
	}

	public void setStart(final ZonedDateTime start) {
		this.start = start;
	}

	public ZonedDateTime getEnd() {
		return this.end;
	}

	public Boolean isCreated() {
		return this.created;
	}

	public Boolean isLoopInDates() {
		return this.loopInDates;
	}

	public Boolean isNoAvailableDate() {
		return this.noAvailableDate;
	}

	public void setNoAvailableDate(final Boolean noAvailableDate) {
		this.noAvailableDate = noAvailableDate;
	}

	public String getMessageKey() {
		return this.messageKey;
	}

	public void setMessageKey(final String messageKey) {
		this.messageKey = messageKey;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setIcon(final String icon) {
		this.icon = icon;
	}

}
