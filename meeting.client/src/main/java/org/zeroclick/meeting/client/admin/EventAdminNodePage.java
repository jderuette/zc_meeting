package org.zeroclick.meeting.client.admin;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.configuration.client.slot.SlotAdminTablePage;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationAdminTablePage;
import org.zeroclick.meeting.client.event.EventAdminTablePage;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventTablePage;
import org.zeroclick.meeting.client.event.involevment.InvolvementTablePage;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.event.ReadEventPermission;

public class EventAdminNodePage extends AbstractPageWithNodes {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.admin");
	}

	@Override
	protected boolean getConfiguredLazyExpandingEnabled() {
		return true;
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Gear;
	}

	@Override
	protected void initConfig() {
		super.initConfig();
		final Boolean isVisible = ACCESS
				.getLevel(new ReadEventPermission((Long) null)) >= ReadEventPermission.LEVEL_ALL;
		this.setEnabledGranted(isVisible);
		this.setVisibleGranted(isVisible);
	}

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		final int currentUserEventLevel = ACCESS.getLevel(new ReadEventPermission((Long) null));
		final Boolean isEventAdmin = currentUserEventLevel == ReadEventPermission.LEVEL_ALL;

		final int currentUserSlotLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		final Boolean isSlotAdmin = currentUserSlotLevel == ReadSlotPermission.LEVEL_ALL;

		final int currentUserCalendarConfigLevel = ACCESS
				.getLevel(new ReadCalendarConfigurationPermission((Long) null));
		final Boolean iscalendarConfigAdmin = currentUserCalendarConfigLevel == ReadCalendarConfigurationPermission.LEVEL_ALL;

		final EventAdminTablePage eventAdminTablePage = new EventAdminTablePage();
		eventAdminTablePage.setVisibleGranted(isEventAdmin);
		eventAdminTablePage.setLeaf(true);
		pageList.add(eventAdminTablePage);

		final SlotAdminTablePage slotAdminTablePage = new SlotAdminTablePage();
		slotAdminTablePage.setVisibleGranted(isSlotAdmin);
		slotAdminTablePage.setLeaf(true);
		pageList.add(slotAdminTablePage);

		final CalendarConfigurationAdminTablePage calendarConfigurationTablePage = new CalendarConfigurationAdminTablePage();
		calendarConfigurationTablePage.setVisibleGranted(iscalendarConfigAdmin);
		calendarConfigurationTablePage.setLeaf(true);
		pageList.add(calendarConfigurationTablePage);

		final ExternalEventTablePage externalEventTablePage = new ExternalEventTablePage();
		externalEventTablePage.setVisibleGranted(isEventAdmin);
		externalEventTablePage.setLeaf(true);
		pageList.add(externalEventTablePage);

		final InvolvementTablePage involevmentTablePage = new InvolvementTablePage();
		involevmentTablePage.setVisibleGranted(isEventAdmin);
		involevmentTablePage.setLeaf(true);
		pageList.add(involevmentTablePage);

	}
}
