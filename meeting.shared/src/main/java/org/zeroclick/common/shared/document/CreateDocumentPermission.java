package org.zeroclick.common.shared.document;

import java.security.BasicPermission;

public class CreateDocumentPermission extends BasicPermission {

	private static final long serialVersionUID = 1L;

	public CreateDocumentPermission() {
		super(CreateDocumentPermission.class.getSimpleName());
	}
}
