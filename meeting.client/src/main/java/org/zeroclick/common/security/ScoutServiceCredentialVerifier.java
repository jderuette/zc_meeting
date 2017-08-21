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
package org.zeroclick.common.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractSubjectConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.configuration.shared.user.UserFormData.Password;

/**
 * Credential verifier against credentials configured in <i>Scout Service</i>
 * file. Password Hash is the same as #ConfigFileCredentialVerifier
 *
 * @author djer
 */
public class ScoutServiceCredentialVerifier implements ICredentialVerifier {

	private static final Logger LOG = LoggerFactory.getLogger(ScoutServiceCredentialVerifier.class);

	@Override
	public int verify(final String username, final char[] passwordPlainText) {
		if (StringUtility.isNullOrEmpty(username) || passwordPlainText == null || passwordPlainText.length == 0) {
			String logDisplayedPass = null;
			if (null != passwordPlainText) {
				logDisplayedPass = new String(passwordPlainText).substring(0, 4);
			}
			LOG.warn("Try to authenticate without username or password. Username : " + username
					+ ", (partial) password : " + logDisplayedPass);
			return AUTH_CREDENTIALS_REQUIRED;
		}

		IPassword password = null;
		UserFormData savedPassword = null;

		if (this.isTunnelServiceActive()) {
			final IFuture<UserFormData> passwordCaller = Jobs.schedule(new Callable<UserFormData>() {

				@Override
				@SuppressWarnings("PMD.SignatureDeclareThrowsException")
				public UserFormData call() throws Exception {
					final IUserService userService = BEANS.get(IUserService.class);
					final UserFormData userFormDataInput = new UserFormData();
					userFormDataInput.getLogin().setValue(username);
					userFormDataInput.getEmail().setValue(username);
					final UserFormData sserPassFormData = userService.getPassword(userFormDataInput);
					return sserPassFormData;
				}
			}, Jobs.newInput()
					.withRunContext(ClientRunContexts.copyCurrent().withSubject(this.retrievePasswordCheckerSubject())
							.withUserAgent(UserAgents.createDefault()).withSession(null, false))
					.withName(this.buildJobName()));

			savedPassword = passwordCaller.awaitDoneAndGet(30, TimeUnit.SECONDS);

			// final String savedPassword = userService.getPassword(username);
			if (null != savedPassword) {
				final Password passwordField = savedPassword.getPassword();
				password = this.createPassword(passwordField.getValue());
			} else {
				LOG.warn("No saved user with ID : " + username);
			}
		} else {
			LOG.warn("Cannot perform password check because ServiceTunel is not active (impacted user authentication : "
					+ username + ")");
		}

		final Boolean isPasswordMatch = password.isEqual(passwordPlainText);

		if (password == null || !isPasswordMatch) {
			LOG.warn("Bad password for user : " + username);
			return AUTH_FORBIDDEN;
		}
		LOG.info("User : " + username + " with correct password");
		return AUTH_OK;
	}

	private Boolean isTunnelServiceActive() {
		final IServiceTunnel tunnelService = BEANS.get(IServiceTunnel.class);
		final Boolean isTunelActive = tunnelService.isActive();
		return isTunelActive;
	}

	private Subject retrievePasswordCheckerSubject() {
		final PasswordCheckerSubjectProperty property = BEANS.get(PasswordCheckerSubjectProperty.class);
		final Subject subject = property.getValue();
		return subject;
	}

	private String buildJobName() {
		return ScoutServiceCredentialVerifier.class.getSimpleName();
	}

	/**
	 * Method invoked to create the {@link IPassword} for a password from
	 * database store.
	 */
	protected IPassword createPassword(final String password) {
		if (CONFIG.getPropertyValue(CredentialScoutServicePlainTextProperty.class)) {
			return new PlainTextPassword(password.toCharArray());
		} else {
			return new HashedPassword(password);
		}
	}

