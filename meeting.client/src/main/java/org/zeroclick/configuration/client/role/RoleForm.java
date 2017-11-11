package org.zeroclick.configuration.client.role;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.role.RoleForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.role.RoleForm.MainBox.OkButton;
import org.zeroclick.configuration.client.role.RoleForm.MainBox.RoleNameField;
import org.zeroclick.configuration.client.role.RoleForm.MainBox.TypeField;
import org.zeroclick.configuration.shared.role.CreateRolePermission;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.configuration.shared.role.RoleTypeLookupCall;
import org.zeroclick.configuration.shared.role.UpdateRolePermission;

@FormData(value = RoleFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class RoleForm extends AbstractForm {

	private Long roleId;
	private Boolean deleteAction;

	@FormData
	public Long getRoleId() {
		return this.roleId;
	}

	@FormData
	public void setRoleId(final Long roleId) {
		this.roleId = roleId;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.role");
	}

	@Override
	public Object computeExclusiveKey() {
		return this.getRoleId();
	}

	@Override
	protected int getConfiguredDisplayHint() {
		return IForm.DISPLAY_HINT_VIEW;
	}

	public void startModify() {
		this.deleteAction = Boolean.FALSE;
		this.startInternalExclusive(new ModifyHandler());
	}

	public void startNew() {
		this.deleteAction = Boolean.FALSE;
		this.startInternal(new NewHandler());
	}

	public void startDelete() {
		this.deleteAction = Boolean.TRUE;
		this.startInternalExclusive(new DeleteHandler());
	}

	public CancelButton getCancelButton() {
		return this.getFieldByClass(CancelButton.class);
	}

	public MainBox getMainBox() {
		return this.getFieldByClass(MainBox.class);
	}

	public RoleNameField getRoleNameField() {
		return this.getFieldByClass(RoleNameField.class);
	}

	public TypeField getTypeField() {
		return this.getFieldByClass(TypeField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(2000)
		public class RoleNameField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("Name");
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
		public class TypeField extends AbstractProposalField<String> {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.role.type");
			}

			@Override
			protected boolean getConfiguredMandatory() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return RoleTypeLookupCall.class;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
			@Override
			protected boolean execIsSaveNeeded() {
				// to force form save even if no modification done in Fields
				if (RoleForm.this.deleteAction) {
					return Boolean.TRUE;
				} else {
					return Boolean.FALSE;
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
			final IRoleService service = BEANS.get(IRoleService.class);
			RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			formData.setRoleId(RoleForm.this.getRoleId());
			formData = service.load(formData);
			RoleForm.this.importFormData(formData);

			RoleForm.this.setEnabledPermission(new UpdateRolePermission());
		}

		@Override
		protected void execStore() {
			final IRoleService service = BEANS.get(IRoleService.class);
			final RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IRoleService service = BEANS.get(IRoleService.class);
			RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			RoleForm.this.importFormData(formData);

			this.getForm().setDisplayHint(IForm.DISPLAY_HINT_DIALOG);

			RoleForm.this.setEnabledPermission(new CreateRolePermission());
		}

		@Override
		protected void execStore() {
			final IRoleService service = BEANS.get(IRoleService.class);
			final RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			service.create(formData);
		}
	}

	public class DeleteHandler extends AbstractFormHandler {
		@Override
		protected void execLoad() {
			final IRoleService service = BEANS.get(IRoleService.class);
			RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			formData.setRoleId(RoleForm.this.getRoleId());
			formData = service.load(formData);
			RoleForm.this.importFormData(formData);

			RoleForm.this.setVisiblePermission(new UpdateRolePermission());
			RoleForm.this.setEnabledPermission(new UpdateRolePermission());
		}

		@Override
		protected void execStore() {
			final IRoleService service = BEANS.get(IRoleService.class);
			final RoleFormData formData = new RoleFormData();
			RoleForm.this.exportFormData(formData);
			formData.setRoleId(RoleForm.this.getRoleId());

			service.delete(formData);
		}
	}
}
