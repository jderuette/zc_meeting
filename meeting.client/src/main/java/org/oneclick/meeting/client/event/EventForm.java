package org.oneclick.meeting.client.event;

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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.oneclick.common.email.IMailSender;
import org.oneclick.common.email.MailException;
import org.oneclick.configuration.client.user.UserForm;
import org.oneclick.configuration.shared.user.IUserService;
import org.oneclick.configuration.shared.user.UserFormData;
import org.oneclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.oneclick.meeting.client.common.DurationLookupCall;
import org.oneclick.meeting.client.common.EventStateLookupCall;
import org.oneclick.meeting.client.common.SlotLookupCall;
import org.oneclick.meeting.client.event.EventForm.MainBox.CancelButton;
import org.oneclick.meeting.client.event.EventForm.MainBox.DurationField;
import org.oneclick.meeting.client.event.EventForm.MainBox.EmailField;
import org.oneclick.meeting.client.event.EventForm.MainBox.EndDateField;
import org.oneclick.meeting.client.event.EventForm.MainBox.GuestIdField;
import org.oneclick.meeting.client.event.EventForm.MainBox.OkButton;
import org.oneclick.meeting.client.event.EventForm.MainBox.OrganizerEmailField;
import org.oneclick.meeting.client.event.EventForm.MainBox.OrganizerField;
import org.oneclick.meeting.client.event.EventForm.MainBox.SlotField;
import org.oneclick.meeting.client.event.EventForm.MainBox.StartDateField;
import org.oneclick.meeting.client.event.EventForm.MainBox.StateField;
import org.oneclick.meeting.client.event.EventForm.MainBox.SubjectField;
import org.oneclick.meeting.shared.event.CreateEventPermission;
import org.oneclick.meeting.shared.event.EventFormData;
import org.oneclick.meeting.shared.event.IEventService;
import org.oneclick.meeting.shared.event.UpdateEventPermission;

@FormData(value = EventFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class EventForm extends AbstractForm {

	// represents the Event primary key
	private Long eventId;

	private String externalIdOrganizer;
	private String externalIdRecipient;

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
		return TEXTS.get("Event");
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
				return TEXTS.get("Organizer");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(950)
		public class OrganizerEmailField extends AbstractStringField {
			private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Organizer");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (rawValue != null && !Pattern.matches(EMAIL_PATTERN, rawValue)) {
					throw new VetoException(TEXTS.get("BadEmailAddress"));
				}
				return null == rawValue ? null : rawValue.toLowerCase();
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(975)
		public class SubjectField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Event.subject");
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
					throw new VetoException(TEXTS.get("event.subject.tooLong"));
				}
				return rawValue;
			}
		}

		@Order(1000)
		public class EmailField extends AbstractStringField {
			private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("AttendeeEmail");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected String execValidateValue(final String rawValue) {
				if (rawValue != null) {
					if (!Pattern.matches(EMAIL_PATTERN, rawValue)) {
						throw new VetoException(TEXTS.get("BadEmailAddress"));
					}

					if (rawValue.equals(EventForm.this.getOrganizerEmailField().getValue())) {
						throw new VetoException(TEXTS.get("YouCannotInviteYourself"));
					}
				}
				return null == rawValue ? null : rawValue.toLowerCase();
			}
		}

		@Order(1500)
		public class GuestIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("AttendeeId");
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
				return TEXTS.get("Slot");
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
				return TEXTS.get("Duration");
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
				return TEXTS.get("State");
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
				return TEXTS.get("Start");
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
				return TEXTS.get("End");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			public String getLabel() {
				return TEXTS.get("ScheduleMeeting");
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
			final IEventService eventService = BEANS.get(IEventService.class);
			final IUserService userService = BEANS.get(IUserService.class);
			final EventFormData formData = new EventFormData();
			EventForm.this.exportFormData(formData);

			final String eventGuestEmail = formData.getEmail().getValue();
			final String eventHeldEmail = formData.getOrganizerEmail().getValue();
			final Long eventGuest = userService.getUserIdByEmail(eventGuestEmail);

			if (null == eventGuest) {
				final UserForm userForm = new UserForm();
				userForm.autoFillInviteUser(eventGuestEmail, eventHeldEmail);
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
			final String subject = TEXTS.get("email.event.new.subject", organizerEmail);
			final String content = TEXTS.get("email.event.new.html", organizerEmail,
					new ApplicationUrlProperty().getValue(), meetingSubject);
			// final String subject = "ZeroClick Meeting : " + organizerEmail +
			// " ask for a meeting";
			// final String content = "Go to " + new
			// ApplicationUrlProperty().getValue() + " to answer him."
			// + " <br /> <br />Best Regards<br /> ZeroClick Meeting Team";
			try {
				mailSender.sendEmail(recipient, subject, content);
			} catch (final MailException e) {
				throw new VetoException(TEXTS.get("CannotSendEmail"));
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

		return StringUtility.join(" ", durationText, slotText, "\r\n", TEXTS.get("With"),
				this.getEmailField().getValue());
	}

}
