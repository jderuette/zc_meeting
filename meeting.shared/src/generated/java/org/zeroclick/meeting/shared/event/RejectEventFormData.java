package org.zeroclick.meeting.shared.event;

import java.time.ZonedDateTime;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.zeroclick.meeting.client.event.RejectEventForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class RejectEventFormData extends AbstractFormData {

	private static final long serialVersionUID = 1L;

	public Email getEmail() {
		return getFieldByClass(Email.class);
	}

	/**
	 * access method for property End.
	 */
	public ZonedDateTime getEnd() {
		return getEndProperty().getValue();
	}

	/**
	 * access method for property End.
	 */
	public void setEnd(ZonedDateTime end) {
		getEndProperty().setValue(end);
	}

	public EndProperty getEndProperty() {
		return getPropertyByClass(EndProperty.class);
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

	/**
	 * access method for property GuestId.
	 */
	public Long getGuestId() {
		return getGuestIdProperty().getValue();
	}

	/**
	 * access method for property GuestId.
	 */
	public void setGuestId(Long guestId) {
		getGuestIdProperty().setValue(guestId);
	}

	public GuestIdProperty getGuestIdProperty() {
		return getPropertyByClass(GuestIdProperty.class);
	}

	/**
	 * access method for property Organizer.
	 */
	public Long getOrganizer() {
		return getOrganizerProperty().getValue();
	}

	/**
	 * access method for property Organizer.
	 */
	public void setOrganizer(Long organizer) {
		getOrganizerProperty().setValue(organizer);
	}

	public OrganizerEmail getOrganizerEmail() {
		return getFieldByClass(OrganizerEmail.class);
	}

	public OrganizerProperty getOrganizerProperty() {
		return getPropertyByClass(OrganizerProperty.class);
	}

	public Reason getReason() {
		return getFieldByClass(Reason.class);
	}

	/**
	 * access method for property Start.
	 */
	public ZonedDateTime getStart() {
		return getStartProperty().getValue();
	}

	/**
	 * access method for property Start.
	 */
	public void setStart(ZonedDateTime start) {
		getStartProperty().setValue(start);
	}

	public StartProperty getStartProperty() {
		return getPropertyByClass(StartProperty.class);
	}

	/**
	 * access method for property State.
	 */
	public String getState() {
		return getStateProperty().getValue();
	}

	/**
	 * access method for property State.
	 */
	public void setState(String state) {
		getStateProperty().setValue(state);
	}

	public StateProperty getStateProperty() {
		return getPropertyByClass(StateProperty.class);
	}

	public Subject getSubject() {
		return getFieldByClass(Subject.class);
	}

	public Venue getVenue() {
		return getFieldByClass(Venue.class);
	}

	public static class Email extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class EndProperty extends AbstractPropertyData<ZonedDateTime> {

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

	public static class GuestIdProperty extends AbstractPropertyData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class OrganizerEmail extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class OrganizerProperty extends AbstractPropertyData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class Reason extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class StartProperty extends AbstractPropertyData<ZonedDateTime> {

		private static final long serialVersionUID = 1L;
	}

	public static class StateProperty extends AbstractPropertyData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Subject extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class Venue extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}
}
