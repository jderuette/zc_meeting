package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.zeroclick.configuration.shared.user.IUserService;

public class UpdateAssignSubscriptionToUserPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private final Long userId;

	public UpdateAssignSubscriptionToUserPermission(final Integer level) {
		super(UpdateAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", level);
		this.userId = null;
	}

	public UpdateAssignSubscriptionToUserPermission(final Long userId) {
		super(UpdateAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", LEVEL_UNDEFINED);
		this.userId = userId;
	}

	public UpdateAssignSubscriptionToUserPermission() {
		super(UpdateAssignSubscriptionToUserPermission.class.getSimpleName());
		this.userId = null;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof UpdateAssignSubscriptionToUserPermission) {
			final Long userId = ((UpdateAssignSubscriptionToUserPermission) other).getUserId();
			final IUserService userService = BEANS.get(IUserService.class);
			if (userService.isOwn(userId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getUserId() {
		return this.userId;
	}
}
