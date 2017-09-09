package org.zeroclick.configuration.client.slot;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.SlotFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SlotFormTest {

	@BeanMock
	private ISlotService m_mockSvc;

	@Before
	public void setup() {
		SlotFormData answer = new SlotFormData();
		Mockito.when(m_mockSvc.prepareCreate(Matchers.any(SlotFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.create(Matchers.any(SlotFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.load(Matchers.any(SlotFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.store(Matchers.any(SlotFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
