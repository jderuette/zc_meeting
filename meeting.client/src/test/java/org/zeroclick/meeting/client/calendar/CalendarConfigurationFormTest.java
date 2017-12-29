package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
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
	public void setup() {
		CalendarConfigurationFormData answer = new CalendarConfigurationFormData();
		Mockito.when(m_mockSvc.prepareCreate(Matchers.any(CalendarConfigurationFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.create(Matchers.any(CalendarConfigurationFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.load(Matchers.any(CalendarConfigurationFormData.class))).thenReturn(answer);
		Mockito.when(m_mockSvc.store(Matchers.any(CalendarConfigurationFormData.class))).thenReturn(answer);
	}

	// TODO [djer] add test cases
}
