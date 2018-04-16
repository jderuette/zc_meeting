package org.zeroclick.common.document.link;

import java.util.Date;

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
import org.zeroclick.common.document.RoleAndSubscriptionLookupServiceMock;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.IRoleTypeLookupService;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.meeting.client.event.KnowEmailLookupServiceMock;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AssignDocumentToRoleFormTest {

	@BeanMock
	private IRoleService mockSvc;

	private IBean<?> beanRegistration;

	private IBean<?> roleAndSubLookupbeanRegistration;

	@Before
	public void setUp() {
		final RoleFormData answer = new RoleFormData();

		answer.setRoleId(1L);
		answer.getRoleName().setValue("UTAdmin");
		answer.getType().setValue(IRoleTypeLookupService.TYPE_SUBSCRIPTION);

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(RoleFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(RoleFormData.class))).thenReturn(answer);

		final BeanMetaData KnowEmailLookupServiceBean = new BeanMetaData(KnowEmailLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(KnowEmailLookupServiceBean);

		final BeanMetaData roleAndSubLookupBean = new BeanMetaData(RoleAndSubscriptionLookupServiceMock.class);
		this.roleAndSubLookupbeanRegistration = TestingUtility.registerBean(roleAndSubLookupBean);

	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}

		if (null != this.roleAndSubLookupbeanRegistration) {
			TestingUtility.unregisterBean(this.roleAndSubLookupbeanRegistration);
		}
	}

	@Test
	public void testStartNew() {
		final AssignDocumentToRoleForm form = new AssignDocumentToRoleForm();

		form.startNew();

		form.getRoleIdField().setValue(1L);
		form.getDocumentIdField().setValue(1L);
		form.getStartDateField().setValue(new Date());

		form.doSave();
		form.doClose();
	}
}
