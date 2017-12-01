package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.event.EventProcessedTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.EventFormData;
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
	protected Boolean canHandleNew(final AbstractEventNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandleModified(final AbstractEventNotification notification) {
		final EventFormData formData = notification.getEventForm();
		return CompareUtility.isOneOf(formData.getState().getValue(), StateCodeType.AcceptedCode.ID,
				StateCodeType.RefusededCode.ID);
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void initConfig() {
			super.initConfig();
			this.getReasonColumn().setVisible(Boolean.TRUE);
			this.getOrganizerEmailColumn().setVisible(Boolean.TRUE);
			this.getEmailColumn().setVisible(Boolean.TRUE);
			super.getStateColumn().setVisible(Boolean.TRUE);
			super.getStateColumn().setOrder(10);
		}

		@Override
		protected String getConfiguredTitle() {
			return TEXTS.get("zc.meeting.eventsProccessed");
		}

	}
}
