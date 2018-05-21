package org.zeroclick.meeting.client.event.involevment;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class InvolvementFormTest {

	@BeanMock
	private IInvolvementService m_mockSvc;

	@Before
	public void setup() {
		InvolvementFormData answer = new InvolvementFormData();
		Mockito.when(m_mockSvc.prepareCreate(Matchers.any(InvolvementFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.create(Matchers.any(InvolvementFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.load(Matchers.any(InvolvementFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.store(Matchers.any(InvolvementFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
