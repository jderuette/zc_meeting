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
package org.zeroclick.meeting.client.calendar;

import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.meeting.client.api.google.GoogleApiHelper;
import org.zeroclick.meeting.client.api.microsoft.MicrosoftApiHelper;

/**
 * @author djer
 *
 */
public class AddCalendarField extends AbstractHtmlField {
	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.meeting.addCalendar");
	}

	@Override
	protected String getConfiguredTooltipText() {
		return TEXTS.get("zc.meeting.addCalendar.tooltips");
	}

	@Override
	protected int getConfiguredGridH() {
		return 3;
	}

	@Override
	protected void execInitField() {
		super.execInitField();
		this.setHtmlEnabled(Boolean.TRUE);
		final StringBuilder sbValue = new StringBuilder(100);
		sbValue.append("<ul>").append(BEANS.get(GoogleApiHelper.class).getAuthorisationLinksAsLi())
				.append(BEANS.get(MicrosoftApiHelper.class).getAuthorisationLinksAsLi()).append("</ul>");

		this.setValue(sbValue.toString());
	}
}