	public String generatePassword(final String plainText) {
		IPassword hashedPassword = null;
		String plainPassword = plainText;
		if (null == plainText || "".equals(plainText)) {
			LOG.warn("No PlainText provided for password hash, using default");
			plainPassword = "GR4p-gO8";
		}
		if (CONFIG.getPropertyValue(CredentialScoutServicePlainTextProperty.class)) {
			hashedPassword = new PlainTextPassword(plainPassword.toCharArray());
		} else {
			hashedPassword = new HashedPassword(plainPassword, SecurityUtility.createRandomBytes());
		}
		return hashedPassword.toString();
	}

	/**
	 * Represents a password from 'config.properties'. (from
	 * ConfigFileCredentialVerifier)
	 */
	public static interface IPassword {

		/**
		 * Returns whether the given password matches the password in
		 * 'config.properties'. (from ConfigFileCredentialVerifier)
		 */
		boolean isEqual(char[] password);
	}

	/**
	 * Represents a plain text password from 'config.properties'. (from
	 * ConfigFileCredentialVerifier)
	 */
	protected static class PlainTextPassword implements IPassword {

		private final char[] m_password;

		public PlainTextPassword(final char[] password) {
			this.m_password = password;
		}

		@Override
		public boolean isEqual(final char[] password) {
			return Arrays.equals(this.m_password, password);
		}
	}

	/**
	 * Represents a password from 'config.properties' with its salt and hash
	 * separated by a dot. (from ConfigFileCredentialVerifier)
	 */
	protected static class HashedPassword implements IPassword {

		protected static final Charset CHARSET = StandardCharsets.UTF_16;

		private final byte[] m_salt;
		private final byte[] m_hash;

		public HashedPassword(final String saltAndHash) {
			final String[] tokens = saltAndHash.split("\\.");
			Assertions.assertEqual(2, tokens.length,
					"Invalid password entry: salt and password-hash are to be separated with the dot (.).");
			Assertions.assertGreater(tokens[0].length(), 0, "Invalid password entry: 'salt' must not be empty");
			Assertions.assertGreater(tokens[1].length(), 0,
					"Invalid password entry: 'password-hash' must not be empty");
			this.m_salt = Base64Utility.decode(tokens[0]);
			this.m_hash = Base64Utility.decode(tokens[1]);
		}

		/**
		 * Create a secure password for storage
		 *
		 * @param password
		 *            : the plain password.
		 * @param salt
		 *            : the (user specific) salt. You may Use :
		 *            SecurityUtility.createRandomBytes()
		 */
		public HashedPassword(final String password, final byte[] salt) {
			this.m_salt = salt;
			this.m_hash = this.createPasswordHash(password.toCharArray(), salt);
		}

		@Override
		public boolean isEqual(final char[] password) {
			return Arrays.equals(this.m_hash, this.createPasswordHash(password, this.m_salt));
		}

		protected byte[] createPasswordHash(final char[] password, final byte[] salt) {
			return SecurityUtility.hash(this.toBytes(password), salt);
		}

		protected byte[] toBytes(final char[] password) {
			try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				final OutputStreamWriter writer = new OutputStreamWriter(outputStream, CHARSET);
				writer.write(password);
				writer.flush();
				return outputStream.toByteArray();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			return String.format("%s.%s", Base64Utility.encode(this.m_salt), Base64Utility.encode(this.m_hash));
		}
	}

	/**
	 * Technical {@link Subject} used to authenticate password checker requests.
	 */
	public static class PasswordCheckerSubjectProperty extends AbstractSubjectConfigProperty {

		@Override
		public String getKey() {
			return "password.checker.authenticator";
		}

		@Override
		protected Subject getDefaultValue() {
			return this.convertToSubject("0");
		}
	}

	/**
	 * Indicates whether plain-text or hashed passwords are stored in
	 * 'config.properties'. By default, this verifier expects hashed passwords.
	 */
	public static class CredentialScoutServicePlainTextProperty extends AbstractBooleanConfigProperty {

		@Override
		public String getKey() {
			return "scout.auth.service.credentials.plaintext";
		}

		@Override
		protected Boolean getDefaultValue() {
			return Boolean.FALSE;
		}
	}

}
