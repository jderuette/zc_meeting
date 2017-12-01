package org.zeroclick.meeting.client.event;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.zeroclick.configuration.shared.importer.ValueSeparatorCodeType;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.AppendToExistingField;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.CancelButton;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.ImportedEmailPreviewField;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.OkButton;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.RawDataField;
import org.zeroclick.meeting.client.event.ImportEmailsForm.MainBox.ValueSeparatorField;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.event.ImportEmailsFormData;

@FormData(value = ImportEmailsFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ImportEmailsForm extends AbstractForm {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.meeting.event.importEmails");
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	@Override
	protected boolean getConfiguredAskIfNeedSave() {
		return Boolean.FALSE;
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public RawDataField getRawDataField() {
		return this.getFieldByClass(RawDataField.class);
	}

	public ValueSeparatorField getValueSeparatorField() {
		return this.getFieldByClass(ValueSeparatorField.class);
	}

	public ImportedEmailPreviewField getImportedEmailPreviewField() {
		return this.getFieldByClass(ImportedEmailPreviewField.class);
	}

	public AppendToExistingField getAppendToExistingField() {
		return this.getFieldByClass(AppendToExistingField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	private void fillPreview() {
		final String rawData = this.getRawDataField().getValue();
		final String separator = this.getValueSeparatorField().getValue();
		final ValueSeparatorCodeType valueSeparatorCodes = new ValueSeparatorCodeType();
		final ICode<String> code = valueSeparatorCodes.getCodeByText(separator);
		String separatorValue;
		if (null == code) {
			separatorValue = separator;
		} else {
			separatorValue = code.getId();
		}

		final Boolean appendToExisting = this.getAppendToExistingField().getValue();
		List<String> emails = null;

		if (null != rawData && !rawData.isEmpty()) {
			emails = Arrays.asList(rawData.split("\\s*" + separatorValue + "\\s*"));
		}

		if (null != emails && !emails.isEmpty()) {
			if (null == appendToExisting || !appendToExisting) {
				this.getImportedEmailPreviewField().getTable().deleteAllRows();
			}
			this.getImportedEmailPreviewField().getTable().addRowsByArray(emails);
		} else {
			this.getImportedEmailPreviewField().getTable().deleteAllRows();
		}

	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class ValueSeparatorField extends AbstractProposalField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.importEmails.valueSeparator");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ICodeType<?, String>> getConfiguredCodeType() {
				return ValueSeparatorCodeType.class;
			}

			@Override
			protected void execChangedValue() {
				ImportEmailsForm.this.fillPreview();
			}
		}

		@Order(2000)
		public class RawDataField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.importEmails.rawData");
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
				return 5;
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 16000;
			}

			@Override
			protected void execChangedValue() {
				ImportEmailsForm.this.fillPreview();
			}
		}

		@Order(3000)
		public class AppendToExistingField extends AbstractBooleanField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.importEmails.appendToExisting");
			}
		}

		@Order(4000)
		public class ImportedEmailPreviewField extends AbstractTableField<ImportedEmailPreviewField.Table> {
			public class Table extends AbstractTable {

				@Override
				protected boolean getConfiguredHeaderEnabled() {
					return Boolean.FALSE;
				}

				@Order(1000)
				public class ClearMenu extends AbstractMenu {
					@Override
					protected String getConfiguredText() {
						return TEXTS.get("zc.meeting.event.importEmails.clear");
					}

					@Override
					protected String getConfiguredIconId() {
						return Icons.Circle;
					}

					@Override
					protected Set<? extends IMenuType> getConfiguredMenuTypes() {
						return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
								TableMenuType.EmptySpace);
					}

					@Override
					protected void execAction() {
						ImportEmailsForm.this.getImportedEmailPreviewField().getTable().deleteAllRows();
					}
				}

				public EmailsColumn getEmailsColumn() {
					return this.getColumnSet().getColumnByClass(EmailsColumn.class);
				}

				@Order(1000)
				public class EmailsColumn extends AbstractStringColumn {
					@Override
					protected String getConfiguredHeaderText() {
						return TEXTS.get("zc.meeting.attendeeEmail");
					}

					@Override
					protected int getConfiguredWidth() {
						return 200;
					}
				}
			}

			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.meeting.event.importEmails.preview");
			}

			@Override
			protected int getConfiguredGridH() {
				return 10;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
		}

		@Override
		protected void execStore() {
		}
	}
}
