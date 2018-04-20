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
package org.zeroclick.meeting.client.api.microsoft.data;

import java.util.Collection;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author djer
 * @see {@link https://developer.microsoft.com/fr-fr/graph/docs/api-reference/v1.0/resources/event}
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({ "PMD.TooManyFields", "PMD.ExcessivePublicCount" })
public class Event {
	@SuppressWarnings("PMD.ShortVariable")
	private String id;
	private String subject;
	private Recipient organizer;
	private DateTimeTimeZone start;
	private DateTimeTimeZone end;
	private Collection<Attendee> attendees;
	private ItemBody body;
	private String bodyPreview;
	private Collection<String> categories;
	private String changeKey;
	private Date createdDateTime;
	private Boolean hasAttachments;
	private String iCalUId;
	/**
	 * One of : Low, Normal and High;
	 */
	private String importance;
	private Boolean isAllDay;
	private Boolean isCancelled;
	private Boolean isOrganizer;
	private Boolean isReminderOn;
	private Date lastModifiedDateTime;
	private Location location;
	private String onlineMeetingUrl;
	private String originalEndTimeZone;
	private Date originalStart;
	private String originalStartTimeZone;
	private PatternedRecurrence recurrence;
	private Integer reminderMinutesBeforeStart;
	private Boolean responseRequested;
	private ResponseStatus responseStatus;
	/**
	 * One of : Normal, Personal, Private, Confidential.
	 */
	private String sensitivity;
	private String seriesMasterId;
	/**
	 * one of : Free, Tentative, Busy, Oof, WorkingElsewhere, Unknown.
	 */
	private String showAs;
	/**
	 * One of : SingleInstance, Occurrence, Exception, SeriesMaster.
	 */
	private String type;
	private String webLink;

	public String getId() {
		return this.id;
	}

	public void setId(final String eventId) {
		this.id = eventId;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public Recipient getOrganizer() {
		return this.organizer;
	}

	public void setOrganizer(final Recipient organizer) {
		this.organizer = organizer;
	}

	public DateTimeTimeZone getStart() {
		return this.start;
	}

	public void setStart(final DateTimeTimeZone start) {
		this.start = start;
	}

	public DateTimeTimeZone getEnd() {
		return this.end;
	}

	public void setEnd(final DateTimeTimeZone end) {
		this.end = end;
	}

	public Collection<Attendee> getAttendees() {
		return this.attendees;
	}

	public void setAttendees(final Collection<Attendee> attendees) {
		this.attendees = attendees;
	}

	public ItemBody getBody() {
		return this.body;
	}

	public void setBody(final ItemBody body) {
		this.body = body;
	}

	public String getBodyPreview() {
		return this.bodyPreview;
	}

	public void setBodyPreview(final String bodyPreview) {
		this.bodyPreview = bodyPreview;
	}

	public Collection<String> getCategories() {
		return this.categories;
	}

	public void setCategories(final Collection<String> categories) {
		this.categories = categories;
	}

	public String getChangeKey() {
		return this.changeKey;
	}

	public void setChangeKey(final String changeKey) {
		this.changeKey = changeKey;
	}

	public Date getCreatedDateTime() {
		return this.createdDateTime;
	}

	public void setCreatedDateTime(final Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public Boolean getHasAttachments() {
		return this.hasAttachments;
	}

	public void setHasAttachments(final Boolean hasAttachments) {
		this.hasAttachments = hasAttachments;
	}

	public String getiCalUId() {
		return this.iCalUId;
	}

	public void setiCalUId(final String iCalUId) {
		this.iCalUId = iCalUId;
	}

	public String getImportance() {
		return this.importance;
	}

	public void setImportance(final String importance) {
		this.importance = importance;
	}

	public Boolean getIsAllDay() {
		return this.isAllDay;
	}

	public void setIsAllDay(final Boolean isAllDay) {
		this.isAllDay = isAllDay;
	}

	public Boolean getIsCancelled() {
		return this.isCancelled;
	}

	public void setIsCancelled(final Boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public Boolean getIsOrganizer() {
		return this.isOrganizer;
	}

	public void setIsOrganizer(final Boolean isOrganizer) {
		this.isOrganizer = isOrganizer;
	}

	public Boolean getIsReminderOn() {
		return this.isReminderOn;
	}

	public void setIsReminderOn(final Boolean isReminderOn) {
		this.isReminderOn = isReminderOn;
	}

	public Date getLastModifiedDateTime() {
		return this.lastModifiedDateTime;
	}

	public void setLastModifiedDateTime(final Date lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public String getOnlineMeetingUrl() {
		return this.onlineMeetingUrl;
	}

	public void setOnlineMeetingUrl(final String onlineMeetingUrl) {
		this.onlineMeetingUrl = onlineMeetingUrl;
	}

	public String getOriginalEndTimeZone() {
		return this.originalEndTimeZone;
	}

	public void setOriginalEndTimeZone(final String originalEndTimeZone) {
		this.originalEndTimeZone = originalEndTimeZone;
	}

	public Date getOriginalStart() {
		return this.originalStart;
	}

	public void setOriginalStart(final Date originalStart) {
		this.originalStart = originalStart;
	}

	public String getOriginalStartTimeZone() {
		return this.originalStartTimeZone;
	}

	public void setOriginalStartTimeZone(final String originalStartTimeZone) {
		this.originalStartTimeZone = originalStartTimeZone;
	}

	public PatternedRecurrence getRecurrence() {
		return this.recurrence;
	}

	public void setRecurrence(final PatternedRecurrence recurrence) {
		this.recurrence = recurrence;
	}

	public Integer getReminderMinutesBeforeStart() {
		return this.reminderMinutesBeforeStart;
	}

	public void setReminderMinutesBeforeStart(final Integer reminderMinutesBeforeStart) {
		this.reminderMinutesBeforeStart = reminderMinutesBeforeStart;
	}

	public Boolean getResponseRequested() {
		return this.responseRequested;
	}

	public void setResponseRequested(final Boolean responseRequested) {
		this.responseRequested = responseRequested;
	}

	public ResponseStatus getResponseStatus() {
		return this.responseStatus;
	}

	public void setResponseStatus(final ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getSensitivity() {
		return this.sensitivity;
	}

	public void setSensitivity(final String sensitivity) {
		this.sensitivity = sensitivity;
	}

	public String getSeriesMasterId() {
		return this.seriesMasterId;
	}

	public void setSeriesMasterId(final String seriesMasterId) {
		this.seriesMasterId = seriesMasterId;
	}

	public String getShowAs() {
		return this.showAs;
	}

	public void setShowAs(final String showAs) {
		this.showAs = showAs;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getWebLink() {
		return this.webLink;
	}

	public void setWebLink(final String webLink) {
		this.webLink = webLink;
	}
}
