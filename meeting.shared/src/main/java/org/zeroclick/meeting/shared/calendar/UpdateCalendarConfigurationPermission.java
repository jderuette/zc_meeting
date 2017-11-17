package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class UpdateCalendarConfigurationPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private Long calendarConfigurationId;

	public UpdateCalendarConfigurationPermission(final Integer level) {
		super(UpdateCalendarConfigurationPermission.class.getSimpleName() + ".*", level);
	}

	public UpdateCalendarConfigurationPermission(final Long calendarConfigurationId) {
		super(UpdateCalendarConfigurationPermission.class.getSimpleName() + "." + calendarConfigurationId,
				LEVEL_UNDEFINED);
		this.calendarConfigurationId = calendarConfigurationId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof UpdateCalendarConfigurationPermission) {
			final Long calendarConfigurationId = ((UpdateCalendarConfigurationPermission) other)
					.getCalendarConfigurationId();
			if (BEANS.get(ICalendarConfigurationService.class).isOwn(calendarConfigurationId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getCalendarConfigurationId() {
		return this.calendarConfigurationId;
	}

	public void setCalendarConfigurationId(final Long calendarConfigurationId) {
		this.calendarConfigurationId = calendarConfigurationId;
	}
}
