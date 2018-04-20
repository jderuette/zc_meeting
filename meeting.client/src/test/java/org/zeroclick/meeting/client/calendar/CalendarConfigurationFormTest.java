package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class CalendarConfigurationFormTest {

	@BeanMock
	private ICalendarConfigurationService m_mockSvc;

	@Before
	public void setUp() {
		final CalendarConfigurationFormData mainGoogleAnswer = new CalendarConfigurationFormData();

		mainGoogleAnswer.getUserId().setValue(1L);
		mainGoogleAnswer.getCalendarConfigurationId().setValue(1L);
		mainGoogleAnswer.getAddEventToCalendar().setValue(true);
		mainGoogleAnswer.getExternalId().setValue("UnitTestMainCalendarExternalId");
		mainGoogleAnswer.getMain().setValue(true);
		mainGoogleAnswer.getName().setValue("UnitTestMain");
		mainGoogleAnswer.getOAuthCredentialId().setValue(1L);
		mainGoogleAnswer.getProcess().setValue(true);
		mainGoogleAnswer.getProcessFreeEvent().setValue(false);
		mainGoogleAnswer.getProcessFullDayEvent().setValue(true);
		mainGoogleAnswer.getProcessFreeEvent().setValue(false);
		mainGoogleAnswer.getReadOnly().setValue(false);

		Mockito.when(this.m_mockSvc.prepareCreate(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(mainGoogleAnswer);
		Mockito.when(this.m_mockSvc.create(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(mainGoogleAnswer);
		Mockito.when(this.m_mockSvc.load(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(mainGoogleAnswer);
		Mockito.when(this.m_mockSvc.store(Matchers.any(CalendarConfigurationFormData.class)))
				.thenReturn(mainGoogleAnswer);
	}

	@Test
	public void testStartModify() {
		final CalendarConfigurationForm form = new CalendarConfigurationForm();

		form.getCalendarConfigurationIdField().setValue(1L);
		form.startModify();

		form.getProcessFreeEventField().setValue(true);

		form.doSave();
		form.doClose();
	}
}
