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
package org.zeroclick.configuration.client.user.fields;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.shared.role.SubscriptionLookupCall;

/**
 * @author djer
 *
 */
public class SubscriptionBox extends AbstractSmartField<Long> {
	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.user.role.subscription");
	}

	@Override
	protected boolean getConfiguredMandatory() {
		return Boolean.TRUE;
	}

	@Override
	protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
		return SubscriptionLookupCall.class;
	}
}
