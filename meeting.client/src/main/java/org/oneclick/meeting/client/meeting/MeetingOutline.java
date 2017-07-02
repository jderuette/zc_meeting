package org.oneclick.meeting.client.meeting;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.meeting.client.event.EventProcessedTablePage;
import org.oneclick.meeting.client.event.EventTablePage;
import org.oneclick.meeting.shared.Icons;
import org.oneclick.meeting.shared.event.ReadEventPermission;

/**
 * <h3>{@link MeetingOutline}</h3>
 *
 * @author djer
 */
@Order(1000)
public class MeetingOutline extends AbstractOutline {

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		// super.execCreateChildPages(pageList);
		final int currentUserEventLevel = ACCESS.getLevel(new ReadEventPermission((Long) null));
		final Boolean isEventUser = currentUserEventLevel >= ReadEventPermission.LEVEL_OWN;
		final Boolean isEventAdmin = currentUserEventLevel > ReadEventPermission.LEVEL_OWN;
		final EventTablePage eventTablePage = new EventTablePage();
		eventTablePage.setVisibleGranted(isEventUser);

		final EventProcessedTablePage eventProcessedTablePage = new EventProcessedTablePage();
		eventProcessedTablePage.setVisibleGranted(isEventUser);

		pageList.add(eventTablePage);
		pageList.add(eventProcessedTablePage);
		// pageList.add(new TestNodePage());
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
		return TEXTS.get("Meetings");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Calendar;
	}
}
