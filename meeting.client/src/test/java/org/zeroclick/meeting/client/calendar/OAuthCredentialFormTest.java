package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.IApiService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OAuthCredentialFormTest {

	@BeanMock
	private IApiService m_mockSvc;

	@Before
	public void setup() {
		final ApiFormData answer = new ApiFormData();
		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(ApiFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(ApiFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases

	public void testStartNew() {
		// TOD Djer13 implements some tests
	}
}
