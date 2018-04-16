package org.zeroclick.meeting.client.calendar;

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
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.testing.BatchLookupServiceClient;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CalendarsConfigurationFormTest {

	@BeanMock
	private ICalendarConfigurationService mockSvc;

	private IBean<?> beanRegistration;
	private IBean<?> batchLookupBeanRegistration;

	@Before
	public void setUp() {
		final CalendarsConfigurationFormData answer = new CalendarsConfigurationFormData();

		final CalendarConfigTableRowData firstCalendar = answer.getCalendarConfigTable().addRow();
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

		final CalendarConfigTableRowData secondCalendar = answer.getCalendarConfigTable().addRow();
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

		final CalendarConfigTableRowData thirdCalendar = answer.getCalendarConfigTable().addRow();
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

		final CalendarConfigTableRowData FourthCalendar = answer.getCalendarConfigTable().addRow();
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

		Mockito.when(this.mockSvc.getCalendarConfigurationTableData(Matchers.any(Boolean.class))).thenReturn(answer);

		final BeanMetaData apiLookupServiceBean = new BeanMetaData(ApiLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(apiLookupServiceBean);

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
		final CalendarsConfigurationForm form = new CalendarsConfigurationForm();

		form.startModify();

		form.doSave();
		form.doClose();
	}
}
