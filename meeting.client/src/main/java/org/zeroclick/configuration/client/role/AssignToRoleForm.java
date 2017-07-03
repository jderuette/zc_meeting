package org.zeroclick.configuration.client.role;

import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.role.AssignToRoleForm.MainBox.CancelButton;
import org.zeroclick.configuration.client.role.AssignToRoleForm.MainBox.LevelField;
import org.zeroclick.configuration.client.role.AssignToRoleForm.MainBox.OkButton;
import org.zeroclick.configuration.client.role.AssignToRoleForm.MainBox.RoleIdField;
import org.zeroclick.configuration.shared.role.AssignToRoleFormData;
import org.zeroclick.configuration.shared.role.CreateAssignToRolePermission;
import org.zeroclick.configuration.shared.role.IRolePermissionService;
import org.zeroclick.configuration.shared.role.RoleLookupCall;

@FormData(value = AssignToRoleFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class AssignToRoleForm extends AbstractForm {

	private List<String> permission;

	@FormData
	public List<String> getPermission() {
		return this.permission;
	}

	@FormData
	public void setPermission(final List<String> permission) {
		this.permission = permission;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.assignToRole");
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

	public RoleIdField getRoleIdField() {
		return this.getFieldByClass(RoleIdField.class);
	}

	public LevelField getLevelField() {
		return this.getFieldByClass(LevelField.class);
	}

	public OkButton getOkButton() {
		return this.getFieldByClass(OkButton.class);
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
				return RoleLookupCall.class;
			}
		}

		@Order(2000)
		public class LevelField extends AbstractLongField {
			@Override
			protected String getConfiguredLabel() {
				return TEXTS.get("zc.user.level");
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0L;
			}

			@Override
			protected Long getConfiguredMaxValue() {
				return 100L;
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
			AssignToRoleForm.this.setEnabledPermission(new CreateAssignToRolePermission());
		}

		@Override
		protected void execStore() {
			final IRolePermissionService service = BEANS.get(IRolePermissionService.class);
			final AssignToRoleFormData formData = new AssignToRoleFormData();
			AssignToRoleForm.this.exportFormData(formData);
			service.create(formData);
		}
	}
}
