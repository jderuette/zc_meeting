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
package org.zeroclick.meeting.client.event;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.comon.date.DateHelper;
import org.zeroclick.comon.text.TextsHelper;
import org.zeroclick.comon.user.AppUserHelper;
import org.zeroclick.configuration.shared.duration.DurationCodeType;
import org.zeroclick.meeting.client.GlobalConfig.ApplicationUrlProperty;
import org.zeroclick.meeting.shared.event.EventFormData;

import com.google.api.services.calendar.model.Event;

/**
 * @author djer
 *
 */
@ApplicationScoped
public class EventMessageHelper {

	private static final Logger LOG = LoggerFactory.getLogger(EventMessageHelper.class);

	private AppUserHelper appUserHelper;
	private DateHelper dateHelper;

	@PostConstruct
	public void init() {
		this.appUserHelper = BEANS.get(AppUserHelper.class);
		this.dateHelper = BEANS.get(DateHelper.class);
	}

	public String[] buildValuesForLocaleMessages(final EventFormData formData, final Long recipientUserId) {
		final List<String> values = new ArrayList<>();
		final ZoneId userZoneId = this.getAppUserHelper().getUserZoneId(recipientUserId);
		final Locale userLocal = TextsHelper.getUserLocal(recipientUserId);

		final String actor = this.getActorEmail(formData);
		final String receiver = this.getReceiverEmail(formData);
		final Long receiverId = recipientUserId;
		ZonedDateTime zonedStartRecipient = null;
		ZonedDateTime zonedEndRecipient = null;

		values.add(actor); // 0
		final String stateText = TextsHelper.get(receiverId,
				"zc.meeting.state." + formData.getState().getValue().toLowerCase());
		values.add(stateText.toLowerCase()); // 1

		values.add(formData.getSubject().getValue());// 2

		final String slotText = TextsHelper.get(receiverId, "zc.meeting.slot." + formData.getSlot().getValue());
		values.add(slotText.toLowerCase());// 3

		// final String durationText = TextsHelper.get(receiverId,
		// "zc.meeting.duration." + formData.getDuration().getValue());
		final String durationText = DurationCodeType.getText(formData.getDuration().getValue());

		values.add(durationText.toLowerCase());// 4

		String startDate = null;
		if (null != formData.getStartDate().getValue()) {
			// WARNING use the CURRENT USER ZONE, Then translate to "recipient"
			// user Zone
			final ZonedDateTime zonedStart = this.getDateHelper().getZonedValue(
					this.getAppUserHelper().getCurrentUserTimeZone(), formData.getStartDate().getValue());
			zonedStartRecipient = this.getDateHelper().atZone(zonedStart, userZoneId);
		}

		if (null != zonedStartRecipient) {
			startDate = this.getDateHelper().format(zonedStartRecipient, Boolean.TRUE);
		}
		values.add(startDate);// 5

		String endDate = null;
		if (null != formData.getEndDate().getValue()) {
			// WARNING use the CURRENT USER ZONE, Then translate to "recipient"
			// user Zone
			final ZonedDateTime zonedEnd = this.getDateHelper()
					.getZonedValue(this.getAppUserHelper().getCurrentUserTimeZone(), formData.getEndDate().getValue());
			zonedEndRecipient = this.getDateHelper().atZone(zonedEnd, userZoneId);
		}
		if (null != zonedEndRecipient) {
			endDate = this.getDateHelper().format(zonedEndRecipient, Boolean.TRUE);
		}
		values.add(endDate);// 6
		values.add(formData.getReason().getValue());// 7
		values.add(receiver);// 8

		String relativeStartDateDay = null;
		String startDateHours = null;
		if (null != zonedStartRecipient) {
			relativeStartDateDay = this.getDateHelper().getRelativeDay(zonedStartRecipient, userLocal, Boolean.TRUE,
					Boolean.TRUE);
			startDateHours = this.getDateHelper().formatHours(zonedStartRecipient, userLocal);
		}
		values.add(relativeStartDateDay);// 9
		values.add(startDateHours);// 10

		String endDateHours = null;
		if (null != zonedEndRecipient) {
			endDateHours = this.getDateHelper().formatHours(zonedEndRecipient, userLocal);
		}
		values.add(endDateHours);// 11

		String venue = null;
		if (null != formData.getVenue().getValue()) {
			venue = " (" + TextsHelper.get(receiverId, formData.getVenue().getValue()) + ")";
		}
		values.add(venue);// 12
		values.add(new ApplicationUrlProperty().getValue()); // 13

		return CollectionUtility.toArray(values, String.class);
	}

	public String[] buildValuesForLocaleMessages(final EventFormData formData, final Long recipientUserId,
			final Event externalEvent) {
		final List<String> values = CollectionUtility
				.arrayList(this.buildValuesForLocaleMessages(formData, recipientUserId));

		values.add(externalEvent.getHtmlLink()); // 14

		return CollectionUtility.toArray(values, String.class);
	}

	public String[] buildValuesForLocaleMessages(final EventFormData formData, final Long recipientUserId,
			final Event externalEvent, final String... otherParams) {
		final List<String> values = CollectionUtility
				.arrayList(this.buildValuesForLocaleMessages(formData, recipientUserId));

		values.add(externalEvent.getHtmlLink()); // 14

		for (final String param : otherParams) {
			values.add(param);
		}

		return CollectionUtility.toArray(values, String.class);
	}

