package org.zeroclick.common.shared.document;

import java.security.BasicPermission;

public class UpdateDocumentPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public UpdateDocumentPermission() {
		super(UpdateDocumentPermission.class.getSimpleName());
	}
}
