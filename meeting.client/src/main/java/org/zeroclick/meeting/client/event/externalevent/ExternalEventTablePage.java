package org.zeroclick.meeting.client.event.externalevent;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.event.externalevent.ExternalEventTablePage.Table;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventTablePageData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;
import org.zeroclick.ui.action.menu.AbstractEditMenu;
import org.zeroclick.ui.action.menu.AbstractNewMenu;

@Data(ExternalEventTablePageData.class)
public class ExternalEventTablePage extends AbstractPageWithTable<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.externalEvent");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IExternalEventService.class).getExternalEventTableData(filter));
	}

	public class Table extends AbstractTable {
		@Order(1000)
		public class NewExternalEventMenu extends AbstractNewMenu {

			@Override
			protected void execAction() {
				final ExternalEventForm form = new ExternalEventForm();
				form.addFormListener(new ExternalEventFormListener());
				form.startNew();
			}
		}

		@Order(2000)
		public class EditExternalEventMenu extends AbstractEditMenu {
			@Override
			protected void execAction() {
				final ExternalEventForm form = new ExternalEventForm();
				form.getExternalIdField().setValue(Table.this.getExternalIdColumn().getSelectedValue());

				form.addFormListener(new ExternalEventFormListener());
				form.startModify();
			}
		}

		private class ExternalEventFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
					ExternalEventTablePage.this.reloadPage();
				}
			}
		}

		public ExternalCalendarIdColumn getExternalCalendarIdColumn() {
			return this.getColumnSet().getColumnByClass(ExternalCalendarIdColumn.class);
		}

		public ExternalEventIdColumn getExternalEventIdColumn() {
			return this.getColumnSet().getColumnByClass(ExternalEventIdColumn.class);
		}

		public ApiCredentialIdColumn getApiCredentialIdColumn() {
			return this.getColumnSet().getColumnByClass(ApiCredentialIdColumn.class);
		}

		public ExternalIdColumn getExternalIdColumn() {
			return this.getColumnSet().getColumnByClass(ExternalIdColumn.class);
		}

		@Order(1000)
		public class ExternalIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class ExternalCalendarIdColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalCalendarId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 250;
			}
		}

		@Order(3000)
		public class ExternalEventIdColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.externalEvent.externalEventId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 250;
			}
		}

		@Order(4000)
		public class ApiCredentialIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.externalEvent.apiCredentialId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

	}
}
