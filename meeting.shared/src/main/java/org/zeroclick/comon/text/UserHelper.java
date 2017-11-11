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
package org.zeroclick.comon.text;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class UserHelper {

	public Long getCurrentUserId() {
		final AccessControlService acs = BEANS.get(AccessControlService.class);
		return acs.getZeroClickUserIdOfCurrentSubject();
	}

	public Subject getCurrentUserSubject() {
		return Subject.getSubject(AccessController.getContext());
	}
}
