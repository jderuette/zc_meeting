package org.zeroclick.meeting.client.event.involevment;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.shared.user.UserLookupCall;
import org.zeroclick.meeting.client.event.involevment.InvolvementTablePage.Table;
import org.zeroclick.meeting.shared.event.StateCodeType;
import org.zeroclick.meeting.shared.event.involevment.EventRoleCodeType;
import org.zeroclick.meeting.shared.event.involevment.EventStateCodeType;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementTablePageData;
import org.zeroclick.ui.action.menu.AbstractEditMenu;
import org.zeroclick.ui.action.menu.AbstractNewMenu;
import org.zeroclick.ui.form.columns.userid.AbstractUserIdColumn;

@Data(InvolvementTablePageData.class)
public class InvolvementTablePage extends AbstractPageWithTable<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.involevment");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IInvolvementService.class).getInvolevmentTableData(filter));
	}

	public class Table extends AbstractTable {

		@Order(1000)
		public class NewInvolvementMenu extends AbstractNewMenu {

			@Override
			protected void execAction() {
				final InvolvementForm form = new InvolvementForm();
				form.addFormListener(new InvolvementFormListener());
				form.startNew();
			}
		}

		@Order(2000)
		public class EditInvolvementMenu extends AbstractEditMenu {

			@Override
			protected void execAction() {
				final InvolvementForm form = new InvolvementForm();
				form.getEventIdField().setValue(Table.this.getEventIdColumn().getSelectedValue());
				form.getUserIdField().setValue(Table.this.getUserIdColumn().getSelectedValue());
				form.addFormListener(new InvolvementFormListener());
				form.startModify();
			}
		}

		private class InvolvementFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
					InvolvementTablePage.this.reloadPage();
				}
			}
		}

		public RoleColumn getRoleColumn() {
			return this.getColumnSet().getColumnByClass(RoleColumn.class);
		}

		public ExternalEventIdColumn getExternalEventIdColumn() {
			return this.getColumnSet().getColumnByClass(ExternalEventIdColumn.class);
		}

		public InvitedByColumn getInvitedByColumn() {
			return this.getColumnSet().getColumnByClass(InvitedByColumn.class);
		}

		public ReasonColumn getReasonColumn() {
			return this.getColumnSet().getColumnByClass(ReasonColumn.class);
		}

		public StateColumn getStateColumn() {
			return this.getColumnSet().getColumnByClass(StateColumn.class);
		}

		public UserIdColumn getUserIdColumn() {
			return this.getColumnSet().getColumnByClass(UserIdColumn.class);
		}

		public EventIdColumn getEventIdColumn() {
			return this.getColumnSet().getColumnByClass(EventIdColumn.class);
		}

		@Order(1000)
		public class EventIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.eventId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class UserIdColumn extends AbstractUserIdColumn {
		}

		@Order(3000)
		public class RoleColumn extends AbstractSmartColumn<String> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.role");
			}

			@Override
			protected Class<? extends ICodeType<?, String>> getConfiguredCodeType() {
				return EventRoleCodeType.class;
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(4000)
		public class StateColumn extends AbstractSmartColumn<String> {
			// In User context, so texts are translated
			final EventStateCodeType eventStateCodes = new EventStateCodeType();

			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.state");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}

			@Override
			protected void execDecorateCell(final Cell cell, final ITableRow row) {
				super.execDecorateCell(cell, row);

				final String stateColumnValue = (String) cell.getValue();

				final ICode<String> currentStateCode = this.eventStateCodes.getCode(stateColumnValue);
				cell.setIconId(currentStateCode.getIconId());
				cell.setBackgroundColor(currentStateCode.getBackgroundColor());
				cell.setForegroundColor(currentStateCode.getForegroundColor());
				cell.setText(currentStateCode.getText());
			}

			@Override
			protected Class<? extends ICodeType<Long, String>> getConfiguredCodeType() {
				return StateCodeType.class;
			}
		}

		@Order(5000)
		public class ReasonColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.rejectReason");
			}

			@Override
			protected int getConfiguredWidth() {
				return 300;
			}
		}

		@Order(6000)
		public class ExternalEventIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.externalEventId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(7000)
		public class InvitedByColumn extends AbstractSmartColumn<Long> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.event.involevment.invitedBy");
			}

			@Override
			protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
				return UserLookupCall.class;
			}

			@Override
			protected int getConfiguredWidth() {
				return 200;
			}
		}

	}
}
