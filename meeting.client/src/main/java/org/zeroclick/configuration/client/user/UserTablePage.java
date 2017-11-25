package org.zeroclick.configuration.client.user;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.user.UserTablePage.Table;
import org.zeroclick.configuration.shared.user.CreateUserPermission;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.LanguageLookupCall;
import org.zeroclick.configuration.shared.user.UpdateUserPermission;
import org.zeroclick.configuration.shared.user.UserTablePageData;
import org.zeroclick.ui.action.menu.AbstractEditMenu;
import org.zeroclick.ui.action.menu.AbstractNewMenu;
import org.zeroclick.ui.form.columns.zoneddatecolumn.AbstractZonedDateColumn;

@Data(UserTablePageData.class)
public class UserTablePage extends AbstractPageWithTable<Table> {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(UserTablePage.class);

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.user.utilisateurs");
	}

	@Override
	public boolean isLeaf() {
		return Boolean.TRUE;
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IUserService.class).getUserTableData(filter));
	}

	public class Table extends AbstractTable {

		@Order(1000)
		public class NewUserMenu extends AbstractNewMenu {
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
		}

		@Order(2000)
		public class EditUserMenu extends AbstractEditMenu {
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
		}

		public class UserFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
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

		public InvitedByColumn getInvitedByColumn() {
			return this.getColumnSet().getColumnByClass(InvitedByColumn.class);
		}

		public LanguageColumn getLanguageColumn() {
			return this.getColumnSet().getColumnByClass(LanguageColumn.class);
		}

		public LastLoginColumn getLastLoginColumn() {
			return this.getColumnSet().getColumnByClass(LastLoginColumn.class);
		}

		public NbOrganizedEventWaitingColumn getNbOrganizedEventWaitingColumn() {
			return this.getColumnSet().getColumnByClass(NbOrganizedEventWaitingColumn.class);
		}

		public NbInvitedEventWaitingColumn getNbInvitedEventWaitingColumn() {
			return this.getColumnSet().getColumnByClass(NbInvitedEventWaitingColumn.class);
		}

		public NbProcessedEventColumn getNbProcessedEventColumn() {
			return this.getColumnSet().getColumnByClass(NbProcessedEventColumn.class);
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
				return TEXTS.get("zc.user.userId");
			}

			@Override
			protected boolean getConfiguredSummary() {
				return Boolean.TRUE;
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
				return TEXTS.get("zc.user.login");
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
				return TEXTS.get("zc.common.email");
			}

			@Override
			protected boolean getConfiguredSummary() {
				return Boolean.TRUE;
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

		@Order(4000)
		public class InvitedByColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.invitedBy");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(5000)
		public class LanguageColumn extends AbstractSmartColumn<String> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.language");
			}

			@Override
			protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
				return LanguageLookupCall.class;
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(6000)
		public class LastLoginColumn extends AbstractZonedDateColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.lastLogin");
			}

			@Override
			protected int getConfiguredWidth() {
				return 100;
			}
		}

		@Order(7000)
		public class NbProcessedEventColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.nbProcessedEvent");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(8000)
		public class NbOrganizedEventWaitingColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.nbOrganizedEventWainting");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

		@Order(9000)
		public class NbInvitedEventWaitingColumn extends AbstractIntegerColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.user.nbInvitedEventWaiting");
			}

			@Override
			protected int getConfiguredWidth() {
				return 50;
			}
		}

	}
}
