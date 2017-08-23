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
package org.zeroclick.ui.form.fields.languagefield;

import java.util.Locale;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.configuration.shared.user.LanguageLookupCall;

/**
 * @author djer
 *
 */
public class LanguageField extends AbstractSmartField<String> {
	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.user.language");
	}

	@Override
	protected boolean getConfiguredMandatory() {
		return Boolean.TRUE;
	}

	@Override
	protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
		return LanguageLookupCall.class;
	}

	/**
	 * Should be call after filed populated, to set a default language if none
	 * is set by user
	 */
	public void setDefaultLanguage() {
		final Locale currentLocale = NlsLocale.get();
		if (null == this.getValue() && null != currentLocale && !currentLocale.getLanguage().isEmpty()) {
			this.setValue(currentLocale.getLanguage());
		}
	}
}
