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
package org.zeroclick.meeting.server.sql.migrate.data;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.event.EventStateCodeType;
import org.zeroclick.meeting.shared.event.EventTablePageData.EventTableRowData;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.event.RejectEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.ExternalEventFormData;
import org.zeroclick.meeting.shared.event.externalevent.IExternalEventService;
import org.zeroclick.meeting.shared.event.involevment.EventRoleCodeType;
import org.zeroclick.meeting.shared.event.involevment.IInvolvementService;
import org.zeroclick.meeting.shared.event.involevment.InvolvementFormData;
import org.zeroclick.meeting.shared.event.involevment.InvolvmentStateCodeType;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public class PatchMultiInviteeMeeting extends AbstractDataPatcher {

	public static final String PATCHED_TABLE = "EVENT";
	public static final String PATCHED_CREATED_INVOLVEMENT_TABLE = "INVOLVEMENT";
	public static final String PATCHED_CREATED_EXTERNAL_EVENT_TABLE = "EXTERNAL_EVENT";

	public static final String PATCHED_MOOVED_STATE_COLUMN = "state";
	public static final String PATCHED_MOOVED_REASON_COLUMN = PatchEventRejectReason.PATCHED_COLUMN;

	private static final Logger LOG = LoggerFactory.getLogger(PatchMultiInviteeMeeting.class);
	public static final String PATCHED_CREATED_EXTERNAL_CALENDAR_ID_COLUMN = "external_calendar_id";
	public static final String PATCHED_CREATED_EXTERNAL_EVENT_ID_COLUMN = "external_event_id";

	public static final String PATCHED_REMOVED_EXTERNAL_ORGANIZER_ID_COLUMN = "externalIdOrganizer";
	public static final String PATCHED_REMOVED_EXTERNAL_INVETEE_ID_COLUMN = "externalIdRecipient";
	public static final String EXTERNAL_EVENT_ID_SEQ = "EXTERNAL_EVENT_ID_SEQ";

	public PatchMultiInviteeMeeting() {
		this.setDescription("Allow multiple invetee for event's and default required params key/value");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.zeroclick.meeting.server.sql.migrate.IDataPatcher#getVersion()
	 */
	@Override
	public Version getVersion() {
		return Version.valueOf("1.1.16");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.zeroclick.meeting.server.sql.migrate.AbstractDataPatcher#execute()
	 */
	@Override
	protected void execute() {
		if (super.canMigrate()) {
			LOG.info("Allow multiple invetee for event's will be apply to the data");
			final Boolean strtcureAltered = this.migrateStrucutre();
			if (strtcureAltered) {
				this.migrateData();
			}

			// if no exception during migration, remove useless Column
			// this.migrateStrucutreRemoveUseless();

		}
	}

	private Boolean migrateStrucutre() {
		LOG.info("Allow multiple invetee for event's upgrading data strcuture");
		Boolean structureAltered = Boolean.FALSE;

		if (!this.getDatabaseHelper().existTable(PATCHED_CREATED_INVOLVEMENT_TABLE)) {
			SQL.insert(SQLs.INVOLVEMENT_CREATE_TABLE);
			SQL.insert(SQLs.INVOLVEMENT_PK);
			SQL.insert(SQLs.INVOLVEMENT_EVENT_FK);
			SQL.insert(SQLs.INVOLVEMENT_USER_FK);
			SQL.insert(SQLs.INVOLVEMENT_USER_INVITED_BY_FK);

			structureAltered = Boolean.TRUE;
		}

		if (!this.getDatabaseHelper().existTable(PATCHED_CREATED_EXTERNAL_EVENT_TABLE)) {
			SQL.insert(SQLs.EXTERNAL_EVENT_CREATE_TABLE);
			SQL.insert(SQLs.EXTERNAL_EVENT_API_FK);

			SQL.insert(SQLs.INVOLVEMENT_EXTERNAL_EVENT_FK);

			if (!this.getDatabaseHelper().isSequenceExists(EXTERNAL_EVENT_ID_SEQ)) {
				this.getDatabaseHelper().createSequence(EXTERNAL_EVENT_ID_SEQ);
			}

			structureAltered = Boolean.TRUE;
		}

		// as it create a Table force a refresh of Table Cache
		this.getDatabaseHelper().resetExistingTablesCache();

		return structureAltered;
	}

	private void migrateData() {
		LOG.info("Allow multiple invetee for event's upgraing default data");
		final IEventService eventService = BEANS.get(IEventService.class);

		// load all event
		final AbstractTablePageData eventTableData = eventService.getEventAdminTableDataOld(null);

		if (eventTableData.getRowCount() > 0) {
			for (final AbstractTableRowData rawEvent : eventTableData.getRows()) {

				final EventTableRowData event = (EventTableRowData) rawEvent;

				String newEventState = null;

				final Long eventId = event.getEventId();
				LOG.info("Allow multiple invetee for event's migrating event : " + eventId);

				// -- extract Orga_external_id => {orgaExternalIdData} Create
				// External_id
				// ROW (external calendar id ) current "add" calendar ID
				final String orgaExternalEventId = event.getExternalIdOrganizer();
				final Long orgaUserId = event.getOrganizer();
				ExternalEventFormData orgaExternalIdData = null;
				if (null != orgaExternalEventId) {
					orgaExternalIdData = this.createExternalId(orgaUserId, orgaExternalEventId);
				}

				// -- invetee_external_id ==> {inveteeExternalIdData} If same as
				// Orga, retrieve external ID, else ==> Create External_id ROW
				// (external calendar id ) current "add" calendar ID
				final String guestExternalEventId = event.getExternalIdRecipient();
				final Long guestUserId = event.getGuestId();
				ExternalEventFormData guestExternalIdData = null;

				if (null != guestExternalEventId) {
					if (orgaExternalEventId.equals(guestExternalEventId)) {
						// orga and guest share same external event
						guestExternalIdData = orgaExternalIdData;
					} else {
						if (null != guestExternalEventId) {
							guestExternalIdData = this.createExternalId(guestUserId, guestExternalEventId);
						}
					}
				}

				// -- extract Organizer_id, event_id ==> create Involvement Row
				// (state = "PROPOSED"), with {orgaExternalIdData}
				Long orgaExternalId = null;
				if (null != orgaExternalIdData) {
					orgaExternalId = orgaExternalIdData.getExternalId().getValue();
				}

				this.createInvolvment(eventId, orgaUserId, EventRoleCodeType.OrganizerCode.ID,
						InvolvmentStateCodeType.ProposedCode.ID, null, orgaExternalId, orgaExternalId);

				// -- extract guest_id, event_id, state, reason ==> create
				// Involvement Row with {inveteeExternalIdData}
				final String state = event.getState();
				final String reason = event.getReason();

				Long guestExternalId = null;
				if (null != guestExternalIdData) {
					guestExternalId = guestExternalIdData.getExternalId().getValue();
				}

				this.createInvolvment(eventId, guestUserId, EventRoleCodeType.RequiredGuestCode.ID, state, reason,
						guestExternalId, orgaExternalId);

				// update the EVENT state based on new Values (distinct from
				// Involvement state)
				final String currentEventState = event.getState();
				if (null == currentEventState) {
					newEventState = EventStateCodeType.WaitingCode.ID;
				} else {
					if ("ACCEPTED".equals(currentEventState)) {
						newEventState = EventStateCodeType.PlannedCode.ID;
					} else if ("REFUSED".equals(currentEventState)) {
						newEventState = EventStateCodeType.CanceledCode.ID;
					} else {
						// ASKED or any other values
						newEventState = EventStateCodeType.WaitingCode.ID;
					}
				}

				final RejectEventFormData formData = new RejectEventFormData();
				formData.setEventId(event.getEventId());
				formData.setState(newEventState);
				formData.getReason().setValue(reason);
				eventService.storeNewState(formData, event.getOrganizer());
			}
		}
	}

	private ExternalEventFormData createExternalId(final Long userId, final String externalEventId) {
		final ICalendarConfigurationService calendarConfigurationService = BEANS
				.get(ICalendarConfigurationService.class);
		final IExternalEventService externalEventService = BEANS.get(IExternalEventService.class);

		final CalendarConfigurationFormData calendarToStoreEvent = calendarConfigurationService
				.getCalendarToStoreEvents(userId);

		final ExternalEventFormData formData = new ExternalEventFormData();
		formData.getExternalEventId().setValue(externalEventId);
		if (null == calendarToStoreEvent) {
			LOG.warn("Migration failled (partial Data), because organizer hasen't set is claendar to Store event");
			formData.getExternalCalendarId().setValue("UNKNOW_EXTERNAL_CALENDAR_FOR_USER_" + userId);
		} else {
			formData.getExternalCalendarId().setValue(calendarToStoreEvent.getExternalId().getValue());
			formData.getApiCredentialId().setValue(calendarToStoreEvent.getOAuthCredentialId().getValue());
		}

		final ExternalEventFormData orgaCreatedExternalIdData = externalEventService.create(formData);

		return orgaCreatedExternalIdData;
	}

	private void createInvolvment(final Long eventId, final Long userId, final String role, final String state,
			final String reason, final Long externalEventId, final Long invitedBy) {
		final IInvolvementService involevmentService = BEANS.get(IInvolvementService.class);

		final InvolvementFormData formData = new InvolvementFormData();
		formData.getEventId().setValue(eventId);
		formData.getUserId().setValue(userId);
		formData.getRole().setValue(role);
		formData.getState().setValue(state);
		formData.getReason().setValue(reason);
		formData.getExternalEventId().setValue(externalEventId);
		formData.getInvitedBy().setValue(invitedBy);

		involevmentService.create(formData);
	}

	private void migrateStrucutreRemoveUseless() {
		LOG.info("Allow multiple invetee for event's removing useless Column avfter migration");

		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_REMOVED_EXTERNAL_ORGANIZER_ID_COLUMN);
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_REMOVED_EXTERNAL_INVETEE_ID_COLUMN);
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_MOOVED_STATE_COLUMN);
		this.getDatabaseHelper().removeColumn(PATCHED_TABLE, PATCHED_MOOVED_REASON_COLUMN);

	}

	@Override
	public void undo() {
		LOG.warn("Allow multiple invetee for event's dowgrade structure");
		this.getDatabaseHelper().dropTable(PATCHED_CREATED_INVOLVEMENT_TABLE);
		this.getDatabaseHelper().dropTable(PATCHED_CREATED_EXTERNAL_EVENT_TABLE);

		LOG.warn(
				"Allow multiple invetee for event's CANNOT dowgrade data (created table removed, but data not migrated Back to Event Table)");
	}
}
