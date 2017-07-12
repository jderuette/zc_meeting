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
package org.zeroclick.ui.form.fields.loginfield;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.zeroclick.configuration.shared.user.IUserService;

/**
 * @author djer
 *
 */
public class LoginField extends AbstractStringField {

	private static final String LOGIN_PATTERN = "^[A-Za-z0-9\\-_]*$";
	private String initialValue = null;

	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("Login");
	}

	@Override
	protected int getConfiguredMaxLength() {
		return 64;
	}

	@Override
	protected String getConfiguredTooltipText() {
		return TEXTS.get("zc.user.Login.tooltipText");
	}

	@Override
	protected String execValidateValue(final String rawValue) {
		final IUserService userService = BEANS.get(IUserService.class);
		Boolean modified = Boolean.FALSE;

		if (null == this.initialValue) {
			this.initialValue = rawValue;
		}

		if (null != rawValue && !rawValue.equals(this.initialValue)) {
			modified = Boolean.TRUE;
		}

		if (null != rawValue && modified) {
			if (rawValue.length() < 4) {
				throw new VetoException(TEXTS.get("zc.login.tooShort"));
			}
			if (!Pattern.matches(LOGIN_PATTERN, rawValue)) {
				throw new VetoException(TEXTS.get("zc.login.invalid"));
			}

			if (userService.isLoginAlreadyUsed(rawValue)) {
				throw new VetoException(TEXTS.get("zc.login.invalid"));
			}
		}
		return null == rawValue ? null : rawValue.toLowerCase();
	}

}
