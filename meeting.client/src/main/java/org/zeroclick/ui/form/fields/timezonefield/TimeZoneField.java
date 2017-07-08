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
package org.zeroclick.ui.form.fields.timezonefield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.meeting.client.common.ZoneIdLookupCall;

/**
 * @author djer
 *
 */
public class TimeZoneField extends AbstractSmartField<String> {
	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.timezone");
	}

	@Override
	protected boolean getConfiguredMandatory() {
		return Boolean.TRUE;
	}

	@Override
	protected String getConfiguredTooltipText() {
		return TEXTS.get("zc.timezone.tooltipText");
	}

	@Override
	protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
		return ZoneIdLookupCall.class;
	}
}
