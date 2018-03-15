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
package org.zeroclick.comon.text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

/**
 * @author djer
 *
 */
@SuppressWarnings("PMD.MethodNamingConventions")
public class StringHelperTest {

	@Test
	public void testIsValidEmail_valid() throws ParseException {
		final StringHelper stringHelper = BEANS.get(StringHelper.class);

		final String email = "bob.name@provider.com";
		final Boolean isValid = stringHelper.isValidEmail(email);

		assertTrue("Email : " + email + " should be valid address email", isValid);
	}

	@Test
	public void testIsValidEmail_noProvider() throws ParseException {
		final StringHelper stringHelper = BEANS.get(StringHelper.class);

		final String email = "bob.name@";
		final Boolean isValid = stringHelper.isValidEmail(email);

		assertFalse("Email : " + email + " should NOT be valid address email", isValid);
	}

	@Test
	public void testIsValidEmail_noAtSeparator() throws ParseException {
		final StringHelper stringHelper = BEANS.get(StringHelper.class);

		final String email = "bob.nameprovider.com";
		final Boolean isValid = stringHelper.isValidEmail(email);

		assertFalse("Email : " + email + " should NOT be valid address email", isValid);
	}
}
