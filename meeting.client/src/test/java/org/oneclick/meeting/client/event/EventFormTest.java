package org.oneclick.meeting.client.event;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oneclick.meeting.shared.event.EventFormData;
import org.oneclick.meeting.shared.event.IEventService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class EventFormTest {

	@BeanMock
	private IEventService m_mockSvc;

	@Before
	public void setup() {
		final EventFormData answer = new EventFormData();
		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(EventFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(EventFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(EventFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(EventFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
	public void testStartNew() {
		// TOD Djer13 implements some tests
	}
}
