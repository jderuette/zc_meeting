package org.zeroclick.meeting.client.event;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.EmailField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.OrganizerEmailField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.ReasonField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.SubjectField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.VenueField;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper.ApiCalendar;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.StateCodeType;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;

@FormData(value = RejectEventFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class RejectEventForm extends AbstractForm {

	private static final Logger LOG = LoggerFactory.getLogger(RejectEventForm.class);

	private static final int ACTION_REJECT = 0;
	private static final int ACTION_CANCEL = 1;

	private Integer subAction;

	private Long eventId;
	private Long organizerId;
	private Long guestId;
	private String externalIdOrganizer;
	private String externalIdRecipient;
	private String state;
	private ZonedDateTime start;
	private ZonedDateTime end;

	private Boolean askByHost;

	@FormData
	public Long getEventId() {
		return this.eventId;
	}

	@FormData
	public void setEventId(final Long eventId) {
		this.eventId = eventId;
	}

	@FormData
	public Long getOrganizer() {
		return this.organizerId;
	}

	@FormData
	public void setOrganizer(final Long organizer) {
		this.organizerId = organizer;
	}

	@FormData
	public Long getGuestId() {
		return this.guestId;
	}

	@FormData
	public void setGuestId(final Long guestId) {
		this.guestId = guestId;
	}

	@FormData
	public String getExternalIdOrganizer() {
		return this.externalIdOrganizer;
	}

	@FormData
	public void setExternalIdOrganizer(final String extIdOrganizer) {
		this.externalIdOrganizer = extIdOrganizer;
	}

	@FormData
	public String getExternalIdRecipient() {
		return this.externalIdRecipient;
	}

	@FormData
	public void setExternalIdRecipient(final String extIdAttendee) {
		this.externalIdRecipient = extIdAttendee;
	}

	@FormData
	public String getState() {
		return this.state;
	}

	@FormData
	public void setState(final String state) {
		this.state = state;
	}

	@FormData
	public ZonedDateTime getStart() {
		return this.start;
	}

	@FormData
	public void setStart(final ZonedDateTime start) {
		this.start = start;
	}

	@FormData
	public ZonedDateTime getEnd() {
		return this.end;
	}

	@FormData
	public void setEnd(final ZonedDateTime end) {
		this.end = end;
	}

	public Boolean isAskByHost() {
		return this.askByHost;
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.ExclamationMark;
	}

	@Override
	protected IDisplayParent getConfiguredDisplayParent() {
		return ClientSession.get().getDesktop().getOutline();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.rejectEvent");
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return Boolean.FALSE;
	}

	public void startReject(final Boolean isAskByHost) {
		this.subAction = ACTION_REJECT;
		this.askByHost = isAskByHost;
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startCancel(final Boolean isAskByHost) {
		this.subAction = ACTION_CANCEL;
		this.askByHost = isAskByHost;
		this.startInternalExclusive(new ModifyHandler());
	}

	private String buildUnknowSubActionMessage() {
		final StringBuilder builder = new StringBuilder(50);
		builder.append("Unknow sub action ").append(RejectEventForm.this.subAction).append(" for reject Action");
		return builder.toString();
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public OrganizerEmailField getOrganizerEmailField() {
		return this.getFieldByClass(OrganizerEmailField.class);
	}

	public EmailField getEmailField() {
		return this.getFieldByClass(EmailField.class);
	}

	public SubjectField getSubjectField() {
		return this.getFieldByClass(SubjectField.class);
	}

	public GuestIdField getGuestIdField() {
		return this.getFieldByClass(GuestIdField.class);
	}

	public OrganizerField getOrganizerField() {
		return this.getFieldByClass(OrganizerField.class);
	}

	public ReasonField getReasonField() {
		return this.getFieldByClass(ReasonField.class);
	}

	public VenueField getVenueField() {
		return this.getFieldByClass(VenueField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Override
		protected int getConfiguredGridW() {
			return 1;
		}

		@Override
		protected int getConfiguredGridColumnCount() {
			return 1;
		}

		@Order(2000)
		public class OrganizerEmailField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected void execInitField() {
				this.setVisible(!RejectEventForm.this.isAskByHost());
			}
		}

		@Order(3000)
		public class EmailField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.common.email");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected void execInitField() {
				this.setVisible(RejectEventForm.this.isAskByHost());
			}
		}

		@Order(4000)
		public class SubjectField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("EmailSubject");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}
		}

		@Order(4500)
		public class VenueField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.venue");
			}

			@Override
			protected void execInitField() {
				this.setValueChangeTriggerEnabled(Boolean.TRUE);
				this.changeDisplayText();
			}

			@Override
			protected void execChangedValue() {
				this.changeDisplayText();
			}

			private void changeDisplayText() {
				if (this.getValue() != null && !this.getValue().isEmpty()) {
					this.setDisplayText(TextsHelper.get(this.getValue()));
				}
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}
		}

		@Order(5000)
		public class ReasonField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.rejectReason");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.rejectEvent");
			}

			@Override
			protected String getConfiguredLabelBackgroundColor() {
				return "FF3333";
			}

			@Override
			protected void execInitField() {
				switch (RejectEventForm.this.subAction) {
				case ACTION_REJECT:
					this.setLabel(TEXTS.get("zc.meeting.rejectEvent"));
					break;
				case ACTION_CANCEL:
					this.setLabel(TEXTS.get("zc.meeting.cancelEvent"));
					break;
				default:
					LOG.warn(RejectEventForm.this.buildUnknowSubActionMessage());
					break;
				}
			}

			@Override
			protected boolean execIsSaveNeeded() {
				// to force form save even if no modification done in Fields
				return Boolean.TRUE;
			}
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {
		List<ApiCalendar> attendeeGCalSrv;
		List<ApiCalendar> hostGCalSrv;

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			RejectEventFormData formData = new RejectEventFormData();
			RejectEventForm.this.exportFormData(formData);
			formData = service.load(formData);
			RejectEventForm.this.importFormData(formData);
			RejectEventForm.this.checkSaveNeeded();

			RejectEventForm.this.setEnabledPermission(new UpdateEventPermission(formData.getEventId()));

			switch (RejectEventForm.this.subAction) {
			case ACTION_REJECT:
				RejectEventForm.this.setTitle(TEXTS.get("zc.meeting.rejectEvent"));
				RejectEventForm.this.setSubTitle(TEXTS.get("zc.meeting.confirmRejectEvent"));
				break;
			case ACTION_CANCEL:
				RejectEventForm.this.setTitle(TEXTS.get("zc.meeting.cancelEvent"));
				RejectEventForm.this.setSubTitle(TEXTS.get("zc.meeting.confirmCancelEvent"));
				break;
			default:
				LOG.warn(RejectEventForm.this.buildUnknowSubActionMessage());
				break;
			}

			try {
				this.attendeeGCalSrv = this.getCalendarService(formData.getGuestId());
			} catch (final IOException e) {
				LOG.debug("No calendar service configured for user Id : " + formData.getGuestId(), e);
			}
			try {
				this.hostGCalSrv = this.getCalendarService(formData.getOrganizer());
			} catch (final IOException e) {
				LOG.error("No calendar service configured for (organizer) user Id : " + formData.getOrganizer(), e);
				throw new VetoException(TEXTS.get("ErrorAndRetryTextDefault"));
			}
		}

		private List<ApiCalendar> getCalendarService(final Long userId) throws IOException {
			List<ApiCalendar> gCalendarSrv = null;
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
			try {
				if (null != userId) {
					gCalendarSrv = googleHelper.getCalendarsServices(userId);
				}
			} catch (final UserAccessRequiredException uare) {
				LOG.debug("No calendar provider for user " + userId);
			}

			return gCalendarSrv;
		}

		@Override
		protected void execStore() {

			final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
			notificationHelper.addProcessingNotification("zc.meeting.notification.rejectingEvent");

			Jobs.schedule(new IRunnable() {
				@Override
				public void run() {
					final RejectEventFormData formData = new RejectEventFormData();
					RejectEventForm.this.exportFormData(formData);

					if (null != RejectEventForm.this.getExternalIdOrganizer() && null != ModifyHandler.this.hostGCalSrv
							&& ModifyHandler.this.hostGCalSrv.size() > 0) {

						LOG.info(this.buildRejectLog("Deleting", RejectEventForm.this.getEventId(),
								RejectEventForm.this.getExternalIdOrganizer(), RejectEventForm.this.getOrganizer()));
						final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
						Boolean eventDeleted = Boolean.FALSE;

						for (final ApiCalendar hostGCalSrv : ModifyHandler.this.hostGCalSrv) {
							try {
								hostGCalSrv.getCalendar().events()
										.delete(googleHelper
												.getUserCreateEventCalendar(RejectEventForm.this.getOrganizer()),
												RejectEventForm.this.getExternalIdOrganizer())
										.execute();
								eventDeleted = Boolean.TRUE;
							} catch (final IOException e) {
								LOG.warn(this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
										RejectEventForm.this.getExternalIdOrganizer(),
										RejectEventForm.this.getOrganizer()), e);
							}
						}

						if (!eventDeleted) {
							LOG.warn(this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
									RejectEventForm.this.getExternalIdOrganizer(),
									RejectEventForm.this.getOrganizer()));
							throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
						}
					}

					final IEventService service = BEANS.get(IEventService.class);

					formData.setState(StateCodeType.RefusededCode.ID);
					final EventFormData fullEventFormData = service.storeNewState(formData);

					RejectEventForm.this.sendEmail(fullEventFormData);
				}

				private String buildRejectLog(final String prefix, final Long eventId, final String externalId,
						final Long userId) {
					final StringBuilder builder = new StringBuilder(75);
					builder.append(prefix).append(" (Google) event : Id ").append(eventId).append(", external ID : ")
							.append(externalId).append("for user : ").append(userId);
					return builder.toString();
				}
			}, Jobs.newInput().withName("Refusing event {}", RejectEventForm.this.getEventId())
					.withRunContext(ClientRunContexts.copyCurrent()).withThreadName("Refusing event"));
		}

	}

	private void sendEmail(final EventFormData formData) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);
		final String organizerEmail = formData.getOrganizerEmail().getValue();
		final String guestEmail = formData.getEmail().getValue();
		final String eventSubject = formData.getSubject().getValue();
		final String eventReason = formData.getReason().getValue();

		String destEmail;
		Long destId;
		String senderEmail;
		if (this.isAskByHost()) {
			senderEmail = organizerEmail;
			destEmail = guestEmail;
			destId = this.guestId;
		} else {
			senderEmail = guestEmail;
			destEmail = organizerEmail;
			destId = this.organizerId;
		}

		String subject = null;
		String content = null;

		switch (RejectEventForm.this.subAction) {
		case ACTION_REJECT:
			subject = TextsHelper.get(destId, "zc.meeting.email.refuse.subject", senderEmail);
			content = TextsHelper.get(destId, "zc.meeting.email.refuse.html", senderEmail, eventSubject,
					new ApplicationUrlProperty().getValue(), eventReason);
			break;
		case ACTION_CANCEL:
			subject = TextsHelper.get(destId, "zc.meeting.email.cancel.subject", senderEmail);
			content = TextsHelper.get(destId, "zc.meeting.email.cancel.html", senderEmail, eventSubject, eventReason);
			break;
		default:
			LOG.warn("Unknow sub action " + RejectEventForm.this.subAction + " for reject Action");
			break;
		}

		try {
			mailSender.sendEmail(destEmail, subject, content);
		} catch (final MailException e) {
			LOG.error("Cannot send email for : " + destEmail + ", subject : " + subject, e);
			throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
		}

	}
}
