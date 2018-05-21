package org.zeroclick.meeting.client.meeting;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.common.desktop.pages.FormPage;
import org.zeroclick.comon.text.UserHelper;
import org.zeroclick.meeting.client.admin.EventAdminNodePage;
import org.zeroclick.meeting.client.event.EventAskedTablePage;
import org.zeroclick.meeting.client.event.EventInvitedPageForm;
import org.zeroclick.meeting.client.event.EventProcessedTablePage;
import org.zeroclick.meeting.client.event.EventTablePage;
import org.zeroclick.meeting.shared.Icons;

/**
 * <h3>{@link MeetingOutline}</h3>
 *
 * @author djer
 */
@Order(1000)
public class MeetingOutline extends AbstractOutline {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(MeetingOutline.class);

	private UserHelper getUserHelper() {
		return BEANS.get(UserHelper.class);
	}

	@Override
	protected void execCreateChildPages(final List<IPage<?>> pageList) {
		final Boolean isEventUser = this.getUserHelper().isEventUser();
		final Boolean isEventAdmin = this.getUserHelper().isEventAdmin();
		final Boolean isSlotAdmin = this.getUserHelper().isSlotUser();
		final Boolean iscalendarConfigAdmin = this.getUserHelper().isCalendarAdmin();

		final EventTablePage eventTablePage = new EventTablePage();
		eventTablePage.setVisibleGranted(isEventUser);

		final EventProcessedTablePage eventProcessedTablePage = new EventProcessedTablePage();
		eventProcessedTablePage.setVisibleGranted(isEventUser);

		final EventAskedTablePage eventAskedTablePage = new EventAskedTablePage();
		eventAskedTablePage.load();
		eventAskedTablePage.setVisibleGranted(isEventUser);

		final EventAdminNodePage eventAdminNodePage = new EventAdminNodePage();
		eventAdminNodePage.setVisibleGranted(isEventAdmin || isSlotAdmin || iscalendarConfigAdmin);

		final FormPage eventInvited = new FormPage(EventInvitedPageForm.class);
		eventInvited.setVisibleGranted(isEventUser);

		pageList.add(new FormPage(EventInvitedPageForm.class));

		pageList.add(eventTablePage);
		pageList.add(eventAskedTablePage);
		pageList.add(eventProcessedTablePage);
		pageList.add(eventAdminNodePage);
	}

	@Override
	protected void initConfig() {
		super.initConfig();
		final Boolean isVisible = this.getUserHelper().isEventUser();
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
