package org.zeroclick.configuration.client.slot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.slot.AbstractSlotTablePageData;
import org.zeroclick.configuration.shared.slot.DayDurationFormData;
import org.zeroclick.configuration.shared.slot.DayDurationModifiedNotification;
import org.zeroclick.configuration.shared.slot.ISlotService;
import org.zeroclick.configuration.shared.slot.ReadSlotPermission;
import org.zeroclick.meeting.client.common.SlotLookupCall;

@Data(AbstractSlotTablePageData.class)
public abstract class AbstractSlotTablePage<T extends AbstractSlotTablePage<T>.Table> extends AbstractPageWithTable<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSlotTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.slot");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(ISlotService.class).getDayDurationTableData(filter));
		this.getTable().sort();
	}

	@Override
	protected void execInitPage() {
		super.execInitPage();
		// this.setDetailForm(new DayDurationForm());
		// this.setDetailFormVisible(Boolean.TRUE);
		this.getTable().sort();
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected IPage<?> execCreateChildPage(final ITableRow row) {
		// TODO Auto-generated method stub
		return super.execCreateChildPage(row);
	}

	protected Boolean isSlotAdmin() {
		final int currentUserSlotLevel = ACCESS.getLevel(new ReadSlotPermission((Long) null));
		return currentUserSlotLevel == ReadSlotPermission.LEVEL_ALL;
	}

	public class Table extends AbstractTable {

		protected INotificationListener<DayDurationModifiedNotification> dayDurationModifiedListener;

		@Override
		protected boolean getConfiguredHeaderEnabled() {
			return Boolean.FALSE;
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

		@Override
		protected void initConfig() {
			super.initConfig();
			this.setRowIconVisible(Boolean.FALSE);

			final DayDurationModifiedNotificationHandler createDayDurationModifiedHandler = BEANS
					.get(DayDurationModifiedNotificationHandler.class);
			createDayDurationModifiedHandler.addListener(this.createDayDurationModifiedListener());

		}

		private INotificationListener<DayDurationModifiedNotification> createDayDurationModifiedListener() {
			this.dayDurationModifiedListener = new INotificationListener<DayDurationModifiedNotification>() {
				@Override
				public void handleNotification(final DayDurationModifiedNotification notification) {
					try {
						final DayDurationFormData dayDurationForm = notification.getDayDurationForm();
						LOG.debug("Day Duration modified prepare modify row values (" + Table.this.getTitle()
								+ ") for slotCode : " + notification.getSlotCode() + " ("
								+ dayDurationForm.getDayDurationId() + ")");

						final ITableRow row = AbstractSlotTablePage.this.getTable()
								.getRow(dayDurationForm.getDayDurationId());
						Table.this.updateTableRowFromForm(row, dayDurationForm);
						Table.this.applyRowFilters();
						Table.this.sort();

					} catch (final RuntimeException e) {
						LOG.error("Could not handle modified DayDuration. (" + Table.this.getTitle() + ")", e);
					}
				}
			};

			return this.dayDurationModifiedListener;
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

		public AbstractSlotTablePage<?>.Table.EndColumn getEndColumn() {
			return this.getColumnSet().getColumnByClass(EndColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.MondayColumn getMondayColumn() {
			return this.getColumnSet().getColumnByClass(MondayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.TuesdayColumn getTuesdayColumn() {
			return this.getColumnSet().getColumnByClass(TuesdayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.WednesdayColumn getWednesdayColumn() {
			return this.getColumnSet().getColumnByClass(WednesdayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.ThursdayColumn getThursdayColumn() {
			return this.getColumnSet().getColumnByClass(ThursdayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.FridayColumn getFridayColumn() {
			return this.getColumnSet().getColumnByClass(FridayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.SundayColumn getSundayColumn() {
			return this.getColumnSet().getColumnByClass(SundayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.SlotIdColumn getSlotIdColumn() {
			return this.getColumnSet().getColumnByClass(SlotIdColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.UserIdColumn getUserIdColumn() {
			return this.getColumnSet().getColumnByClass(UserIdColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.OrderInSlotColumn getOrderInSlotColumn() {
			return this.getColumnSet().getColumnByClass(OrderInSlotColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.NameColumn getNameColumn() {
			return this.getColumnSet().getColumnByClass(NameColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.SaturdayColumn getSaturdayColumn() {
			return this.getColumnSet().getColumnByClass(SaturdayColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.SlotColumn getSlotColumn() {
			return this.getColumnSet().getColumnByClass(SlotColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.StartColumn getStartColumn() {
			return this.getColumnSet().getColumnByClass(StartColumn.class);
		}

		public AbstractSlotTablePage<?>.Table.DayDurationIdColumn getDayDurationIdColumn() {
			return this.getColumnSet().getColumnByClass(DayDurationIdColumn.class);
		}

		@Order(500)
		public class NameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.name");
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
				final String slotName = Table.this.getSlotColumn().getDisplayText(row);
				String displayValue;
				if (null == value || value.isEmpty() || defaultName.equals(value)) {
					displayValue = slotName;
				} else {
					displayValue = slotName + " (" + TEXTS.get(value) + ")";
				}
				return displayValue;
			}
		}

		@Order(1000)
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

		@Order(2000)
		public class StartColumn extends AbstractDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.hour.start");
			}

			@Override
			protected boolean getConfiguredHasDate() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 75;
			}
		}

		@Order(3000)
		public class EndColumn extends AbstractDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.hour.end");
			}

			@Override
			protected boolean getConfiguredHasDate() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 75;
			}
		}

		@Order(4000)
		public class SlotColumn extends AbstractSmartColumn<Integer> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.code");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return SlotLookupCall.class;
			}

			@Override
			protected int getConfiguredWidth() {
				return 150;
			}
		}

		@Order(4500)
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

		@Order(4625)
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

		@Order(4750)
		public class UserIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.userId");
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

		@Order(5000)
		public class MondayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.monday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(6000)
		public class TuesdayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.tuesday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(7000)
		public class WednesdayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.wednesday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(8000)
		public class ThursdayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.thursday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(9000)
		public class FridayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.friday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(10000)
		public class SaturdayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.saturday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

		@Order(11000)
		public class SundayColumn extends AbstractBooleanColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.meeting.dayDuration.sunday");
			}

			@Override
			protected int getConfiguredWidth() {
				return 35;
			}
		}

	}
}
