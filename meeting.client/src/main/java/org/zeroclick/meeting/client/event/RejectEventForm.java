package org.zeroclick.meeting.client.event;

import java.io.IOException;

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
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.common.UserAccessRequiredException;
import org.zeroclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.EmailField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.OrganizerEmailField;
import org.zeroclick.meeting.client.event.RejectEventForm.MainBox.SubjectField;
import org.zeroclick.meeting.client.google.api.GoogleApiHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;

import com.google.api.services.calendar.Calendar;

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
		Calendar attendeeGCalSrv;
		Calendar hostGCalSrv;

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

			this.attendeeGCalSrv = this.getCalendarService(formData.getGuestId());
			this.hostGCalSrv = this.getCalendarService(formData.getOrganizer());

		}

		private Calendar getCalendarService(final Long userId) {
			Calendar gCalendarSrv = null;
			final GoogleApiHelper googleHelper = GoogleApiHelper.get();
			try {
				if (null != userId) {
					gCalendarSrv = googleHelper.getCalendarService(userId);
				}
			} catch (final UserAccessRequiredException uare) {
				LOG.debug("No calendar provider for user " + userId);
			} catch (final IOException e) {
				throw new VetoException(TEXTS.get("ErrorAndRetryTextDefault"));
			}

			return gCalendarSrv;
		}

		@Override
		protected void execStore() {
			final RejectEventFormData formData = new RejectEventFormData();
			RejectEventForm.this.exportFormData(formData);

			if (null != RejectEventForm.this.getExternalIdOrganizer() && null != this.hostGCalSrv) {
				try {
					LOG.info(this.buildRejectLog("Deleting", RejectEventForm.this.getEventId(),
							RejectEventForm.this.getExternalIdOrganizer(), RejectEventForm.this.getOrganizer()));
					this.hostGCalSrv.events().delete("primary", RejectEventForm.this.getExternalIdOrganizer())
							.execute();
				} catch (final IOException e) {
					LOG.error(
							this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
									RejectEventForm.this.getExternalIdOrganizer(), RejectEventForm.this.getOrganizer()),
							e);
					throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
				}
			}

			if (null != RejectEventForm.this.getExternalIdRecipient() && null != this.attendeeGCalSrv) {
				try {
					LOG.info(this.buildRejectLog("Deleting", RejectEventForm.this.getEventId(),
							RejectEventForm.this.getExternalIdRecipient(), RejectEventForm.this.getGuestId()));
					this.attendeeGCalSrv.events().delete("primary", RejectEventForm.this.getExternalIdRecipient())
							.execute();
				} catch (final IOException e) {
					LOG.error(
							this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
									RejectEventForm.this.getExternalIdRecipient(), RejectEventForm.this.getGuestId()),
							e);
					throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
				}
			}

			final IEventService service = BEANS.get(IEventService.class);

			formData.setState("REFUSED");
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
	}

	private void sendEmail(final EventFormData formData) {
		final IMailSender mailSender = BEANS.get(IMailSender.class);
		final String organizerEmail = formData.getOrganizerEmail().getValue();
		final String guestEmail = formData.getEmail().getValue();
		final String eventSubject = formData.getSubject().getValue();

		String destEmail;
		String senderEmail;

		if (this.isAskByHost()) {
			senderEmail = organizerEmail;
			destEmail = guestEmail;
		} else {
			senderEmail = guestEmail;
			destEmail = organizerEmail;
		}

		String subject = null;
		String content = null;

		switch (RejectEventForm.this.subAction) {
		case ACTION_REJECT:
			subject = TEXTS.get("zc.meeting.email.refuse.subject", senderEmail);
			content = TEXTS.get("zc.meeting.email.refuse.html", senderEmail, eventSubject,
					new ApplicationUrlProperty().getValue());
			break;
		case ACTION_CANCEL:
			subject = TEXTS.get("zc.meeting.email.cancel.subject", senderEmail);
			content = TEXTS.get("zc.meeting.email.cancel.html", senderEmail, eventSubject);
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
