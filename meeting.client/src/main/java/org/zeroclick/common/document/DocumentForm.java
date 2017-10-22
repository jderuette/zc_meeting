package org.zeroclick.common.document;

import java.util.Date;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.common.document.DocumentForm.MainBox.CancelButton;
import org.zeroclick.common.document.DocumentForm.MainBox.ContentField;
import org.zeroclick.common.document.DocumentForm.MainBox.DocumentIdField;
import org.zeroclick.common.document.DocumentForm.MainBox.DocumentMetadataBox;
import org.zeroclick.common.document.DocumentForm.MainBox.DocumentMetadataBox.LinkedRoleBox;
import org.zeroclick.common.document.DocumentForm.MainBox.DocumentMetadataBox.LinkedRoleBox.LinkedRoleField;
import org.zeroclick.common.document.DocumentForm.MainBox.LastModificationDateField;
import org.zeroclick.common.document.DocumentForm.MainBox.NameField;
import org.zeroclick.common.document.DocumentForm.MainBox.OkButton;
import org.zeroclick.common.document.DocumentForm.MainBox.PreviewHtmlField;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm;
import org.zeroclick.common.shared.document.CreateDocumentPermission;
import org.zeroclick.common.shared.document.IDocumentService;
import org.zeroclick.common.shared.document.UpdateDocumentPermission;
import org.zeroclick.configuration.shared.role.RoleAndSubscriptionLookupCall;
import org.zeroclick.meeting.shared.Icons;

