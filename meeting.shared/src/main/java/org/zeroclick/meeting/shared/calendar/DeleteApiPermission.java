package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class DeleteApiPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	private Long apiCredentialId;

	public DeleteApiPermission(final Integer level) {
		super(DeleteApiPermission.class.getSimpleName() + ".*");
	}

	public DeleteApiPermission(final Long apiCredentialId) {
		super(DeleteApiPermission.class.getSimpleName() + "." + apiCredentialId, LEVEL_UNDEFINED);
		this.apiCredentialId = apiCredentialId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof DeleteApiPermission) {
			final Long apiCredentialId = ((DeleteApiPermission) other).getApiCredentialId();
			if (BEANS.get(IApiService.class).isOwn(apiCredentialId)) {
				result = LEVEL_OWN;
			}
		}

		return result;
	}

	public Long getApiCredentialId() {
		return this.apiCredentialId;
	}

	public void setApiCredentialId(final Long apiCredentialId) {
		this.apiCredentialId = apiCredentialId;
	}
}
