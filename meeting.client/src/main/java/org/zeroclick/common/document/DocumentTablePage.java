package org.zeroclick.common.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.document.DocumentTablePage.Table;
import org.zeroclick.common.shared.document.DocumentCreatedNotification;
import org.zeroclick.common.shared.document.DocumentModifiedNotification;
import org.zeroclick.common.shared.document.IDocumentService;
import org.zeroclick.meeting.client.NotificationHelper;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.ui.form.columns.zoneddatecolumn.AbstractZonedDateColumn;

@Data(DocumentTablePageData.class)
public class DocumentTablePage extends AbstractPageWithTable<Table> {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.document");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IDocumentService.class).getDocumentTableData(filter));
	}

	public class Table extends AbstractTable {

		protected INotificationListener<DocumentCreatedNotification> documentCreatedListener;
		protected INotificationListener<DocumentModifiedNotification> documentModifiedListener;

		@Override
		protected void initConfig() {
			super.initConfig();

			final DocumentCreatedNotificationHandler documentCreatedNotificationHandler = BEANS
					.get(DocumentCreatedNotificationHandler.class);
			documentCreatedNotificationHandler.addListener(this.createDocumentCreatedListener());

			final DocumentModifiedNotificationHandler documentModifiedNotificationHandler = BEANS
					.get(DocumentModifiedNotificationHandler.class);
			documentModifiedNotificationHandler.addListener(this.createDocumentModifiedListener());

		}

		protected INotificationListener<DocumentCreatedNotification> createDocumentCreatedListener() {
			this.documentCreatedListener = new INotificationListener<DocumentCreatedNotification>() {
				@Override
				public void handleNotification(final DocumentCreatedNotification notification) {

					final DocumentFormData formData = notification.getFormData();
					LOG.debug("New document prepare to add to table (in " + Table.this.getTitle()
							+ ") for document Id : " + formData.getDocumentId());
					try {
						DocumentTablePage.this.getTable()
								.addRow(DocumentTablePage.this.getTable().createTableRowFromForm(formData));
						DocumentTablePage.this.getTable().applyRowFilters();

						final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
						notificationHelper.addProccessedNotification("zc.document.notification.createdDocument",
								formData.getName().getValue());

					} catch (final RuntimeException e) {
						LOG.error("Could not add new event. (" + Table.this.getTitle() + ")", e);
					}
				}
			};
			return this.documentCreatedListener;
		}

		protected INotificationListener<DocumentModifiedNotification> createDocumentModifiedListener() {
			this.documentModifiedListener = new INotificationListener<DocumentModifiedNotification>() {
				@Override
				public void handleNotification(final DocumentModifiedNotification notification) {
					final DocumentFormData formData = notification.getFormData();
					try {
						final ITableRow row = DocumentTablePage.this.getTable()
								.getRow(formData.getDocumentId().getValue());
						if (null != row) {
							LOG.debug("Modified document prepare to modify table row (in " + Table.this.getTitle()
									+ ") for document Id : " + formData.getDocumentId());

							Table.this.updateTableRowFromForm(row, formData);

							DocumentTablePage.this.getTable().applyRowFilters();

							final NotificationHelper notificationHelper = BEANS.get(NotificationHelper.class);
							notificationHelper.addProccessedNotification("zc.meeting.document.modifiedDocument",
									formData.getName().getValue());

						} else {
							LOG.debug("Modified document ignored because it's not a current table row (in "
									+ Table.this.getTitle() + ") for document Id : " + formData.getDocumentId());
						}

					} catch (final RuntimeException e) {
						LOG.error("Could not update ocument. (" + Table.this.getTitle() + ") for doument Id : "
								+ formData.getDocumentId(), e);
					}
				}
			};
			return this.documentModifiedListener;
		}

		protected ITableRow getRow(final Long documentId) {
			final List<ITableRow> currentRows = this.getRows();
			for (final ITableRow aRow : currentRows) {
				if (documentId.equals(aRow.getCell(this.getDocumentIdColumn()).getValue())) {
					return aRow;
				}
			}

			return null;
		}

		private List<Object> getListFromForm(final DocumentFormData formData) {
			final List<Object> datas = new ArrayList<>();
			datas.add(formData.getName().getValue());
			datas.add(formData.getDocumentId().getValue());
			datas.add(formData.getContent().getValue());
			datas.add(formData.getLastModificationDate().getValue());
			return datas;
		}

		protected ITableRow createTableRowFromForm(final DocumentFormData formData) {
			return new TableRow(this.getColumnSet(), this.getListFromForm(formData));
		}

		protected void updateTableRowFromForm(final ITableRow row, final DocumentFormData formData) {
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

		@Override
		protected void execDisposeTable() {
			final DocumentCreatedNotificationHandler documentCreatedNotificationHandler = BEANS
					.get(DocumentCreatedNotificationHandler.class);
			documentCreatedNotificationHandler.removeListener(this.documentCreatedListener);

			final DocumentModifiedNotificationHandler documentModifiedNotificationHandler = BEANS
					.get(DocumentModifiedNotificationHandler.class);
			documentModifiedNotificationHandler.removeListener(this.documentModifiedListener);

			super.execDisposeTable();
		}

		@Order(1000)
		public class NewDocumentMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.document.new");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Plus;
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
						TableMenuType.EmptySpace);
			}

			@Override
			protected void execAction() {
				final DocumentForm form = new DocumentForm();
				form.startNew();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "n");
			}
		}

		@Order(2000)
		public class EditDocumentMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.document.edit");
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
			protected void execAction() {
				final DocumentForm form = new DocumentForm();
				form.getDocumentIdField().setValue(Table.this.getDocumentIdColumn().getSelectedValue());
				form.startModify();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "e");
			}
		}

		public NameColumn getNameColumn() {
			return this.getColumnSet().getColumnByClass(NameColumn.class);
		}

		public ContentColumn getContentColumn() {
			return this.getColumnSet().getColumnByClass(ContentColumn.class);
		}

		public LastModificationDateColumn getLastModificationDateColumn() {
			return this.getColumnSet().getColumnByClass(LastModificationDateColumn.class);
		}

		public DocumentIdColumn getDocumentIdColumn() {
			return this.getColumnSet().getColumnByClass(DocumentIdColumn.class);
		}

		@Order(1000)
		public class NameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.document.name");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(2000)
		public class DocumentIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.document.id");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(3000)
		public class ContentColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.document.content");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.FALSE;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(4000)
		public class LastModificationDateColumn extends AbstractZonedDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.document.lastModificationDate");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

	}
}
