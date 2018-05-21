package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.event.EventProcessedTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.StateCodeType;
import org.zeroclick.meeting.shared.eventb.EventsTablePageData;

@Data(EventsTablePageData.class)
public class EventProcessedTablePage extends AbstractEventsTablePage<Table> {

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IEventService.class).getEventProcessedTableData(filter));
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.eventsProccessed");
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.Checked;
	}

	@Override
	protected Boolean canHandle(final EventCreatedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final EventModifiedNotification notification) {
		final EventFormData formData = notification.getFormData();
		return CompareUtility.isOneOf(formData.getState().getValue(), StateCodeType.AcceptedCode.ID,
				StateCodeType.RefusededCode.ID);
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void initConfig() {
			super.initConfig();
			// this.getReasonColumn().setVisible(true);
			this.getOrganizerEmailColumn().setVisible(true);
			// this.getEmailColumn().setVisible(true);
			// super.getStateColumn().setVisible(true);
			// super.getStateColumn().setOrder(10);
			// this.getRefusedByColumn().setVisible(true);
		}

		@Override
		protected String getConfiguredTitle() {
			return TEXTS.get("zc.meeting.eventsProccessed");
		}

	}
}
