package org.zeroclick.common.document.link;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm.MainBox.CancelButton;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm.MainBox.DocumentIdField;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm.MainBox.OkButton;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm.MainBox.RoleIdField;
import org.zeroclick.common.document.link.AssignDocumentToRoleForm.MainBox.StartDateField;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleAndSubscriptionLookupCall;
import org.zeroclick.configuration.shared.role.UpdateRolePermission;

@FormData(value = AssignDocumentToRoleFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class AssignDocumentToRoleForm extends AbstractForm {

	private Boolean forDeletion = Boolean.FALSE;

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.document.link.assignToRole");
	}

	public void startModify() {
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.startInternal(new NewHandler());
	}

	public void startDelete() {
		this.startInternal(new DeleteHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public RoleIdField getRoleIdField() {
		return this.getFieldByClass(RoleIdField.class);
	}

	public StartDateField getStartDateField() {
		return this.getFieldByClass(StartDateField.class);
	}

	public DocumentIdField getDocumentIdField() {
		return this.getFieldByClass(DocumentIdField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	public void delete() {
		final IRoleService service = BEANS.get(IRoleService.class);
		final AssignDocumentToRoleFormData formData = new AssignDocumentToRoleFormData();
		AssignDocumentToRoleForm.this.exportFormData(formData);
		service.delete(formData);
	}

	public void create() {
		final IRoleService service = BEANS.get(IRoleService.class);
		final AssignDocumentToRoleFormData formData = new AssignDocumentToRoleFormData();
		AssignDocumentToRoleForm.this.exportFormData(formData);
		service.create(formData);
	}

	public void update() {
		final IRoleService service = BEANS.get(IRoleService.class);
		final AssignDocumentToRoleFormData formData = new AssignDocumentToRoleFormData();
		AssignDocumentToRoleForm.this.exportFormData(formData);
		service.store(formData);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class RoleIdField extends AbstractSmartField<Long> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role");
			}

			@Override
			protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
				return RoleAndSubscriptionLookupCall.class;
			}
		}

		@Order(2000)
		public class StartDateField extends AbstractDateField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.link.startDate");
			}

			@Override
			protected boolean getConfiguredHasTime() {
				return Boolean.TRUE;
			}
		}

		@Order(3000)
		public class DocumentIdField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.document.link.documentId");
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

		@Order(100000)
		public class OkButton extends AbstractOkButton {

			@Override
			protected boolean execIsSaveNeeded() {
				if (AssignDocumentToRoleForm.this.forDeletion) {
					return Boolean.TRUE;
				} else {
					return super.execIsSaveNeeded();
				}
			}
		}

		@Order(101000)
		public class CancelButton extends AbstractCancelButton {
		}
	}

	public class ModifyHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			AssignDocumentToRoleForm.this.setEnabledPermission(new UpdateRolePermission());
			AssignDocumentToRoleForm.this.setSubTitle(TEXTS.get("zc.document.link.editAssignToRole"));
		}

		@Override
		protected void execStore() {
			AssignDocumentToRoleForm.this.update();
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			AssignDocumentToRoleForm.this.setEnabledPermission(new UpdateRolePermission());
			AssignDocumentToRoleForm.this.setSubTitle(TEXTS.get("zc.document.link.newAssignToRole"));
		}

		@Override
		protected void execStore() {
			AssignDocumentToRoleForm.this.create();
		}
	}

	public class DeleteHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			AssignDocumentToRoleForm.this.setEnabledPermission(new UpdateRolePermission());
			AssignDocumentToRoleForm.this.getStartDateField().setEnabled(Boolean.FALSE);
			AssignDocumentToRoleForm.this.getRoleIdField().setEnabled(Boolean.FALSE);
			AssignDocumentToRoleForm.this.setSubTitle(TEXTS.get("zc.document.link.deleteAssignToRole"));
			// fake modification to enable save
			AssignDocumentToRoleForm.this.forDeletion = Boolean.TRUE;
		}

		@Override
		protected void execStore() {
			AssignDocumentToRoleForm.this.delete();
		}
	}

}
