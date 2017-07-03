package org.oneclick.configuration.shared.role;

import java.security.BasicPermission;

public class UpdateAssignToRolePermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public UpdateAssignToRolePermission() {
		super(UpdateAssignToRolePermission.class.getSimpleName());
	}
}
