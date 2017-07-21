package org.zeroclick.meeting.client.meeting;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.meeting.client.event.EventAdminTablePage;
import org.zeroclick.meeting.client.event.EventProcessedTablePage;
import org.zeroclick.meeting.client.event.EventTablePage;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.ReadEventPermission;

/**
 * <h3>{@link MeetingOutline}</h3>
 *
 * @author djer
 */
@Order(1000)
public class MeetingOutline extends AbstractOutline {

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		final int currentUserEventLevel = ACCESS.getLevel(new ReadEventPermission((Long) null));
		final Boolean isEventUser = currentUserEventLevel >= ReadEventPermission.LEVEL_OWN;
		final Boolean isEventAdmin = currentUserEventLevel == ReadEventPermission.LEVEL_ALL;

		final EventTablePage eventTablePage = new EventTablePage();
		eventTablePage.setVisibleGranted(isEventUser);

		final EventProcessedTablePage eventProcessedTablePage = new EventProcessedTablePage();
		eventProcessedTablePage.setVisibleGranted(isEventUser);

		final EventAdminTablePage eventAdminTablePage = new EventAdminTablePage();
		eventAdminTablePage.setVisibleGranted(isEventAdmin);

		pageList.add(eventTablePage);
		pageList.add(eventProcessedTablePage);
		pageList.add(eventAdminTablePage);
	}

	@Override
	protected void initConfig() {
		super.initConfig();
		final Boolean isVisible = ACCESS
				.getLevel(new ReadEventPermission((Long) null)) >= ReadEventPermission.LEVEL_OWN;
		this.setEnabledGranted(isVisible);
		this.setVisibleGranted(isVisible);
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.meetings");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Calendar;
	}
}
