package org.oneclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class ReadApiPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	public static final int LEVEL_RELATED = 15;

	private Long apiCredentialId;

	public ReadApiPermission(final Integer level) {
		super(ReadApiPermission.class.getSimpleName() + ".*", level);
	}

	public ReadApiPermission(final Long apiCredentialId) {
		super(ReadApiPermission.class.getSimpleName() + "." + apiCredentialId, LEVEL_UNDEFINED);
		this.apiCredentialId = apiCredentialId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof ReadApiPermission) {
			final Long apiCredentialId = ((ReadApiPermission) other).getApiCredentialId();
			if (BEANS.get(IApiService.class).isOwn(apiCredentialId)) {
				result = LEVEL_OWN;
			} else if (BEANS.get(IApiService.class).isRelated(apiCredentialId)) {
				result = LEVEL_RELATED;
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
