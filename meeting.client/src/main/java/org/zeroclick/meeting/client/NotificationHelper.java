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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.status.IStatus;
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

	public void addProcessingNotification(final String messageKey) {
		this.addDesktopNotification(IStatus.INFO, DURATION_SHORT, messageKey);
	}

	public void addProcessingNotification(final String messageKey, final String... messageArguments) {
		this.addDesktopNotification(IStatus.INFO, DURATION_SHORT, messageKey, messageArguments);
	}

	public void addProccessedNotification(final String messageKey) {
		this.addDesktopNotification(IStatus.OK, DURATION_MEDIUM, messageKey);
	}

	public void addProccessedNotification(final String messageKey, final String... messageArguments) {
		this.addDesktopNotification(IStatus.OK, DURATION_MEDIUM, messageKey, messageArguments);
	}

	private void addDesktopNotification(final Integer severity, final Long duration, final String messageKey) {
		this.addDesktopNotification(severity, duration, messageKey, (String[]) null);
	}

	private void addDesktopNotification(final Integer severity, final Long duration, final String messageKey,
			final String... messageArguments) {
		final Desktop desktop = (Desktop) ClientSession.get().getDesktop();

		if (null != desktop) {
			if (null == messageArguments) {
				desktop.addNotification(severity, duration, Boolean.TRUE, messageKey);
			} else {
				desktop.addNotification(severity, duration, Boolean.TRUE, messageKey, messageArguments);
			}
		} else {
			LOG.warn("Unable to publish Notification because desktop is null, severity : " + severity
					+ ", messageKey : " + messageKey);
		}
	}
}
