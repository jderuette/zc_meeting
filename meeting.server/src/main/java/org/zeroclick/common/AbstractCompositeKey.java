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
package org.zeroclick.common;

/**
 * @author djer
 *
 */
public abstract class AbstractCompositeKey<K, L> {

	K keyOne;
	L keyTwo;

	public K getKeyOne() {
		return this.keyOne;
	}

	public void setKeyOne(final K keyOne) {
		this.keyOne = keyOne;
	}

	public L getKeyTwo() {
		return this.keyTwo;
	}

	public void setKeyTwo(final L keyTwo) {
		this.keyTwo = keyTwo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.keyOne == null ? 0 : this.keyOne.hashCode());
		result = prime * result + (this.keyTwo == null ? 0 : this.keyTwo.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final AbstractCompositeKey other = (AbstractCompositeKey) obj;
		if (this.keyOne == null) {
			if (other.keyOne != null) {
				return false;
			}
		} else if (!this.keyOne.equals(other.keyOne)) {
			return false;
		}
		if (this.keyTwo == null) {
			if (other.keyTwo != null) {
				return false;
			}
		} else if (!this.keyTwo.equals(other.keyTwo)) {
			return false;
		}
		return true;
	}

}
