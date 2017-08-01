package org.zeroclick.meeting.client.event;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.common.email.IMailSender;
import org.zeroclick.common.email.MailException;
import org.zeroclick.configuration.client.user.UserForm;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.client.Desktop;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.client.common.DurationLookupCall;
import org.zeroclick.meeting.client.common.EventStateLookupCall;
import org.zeroclick.meeting.client.common.SlotLookupCall;
import org.zeroclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.DurationField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EmailField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.EndDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerEmailField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.ReasonField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.SlotField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.StartDateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.StateField;
import org.zeroclick.meeting.client.event.EventForm.MainBox.SubjectField;
import org.zeroclick.meeting.shared.event.CreateEventPermission;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.KnowEmailLookupCall;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;

@FormData(value = EventFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class EventForm extends AbstractForm {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	// represents the Event primary key
	private Long eventId;

	private String externalIdOrganizer;
	private String externalIdRecipient;
	/** to know who performed the last action (for notification) **/
	private Long lastModifier;

	@FormData
	public Long getEventId() {
		return this.eventId;
	}

	@FormData
	public void setEventId(final Long eventId) {
		this.eventId = eventId;
	}

	@FormData
	public String getExternalIdOrganizer() {
		return this.externalIdOrganizer;
	}

	@FormData
	public void setExternalIdOrganizer(final String eventExternalIdOrganizer) {
		this.externalIdOrganizer = eventExternalIdOrganizer;
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
	public Long getLastModifier() {
		return this.lastModifier;
	}

	@FormData
	public void setLastModifier(final Long lastModifier) {
		this.lastModifier = lastModifier;
	}

	@Override
	public Object computeExclusiveKey() {
		return this.getEventId();
	}

	@Override
	protected int getConfiguredDisplayHint() {
		return IForm.DISPLAY_HINT_VIEW;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event");
	}

	@Override
	protected void execInitForm() {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();
		this.getOrganizerEmailField().setValue(userDetails.getEmail().getValue());
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public void startAccept() {
		this.startInternal(new AcceptHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public EmailField getEmailField() {
		return this.getFieldByClass(EmailField.class);
	}

	public SlotField getSlotField() {
		return this.getFieldByClass(SlotField.class);
	}

	public DurationField getDurationField() {
		return this.getFieldByClass(DurationField.class);
	}

	public StateField getStateField() {
		return this.getFieldByClass(StateField.class);
	}

	public StartDateField getStartDateField() {
		return this.getFieldByClass(StartDateField.class);
	}

	public EndDateField getMyDateField() {
		return this.getFieldByClass(EndDateField.class);
	}

	public OrganizerField getOrganizerField() {
		return this.getFieldByClass(OrganizerField.class);
	}

	public OrganizerEmailField getOrganizerEmailField() {
		return this.getFieldByClass(OrganizerEmailField.class);
	}

	public GuestIdField getGuestIdField() {
		return this.getFieldByClass(GuestIdField.class);
	}

	public SubjectField getSubjectField() {
		return this.getFieldByClass(SubjectField.class);
	}

	public ReasonField getReasonField() {
		return this.getFieldByClass(ReasonField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
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

		@Order(900)
		public class OrganizerField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(950)
		public class OrganizerEmailField extends org.zeroclick.ui.form.fields.emailfield.EmailField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.host");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

		}

		@Order(975)
		public class SubjectField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.subject");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (null != rawValue && rawValue.length() > 150) {
					throw new VetoException(TEXTS.get("zc.meeting.event.subject.tooLong"));
				}
				return rawValue;
			}
		}

		@Order(1000)
		public class EmailField extends AbstractProposalField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.attendeeEmail");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return KnowEmailLookupCall.class;
			}

			// some validation logic done on save due to limitation in
			// proposalField (No maxLeng, execvalidate final
		}

		@Order(1500)
		public class GuestIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.attendeeId");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(2000)
		public class SlotField extends AbstractSmartField<Integer> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.slot");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return SlotLookupCall.class;
			}
		}

		@Order(3000)
		public class DurationField extends AbstractSmartField<Integer> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.duration");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return DurationLookupCall.class;
			}
		}

		@Order(4000)
		public class StateField extends AbstractSmartField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.state");
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return EventStateLookupCall.class;
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(5000)
		public class StartDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.start");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(6000)
		public class EndDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.end");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(7000)
		public class ReasonField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.rejectReason");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			public String getLabel() {
				return TEXTS.get("zc.meeting.scheduleMeeting");
			}

			@Override
			protected void execClickAction() {
				final Desktop desktop = (Desktop) ClientSession.get().getDesktop();
				desktop.addNotification(IStatus.INFO, 5000l, Boolean.TRUE, "zc.meeting.notification.creatingEvent");
			}
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Cancel");
			}
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.load(formData);
			EventForm.this.importFormData(formData);

			EventForm.this.setEnabledPermission(new UpdateEventPermission(formData.getEventId()));

			this.getForm().setSubTitle(EventForm.this.calculateSubTitle());
		}

		@Override
		protected void execStore() {
			final IEventService service = BEANS.get(IEventService.class);

			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			EventForm.this.importFormData(formData);

			this.getForm().setDisplayHint(IForm.DISPLAY_HINT_DIALOG);

			EventForm.this.setEnabledPermission(new CreateEventPermission());
		}

		@Override
		protected void execStore() {
			EventForm.this.checkAttendeeEmail();

			final IEventService eventService = BEANS.get(IEventService.class);
			final IUserService userService = BEANS.get(IUserService.class);
			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);

			final String eventGuestEmail = formData.getEmail().getValue();
			final String eventHeldEmail = formData.getOrganizerEmail().getValue();
			final Long eventGuest = userService.getUserIdByEmail(eventGuestEmail);
			final String meetingSubject = formData.getSubject().getValue();

			if (null == eventGuest) {
				final UserForm userForm = new UserForm();
				userForm.autoFillInviteUser(eventGuestEmail, eventHeldEmail, meetingSubject);
				// eventGuest = form.getUserIdField().getValue();
				formData.getGuestId().setValue(userForm.getUserIdField().getValue());
			} else {
				this.sendNewEventEmail(formData);
			}

			eventService.create(formData);
		}

		private void sendNewEventEmail(final EventFormData formData) {
			final IMailSender mailSender = BEANS.get(IMailSender.class);
			final String recipient = formData.getEmail().getValue();
			final String organizerEmail = formData.getOrganizerEmail().getValue();
			final String meetingSubject = formData.getSubject().getValue();
			final String subject = TEXTS.get("zc.meeting.email.event.new.subject", organizerEmail);
			final String content = TEXTS.get("zc.meeting.email.event.new.html", organizerEmail,
					new ApplicationUrlProperty().getValue(), meetingSubject);
			// final String subject = "ZeroClick Meeting : " + organizerEmail +
			// " ask for a meeting";
			// final String content = "Go to " + new
			// ApplicationUrlProperty().getValue() + " to answer him."
			// + " <br /> <br />Best Regards<br /> ZeroClick Meeting Team";
			try {
				mailSender.sendEmail(recipient, subject, content);
			} catch (final MailException e) {
				throw new VetoException(TEXTS.get("zc.common.cannotSendEmail"));
			}
		}
	}

	public class AcceptHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IEventService service = BEANS.get(IEventService.class);
			EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData = service.load(formData);
			EventForm.this.importFormData(formData);

			EventForm.this.setEnabledPermission(new UpdateEventPermission(formData.getEventId()));
		}

		@Override
		protected void execStore() {
			final IEventService service = BEANS.get(IEventService.class);
			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);
			formData.getState().setValue("ACCEPTED");
			service.store(formData);
		}
	}

	private String calculateSubTitle() {
		final DurationLookupCall durationLookupCall = BEANS.get(DurationLookupCall.class);
		final SlotLookupCall slotLookupCall = BEANS.get(SlotLookupCall.class);

		final String durationText = durationLookupCall.getText(this.getDurationField().getValue());
		final String slotText = slotLookupCall.getText(this.getSlotField().getValue());

		return StringUtility.join(" ", durationText, slotText, "\r\n", TEXTS.get("zc.common.with"),
				this.getEmailField().getValue());
	}

	protected void checkAttendeeEmail() {
		final Integer maxCaracters = 128;
		// force to lowerCase
		EventForm.this.getEmailField().setValue(EventForm.this.getEmailField().getValue().toLowerCase());
		final String rawValue = EventForm.this.getEmailField().getValue();

		if (rawValue != null) {
			if (!Pattern.matches(EMAIL_PATTERN, rawValue)) {
				throw new VetoException(TEXTS.get("zc.common.badEmailAddress"));
			}

			if (rawValue.equals(EventForm.this.getOrganizerEmailField().getValue())) {
				throw new VetoException(TEXTS.get("zc.meeting.youCannotInviteYourself"));
			}
			if (rawValue.length() > maxCaracters) {
				throw new VetoException(TEXTS.get("zc.common.tooLong", TEXTS.get("zc.meeting.attendeeEmail"),
						String.valueOf(maxCaracters)));
			}
		}
	}

}
