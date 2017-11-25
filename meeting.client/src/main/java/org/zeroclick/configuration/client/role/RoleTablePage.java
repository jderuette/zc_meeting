package org.zeroclick.configuration.client.role;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractProposalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.role.RoleTablePage.Table;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleTablePageData;
import org.zeroclick.configuration.shared.role.RoleTypeLookupCall;
import org.zeroclick.configuration.shared.role.UpdateRolePermission;
import org.zeroclick.ui.action.menu.AbstractDeleteMenu;
import org.zeroclick.ui.action.menu.AbstractEditMenu;
import org.zeroclick.ui.action.menu.AbstractNewMenu;

@Data(RoleTablePageData.class)
public class RoleTablePage extends AbstractPageWithTable<Table> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.roles");
	}

	@Override
	protected IPage<?> execCreateChildPage(final ITableRow row) {
		final PermissionTablePage page = new PermissionTablePage();
		page.setRoleId(this.getTable().getRoleIdColumn().getValue(row.getRowIndex()));
		page.setLeaf(Boolean.TRUE);
		return page;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IRoleService.class).getRoleTableData(filter));
	}

	public class Table extends AbstractTable {

		@Order(1000)
		public class NewRoleMenu extends AbstractNewMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.new");
			}

			@Override
			protected void execAction() {
				final RoleForm form = new RoleForm();
				form.addFormListener(new RoleFormListener());
				form.startNew();
			}
		}

		@Order(2000)
		public class EditRoleMenu extends AbstractEditMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.edit");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
			}

			@Override
			protected void execAction() {
				final RoleForm form = new RoleForm();
				form.setRoleId(Table.this.getRoleIdColumn().getSelectedValue());
				form.addFormListener(new RoleFormListener());
				form.startModify();
			}
		}

		@Order(3000)
		public class DeleteMenu extends AbstractDeleteMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.delete");
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return ACCESS.check(new UpdateRolePermission());
			}

			@Override
			protected void execAction() {
				final RoleForm form = new RoleForm();
				form.setRoleId(Table.this.getRoleIdColumn().getSelectedValue());
				form.addFormListener(new RoleFormListener());
				form.startDelete();
			}
		}

		private class RoleFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
					RoleTablePage.this.reloadPage();
				}
			}
		}

		public TypeColumn getTypeColumn() {
			return this.getColumnSet().getColumnByClass(TypeColumn.class);
		}

		public RoleIdColumn getRoleIdColumn() {
			return this.getColumnSet().getColumnByClass(RoleIdColumn.class);
		}

		public RoleNameColumn getRoleNameColumn() {
			return this.getColumnSet().getColumnByClass(RoleNameColumn.class);
		}

		@Order(5)
		public class RoleIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.common.id");
			}

			@Override
			protected boolean getConfiguredVisible() {
				return Boolean.TRUE;
			}

			@Override
			protected Long getConfiguredMinValue() {
				return 0l;
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(2000)
		public class TypeColumn extends AbstractProposalColumn<String> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.role.type");
			}

			@Override
			protected boolean getConfiguredSummary() {
				return Boolean.TRUE;
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return RoleTypeLookupCall.class;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(3000)
		public class RoleNameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.role.name");
			}

			@Override
			protected boolean getConfiguredSummary() {
				return Boolean.TRUE;
			}

			@Override
			protected void execDecorateCell(final Cell cell, final ITableRow row) {
				final String translated = TEXTS.getWithFallback(cell.getText(), cell.getText());
				cell.setText(translated);
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

	}
}
