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
package org.zeroclick.configuration.client.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

/**
 * @author djer
 *
 */
public class AccessControlServiceHelperMock implements IAccessControlServiceHelper {

	@Override
	public void clearUserIdsCache(final Collection<? extends String> cacheKeys) throws ProcessingException {
		// No cache in this mock, clearcache does Nothing
	}

	@Override
	public Set<String> getUserNotificationIds(final Long userId) {
		final Set<String> notifIds = new HashSet<>();

		if (userId == 1L) {
			notifIds.add("anonymous");
		}
		return notifIds;
	}

	@Override
	public Long getZeroClickUserIdOfCurrentSubject() {
		return 1L;
	}

}
