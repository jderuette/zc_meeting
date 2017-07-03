package org.zeroclick.configuration.shared.user;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class UpdateUserPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private Long userId;

	public UpdateUserPermission(final Integer level) {
		super(UpdateUserPermission.class.getSimpleName() + ".*", level);
		this.userId = null;
	}

	public UpdateUserPermission(final Long userId) {
		super(UpdateUserPermission.class.getSimpleName() + ".*", LEVEL_UNDEFINED);
		this.userId = userId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof UpdateUserPermission) {
			final Long userId = ((UpdateUserPermission) other).getUserId();
			if (BEANS.get(IUserService.class).isOwn(userId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(final Long userId) {
		this.userId = userId;
	}
}
