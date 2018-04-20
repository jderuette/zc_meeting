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
import org.zeroclick.common.document.DocumentFormData;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserFormData.SubscriptionsListTable.SubscriptionsListTableRowData;
import org.zeroclick.configuration.shared.user.ValidateCpsFormData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ViewCpsFormTest {

	@BeanMock
	private IUserService mockSvc;

	@BeanMock
	private IRoleService mockRoleSvc;

	private IBean<?> beanRegistration;

	@Before
	public void setUp() {
		final ValidateCpsFormData answer = new ValidateCpsFormData();

		final String cpsText = "<h1>A CPS Text</h1><p>for Unit Test</p>";

		final Calendar calendar = new GregorianCalendar();
		calendar.set(118 + 1900, 04, 02, 17, 05, 00);
		final Date acceptedDate = calendar.getTime();

		answer.getUserId().setValue(1L);
		answer.getAcceptCps().setValue(true);
		answer.getAcceptedCpsDate().setValue(acceptedDate);
		answer.getAcceptWithdrawal().setValue(true);
		answer.getAcceptedWithdrawalDate().setValue(acceptedDate);
		answer.getCpsText().setValue(cpsText);
		answer.getStartDate().setValue(acceptedDate);
		answer.getSubscriptionId().setValue(1L);

		final String testUserTimeZone = "Europe/Paris";
		final String testUserLanguage = "FR-fr";
		final UserFormData userAnswer = new UserFormData();

		userAnswer.getUserId().setValue(1L);
		userAnswer.setInvitedBy(1L);
		userAnswer.setAutofilled(false);
		userAnswer.getLogin().setValue("UnitTest");
		userAnswer.getEmail().setValue("UnitTest@someProvider42.org");
		userAnswer.getPassword().setValue("UT123$!");
		userAnswer.getConfirmPassword().setValue("UT123$!");
		userAnswer.getLanguage().setValue(testUserLanguage);
		userAnswer.getTimeZone().setValue(testUserTimeZone);

		final Long userRole = 2L;
		userAnswer.getRolesBox().setValue(new HashSet<>());
		userAnswer.getRolesBox().getValue().add(userRole);

		final Long proSubRole = 4L;
		userAnswer.getSubscriptionBox().setValue(proSubRole);

		final Calendar freeSubAcceptedCalendar = new GregorianCalendar();
		freeSubAcceptedCalendar.set(118 + 1900, 04, 02, 15, 27, 48);
		final Date freeSubscriptionDate = freeSubAcceptedCalendar.getTime();

		final SubscriptionsListTableRowData freeSubHisto = userAnswer.getSubscriptionsListTable().addRow();
		freeSubHisto.setAcceptedCpsDate(freeSubscriptionDate);
		freeSubHisto.setAcceptedWithdrawalDate(freeSubscriptionDate);
		freeSubHisto.setStartDate(freeSubscriptionDate);
		freeSubHisto.setSubscriptionId(3L);
		freeSubHisto.setUserId(1L);

		final DocumentFormData documentAnswer = new DocumentFormData();

		final Calendar proSubCpsLatsModifcalendar = new GregorianCalendar();
		calendar.set(118 + 1900, 03, 01, 14, 00, 00);
		final Date proSubCpsLatsModifDate = proSubCpsLatsModifcalendar.getTime();

		documentAnswer.setContentData(cpsText.getBytes());
		documentAnswer.getDocumentId().setValue(28L);
		documentAnswer.getLastModificationDate().setValue(proSubCpsLatsModifDate);
		documentAnswer.getName().setValue("UnitTest Pro CPS");

		Mockito.when(this.mockSvc.load(Matchers.any(ValidateCpsFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.getActiveSubscriptionDetails(Matchers.any(Long.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.load(Matchers.any(ValidateCpsFormData.class))).thenReturn(answer);

		Mockito.when(this.mockRoleSvc.getActiveDocument(Matchers.any(Long.class))).thenReturn(documentAnswer);

		Mockito.when(this.mockSvc.load(Matchers.any(UserFormData.class))).thenReturn(userAnswer);

		final BeanMetaData subscriptionLookupServiceBean = new BeanMetaData(SubscriptionLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(subscriptionLookupServiceBean);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}
	}

	@Test
	public void testNew() {
		final ValidateCpsForm form = new ValidateCpsForm();

		form.startNew();

		form.doSave();
		form.doClose();
	}

}
