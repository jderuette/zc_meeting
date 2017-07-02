package org.oneclick.configuration.shared.role;

import java.security.BasicPermission;

public class UpdatePermissionPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public UpdatePermissionPermission() {
		super(UpdatePermissionPermission.class.getSimpleName());
	}
}
