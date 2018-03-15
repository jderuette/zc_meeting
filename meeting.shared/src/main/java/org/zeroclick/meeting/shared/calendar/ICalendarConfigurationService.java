package org.zeroclick.meeting.shared.calendar;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;

@TunnelToServer
public interface ICalendarConfigurationService extends IService {

	CalendarConfigurationTablePageData getCalendarConfigurationTableData(SearchFilter filter,
			Boolean displayAllForAdmin);

	CalendarsConfigurationFormData getCalendarConfigurationTableData(Boolean displayAllForAdmin);

	void autoConfigure(Map<String, AbstractCalendarConfigurationTableRowData> calendars);

	CalendarConfigurationFormData prepareCreate(CalendarConfigurationFormData formData);

	CalendarConfigurationFormData create(CalendarConfigurationFormData formData);

	CalendarConfigurationFormData load(CalendarConfigurationFormData formData);

	CalendarConfigurationFormData store(CalendarConfigurationFormData formData);

	CalendarsConfigurationFormData store(CalendarsConfigurationFormData formData);

	CalendarConfigurationFormData createWithDefault(CalendarConfigurationFormData formData);

	boolean isOwn(Long apiCredentailId);

	boolean isInvolved(Long apiCredentailId);

	void deleteByApiId(Long apiCredentialId);

	CalendarConfigurationFormData getCalendarToStoreEvents(Long userId);

	Set<AbstractCalendarConfigurationTableRowData> getUsedCalendars(Long userId);

	Long getCalendarApiId(String calendarId, Long eventHeldBy);

}
