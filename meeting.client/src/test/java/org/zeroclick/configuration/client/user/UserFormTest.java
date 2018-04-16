package org.zeroclick.configuration.client.user;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

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
import org.zeroclick.configuration.client.role.RoleLookupServiceMock;
import org.zeroclick.configuration.client.role.SubscriptionLookupCallMock;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserFormData.SubscriptionsListTable.SubscriptionsListTableRowData;
import org.zeroclick.testing.BatchLookupServiceClient;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class UserFormTest {

	@BeanMock
	private IUserService mockSvc;

	private IBean<?> beanRegistration;
	private IBean<?> subdcriptionRegistration;
	private IBean<?> batchLookupBeanRegistration;

	@Before
	public void setUp() {
		final String testUserTimeZone = "Europe/Paris";
		final String testUserLanguage = "FR-fr";
		final UserFormData answer = new UserFormData();

		answer.getUserId().setValue(1L);
		answer.setInvitedBy(1L);
		answer.setAutofilled(false);
		answer.getLogin().setValue("UnitTest");
		answer.getEmail().setValue("UnitTest@someProvider42.org");
		answer.getPassword().setValue("UT123$!");
		answer.getConfirmPassword().setValue("UT123$!");
		answer.getLanguage().setValue(testUserLanguage);
		answer.getTimeZone().setValue(testUserTimeZone);

		final Long userRole = 2L;
		answer.getRolesBox().setValue(new HashSet<>());
		answer.getRolesBox().getValue().add(userRole);

		final Long proSubRole = 4L;
		answer.getSubscriptionBox().setValue(proSubRole);

		final Calendar calendar = new GregorianCalendar();
		calendar.set(118 + 1900, 04, 02, 15, 27, 48);
		final Date freeSubscriptionDate = calendar.getTime();

		final SubscriptionsListTableRowData freeSubHisto = answer.getSubscriptionsListTable().addRow();
		freeSubHisto.setAcceptedCpsDate(freeSubscriptionDate);
		freeSubHisto.setAcceptedWithdrawalDate(freeSubscriptionDate);
		freeSubHisto.setStartDate(freeSubscriptionDate);
		freeSubHisto.setSubscriptionId(3L);
		freeSubHisto.setUserId(1L);

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.create(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(UserFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(UserFormData.class))).thenReturn(answer);

		Mockito.when(this.mockSvc.getCurrentUserDetails()).thenReturn(answer);

		Mockito.when(this.mockSvc.getUserTimeZone(Matchers.any(Long.class))).thenReturn(testUserTimeZone);
		Mockito.when(this.mockSvc.getUserLanguage(Matchers.any(Long.class))).thenReturn(testUserLanguage);

		final BeanMetaData roleLookupServiceBean = new BeanMetaData(RoleLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(roleLookupServiceBean);

		final BeanMetaData subdcriptionLoonkupBean = new BeanMetaData(SubscriptionLookupCallMock.class);
		this.subdcriptionRegistration = TestingUtility.registerBean(subdcriptionLoonkupBean);

		final BeanMetaData batchLookupServiceBeans = new BeanMetaData(BatchLookupServiceClient.class);
		this.batchLookupBeanRegistration = TestingUtility.registerBean(batchLookupServiceBeans);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}

		if (null != this.subdcriptionRegistration) {
			TestingUtility.unregisterBean(this.subdcriptionRegistration);
		}

		if (null != this.batchLookupBeanRegistration) {
			TestingUtility.unregisterBean(this.batchLookupBeanRegistration);
		}
	}

	@Test
	public void testStartModify() {
		final UserForm form = new UserForm();

		form.getUserIdField().setValue(1L);

		form.startModify();

		form.getLoginField().setValue("MyNewLogin");
		form.doSave();
		form.doClose();

	}
}
