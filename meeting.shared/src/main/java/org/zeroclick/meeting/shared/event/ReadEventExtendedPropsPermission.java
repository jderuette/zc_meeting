package org.zeroclick.meeting.shared.event;

import java.security.BasicPermission;

public class ReadEventExtendedPropsPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadEventExtendedPropsPermission() {
		super(ReadEventExtendedPropsPermission.class.getSimpleName());
	}
}
