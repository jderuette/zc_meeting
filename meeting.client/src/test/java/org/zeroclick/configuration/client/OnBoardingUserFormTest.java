package org.zeroclick.configuration.client;

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
import org.zeroclick.configuration.onboarding.OnBoardingUserForm;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.configuration.shared.api.ApiTablePageData.ApiTableRowData;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.client.calendar.ApiLookupServiceMock;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.testing.BatchLookupServiceClient;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OnBoardingUserFormTest {

	@BeanMock
	private IUserService mockSvc;

	@BeanMock
	private IApiService apiMockSvc;

	@BeanMock
	private ICalendarConfigurationService calendarConfigurationMockSvc;

	private IBean<?> beanRegistration;
	private IBean<?> subdcriptionRegistration;
	private IBean<?> batchLookupBeanRegistration;

	@Before
	public void setUp() {
		final String testUserTimeZone = "Europe/Paris";
		final String testUserLanguage = "FR-fr";

		final OnBoardingUserFormData onBoardingAnswer = new OnBoardingUserFormData();

		onBoardingAnswer.getUserId().setValue(1L);
		onBoardingAnswer.getLanguage().setValue(testUserLanguage);
		onBoardingAnswer.getLogin().setValue("UnitTest");
		onBoardingAnswer.getTimeZone().setValue(testUserTimeZone);

		Mockito.when(this.mockSvc.load(Matchers.any(OnBoardingUserFormData.class))).thenReturn(onBoardingAnswer);
		Mockito.when(this.mockSvc.store(Matchers.any(OnBoardingUserFormData.class))).thenReturn(onBoardingAnswer);

		final BeanMetaData roleLookupServiceBean = new BeanMetaData(RoleLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(roleLookupServiceBean);

		final BeanMetaData subdcriptionLoonkupBean = new BeanMetaData(SubscriptionLookupCallMock.class);
		this.subdcriptionRegistration = TestingUtility.registerBean(subdcriptionLoonkupBean);

		final BeanMetaData batchLookupServiceBeans = new BeanMetaData(BatchLookupServiceClient.class);
		this.batchLookupBeanRegistration = TestingUtility.registerBean(batchLookupServiceBeans);

		final ApiTablePageData apiAnswer = new ApiTablePageData();

		final ApiTableRowData firstApi = apiAnswer.addRow();
		firstApi.setUserId(1L);
		firstApi.setAccessToken("GoogleUnitTestAccesToken");
		firstApi.setAccountEmail("UnitTest@gmail42.org");
		firstApi.setApiCredentialId(1L);
		firstApi.setExpirationTimeMilliseconds(100L);
		firstApi.setProvider(ProviderCodeType.GoogleCode.ID);
		firstApi.setRefreshToken("UnitTestRefreshToken");
		firstApi.setTenantId(null);// No tenant ID for Google APIs

		final ApiTableRowData secondApi = apiAnswer.addRow();
		secondApi.setUserId(1L);
		secondApi.setAccessToken("MicrosoftUnitTestAccesToken");
		secondApi.setAccountEmail("UnitTest@hotmail42.org");
		secondApi.setApiCredentialId(2L);
		secondApi.setExpirationTimeMilliseconds(100L);
		secondApi.setProvider(ProviderCodeType.MicrosoftCode.ID);
		secondApi.setRefreshToken("UnitTestRefreshToken");
		secondApi.setTenantId("AUnitTestTennantId");

		Mockito.when(this.apiMockSvc.getApiTableData(Matchers.any(Boolean.class))).thenReturn(apiAnswer);

		final CalendarConfigurationFormData calConfigurationAnswer = new CalendarConfigurationFormData();
		calConfigurationAnswer.getUserId().setValue(1L);
		calConfigurationAnswer.getAddEventToCalendar().setValue(true);
		calConfigurationAnswer.getCalendarConfigurationId().setValue(1L);
		calConfigurationAnswer.getExternalId().setValue("UnitTestMainCalendarExternalId");
		calConfigurationAnswer.getMain().setValue(true);
		calConfigurationAnswer.getName().setValue("UnitTestMain");
		calConfigurationAnswer.getOAuthCredentialId().setValue(1L);
		calConfigurationAnswer.getProcess().setValue(true);
		calConfigurationAnswer.getProcessFreeEvent().setValue(false);
		calConfigurationAnswer.getProcessFullDayEvent().setValue(true);
		calConfigurationAnswer.getProcessNotRegistredOnEvent().setValue(false);
		calConfigurationAnswer.getReadOnly().setValue(false);

		Mockito.when(this.calendarConfigurationMockSvc.prepareCreate(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(calConfigurationAnswer);
		Mockito.when(this.calendarConfigurationMockSvc.create(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(calConfigurationAnswer);
		Mockito.when(this.calendarConfigurationMockSvc.load(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(calConfigurationAnswer);
		Mockito.when(this.calendarConfigurationMockSvc.store(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(calConfigurationAnswer);

		final BeanMetaData apiLookupServiceBean = new BeanMetaData(ApiLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(apiLookupServiceBean);

		final CalendarsConfigurationFormData calendarsDataTableAnswer = new CalendarsConfigurationFormData();

		final CalendarConfigTableRowData firstCalendar = calendarsDataTableAnswer.getCalendarConfigTable().addRow();
		firstCalendar.setUserId(1L);
		firstCalendar.setAddEventToCalendar(true);
		firstCalendar.setCalendarConfigurationId(1L);
		firstCalendar.setExternalId("UnitTestMainCalendarExternalId");
		firstCalendar.setMain(true);
		firstCalendar.setName("UnitTestMain");
		firstCalendar.setOAuthCredentialId(1L);
		firstCalendar.setProcess(true);
		firstCalendar.setProcessFreeEvent(false);
		firstCalendar.setProcessFullDayEvent(true);
		firstCalendar.setProcessNotRegistredOnEvent(false);
		firstCalendar.setReadOnly(false);

		final CalendarConfigTableRowData secondCalendar = calendarsDataTableAnswer.getCalendarConfigTable().addRow();
		secondCalendar.setUserId(1L);
		secondCalendar.setAddEventToCalendar(false);
		secondCalendar.setCalendarConfigurationId(1L);
		secondCalendar.setExternalId("UnitTestSharedCalendarExternalId");
		secondCalendar.setMain(true);
		secondCalendar.setName("UnitTestShared");
		secondCalendar.setOAuthCredentialId(1L);
		secondCalendar.setProcess(true);
		secondCalendar.setProcessFreeEvent(false);
		secondCalendar.setProcessFullDayEvent(true);
		secondCalendar.setProcessNotRegistredOnEvent(false);
		secondCalendar.setReadOnly(false);

		final CalendarConfigTableRowData thirdCalendar = calendarsDataTableAnswer.getCalendarConfigTable().addRow();
		thirdCalendar.setUserId(1L);
		thirdCalendar.setAddEventToCalendar(false);
		thirdCalendar.setCalendarConfigurationId(1L);
		thirdCalendar.setExternalId("UnitTestUnusedCalendarExternalId");
		thirdCalendar.setMain(true);
		thirdCalendar.setName("UnitTestUnused");
		thirdCalendar.setOAuthCredentialId(1L);
		thirdCalendar.setProcess(false);
		thirdCalendar.setProcessFreeEvent(false);
		thirdCalendar.setProcessFullDayEvent(true);
		thirdCalendar.setProcessNotRegistredOnEvent(false);
		thirdCalendar.setReadOnly(false);

		final CalendarConfigTableRowData FourthCalendar = calendarsDataTableAnswer.getCalendarConfigTable().addRow();
		FourthCalendar.setUserId(1L);
		FourthCalendar.setAddEventToCalendar(false);
		FourthCalendar.setCalendarConfigurationId(1L);
		FourthCalendar.setExternalId("UnitTestAutoCalendarExternalId");
		FourthCalendar.setMain(true);
		FourthCalendar.setName("UnitTestAuto");
		FourthCalendar.setOAuthCredentialId(1L);
		FourthCalendar.setProcess(false);
		FourthCalendar.setProcessFreeEvent(false);
		FourthCalendar.setProcessFullDayEvent(false);
		FourthCalendar.setProcessNotRegistredOnEvent(false);
		FourthCalendar.setReadOnly(true);

		Mockito.when(this.calendarConfigurationMockSvc.getCalendarConfigurationTableData(Matchers.any(Boolean.class)))
				.thenReturn(calendarsDataTableAnswer);
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
		final OnBoardingUserForm form = new OnBoardingUserForm();

		form.getUserIdField().setValue(1L);

		form.startModify();

		form.getLoginField().setValue("ANewUnitTestLogin");

		form.doSave();
		form.doClose();
	}

}
