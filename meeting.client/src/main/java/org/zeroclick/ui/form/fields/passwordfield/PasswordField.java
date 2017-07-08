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
package org.zeroclick.ui.form.fields.passwordfield;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @author djer
 *
 */
public class PasswordField extends AbstractStringField {
	private static final String NUMBER_PATTERN = "^.*[0-9]+.*$";
	private static final String LETTER_PATTERN = "^.*[A-Za-z]+.*";

	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("Password");
	}

	@Override
	protected int getConfiguredMaxLength() {
		return 128;
	}

	@Override
	protected boolean getConfiguredMandatory() {
		return Boolean.FALSE;
	}

	@Override
	protected boolean getConfiguredInputMasked() {
		return Boolean.TRUE;
	}

	@Override
	protected String execValidateValue(final String rawValue) {
		if (null != rawValue) {
			if (rawValue.length() < 6) {
				throw new VetoException(TEXTS.get("zc.user.passwordTooShort"));
			}
			if (!Pattern.matches(NUMBER_PATTERN, rawValue)) {
				throw new VetoException(TEXTS.get("zc.user.passwordMustContainNumber"));
			}
			if (!Pattern.matches(LETTER_PATTERN, rawValue)) {
				throw new VetoException(TEXTS.get("zc.user.passwordMustContainLetter"));
			}
		}
		return rawValue;
	}
}
