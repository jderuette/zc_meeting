package org.zeroclick.meeting.shared.calendar;

public class ReadCalendarConfigurationPermission extends AbstractHierarchyCalendarConfigurationPermission {

	private static final long serialVersionUID = 1L;

	private static final String PERMISSION_NAME = ReadCalendarConfigurationPermission.class.getSimpleName();

	public ReadCalendarConfigurationPermission(final Integer level) {
		super(level);
	}

	public ReadCalendarConfigurationPermission(final Long calendarConfigurationId) {
		super(calendarConfigurationId);
	}

	protected static String getPermissionName() {
		return PERMISSION_NAME;
	}
}
