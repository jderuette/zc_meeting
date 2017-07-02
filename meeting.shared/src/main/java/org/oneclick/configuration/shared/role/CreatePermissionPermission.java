package org.oneclick.configuration.shared.role;

import java.security.BasicPermission;

public class CreatePermissionPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreatePermissionPermission() {
		super(CreatePermissionPermission.class.getSimpleName());
	}
}
