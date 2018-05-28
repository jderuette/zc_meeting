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
package org.zeroclick.meeting.service;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.params.AppParamsFormData;
import org.zeroclick.common.params.ParamCreatedNotificationHandler;
import org.zeroclick.common.params.ParamModifiedNotificationHandler;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.params.ParamCreatedNotification;
import org.zeroclick.configuration.shared.params.ParamModifiedNotification;
import org.zeroclick.meeting.client.common.CallTrackerService;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class CallTrackerHelper {
	private static final Logger LOG = LoggerFactory.getLogger(CallTrackerHelper.class);

	protected INotificationListener<ParamCreatedNotification> paramCreatedListener;
	protected INotificationListener<ParamModifiedNotification> paramModifiedListener;

	private CallTrackerService<Long> eventCallTracker;

	@PostConstruct
	public void init() {

		final ParamCreatedNotificationHandler paramCreatedNotifHand = BEANS.get(ParamCreatedNotificationHandler.class);
		paramCreatedNotifHand.addListener(this.createParamCreatedListener());

		final ParamModifiedNotificationHandler paramModifiedNotifHand = BEANS
				.get(ParamModifiedNotificationHandler.class);
		paramModifiedNotifHand.addListener(this.createParamModifiedListener());
	}

	@PreDestroy
	public void destroy() {
		final ParamCreatedNotificationHandler paramCreatedNotifHand = BEANS.get(ParamCreatedNotificationHandler.class);
		paramCreatedNotifHand.removeListener(this.paramCreatedListener);

		final ParamModifiedNotificationHandler paramModifiedNotifHand = BEANS
				.get(ParamModifiedNotificationHandler.class);
		paramModifiedNotifHand.removeListener(this.paramModifiedListener);
	}

	private Integer getMaxNbCall() {
		final IAppParamsService appParamService = BEANS.get(IAppParamsService.class);
		Integer maxNbCall = 20;

		final String maxNbCallParam = appParamService.getValue(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX);

		try {
			if (null != maxNbCallParam) {
				maxNbCall = Integer.valueOf(maxNbCallParam);
			}
		} catch (final NumberFormatException nfe) {
			LOG.warn("No params aviable for Calltracker event max ("
					+ IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX + ") FallBack to default value");
		}

		return maxNbCall;
	}

	private Integer getConfiguredDuration() {
		final IAppParamsService appParamService = BEANS.get(IAppParamsService.class);

		Integer maxDuration = 3;
		final String maxDurationParam = appParamService
				.getValue(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION);

		try {
			maxDuration = Integer.valueOf(maxDurationParam);
		} catch (final NumberFormatException nfe) {
			LOG.warn("No params aviable for Calltracker event Duration ("
					+ IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION + ") FallBack to default value");
		}

		return maxDuration;
	}

	private void updateEventCallTracker(final AppParamsFormData paramForm) {
		final String appParamKey = paramForm.getKey().getValue();
		final String appParamValue = paramForm.getValue().getValue();
		if (LOG.isDebugEnabled()) {
			LOG.debug(
					new StringBuilder().append("New Param prepare to update CallTracker configuration for param Id : ")
							.append(paramForm.getParamId()).append(" with key : ").append(appParamKey).toString());
		}
		try {

			if (null != appParamKey) {
				if (appParamKey.equals(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_MAX)) {
					LOG.info("Updatding Events CallTracker max try with value : " + appParamValue);
					Integer maxSuccesivCall = null;
					try {
						maxSuccesivCall = Integer.valueOf(appParamValue);
						this.getEventCallTracker().setMaxSuccessiveCall(maxSuccesivCall);
					} catch (final NumberFormatException nfe) {
						LOG.warn("Cannot update Event Call Tracker max successive Call with invalid value : "
								+ appParamValue, nfe);
					}

				} else if (appParamKey.equals(IAppParamsService.APP_PARAM_KEY_EVENT_CALL_TRACKER_DURATION)) {
					LOG.info("Updatding Events CallTracker duration try with value : " + appParamValue);

					Integer ttl = null;
					try {
						ttl = Integer.valueOf(appParamValue);
						this.getEventCallTracker().setTimeToLive(Duration.ofMinutes(ttl));
					} catch (final NumberFormatException nfe) {
						LOG.warn("Cannot update Event Call Tracker ttl with invalid value : " + appParamValue, nfe);
					}
				}
			}

		} catch (final RuntimeException e) {
			LOG.error("Could not update Event configuration (new AppParam).", e);
		}
	}

	protected INotificationListener<ParamCreatedNotification> createParamCreatedListener() {
		this.paramCreatedListener = new INotificationListener<ParamCreatedNotification>() {
			@Override
			public void handleNotification(final ParamCreatedNotification notification) {

				final AppParamsFormData paramForm = notification.getFormData();
				CallTrackerHelper.this.updateEventCallTracker(paramForm);
			}
		};
		return this.paramCreatedListener;
	}

	protected INotificationListener<ParamModifiedNotification> createParamModifiedListener() {
		this.paramModifiedListener = new INotificationListener<ParamModifiedNotification>() {
			@Override
			public void handleNotification(final ParamModifiedNotification notification) {

				final AppParamsFormData paramForm = notification.getFormData();
				CallTrackerHelper.this.updateEventCallTracker(paramForm);
			}
		};
		return this.paramModifiedListener;
	}

	public CallTrackerService<Long> getEventCallTracker() {
		if (null == this.eventCallTracker) {
			this.createEventCallTracker();
		}

		return this.eventCallTracker;

	}

	private void createEventCallTracker() {
		this.eventCallTracker = new CallTrackerService<>(this.getMaxNbCall(),
				Duration.ofMinutes(this.getConfiguredDuration()), "Get calendar Events");
	}

}
