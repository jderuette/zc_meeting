package org.zeroclick.common.params;

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
import org.zeroclick.configuration.shared.params.IAppParamsService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AppParamsFormTest {

	@BeanMock
	private IAppParamsService mockSvc;
	private IBean<?> beanRegistration;

	@Before
	public void setUp() {
		final AppParamsFormData answer = new AppParamsFormData();
		answer.getParamId().setValue(1L);
		answer.getCategory().setValue("Unit Test");
		answer.getKey().setValue("unit.test.param1.key");
		answer.getValue().setValue("The value");

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(AppParamsFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(AppParamsFormData.class))).thenReturn(answer);

		final BeanMetaData paramCategoryBean = new BeanMetaData(AppParamLookUpCallMock.class);
		this.beanRegistration = TestingUtility.registerBean(paramCategoryBean);

	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}
	}

	@Test
	public void testModify() {
		final AppParamsForm formTest = new AppParamsForm();

		formTest.getParamIdField().setValue(1L);
		formTest.startModify();

		formTest.doSave();
		formTest.doClose();
	}
}
