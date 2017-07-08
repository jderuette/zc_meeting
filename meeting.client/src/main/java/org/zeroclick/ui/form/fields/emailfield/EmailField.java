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
package org.zeroclick.ui.form.fields.emailfield;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * @author djer
 *
 */
public class EmailField extends AbstractStringField {
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Override
	protected String getConfiguredLabel() {
		return TEXTS.get("zc.common.email");
	}

	@Override
	protected int getConfiguredMaxLength() {
		return 128;
	}

	@Override
	protected boolean getConfiguredMandatory() {
		return Boolean.TRUE;
	}

	@Override
	protected String execValidateValue(final String rawValue) {
		if (rawValue != null && !Pattern.matches(EMAIL_PATTERN, rawValue)) {
			throw new VetoException(TEXTS.get("zc.common.badEmailAddress"));
		}
		return null == rawValue ? null : rawValue.toLowerCase();
	}
}
