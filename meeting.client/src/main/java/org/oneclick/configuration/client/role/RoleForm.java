package org.oneclick.configuration.client.role;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.oneclick.configuration.client.role.RoleForm.MainBox.CancelButton;
import org.oneclick.configuration.client.role.RoleForm.MainBox.RoleNameField;
import org.oneclick.configuration.client.role.RoleForm.MainBox.OkButton;
import org.oneclick.configuration.shared.role.CreateRolePermission;
import org.oneclick.configuration.shared.role.IRoleService;
import org.oneclick.configuration.shared.role.RoleFormData;
import org.oneclick.configuration.shared.role.UpdateRolePermission;

@FormData(value = RoleFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class RoleForm extends AbstractForm {

	private Integer roleId;

	@FormData
	public Integer getRoleId() {
		return this.roleId;
	}

	@FormData
	public void setRoleId(final Integer roleId) {
		this.roleId = roleId;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("Role");
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

	public RoleNameField getRoleNameField() {
		return this.getFieldByClass(RoleNameField.class);
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
			protected int getConfiguredMaxLength() {
				return 128;
			}
		}

		@Order(100000)
		public class OkButton extends AbstractOkButton {
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
}
