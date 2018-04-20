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

import java.util.Comparator;

/**
 * @author djer
 *
 */
public abstract class AbstractDateComparator<T> implements Comparator<T> {

	protected abstract Long extractDateAsLong(T objectContainingDate);

	@Override
	public int compare(final T object1, final T object2) {
		final long e1StartValue = this.extractDateAsLong(object1);
		final long e2StartValue = this.extractDateAsLong(object2);

		if (e1StartValue < e2StartValue) {
			return -1;
		} else if (e1StartValue == e2StartValue) {
			return 0;
		}
		return 1;
	}
}
