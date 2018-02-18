package org.zeroclick.common.params;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.params.AppParamsTablePage.Table;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.params.ParamCreatedNotification;
import org.zeroclick.configuration.shared.params.ParamModifiedNotification;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.ui.action.menu.AbstractEditMenu;
import org.zeroclick.ui.action.menu.AbstractNewMenu;

@Data(AppParamsTablePageData.class)
public class AppParamsTablePage extends AbstractPageWithTable<Table> {

	private static final Logger LOG = LoggerFactory.getLogger(AppParamsTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.params");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IAppParamsService.class).getAppParamsTableData(filter));
	}

	public class Table extends AbstractTable {

		protected INotificationListener<ParamCreatedNotification> paramCreatedListener;
		protected INotificationListener<ParamModifiedNotification> paramModifiedListener;

		@Override
		protected void initConfig() {
			super.initConfig();
			final ParamCreatedNotificationHandler paramCreatedNotifHand = BEANS
					.get(ParamCreatedNotificationHandler.class);
			paramCreatedNotifHand.addListener(this.createParamCreatedListener());

			final ParamModifiedNotificationHandler paramModifiedNotifHand = BEANS
					.get(ParamModifiedNotificationHandler.class);
			paramModifiedNotifHand.addListener(this.createParamModifiedListener());
		}

		@Override
		protected void execDisposeTable() {
			final ParamCreatedNotificationHandler paramCreatedNotifHand = BEANS
					.get(ParamCreatedNotificationHandler.class);
			paramCreatedNotifHand.removeListener(this.paramCreatedListener);
			final ParamModifiedNotificationHandler paramModifiedNotifHand = BEANS
					.get(ParamModifiedNotificationHandler.class);
			paramModifiedNotifHand.removeListener(this.paramModifiedListener);

			super.execDisposeTable();
		}

		private INotificationListener<ParamModifiedNotification> createParamModifiedListener() {
			this.paramModifiedListener = new INotificationListener<ParamModifiedNotification>() {
				@Override
				public void handleNotification(final ParamModifiedNotification notification) {
					final AppParamsFormData paramForm = notification.getFormData();
					try {
						ITableRow row = AppParamsTablePage.this.getTable().getRow(paramForm.getParamId().getValue());
						if (null == row) {
							row = AppParamsTablePage.this.getTable()
									.addRow(AppParamsTablePage.this.getTable().createTableRowFromForm(paramForm));
						}
						if (null != row) {
							if (LOG.isDebugEnabled()) {
								LOG.debug(new StringBuilder().append("Modified param prepare to modify table row (in ")
										.append(Table.this.getTitle()).append(") for event Id : ")
										.append(paramForm.getParamId().getValue()).toString());
							}

							Table.this.updateTableRowFromForm(row, paramForm);

							AppParamsTablePage.this.getTable().applyRowFilters();

							final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
							notificationHelper.addProccessedNotification("zc.params.modified");

						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug(new StringBuilder()
										.append("Modified param ignored because it's not a current table row (in ")
										.append(Table.this.getTitle()).append(") for param Id : ")
										.append(paramForm.getParamId().getValue()).toString());
							}
						}

					} catch (final RuntimeException e) {
						LOG.error("Could not update param. (" + Table.this.getTitle() + ") for param Id : "
								+ paramForm.getParamId().getValue(), e);
					}
				}
			};
			return this.paramModifiedListener;
		}

