/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.meeting.client.calendar;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.html.internal.HtmlPlainBuilder;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.api.ApiLookupCall;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.service.CalendarService;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.security.AccessControlService;
import org.zeroclick.ui.action.menu.AbstractEditMenu;

/**
 * @author djer
 *
 */
public abstract class AbstractCalendarConfigurationTable extends AbstractTable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCalendarConfigurationTable.class);

	private boolean displayAllUsers;

	public boolean isDisplayAllUsers() {
		return this.displayAllUsers;
	}

	public void setDisplayAllUsers(final boolean displayAllUsers) {
		this.displayAllUsers = displayAllUsers;
	}

	@Override
	protected boolean getConfiguredHeaderEnabled() {
		return true;
	}

	@Override
	protected boolean getConfiguredAutoResizeColumns() {
		return Boolean.TRUE;
	}

	@Override
	protected boolean getConfiguredMultilineText() {
		return true;
	}

	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredDisplayAllUsers() {
		return false;
	}

	@SuppressWarnings("PMD.BooleanGetMethodName")
	protected boolean getConfiguredAutoLoad() {
		return true;
	}

	@Override
	protected void execInitTable() {
		this.displayAllUsers = this.getConfiguredDisplayAllUsers();
		this.loadData();

		if (AbstractCalendarConfigurationTable.this.isDisplayAllUsers()) {
			this.getUserIdColumn().setVisible(true);
			this.getUserIdColumn().setGroupingUsed(true);
			this.getUserIdColumn().setInitialGrouped(true);
			this.getUserIdColumn().setInitialSortIndex(1);
			this.getUserIdColumn().setInitialSortAscending(true);
			final List<IColumn<?>> columns = this.getColumns();

			if (null != columns && columns.size() > 0) {
				for (final IColumn<?> column : columns) {
					column.setEditable(false);
				}
			}

			final AutoImportMyCalendarsMenu autoImportMenu = this.getMenuByClass(AutoImportMyCalendarsMenu.class);
			autoImportMenu.setVisible(false);

			final EditCalendarCondfigMenu editCalendarConfigurationMenu = this
					.getMenuByClass(EditCalendarCondfigMenu.class);
			editCalendarConfigurationMenu.setVisible(true);
		}
	}

	protected void loadData() {
		this.importFromTableBeanData(BEANS.get(ICalendarConfigurationService.class)
				.getCalendarConfigurationTableData(this.isDisplayAllUsers()).getCalendarConfigTable());
	}

	public CalendarConfigurationFormData getRowAsFormData(final ITableRow row) {
		final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();

		formData.getCalendarConfigurationId().setValue(this.getCalendarConfigurationIdColumn().getValue(row));
		formData.getExternalId().setValue(this.getExternalIdColumn().getValue(row));
		formData.getName().setValue(this.getNameColumn().getValue(row));
		formData.getReadOnly().setValue(this.getReadOnlyColumn().getValue(row));
		formData.getMain().setValue(this.getMainColumn().getValue(row));
		formData.getProcess().setValue(this.getProcessColumn().getValue(row));
		formData.getAddEventToCalendar().setValue(this.getAddEventToCalendarColumn().getValue(row));
		formData.getProcessFullDayEvent().setValue(this.getProcessFullDayEventColumn().getValue(row));
		formData.getProcessFreeEvent().setValue(this.getProcessFreeEventColumn().getValue(row));
		formData.getProcessNotRegistredOnEvent().setValue(this.getProcessNotRegistredOnEventColumn().getValue(row));
		formData.getOAuthCredentialId().setValue(this.getOAuthCredentialIdColumn().getValue(row));
		formData.getUserId().setValue(this.getUserIdColumn().getValue(row));

		return formData;
	}

	public CalendarConfigurationIdColumn getCalendarConfigurationIdColumn() {
		return this.getColumnSet().getColumnByClass(CalendarConfigurationIdColumn.class);
	}

	public ExternalIdColumn getExternalIdColumn() {
		return this.getColumnSet().getColumnByClass(ExternalIdColumn.class);
	}

	public NameColumn getNameColumn() {
		return this.getColumnSet().getColumnByClass(NameColumn.class);
	}

	public ReadOnlyColumn getReadOnlyColumn() {
		return this.getColumnSet().getColumnByClass(ReadOnlyColumn.class);
	}

	public MainColumn getMainColumn() {
		return this.getColumnSet().getColumnByClass(MainColumn.class);
	}

	public ProcessColumn getProcessColumn() {
		return this.getColumnSet().getColumnByClass(ProcessColumn.class);
	}

	public AddEventToCalendarColumn getAddEventToCalendarColumn() {
		return this.getColumnSet().getColumnByClass(AddEventToCalendarColumn.class);
	}

	public ProcessFullDayEventColumn getProcessFullDayEventColumn() {
		return this.getColumnSet().getColumnByClass(ProcessFullDayEventColumn.class);
	}

	public ProcessFreeEventColumn getProcessFreeEventColumn() {
		return this.getColumnSet().getColumnByClass(ProcessFreeEventColumn.class);
	}

	public ProcessNotRegistredOnEventColumn getProcessNotRegistredOnEventColumn() {
		return this.getColumnSet().getColumnByClass(ProcessNotRegistredOnEventColumn.class);
	}

	public OAuthCredentialIdColumn getOAuthCredentialIdColumn() {
		return this.getColumnSet().getColumnByClass(OAuthCredentialIdColumn.class);
	}

	public AbstractCalendarConfigurationTable.UserIdColumn getUserIdColumn() {
		return this.getColumnSet().getColumnByClass(UserIdColumn.class);
	}

	@Order(1000)
	public class EditCalendarCondfigMenu extends AbstractEditMenu {

		@Override
		protected void execAction() {
			final CalendarConfigurationForm form = new CalendarConfigurationForm();
			final Long calendarId = AbstractCalendarConfigurationTable.this.getCalendarConfigurationIdColumn()
					.getSelectedValue();
			form.getCalendarConfigurationIdField().setValue(calendarId);
			form.setEnabledPermission(new UpdateCalendarConfigurationPermission(calendarId));
			form.addFormListener(new FormListener() {

				@Override
				public void formChanged(final FormEvent fromEvent) {
					if (FormEvent.TYPE_CLOSED == fromEvent.getType() && form.isFormStored()) {
						AbstractCalendarConfigurationTable.this.loadData();
					}
				}
			});
			form.startModify();
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}
	}

	@Order(2000)
	public class AutoImportMyCalendarsMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.calendar.synchronise");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
					TableMenuType.EmptySpace);
		}

		@Override
		protected void execAction() {
			final GoogleApiHelper googleHelper = BEANS.get(GoogleApiHelper.class);
			final CalendarService calendarService = BEANS.get(CalendarService.class);

			if (!calendarService.isCalendarConfigured()) {
				googleHelper.askToAddApi(BEANS.get(AccessControlService.class).getZeroClickUserIdOfCurrentSubject());
			}
			if (!calendarService.isCalendarConfigured()) {
				// User really won't provide required data
				return; // early Break
			}
			calendarService.autoConfigureCalendars();

			AbstractCalendarConfigurationTable.this.loadData();
		}
	}

	@Order(2000)
	public class RefreshMenu extends AbstractMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.calendar.refresh");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
					TableMenuType.EmptySpace);
		}

		@Override
		protected void execAction() {
			AbstractCalendarConfigurationTable.this.loadData();
		}
	}

	private Long getFirstCalendarToAddEvent() {
		final List<ITableRow> rows = this.getRows();

		for (final ITableRow row : rows) {
			if (this.getAddEventToCalendarColumn().getValue(row)) {
				return this.getCalendarConfigurationIdColumn().getValue(row);
			}
		}
		return null;
	}

	protected Integer askToAddAtLeastOneCalendarToStoreEvent() {
		if (null == this.getFirstCalendarToAddEvent()) {
			final int userDecision = MessageBoxes.createYesNo()
					.withHeader(TEXTS.get("zc.meeting.calendar.askToAddCalendarToStoreEvent.title"))
					.withBody(TEXTS.get("zc.meeting.calendar.askToAddCalendarToStoreEvent.message"))
					.withYesButtonText(TEXTS.get("zc.meeting.calendar.askToAddCalendarToStoreEvent.yesButton"))
					.withNoButtonText(TEXTS.get("zc.meeting.calendar.askToAddCalendarToStoreEvent.noButton"))
					.withIconId(Icons.ExclamationMark).withSeverity(IStatus.WARNING).show();

			return userDecision;
		}
		return null;
	}

	@Override
	protected void execDecorateRow(final ITableRow row) {
		if (this.getProcessColumn().getValue(row)) {
			row.setBackgroundColor(this.buildRowBackgroundColor(row));
		} else {
			row.setBackgroundColor(null);
		}
	}

	@Override
	protected void execDecorateCell(final Cell view, final ITableRow row, final IColumn<?> col) {
		if (col.equals(this.getAddEventToCalendarColumn())) {
			if (this.getReadOnlyColumn().getValue(row)) {
				this.disableCell(view);
			} else {
				this.enableCell(view, this.buildRowBackgroundColor(row));
			}
		}

		if (col.equals(this.getProcessFreeEventColumn()) || col.equals(this.getProcessFullDayEventColumn())
				|| col.equals(this.getProcessNotRegistredOnEventColumn())) {
			if (this.getProcessColumn().getValue(row)) {
				this.enableCell(view, this.buildRowBackgroundColor(row));
			} else {
				this.disableCell(view);
			}
		}
	}

	private String buildRowBackgroundColor(final ITableRow row) {
		String rowColor = "ffffff";
		if (this.isHightLight(row)) {
			rowColor = "a0f296";
		}
		return rowColor;
	}

	private Boolean isHightLight(final ITableRow row) {
		return this.getProcessColumn().getValue(row);
	}

	private void enableCell(final Cell view, final String rowColor) {
		if (!this.displayAllUsers) {
			view.setEditable(true);
		}
		view.setBackgroundColor(rowColor);
	}

	private void disableCell(final Cell view) {
		view.setEditable(false);
		view.setBackgroundColor("f6f6f6");
	}

	protected Boolean canStore() {
		final Integer userDecision = this.askToAddAtLeastOneCalendarToStoreEvent();
		return null == userDecision || userDecision == IMessageBox.YES_OPTION;
	}

	@Order(1000)
	public class CalendarConfigurationIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.id");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 50;
		}
	}

	@Order(2000)
	public class OAuthCredentialIdColumn extends AbstractSmartColumn<Long> {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.OAuthCredentialId.accountEmail");
		}

		@Override
		protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
			return ApiLookupCall.class;
		}

		@Override
		protected int getConfiguredSortIndex() {
			return 0;
		}

		@Override
		protected int getConfiguredWidth() {
			return 150;
		}
	}

	@Order(3000)
	public class ExternalIdColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.externalId");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 150;
		}
	}

	@Order(4000)
	public class NameColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.name");
		}

		@Override
		protected int getConfiguredWidth() {
			return 150;
		}
	}

	@Order(5000)
	public class ReadOnlyColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.readOnly");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 75;
		}
	}

	@Order(6000)
	public class MainColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.main");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 75;
		}
	}

	@Order(7000)
	public class ProcessColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.processThisCalendar");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 90;
		}
	}

	@Order(8000)
	public class AddEventToCalendarColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.addEventToThisCalendar");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected Boolean execValidateValue(final ITableRow row, final Boolean rawValue) {
			// the cell is checked, disabling other "addToCalendar"
			if (rawValue) {
				this.checkCanStoreNewEvent(row);
				this.uncheckOtherAddToCalendar(row);
			}
			return super.execValidateValue(row, rawValue);
		}

		@Override
		protected int getConfiguredWidth() {
			return 90;
		}

		private void checkCanStoreNewEvent(final ITableRow row) {
			if (AbstractCalendarConfigurationTable.this.getReadOnlyColumn().getValue(row)) {
				final HtmlPlainBuilder message = new HtmlPlainBuilder(
						TEXTS.get("zc.meeting.calendar.cannotStoreEventToReadOnlyCalendar.text"));
				throw new ProcessingException(TEXTS.get("zc.meeting.calendar.cannotStoreEventToReadOnlyCalendar.title"),
						message);
				// throw new VetoException().withHtmlMessage(message)
				// .withTitle(TEXTS.get("zc.meeting.calendar.cannotStoreEventToReadOnlyCalendar.title"));
			}
		}

		protected void uncheckOtherAddToCalendar(final ITableRow row) {
			final List<ITableRow> rows = AbstractCalendarConfigurationTable.this.getRows();
			final Long checkedCalendarId = AbstractCalendarConfigurationTable.this.getCalendarConfigurationIdColumn()
					.getValue(row);
			final Long userId = AbstractCalendarConfigurationTable.this.getUserIdColumn().getValue(row);

			if (null != rows) {
				for (final ITableRow aRow : rows) {
					final Long aRowCalendarConfigId = AbstractCalendarConfigurationTable.this
							.getCalendarConfigurationIdColumn().getValue(aRow);
					final Long aRowUserId = AbstractCalendarConfigurationTable.this.getUserIdColumn().getValue(aRow);
					final boolean isRowChecked = AbstractCalendarConfigurationTable.this.getAddEventToCalendarColumn()
							.getValue(aRow);

					if (aRowCalendarConfigId != checkedCalendarId && aRowUserId.equals(userId) && isRowChecked) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Unchecking addEventToCalendar for calendar configuration ID : "
									+ aRowCalendarConfigId);
						}
						AbstractCalendarConfigurationTable.this.getAddEventToCalendarColumn().setValue(aRow, false);
					}
				}
			}
		}
	}

	@Order(9000)
	public class ProcessFullDayEventColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.processFullDayEvent");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 90;
		}

		@Override
		protected String getConfiguredHeaderTooltipText() {
			return TEXTS.get("zc.meeting.calendar.processFullDayEvent.tooltip");
		}
	}

	@Order(10000)
	public class ProcessFreeEventColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.processFreeEvent");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 90;
		}

		@Override
		protected String getConfiguredHeaderTooltipText() {
			return TEXTS.get("zc.meeting.calendar.processFreeEvent.tooltip");
		}
	}

	@Order(11000)
	public class ProcessNotRegistredOnEventColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.processNotRegisteredOnEvent");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 90;
		}

		@Override
		protected String getConfiguredHeaderTooltipText() {
			return TEXTS.get("zc.meeting.calendar.processNotRegisteredOnEvent.tooltip");
		}
	}

	@Order(12000)
	public class UserIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.calendar.userId");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}

		@Override
		protected int getConfiguredWidth() {
			return 100;
		}
	}
}
