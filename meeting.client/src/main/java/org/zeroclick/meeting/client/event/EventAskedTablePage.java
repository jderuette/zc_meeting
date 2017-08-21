package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.EventAskedTablePageData;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;

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
	protected Boolean canHandleNew(final AbstractEventNotification notification) {
		final EventFormData formData = notification.getEventForm();
		return super.isHeldByCurrentUser(formData) && "ASKED".equals(formData.getState().getValue());
	}

	@Override
	protected Boolean canHandleModified(final AbstractEventNotification notification) {
		final EventFormData formData = notification.getEventForm();
		return super.isHeldByCurrentUser(formData) && "ASKED".equals(formData.getState().getValue());
	}

	public class Table extends EventTablePage.Table {

		// add/delete row handle by parent (for inc and dec nbEvent title)

		@Override
		protected void initConfig() {
			super.initConfig();
			this.getOrganizerEmailColumn().setVisible(Boolean.FALSE);
			this.getEmailColumn().setVisible(Boolean.TRUE);
			this.getStartDateColumn().setVisible(Boolean.FALSE);
			this.getEndDateColumn().setVisible(Boolean.FALSE);
		}
	}
}
