package org.zeroclick.configuration.client.role;

import java.util.ArrayList;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zeroclick.configuration.shared.role.AssignToRoleFormData;
import org.zeroclick.configuration.shared.role.IRolePermissionService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AssignToRoleFormTest {

	@BeanMock
	private IRolePermissionService mockSvc;
	private IBean<?> beanRegistration;

	@Before
	public void setup() {
		final AssignToRoleFormData answer = new AssignToRoleFormData();
		Mockito.when(this.mockSvc.create(Matchers.any(AssignToRoleFormData.class))).thenReturn(answer);

		final IRolePermissionService rolePermissionMock = Mockito.mock(IRolePermissionService.class);

		final Answer<Void> doNothing = new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				return null;
			}
		};

		Mockito.doAnswer(doNothing).when(rolePermissionMock).remove(Matchers.any(Long.class));
		Mockito.doAnswer(doNothing).when(rolePermissionMock).remove(Matchers.any(Long.class),
				Matchers.anyListOf(String.class));

		final BeanMetaData roleLookupServiceBean = new BeanMetaData(RoleLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(roleLookupServiceBean);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}
	}

	@Test
	public void testStartNew() {
		final AssignToRoleForm form = new AssignToRoleForm();

		form.getRoleIdField().setValue(1L);
		form.setPermission(new ArrayList<>());
		form.getPermission().add("test.createPermission");
		form.getLevelField().setValue(100L);

		form.startNew();
		form.doClose();

	}
}
