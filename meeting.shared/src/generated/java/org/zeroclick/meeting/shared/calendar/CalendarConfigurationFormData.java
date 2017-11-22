package org.zeroclick.meeting.shared.calendar;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications
 * recommended.
 */
@Generated(value = "org.zeroclick.meeting.client.calendar.CalendarConfigurationForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class CalendarConfigurationFormData extends AbstractFormData {

	private static final long serialVersionUID = 1L;

	public CalendarConfigurationId getCalendarConfigurationId() {
		return getFieldByClass(CalendarConfigurationId.class);
	}

	public ExternalId getExternalId() {
		return getFieldByClass(ExternalId.class);
	}

	public OAuthCredentialId getOAuthCredentialId() {
		return getFieldByClass(OAuthCredentialId.class);
	}

	public ProcessFreeEvent getProcessFreeEvent() {
		return getFieldByClass(ProcessFreeEvent.class);
	}

	public ProcessFullDayEvent getProcessFullDayEvent() {
		return getFieldByClass(ProcessFullDayEvent.class);
	}

	public ProcessNotRegistredOnEvent getProcessNotRegistredOnEvent() {
		return getFieldByClass(ProcessNotRegistredOnEvent.class);
	}

	public UserId getUserId() {
		return getFieldByClass(UserId.class);
	}

	public static class CalendarConfigurationId extends AbstractValueFieldData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class ExternalId extends AbstractValueFieldData<String> {

		private static final long serialVersionUID = 1L;
	}

	public static class OAuthCredentialId extends AbstractValueFieldData<Long> {

		private static final long serialVersionUID = 1L;
	}

	public static class ProcessFreeEvent extends AbstractValueFieldData<Boolean> {

		private static final long serialVersionUID = 1L;
	}

	public static class ProcessFullDayEvent extends AbstractValueFieldData<Boolean> {

		private static final long serialVersionUID = 1L;
	}

	public static class ProcessNotRegistredOnEvent extends AbstractValueFieldData<Boolean> {

		private static final long serialVersionUID = 1L;
	}

	public static class UserId extends AbstractValueFieldData<Long> {

		private static final long serialVersionUID = 1L;
	}
}