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
package org.zeroclick.meeting.client.event;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.PlannerMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TabBoxMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;
import org.zeroclick.ui.action.menu.AbstractValidateMenu;

/**
 * @author djer
 *
 */
public abstract class AbstractAcceptEventInvolvementMenu extends AbstractValidateMenu {
	@Override
	protected String getConfiguredText() {
		return TEXTS.get("zc.meeting.Accept");
	}

	abstract protected Long getEventId();

	@Override
	protected void execOwnerValueChanged(final Object newOwnerValue) {
		if (null != newOwnerValue) {
			this.refresh();
		}
	}

	public void refresh() {
		final EventWorkflow eventWorkflow = BEANS.get(EventWorkflow.class);
		this.setVisible(eventWorkflow.canAccept(this.getEventId()));
	}

	@Override
	protected void execAction() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);

		final ChooseDateForm form = new ChooseDateForm();
		form.getEventIdField().setValue(this.getEventId());
		form.setForUserId(acsHelper.getZeroClickUserIdOfCurrentSubject());

		form.startAccept();
	}

	@Override
	protected Set<? extends IMenuType> getConfiguredMenuTypes() {
		return CollectionUtility.<IMenuType>hashSet(TableMenuType.SingleSelection, TreeMenuType.SingleSelection,
				ValueFieldMenuType.NotNull, CalendarMenuType.CalendarComponent, PlannerMenuType.Activity,
				TabBoxMenuType.Header);
	}

}
