package org.zeroclick.configuration.client.api;

import java.util.Set;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractNumberColumn;
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
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.client.api.ApiTablePage.ApisTable;
import org.zeroclick.configuration.shared.api.ApiTablePageData;
import org.zeroclick.meeting.client.common.ProviderLookupCall;
import org.zeroclick.meeting.shared.Icons;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.IApiService;
import org.zeroclick.meeting.shared.calendar.UpdateApiPermission;

@Data(ApiTablePageData.class)
public class ApiTablePage extends AbstractPageWithTable<ApisTable> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.api.apisManagement");
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
				return TEXTS.get("zc.common.edit");
			}

			@Override
			protected Set<? extends IMenuType> getConfiguredMenuTypes() {
				return CollectionUtility.hashSet(TableMenuType.SingleSelection);
			}

			@Override
			protected boolean getConfiguredEnabled() {
				return ACCESS.getLevel(new UpdateApiPermission((Long) null)) > UpdateApiPermission.LEVEL_OWN;
			}

			@Override
			protected void execAction() {
				final ApiForm form = new ApiForm();
				final Long apiCredentailId = ApisTable.this.getApiCredentialIdColumn().getSelectedValue();
				form.setApiCredentialId(apiCredentailId);
				form.addFormListener(new ApiFormListener());
				// start the form using its modify handler
				form.startModify();
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
			protected boolean getConfiguredEnabled() {
				return ACCESS.getLevel(new DeleteApiPermission((Long) null)) > DeleteApiPermission.LEVEL_OWN;
			}

			@Override
			protected void execAction() {
				final ApiForm form = new ApiForm();
				final Long apiCredentailId = ApisTable.this.getApiCredentialIdColumn().getSelectedValue();
				form.setApiCredentialId(apiCredentailId);
				form.addFormListener(new ApiFormListener());
				// start the form using its modify handler
				form.startDelete();
			}

			@Override
			protected String getConfiguredKeyStroke() {
				return combineKeyStrokes(IKeyStroke.SHIFT, IKeyStroke.DELETE);
			}

			@Override
			public String getConfiguredIconId() {
				return Icons.ExclamationMark;
			}
		}

		private class ApiFormListener implements FormListener {

			@Override
			public void formChanged(final FormEvent event) {
				// reload page to reflect new/changed data after saving any
				// changes
				if (FormEvent.TYPE_CLOSED == event.getType() && event.getForm().isFormStored()) {
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
		public class ProviderColumn extends AbstractSmartColumn<Integer> {
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
			protected Class<? extends ILookupCall<Integer>> getConfiguredLookupCall() {
				return ProviderLookupCall.class;
			}
		}

		@Order(3000)
		public class AccessTokenColumn extends AbstractStringColumn {
			@Override
			protected String getConfiguredHeaderText() {
				return TEXTS.get("zc.api.accessToken");
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
				// TODO Auto-generated method stub
				return Long.MAX_VALUE;
			}
		}
	}
}
