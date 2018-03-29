package org.zeroclick.meeting.shared.calendar;

public class CreateCalendarConfigurationPermission extends AbstractHierarchyCalendarConfigurationPermission {

	private static final long serialVersionUID = 1L;

	private static final String PERMISSION_NAME = CreateCalendarConfigurationPermission.class.getSimpleName();

	public CreateCalendarConfigurationPermission(final Integer level) {
		super(level);
	}

	public CreateCalendarConfigurationPermission(final Long oAuthCredentialId) {
		super(oAuthCredentialId);
	}

	protected static String getPermissionName() {
		return PERMISSION_NAME;
	}
}
