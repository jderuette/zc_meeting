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
package org.zeroclick.meeting.client;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.notification.DesktopNotification;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class NotificationHelper {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationHelper.class);

	private static final Long DURATION_SHORT = 5000L;
	private static final Long DURATION_MEDIUM = 10000L;
	private static final Long DURATION_LONG = 15000L;
	private static final Long DURATION_VERY_LONG = 20000L;

	public void addProcessingNotification(final String messageKey) {
		this.addNotification(IStatus.INFO, DURATION_SHORT, messageKey);
	}

	public void addProcessingNotification(final String messageKey, final String... messageArguments) {
		this.addNotification(IStatus.INFO, DURATION_SHORT, messageKey, messageArguments);
	}

	public void addProccessedNotification(final String messageKey) {
		this.addNotification(IStatus.OK, DURATION_MEDIUM, messageKey);
	}

	public void addProccessedNotification(final String messageKey, final String... messageArguments) {
		this.addNotification(IStatus.OK, DURATION_MEDIUM, messageKey, messageArguments);
	}

	public void addWarningNotification(final String messageKey) {
		this.addNotification(IStatus.WARNING, DURATION_LONG, messageKey);
	}

	public void addWarningNotification(final String messageKey, final String... messageArguments) {
		this.addNotification(IStatus.WARNING, DURATION_LONG, messageKey, messageArguments);
	}

	public void addErrorNotification(final String messageKey) {
		this.addNotification(IStatus.ERROR, DURATION_VERY_LONG, messageKey);
	}

	public void addErrorNotification(final String messageKey, final String... messageArguments) {
		this.addNotification(IStatus.ERROR, DURATION_VERY_LONG, messageKey, messageArguments);
	}

	private void addNotification(final Integer severity, final Long duration, final String messageKey) {
		this.addNotification(severity, duration, messageKey, (String[]) null);
	}

	public void addNotification(final Integer severity, final Long duration, final Boolean closable,
			final String messageKey) {
		final IStatus status = new Status(TEXTS.get(messageKey), severity);
		this.addNotification(status, duration, closable);
	}

	public void addNotification(final Integer severity, final Long duration, final Boolean closable,
			final String messageKey, final String... messageArguments) {
		final IStatus status = new Status(TEXTS.get(messageKey, messageArguments), severity);
		this.addNotification(status, duration, closable);
	}

	private void addNotification(final IStatus status, final Long duration, final Boolean closable) {
		Jobs.schedule(new IRunnable() {

			@Override
			@SuppressWarnings("PMD.SignatureDeclareThrowsException")
			public void run() throws Exception {
				if (LOG.isDebugEnabled()) {
					LOG.debug(new StringBuilder().append("Adding desktop notification : ").append(status.getMessage())
							.append("(").append(status.getClass()).append(")").toString());
				}
				final DesktopNotification desktopNotification = new DesktopNotification(status, duration, closable);
				ClientSession.get().getDesktop().addNotification(desktopNotification);
			}
		}, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("adding desktop notification (in ModelJob)"));
	}

	private void addNotification(final Integer severity, final Long duration, final String messageKey,
			final String... messageArguments) {

		if (null == messageArguments) {
			this.addNotification(severity, duration, Boolean.TRUE, messageKey);
		} else {
			this.addNotification(severity, duration, Boolean.TRUE, messageKey, messageArguments);
		}
	}
}
