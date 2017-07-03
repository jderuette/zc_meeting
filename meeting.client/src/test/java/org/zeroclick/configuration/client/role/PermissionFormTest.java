package org.oneclick.configuration.client.role;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oneclick.configuration.shared.role.IAppPermissionService;
import org.oneclick.configuration.shared.role.PermissionFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PermissionFormTest {

	@BeanMock
	private IAppPermissionService m_mockSvc;

	@Before
	public void setup() {
		final PermissionFormData answer = new PermissionFormData();
		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(PermissionFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases

	public void testStartNew() {
		// TOD Djer13 implements some tests
	}
}
