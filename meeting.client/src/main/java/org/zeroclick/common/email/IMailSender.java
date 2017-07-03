package org.zeroclick.common.email;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;

@ApplicationScoped
public interface IMailSender {

	/**
	 * Send email to recipientTo. By default include the default Footer
	 *
	 * @param recipientTo
	 * @param subject
	 * @param messageBody
	 * @throws MailException
	 */
	void sendEmail(final String recipientTo, final String subject, final String messageBody) throws MailException;

	void sendEmail(final String recipientTo, final String subject, final String messageBody,
			final Boolean includeDefaultFooter) throws MailException;

	/**
	 * Email SMTP port.
	 */
	public static class EmailSmtpPortProperty extends AbstractPositiveIntegerConfigProperty {

		@Override
		protected Integer getDefaultValue() {
			return 587;
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.smtp.port";
		}
	}

	/**
	 * Email SMTP use Auth.
	 */
	public static class EmailSmtpAuthProperty extends AbstractBooleanConfigProperty {

		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.smtp.auth";
		}
	}

	/**
	 * Email SMTP use TLS
	 */
	public static class EmailSmtpTlsProperty extends AbstractBooleanConfigProperty {

		@Override
		protected Boolean getDefaultValue() {
			return Boolean.TRUE;
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.smtp.starttls.enable";
		}
	}

	/**
	 * Email from email aderess.
	 */
	public static class EmailFromProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "admin@0click.org";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.from";
		}
	}

	/**
	 * Email from email aderess.
	 */
	public static class EmailFromNameProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "admin";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.from.name";
		}
	}

	/**
	 * Email from email aderess.
	 */
	public static class EmailBccProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "djer13@gmail.com";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.bcc";
		}
	}

	/**
	 * Email Transport protocol
	 */
	public static class EmailTransportProtocolProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "smtp";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.transport.protocol";
		}
	}

	/**
	 * Email host.
	 */
	public static class EmailHostProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "in.mailjet.com";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.host";
		}
	}

	/**
	 * Email auth user.
	 */
	public static class EmailUserProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "emailUser";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.auth.user";
		}
	}

	/**
	 * Email auth password.
	 */
	public static class EmailPasswordProperty extends AbstractStringConfigProperty {

		@Override
		protected String getDefaultValue() {
			return "emailpassword";
		}

		@Override
		public String getKey() {
			return "zeroclick.mail.auth.password";
		}
	}
}