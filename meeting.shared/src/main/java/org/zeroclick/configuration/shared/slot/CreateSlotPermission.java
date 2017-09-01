package org.zeroclick.configuration.shared.slot;

import java.security.BasicPermission;

public class CreateSlotPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateSlotPermission() {
		super(CreateSlotPermission.class.getSimpleName());
	}
}
