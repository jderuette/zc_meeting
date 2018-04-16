package org.zeroclick.configuration.client.role;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.PermissionFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PermissionFormTest {

	@BeanMock
	private IAppPermissionService mockSvc;

	@Before
	public void setUp() {
		final PermissionFormData answer = new PermissionFormData();
		answer.setRoleId(1);
		answer.getPermission().setValue("UnitTest.create");
		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(PermissionFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(PermissionFormData.class))).thenReturn(answer);
	}

	@Test
	public void testStartNew() {
		final PermissionForm form = new PermissionForm();

		form.startNew();
		// form.setRoleId(58);
		form.getPermissionField().setValue("UnitTest.create.new");

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartModify() {
		final PermissionForm form = new PermissionForm();

		form.setRoleId(1);
		form.startModify();

		final String currentRoleName = form.getPermissionField().getValue();
		Assert.assertEquals("Role name not loaded correctly", "UnitTest.create", currentRoleName);

		// updading ID allowed ?
		form.setRoleId(44);
		form.getPermissionField().setValue("UnitTest.create.new");

		form.doSave();
		form.doClose();
	}
}
