package org.zeroclick.meeting.server.calendar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.AbstractCommonService;
import org.zeroclick.common.AbstractFormDataCache;
import org.zeroclick.common.AbstractPageDataDataCache;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.shared.calendar.AbstractCalendarConfigurationTablePageData.AbstractCalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationFormData.UserId;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationModifiedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData;
import org.zeroclick.meeting.shared.calendar.CalendarConfigurationTablePageData.CalendarConfigurationTableRowData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationCreatedNotification;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData;
import org.zeroclick.meeting.shared.calendar.CalendarsConfigurationFormData.CalendarConfigTable.CalendarConfigTableRowData;
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

	private final AbstractFormDataCache<Long, CalendarConfigurationFormData> dataCache = new AbstractFormDataCache<Long, CalendarConfigurationFormData>() {
		@Override
		public CalendarConfigurationFormData loadForCache(final Long calendarConfigId) {
			final CalendarConfigurationFormData calendarConfigurationFormData = new CalendarConfigurationFormData();
			calendarConfigurationFormData.getCalendarConfigurationId().setValue(calendarConfigId);
			return CalendarConfigurationService.this.loadForCache(calendarConfigurationFormData);
		}
	};

	private final AbstractPageDataDataCache<Long, CalendarConfigurationTablePageData> dataCacheByUserId = new AbstractPageDataDataCache<Long, CalendarConfigurationTablePageData>() {
		@Override
		public CalendarConfigurationTablePageData loadForCache(final Long userId) {
			return CalendarConfigurationService.this.loadForCacheByUserId(userId);
		}
	};

	private ICache<Long, CalendarConfigurationFormData> getDataCache() {
		return this.dataCache.getCache();
	}

	private ICache<Long, CalendarConfigurationTablePageData> getDataCacheByUser() {
		return this.dataCacheByUserId.getCache();
	}

	protected CalendarConfigurationTablePageData loadForCacheByUserId(final Long userId) {
		final CalendarConfigurationTablePageData pageData = new CalendarConfigurationTablePageData();

		final String sql = SQLs.CALENDAR_CONFIG_PAGE_SELECT + SQLs.CALENDAR_CONFIG_FILTER_USER_ID
				+ SQLs.CALENDAR_CONFIG_PAGE_SELECT_INTO;

		SQL.selectInto(sql, new NVPair("page", pageData), new NVPair("userId", userId));

		return pageData;
	}

	protected CalendarConfigurationFormData loadForCache(final CalendarConfigurationFormData formData) {
		// permission check done by Load
		final String sql = SQLs.CALENDAR_CONFIG_SELECT + SQLs.GENERIC_WHERE_FOR_SECURE_AND
				+ SQLs.CALENDAR_CONFIG_FILTER_ID + SQLs.CALENDAR_CONFIG_SELECT_INTO;
		SQL.selectInto(sql, formData);
		return formData;
	}

	@Override
	public CalendarConfigurationTablePageData getCalendarConfigurationTableData(final SearchFilter filter,
			final Boolean displayAllForAdmin) {
		Long userId = null;

		if (null != filter && null != filter.getFormData()
				&& null != filter.getFormData().getFieldByClass(UserId.class)) {
			userId = filter.getFormData().getFieldByClass(UserId.class).getValue();
		}

		final Boolean isAdmin = ACCESS.getLevel(
				new ReadCalendarConfigurationPermission((Long) null)) == ReadCalendarConfigurationPermission.LEVEL_ALL;
		final Boolean standardUserCanAccesUserId = ACCESS.getLevel(
				new ReadCalendarConfigurationPermission(userId)) >= ReadCalendarConfigurationPermission.LEVEL_INVOLVED;

		if (isAdmin) {
			if (null == userId && !displayAllForAdmin) {
				userId = super.userHelper.getCurrentUserId();
			}
		} else if (!standardUserCanAccesUserId) {
			userId = super.userHelper.getCurrentUserId();
		}

		if (!isAdmin && !displayAllForAdmin && null == userId) {
			LOG.warn(
					"Trying to get APIs of null userId from a non admin or not listing ALL CalendarConfiguration (as admin)");
			userId = super.userHelper.getCurrentUserId();
		}
		CalendarConfigurationTablePageData pageData = new CalendarConfigurationTablePageData();

		// String ownerFilter = "";
		// Long currentConnectedUserId = 0L;
		// if (!displayAllForAdmin || ACCESS.getLevel(new
		// CreateCalendarConfigurationPermission(
		// (Long) null)) != CreateCalendarConfigurationPermission.LEVEL_ALL) {
		// ownerFilter = SQLs.CALENDAR_CONFIG_FILTER_CURRENT_USER;
		// currentConnectedUserId = super.userHelper.getCurrentUserId();
		// }

		if (null == userId) {
			// load for admin. Cached does not handle null "key"
			pageData = this.loadForCacheByUserId(null);
		} else {
			pageData = this.getDataCacheByUser().get(userId);
		}
		// Filter user specific visibility on a COPY else cache is modified !
		final CalendarConfigurationTablePageData visiblePageData = new CalendarConfigurationTablePageData();

		if (pageData.getRowCount() > 0) {
			// Local cache to avoid multiple validation of same apiCredentialId
			final Map<Long, Boolean> alreadyCheckReadAcces = new HashMap<>();
			// Post check permission base on OAuthId
			for (final CalendarConfigurationTableRowData row : pageData.getRows()) {
				if (null == alreadyCheckReadAcces.get(row.getOAuthCredentialId())) {
					@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
					final Boolean canRead = ACCESS
							.check(new ReadCalendarConfigurationPermission(row.getOAuthCredentialId()));
					alreadyCheckReadAcces.put(row.getOAuthCredentialId(), canRead);
				}
				if (!alreadyCheckReadAcces.get(row.getOAuthCredentialId())) {
					LOG.warn("User : " + super.userHelper.getCurrentUserId() + " try to access CalendarConfig : "
							+ row.getCalendarConfigurationId() + " from apiId : " + row.getOAuthCredentialId()
							+ " belonging to user : " + row.getUserId()
							+ " but hasen't acces. Silently removing this (calendarConfig) row");
				} else {
					final CalendarConfigurationTableRowData visibleRow = visiblePageData.addRow();
					this.copyRow(row, visibleRow);
				}
			}
		}

		return visiblePageData;
	}

	private void copyRow(final CalendarConfigurationTableRowData source, final CalendarConfigurationTableRowData dest) {
		dest.setAddEventToCalendar(source.getAddEventToCalendar());
		dest.setCalendarConfigurationId(source.getCalendarConfigurationId());
		dest.setExternalId(source.getExternalId());
		dest.setMain(source.getMain());
		dest.setName(source.getName());
		dest.setOAuthCredentialId(source.getOAuthCredentialId());
		dest.setProcess(source.getProcess());
		dest.setProcessFreeEvent(source.getProcessFreeEvent());
		dest.setProcessFullDayEvent(source.getProcessFullDayEvent());
		dest.setProcessNotRegistredOnEvent(source.getProcessNotRegistredOnEvent());
		dest.setReadOnly(source.getReadOnly());
		dest.setRowState(source.getRowState());
		dest.setUserId(source.getUserId());
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
	public CalendarConfigurationTablePageData getCalendarConfiguration(final Long userId) {
		final SearchFilter filter = new SearchFilter();
		final CalendarConfigurationFormData calendarConfigurationSearchFilterForm = new CalendarConfigurationFormData();
		calendarConfigurationSearchFilterForm.getUserId().setValue(userId);
		filter.setFormData(calendarConfigurationSearchFilterForm);

		return this.getCalendarConfigurationTableData(filter, Boolean.FALSE);
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
		return this.create(formData, Boolean.TRUE);
	}

	private CalendarConfigurationFormData create(final CalendarConfigurationFormData formData,
			final Boolean sendNotification) {
		LOG.info(new StringBuffer().append("Creating new Calendar Configuration for API Id : ")
				.append(formData.getOAuthCredentialId().getValue()).append(" for calendar Key : ")
				.append(formData.getExternalId().getValue()).append(" for User ID : ")
				.append(formData.getUserId().getValue()).toString());
		if (!ACCESS.check(new CreateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()))) {
			super.throwAuthorizationFailed();
		}
		if (null == formData.getCalendarConfigurationId().getValue()) {
			formData.getCalendarConfigurationId().setValue(Long.valueOf(SQL.getSequenceNextval("EVENT_ID_SEQ")));
		}
		SQL.insert(SQLs.CALENDAR_CONFIG_INSERT, formData);

		if (sendNotification) {
			this.sendCreatedNotifications(formData);
		}

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
		return this.create(formData);
	}

	@Override
	public CalendarConfigurationFormData load(final CalendarConfigurationFormData formData) {
		CalendarConfigurationFormData cachedData = this.getDataCache()
				.get(formData.getCalendarConfigurationId().getValue());
		if (null == cachedData) {
			// avoid NPE
			cachedData = formData;
		}
		return cachedData;
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public CalendarsConfigurationFormData store(final CalendarsConfigurationFormData formData) {

		final CalendarConfigTableRowData[] rows = formData.getCalendarConfigTable().getRows();

		for (final CalendarConfigTableRowData row : rows) {
			this.checkPermission(new UpdateCalendarConfigurationPermission(row.getOAuthCredentialId()));
			SQL.insert(SQLs.CALENDAR_CONFIG_UPDATE + SQLs.GENERIC_WHERE_FOR_SECURE_AND + SQLs.CALENDAR_CONFIG_FILTER_ID
					+ SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID, row);

			this.dataCache.clearCache(row.getCalendarConfigurationId());
			this.dataCacheByUserId.clearCache(row.getUserId());
		}

		this.sendModifiedNotifications(formData);

		return formData;
	}

	@Override
	public CalendarConfigurationFormData store(final CalendarConfigurationFormData formData) {
		return this.store(formData, Boolean.FALSE);
	}

	private CalendarConfigurationFormData store(final CalendarConfigurationFormData formData,
			final Boolean duringCreate) {
		this.checkPermission(new UpdateCalendarConfigurationPermission(formData.getOAuthCredentialId().getValue()));
		LOG.info(new StringBuilder().append("Storing calendarConfiguration, duringCreate : ").append(duringCreate)
				.toString());
		SQL.insert(SQLs.CALENDAR_CONFIG_UPDATE + SQLs.GENERIC_WHERE_FOR_SECURE_AND + SQLs.CALENDAR_CONFIG_FILTER_ID
				+ SQLs.CALENDAR_CONFIG_FILTER_EXTERNAL_ID, formData);

		if (!duringCreate) {
			this.sendModifiedNotifications(formData);
		}

		this.dataCache.clearCache(formData.getCalendarConfigurationId().getValue());
		this.dataCacheByUserId.clearCache(formData.getUserId().getValue());

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

		LOG.info(new StringBuffer().append("Deleting calendars configuration with API ID : ").append(apiCredentialId)
				.toString());

		final int nbDeletedRows = SQL.delete(SQLs.CALENDAR_CONFIG_DELETE_BY_API_ID,
				new NVPair("oAuthCredentialId", apiCredentialId));
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuffer().append(nbDeletedRows)
					.append(" deleteted calendars configuration with API ID : ").append(apiCredentialId).toString());
		}
	}

	private void delete(final String externalId, final Long apiCredentialId, final Long userId) {
		// permission based on API permissions (not calendar permissions)
		this.checkPermission(new DeleteApiPermission(apiCredentialId));

		LOG.info(new StringBuilder().append("Deleting calendars configuration : ").append(externalId)
				.append(" for API ID : ").append(apiCredentialId).toString());

		final int nbDeletedRows = SQL.delete(SQLs.CALENDAR_CONFIG_DELETE_BY_EXTERNAL_ID,
				new NVPair("externalId", externalId), new NVPair("oAuthCredentialId", apiCredentialId));

		if (nbDeletedRows == 0) {
			LOG.warn(new StringBuffer().append("No row deleted calendars configuration : ").append(externalId)
					.append(" for API ID : ").append(apiCredentialId).toString());
		}

		this.dataCache.clearCache(apiCredentialId);
		this.dataCacheByUserId.clearCache(userId);
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void autoConfigure(final Map<String, AbstractCalendarConfigurationTableRowData> calendars) {
		LOG.info(new StringBuffer().append("Auto importing calendars for User with : ")
				.append(null == calendars ? "null" : calendars.size()).append(" calendars from provider").toString());
		Long lastUserId = null;
		Boolean atLeastOneCalendarConfigModified = Boolean.FALSE;
		Boolean atLeastOneCalendarConfigAdded = Boolean.FALSE;

		for (final String calendarKey : calendars.keySet()) {
			final AbstractCalendarConfigurationTableRowData calendarData = calendars.get(calendarKey);

			@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
			final CalendarConfigurationFormData data = new CalendarConfigurationFormData();
			data.getUserId().setValue(calendarData.getUserId());
			data.getExternalId().setValue(calendarData.getExternalId());
			data.getName().setValue(calendarData.getName());
			data.getProcess().setValue(calendarData.getMain());
			data.getMain().setValue(calendarData.getMain());
			data.getReadOnly().setValue(calendarData.getReadOnly());
			data.getAddEventToCalendar()
					.setValue(calendarData.getMain() && !this.hasCreateEventCalendar(calendarData.getUserId()));
			data.getOAuthCredentialId().setValue(calendarData.getOAuthCredentialId());
			data.getProcessFullDayEvent().setValue(Boolean.TRUE);
			data.getProcessFreeEvent().setValue(Boolean.FALSE);
			data.getProcessNotRegistredOnEvent().setValue(Boolean.FALSE);

			lastUserId = calendarData.getUserId();

			final Long existingCalendarConfigId = this.getCalendarConfigId(data);
			if (null == existingCalendarConfigId) {
				this.create(data, Boolean.FALSE);
				atLeastOneCalendarConfigAdded = Boolean.TRUE;
			} else {
				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final CalendarConfigurationFormData calendarConfigInput = new CalendarConfigurationFormData();

				LOG.info(new StringBuffer().append("Calendar Configuration already exists for API : ")
						.append(calendarData.getOAuthCredentialId()).append(" for calendarID : ")
						.append(existingCalendarConfigId).append(" key : ").append(calendarData.getExternalId())
						.append("(" + calendarData.getName()).append(") for User Id : ")
						.append(calendarData.getUserId()).append(" Checking for updates calendar Data").toString());

				calendarConfigInput.getCalendarConfigurationId().setValue(existingCalendarConfigId);
				calendarConfigInput.getUserId().setValue(calendarData.getUserId());
				final CalendarConfigurationFormData existingCalendarConfig = this.load(calendarConfigInput);
				if (this.isCalendarCondifgRequiredModification(calendarData, existingCalendarConfig)) {
					atLeastOneCalendarConfigModified = Boolean.TRUE;
					// override "provider specific" data
					existingCalendarConfig.getExternalId().setValue(calendarData.getExternalId());
					existingCalendarConfig.getName().setValue(calendarData.getName());
					existingCalendarConfig.getReadOnly().setValue(calendarData.getReadOnly());
					existingCalendarConfig.getOAuthCredentialId().setValue(calendarData.getOAuthCredentialId());
					if (calendarData.getReadOnly() && null != existingCalendarConfig.getAddEventToCalendar().getValue()
							&& existingCalendarConfig.getAddEventToCalendar().getValue()) {
						LOG.warn(new StringBuffer().append("The calendar ")
								.append(existingCalendarConfig.getCalendarConfigurationId()).append("(")
								.append(existingCalendarConfig.getName()).append(") for User ")
								.append(existingCalendarConfig.getUserId())
								.append(" is (became ?) readOnly and is configured to store event. Disabling addEventToCalendar for this calendar.")
								.toString());
						existingCalendarConfig.getAddEventToCalendar().setValue(Boolean.FALSE);
					}

					// simulate creation to avoid individual notifications
					this.store(existingCalendarConfig, Boolean.TRUE);

				} else {
					LOG.info(new StringBuffer().append("Calendar configuration : ")
							.append(existingCalendarConfigId + " (name : ")
							.append(existingCalendarConfig.getName().getValue())
							.append(") don't need to be save during sync").toString());
				}
			}
		}

		// FIXME Djer13 handle deletes calendars (already created in DB but
		// doesn't exist anymore in calendars Provider)
		final CalendarsConfigurationFormData configuredCalendars = this.getCalendarConfigurationTableData(false);

		if (null != configuredCalendars && null != configuredCalendars.getCalendarConfigTable()
				&& configuredCalendars.getCalendarConfigTable().getRowCount() > 0) {
			for (final CalendarConfigTableRowData calendar : configuredCalendars.getCalendarConfigTable().getRows()) {
				@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
				final StringBuilder calendarId = new StringBuilder();
				calendarId.append(calendar.getUserId()).append('_').append(calendar.getExternalId()).append('_')
						.append(calendar.getOAuthCredentialId());

				if (!calendars.keySet().contains(calendarId.toString())) {
					LOG.info(new StringBuffer().append("Calendar : ").append(calendar.getExternalId())
							.append(" does not exist anymore in provider data, removing it from user config")
							.toString());
					this.delete(calendar.getExternalId(), calendar.getOAuthCredentialId(), calendar.getUserId());
				}
			}
		}

		if (atLeastOneCalendarConfigAdded) {
			this.sendCreatedNotifications(lastUserId);
		}
		if (atLeastOneCalendarConfigModified) {
			this.sendModifiedNotifications(lastUserId);
		}
	}

	private Boolean hasCreateEventCalendar(final Long userId) {
		Boolean alreadyHasAddEventToCalendar = Boolean.FALSE;
		final CalendarConfigurationFormData currentUserCreateCalendar = this.getCalendarToStoreEvents(userId);
		if (null != currentUserCreateCalendar && null != currentUserCreateCalendar.getExternalId().getValue()) {
			alreadyHasAddEventToCalendar = Boolean.TRUE;
		}
		return alreadyHasAddEventToCalendar;
	}

	private boolean isCalendarCondifgRequiredModification(
			final AbstractCalendarConfigurationTableRowData newCalendarData,
			final CalendarConfigurationFormData existingCalendarConfig) {
		final Boolean externalIdChanged = !newCalendarData.getExternalId()
				.equals(existingCalendarConfig.getExternalId().getValue());
		final Boolean nameChanged = !newCalendarData.getName().equals(existingCalendarConfig.getName().getValue());
		final Boolean readOnlyChanged = !newCalendarData.getReadOnly()
				.equals(existingCalendarConfig.getReadOnly().getValue());
		final Boolean apiCredentialIdChanged = !newCalendarData.getOAuthCredentialId()
				.equals(existingCalendarConfig.getOAuthCredentialId().getValue());

		final Boolean requiredSave = externalIdChanged || nameChanged || readOnlyChanged || apiCredentialIdChanged;

		LOG.info(new StringBuilder().append("Calendar need change ? ").append(requiredSave)
				.append(" (externalIdChanged : ").append(externalIdChanged).append(", nameChanged : ")
				.append(nameChanged).append(" , readOnlyChanged : ").append(readOnlyChanged)
				.append(" , apiCredentialIdChanged : ").append(apiCredentialIdChanged).append(')').toString());

		return requiredSave;
	}

	private Long getCalendarConfigId(final CalendarConfigurationFormData formData) {
		Long calendarConfigId = null;
		final CalendarConfigurationTablePageData pageData = this.getDataCacheByUser()
				.get(formData.getUserId().getValue());

		if (pageData.getRowCount() > 0) {
			for (final CalendarConfigurationTableRowData calendarConfig : pageData.getRows()) {
				if (ACCESS.check(new ReadCalendarConfigurationPermission(calendarConfig.getOAuthCredentialId()))) {
					// externalId are NOT by User in Google (ex :
					// frenchHolydays), we NEED to use the OAuthID to get the
					// good one (if exists)
					if (calendarConfig.getExternalId().equals(formData.getExternalId().getValue()) && calendarConfig
							.getOAuthCredentialId().equals(formData.getOAuthCredentialId().getValue())) {
						calendarConfigId = calendarConfig.getCalendarConfigurationId();
						break;
					}
				} else {
					LOG.warn(new StringBuilder().append("Calendar configuration : ")
							.append(calendarConfig.getCalendarConfigurationId())
							.append(" ignored in configured list because current user don't have Read permission")
							.toString());
				}
			}
		}
		return calendarConfigId;
	}

	protected Long getCalendarIdToStoreEvents(final Long userId) {
		Long calendarConfigId = null;
		final CalendarConfigurationTablePageData pageData = this.getDataCacheByUser().get(userId);

		if (pageData.getRowCount() > 0) {
			for (final CalendarConfigurationTableRowData calendarConfig : pageData.getRows()) {
				if (ACCESS.check(new ReadCalendarConfigurationPermission(calendarConfig.getOAuthCredentialId()))) {
					if (calendarConfig.getAddEventToCalendar()) {
						calendarConfigId = calendarConfig.getCalendarConfigurationId();
						break;
					}
				} else {
					LOG.warn(new StringBuilder().append("Calendar configuration : ")
							.append(calendarConfig.getCalendarConfigurationId())
							.append(" ignored in configured list because current user don't have Read permission")
							.toString());
				}
			}
		}

		return calendarConfigId;
	}

	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Set<AbstractCalendarConfigurationTableRowData> getUsedCalendars(final Long userId) {
		final Set<AbstractCalendarConfigurationTableRowData> configuredCalendars = new HashSet<>();

		final CalendarConfigurationTablePageData pageData = this.getDataCacheByUser().get(userId);

		if (pageData.getRowCount() > 0) {
			for (final CalendarConfigurationTableRowData calendarConfig : pageData.getRows()) {
				if (ACCESS.check(new ReadCalendarConfigurationPermission(calendarConfig.getOAuthCredentialId()))) {
					if (calendarConfig.getProcess()) {
						configuredCalendars.add(calendarConfig);
					}
				} else {
					LOG.warn(new StringBuilder().append("Calendar configuration : ")
							.append(calendarConfig.getCalendarConfigurationId())
							.append(" ignored in configured list because current user don't have Read permission")
							.toString());
				}
			}
		}

		return configuredCalendars;
	}

	@Override
	public Long getCalendarApiId(final String calendarId, final Long calendarOwnerId) {
		Long calendarLinkedApiId = null;
		final Set<AbstractCalendarConfigurationTableRowData> userCalendars = this.getUsedCalendars(calendarOwnerId);

		if (null != userCalendars && userCalendars.size() > 0) {
			for (final AbstractCalendarConfigurationTableRowData calendarConfig : userCalendars) {
				if (calendarConfig.getExternalId().equals(calendarId)) {
					calendarLinkedApiId = calendarConfig.getOAuthCredentialId();
					break;
				}
			}
		}

		if (null == calendarLinkedApiId) {
			Integer nbUserCalendars = 0;
			if (null != userCalendars) {
				nbUserCalendars = userCalendars.size();
			}
			LOG.warn(new StringBuilder().append("No Api Id found for calendar ID : ").append(calendarId)
					.append(" for user ID : ").append(calendarOwnerId).append("(in ").append(nbUserCalendars)
					.append(" calendars)").toString());
		}

		return calendarLinkedApiId;
	}

	private void sendModifiedNotifications(final CalendarsConfigurationFormData formData) {
		if (formData.getCalendarConfigTable().getRowCount() > 0) {
			this.sendModifiedNotifications(formData.getCalendarConfigTable().getRows()[0].getUserId(), formData);
		} else {
			LOG.warn(
					"Cannot send user CalendarsConfigurationModifiedNotification because no User ID (no calendars modified)");
		}
	}

	private void sendModifiedNotifications(final Long ownerUserId) {
		final CalendarsConfigurationFormData formData = new CalendarsConfigurationFormData();
		formData.getCalendarConfigTable().addRow();
		formData.getCalendarConfigTable().getRows()[0].setUserId(ownerUserId);

		this.sendModifiedNotifications(ownerUserId, formData);
	}

	private void sendModifiedNotifications(final Long ownerUserId, final CalendarsConfigurationFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(ownerUserId, Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new CalendarsConfigurationModifiedNotification(formData));
	}

	private void sendModifiedNotifications(final CalendarConfigurationFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(formData.getUserId().getValue(), Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new CalendarConfigurationModifiedNotification(formData));
	}

	private void sendCreatedNotifications(final Long ownerUserId) {
		final CalendarsConfigurationFormData formData = new CalendarsConfigurationFormData();
		formData.getCalendarConfigTable().addRow();
		formData.getCalendarConfigTable().getRows()[0].setUserId(ownerUserId);

		this.sendCreatedNotifications(ownerUserId, formData);

	}

	private void sendCreatedNotifications(final Long ownerUserId, final CalendarsConfigurationFormData formData) {
		final Set<String> notifiedUsers = this.buildNotifiedUsers(ownerUserId, Boolean.TRUE);
		BEANS.get(ClientNotificationRegistry.class).putForUsers(notifiedUsers,
				new CalendarsConfigurationCreatedNotification(formData));
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
			LOG.error(new StringBuffer().append("apiCredentailId ").append(apiCredentailId)
					.append(" as NO owner (user_id) on his OAuth related data").toString());
			return false;
		} else if (apiCredentialOwner.equals(super.userHelper.getCurrentUserId())) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isInvolved(final Long apiCredentailId) {
		final Long apiCredentialOwner = this.getOwner(apiCredentailId);

		if (null == apiCredentialOwner) {
			LOG.error(new StringBuffer().append("apiCredentailId ").append(apiCredentailId)
					.append(" as NO owner (user_id) on his OAuth related data").toString());
			return false;
		}
		final Set<Long> relatedUserId = this.getUserWithPendingEvent();
		return relatedUserId.contains(apiCredentialOwner);
	}
}
