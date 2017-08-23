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
package org.zeroclick.common.text;

import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.user.IUserService;

/**
 * @author djer
 *
 */
public class TextsHelper extends TEXTS {
	private static final Logger LOG = LoggerFactory.getLogger(TextsHelper.class);

	/**
	 * see TEXTS.get(String)<br/>
	 * Wrap TEXTS.get to allow locale based on the specified user ID instead
	 * currentUser locale
	 *
	 * @param key
	 * @return
	 */
	public static String get(final Long userId, final String key) {
		return ScoutTexts.getInstance().getText(getUserLocal(userId), key);
	}

	/**
	 * see TEXTS.get(String, String...)<br/>
	 * Wrap TEXTS.get to allow locale based on the specified user ID instead
	 * currentUser locale
	 *
	 * @param key
	 * @param messageArguments
	 * @return
	 */
	public static String get(final Long userId, final String key, final String... messageArguments) {
		return ScoutTexts.getInstance().getText(getUserLocal(userId), key, messageArguments);
	}

	public static Locale getUserLocal(final Long userId) {
		Locale destLocal;
		String destLanguage = null;
		if (null == userId) {
			LOG.warn("Canno't get user language for user id NULL, using default language");
		} else {
			final IUserService userService = BEANS.get(IUserService.class);
			destLanguage = userService.getUserLanguage(userId);
		}

		if (null == destLanguage) {
			LOG.warn("No language defined for user Id : " + userId
					+ " using default (the current user language, assuming emmter and dest share same language)");
			destLocal = NlsLocale.get();
		} else {
			destLocal = new Locale(destLanguage);
		}

		return destLocal;
	}
}
