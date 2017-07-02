package org.oneclick.meeting.shared.event;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class ReadEventPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	/**
	 * Only my OWNED object
	 */
	public static final int LEVEL_OWN = 10;
	/**
	 * Only object where I'm Involved
	 */
	public static final int LEVEL_INVOLVED = 5;

	private final Long eventId;

	/**
	 * Use for to save/load user (role) permissions.
	 *
	 * @param level
	 */
	public ReadEventPermission(final Integer level) {
		super(ReadEventPermission.class.getSimpleName() + ".*", level);
		this.eventId = null;
	}

	public ReadEventPermission(final Long eventId) {
		super(ReadEventPermission.class.getSimpleName() + "." + eventId, LEVEL_UNDEFINED);
		this.eventId = eventId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadEventPermission) {
			final Long eventId = ((ReadEventPermission) other).getEventId();
			final IEventService eventService = BEANS.get(IEventService.class);
			if (eventService.isOwn(eventId)) {
				result = LEVEL_OWN;
			}
			if (eventService.isRecipient(eventId)) {
				result = LEVEL_INVOLVED;
			}
		}

		return result;
	}

	public Long getEventId() {
		return this.eventId;
	}
}
