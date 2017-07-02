package org.oneclick.meeting.shared.event;

import java.security.BasicPermission;

public class CreateEventPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateEventPermission() {
		super(CreateEventPermission.class.getSimpleName());
	}
}
