package org.oneclick.configuration.shared.role;

import java.security.BasicPermission;

public class ReadPermissionPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadPermissionPermission() {
		super(ReadPermissionPermission.class.getSimpleName());
	}
}
