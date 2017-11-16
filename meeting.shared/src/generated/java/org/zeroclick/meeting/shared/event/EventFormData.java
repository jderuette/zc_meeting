package org.zeroclick.meeting.shared.event;

import java.util.Date;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.zeroclick.meeting.client.event.EventForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class EventFormData extends AbstractFormData {

	private static final long serialVersionUID = 1L;

	public CreatedDate getCreatedDate() {
		return getFieldByClass(CreatedDate.class);
	}

	public Duration getDuration() {
		return getFieldByClass(Duration.class);
	}

	public Email getEmail() {
		return getFieldByClass(Email.class);
	}

	public Emails getEmails() {
		return getFieldByClass(Emails.class);
	}

	public EndDate getEndDate() {
		return getFieldByClass(EndDate.class);
	}

	/**
	 * access method for property EventId.
	 */
	public Long getEventId() {
		return getEventIdProperty().getValue();
	}

	/**
	 * access method for property EventId.
	 */
	public void setEventId(Long eventId) {
		getEventIdProperty().setValue(eventId);
	}

	public EventIdProperty getEventIdProperty() {
		return getPropertyByClass(EventIdProperty.class);
	}

	/**
	 * access method for property ExternalIdOrganizer.
	 */
	public String getExternalIdOrganizer() {
		return getExternalIdOrganizerProperty().getValue();
	}

	/**
	 * access method for property ExternalIdOrganizer.
	 */
	public void setExternalIdOrganizer(String externalIdOrganizer) {
		getExternalIdOrganizerProperty().setValue(externalIdOrganizer);
	}

	public ExternalIdOrganizerProperty getExternalIdOrganizerProperty() {
		return getPropertyByClass(ExternalIdOrganizerProperty.class);
	}

	/**
	 * access method for property ExternalIdRecipient.
	 */
	public String getExternalIdRecipient() {
		return getExternalIdRecipientProperty().getValue();
	}

	/**
	 * access method for property ExternalIdRecipient.
	 */
	public void setExternalIdRecipient(String externalIdRecipient) {
		getExternalIdRecipientProperty().setValue(externalIdRecipient);
	}

	public ExternalIdRecipientProperty getExternalIdRecipientProperty() {
		return getPropertyByClass(ExternalIdRecipientProperty.class);
	}

	public GuestId getGuestId() {
		return getFieldByClass(GuestId.class);
	}

	/**
	 * access method for property LastModifier.
	 */
	public Long getLastModifier() {
		return getLastModifierProperty().getValue();
	}

	/**
	 * access method for property LastModifier.
	 */
	public void setLastModifier(Long lastModifier) {
		getLastModifierProperty().setValue(lastModifier);
	}

	public LastModifierProperty getLastModifierProperty() {
		return getPropertyByClass(LastModifierProperty.class);
	}

	public Organizer getOrganizer() {
		return getFieldByClass(Organizer.class);
	}

	public OrganizerEmail getOrganizerEmail() {
		return getFieldByClass(OrganizerEmail.class);
	}

	/**
	 * access method for property PreviousState.
	 */
	public String getPreviousState() {
		return getPreviousStateProperty().getValue();
	}

	/**
	 * access method for property PreviousState.
	 */
	public void setPreviousState(String previousState) {
		getPreviousStateProperty().setValue(previousState);
	}

	public PreviousStateProperty getPreviousStateProperty() {
		return getPropertyByClass(PreviousStateProperty.class);
	}

	public Reason getReason() {
		return getFieldByClass(Reason.class);
	}

	public Slot getSlot() {
		return getFieldByClass(Slot.class);
	}

	public StartDate getStartDate() {
		return getFieldByClass(StartDate.class);
	}

	public State getState() {
		return getFieldByClass(State.class);
	}

	public Subject getSubject() {
		return getFieldByClass(Subject.class);
	}

	public Venue getVenue() {
		return getFieldByClass(Venue.class);
	}

	public static class CreatedDate extends AbstractValueFieldData<Date> {

		private static final long serialVersionUID = 1L;
	}

	public static class Duration extends AbstractValueFieldData<Integer> {

		private static final long serialVersionUID = 1L;
	}

	public static class Email extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Emails extends AbstractTableFieldBeanData {

		private static final long serialVersionUID = 1L;

		@Override
		public EmailsRowData addRow() {
			return (EmailsRowData) super.addRow();
		}

		@Override
		public EmailsRowData addRow(int rowState) {
			return (EmailsRowData) super.addRow(rowState);
		}

		@Override
		public EmailsRowData createRow() {
			return new EmailsRowData();
		}

		@Override
		public Class<? extends AbstractTableRowData> getRowType() {
			return EmailsRowData.class;
		}

		@Override
		public EmailsRowData[] getRows() {
			return (EmailsRowData[]) super.getRows();
		}

		@Override
		public EmailsRowData rowAt(int index) {
			return (EmailsRowData) super.rowAt(index);
		}

		public void setRows(EmailsRowData[] rows) {
			super.setRows(rows);
		}

		public static class EmailsRowData extends AbstractTableRowData {

			private static final long serialVersionUID = 1L;
			public static final String email = "email";
			private String m_email;

			public String getEmail() {
				return m_email;
			}

			public void setEmail(String newEmail) {
				m_email = newEmail;
			}
		}
	}

	public static class EndDate extends AbstractValueFieldData<Date> {

		private static final long serialVersionUID = 1L;
	}

	public static class EventIdProperty extends AbstractPropertyData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class ExternalIdOrganizerProperty extends AbstractPropertyData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class ExternalIdRecipientProperty extends AbstractPropertyData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class GuestId extends AbstractValueFieldData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class LastModifierProperty extends AbstractPropertyData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class Organizer extends AbstractValueFieldData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class OrganizerEmail extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class PreviousStateProperty extends AbstractPropertyData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Reason extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Slot extends AbstractValueFieldData<Integer> {

		private static final long serialVersionUID = 1L;
	}

	public static class StartDate extends AbstractValueFieldData<Date> {

		private static final long serialVersionUID = 1L;
	}

	public static class State extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Subject extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Venue extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}
}
