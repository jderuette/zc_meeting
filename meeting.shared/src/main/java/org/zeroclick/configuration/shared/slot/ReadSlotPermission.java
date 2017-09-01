package org.zeroclick.configuration.shared.slot;

import java.security.BasicPermission;

public class ReadSlotPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadSlotPermission() {
		super(ReadSlotPermission.class.getSimpleName());
	}
}
