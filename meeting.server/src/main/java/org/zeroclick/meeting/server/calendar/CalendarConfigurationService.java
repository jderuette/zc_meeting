package org.zeroclick.meeting.server.calendar;

import java.util.Map;

import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.CommonService;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.CreateCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.DeleteApiPermission;
import org.zeroclick.meeting.shared.calendar.ICalendarConfigurationService;
import org.zeroclick.meeting.shared.calendar.ReadCalendarConfigurationPermission;
import org.zeroclick.meeting.shared.calendar.UpdateCalendarConfigurationPermission;

public class CalendarConfigurationService extends CommonService implements ICalendarConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(CalendarConfigurationService.class);

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
	public CalendarConfigurationFormData prepareCreate(final CalendarConfigurationFormData formData) {
		if (!ACCESS
				.check(new CreateCalendarConfigurationPermission(formData.getCalendarConfigurationId().getValue()))) {
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
		if (!ACCESS
				.check(new CreateCalendarConfigurationPermission(formData.getCalendarConfigurationId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getCalendarConfigurationId().getValue()) {
			formData.getCalendarConfigurationId().setValue(Long.valueOf(SQL.getSequenceNextval("EVENT_ID_SEQ")));
		}
		SQL.insert(SQLs.CALENDAR_CONFIG_INSERT, formData);
		return formData;
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
		return this.store(formData);
	}

	@Override
	public CalendarConfigurationFormData load(final CalendarConfigurationFormData formData) {
		if (!ACCESS.check(new ReadCalendarConfigurationPermission(formData.getCalendarConfigurationId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		final String sql = SQLs.CALENDAR_CONFIG_SELECT + SQLs.GENERIC_WHERE_FOR_SECURE_AND
				+ SQLs.CALENDAR_CONFIG_FILTER_USER_ID + SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID
				+ SQLs.CALENDAR_CONFIG_SELECT_INTO;

		SQL.selectInto(sql, formData);
		return formData;
	}

	@Override
	public CalendarConfigurationFormData store(final CalendarConfigurationFormData formData) {
		if (!ACCESS
				.check(new UpdateCalendarConfigurationPermission(formData.getCalendarConfigurationId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		SQL.insert(SQLs.CALENDAR_CONFIG_INSERT + SQLs.CALENDAR_CONFIG_FILTER_USER_ID
				+ SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID, formData);
		return formData;
	}

	@Override
	public void deleteByApiId(final Long apiCredentialId) {
		// permission based on API permissions (not calendar permissions)
		if (!ACCESS.check(new DeleteApiPermission(apiCredentialId))) {
			super.throwAuthorizationFailed();
		}

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
			data.getOAuthCredentialId().setValue(calendarData.getOAuthCredentialId());
			data.getProcessFreeEvent().setValue(Boolean.TRUE);
			data.getProcessFullDayEvent().setValue(Boolean.TRUE);
			data.getProcessNotRegistredOnEvent().setValue(Boolean.TRUE);

			final Long existingCalendarConfigId = this.getCalendarConfigId(data);
			if (null == existingCalendarConfigId) {
				this.create(data);
			} else {
				LOG.info("Calendar Configuration already exists for API : " + calendarData.getOAuthCredentialId()
						+ " for calendar key : " + calendarData.getExternalId() + " for User Id : "
						+ calendarData.getUserId());
			}
		}
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

	private Long getOwner(final Long calendarConfigurationId) {
		Long calendarOwner = null;
		final CalendarConfigurationFormData formData = new CalendarConfigurationFormData();
		formData.getCalendarConfigurationId().setValue(calendarConfigurationId);
		final Object[][] data = SQL.select(SQLs.CALENDAR_CONFIG_SELECT_OWNER, formData);

		if (null != data && data.length > 0 && data[0] != null && data[0].length > 0) {
			calendarOwner = (Long) data[0][0];
		}
		return calendarOwner;
	}

	@Override
	public boolean isOwn(final Long calendarConfigurationId) {
		final Long apiCredentialOwner = this.getOwner(calendarConfigurationId);

		if (null == apiCredentialOwner) {
			LOG.error("calendarConfigurationId " + calendarConfigurationId
					+ " as NO owner (user_id) on his OAuth related data ");
			return false;
		} else if (apiCredentialOwner.equals(super.userHelper.getCurrentUserId())) {
			return true;
		}

		return false;
	}

}
