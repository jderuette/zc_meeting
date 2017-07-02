package org.oneclick.configuration.client.api;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.oneclick.configuration.client.api.ApiTablePage.ApisTable;
import org.oneclick.configuration.shared.api.ApiTablePageData;
import org.oneclick.meeting.client.common.ProviderLookupCall;
import org.oneclick.meeting.shared.Icons;
import org.oneclick.meeting.shared.calendar.DeleteApiPermission;
import org.oneclick.meeting.shared.calendar.IApiService;
import org.oneclick.meeting.shared.calendar.UpdateApiPermission;

@Data(ApiTablePageData.class)
public class ApiTablePage extends AbstractPageWithTable<ApisTable> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("ApisManagement");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		this.importPageData(BEANS.get(IApiService.class).getApiTableData(filter));
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return true;
	}

	public class ApisTable extends AbstractTable {

		@Order(1000)
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
			protected void execAction() {
				final ApiForm form = new ApiForm();
				final Long apiCredentailId = ApisTable.this.getApiCredentialIdColumn().getSelectedValue();
				form.setApiCredentialId(apiCredentailId);
				form.addFormListener(new ApiFormListener());
				// start the form using its modify handler
				form.startModify();
				this.setEnabledPermission(new UpdateApiPermission(apiCredentailId));
			}
		}

		@Order(2000)
		public class DeleteMenu extends AbstractMenu {
			@Override
			protected String getConfiguredText() {
				return TEXTS.get("DeleteMenu");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected void execAction() {
				final ApiForm form = new ApiForm();
				final Long apiCredentailId = ApisTable.this.getApiCredentialIdColumn().getSelectedValue();
				form.setApiCredentialId(apiCredentailId);
				form.addFormListener(new ApiFormListener());
				// start the form using its modify handler
				form.startDelete();

				this.setEnabledPermission(new DeleteApiPermission(apiCredentailId));
			}

			@Override
			public String getConfiguredIconId() {
				return Icons.ExclamationMark;
			}
		}

		private class ApiFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent e) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == e.getType() && e.getForm().isFormStored()) {
					ApiTablePage.this.reloadPage();
				}
			}
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

		@Order(1)
		public class ApiCredentialIdColumn extends AbstractLongColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Id");
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
		public class ProviderColumn extends AbstractSmartColumn<Integer> {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("Provider");
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}

			@Override
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return ProviderLookupCall.class;
			}
		}

		@Order(3000)
		public class AccessTokenColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("AccessToken");
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
				return TEXTS.get("ExpirationTimeMilliseconds");
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
				return TEXTS.get("RefreshToken");
			}

			@Override
			protected int getConfiguredWidth() {
				return 256;
			}
		}

		@Order(6000)
		public class UserIdColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("UserId");
			}

			@Override
			protected int getConfiguredWidth() {
				return 128;
			}
		}
	}
}
