package org.zeroclick.meeting.shared.calendar;

import java.security.BasicPermission;

public class CreateApiPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateApiPermission() {
		super(CreateApiPermission.class.getSimpleName());
	}
}
