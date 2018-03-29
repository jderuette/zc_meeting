package org.zeroclick.configuration.client.api;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.client.api.ApiTablePage.ApisTable;
import org.zeroclick.configuration.shared.api.ApiTablePageData;

@Data(ApiTablePageData.class)
public class ApiTablePage extends AbstractPageWithTable<ApisTable> {

	@Override
	protected String getConfiguredTitle() {
		return TEXTS.get("zc.api.apisManagement");
	}

	@Override
	protected void execLoadData(final SearchFilter filter) {
		// manual load, to allow correct handle of refresh (F5)
		this.getTable().loadData();
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return true;
	}

	public class ApisTable extends AbstractApiTable {

		@Override
		public FormListener getConfiguredFormListener() {
			return new ApiFormListener();
		}

		@Override
		protected boolean getConfiguredDisplayAllUsers() {
			return true;
		}

		@Override
		protected boolean getConfiguredAutoLoad() {
			return false;
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
}
