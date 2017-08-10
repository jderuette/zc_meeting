package org.zeroclick.meeting.client.event;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.client.event.EventAdminTablePage.Table;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.AbstractEventNotification;
import org.zeroclick.meeting.shared.event.EventAdminTablePageData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.UpdateEventPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;

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
	protected Boolean canHandleNew(final AbstractEventNotification notification) {
		return Boolean.FALSE;
	}

	@Override
	protected Boolean canHandleModified(final AbstractEventNotification notification) {
		return Boolean.FALSE;
	}

	public class Table extends AbstractEventsTablePage<Table>.Table {

		@Override
		protected void initConfig() {
			super.initConfig();
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
		public class EditMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.common.edit");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Pencil;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			// private Boolean isWorkFlowVisible() {
			// final String currentState = (String)
			// EventTablePage.this.getTable().getSelectedRow()
			// .getCell(Table.this.getStateColumn()).getValue();
			// return "ASKED".equals(currentState);
			// }

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final AccessControlService acs = BEANS.get(AccessControlService.class);

				final Long rowId = Table.this.getEventIdColumn().getSelectedValue();
				// final Boolean isHeld =
				// Table.this.isHeldByCurrentUser(EventTablePage.this.getTable().getSelectedRow());
				// this.setVisible(this.isWorkFlowVisible() && (isHeld || acs
				// .getPermissionLevel(new UpdateEventPermission(rowId)) >=
				// UpdateEventPermission.LEVEL_ALL));
				// Only for admin
				this.setVisible(
						acs.getPermissionLevel(new UpdateEventPermission(rowId)) >= UpdateEventPermission.LEVEL_ALL);

				this.setEnabled(EventAdminTablePage.this.isUserCalendarConfigured());
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

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "e");
			}
		}

	}
}
