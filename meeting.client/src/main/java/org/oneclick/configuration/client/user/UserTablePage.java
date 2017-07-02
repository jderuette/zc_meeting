package org.oneclick.configuration.client.user;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.oneclick.configuration.client.user.UserTablePage.Table;
import org.oneclick.configuration.shared.user.CreateUserPermission;
import org.oneclick.configuration.shared.user.IUserService;
import org.oneclick.configuration.shared.user.UpdateUserPermission;
import org.oneclick.configuration.shared.user.UserTablePageData;

@Data(UserTablePageData.class)
public class UserTablePage extends AbstractPageWithTable<Table> {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(UserTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("Utilisateurs");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IUserService.class).getUserTableData(filter));
	}

	public class Table extends AbstractTable {

		@Order(1000)
		public class NewMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("New");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.EmptySpace, TableMenuType.SingleSelection,
						TableMenuType.MultiSelection);
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final Boolean isUserAllowed = ACCESS.check(new CreateUserPermission());
				this.setVisibleGranted(isUserAllowed);
				super.execOwnerValueChanged(newOwnerValue);
			}

			@Override
			protected void execAction() {
				final UserForm form = new UserForm(Boolean.TRUE);
				form.addFormListener(new UserFormListener());
				form.setEnabledPermission(new CreateUserPermission());
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
				return TEXTS.get("Edit");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execOwnerValueChanged(final Object newOwnerValue) {
				final Long currentUserId = Table.this.getUserIdColumn().getSelectedValue();
				final Boolean isUserAllowed = ACCESS.check(new UpdateUserPermission(currentUserId));
				this.setVisibleGranted(isUserAllowed);
				super.execOwnerValueChanged(newOwnerValue);
			}

			@Override
			protected void execAction() {
				final Long currentUserId = Table.this.getUserIdColumn().getSelectedValue();
				final UserForm form = new UserForm();
				form.getUserIdField().setValue(Table.this.getUserIdColumn().getSelectedValue());
				form.addFormListener(new UserFormListener());
				form.setEnabledPermission(new UpdateUserPermission(currentUserId));
				form.startModify();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, "e");
			}
		}

		public class UserFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent e) {
				if (FormEvent.TYPE_CLOSED == e.getType() && e.getForm().isFormStored()) {
					UserTablePage.this.reloadPage();
				}
			}
		}

		public EmailColumn getEmailColumn() {
			return this.getColumnSet().getColumnByClass(EmailColumn.class);
		}

		public LoginColumn getLoginColumn() {
			return this.getColumnSet().getColumnByClass(LoginColumn.class);
		}

		public TimeZoneColumn getTimeZoneColumn() {
			return this.getColumnSet().getColumnByClass(TimeZoneColumn.class);
		}

		public UserIdColumn getUserIdColumn() {
			return this.getColumnSet().getColumnByClass(UserIdColumn.class);
		}

		@Order(1000)
		public class UserIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("UserId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}
		}

		@Order(1500)
		public class LoginColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Login");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(2000)
		public class EmailColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Email");
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}
		}

		@Order(3000)
		public class TimeZoneColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.timezone");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

	}
}
