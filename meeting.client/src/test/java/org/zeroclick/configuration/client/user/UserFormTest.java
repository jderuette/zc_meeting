package org.zeroclick.configuration.client.user;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class UserFormTest {

	@BeanMock
	private IUserService m_mockSvc;

	@Before
	public void setup() {
		final UserFormData answer = new UserFormData();
		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(UserFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases

	public void testStartNew() {
		// TOD Djer13 implements some tests
	}
}
