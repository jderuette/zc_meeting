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
package org.zeroclick.meeting.client.api.microsoft.data;

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/patternedrecurrence}
 */
public class PatternedRecurrence {
	private RecurrencePattern pattern;
	private RecurrenceRange range;

	public RecurrencePattern getPattern() {
		return this.pattern;
	}

	public void setPattern(final RecurrencePattern pattern) {
		this.pattern = pattern;
	}

	public RecurrenceRange getRange() {
		return this.range;
	}

	public void setRange(final RecurrenceRange range) {
		this.range = range;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(100);
		builder.append("PatternedRecurrence [pattern=").append(this.pattern).append(", range=").append(this.range)
				.append(']');
		return builder.toString();
	}
}
