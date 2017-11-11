package org.zeroclick.configuration.shared.params;

import java.security.BasicPermission;

public class ReadAppParamsPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadAppParamsPermission() {
		super(ReadAppParamsPermission.class.getSimpleName());
	}
}
