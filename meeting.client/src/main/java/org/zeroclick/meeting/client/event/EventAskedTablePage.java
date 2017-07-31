package org.zeroclick.meeting.client.event;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	protected void onNewEvent(final EventFormData formData) {
		if (this.isHeldByCurrentUser(formData)) {
			LOG.debug("New event detected Organized by current User incrementing nb Event to Process");
			this.incNbEventToProcess();
		}
	}

	@Override
	protected void onModifiedEvent(final EventFormData formData, final String previousState) {
		if (super.isHeldByCurrentUser(formData)) {
			LOG.debug("Modified event detected Organized by current User changing nb Event to Process");
			if ("ASKED".equals(formData.getState())) {
				this.incNbEventToProcess();
			} else if (null != previousState && !"ACCEPTED".equals(previousState)) {
				this.decNbEventToProcess();
			}
		}
	}

	public class Table extends EventTablePage.Table {

		@Override
		protected void addDefaultFilters() {
			this.addAskedStateFilter();
			this.addIamOrganizerFilter();
		}

		protected void addIamOrganizerFilter() {
			if (null == this.getUserFilterManager()) {
				this.setUserFilterManager(this.createUserFilterManager());
			}
			if (this.getUserFilterManager().getFilter(this.getOrganizerEmailColumn().getColumnId()) == null) {
				final TextColumnUserFilterState iamOrganizerFilter = new TextColumnUserFilterState(
						this.getOrganizerEmailColumn());

				final Set<Object> selectedValues = new HashSet<>();
				selectedValues.addAll(this.getCurrentUserEmails());
				selectedValues.add(TEXTS.get("zc.common.me"));
				iamOrganizerFilter.setSelectedValues(selectedValues);

				this.getUserFilterManager().addFilter(iamOrganizerFilter);
			}
		}

	}
}
