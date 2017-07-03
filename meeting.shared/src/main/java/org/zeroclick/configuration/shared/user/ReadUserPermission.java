package org.oneclick.configuration.shared.user;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class ReadUserPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private Long userId;

	public ReadUserPermission(final Integer level) {
		super(ReadUserPermission.class.getSimpleName() + ".*", level);
		this.userId = null;
	}

	public ReadUserPermission(final Long userId) {
		super(ReadUserPermission.class.getSimpleName() + ".*", LEVEL_UNDEFINED);
		this.userId = userId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadUserPermission) {
			final Long userId = ((ReadUserPermission) other).getUserId();
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
