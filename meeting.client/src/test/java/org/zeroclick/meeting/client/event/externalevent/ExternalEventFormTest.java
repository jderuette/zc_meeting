package org.zeroclick.meeting.client.event.externalevent;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ExternalEventFormTest {

	@BeanMock
	private IExternalEventService m_mockSvc;

	@Before
	public void setup() {
		ExternalEventFormData answer = new ExternalEventFormData();
		Mockito.when(m_mockSvc.prepareCreate(Matchers.any(ExternalEventFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.create(Matchers.any(ExternalEventFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.load(Matchers.any(ExternalEventFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.store(Matchers.any(ExternalEventFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
