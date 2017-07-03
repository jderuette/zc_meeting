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
package org.zeroclick.meeting.client.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
public class CallTrackerService<KEY_TYPE> {

	private static final Logger LOG = LoggerFactory.getLogger(CallTrackerService.class);

	private final Integer maxSuccessiveCall;
	private final Duration timeBeforeAutoReset;
	private final String context;

	private final Map<KEY_TYPE, CallTracker> alreadyAsk = new HashMap<>();

	public CallTrackerService(final Integer maxSuccessiveCall, final Duration timeBeforeAutoReset) {
		this.maxSuccessiveCall = maxSuccessiveCall;
		this.timeBeforeAutoReset = timeBeforeAutoReset;
		this.context = null;
	}

	public CallTrackerService(final Integer maxSuccessiveCall, final Duration timeBeforeAutoReset,
			final String context) {
		super();
		this.maxSuccessiveCall = maxSuccessiveCall;
		this.timeBeforeAutoReset = timeBeforeAutoReset;
		this.context = context;
	}

	/**
	 * Try to avoid infinite loop, if user credential storage fail.
	 *
	 * Don't forget to reset when credential are validated and stored.
	 *
	 * @param userId
	 * @throws VetoException
	 *             if number of successive calls for this user exceed the
	 *             allowed max.
	 */
	public void validateCanCall(final KEY_TYPE key) {
		if (!this.canIncrementNbCall(key)) {
			LOG.warn(this.context + " limit of " + this.maxSuccessiveCall + " reached. Calls locked for "
					+ this.timeBeforeAutoReset.get(ChronoUnit.SECONDS) + " secondes");
			throw new VetoException(this.context + " Too many successive Call");
		}
	}

	/**
	 * Indicate if number of call exceed max for key;
	 *
	 * @param key
	 * @return TRUE if counter can be incremented
	 */
	public Boolean canIncrementNbCall(final KEY_TYPE key) {
		// create a counter for the key (often userId) if necessary
		if (!this.alreadyAsk.containsKey(key)) {
			this.alreadyAsk.put(key, new CallTracker());
		}
		// check for auto reset
		if (this.alreadyAsk.get(key).getLastCall().plus(this.timeBeforeAutoReset).isBefore(LocalDateTime.now())) {
			LOG.debug(this.context + " Last call was more than " + this.timeBeforeAutoReset
					+ " reseting the call Tracker");
			this.alreadyAsk.get(key).reset();
		}
		// Check for Max call
		if (this.alreadyAsk.get(key).getValue() >= this.maxSuccessiveCall) {
			LOG.error(this.context + " Probable Loop for key " + key + ", " + this.maxSuccessiveCall + " reached");
			return Boolean.FALSE;
		}
		// increment for the next check
		this.alreadyAsk.put(key, this.alreadyAsk.get(key).increment());
		LOG.debug(this.context + " Tracker state " + this.alreadyAsk.get(key) + "(max configured :"
				+ this.maxSuccessiveCall + ")");

		return Boolean.TRUE;
	}

	public void resetNbCall(final KEY_TYPE key) {
		final CallTracker realNbCalls = this.alreadyAsk.remove(key);
		if (null == realNbCalls) {
			LOG.warn(this.context + " Resting without controlling nbCall (value Null in map for this key : " + key
					+ ")");
		} else {
			LOG.info(this.context + " Reseting after " + realNbCalls + " for key : " + key);
		}
	}

	public class CallTracker {
		private Integer value;
		private LocalDateTime lastCall;

		public CallTracker() {
			this.value = 0;
			this.lastCall = LocalDateTime.now();
		}

		public CallTracker(final Integer value) {
			this.value = value;
			this.lastCall = LocalDateTime.now();
		}

		public void reset() {
			this.value = 0;
			this.lastCall = LocalDateTime.now();
		}

		/**
		 * Increment the current value, and update the last Call date
		 *
		 * @return
		 */
		public CallTracker increment() {
			this.value++;
			this.lastCall = LocalDateTime.now();
			return this;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("CallTracker [value=").append(this.value).append(", lastCall=").append(this.lastCall)
					.append("]");
			return builder.toString();
		}

		public Integer getValue() {
			return this.value;
		}

		public void setValue(final Integer value) {
			this.value = value;
		}

		public LocalDateTime getLastCall() {
			return this.lastCall;
		}

		public void setLastCall(final LocalDateTime lastCall) {
			this.lastCall = lastCall;
		}
	}
}