		private INotificationListener<ParamCreatedNotification> createParamCreatedListener() {
			this.paramCreatedListener = new INotificationListener<ParamCreatedNotification>() {
				@Override
				public void handleNotification(final ParamCreatedNotification notification) {

					final AppParamsFormData paramForm = notification.getFormData();
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("New param prepare to add to table (in ")
								.append(Table.this.getTitle() + ") for param Id : ")
								.append(paramForm.getParamId().getValue()).toString());
					}
					try {
						AppParamsTablePage.this.getTable()
								.addRow(AppParamsTablePage.this.getTable().createTableRowFromForm(paramForm));
						AppParamsTablePage.this.getTable().applyRowFilters();

						final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
						notificationHelper.addProccessedNotification("zc.params.created");

					} catch (final RuntimeException e) {
						LOG.error("Could not add new Param. (" + Table.this.getTitle() + ")", e);
					}
				}
			};
			return this.paramCreatedListener;
		}

		protected ITableRow getRow(final Long paramId) {
			final List<ITableRow> currentRows = this.getRows();
			for (final ITableRow aRow : currentRows) {
				if (paramId.equals(aRow.getCell(this.getParamIdColumn()).getValue())) {
					return aRow;
				}
			}

			return null;
		}

		protected ITableRow createTableRowFromForm(final AppParamsFormData paramForm) {
			return new TableRow(this.getColumnSet(), this.getListFromForm(paramForm));
		}

		protected void updateTableRowFromForm(final ITableRow row, final AppParamsFormData paramForm) {
			if (null != row) {
				final List<Object> datas = this.getListFromForm(paramForm);
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

		private List<Object> getListFromForm(final AppParamsFormData paramForm) {
			final List<Object> datas = new ArrayList<>();
			datas.add(paramForm.getParamId().getValue());
			datas.add(paramForm.getKey().getValue());
			datas.add(paramForm.getValue().getValue());
			datas.add(paramForm.getCategory().getValue());
			return datas;
		}

		@Order(1000)
		public class EditParamMenu extends AbstractEditMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.params.edit");
			}

			@Override
			protected void execAction() {
				Table.this.loadParamsForm(Table.this.getSelectedRow());
			}
		}

		@Order(2000)
		public class NewParamMenu extends AbstractNewMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.params.new");
			}

			@Override
			protected void execAction() {
				Table.this.loadParamsForm();
			}
		}

		protected void loadParamsForm(final ITableRow row) {
			final List<IForm> forms = IDesktop.CURRENT.get().getForms(AppParamsTablePage.this.getOutline());

			final String paramKey = Table.this.getKeyColumn().getValue(row.getRowIndex());

			for (final IForm form : forms) {
				if (null != paramKey && paramKey.equals(form.getSubTitle())) {
					form.activate();
					// early break;
					return;
				}
			}

			final Long paramId = Table.this.getParamIdColumn().getValue(row.getRowIndex());
			final AppParamsForm appParamsForm = new AppParamsForm();

			appParamsForm.getParamIdField().setValue(paramId);
			appParamsForm.setSubTitle(paramKey);
			appParamsForm.getMainBox().setGridColumnCountHint(1);
			appParamsForm.setDisplayParent(IDesktop.CURRENT.get().getOutline());
			appParamsForm.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
			appParamsForm.setDisplayViewId(IForm.VIEW_ID_E);

			appParamsForm.startModify();
		}

		protected void loadParamsForm() {
			final AppParamsForm appParamsForm = new AppParamsForm();
			appParamsForm.getMainBox().setGridColumnCountHint(1);
			appParamsForm.setDisplayParent(IDesktop.CURRENT.get().getOutline());
			appParamsForm.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
			appParamsForm.setDisplayViewId(IForm.VIEW_ID_E);

			appParamsForm.startNew();
		}

		public KeyColumn getKeyColumn() {
			return this.getColumnSet().getColumnByClass(KeyColumn.class);
		}

		public ValueColumn getValueColumn() {
			return this.getColumnSet().getColumnByClass(ValueColumn.class);
		}

		public CategoryColumn getCategoryColumn() {
			return this.getColumnSet().getColumnByClass(CategoryColumn.class);
		}

		public ParamIdColumn getParamIdColumn() {
			return this.getColumnSet().getColumnByClass(ParamIdColumn.class);
		}

		@Order(1000)
		public class ParamIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.params.id");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class KeyColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.params.key");
			}

			@Override
			protected boolean getConfiguredSummary() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 250;
			}
		}

		@Order(3000)
		public class ValueColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.params.value");
			}

			@Override
			protected int getConfiguredWidth() {
				return 250;
			}
		}

		@Order(4000)
		public class CategoryColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.params.category");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}
	}
}
