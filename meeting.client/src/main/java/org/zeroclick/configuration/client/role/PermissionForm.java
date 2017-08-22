package org.zeroclick.configuration.client.role;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.client.role.PermissionForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.role.PermissionForm.MainBox.OkButton;
import org.zeroclick.configuration.client.role.PermissionForm.MainBox.PermissionField;
import org.zeroclick.configuration.shared.role.CreatePermissionPermission;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.PermissionFormData;
import org.zeroclick.configuration.shared.role.UpdatePermissionPermission;

@FormData(value = PermissionFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class PermissionForm extends AbstractForm {

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
		return TEXTS.get("zc.user.permissions");
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

	public PermissionField getPermissionField() {
		return this.getFieldByClass(PermissionField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
	}

	@Order(1000)
	public class MainBox extends AbstractGroupBox {

		@Order(1000)
		public class PermissionField extends AbstractStringField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.permission");
			}

			@Override
			protected int getConfiguredMaxLength() {
				return 256;
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
			final IAppPermissionService service = BEANS.get(IAppPermissionService.class);
			PermissionFormData formData = new PermissionFormData();
			PermissionForm.this.exportFormData(formData);
			formData = service.load(formData);
			PermissionForm.this.importFormData(formData);

			PermissionForm.this.setEnabledPermission(new UpdatePermissionPermission());
		}

		@Override
		protected void execStore() {
			final IAppPermissionService service = BEANS.get(IAppPermissionService.class);
			final PermissionFormData formData = new PermissionFormData();
			PermissionForm.this.exportFormData(formData);
			service.store(formData);
		}
	}

	public class NewHandler extends AbstractFormHandler {

		@Override
		protected void execLoad() {
			final IAppPermissionService service = BEANS.get(IAppPermissionService.class);
			PermissionFormData formData = new PermissionFormData();
			PermissionForm.this.exportFormData(formData);
			formData = service.prepareCreate(formData);
			PermissionForm.this.importFormData(formData);

			PermissionForm.this.setEnabledPermission(new CreatePermissionPermission());
		}

		@Override
		protected void execStore() {
			final IAppPermissionService service = BEANS.get(IAppPermissionService.class);
			final PermissionFormData formData = new PermissionFormData();
			PermissionForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
