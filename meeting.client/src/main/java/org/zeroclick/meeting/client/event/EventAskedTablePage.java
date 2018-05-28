package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.event.EventAskedTablePageData;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.EventStateCodeType;

@Data(EventAskedTablePageData.class)
public class EventAskedTablePage extends EventTablePage {

	@Override
	protected void execLoadData(final SearchFilter filter) {
		final AbstractTablePageData pageData = BEANS.get(IEventService.class).getEventAskedTableData(filter);
		this.importPageData(pageData);
		this.setNbEventToProcess(pageData.getRowCount());

		this.refreshTitle();
	}

	public void load() {
		this.execLoadData(null);
	}

	@Override
	protected String getConfiguredTitle() {
		return this.buildTitle();
	}

	@Override
	protected String buildTitle() {
		return TEXTS.get("zc.meeting.eventsAsked",
				null == this.getNbEventToProcess() ? "0" : this.getNbEventToProcess().toString());
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.LongArrowLeft;
	}

	@Override
	protected Boolean canHandle(final EventCreatedNotification notification) {
		final EventFormData formData = notification.getFormData();
		return super.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(EventStateCodeType.WaitingCode.ID, formData.getState().getValue());
	}

	@Override
	protected Boolean canHandle(final EventModifiedNotification notification) {
		final EventFormData formData = notification.getFormData();
		return super.getEventMessageHelper().isHeldByCurrentUser(formData)
				&& CompareUtility.equals(EventStateCodeType.WaitingCode.ID, formData.getState().getValue());
	}

	@Override
	protected Boolean canHandle(final CalendarConfigurationCreatedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final CalendarConfigurationModifiedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final CalendarsConfigurationModifiedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final CalendarsConfigurationCreatedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final ApiCreatedNotification notification) {
		return Boolean.TRUE;
	}

	@Override
	protected Boolean canHandle(final ApiDeletedNotification notification) {
		return Boolean.TRUE;
	}

	@Override
	protected void onModifiedEvent(final EventFormData formData, final String previousStateRow,
			final ITableRow modifiedRow) {
		final String newState = formData.getState().getValue();

		if (CompareUtility.equals(EventStateCodeType.WaitingCode.ID, newState)) {
			this.getTable().refreshAutoFillDate(modifiedRow);
		}
	}

	public class Table extends EventTablePage.Table {

		// add/delete row handled by parent (for inc and dec nbEvent title)

		@Override
		protected void initConfig() {

			super.initConfig();
			this.getOrganizerEmailColumn().setVisible(Boolean.FALSE);
			// this.getEmailColumn().setVisible(Boolean.TRUE);
			this.getStartDateColumn().setVisible(Boolean.FALSE);
			this.getEndDateColumn().setVisible(Boolean.FALSE);
		}
	}
}
