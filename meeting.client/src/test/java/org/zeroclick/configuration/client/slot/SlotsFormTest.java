package org.zeroclick.configuration.client.slot;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
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
import org.zeroclick.configuration.shared.slot.SlotTablePageData;
import org.zeroclick.configuration.shared.slot.SlotTablePageData.SlotTableRowData;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SlotsFormTest {

	@BeanMock
	private ISlotService mockSvc;

	@Before
	public void setUp() throws ParseException {
		final DayDurationFormData answer = new DayDurationFormData();
		answer.setDayDurationId(1L);
		answer.setName("UnitTestSlot");
		answer.setSlotCode("Morning");
		answer.setSlotId(3L);
		answer.setUserId(1L);

		answer.getWeeklyPerpetual().setValue(true);
		answer.getOrderInSlot().setValue(1L);

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

		Mockito.when(this.mockSvc.load(Matchers.any(DayDurationFormData.class))).thenReturn(answer);
		Mockito.when(this.mockSvc.store(Matchers.any(DayDurationFormData.class))).thenReturn(answer);

		final SlotTablePageData slotList = new SlotTablePageData();
		// morning in workDays
		final SlotTableRowData firstDayDuration = slotList.addRow();

		firstDayDuration.setDayDurationId(2L);
		firstDayDuration.setName("FirstDayDuration");
		firstDayDuration.setSlot(3L);
		firstDayDuration.setUserId(1L);
		firstDayDuration.setWeeklyPerpetual(true);
		firstDayDuration.setOrderInSlot(1);

		firstDayDuration.setMonday(true);
		firstDayDuration.setTuesday(true);
		firstDayDuration.setWednesday(true);
		firstDayDuration.setThursday(true);
		firstDayDuration.setFriday(true);
		firstDayDuration.setSaturday(false);
		firstDayDuration.setSunday(false);

		firstDayDuration.setSlotStart(formatter.parse("1970-01-01 08:00:00"));
		firstDayDuration.setSlotEnd(formatter.parse("1970-01-01 12:00:00"));

		// Afternoon in workDays
		final SlotTableRowData secondDayDuration = slotList.addRow();
		secondDayDuration.setDayDurationId(3L);
		secondDayDuration.setName("FirstDayDuration");
		secondDayDuration.setSlot(3L);
		secondDayDuration.setUserId(1L);
		secondDayDuration.setWeeklyPerpetual(true);
		secondDayDuration.setOrderInSlot(2);

		secondDayDuration.setMonday(true);
		secondDayDuration.setTuesday(true);
		secondDayDuration.setWednesday(true);
		secondDayDuration.setThursday(true);
		secondDayDuration.setFriday(true);
		secondDayDuration.setSaturday(false);
		secondDayDuration.setSunday(false);

		secondDayDuration.setSlotStart(formatter.parse("1970-01-01 14:00:00"));
		secondDayDuration.setSlotEnd(formatter.parse("1970-01-01 18:00:00"));

		// WeekEnd
		final SlotTableRowData thirdDayDuration = slotList.addRow();

		thirdDayDuration.setDayDurationId(2L);
		thirdDayDuration.setName("FirstDayDuration");
		thirdDayDuration.setSlot(3L);
		thirdDayDuration.setUserId(1L);
		thirdDayDuration.setWeeklyPerpetual(true);
		thirdDayDuration.setOrderInSlot(1);

		thirdDayDuration.setMonday(false);
		thirdDayDuration.setTuesday(false);
		thirdDayDuration.setWednesday(false);
		thirdDayDuration.setThursday(false);
		thirdDayDuration.setFriday(false);
		thirdDayDuration.setSaturday(true);
		thirdDayDuration.setSunday(true);

		thirdDayDuration.setSlotStart(formatter.parse("1970-01-01 10:00:00"));
		thirdDayDuration.setSlotEnd(formatter.parse("1970-01-01 19:00:00"));

		Mockito.when(this.mockSvc.getDayDurationTableData(Matchers.any(SearchFilter.class))).thenReturn(slotList);
	}

	@Test
	public void testModify() {
		final SlotsForm form = new SlotsForm();

		form.startModify();
		// remove Thursday morning
		form.getSlotsTableField().getTable().getThursdayColumn().setValue(0, false);

		// remove Sunday from WeekEnd
		form.getSlotsTableField().getTable().getSundayColumn().setValue(2, false);

		form.doSave();
		form.doClose();
	}
}
