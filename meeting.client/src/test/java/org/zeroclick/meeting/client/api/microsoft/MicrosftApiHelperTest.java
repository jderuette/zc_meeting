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
package org.zeroclick.meeting.client.api.microsoft;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author djer
 *
 */

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class MicrosftApiHelperTest {

	@Test
	public void testgetLoginUrl() {
		final MicrosoftApiHelper microsoftApiHelper = BEANS.get(MicrosoftApiHelper.class);

		final UUID state = UUID.randomUUID();
		final UUID nonce = UUID.randomUUID();

		ClientSessionProvider.currentSession().setData("state", state);
		ClientSessionProvider.currentSession().setData("nonce", nonce);

		final String userLoginUrl = microsoftApiHelper.getLoginUrl(state, nonce);

		assertNotNull("user URL login not built", userLoginUrl);

		// final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd
		// HH:mm:ss");
		// final Date startDate = formatter.parse("2010-05-01 12:00:00");
		// final Date endDate = formatter.parse("2010-05-30 13:00:00");
		//
		// microsoftApiHelper.findAppointments(null, startDate, endDate,
		// service);

	}

}
