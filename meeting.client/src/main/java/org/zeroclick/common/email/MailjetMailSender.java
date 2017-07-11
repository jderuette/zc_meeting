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
package org.zeroclick.common.email;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.shared.TEXTS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Contact;
import com.mailjet.client.resource.Email;

/**
 * @author djer
 *
 */
@Order(10000)
public class MailjetMailSender implements IMailSender {

	private static final Logger LOG = LoggerFactory.getLogger(MailjetMailSender.class);
	private MailjetClient client;

	public MailjetMailSender() {
		this.init();
	}

	private void init() {

		final String user = this.getUser();
		final String password = this.getPass();
		LOG.info("Initializing new MailjetMailSender. user : " + user + ", password (partial) : "
				+ password.substring(0, 5));
		this.client = new MailjetClient(user, password);
		this.client.setDebug(new MailjetLogLevelProperty().getValue());
	}

	private String getUser() {
		final String user = new EmailUserProperty().getValue();
		return user;
	}

	private String getPass() {
		final String pass = new EmailPasswordProperty().getValue();
		return pass;
	}

	private String addDefaultFooter() {
		return TEXTS.get("zc.common.email.footer", new ApplicationUrlProperty().getValue() + "/res/logo.png");
	}

	@Override
	public void sendEmail(final String recipientTo, String subject, String messageBody,
			final Boolean includeDefaultFooter) throws MailException {
		if (includeDefaultFooter) {
			final StringBuffer sbMessageBodyWithFooter = new StringBuffer();
			sbMessageBodyWithFooter.append(messageBody);
			sbMessageBodyWithFooter.append(this.addDefaultFooter());
			messageBody = sbMessageBodyWithFooter.toString();
		}

		final String currentEnv = System.getenv("ZEROCLICK_ENV");
		if (null != currentEnv) {
			subject = "[" + currentEnv + "] " + subject;
		}

		LOG.info("Sending mail to : " + recipientTo + " subject : " + subject + " BodySize : " + messageBody.length());

		if (LOG.isDebugEnabled()) {
			LOG.debug(messageBody);
		}

		final MailjetRequest email = new MailjetRequest(Email.resource)
				.property(Email.FROMEMAIL, new EmailFromProperty().getValue())
				.property(Email.FROMNAME, new EmailFromNameProperty().getValue()).property(Email.SUBJECT, subject)
				.property(Email.TEXTPART, "No message text use an html compliant mail reader")
				.property(Email.HTMLPART, messageBody)
				.property(Email.RECIPIENTS, new JSONArray().put(new JSONObject().put(Contact.EMAIL, recipientTo)))
				.property(Email.BCC, new EmailBccProperty().getValue());
		try {
			// trigger the API call
			final MailjetResponse response = this.client.post(email);
			// Read the response data and status
			if (response.getStatus() != 200) {
				LOG.error("Mail (probably) NOT sent with status : " + response.getStatus() + " and data : "
						+ response.getData());
				throw new MailException("Error while sending mail");
			} else {
				LOG.info("Mail sent with status : " + response.getStatus() + " and data : " + response.getData());
			}
		} catch (final MailjetException e) {
			LOG.error("Error while sending mail." + e);
			throw new MailException("Error while sending mail", e);
		} catch (final MailjetSocketTimeoutException e) {
			LOG.error("Error while sending mail." + e);
			throw new MailException("Error while sending mail", e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.common.email.IMailSender#sendEmail(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void sendEmail(final String recipientTo, final String subject, final String messageBody)
			throws MailException {
		this.sendEmail(recipientTo, subject, messageBody, Boolean.TRUE);
	}

	/**
	 * Mailjet specific log level (see MailjetCLient#setDebug)
	 */
	public static class MailjetLogLevelProperty extends AbstractPositiveIntegerConfigProperty {

		@Override
		protected Integer getDefaultValue() {
			return MailjetClient.NO_DEBUG;
		}

		@Override
		public String getKey() {
			return "mailjet.log.level";
		}
	}

}
