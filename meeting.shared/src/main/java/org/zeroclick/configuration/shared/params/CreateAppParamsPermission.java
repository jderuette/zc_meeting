package org.zeroclick.configuration.shared.params;

import java.security.BasicPermission;

public class CreateAppParamsPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateAppParamsPermission() {
		super(CreateAppParamsPermission.class.getSimpleName());
	}
}
