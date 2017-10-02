package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 *
 * @author djer
 *
 */
public class CreateAssignSubscriptionToUserPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private final Long userId;

	public CreateAssignSubscriptionToUserPermission(final Integer level) {
		super(CreateAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", level);
		this.userId = null;
	}

	public CreateAssignSubscriptionToUserPermission(final Long userId) {
		super(CreateAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", LEVEL_UNDEFINED);
		this.userId = userId;
	}

	public CreateAssignSubscriptionToUserPermission() {
		super(CreateAssignSubscriptionToUserPermission.class.getSimpleName());
		this.userId = null;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof CreateAssignSubscriptionToUserPermission) {
			final Long userId = ((CreateAssignSubscriptionToUserPermission) other).getUserId();
			final AccessControlService acs = BEANS.get(AccessControlService.class);
			final Long currentUserId = acs.getZeroClickUserIdOfCurrentSubject();
			if (currentUserId.equals(userId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getUserId() {
		return this.userId;
	}
}
