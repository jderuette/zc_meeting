package org.zeroclick.configuration.client.role;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
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
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.role.RoleTablePage.Table;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleTablePageData;
import org.zeroclick.configuration.shared.role.RoleTypeLookupCall;
import org.zeroclick.meeting.shared.Icons;

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
		public class NewMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.new");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection,
						TableMenuType.EmptySpace);
			}

			@Override
			protected void execAction() {
				final RoleForm form = new RoleForm();
				form.addFormListener(new RoleFormListener());
				form.startNew();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "n");
			}
		}

		@Order(2000)
		public class EditMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("zc.user.role.edit");
			}

			@Override
			protected String getConfiguredIconId() {
				return Icons.Pencil;
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

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "e");
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
		public class RoleNameColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.role.name");
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

		@Order(3000)
		public class TypeColumn extends AbstractProposalColumn<String> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.role.type");
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

	}
}
