package org.zeroclick.common.document;

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
import org.zeroclick.common.document.DocumentFormData.LinkedRole.LinkedRoleRowData;
import org.zeroclick.common.shared.document.IDocumentService;
import org.zeroclick.testing.BatchLookupServiceClient;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DocumentFormTest {

	@BeanMock
	private IDocumentService mockSvc;
	private IBean<?> beanRegistration;
	private IBean<?> batchLookupBeanRegistration;

	@Before
	public void setUp() {
		final long documentId = 1L;
		final DocumentFormData answer = new DocumentFormData();
		answer.setContentData("<h1>UnitTest document</h1><p>Basic document content for Testing purpose</p>".getBytes());
		answer.getDocumentId().setValue(documentId);
		answer.getLastModificationDate().setValue(new Date());
		final LinkedRoleRowData linkedRoleRow = answer.getLinkedRole().addRow();
		linkedRoleRow.setRoleId(1L);
		linkedRoleRow.setStartDate(new Date());
		linkedRoleRow.setDocumentId(documentId);
		answer.getName().setValue("UnitTest document");

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(DocumentFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(DocumentFormData.class))).thenReturn(answer);

		final BeanMetaData RoleAndSubLookupBean = new BeanMetaData(RoleAndSubscriptionLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(RoleAndSubLookupBean);

		final BeanMetaData batchLookupServiceBeans = new BeanMetaData(BatchLookupServiceClient.class);
		this.batchLookupBeanRegistration = TestingUtility.registerBean(batchLookupServiceBeans);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}

		if (null != this.batchLookupBeanRegistration) {
			TestingUtility.unregisterBean(this.batchLookupBeanRegistration);
		}
	}

	@Test
	public void testModify() {
		final DocumentForm form = new DocumentForm();

		form.getDocumentIdField().setValue(1L);
		form.startModify();

		form.doClose();
	}
}
