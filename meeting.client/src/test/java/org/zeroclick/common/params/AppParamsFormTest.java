package org.zeroclick.common.params;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.params.IAppParamsService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AppParamsFormTest {

	@BeanMock
	private IAppParamsService m_mockSvc;

	@Before
	public void setup() {
		final AppParamsFormData answer = new AppParamsFormData();
		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
