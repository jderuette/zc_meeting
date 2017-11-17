package org.zeroclick.meeting.client.meeting;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.zeroclick.common.desktop.pages.FormPage;
import org.zeroclick.configuration.client.slot.SlotAdminTablePage;
import org.zeroclick.configuration.client.slot.SlotForm;
import org.zeroclick.configuration.client.slot.SlotTablePage;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.meeting.client.calendar.CalendarConfigurationTablePage;
import org.zeroclick.meeting.client.event.EventAdminTablePage;
import org.zeroclick.meeting.client.event.EventAskedTablePage;
import org.zeroclick.meeting.client.event.EventProcessedTablePage;
import org.zeroclick.meeting.client.event.EventTablePage;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
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
		final int currentUserSlotLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		final Boolean isEventUser = currentUserEventLevel >= ReadEventPermission.LEVEL_OWN;
		final Boolean isEventAdmin = currentUserEventLevel == ReadEventPermission.LEVEL_ALL;
		final Boolean isSlotUser = currentUserSlotLevel >= ReadSlotPermission.LEVEL_OWN;
		final Boolean isSlotAdmin = currentUserSlotLevel == ReadSlotPermission.LEVEL_ALL;
		final Boolean iscalednarConfigAdmin = currentUserSlotLevel == ReadCalendarConfigurationPermission.LEVEL_ALL;

		final EventTablePage eventTablePage = new EventTablePage();
		eventTablePage.setVisibleGranted(isEventUser);

		final EventProcessedTablePage eventProcessedTablePage = new EventProcessedTablePage();
		eventProcessedTablePage.setVisibleGranted(isEventUser);

		final EventAskedTablePage eventAskedTablePage = new EventAskedTablePage();
		eventAskedTablePage.load();
		eventAskedTablePage.setVisibleGranted(isEventUser);

		final EventAdminTablePage eventAdminTablePage = new EventAdminTablePage();
		eventAdminTablePage.setVisibleGranted(isEventAdmin);

		final SlotTablePage slotTablePage = new SlotTablePage();
		slotTablePage.setVisibleGranted(isSlotUser);

		final SlotAdminTablePage slotAdminTablePage = new SlotAdminTablePage();
		slotAdminTablePage.setVisibleGranted(isSlotAdmin);

		// TODO Djer13 try to directly use the "configuredTitle"'s form
		final FormPage slotForm = new FormPage(SlotForm.class, Boolean.TRUE,
				TEXTS.get("zc.meeting.slot.config") + "(tree)");
		slotForm.setVisibleGranted(isSlotAdmin);

		final CalendarConfigurationTablePage calendarConfigurationTablePage = new CalendarConfigurationTablePage();
		calendarConfigurationTablePage.setVisibleGranted(Boolean.TRUE);
		calendarConfigurationTablePage.setVisibleGranted(iscalednarConfigAdmin);

		pageList.add(eventTablePage);
		pageList.add(eventAskedTablePage);
		pageList.add(eventProcessedTablePage);
		pageList.add(eventAdminTablePage);
		pageList.add(slotTablePage);
		pageList.add(slotAdminTablePage);
		pageList.add(calendarConfigurationTablePage);
		pageList.add(slotForm);
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
