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
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.comon.text.UserHelper;
import org.zeroclick.configuration.shared.user.LanguageLookupCall;
import org.zeroclick.meeting.client.ClientSession;
import org.zeroclick.meeting.shared.Icons;

/**
 * @author djer
 *
 */
public class LanguageField extends AbstractSmartField<String> {
	private Boolean valueChanged = Boolean.FALSE;

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

	public Boolean getValueChanged() {
		return this.valueChanged;
	}

	protected void setValueChanged(final Boolean valueChanged) {
		this.valueChanged = valueChanged;
	}

	@Override
	protected void execChangedValue() {
		super.execChangedValue();
		final Locale currentLocal = NlsLocale.get();
		if (null != this.getValue() && !this.getValue().equals(currentLocal.getLanguage())) {
			this.setValueChanged(Boolean.TRUE);
		}
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

	public void askToReloadSession() {
		final UserHelper userHelper = BEANS.get(UserHelper.class);
		final int userChooseReload = MessageBoxes.createYesNo()
				.withHeader(TextsHelper.get(userHelper.getCurrentUserId(), "zc.user.language.needReload.title"))
				.withBody(TextsHelper.get(userHelper.getCurrentUserId(), "zc.user.language.needReload"))
				.withIconId(Icons.Info).withSeverity(IStatus.INFO).show();
		if (IMessageBox.YES_OPTION == userChooseReload) {
			ClientSession.get().stop();
		}
	}
}
