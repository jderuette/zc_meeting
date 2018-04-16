package org.zeroclick.meeting.client.event;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.StateCodeType;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class RejectEventFormTest {

	@BeanMock
	private IEventService mockSvc;

	private IBean<?> beanRegistration;
	private IBean<?> venueBeanRegistration;

	@Before
	public void setUp() {
		final EventFormData accepetedEventAnswer = new EventFormData();

		final Calendar acceptEventCreated = new GregorianCalendar();
		acceptEventCreated.set(118 + 1900, 04, 01, 13, 20, 42);
		final Date acceptEventCreatedDate = acceptEventCreated.getTime();

		final Calendar acceptEventStart = new GregorianCalendar();
		acceptEventStart.set(118 + 1900, 04, 02, 14, 00, 00);
		final Date acceptEventStartDate = acceptEventStart.getTime();

		final Calendar acceptEventEnd = new GregorianCalendar();
		acceptEventEnd.set(118 + 1900, 04, 02, 15, 00, 00);
		final Date acceptEventEndDate = acceptEventEnd.getTime();

		accepetedEventAnswer.setEventId(1L);
		accepetedEventAnswer.setExternalIdOrganizer("AGoogleCalEventId");
		accepetedEventAnswer.setExternalIdRecipient("AGoogleCalEventId");
		accepetedEventAnswer.setPreviousState(StateCodeType.AskedCode.ID);

		accepetedEventAnswer.getState().setValue(StateCodeType.AcceptedCode.ID);
		accepetedEventAnswer.getCreatedDate().setValue(acceptEventCreatedDate);
		accepetedEventAnswer.getDuration().setValue(DurationCodeType.OneHourCode.ID);
		accepetedEventAnswer.getOrganizer().setValue(1L);
		accepetedEventAnswer.getOrganizerEmail().setValue("UnitTest@someProvider42.org");

		accepetedEventAnswer.getStartDate().setValue(acceptEventStartDate);
		accepetedEventAnswer.getEndDate().setValue(acceptEventEndDate);

		accepetedEventAnswer.getGuestId().setValue(2L);
		accepetedEventAnswer.getEmail().setValue("UnitTest2@gmail42.org");

		accepetedEventAnswer.getMaximalStartDate().setValue(null);
		accepetedEventAnswer.getMinimalStartDate().setValue(null);

		accepetedEventAnswer.getReason().setValue(null);
		accepetedEventAnswer.getSubject().setValue("A Unit Test accepted event");
		accepetedEventAnswer.getVenue().setValue("42 Unit Test street 74000 Annecy");

		final RejectEventFormData rejectEventAnswer = new RejectEventFormData();
		final DateHelper dateHelper = BEANS.get(DateHelper.class);
		rejectEventAnswer.setEventId(accepetedEventAnswer.getEventId());
		rejectEventAnswer.setStart(
				dateHelper.getZonedValue(ZoneId.of("Europe/Paris"), accepetedEventAnswer.getStartDate().getValue()));
		rejectEventAnswer.setEnd(
				dateHelper.getZonedValue(ZoneId.of("Europe/Paris"), accepetedEventAnswer.getEndDate().getValue()));
		rejectEventAnswer.setState(accepetedEventAnswer.getState().getValue());
		rejectEventAnswer.setExternalIdOrganizer(accepetedEventAnswer.getExternalIdOrganizer());
		rejectEventAnswer.setExternalIdRecipient(accepetedEventAnswer.getExternalIdRecipient());
		rejectEventAnswer.setGuestId(accepetedEventAnswer.getGuestId().getValue());
		rejectEventAnswer.getEmail().setValue(accepetedEventAnswer.getEmail().getValue());
		rejectEventAnswer.getOrganizerEmail().setValue(accepetedEventAnswer.getOrganizerEmail().getValue());

		Mockito.when(this.mockSvc.load(Matchers.any(RejectEventFormData.class))).thenReturn(rejectEventAnswer);
		Mockito.when(this.mockSvc.storeNewState(Matchers.any(RejectEventFormData.class)))
				.thenReturn(accepetedEventAnswer);

		final BeanMetaData KnowEmailLookupServiceBean = new BeanMetaData(KnowEmailLookupServiceMock.class);
		this.beanRegistration = TestingUtility.registerBean(KnowEmailLookupServiceBean);

		final BeanMetaData VenueLookupServiceBean = new BeanMetaData(VenueLookupServiceMock.class);
		this.venueBeanRegistration = TestingUtility.registerBean(VenueLookupServiceBean);
	}

	@After
	public void tearDown() {
		if (null != this.beanRegistration) {
			TestingUtility.unregisterBean(this.beanRegistration);
		}

		if (null != this.venueBeanRegistration) {
			TestingUtility.unregisterBean(this.venueBeanRegistration);
		}
	}

	@Test
	public void testStartReject() {
		final RejectEventForm form = new RejectEventForm();

		form.setEventId(1L);
		form.startReject(false);

		Assert.assertEquals("Bad Title", TEXTS.get("zc.meeting.rejectEvent"), form.getTitle());
		Assert.assertEquals("Bad SubTitle", TEXTS.get("zc.meeting.confirmRejectEvent"), form.getSubTitle());

		form.getReasonField().setValue("Rejecting a UnitTest event");

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartCancel() {
		final RejectEventForm form = new RejectEventForm();

		form.setEventId(1L);
		form.startCancel(false);

		Assert.assertEquals("Bad Title", TEXTS.get("zc.meeting.cancelEvent"), form.getTitle());
		Assert.assertEquals("Bad SubTitle", TEXTS.get("zc.meeting.confirmCancelEvent"), form.getSubTitle());

		form.getReasonField().setValue("Rejecting a UnitTest event");

		form.doSave();
		form.doClose();
	}
}
