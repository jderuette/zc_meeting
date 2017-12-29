package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class ReadCalendarConfigurationPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private Long calendarConfigurationId;

	public ReadCalendarConfigurationPermission(final Integer level) {
		super(ReadCalendarConfigurationPermission.class.getSimpleName() + ".*", level);
	}

	public ReadCalendarConfigurationPermission(final Long calendarConfigurationId) {
		super(ReadCalendarConfigurationPermission.class.getSimpleName() + "." + calendarConfigurationId,
				LEVEL_UNDEFINED);
		this.calendarConfigurationId = calendarConfigurationId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadCalendarConfigurationPermission) {
			final Long calendarConfigurationId = ((ReadCalendarConfigurationPermission) other)
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
