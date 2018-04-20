package org.zeroclick.configuration.client.role;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class RoleFormTest {

	@BeanMock
	private IRoleService mockSvc;
	private IBean<?> beanRegistration;

	@Before
	public void setUp() {
		final RoleFormData answer = new RoleFormData();
		answer.setRoleId(1L);
		answer.getRoleName().setValue("UnitTestRole");

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(RoleFormData.class))).thenReturn(answer);

		final BeanMetaData roleTypeLookupServiceBean = new BeanMetaData(RoleTypeLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(roleTypeLookupServiceBean);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}
	}

	@Test
	public void testStartNew() {
		// TOD0 Djer13 implements some tests
		final RoleForm form = new RoleForm();

		form.startNew();

		form.getRoleNameField().setValue("ANewRole");
		form.getTypeField().setValue("Type3");

		form.doSave();
		form.doClose();
	}
}
