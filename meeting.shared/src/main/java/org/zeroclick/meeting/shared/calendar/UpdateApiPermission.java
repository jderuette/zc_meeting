package org.zeroclick.meeting.shared.calendar;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

public class UpdateApiPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	public static final int LEVEL_OWN = 10;

	/**
	 * Allow refreshToken IF user performing refresh has planned meeting with
	 * "refreshed" user
	 */
	public static final int LEVEL_RELATED = 15;

	private Long apiCredentialId;

	public UpdateApiPermission(final Integer level) {
		super(UpdateApiPermission.class.getSimpleName() + ".*", level);
	}

	public UpdateApiPermission(final Long apiCredentialId) {
		super(UpdateApiPermission.class.getSimpleName() + "." + apiCredentialId, LEVEL_UNDEFINED);
		this.apiCredentialId = apiCredentialId;
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int result = LEVEL_ALL;

		if (other instanceof UpdateApiPermission) {
			final Long apiCredentialId = ((UpdateApiPermission) other).getApiCredentialId();
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
