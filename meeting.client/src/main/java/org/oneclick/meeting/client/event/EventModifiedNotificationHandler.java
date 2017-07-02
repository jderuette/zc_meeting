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
package org.oneclick.meeting.client.event;

import org.oneclick.meeting.client.clientnotification.AbstractObservableModelNotificationHandler;
import org.oneclick.meeting.shared.event.EventModifiedNotification;

/**
 * @author djer
 *
 */
public class EventModifiedNotificationHandler
		extends AbstractObservableModelNotificationHandler<EventModifiedNotification> {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(EventModifiedNotificationHandler.class);

	// private static final Logger LOG =
	// LoggerFactory.getLogger(EventNotificationHandler.class);
	//
	// private final EventListenerList listenerList = new EventListenerList();
	//
	// public void addEventServiceListener(final EventServiceListener listener)
	// {
	// this.listenerList.add(EventServiceListener.class, listener);
	// }
	//
	// public void removeEventServiceListener(final EventServiceListener
	// listener) {
	// this.listenerList.remove(EventServiceListener.class, listener);
	// }
	//
	// private void fireEventSeviceEvent(final EventServiceEvent e) {
	// final EventListener[] a =
	// this.listenerList.getListeners(EventServiceListener.class);
	// if (a != null) {
	// for (int i = 0; i < a.length; i++) {
	// final EventServiceListener listener = (EventServiceListener) a[i];
	// switch (e.getType()) {
	// case EventServiceEvent.TYPE_CHANGED:
	// listener.eventChanged(e);
	// break;
	// case EventServiceEvent.TYPE_NEW:
	// listener.eventCreated(e);
	// default:
	// LOG.warn("Cannot fire event EventServiceEvent because type (" +
	// e.getType() + ") is unknow");
	// break;
	// }
	// }
	// }
	// }
	//
	// private void fireEventChanged() {
	// final EventServiceEvent e = new EventServiceEvent(this,
	// EventServiceEvent.TYPE_CHANGED, null);
	// this.fireEventSeviceEvent(e);
	// }
	//
	// private void fireEventCreated() {
	// final EventServiceEvent e = new EventServiceEvent(this,
	// EventServiceEvent.TYPE_NEW, null);
	// this.fireEventSeviceEvent(e);
	// }
}
