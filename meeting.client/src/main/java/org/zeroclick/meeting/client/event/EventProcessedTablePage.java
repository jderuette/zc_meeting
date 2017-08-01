package org.zeroclick.meeting.client.event;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.client.Desktop;
import org.zeroclick.meeting.client.event.EventProcessedTablePage.Table;
import org.zeroclick.meeting.shared.event.EventFormData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.eventb.EventsTablePageData;

@Data(EventsTablePageData.class)
public class EventProcessedTablePage extends AbstractEventsTablePage<Table> {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(EventProcessedTablePage.class);

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IEventService.class).getEventProcessedTableData(filter));
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.eventsProccessed");
	}

	@Override
	protected void onNewEvent(final EventFormData formData) {
		// Do nothing, processed event do not handle new event (an event cannot
		// be created already processed)
	}

	@Override
	protected void onModifiedEvent(final EventFormData formData, final String previousState) {
		final Desktop desktop = (Desktop) ClientSession.get().getDesktop();
		desktop.addNotification(IStatus.OK, 0l, Boolean.TRUE, this.getDesktopNotificationModifiedEventKey(formData),
				this.buildValuesForLocaleMessages(formData));
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void initConfig() {
			super.initConfig();
			this.addDefaultFilters();
			this.getReasonColumn().setVisible(Boolean.TRUE);
		}

		protected void addDefaultFilters() {
			if (null == this.getUserFilterManager()) {
				this.setUserFilterManager(this.createUserFilterManager());
			}
			if (this.getUserFilterManager().getFilter(this.getStateColumn().getColumnId()) == null) {
				final TextColumnUserFilterState askedFilter = new TextColumnUserFilterState(this.getStateColumn());
				final Set<Object> selectedValues = new HashSet<>();
				selectedValues.add(TEXTS.get("zc.meeting.state.accepted"));
				selectedValues.add(TEXTS.get("zc.meeting.state.refused"));
				askedFilter.setSelectedValues(selectedValues);
				// askedFilter.setFreeText(TEXTS.get("EventsProccessed"));
				this.getUserFilterManager().addFilter(askedFilter);
			}
		}

		@Override
		protected String getConfiguredTitle() {
			return TEXTS.get("zc.meeting.eventsProccessed");
		}

	}
}