@FormData(value = DocumentFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class DocumentForm extends AbstractForm {

	private byte[] contentData;

	@FormData
	public byte[] getContentData() {
		return null == this.contentData ? null : this.contentData.clone();
	}

	@FormData
	public void setContentData(final byte[] contentData) {
		this.contentData = null == contentData ? null : contentData.clone();
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.document");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public DocumentIdField getDocumentIdField() {
		return this.getFieldByClass(DocumentIdField.class);
	}

	public ContentField getContentField() {
		return this.getFieldByClass(ContentField.class);
	}

	public LastModificationDateField getLastModificationDateField() {
		return this.getFieldByClass(LastModificationDateField.class);
	}

	public PreviewHtmlField getPreviewHtmlField() {
		return this.getFieldByClass(PreviewHtmlField.class);
	}

	public DocumentMetadataBox getDocumentMetadataBox() {
		return this.getFieldByClass(DocumentMetadataBox.class);
	}

	public LinkedRoleBox getLinkedRoleBox() {
		return this.getFieldByClass(LinkedRoleBox.class);
	}

	public LinkedRoleField getLinkedRoleField() {
		return this.getFieldByClass(LinkedRoleField.class);
	}

	public NameField getNameField() {
		return this.getFieldByClass(NameField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class DocumentIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.id");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 999999999999L;
			}
		}

		@Order(2000)
		public class NameField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.name");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(3000)
		public class ContentField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.content");
			}

			@Override
			protected boolean getConfiguredHtmlEnabled() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredMultilineText() {
				return Boolean.TRUE;
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected int getConfiguredGridH() {
				return 10;
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected boolean getConfiguredSelectionTrackingEnabled() {
				return Boolean.TRUE;
			}

			@Override
			protected void execChangedDisplayText() {
				final String displayText = this.getDisplayText();
				DocumentForm.this.getPreviewHtmlField().setValue(displayText);
			}
		}

		@Order(4000)
		@FormData(defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.IGNORE)
		public class PreviewHtmlField extends AbstractHtmlField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.preview.html");
			}

			@Override
			protected int getConfiguredGridH() {
				return 12;
			}

			@Override
			protected int getConfiguredGridW() {
				return 2;
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}

			@Override
			protected boolean getConfiguredScrollBarEnabled() {
				return Boolean.TRUE;
			}
		}

		@Order(5000)
		public class LastModificationDateField extends AbstractDateTimeField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.lastModificationDate");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return Boolean.FALSE;
			}
		}

		@Order(6000)
		public class DocumentMetadataBox extends AbstractTabBox {

			@Order(1000)
			public class LinkedRoleBox extends AbstractGroupBox {
				@Override
				protected String getConfiguredLabel() {
					return TEXTS.get("zc.document.linkedRole");
				}

				@Order(1000)
				public class LinkedRoleField extends AbstractTableField<LinkedRoleField.Table> {
					@Override
					protected String getConfiguredLabel() {
						return TEXTS.get("zc.document.linkedRole");
					}

					@Override
					protected boolean getConfiguredLabelVisible() {
						return Boolean.FALSE;
					}

					@Override
					protected int getConfiguredGridH() {
						return 6;
					}

					@Override
					protected boolean getConfiguredEnabled() {
						return Boolean.TRUE;
					}

					private AssignDocumentToRoleForm createFormFromRow(final ITableRow row) {
						final AssignDocumentToRoleForm form = new AssignDocumentToRoleForm();
						form.getDocumentIdField().setValue(
								(Long) row.getCellValue(this.getTable().getDocumentIdColumn().getColumnIndex()));
						form.getRoleIdField()
								.setValue((Long) row.getCellValue(this.getTable().getRoleIdColumn().getColumnIndex()));
						form.getStartDateField().setValue(
								(Date) row.getCellValue(this.getTable().getStartDateColumn().getColumnIndex()));
						return form;

					}

					@Override
					protected void execSaveDeletedRow(final ITableRow row) {
						this.createFormFromRow(row).delete();
					}

					@Override
					protected void execSaveInsertedRow(final ITableRow row) {
						this.createFormFromRow(row).create();
					}

					@Override
					protected void execSaveUpdatedRow(final ITableRow row) {
						this.createFormFromRow(row).update();
					}

					public class Table extends AbstractTable {

						@Order(1000)
						public class NewAssignDocumentToRoleMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.document.link.newAssignToRole");
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.Plus;
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection,
										TableMenuType.EmptySpace);
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, "n");
							}

							@Override
							protected void execAction() {
								final ITableRow row = Table.this.createRow();

								row.setCell(Table.this.getDocumentIdColumn(),
										Table.this.getDocumentIdColumn().getDefaulValue());
								row.setCell(Table.this.getStartDateColumn(),
										Table.this.getStartDateColumn().getDefaulValue());

								Table.this.addRow(row, Boolean.TRUE);
								LinkedRoleField.this.touch();
							}
						}

						@Order(3000)
						public class DeleteAssignDocumentToToleMenu extends AbstractMenu {
							@Override
							protected String getConfiguredText() {
								return TEXTS.get("zc.document.link.deleteAssignToRole");
							}

							@Override
							protected String getConfiguredIconId() {
								return Icons.Remove;
							}

							@Override
							protected String getConfiguredKeyStroke() {
								return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.DELETE);
							}

							@Override
							protected Set<? extends IMenuType> getConfiguredMenuTypes() {
								return CollectionUtility.hashSet(TableMenuType.SingleSelection);
							}

							@Override
							protected void execAction() {
								Table.this.getSelectedRow().delete();
								LinkedRoleField.this.touch();
							}
						}

						@Override
						protected void execDecorateCell(final Cell view, final ITableRow row, final IColumn<?> col) {
							if (this.getRoleIdColumn().equals(col)) {
								if (null != view.getValue() || "".equals(view.getValue())) {
									view.setEditable(Boolean.FALSE);
								}
							}
							if (this.getDocumentIdColumn().equals(col)) {
								if (null != view.getValue() || "".equals(view.getValue())) {
									view.setEditable(Boolean.FALSE);
								}
							}
							super.execDecorateCell(view, row, col);
						}

						public StartDateColumn getStartDateColumn() {
							return this.getColumnSet().getColumnByClass(StartDateColumn.class);
						}

						public DocumentIdColumn getDocumentIdColumn() {
							return this.getColumnSet().getColumnByClass(DocumentIdColumn.class);
						}

						public RoleIdColumn getRoleIdColumn() {
							return this.getColumnSet().getColumnByClass(RoleIdColumn.class);
						}

						@Order(1000)
						public class RoleIdColumn extends AbstractSmartColumn<Long> {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.document.link.roleId");
							}

							@Override
							protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
								return RoleAndSubscriptionLookupCall.class;
							}

							@Override
							protected Long execValidateValue(final ITableRow row, final Long rawValue) {
								if (Table.this.getRoleIdColumn().getValues().contains(rawValue)) {
									throw new VetoException(
											TEXTS.get("zc.document.link.roleAlreadyLinkedToThisDocument"));
								}

								return super.execValidateValue(row, rawValue);
							}

							@Override
							protected boolean getConfiguredEditable() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredMandatory() {
								return Boolean.TRUE;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}

						@Order(2000)
						public class StartDateColumn extends AbstractDateColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.document.link.startDate");
							}

							@Override
							protected boolean getConfiguredHasTime() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredEditable() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredMandatory() {
								return Boolean.TRUE;
							}

							public ICell getDefaulValue() {
								final Cell cell = new Cell();
								cell.setValue(new Date());
								return cell;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}

						@Order(3000)
						public class DocumentIdColumn extends AbstractLongColumn {
							@Override
							protected String getConfiguredHeaderText() {
								return TEXTS.get("zc.document.link.documentId");
							}

							@Override
							protected boolean getConfiguredVisible() {
								return Boolean.FALSE;
							}

							@Override
							protected boolean getConfiguredEditable() {
								return Boolean.TRUE;
							}

							@Override
							protected boolean getConfiguredMandatory() {
								return Boolean.TRUE;
							}

							public ICell getDefaulValue() {
								final Cell cell = new Cell();
								cell.setValue(DocumentForm.this.getDocumentIdField().getValue());
								return cell;
							}

							@Override
							protected int getConfiguredWidth() {
								return 100;
							}
						}
					}
				}
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
			@Override
			protected boolean execIsSaveNeeded() {
				return super.execIsSaveNeeded() || DocumentForm.this.getLinkedRoleField().isSaveNeeded();
			}
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IDocumentService service = BEANS.get(IDocumentService.class);
			DocumentFormData formData = new DocumentFormData();
			DocumentForm.this.exportFormData(formData);
			formData = service.load(formData);
			DocumentForm.this.importFormData(formData);

			DocumentForm.this.setEnabledPermission(new UpdateDocumentPermission());
		}

		@Override
		protected void execStore() {
			final IDocumentService service = BEANS.get(IDocumentService.class);
			final DocumentFormData formData = new DocumentFormData();
			DocumentForm.this.exportFormData(formData);
			service.store(formData);
			// handle links
			DocumentForm.this.getLinkedRoleField().doSave();
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IDocumentService service = BEANS.get(IDocumentService.class);
			DocumentFormData formData = new DocumentFormData();
			DocumentForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			DocumentForm.this.importFormData(formData);
			if (null == DocumentForm.this.getLastModificationDateField()) {
				DocumentForm.this.getLastModificationDateField().setValue(new Date());
			}
			DocumentForm.this.setEnabledPermission(new CreateDocumentPermission());
		}

		@Override
		protected void execStore() {
			final IDocumentService service = BEANS.get(IDocumentService.class);
			final DocumentFormData formData = new DocumentFormData();
			DocumentForm.this.exportFormData(formData);
			service.create(formData);
			// handle links
			DocumentForm.this.getLinkedRoleField().doSave();
		}
	}
}
