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
package org.zeroclick.ui.form.columns.event;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.shared.venue.VenueLookupCall;

/**
 * @author djer
 *
 */
public abstract class AbstractVenueColumn extends AbstractSmartColumn<String> {
	@Override
	protected String getConfiguredHeaderText() {
		return TEXTS.get("zc.meeting.venue");
	}

	@Override
	protected int getConfiguredWidth() {
		return 100;
	}

	@Override
	protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
		return VenueLookupCall.class;
	}
}
