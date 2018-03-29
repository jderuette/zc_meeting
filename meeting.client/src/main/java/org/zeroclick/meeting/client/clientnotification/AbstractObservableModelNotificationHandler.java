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
package org.zeroclick.meeting.client.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.clientnotification.AbstractObservableNotificationHandler;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.notification.INotificationListener;

/**
 * Similar to AbstractObservableNotificationHandler but the handlers ares called
 * IN the Model (job) see (see {@link ModelJobs})
 *
 * @author djer
 *
 */
public abstract class AbstractObservableModelNotificationHandler<N extends Serializable>
		extends AbstractObservableNotificationHandler<N> {

	@Override
	protected void scheduleHandlingNotifications(final N notification, final EventListenerList list,
			final IClientSession session) {
		Jobs.schedule(new IRunnable() {

			@SuppressWarnings({ "unchecked", "PMD.SignatureDeclareThrowsException" })
			@Override
			public void run() throws Exception {
				if (list != null && list.getListenerCount(INotificationListener.class) > 0) {
					for (final INotificationListener<N> l : list
							.getListeners(INotificationListener.class)) {
						l.handleNotification(notification);
					}
				}
			}
		}, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("Handling Client Notification (in ModelJob)"));
	}
}
