package org.zeroclick.meeting.shared.event;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.zeroclick.configuration.shared.subscription.SubscriptionHelper;

public class CreateEventPermission extends BasicHierarchyPermission {

	private static final long serialVersionUID = 1L;

	private static final int LEVEL_SUB_PERSO = SubscriptionHelper.LEVEL_SUB_PERSO;
	private static final int LEVEL_SUB_PRO = SubscriptionHelper.LEVEL_SUB_PRO;
	private static final int LEVEL_SUB_BUSINESS = SubscriptionHelper.LEVEL_SUB_BUSINESS;

	public CreateEventPermission() {
		super(CreateEventPermission.class.getSimpleName());
	}

	@Override
	protected int execCalculateLevel(final BasicHierarchyPermission other) {
		int minimalRequiredLevel = LEVEL_ALL;

		if (other instanceof CreateEventPermission) {
			final SubscriptionHelper subscriptionHelper = BEANS.get(SubscriptionHelper.class);
			minimalRequiredLevel = subscriptionHelper.getLevelForCurrentUser();
		}

		return minimalRequiredLevel;
	}
}
