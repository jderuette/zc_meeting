package org.zeroclick.configuration.client.role;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.client.role.RoleTablePage.Table;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.IRolePermissionService;
import org.zeroclick.configuration.shared.role.PermissionTablePageData;

@Data(PermissionTablePageData.class)
public class PermissionTablePage extends AbstractPageWithTable<Table> {

	private Long roleId;

	public Long getRoleId() {
		return this.roleId;
	}

	public void setRoleId(final Long roleId) {
		this.roleId = roleId;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.permissions");
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return true;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		if (this.getRoleId() == null) {
			this.importTableData(BEANS.get(IAppPermissionService.class).getpermissions());
		} else {
			this.importPageData(BEANS.get(IAppPermissionService.class).getpermissionsByRole(this.getRoleId()));
		}
	}

	public class Table extends AbstractTable {

		@Order(1000)
		public class AddToRoleMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.addToRole");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}

			@Override
			protected void execAction() {
				final AssignToRoleForm form = new AssignToRoleForm();
				form.setPermission(Table.this.getPermissionNameColumn().getSelectedValues());
				form.addFormListener(new AssignToRoleListener());
				form.startNew();
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				if (PermissionTablePage.this.getRoleId() == null) {
					this.setVisible(Boolean.TRUE);
				} else {
					this.setVisible(Boolean.FALSE);
				}
			}
		}

		@Order(2000)
		public class RemoveToRoleMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.removeToRole");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}

			@Override
			protected void execAction() {
				BEANS.get(IRolePermissionService.class).remove(PermissionTablePage.this.getRoleId(),
						Table.this.getPermissionNameColumn().getSelectedValues());
				PermissionTablePage.this.reloadPage();
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				if (PermissionTablePage.this.getRoleId() == null) {
					this.setVisible(Boolean.FALSE);
				} else {
					this.setVisible(Boolean.TRUE);
				}
			}
		}

		public class AssignToRoleListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
					PermissionTablePage.this.reloadPage();
				}
			}
		}

		public LevelColumn getLevelColumn() {
			return this.getColumnSet().getColumnByClass(LevelColumn.class);
		}

		public PermissionNameColumn getPermissionNameColumn() {
			return this.getColumnSet().getColumnByClass(PermissionNameColumn.class);
		}

		@Order(1000)
		public class PermissionNameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Name");
			}

			@Override
			protected int getConfiguredWidth() {
				return 450;
			}
		}

		@Order(2000)
		public class LevelColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.level");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}

		}
	}
}
