package org.zeroclick.common.shared.document;

import java.security.BasicPermission;

public class ReadDocumentPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public ReadDocumentPermission() {
		super(ReadDocumentPermission.class.getSimpleName());
	}
}
