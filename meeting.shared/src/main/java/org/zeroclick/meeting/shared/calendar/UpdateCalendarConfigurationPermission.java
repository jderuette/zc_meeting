package org.zeroclick.meeting.shared.calendar;

public class UpdateCalendarConfigurationPermission extends AbstractHierarchyCalendarConfigurationPermission {

	private static final long serialVersionUID = 1L;

	private static final String PERMISSION_NAME = UpdateCalendarConfigurationPermission.class.getSimpleName();

	public UpdateCalendarConfigurationPermission(final Integer level) {
		super(level);
	}

	public UpdateCalendarConfigurationPermission(final Long calendarConfigurationId) {
		super(calendarConfigurationId);
	}

	protected static String getPermissionName() {
		return PERMISSION_NAME;
	}
}
