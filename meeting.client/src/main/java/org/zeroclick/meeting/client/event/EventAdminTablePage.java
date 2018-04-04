package org.zeroclick.meeting.client.event;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.event.EventAdminTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.EventAdminTablePageData;
import org.zeroclick.meeting.shared.event.EventCreatedNotification;
import org.zeroclick.meeting.shared.event.EventModifiedNotification;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;
import org.zeroclick.ui.action.menu.AbstractEditMenu;

@Data(EventAdminTablePageData.class)
public class EventAdminTablePage extends AbstractEventsTablePage<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.eventsAdmin");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IEventService.class).getEventAdminTableData(filter));
	}

	@Override
	protected String getConfiguredIconId() {
		return Icons.ExclamationMark;
	}

	@Override
	protected Boolean canHandle(final EventCreatedNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandle(final EventModifiedNotification notification) {
		return Boolean.FALSE;
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void execInitTable() {
			super.execInitTable();
			this.getEventIdColumn().setVisible(Boolean.TRUE);

			this.getExternalIdOrganizerColumn().setVisible(Boolean.TRUE);
			this.getExternalIdRecipientColumn().setVisible(Boolean.TRUE);
			this.getReasonColumn().setVisible(Boolean.TRUE);
			super.getStateColumn().setVisible(Boolean.TRUE);
			this.getOrganizerColumn().setVisible(Boolean.TRUE);
			this.getOrganizerEmailColumn().setVisible(Boolean.TRUE);
			this.getEmailColumn().setVisible(Boolean.TRUE);
			this.getGuestIdColumn().setVisible(Boolean.TRUE);
		}

		@Override
		protected boolean getConfiguredHeaderEnabled() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredSortEnabled() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredTableStatusVisible() {
			return Boolean.TRUE;
		}

		@Order(1500)
		public class EditEventMenu extends AbstractEditMenu {
			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final AccessControlService acs = BEANS.get(AccessControlService.class);

				final Long rowId = Table.this.getEventIdColumn().getSelectedValue();
				// Only for admin
				this.setVisible(
						acs.getPermissionLevel(new UpdateEventPermission(rowId)) >= UpdateEventPermission.LEVEL_ALL);

				this.setEnabled(EventAdminTablePage.this.isUserAddCalendarConfigured());
			}

			@Override
			protected void execAction() {
				final EventForm form = new EventForm();
				final Long currentEventId = Table.this.getEventIdColumn().getSelectedValue();
				form.setEventId(currentEventId);
				form.setEnabledPermission(new UpdateEventPermission(currentEventId));
				// start the form using its modify handler
				form.startModify();
			}
		}
	}
}
