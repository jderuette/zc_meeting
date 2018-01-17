package org.zeroclick.meeting.server.calendar;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission;

public class CalendarConfigurationService extends AbstractCommonService implements ICalendarConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(CalendarConfigurationService.class);

	@Override
	protected Logger getLog() {
		return LOG;
	}

	@Override
	public CalendarConfigurationTablePageData getCalendarConfigurationTableData(final SearchFilter filter,
			final Boolean displayAllForAdmin) {
		final CalendarConfigurationTablePageData pageData = new CalendarConfigurationTablePageData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin || ACCESS.getLevel(new CreateCalendarConfigurationPermission(
				(Long) null)) != CreateCalendarConfigurationPermission.LEVEL_ALL) {
			ownerFilter = SQLs.CALENDAR_CONFIG_FILTER_CURRENT_USER;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.CALENDAR_CONFIG_PAGE_SELECT + ownerFilter + SQLs.CALENDAR_CONFIG_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("currentUser", currentConnectedUserId));
		return pageData;
	}

	@Override
	public CalendarsConfigurationFormData getCalendarConfigurationTableData(final Boolean displayAllForAdmin) {
		final CalendarsConfigurationFormData formData = new CalendarsConfigurationFormData();

		String ownerFilter = "";
		Long currentConnectedUserId = 0L;
		if (!displayAllForAdmin || ACCESS.getLevel(new CreateCalendarConfigurationPermission(
				(Long) null)) != CreateCalendarConfigurationPermission.LEVEL_ALL) {
			ownerFilter = SQLs.CALENDAR_CONFIG_FILTER_CURRENT_USER;
			currentConnectedUserId = super.userHelper.getCurrentUserId();
		}

		final String sql = SQLs.CALENDAR_CONFIG_PAGE_SELECT + ownerFilter + SQLs.CALENDAR_CONFIG_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", formData.getCalendarConfigTable()),
				new NVPair("currentUser", currentConnectedUserId));
		return formData;
	}

	@Override
	public CalendarConfigurationFormData prepareCreate(final CalendarConfigurationFormData formData) {
		if (!ACCESS.check(new CreateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		// TODO [djer] add business logic here.
		return formData;
	}

	@Override
	public CalendarConfigurationFormData create(final CalendarConfigurationFormData formData) {
		LOG.info("Creating new Calendar Configuration for API Id : " + formData.getOAuthCredentialId().getValue()
				+ " for calendar Key : " + formData.getExternalId().getValue() + " for User ID : "
				+ formData.getUserId().getValue());
		if (!ACCESS.check(new CreateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getCalendarConfigurationId().getValue()) {
			formData.getCalendarConfigurationId().setValue(Long.valueOf(SQL.getSequenceNextval("EVENT_ID_SEQ")));
		}
		SQL.insert(SQLs.CALENDAR_CONFIG_INSERT, formData);
		return this.store(formData, Boolean.TRUE);
	}

	@Override
	public CalendarConfigurationFormData createWithDefault(final CalendarConfigurationFormData formData) {
		LOG.info("Creating default Calendar Configuration for API Id : " + formData.getOAuthCredentialId().getValue()
				+ " for calendar Key : " + formData.getExternalId().getValue() + " for User ID : "
				+ formData.getUserId().getValue());
		if (null == formData.getExternalId().getValue()) {
			throw new VetoException("Provider Id (external ID) required");
		}

		if (null == formData.getOAuthCredentialId().getValue()) {
			throw new VetoException("OAuthId required");
		}

		if (null == formData.getProcessFreeEvent().getValue()) {
			formData.getProcessFreeEvent().setValue(Boolean.TRUE);
		}
		if (null == formData.getProcessFullDayEvent().getValue()) {
			formData.getProcessFullDayEvent().setValue(Boolean.TRUE);
		}
		if (null == formData.getProcessNotRegistredOnEvent().getValue()) {
			formData.getProcessNotRegistredOnEvent().setValue(Boolean.TRUE);
		}
		this.create(formData);

		this.sendCreatedNotifications(formData);

		return this.store(formData);
	}

	@Override
	public CalendarConfigurationFormData load(final CalendarConfigurationFormData formData) {
		final Long apiId = formData.getOAuthCredentialId().getValue();
		// apiId is optional, if provides check permission BEFORE request
		if (null != formData.getOAuthCredentialId().getValue()) {
			this.checkPermission(new ReadCalendarConfigurationPermission(apiId));
		}

		final String sql = SQLs.CALENDAR_CONFIG_SELECT + SQLs.GENERIC_WHERE_FOR_SECURE_AND
				+ SQLs.CALENDAR_CONFIG_FILTER_ID + SQLs.CALENDAR_CONFIG_SELECT_INTO;

		SQL.selectInto(sql, formData);

		// check permission, required if no apiId provides in input data, else
		// double check (also required if apiId provided is not the one to the
		// clalendarConfigId)
		this.checkPermission(new ReadCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()));
		return formData;
	}

	@Override
	public CalendarConfigurationFormData store(final CalendarConfigurationFormData formData) {
		return this.store(formData, Boolean.TRUE);
	}

	private CalendarConfigurationFormData store(final CalendarConfigurationFormData formData,
			final Boolean duringCreate) {
		this.checkPermission(new UpdateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()));
		SQL.insert(SQLs.CALENDAR_CONFIG_UPDATE + SQLs.GENERIC_WHERE_FOR_SECURE_AND + SQLs.CALENDAR_CONFIG_FILTER_ID
				+ SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID, formData);

		if (!duringCreate) {
			this.sendModifiedNotifications(formData);
		}

		return formData;
	}

	@Override
	public CalendarConfigurationFormData getCalendarToStoreEvents(final Long userId) {
		final Long calendarConfigId = this.getCalendarIdToStoreEvents(userId);
		if (null == calendarConfigId) {
			return null; // early break
		}

		final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
		formData.getCalendarConfigurationId().setValue(calendarConfigId);

		return this.load(formData);
	}

	@Override
	public void deleteByApiId(final Long apiCredentialId) {
		// permission based on API permissions (not calendar permissions)
		this.checkPermission(new DeleteApiPermission(apiCredentialId));

		SQL.delete(SQLs.CALENDAR_CONFIG_DELETE_BY_API_ID, new NVPair("oAuthCredentialId", apiCredentialId));
	}

	@Override
	public void autoConfigure(final Map<String, AbstractCalendarConfigurationTableRowData> calendars) {
		LOG.info("Auto importing calendars for User");
		for (final String calendarKey : calendars.keySet()) {
			final AbstractCalendarConfigurationTableRowData calendarData = calendars.get(calendarKey);
			final CalendarConfigurationFormData data = new CalendarConfigurationFormData();
			data.getUserId().setValue(calendarData.getUserId());
			data.getExternalId().setValue(calendarData.getExternalId());
			data.getName().setValue(calendarData.getName());
			data.getProcess().setValue(calendarData.getMain());
			data.getMain().setValue(calendarData.getMain());
			data.getAddEventToCalendar().setValue(calendarData.getMain());
			data.getOAuthCredentialId().setValue(calendarData.getOAuthCredentialId());
			data.getProcessFreeEvent().setValue(Boolean.TRUE);
			data.getProcessFullDayEvent().setValue(Boolean.FALSE);
			data.getProcessNotRegistredOnEvent().setValue(Boolean.TRUE);

			final Long existingCalendarConfigId = this.getCalendarConfigId(data);
			if (null == existingCalendarConfigId) {
				this.create(data);
			} else {
				final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();

				LOG.info("Calendar Configuration already exists for API : " + calendarData.getOAuthCredentialId()
						+ " for calendar key : " + calendarData.getExternalId() + "(" + calendarData.getName()
						+ ") for User Id : " + calendarData.getUserId() + " Updating calendar Data");

				formData.getCalendarConfigurationId().setValue(existingCalendarConfigId);
				final CalendarConfigurationFormData existingCalendarConfig = this.load(formData);
				// override "provider specific" data
				existingCalendarConfig.getExternalId().setValue(calendarData.getExternalId());
				existingCalendarConfig.getName().setValue(calendarData.getName());
				existingCalendarConfig.getReadOnly().setValue(calendarData.getReadOnly());
				if (calendarData.getReadOnly() && existingCalendarConfig.getAddEventToCalendar().getValue()) {
					LOG.warn("The calendar " + existingCalendarConfig.getCalendarConfigurationId() + "("
							+ existingCalendarConfig.getName() + ") for User " + existingCalendarConfig.getUserId()
							+ " is (became ?) readOnly and is configured to store event. Disabling addEventToCalendar for this calendar.");
					existingCalendarConfig.getAddEventToCalendar().setValue(Boolean.FALSE);
				}

				this.store(existingCalendarConfig, Boolean.FALSE);
			}
		}
		// TODO Djer send global notification instead of one notification for
		// eache create/update ?
	}

	private Long getCalendarConfigId(final CalendarConfigurationFormData formData) {
		Long calendarConfigId = null;
		final String sql = SQLs.CALENDAR_CONFIG_SELECT_ID + SQLs.GENERIC_WHERE_FOR_SECURE_AND
				+ SQLs.CALENDAR_CONFIG_FILTER_USER_ID + SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID;

		final Object[][] data = SQL.select(sql, formData);
		if (null != data && data.length > 0 && data[0] != null && data[0].length > 0) {
			calendarConfigId = (Long) data[0][0];
		}
		return calendarConfigId;
	}

	protected Long getCalendarIdToStoreEvents(final Long userId) {
		Long calendarConfigId = null;
		final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
		formData.getUserId().setValue(userId);

		final String sql = SQLs.CALENDAR_CONFIG_SELECT_ID + SQLs.GENERIC_WHERE_FOR_SECURE_AND
				+ SQLs.CALENDAR_CONFIG_FILTER_USER_ID + SQLs.CALENDAR_CONFIG_FILTER_ADD_EVENT;

		final Object[][] data = SQL.select(sql, formData);
		if (null != data && data.length > 0 && data[0] != null && data[0].length > 0) {
			calendarConfigId = (Long) data[0][0];
		}

		return calendarConfigId;
	}

	private void sendModifiedNotifications(final CalendarsConfigurationFormData formData) {
		if (formData.getCalendarConfigTable().getRowCount() > 0) {
			final Long userId = formData.getCalendarConfigTable().getRows()[0].getUserId();
			final Set<String> notifiedUsers = this.buildNotifiedUsers(userId, Boolean.TRUE);
			BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
					new CalendarsConfigurationModifiedNotification(formData));
		} else {
			LOG.warn(
					"Cannot send user CalendarsConfigurationModifiedNotification because no User ID (no calendars modified");
		}
	}

	private void sendModifiedNotifications(final CalendarConfigurationFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData.getUserId().getValue(), Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new CalendarConfigurationModifiedNotification(formData));
	}

	private void sendCreatedNotifications(final CalendarConfigurationFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData.getUserId().getValue(), Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new CalendarConfigurationCreatedNotification(formData));
	}

	private Long getOwner(final Long oAuthCredentailId) {
		final ApiService apiService = BEANS.get(ApiService.class);
		return apiService.getOwner(oAuthCredentailId);
	}

	@Override
	public boolean isOwn(final Long apiCredentailId) {
		final Long apiCredentialOwner = this.getOwner(apiCredentailId);

		if (null == apiCredentialOwner) {
			LOG.error("apiCredentailId " + apiCredentailId + " as NO owner (user_id) on his OAuth related data ");
			return false;
		} else if (apiCredentialOwner.equals(super.userHelper.getCurrentUserId())) {
			return true;
		}

		return false;
	}

}
