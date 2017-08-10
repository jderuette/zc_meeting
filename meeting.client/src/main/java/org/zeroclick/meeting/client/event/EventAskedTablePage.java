package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.EventAskedTablePageData;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;

@Data(EventAskedTablePageData.class)
public class EventAskedTablePage extends EventTablePage {

	private static final Logger LOG = LoggerFactory.getLogger(EventAskedTablePage.class);

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

		// @Override
		// protected void addDefaultFilters() {
		// this.addAskedStateFilter();
		// this.addIamOrganizerFilter();
		// }
		//
		// protected void addIamOrganizerFilter() {
		// if (null == this.getUserFilterManager()) {
		// this.setUserFilterManager(this.createUserFilterManager());
		// }
		// if
		// (this.getUserFilterManager().getFilter(this.getOrganizerEmailColumn().getColumnId())
		// == null) {
		// final TextColumnUserFilterState iamOrganizerFilter = new
		// TextColumnUserFilterState(
		// this.getOrganizerEmailColumn());
		//
		// final Set<Object> selectedValues = new HashSet<>();
		// selectedValues.addAll(this.getCurrentUserEmails());
		// selectedValues.add(TEXTS.get("zc.common.me"));
		// iamOrganizerFilter.setSelectedValues(selectedValues);
		//
		// this.getUserFilterManager().addFilter(iamOrganizerFilter);
		// }
		// }

	}
}
