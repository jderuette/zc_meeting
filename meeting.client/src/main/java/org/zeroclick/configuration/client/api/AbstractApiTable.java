/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.configuration.client.api;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractNumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.api.ApiCreatedNotification;
import org.zeroclick.configuration.shared.api.ApiDeletedNotification;
import org.zeroclick.configuration.shared.provider.ProviderCodeType;
import org.zeroclick.meeting.shared.calendar.ApiFormData;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.ReadApiPermission;
import org.zeroclick.meeting.shared.calendar.UpdateApiPermission;
import org.zeroclick.ui.action.menu.AbstractDeleteMenu;
import org.zeroclick.ui.action.menu.AbstractEditMenu;

/**
 * @author djer
 *
 */
public abstract class AbstractApiTable extends AbstractTable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractApiTable.class);

	FormListener formListener;
	private boolean displayAllUsers;

	private INotificationListener<ApiCreatedNotification> apiCreatedListener;
	private INotificationListener<ApiDeletedNotification> apiDeletedListener;

	public boolean isDisplayAllUsers() {
		return this.displayAllUsers;
	}

	public void setDisplayAllUsers(final boolean displayAllUsers) {
		this.displayAllUsers = displayAllUsers;
	}

	public FormListener getConfiguredFormListener() {
		return null;
	}

	protected boolean getConfiguredDisplayAllUsers() {
		return false;
	}

	protected boolean getConfiguredAutoLoad() {
		return true;
	}

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.api.list.title");
	}

	@Override
	protected boolean getConfiguredHeaderEnabled() {
		return this.isApiAdmin();
	}

	private boolean isApiAdmin() {
		final int currentUserLevel = ACCESS.getLevel(new ReadApiPermission((Long) null));
		return currentUserLevel == ReadApiPermission.LEVEL_ALL;
	}

	@Override
	protected void execInitTable() {
		this.formListener = this.getConfiguredFormListener();
		this.displayAllUsers = this.getConfiguredDisplayAllUsers();

		if (this.displayAllUsers) {
			this.getAccessTokenColumn().setVisible(true);
			this.getExpirationTimeMillisecondsColumn().setVisible(true);
			this.getRefreshTokenColumn().setVisible(true);
			this.getUserIdColumn().setVisible(true);

		} else {
			// avoid registering handler for "all user" in admin
			final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
					.get(ApiCreatedNotificationHandler.class);
			apiCreatedNotificationHandler.addListener(this.createApiCreatedListener());

			final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
					.get(ApiDeletedNotificationHandler.class);
			apiDeletedNotificationHandler.addListener(this.createApiDeletedListener());

			final EditApiMenu editMenu = this.getMenuByClass(EditApiMenu.class);
			if (null != editMenu) {
				editMenu.setVisible(false);
			}
			final DeleteApiMenu deleteMenu = this.getMenuByClass(DeleteApiMenu.class);
			if (null != deleteMenu) {
				deleteMenu.setVisible(false);
			}
		}

		if (this.getConfiguredAutoLoad()) {
			this.loadData();
		}
	}

	@Override
	protected void execDisposeTable() {
		if (!this.displayAllUsers) {
			final ApiCreatedNotificationHandler apiCreatedNotificationHandler = BEANS
					.get(ApiCreatedNotificationHandler.class);
			apiCreatedNotificationHandler.removeListener(this.apiCreatedListener);

			final ApiDeletedNotificationHandler apiDeletedNotificationHandler = BEANS
					.get(ApiDeletedNotificationHandler.class);
			apiDeletedNotificationHandler.removeListener(this.apiDeletedListener);
		}
	}

	protected void loadData() {
		this.importFromTableBeanData(BEANS.get(IApiService.class).getApiTableData(this.displayAllUsers));
	}

	private INotificationListener<ApiCreatedNotification> createApiCreatedListener() {
		this.apiCreatedListener = new INotificationListener<ApiCreatedNotification>() {
			@Override
			public void handleNotification(final ApiCreatedNotification notification) {
				try {
					final ApiFormData eventForm = notification.getFormData();
					LOG.debug("Created Api prepare to modify apiTable List: " + eventForm.getUserId());
					AbstractApiTable.this.loadData();
				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiCreatedListener;
	}

	private INotificationListener<ApiDeletedNotification> createApiDeletedListener() {
		this.apiDeletedListener = new INotificationListener<ApiDeletedNotification>() {
			@Override
			public void handleNotification(final ApiDeletedNotification notification) {
				try {
					final ApiFormData eventForm = notification.getFormData();
					LOG.debug("Deleted Api prepare to modify apiTable List : " + eventForm.getUserId());
					AbstractApiTable.this.loadData();
				} catch (final RuntimeException e) {
					LOG.error("Could not handle new api. (" + this.getClass().getName() + ")", e);
				}
			}
		};

		return this.apiDeletedListener;
	}

	@Order(1000)
	public class EditApiMenu extends AbstractEditMenu {
		@Override
		protected boolean getConfiguredEnabled() {
			return ACCESS.getLevel(new UpdateApiPermission((Long) null)) > UpdateApiPermission.LEVEL_OWN;
		}

		@Override
		protected void execAction() {
			final ApiForm form = new ApiForm();
			final Long apiCredentailId = AbstractApiTable.this.getApiCredentialIdColumn().getSelectedValue();
			form.setApiCredentialId(apiCredentailId);
			if (null != AbstractApiTable.this.formListener) {
				form.addFormListener(AbstractApiTable.this.formListener);
			}
			// start the form using its modify handler
			form.startModify();
		}
	}

	@Order(2000)
	public class DeleteApiMenu extends AbstractDeleteMenu {
		@Override
		protected boolean getConfiguredEnabled() {
			return ACCESS.getLevel(new DeleteApiPermission((Long) null)) > DeleteApiPermission.LEVEL_OWN;
		}

		@Override
		protected void execAction() {
			final ApiForm form = new ApiForm();
			final Long apiCredentailId = AbstractApiTable.this.getApiCredentialIdColumn().getSelectedValue();
			form.setApiCredentialId(apiCredentailId);
			if (null != AbstractApiTable.this.formListener) {
				form.addFormListener(AbstractApiTable.this.formListener);
			}
			// start the form using its modify handler
			form.startDelete();
		}
	}

	public AccountEmailColumn getAccountEmailColumn() {
		return this.getColumnSet().getColumnByClass(AccountEmailColumn.class);
	}

	public ProviderColumn getProviderColumn() {
		return this.getColumnSet().getColumnByClass(ProviderColumn.class);
	}

	public AccessTokenColumn getAccessTokenColumn() {
		return this.getColumnSet().getColumnByClass(AccessTokenColumn.class);
	}

	public UserIdColumn getUserIdColumn() {
		return this.getColumnSet().getColumnByClass(UserIdColumn.class);
	}

	public RefreshTokenColumn getRefreshTokenColumn() {
		return this.getColumnSet().getColumnByClass(RefreshTokenColumn.class);
	}

	public ExpirationTimeMillisecondsColumn getExpirationTimeMillisecondsColumn() {
		return this.getColumnSet().getColumnByClass(ExpirationTimeMillisecondsColumn.class);
	}

	public ApiCredentialIdColumn getApiCredentialIdColumn() {
		return this.getColumnSet().getColumnByClass(ApiCredentialIdColumn.class);
	}

	@Order(1000)
	public class ApiCredentialIdColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.common.id");
		}

		@Override
		protected Long getConfiguredMinValue() {
			return 0L;
		}

		@Override
		protected boolean getConfiguredDisplayable() {
			return false;
		}

		@Override
		protected boolean getConfiguredPrimaryKey() {
			return true;
		}
	}

	@Order(2000)
	public class ProviderColumn extends AbstractSmartColumn<Long> {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.api.provider");
		}

		@Override
		protected boolean getConfiguredSummary() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 128;
		}

		@Override
		protected Class<? extends ICodeType<Long, Long>> getConfiguredCodeType() {
			return ProviderCodeType.class;
		}
	}

	@Order(3000)
	public class AccessTokenColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.api.accessToken");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}

		@Override
		protected int getConfiguredWidth() {
			return 256;
		}
	}

	@Order(4000)
	public class ExpirationTimeMillisecondsColumn extends AbstractLongColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.api.expirationTimeMilliseconds");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}

		@Override
		protected int getConfiguredWidth() {
			return 128;
		}
	}

	@Order(5000)
	public class RefreshTokenColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.api.refreshToken");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}

		@Override
		protected int getConfiguredWidth() {
			return 256;
		}
	}

	@Order(6000)
	public class UserIdColumn extends AbstractNumberColumn<Long> {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.user.userId");
		}

		@Override
		protected boolean getConfiguredVisible() {
			return false;
		}

		@Override
		protected boolean getConfiguredSummary() {
			return Boolean.TRUE;
		}

		@Override
		protected int getConfiguredWidth() {
			return 128;
		}

		@Override
		protected Long getConfiguredMinValue() {
			return 0l;
		}

		@Override
		protected Long getConfiguredMaxValue() {
			return Long.MAX_VALUE;
		}
	}

	@Order(7000)
	public class AccountEmailColumn extends AbstractStringColumn {
		@Override
		protected String getConfiguredHeaderText() {
			return TEXTS.get("zc.api.accountEmail");
		}

		@Override
		protected int getConfiguredWidth() {
			return 256;
		}
	}

}
