package org.zeroclick.common.document;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.common.shared.document.IDocumentService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DocumentFormTest {

	@BeanMock
	private IDocumentService m_mockSvc;

	@Before
	public void setup() {
		DocumentFormData answer = new DocumentFormData();
		Mockito.when(m_mockSvc.prepareCreate(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.create(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.load(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.store(Matchers.any(DocumentFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
