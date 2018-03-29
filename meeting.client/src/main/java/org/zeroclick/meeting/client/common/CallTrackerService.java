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
public class CallTrackerService<K> {

	private static final Logger LOG = LoggerFactory.getLogger(CallTrackerService.class);

	private Integer maxSuccessiveCall;
	private Duration timeToLive;
	private final String context;

	private final Map<K, CallTracker> alreadyAsk = new HashMap<>();

	public CallTrackerService(final Integer maxSuccessiveCall, final Duration timeToLive) {
		this.maxSuccessiveCall = maxSuccessiveCall;
		this.timeToLive = timeToLive;
		this.context = null;
	}

	public CallTrackerService(final Integer maxSuccessiveCall, final Duration timeToLive, final String context) {
		super();
		this.maxSuccessiveCall = maxSuccessiveCall;
		this.timeToLive = timeToLive;
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
	public void validateCanCall(final K key) {
		if (!this.canIncrementNbCall(key)) {
			final StringBuilder builder = new StringBuilder();
			builder.append(this.context).append(" limit of ").append(this.maxSuccessiveCall)
					.append(" reached. Calls locked for ").append(this.timeToLive.get(ChronoUnit.SECONDS))
					.append(" secondes");
			LOG.warn(builder.toString());
			throw new VetoException(this.context + " Too many successive Call");
		}
	}

	/**
	 * Indicate if number of call exceed max for key;
	 *
	 * @param key
	 * @return TRUE if counter can be incremented
	 */
	public Boolean canIncrementNbCall(final K key) {
		// create a counter for the key (often userId) if necessary
		if (!this.alreadyAsk.containsKey(key)) {
			this.alreadyAsk.put(key, new CallTracker());
		}
		// check for auto reset
		if (this.alreadyAsk.get(key).getLastCall().plus(this.timeToLive).isBefore(LocalDateTime.now())) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(50).append(this.context).append(" Last call was more than ")
						.append(this.timeToLive.toMinutes()).append(" minutes reseting the call Tracker").toString());
			}
			this.alreadyAsk.get(key).reset();
		}
		// Check for Max call
		if (this.alreadyAsk.get(key).getValue() >= this.maxSuccessiveCall) {
			final StringBuilder builder = new StringBuilder(50);
			builder.append(this.context).append(" probable Loop for key ").append(key).append(", ")
					.append(this.maxSuccessiveCall).append(" reached, last call at : ")
					.append(this.alreadyAsk.get(key).getLastCall());
			LOG.error(builder.toString());
			return Boolean.FALSE;
		}
		// increment for the next check
		this.alreadyAsk.put(key, this.alreadyAsk.get(key).increment());
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder(50).append(this.context).append(" Tracker state ")
					.append(this.alreadyAsk.get(key)).append(" (max configured : ").append(this.maxSuccessiveCall)
					.append(", ttl : ").append(this.timeToLive.toMinutes()).toString());
		}

		return Boolean.TRUE;
	}

	public void resetNbCall(final K key) {
		final CallTracker realNbCalls = this.alreadyAsk.remove(key);
		final StringBuilder builder = new StringBuilder(100);
		if (null == realNbCalls) {
			builder.append(this.context)
					.append(" Resting without controlling nbCall (value Null in map for this key : ").append(key)
					.append(')');
			LOG.warn(builder.toString());
		} else {
			builder.append(this.context).append(" Reseting after ").append(realNbCalls).append(" for key : ")
					.append(key);
			LOG.info(builder.toString());
		}

	}

	public Integer getMaxSuccessiveCall() {
		return this.maxSuccessiveCall;
	}

	public void setMaxSuccessiveCall(final Integer maxSuccessiveCall) {
		this.maxSuccessiveCall = maxSuccessiveCall;
	}

	public Duration getTimeToLive() {
		return this.timeToLive;
	}

	public void setTimeToLive(final Duration timeToLive) {
		this.timeToLive = timeToLive;
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
			final StringBuilder builder = new StringBuilder(60);
			builder.append("CallTracker [value=").append(this.value).append(", lastCall=").append(this.lastCall)
					.append(']');
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
