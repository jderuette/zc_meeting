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

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;

/**
 * @author djer
 *
 */
@Order(800)
@IgnoreBean
public class JavaMailSender implements IMailSender {
	private static final Logger LOG = LoggerFactory.getLogger(JavaMailSender.class);
	Properties mailServerProperties;

	public JavaMailSender() {
		this.init();
	}

	private void init() {
		final Integer port = new EmailSmtpPortProperty().getValue();
		final Boolean authEnabled = new EmailSmtpAuthProperty().getValue();
		final Boolean tlsEnabled = new EmailSmtpTlsProperty().getValue();

		LOG.info("Initializing new JavaMailSender. port : " + port + ", authEnabled : " + authEnabled
				+ ", tlsEnabled : " + tlsEnabled);

		this.mailServerProperties = System.getProperties();
		this.mailServerProperties.put("mail.smtp.port", port);
		this.mailServerProperties.put("mail.smtp.auth", authEnabled);
		this.mailServerProperties.put("mail.smtp.starttls.enable", tlsEnabled);
	}

	private String addDefaultFooter() {
		return TEXTS.get("zc.common.email.footer", new ApplicationUrlProperty().getValue() + "/res/logo.png");
	}

	@Override
	public void sendEmail(final String recipientTo, final String subject, String messageBody,
			final Boolean includeDefaultFooter) throws MailException {
		if (includeDefaultFooter) {
			final StringBuffer sbMessageBodyWithFooter = new StringBuffer();
			sbMessageBodyWithFooter.append(messageBody);
			sbMessageBodyWithFooter.append(this.addDefaultFooter());
			messageBody = sbMessageBodyWithFooter.toString();
		}

		LOG.info("Sending mail to : " + recipientTo + " subject : " + subject + " BodySize : " + messageBody.length());
		final Session mailSession = Session.getDefaultInstance(this.mailServerProperties, null);
		MimeMessage message;
		try {
			message = this.generateEmail(mailSession, recipientTo, subject, messageBody);
			this.sendEmail(message, mailSession);
		} catch (final AddressException e) {
			throw new MailException("Error while sending mail", e);
		} catch (final MessagingException e) {
			throw new MailException("Error while sending mail", e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.common.email.MailSender#sendEmail(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void sendEmail(final String recipientTo, final String subject, final String messageBody)
			throws MailException {
		this.sendEmail(recipientTo, subject, messageBody, Boolean.TRUE);
	}

	private MimeMessage generateEmail(final Session mailSession, final String recipientTo, final String subject,
			final String messageBody) throws AddressException, MessagingException {
		final MimeMessage generateMailMessage = new MimeMessage(mailSession);
		generateMailMessage.setFrom(new EmailFromProperty().getValue());
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientTo));
		final String mailBCC = new EmailBccProperty().getValue();
		if (null != mailBCC) {
			generateMailMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(mailBCC));
		}
		generateMailMessage.setSubject(subject);
		generateMailMessage.setContent(messageBody, "text/html");

		return generateMailMessage;
	}

	private void sendEmail(final Message mailMessage, final Session mailSession) throws MessagingException {
		final Transport transport = mailSession.getTransport(new EmailTransportProtocolProperty().getValue());
		transport.connect(new EmailHostProperty().getValue(), new EmailUserProperty().getValue(),
				new EmailPasswordProperty().getValue());
		transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
		transport.close();
	}

}
