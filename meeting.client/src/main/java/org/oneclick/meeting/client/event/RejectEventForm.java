package org.oneclick.meeting.client.event;

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
import org.oneclick.common.email.IMailSender;
import org.oneclick.common.email.MailException;
import org.oneclick.meeting.client.ClientSession;
import org.oneclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.oneclick.meeting.client.common.UserAccessRequiredException;
import org.oneclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.oneclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.oneclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.oneclick.meeting.client.event.RejectEventForm.MainBox.EmailField;
import org.oneclick.meeting.client.event.RejectEventForm.MainBox.OkButton;
import org.oneclick.meeting.client.event.RejectEventForm.MainBox.OrganizerEmailField;
import org.oneclick.meeting.client.event.RejectEventForm.MainBox.SubjectField;
import org.oneclick.meeting.client.google.api.GoogleApiHelper;
import org.oneclick.meeting.shared.Icons;
import org.oneclick.meeting.shared.event.EventFormData;
import org.oneclick.meeting.shared.event.IEventService;
import org.oneclick.meeting.shared.event.RejectEventFormData;
import org.oneclick.meeting.shared.event.UpdateEventPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void setExternalIdOrganizer(final String externalIdOrganizer) {
		this.externalIdOrganizer = externalIdOrganizer;
	}

	@FormData
	public String getExternalIdRecipient() {
		return this.externalIdRecipient;
	}

	@FormData
	public void setExternalIdRecipient(final String externalIdRecipient) {
		this.externalIdRecipient = externalIdRecipient;
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

	// public void startNew() {
	// this.startInternal(new NewHandler());
	// }

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
				return TEXTS.get("RejectEvent");
			}

			@Override
			protected String getConfiguredLabelBackgroundColor() {
				return "FF3333";
			}

			@Override
			protected void execInitField() {
				switch (RejectEventForm.this.subAction) {
				case ACTION_REJECT:
					this.setLabel(TEXTS.get("RejectEvent"));
					break;
				case ACTION_CANCEL:
					this.setLabel(TEXTS.get("zc.meeting.cancelEvent"));
					break;
				default:
					LOG.warn("Unknow sub action " + RejectEventForm.this.subAction + " for reject Action");
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
		Calendar attendeeCalendarService;
		Calendar hostCalendarService;

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
				RejectEventForm.this.setTitle(TEXTS.get("RejectEvent"));
				RejectEventForm.this.setSubTitle(TEXTS.get("zc.meeting.confirmRejectEvent"));
				break;
			case ACTION_CANCEL:
				RejectEventForm.this.setTitle(TEXTS.get("zc.meeting.cancelEvent"));
				RejectEventForm.this.setSubTitle(TEXTS.get("zc.meeting.confirmCancelEvent"));
				break;
			default:
				LOG.warn("Unknow sub action " + RejectEventForm.this.subAction + " for reject Action");
				break;
			}

			final GoogleApiHelper googleHelper = GoogleApiHelper.get();
			final Long hostId = formData.getOrganizer();
			final Long attendeeId = formData.getGuestId();

			try {
				if (null != attendeeId) {
					this.attendeeCalendarService = googleHelper.getCalendarService(attendeeId);
				}
				if (null != hostId) {
					this.hostCalendarService = googleHelper.getCalendarService(hostId);
				}
			} catch (final UserAccessRequiredException uare) {
				throw new VetoException(TEXTS.get("zc.meeting.calendarProviderRequired"));
			} catch (final IOException e) {
				throw new VetoException(TEXTS.get("ErrorAndRetryTextDefault"));
			}
		}

		@Override
		protected void execStore() {
			final IEventService service = BEANS.get(IEventService.class);
			final RejectEventFormData formData = new RejectEventFormData();
			RejectEventForm.this.exportFormData(formData);

			// TODO Djer13 if there is an external ID, ask provider to
			// delete
			if (null != RejectEventForm.this.getExternalIdOrganizer() && null != this.hostCalendarService) {
				try {
					LOG.info(this.buildRejectLog("Deleting", RejectEventForm.this.getEventId(),
							RejectEventForm.this.getExternalIdOrganizer(), RejectEventForm.this.getOrganizer()));
					this.hostCalendarService.events().delete("primary", RejectEventForm.this.getExternalIdOrganizer())
							.execute();
				} catch (final IOException e) {
					LOG.error(
							this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
									RejectEventForm.this.getExternalIdOrganizer(), RejectEventForm.this.getOrganizer()),
							e);
					throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
				}
			}

			if (null != RejectEventForm.this.getExternalIdRecipient() && null != this.attendeeCalendarService) {
				try {
					LOG.info(this.buildRejectLog("Deleting", RejectEventForm.this.getEventId(),
							RejectEventForm.this.getExternalIdRecipient(), RejectEventForm.this.getGuestId()));
					this.attendeeCalendarService.events()
							.delete("primary", RejectEventForm.this.getExternalIdRecipient()).execute();
				} catch (final IOException e) {
					LOG.error(
							this.buildRejectLog("Error while deleting", RejectEventForm.this.getEventId(),
									RejectEventForm.this.getExternalIdRecipient(), RejectEventForm.this.getGuestId()),
							e);
					throw new VetoException(TEXTS.get("zc.meeting.error.deletingEvent"));
				}
			}
			formData.setState("REFUSED");
			final EventFormData fulleEventFormData = service.storeNewState(formData);

			RejectEventForm.this.sendEmail(fulleEventFormData);
		}

		private String buildRejectLog(final String prefix, final Long eventId, final String externalId,
				final Long userId) {
			final StringBuilder sb = new StringBuilder();
			sb.append(prefix).append(" (Google) event : Id ").append(eventId).append(", external ID : ")
					.append(externalId).append("for user : ").append(userId);
			return sb.toString();
		}
	}

	// public class NewHandler extends AbstractFormHandler {
	//
	// @Override
	// protected void execLoad() {
	// }
	//
	// @Override
	// protected void execStore() {
	// }
	// }

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
