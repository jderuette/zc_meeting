package org.zeroclick.configuration.shared.role;

import java.security.BasicPermission;

public class CreateAssignToRolePermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateAssignToRolePermission() {
		super(CreateAssignToRolePermission.class.getSimpleName());
	}
}
