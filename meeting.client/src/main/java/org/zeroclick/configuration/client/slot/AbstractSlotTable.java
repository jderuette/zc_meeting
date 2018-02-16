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
package org.zeroclick.configuration.client.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.DayDurationModifiedNotification;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.configuration.shared.slot.SlotCodeType;
import org.zeroclick.ui.action.menu.AbstractEditMenu;

/**
 * @author djer
 *
 */
public abstract class AbstractSlotTable extends AbstractTable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSlotTable.class);

	private boolean displayAllUsers;
	protected INotificationListener<DayDurationModifiedNotification> dayDurationModifiedListener;

	public boolean isDisplayAllUsers() {
		return this.displayAllUsers;
	}

	public void setDisplayAllUsers(final boolean displayAllUsers) {
		this.displayAllUsers = displayAllUsers;
	}

	@Override
	protected boolean getConfiguredHeaderEnabled() {
		return this.isSlotAdmin();
	}

	@Override
	protected boolean getConfiguredSortEnabled() {
		return Boolean.TRUE;
	}

	@Override
	protected boolean getConfiguredTableStatusVisible() {
		return Boolean.FALSE;
	}

	@Override
	protected boolean getConfiguredAutoResizeColumns() {
		return Boolean.TRUE;
	}

	protected boolean getConfiguredDisplayAllUsers() {
		return false;
	}

	private void loadData() {
		this.importFromTableBeanData(BEANS.get(ISlotService.class).getDayDurationTableData(null));
		this.sort();
	}

	@Override
	protected void initConfig() {
		this.displayAllUsers = this.getConfiguredDisplayAllUsers();
		super.initConfig();
		this.setRowIconVisible(Boolean.FALSE);

		final DayDurationModifiedNotificationHandler createDayDurationModifiedHandler = BEANS
				.get(DayDurationModifiedNotificationHandler.class);
		createDayDurationModifiedHandler.addListener(this.createDayDurationModifiedListener());

		if (this.displayAllUsers) {
			final List<IColumn<?>> columns = this.getColumns();

			if (null != columns && columns.size() > 0) {
				for (final IColumn<?> column : columns) {
					column.setEditable(false);
				}
			}
		}
		this.loadData();
	}

	@Override
	protected void execDisposeTable() {
		final DayDurationModifiedNotificationHandler createDayDurationModifiedHandler = BEANS
				.get(DayDurationModifiedNotificationHandler.class);
		createDayDurationModifiedHandler.removeListener(this.dayDurationModifiedListener);
	}

	private INotificationListener<DayDurationModifiedNotification> createDayDurationModifiedListener() {
		this.dayDurationModifiedListener = new INotificationListener<DayDurationModifiedNotification>() {
			@Override
			public void handleNotification(final DayDurationModifiedNotification notification) {
				try {
					final DayDurationFormData dayDurationForm = notification.getDayDurationForm();
					LOG.debug("Day Duration modified prepare modify row values (" + AbstractSlotTable.this.getTitle()
							+ ") for slotCode : " + notification.getSlotCode() + " ("
							+ dayDurationForm.getDayDurationId() + ")");

					final ITableRow row = AbstractSlotTable.this.getRow(dayDurationForm.getDayDurationId());
					AbstractSlotTable.this.updateTableRowFromForm(row, dayDurationForm);
					AbstractSlotTable.this.applyRowFilters();
					AbstractSlotTable.this.sort();

				} catch (final RuntimeException e) {
					LOG.error("Could not handle modified DayDuration. (" + AbstractSlotTable.this.getTitle() + ")", e);
				}
			}
		};

		return this.dayDurationModifiedListener;
	}

	protected Boolean isSlotAdmin() {
		final int currentUserSlotLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		return currentUserSlotLevel == ReadSlotPermission.LEVEL_ALL;
	}

	protected void updateTableRowFromForm(final ITableRow row, final DayDurationFormData formData) {
		if (null != row) {
			final List<Object> datas = this.getListFromForm(formData);
			for (int i = 0; i < datas.size(); i++) {
				final Object propertyFormData = datas.get(i);
				final ICell cell = row.getCell(i);
				if (propertyFormData != cell) {
					// TODO enable validation ??
					// row.getTable().getColumns().get(i).setValue(row,
					// propertyFormData);
					row.setCellValue(i, propertyFormData);
				}
			}
		}
	}

	private List<Object> getListFromForm(final DayDurationFormData formData) {
		final List<Object> datas = new ArrayList<>();
		datas.add(formData.getName());
		datas.add(formData.getDayDurationId());
		datas.add(formData.getSlotStart().getValue());
		datas.add(formData.getSlotEnd().getValue());
		datas.add(formData.getSlotCode());
		datas.add(formData.getSlotId());
		datas.add(formData.getOrderInSlot().getValue());
		datas.add(formData.getUserId());
		datas.add(formData.getMonday().getValue());
		datas.add(formData.getTuesday().getValue());
		datas.add(formData.getWednesday().getValue());
		datas.add(formData.getThursday().getValue());
		datas.add(formData.getFriday().getValue());
		datas.add(formData.getSaturday().getValue());
		datas.add(formData.getSunday().getValue());
		return datas;
	}

	protected ITableRow getRow(final Long dayDurationId) {
		final List<ITableRow> currentRows = this.getRows();
		for (final ITableRow aRow : currentRows) {
			if (dayDurationId.equals(aRow.getCell(this.getDayDurationIdColumn()).getValue())) {
				return aRow;
			}
		}

		return null;
	}

	@Order(1000)
	public class EditDayDurationMenu extends AbstractEditMenu {
		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.dayDuration.edit");
		}

		@Override
		protected Set<? extends IMenuType> getConfiguredMenuTypes() {
			return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
		}

		@Override
		protected void execAction() {
			final List<ITableRow> selectedRows = AbstractSlotTable.this.getSelectedRows();
			if (null != selectedRows && !selectedRows.isEmpty()) {
				for (final ITableRow row : selectedRows) {
					if (null != row) {
						AbstractSlotTable.this.loadDayDurationForm(row);
					}
				}
			}
		}
	}

	protected void loadDayDurationForm(final ITableRow row) {
		// final Long slotCode =
		// this.getSlotColumn().getValue(row.getRowIndex());
		final String dayDurationName = this.getNameColumn().buildDisplayValue(row);

		final List<IForm> forms = IDesktop.CURRENT.get().getForms(IDesktop.CURRENT.get().getOutline());

		final StringBuilder slotNameBuilder = new StringBuilder();
		slotNameBuilder.append(dayDurationName);

		final String slotName = slotNameBuilder.toString();

		for (final IForm form : forms) {
			if (null != slotName && slotName.equals(form.getSubTitle())) {
				form.activate();
				// early break;
				return;
			}
		}

		final Long dayDurationId = this.getDayDurationIdColumn().getValue(row.getRowIndex());
		final Long slotId = this.getSlotIdColumn().getValue(row.getRowIndex());

		final Long userId = this.getUserIdColumn().getValue(row.getRowIndex());
		final DayDurationForm dayDurationForm = new DayDurationForm();

		dayDurationForm.setUserId(userId);
		dayDurationForm.setSubTitle(slotName);
		dayDurationForm.getMainBox().setGridColumnCountHint(1);
		dayDurationForm.setDayDurationId(dayDurationId);
		dayDurationForm.setSlotId(slotId);
		dayDurationForm.setDisplayParent(IDesktop.CURRENT.get().getOutline());
		dayDurationForm.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
		// dayDurationForm.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
		// dayDurationForm.setDisplayViewId(IForm.VIEW_ID_E);

		dayDurationForm.startModify();
	}

	public DayDurationIdColumn getDayDurationIdColumn() {
		return this.getColumnSet().getColumnByClass(DayDurationIdColumn.class);
	}

	public WeeklyPerpetualColumn getWeeklyPerpetualColumn() {
		return this.getColumnSet().getColumnByClass(WeeklyPerpetualColumn.class);
	}

	public NameColumn getNameColumn() {
		return this.getColumnSet().getColumnByClass(NameColumn.class);
	}

	public SlotStartColumn getSlotStartColumn() {
		return this.getColumnSet().getColumnByClass(SlotStartColumn.class);
	}

	public SlotEndColumn getSlotEndColumn() {
		return this.getColumnSet().getColumnByClass(SlotEndColumn.class);
	}

	public MondayColumn getMondayColumn() {
		return this.getColumnSet().getColumnByClass(MondayColumn.class);
	}

	public TuesdayColumn getTuesdayColumn() {
		return this.getColumnSet().getColumnByClass(TuesdayColumn.class);
	}

	public WednesdayColumn getWednesdayColumn() {
		return this.getColumnSet().getColumnByClass(WednesdayColumn.class);
	}

	public ThursdayColumn getThursdayColumn() {
		return this.getColumnSet().getColumnByClass(ThursdayColumn.class);
	}

	public FridayColumn getFridayColumn() {
		return this.getColumnSet().getColumnByClass(FridayColumn.class);
	}

	public SaturdayColumn getSaturdayColumn() {
		return this.getColumnSet().getColumnByClass(SaturdayColumn.class);
	}

	public SundayColumn getSundayColumn() {
		return this.getColumnSet().getColumnByClass(SundayColumn.class);
	}

	public SlotIdColumn getSlotIdColumn() {
		return this.getColumnSet().getColumnByClass(SlotIdColumn.class);
	}

	public UserIdColumn getUserIdColumn() {
		return this.getColumnSet().getColumnByClass(UserIdColumn.class);
	}

	public OrderInSlotColumn getOrderInSlotColumn() {
		return this.getColumnSet().getColumnByClass(OrderInSlotColumn.class);
	}

	public SlotColumn getSlotColumn() {
		return this.getColumnSet().getColumnByClass(SlotColumn.class);
	}

	@Order(1000)
	public class NameColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.name");
		}

		@Override
		protected boolean getConfiguredSummary() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 150;
		}

		@Override
		protected void execDecorateCell(final Cell cell, final ITableRow row) {
			super.execDecorateCell(cell, row);
			cell.setText(this.buildDisplayValue(row));
		}

		public String buildDisplayValue(final ITableRow row) {
			final String defaultName = "zc.meeting.dayDuration.default";
			final String value = this.getValue(row.getRowIndex());
			final String slotName = AbstractSlotTable.this.getSlotColumn().getDisplayText(row);
			String displayValue;
			if (null == value || value.isEmpty() || defaultName.equals(value)) {
				displayValue = slotName;
			} else {
				displayValue = slotName + " (" + TEXTS.get(value) + ")";
			}
			return displayValue;
		}
	}

	@Order(2000)
	public class DayDurationIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.DayDuration.id");
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

	@Order(3000)
	public class SlotStartColumn extends AbstractTimeColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.hour.start");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 75;
		}

	}

	@Order(4000)
	public class SlotEndColumn extends AbstractTimeColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.hour.end");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 75;
		}
	}

	@Order(5000)
	public class SlotColumn extends AbstractSmartColumn<Long> {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.code");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected Class<? extends ICodeType<Long, Long>> getConfiguredCodeType() {
			return SlotCodeType.class;
		}

		@Override
		protected int getConfiguredWidth() {
			return 150;
		}
	}

	@Order(6000)
	public class SlotIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.slotId");
		}

		@Override
		protected boolean getConfiguredSortAscending() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredSortIndex() {
			return 2;
		}

		@Override
		protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
			return Boolean.TRUE;
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

	@Order(7000)
	public class OrderInSlotColumn extends AbstractIntegerColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.orderInSlot");
		}

		@Override
		protected boolean getConfiguredSortAscending() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredSortIndex() {
			return 3;
		}

		@Override
		protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
			return Boolean.TRUE;
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

	@Order(8000)
	public class UserIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.userId");
		}

		@Override
		protected boolean getConfiguredSummary() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredVisible() {
			return Boolean.FALSE;
		}

		@Override
		protected boolean getConfiguredSortAscending() {
			return Boolean.TRUE;
		}

		@Override
		protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredSortIndex() {
			return 1;
		}

		@Override
		protected int getConfiguredWidth() {
			return 100;
		}
	}

	@Order(9000)
	public class MondayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.monday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(10000)
	public class TuesdayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.tuesday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(11000)
	public class WednesdayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.wednesday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(12000)
	public class ThursdayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.thursday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(13000)
	public class FridayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.friday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(14000)
	public class SaturdayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.saturday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(15000)
	public class SundayColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.sunday");
		}

		@Override
		protected boolean getConfiguredEditable() {
			return true;
		}

		@Override
		protected int getConfiguredWidth() {
			return 35;
		}
	}

	@Order(16000)
	public class WeeklyPerpetualColumn extends AbstractBooleanColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.meeting.dayDuration.weeklyPerpetual");
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
