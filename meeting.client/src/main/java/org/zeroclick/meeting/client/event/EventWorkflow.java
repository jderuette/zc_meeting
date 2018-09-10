/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.client.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.meeting.service.CalendarService;
import org.zeroclick.meeting.service.CalendarService.EventIdentification;
import org.zeroclick.meeting.service.ParticipantWithStatus;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventStateCodeType;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;
import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData;
import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData.InvolvementTableRowData;
import org.zeroclick.meeting.shared.event.involevment.InvolvmentStateCodeType;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class EventWorkflow {

	private static final Logger LOG = LoggerFactory.getLogger(EventWorkflow.class);

	public Boolean canReject(final Long eventId) {
		final IInvolvementService involvementService = BEANS.get(IInvolvementService.class);
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);

		Boolean canReject = Boolean.FALSE;

		if (ACCESS.check(new UpdateEventPermission(eventId))) {
			final InvolvementFormData userInvolvementData = involvementService
					.load(acsHelper.getZeroClickUserIdOfCurrentSubject(), eventId);

			final String userInvolvementSate = userInvolvementData.getState().getValue();

			if (!InvolvmentStateCodeType.RefusedCode.ID.equals(userInvolvementSate)) {
				canReject = Boolean.TRUE;
			}
		}

		return canReject;
	}

	public Boolean canAccept(final Long eventId) {
		final IEventService eventService = BEANS.get(IEventService.class);
		final IInvolvementService involvementService = BEANS.get(IInvolvementService.class);
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);

		Boolean canAccept = Boolean.FALSE;

		if (ACCESS.check(new UpdateEventPermission(eventId))) {
			final Boolean isRecipient = eventService.isRecipient(eventId);
			final InvolvementFormData userInvolvementData = involvementService
					.load(acsHelper.getZeroClickUserIdOfCurrentSubject(), eventId);

			final String userInvolvementSate = userInvolvementData.getState().getValue();

			if (isRecipient && InvolvmentStateCodeType.AskedCode.ID.equals(userInvolvementSate)) {
				canAccept = Boolean.TRUE;
			}
		}

		return canAccept;
	}

	public void acceptEvent(final EventFormData eventForm, final Long userId) {
		final IInvolvementService involvementService = BEANS.get(IInvolvementService.class);

		// update user involvement status
		involvementService.updateStatusAccepted(eventForm.getEventId(), userId);

		this.handleEventActions(eventForm);
	}

	private void handleEventActions(final EventFormData eventForm) {
		// TODO Djer Decide to create Event, or not (yet)
		this.planEvent(eventForm);
	}

	private void planEvent(final EventFormData eventForm) {
		final CalendarService calendarService = BEANS.get(CalendarService.class);
		final IEventService eventService = BEANS.get(IEventService.class);
		final IInvolvementService involvementService = BEANS.get(IInvolvementService.class);
		final IUserService userService = BEANS.get(IUserService.class);

		/**
		 * Store all external event created that can be shared by user (if same
		 * provider)
		 **/
		final Set<EventIdentification> allExternalEvent = new HashSet<>();

		// save new date
		eventService.store(eventForm);

		// Update Event global State
		final EventFormData fullEventFormData = eventService.storeNewState(eventForm.getEventId(),
				EventStateCodeType.PlannedCode.ID);

		// build required info to PLAN this event
		final InvolvementFormData organizerInvolvement = involvementService.getOrganizer(eventForm.getEventId());
		final String organizerEmail = userService.getUserEmail(organizerInvolvement.getUserId().getValue());
		final InvolvementTablePageData participants = involvementService.getParticipants(eventForm.getEventId());

		final List<ParticipantWithStatus> paticipantsWithStatus = new ArrayList<>();

		if (participants.getRowCount() > 0) {
			for (final InvolvementTableRowData particpant : participants.getRows()) {
				final String participantEmail = userService.getUserEmail(particpant.getUserId());
				final ParticipantWithStatus participantWithStatus = new ParticipantWithStatus(particpant,
						participantEmail);
				paticipantsWithStatus.add(participantWithStatus);
			}
		}

		// create External Event : Organizer first
		final EventIdentification orgaExternalEventId = this.createExternalEvent(fullEventFormData,
				organizerInvolvement.getUserId().getValue(), organizerEmail, paticipantsWithStatus);
		involvementService.updateStatusAccepted(eventForm.getEventId(), organizerInvolvement.getUserId().getValue());
		allExternalEvent.add(orgaExternalEventId);

		final String eventExternalHtmlLink = calendarService.getEventExternalLink(orgaExternalEventId,
				organizerInvolvement.getUserId().getValue());
		this.sendConfirmationEmail(fullEventFormData, eventExternalHtmlLink, organizerEmail,
				organizerInvolvement.getUserId().getValue(), paticipantsWithStatus, Boolean.TRUE);

		// Create the external(s) event : for participants (not organizer)
		if (paticipantsWithStatus.size() > 0) {
			for (final ParticipantWithStatus participant : paticipantsWithStatus) {
				Boolean isAddCalendarConfigured = Boolean.FALSE;

				if (participant.getState().equals(InvolvmentStateCodeType.AcceptedCode.ID)) {

					final CalendarConfigurationFormData calendarToStoreEvent = calendarService
							.getUserCreateEventCalendar(participant.getInvolvementData().getUserId());

					final Long participantProvider = calendarToStoreEvent.getProvider().getValue();

					// search for a valid (same provider) already created
					// ExternaleEvent
					EventIdentification usableExternalEvent = null;
					if (null != allExternalEvent && allExternalEvent.size() > 0) {
						for (final EventIdentification createdExternalEvent : allExternalEvent) {
							if (participantProvider
									.equals(createdExternalEvent.getCalendarData().getProvider().getValue())) {
								usableExternalEvent = createdExternalEvent;
							}
						}
					}

					if (null != usableExternalEvent) {
						// share externalEventId
						final InvolvementFormData updatedInvolvementData = this.setInvolvmentExternalEventId(
								eventForm.getEventId(), participant.getInvolvementData().getUserId(),
								orgaExternalEventId.getExternalEventData().getExternalId().getValue());
						participant.setInvolvementData(updatedInvolvementData);

					} else {
						// as one participant is Holder, the organizer must be
						// added
						// as participant, and THIS participant removed form
						// participant
						usableExternalEvent = this.createExternalEvent(fullEventFormData,
								participant.getInvolvementData().getUserId(), organizerEmail, paticipantsWithStatus);
						allExternalEvent.add(usableExternalEvent);
					}

					if (null != calendarToStoreEvent) {
						isAddCalendarConfigured = Boolean.TRUE;
					}

					final String participantEventExternalHtmlLink = calendarService
							.getEventExternalLink(usableExternalEvent, participant.getInvolvementData().getUserId());

					this.sendConfirmationEmail(fullEventFormData, participantEventExternalHtmlLink,
							participant.getEmail(), participant.getInvolvementData().getUserId(), organizerEmail,
							isAddCalendarConfigured);
				}

				// TODO Djer for user "not accepted", send email to indicate
				// event created and they can participate( but no changing
				// dates)
			}
		}
	}

	private InvolvementFormData setInvolvmentExternalEventId(final Long eventId, final Long userId,
			final Long externalId) {
		final IInvolvementService involvementService = BEANS.get(IInvolvementService.class);

		InvolvementFormData formData = new InvolvementFormData();
		formData.getEventId().setValue(eventId);
		formData.getUserId().setValue(userId);
		formData = involvementService.load(formData);

		formData.getExternalEventId().setValue(externalId);

		formData = involvementService.store(formData);

		return formData;

	}

	private EventIdentification createExternalEvent(final EventFormData eventForm, final Long holderUserId,
			final String organizerEmail, final List<ParticipantWithStatus> participants) {
		final CalendarService calendarService = BEANS.get(CalendarService.class);

		EventIdentification externalEventId;
		try {
			externalEventId = calendarService.createEvent(eventForm.getStartDate().getValue(),
					eventForm.getEndDate().getValue(), holderUserId, organizerEmail, participants,
					eventForm.getSubject().getValue(), eventForm.getVenue().getValue(),
					eventForm.getDescription().getValue());
		} catch (final IOException e) {
			LOG.error("Erro while creating external event for event : " + eventForm.getEventId());
			throw new VetoException("Erro while creating Event, try again");
		}

		final IExternalEventService externalEventService = BEANS.get(IExternalEventService.class);
		final CalendarConfigurationFormData calendarToStoreEvent = externalEventId.getCalendarData();

		// create ExternalId
		ExternalEventFormData externalEventformData = null;
		externalEventformData = new ExternalEventFormData();
		externalEventformData.getExternalEventId().setValue(externalEventId.getEventId());
		externalEventformData.getExternalCalendarId()
				.setValue(externalEventId.getCalendarData().getExternalId().getValue());
		externalEventformData.getApiCredentialId().setValue(calendarToStoreEvent.getOAuthCredentialId().getValue());
		externalEventformData = externalEventService.create(externalEventformData);

		final InvolvementFormData updatedInvolvmentData = this.setInvolvmentExternalEventId(eventForm.getEventId(),
				holderUserId, externalEventformData.getExternalId().getValue());

		externalEventId.setExternalEventData(externalEventformData);

		return externalEventId;
	}

	private void sendNewEventEmail(final EventFormData formData) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);
		final EventMessageHelper eventMessageHelper = BEANS.get(EventMessageHelper.class);
		final String recipient = formData.getEmail().getValue();

		final String[] values = eventMessageHelper.buildValuesForLocaleMessages(formData,
				formData.getGuestId().getValue());

		final String subject = TextsHelper.get(formData.getGuestId().getValue(), "zc.meeting.email.event.new.subject",
				values);

		final String content = TextsHelper.get(formData.getGuestId().getValue(), "zc.meeting.email.event.new.html",
				values);

		try {
			mailSender.sendEmail(recipient, subject, content, Boolean.FALSE);
		} catch (final MailException e) {
			LOG.error("Cannot send email to " + recipient + " with subject : " + subject + " for event ID : "
					+ formData.getEventId(), e);
			throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
		}
	}

	private void sendConfirmationEmail(final EventFormData formData, final String eventHtmlLink, final String recipient,
			final Long recipientUserId, final List<ParticipantWithStatus> otherParticpantEmail,
			final Boolean isAddCalendarConfigured) {
		final StringBuilder otherParticipantsEmails = new StringBuilder(128);

		for (final ParticipantWithStatus participant : otherParticpantEmail) {
			otherParticipantsEmails.append(participant.getEmail()).append(',');
		}

		final String emails = otherParticipantsEmails.toString();

		this.sendConfirmationEmail(formData, eventHtmlLink, recipient, recipientUserId,
				emails.substring(0, emails.length() - 1), isAddCalendarConfigured);

	}

	private void sendConfirmationEmail(final EventFormData formData, final String eventHtmlLink, final String recipient,
			final Long recipientUserId, final String otherParticpantEmail, final Boolean isAddCalendarConfigured) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);
		final EventMessageHelper eventMessageHelper = BEANS.get(EventMessageHelper.class);

		String warningManualManageEvent = "";

		if (!isAddCalendarConfigured) {
			warningManualManageEvent = TextsHelper.get(recipientUserId,
					"zc.meeting.email.event.confirm.requireManualEventManagement");
		}

		final String[] values = eventMessageHelper.buildValuesForLocaleMessages(formData, recipientUserId,
				eventHtmlLink, otherParticpantEmail, warningManualManageEvent);

		final String subject = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.subject", values);
		final String content = TextsHelper.get(recipientUserId, "zc.meeting.email.event.confirm.html", values);

		try {
			mailSender.sendEmail(recipient, subject, content, Boolean.FALSE);
		} catch (final MailException e) {
			throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
		}
	}

}
