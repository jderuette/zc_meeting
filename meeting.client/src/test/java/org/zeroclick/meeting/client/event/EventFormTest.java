package org.zeroclick.meeting.client.event;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.EventStateCodeType;

@RunWithSubject("anonymous")
@RunWith(ClientTestRunner.class)
@RunWithClientSession(TestEnvironmentClientSession.class)
public class EventFormTest {

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
		accepetedEventAnswer.setPreviousState(EventStateCodeType.WaitingCode.ID);

		accepetedEventAnswer.getState().setValue(EventStateCodeType.PlannedCode.ID);
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

		Mockito.when(this.mockSvc.prepareCreate(Matchers.any(EventFormData.class))).thenReturn(accepetedEventAnswer);
		Mockito.when(this.mockSvc.create(Matchers.any(EventFormData.class))).thenReturn(accepetedEventAnswer);
		Mockito.when(this.mockSvc.load(Matchers.any(EventFormData.class))).thenReturn(accepetedEventAnswer);
		Mockito.when(this.mockSvc.store(Matchers.any(EventFormData.class))).thenReturn(accepetedEventAnswer);

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
	public void testStartNew() {
		final EventForm form = new EventForm();

		form.setPreviousState(null);
		form.getDurationField().setValue(DurationCodeType.HalfHourCode.ID);
		form.getEmailField().setValue("ANewUnitTestUser@SomePRovider42.com");
		form.getSubjectField().setValue("A New Unit Test Meeting");
		form.getVenueField().setValue("Just here");

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartNewWithLimitedTimeFrame() {
		final EventForm form = new EventForm();

		form.setPreviousState(null);
		form.getDurationField().setValue(DurationCodeType.HalfHourCode.ID);
		form.getEmailField().setValue("ANewUnitTestUser@SomePRovider42.com");
		form.getSubjectField().setValue("A New Unit Test Meeting with Limited TimeFrame");
		form.getVenueField().setValue("There, not here ! There I said");

		final Calendar minimalStart = new GregorianCalendar();
		minimalStart.add(Calendar.DAY_OF_YEAR, 1); // tomorow
		minimalStart.set(Calendar.HOUR_OF_DAY, 9);
		minimalStart.set(Calendar.MINUTE, 45);
		minimalStart.set(Calendar.SECOND, 00);
		minimalStart.set(Calendar.MILLISECOND, 00);

		final Calendar maximalStart = new GregorianCalendar();
		maximalStart.add(Calendar.DAY_OF_YEAR, 1); // tomorow
		maximalStart.set(Calendar.HOUR_OF_DAY, 18);
		maximalStart.set(Calendar.MINUTE, 45);
		maximalStart.set(Calendar.SECOND, 00);
		maximalStart.set(Calendar.MILLISECOND, 00);

		final Date minimalStartDate = minimalStart.getTime();
		final Date maximalStartDate = maximalStart.getTime();

		form.getMinimalStartDateField().setValue(minimalStartDate);
		form.getMaximalStartDateField().setValue(maximalStartDate);

		form.doSave();
		form.doClose();
	}

	@Test
	public void testStartDelete() {
		final EventForm form = new EventForm();

		form.setEventId(1L);

		form.doSave();
		form.doClose();
	}
}
