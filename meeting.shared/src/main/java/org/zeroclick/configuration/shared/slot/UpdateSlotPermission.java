package org.zeroclick.configuration.shared.slot;

import java.security.BasicPermission;

public class UpdateSlotPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public UpdateSlotPermission() {
		super(UpdateSlotPermission.class.getSimpleName());
	}
}
