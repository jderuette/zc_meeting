package org.zeroclick.configuration.client.slot;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.ISlotService;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DayDurationFormTest {

	@BeanMock
	private ISlotService mockSvc;
	private IBean<?> beanRegistration;

	@Before
	public void setUp() throws ParseException {
		final DayDurationFormData answer = new DayDurationFormData();
		answer.setDayDurationId(1L);
		answer.setName("UnitTestSlot");
		answer.setSlotCode("Morning");
		answer.setSlotId(3L);
		answer.setUserId(1L);

		answer.getMonday().setValue(true);
		answer.getTuesday().setValue(true);
		answer.getWednesday().setValue(true);
		answer.getThursday().setValue(true);
		answer.getFriday().setValue(true);
		answer.getSaturday().setValue(false);
		answer.getSunday().setValue(false);

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		answer.getSlotStart().setValue(formatter.parse("1970-01-01 08:00:00"));
		answer.getSlotEnd().setValue(formatter.parse("1970-01-01 12:00:00"));

		answer.getWeeklyPerpetual().setValue(true);
		answer.getOrderInSlot().setValue(1L);

		Mockito.when(this.mockSvc.load(Matchers.any(DayDurationFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(DayDurationFormData.class))).thenReturn(answer);
	}

	@Test
	@RunWithClientSession(TestEnvironmentClientSession.class)
	public void testModify() {
		final DayDurationForm form = new DayDurationForm();

		form.setDayDurationId(1L);

		form.startModify();
		form.getMainBox().getWorkDayBox().getThursdayField().setValue(false);

		form.doSave();
		form.doClose();
	}

}
