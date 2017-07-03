package org.zeroclick.configuration.shared.role;

import java.security.BasicPermission;

public class ReadAssignToRolePermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadAssignToRolePermission() {
		super(ReadAssignToRolePermission.class.getSimpleName());
	}
}
