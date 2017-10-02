package org.zeroclick.configuration.shared.role;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.zeroclick.configuration.shared.user.IUserService;

public class ReadAssignSubscriptionToUserPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private final Long userId;

	public ReadAssignSubscriptionToUserPermission(final Integer level) {
		super(ReadAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", level);
		this.userId = null;
	}

	public ReadAssignSubscriptionToUserPermission(final Long userId) {
		super(ReadAssignSubscriptionToUserPermission.class.getSimpleName() + ".*", LEVEL_UNDEFINED);
		this.userId = userId;
	}

	public ReadAssignSubscriptionToUserPermission() {
		super(ReadAssignSubscriptionToUserPermission.class.getSimpleName());
		this.userId = null;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadAssignSubscriptionToUserPermission) {
			final Long userId = ((ReadAssignSubscriptionToUserPermission) other).getUserId();
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
