package org.zeroclick.configuration.shared.params;

import java.security.BasicPermission;

public class UpdateAppParamsPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public UpdateAppParamsPermission() {
		super(UpdateAppParamsPermission.class.getSimpleName());
	}
}