	protected Boolean isCurerntUserActor(final EventFormData formData) {
		final Long lastModifierUserId = formData.getLastModifier();
		if (null == lastModifierUserId) {
			return Boolean.FALSE;
		}

		final Long currentUserId = this.getAppUserHelper().getCurrentUserId();
		return lastModifierUserId.equals(currentUserId);
	}

	protected Long getUserId(final EventFormData formData, final Boolean actor) {
		Long actorId = null;
		Long receiverId = null;
		Long userId;
		final Long lastModifierUserId = formData.getLastModifier();
		if (null != lastModifierUserId) {
			if (formData.getOrganizer().getValue().equals(lastModifierUserId)) {
				actorId = formData.getOrganizer().getValue();
				receiverId = formData.getGuestId().getValue();
				this.logExtractedData(formData.getEventId(), lastModifierUserId, actorId, "organizer");
			} else if (formData.getGuestId().getValue().equals(lastModifierUserId)) {
				actorId = formData.getGuestId().getValue();
				receiverId = formData.getOrganizer().getValue();
				this.logExtractedData(formData.getEventId(), lastModifierUserId, receiverId, "attendee");
				LOG.debug("Last modifier for event : " + formData.getEventId() + " is user : " + lastModifierUserId
						+ ", and the actor is the attendee : " + receiverId);
			} else {
				this.logExtractedData(formData.getEventId(), lastModifierUserId, (String) null,
						"neither orgnizer nor attendee user returning is null");
			}
		}
		if (actor) {
			userId = actorId;
		} else {
			userId = receiverId;
		}
		return userId;
	}

	protected String getActorEmail(final EventFormData formData) {
		String actor = this.extractEmail(formData, Boolean.TRUE);

		if (null == actor) {
			LOG.warn("No LastModifier in event : " + formData.getEventId()
					+ " Falling back to using holder to determine actor");
			if (this.isHeldByCurrentUser(formData)) {
				actor = formData.getOrganizerEmail().getValue();
			} else {
				actor = formData.getEmail().getValue();
			}
		}
		return actor;
	}

	protected String getReceiverEmail(final EventFormData formData) {
		String receiver = this.extractEmail(formData, Boolean.FALSE);

		if (null == receiver) {
			LOG.warn("No LastModifier in event : " + formData.getEventId()
					+ " Falling back to using holder to determine receiver");
			if (this.isHeldByCurrentUser(formData)) {
				receiver = formData.getEmail().getValue();
			} else {
				receiver = formData.getOrganizerEmail().getValue();
			}
		}
		return receiver;
	}

	protected String extractEmail(final EventFormData formData, final Boolean actor) {
		String actorEmail = null;
		String receiverEmail = null;
		String userEmail;
		final Long lastModifierUserId = formData.getLastModifier();
		if (null != lastModifierUserId) {
			if (formData.getOrganizer().getValue().equals(lastModifierUserId)) {
				actorEmail = formData.getOrganizerEmail().getValue();
				receiverEmail = formData.getEmail().getValue();
				this.logExtractedData(formData.getEventId(), lastModifierUserId, actorEmail, "organizer");
			} else if (formData.getGuestId().getValue().equals(lastModifierUserId)) {
				actorEmail = formData.getEmail().getValue();
				receiverEmail = formData.getOrganizerEmail().getValue();
				this.logExtractedData(formData.getEventId(), lastModifierUserId, receiverEmail, "attendee");
			} else {
				this.logExtractedData(formData.getEventId(), lastModifierUserId, (String) null,
						"neither orgnizer nor attendee user mail return is null");
			}
		}
		if (actor) {
			userEmail = actorEmail;
		} else {
			userEmail = receiverEmail;
		}
		return userEmail;
	}

	private void logExtractedData(final Long eventId, final Long lastModifierUserId, final Long extractedId,
			final String actorRole) {
		this.logExtractedData(eventId, lastModifierUserId, String.valueOf(extractedId), actorRole);

	}

	private void logExtractedData(final Long eventId, final Long lastModifierUserId, final String extractedValue,
			final String actorRole) {
		if (LOG.isDebugEnabled()) {
			final StringBuilder message = new StringBuilder();
			message.append("Last modifier for event : ").append(eventId).append(" is user : ")
					.append(lastModifierUserId).append(", and the actor is ").append(actorRole).append(" : ")
					.append(extractedValue);
			LOG.debug(message.toString());
		}
	}

	/**
	 * Is row event (formData) held by CurrentUser ?
	 *
	 * @param row
	 * @return
	 */
	protected Boolean isHeldByCurrentUser(final EventFormData formData) {
		return this.isOrganizer(formData.getOrganizer().getValue());
	}

	protected Boolean isOrganizer(final Long userId) {
		final Long currentUser = this.getAppUserHelper().getCurrentUserId();

		return currentUser.equals(userId);
	}

	public AppUserHelper getAppUserHelper() {
		return this.appUserHelper;
	}

	public void setAppUserHelper(final AppUserHelper appUserHelper) {
		this.appUserHelper = appUserHelper;
	}

	public DateHelper getDateHelper() {
		return this.dateHelper;
	}

	public void setDateHelper(final DateHelper dateHelper) {
		this.dateHelper = dateHelper;
	}

}
