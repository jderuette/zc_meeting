package org.zeroclick.configuration.shared.slot;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.zeroclick.meeting.shared.event.ReadEventPermission;

public class ReadSlotPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	/**
	 * Only my OWNED object
	 */
	public static final int LEVEL_OWN = 10;
	/**
	 * Only object where I'm Involved
	 */
	public static final int LEVEL_INVOLVED = 5;

	private final Long slotId;

	/**
	 * Use for to save/load user (Slot) permissions.
	 *
	 * @param level
	 */
	public ReadSlotPermission(final Integer level) {
		super(ReadEventPermission.class.getSimpleName() + ".*", level);
		this.slotId = null;
	}

	public ReadSlotPermission(final Long slotId) {
		super(ReadEventPermission.class.getSimpleName() + "." + slotId, LEVEL_UNDEFINED);
		this.slotId = slotId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadSlotPermission) {
			final Long slotId = ((ReadSlotPermission) other).getSlotId();
			final ISlotService slotService = BEANS.get(ISlotService.class);
			if (slotService.isOwn(slotId)) {
				result = LEVEL_OWN;
			}
			if (slotService.isInvolved(slotId)) {
				result = LEVEL_INVOLVED;
			}
		}

		return result;
	}

	public Long getSlotId() {
		return this.slotId;
	}
}
