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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.shared.TEXTS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationEnvProperty;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;

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
		this.client = new MailjetClient(user, password, new ClientOptions("v3.1"));
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
	public void sendEmail(final String recipientTo, final String subject, final String messageBody,
			final Boolean includeDefaultFooter) throws MailException {
		String messageBodyWithFooter = messageBody;
		if (includeDefaultFooter) {
			final StringBuilder builder = new StringBuilder(250);
			builder.append(messageBody).append(this.addDefaultFooter());
			messageBodyWithFooter = builder.toString();
		}

		final StringBuilder subjectBuilder = new StringBuilder();

		final String currentEnvDisplay = new ApplicationEnvProperty().displayAsText();
		if (!currentEnvDisplay.isEmpty()) {
			subjectBuilder.append(currentEnvDisplay);
			subjectBuilder.append(' ');
		}

		subjectBuilder.append(subject);

		final String subjectWithEnv = subjectBuilder.toString();

		LOG.info("Sending mail to : " + recipientTo + ", subject : " + subjectWithEnv + ", BodySize : "
				+ messageBodyWithFooter.length());

		if (LOG.isDebugEnabled()) {
			LOG.debug(messageBodyWithFooter);
		}

		final JSONObject message = new JSONObject();
		message.put(Emailv31.Message.FROM,
				new JSONObject().put(Emailv31.Message.EMAIL, new EmailFromProperty().getValue())
						.put(Emailv31.Message.NAME, new EmailFromNameProperty().getValue()))
				.put(Emailv31.Message.SUBJECT, subjectWithEnv)
				.put(Emailv31.Message.TEXTPART, "No message text use an html compliant mail reader")
				.put(Emailv31.Message.HTMLPART, messageBodyWithFooter).put(Emailv31.Message.TO,
						new JSONArray().put(new JSONObject().put(Emailv31.Message.EMAIL, recipientTo)));

		String mailBCC;
		final IAppParamsService paramService = BEANS.get(IAppParamsService.class);
		mailBCC = paramService.getValue(IAppParamsService.APP_PARAM_KEY_SUPPORT_BCC);

		if (null == mailBCC || "".equals(mailBCC)) {
			// try with the "old" parameter
			mailBCC = new EmailBccProperty().getValue();
		}

		if (null != mailBCC && !"".equals(mailBCC)) {
			message.put(Emailv31.Message.BCC,
					new JSONArray().put(new JSONObject().put("Email", mailBCC).put("Name", "0ClickSupport")));
		}

		final MailjetRequest email = new MailjetRequest(Emailv31.resource).property(Emailv31.MESSAGES,
				new JSONArray().put(message));

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
