/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.zeroclick.meeting.server.sql;

import org.zeroclick.configuration.shared.role.IRoleTypeLookupService;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddEmailToApi;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddEventMinAndMaxDate;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddLastLogin;
import org.zeroclick.meeting.server.sql.migrate.data.PatchAddSlotCode;
import org.zeroclick.meeting.server.sql.migrate.data.PatchConfigureCalendar;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateParamsTable;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateSubscription;
import org.zeroclick.meeting.server.sql.migrate.data.PatchCreateVenue;
import org.zeroclick.meeting.server.sql.migrate.data.PatchEventAddCreatedDate;
import org.zeroclick.meeting.server.sql.migrate.data.PatchEventRejectReason;
import org.zeroclick.meeting.server.sql.migrate.data.PatchManageMicrosoftCalendars;
import org.zeroclick.meeting.server.sql.migrate.data.PatchSlotTable;
import org.zeroclick.meeting.shared.event.StateCodeType;

@SuppressWarnings("PMD.LongVariable")
public interface SQLs {

	String SELECT_TABLE_NAMES_DERBY = "SELECT UPPER(tablename) FROM sys.systables INTO :result";
	String SELECT_TABLE_NAMES_POSTGRESQL = "select UPPER(tables.table_name) from information_schema.tables INTO :result";

	String SELECT_COLUMN_EXISTS_POSTGRESQL = "SELECT column_name FROM information_schema.columns WHERE UPPER(table_name)=UPPER('__table__') and UPPER(column_name)=UPPER('__column__')";
	String GENERIC_REMOVE_COLUMN_POSTGRESQL = "ALTER TABLE UPPER(__table__) DROP COLUMN __column__";

	String GENERIC_DROP_TABLE = "DROP TABLE __tableName__ CASCADE";
	String GENERIC_CREATE_SEQUENCE = "CREATE SEQUENCE __seqName__ START __seqStart__";
	String GENERIC_DROP_SEQUENCE = "DROP SEQUENCE __seqName__";

	String GENERIC_SEQUENCE_EXISTS = "SELECT to_regclass('__seqName__')";

	String GENERIC_WHERE_FOR_SECURE_AND = " WHERE 1=1";

	String GENERIC_DROP_CONSTRAINT = "ALTER TABLE __table__ DROP CONSTRAINT __constraintName__";

	/**
	 * EVENT
	 */

	String EVENT_CREATE_TABLE = "CREATE TABLE EVENT (event_id INTEGER NOT NULL CONSTRAINT EVENT_PK PRIMARY KEY, organizer INTEGER, organizer_email VARCHAR(120), duration INTEGER, slot INTEGER, email VARCHAR(120), guest_id INTEGER, state VARCHAR(50), subject VARCHAR(250), startDate TIMESTAMP, endDate TIMESTAMP, externalIdRecipient VARCHAR(250), externalIdOrganizer VARCHAR(250))";

	String ORGANIZATION_LOOKUP = "SELECT   organization_id, name FROM ORGANIZATION "
			+ "WHERE 1=1<key> AND organization_id = :key</key> "
			+ "<text> AND UPPER(name) LIKE UPPER(:text||'%') </text> <all></all>";

	String AND_LIKE_CAUSE = "AND LOWER(%s) LIKE LOWER(:%s || '%%') ";

	String EVENT_PAGE_SELECT = "SELECT event_id, organizer, organizer_email, duration, slot, email, guest_id, state, reason, subject, venue, startDate, endDate, externalIdRecipient, externalIdOrganizer, "
			+ PatchEventAddCreatedDate.PATCHED_COLUMN + ", " + PatchAddEventMinAndMaxDate.PATCHED_ADDED_MIN_DATE_COLUMN
			+ ", " + PatchAddEventMinAndMaxDate.PATCHED_ADDED_MAX_DATE_COLUMN + " FROM EVENT WHERE 1=1";
	// + "CASE WHEN organizer = :currentUser THEN 1 ELSE 0 END AS held"
	// + "CASE WHEN email = :currentUserEmail THEN 1 ELSE 0 END AS guest FROM
	// EVENT WHERE 1=1";

	// String EVENT_PAGE_SELECT_FROM = " FROM EVENT WHERE 1=1";

	// String EVENT_PAGE_SELECT_ALL = EVENT_PAGE_SELECT + ", 0 as held, 0 as
	// guest";
	// String EVENT_PAGE_SELECT_HELD = EVENT_PAGE_SELECT + ", 1 as held, 0 as
	// guest";
	// String EVENT_PAGE_SELECT_GUEST = EVENT_PAGE_SELECT + ", 0 as held , 1 as
	// guest";

	String EVENT_PAGE_SELECT_FILTER_USER = " AND organizer = :currentUser";
	String EVENT_PAGE_SELECT_FILTER_RECIPIENT = " OR email = :currentUserEmail";
	String EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT = " AND (organizer = :currentUser OR guest_id = :currentUser)";

	String EVENT_PAGE_DATA_SELECT_INTO = " INTO :{page.eventId}, :{page.organizer}, :{page.organizerEmail}, :{page.duration}, :{page.slot}, :{page.email}, :{page.guestId}, :{page.state}, :{page.reason}, :{page.subject}, :{page.venue}, :{page.startDate}, :{page.endDate}, :{page.externalIdRecipient}, :{page.externalIdOrganizer}, :{page.createdDate}, :{page.minimalStartDate}, :{page.maximalStartDate}";

	String EVENT_SELECT_USERS_EVENT_GUEST = "SELECT event_id, organizer FROM EVENT WHERE guest_id=:currentUser";
	String EVENT_SELECT_USERS_EVENT_HOST = "SELECT event_id, guest_id FROM EVENT WHERE organizer=:currentUser";
	String EVENT_SELECT_FILTER_SATE = " AND state=:state";

	String EVENT_INSERT = "INSERT INTO EVENT (event_id, organizer) " + "VALUES (:eventId, :organizer)";

	String EVENT_UPDATE = "UPDATE EVENT SET organizer_email=:organizerEmail, duration=:duration, slot=:slot, email=:email, guest_id=:guestId, state=:state, subject=:subject, venue=:venue, startDate=:startDate, endDate=:endDate, externalIdRecipient=:externalIdRecipient, externalIdOrganizer=:externalIdOrganizer, "
			+ PatchEventAddCreatedDate.PATCHED_COLUMN + "=:createdDate, "
			+ PatchAddEventMinAndMaxDate.PATCHED_ADDED_MIN_DATE_COLUMN + "=:minimalStartDate, "
			+ PatchAddEventMinAndMaxDate.PATCHED_ADDED_MAX_DATE_COLUMN + "=:maximalStartDate WHERE event_id=:eventId";
	String EVENT_UPDATE_STATE = "UPDATE EVENT SET state=:state, reason=:reason WHERE event_id=:eventId";

	String EVENT_SELECT = "SELECT duration, slot, email, guest_id, state, reason, subject, venue, startDate, endDate, externalIdRecipient, externalIdOrganizer, organizer, organizer_email, "
			+ PatchEventAddCreatedDate.PATCHED_COLUMN + ", " + PatchAddEventMinAndMaxDate.PATCHED_ADDED_MIN_DATE_COLUMN
			+ ", " + PatchAddEventMinAndMaxDate.PATCHED_ADDED_MAX_DATE_COLUMN + " FROM EVENT WHERE event_id=:eventId"
			+ " INTO :duration, :slot, :email, :guestId, :state, :reason, :subject, :venue, :startDate, :endDate, :externalIdRecipient, :externalIdOrganizer, :organizer, :organizerEmail, :createdDate, :minimalStartDate, :maximalStartDate";

	String EVENT_SELECT_REJECT = "SELECT organizer_email, email, subject, venue, organizer, guest_id, externalIdOrganizer, externalIdRecipient FROM EVENT WHERE event_id=:eventId INTO :organizerEmail, :email, :subject, :venue, :organizer, :guestId, :externalIdOrganizer, :externalIdRecipient";

	String EVENT_SELECT_RECIPIENT = "SELECT email FROM EVENT WHERE event_id=:eventId INTO :email";
	String EVENT_SELECT_OWNER = "SELECT organizer FROM EVENT WHERE event_id=:eventId INTO :organizer";

	String EVENT_SELECT_KNOWN_ATTENDEE = "SELECT DISTINCT email FROM EVENT WHERE organizer=:currentUser AND email LIKE :searchEmail AND guest_id!=:currentUser";
	String EVENT_SELECT_KNOWN_HOST = "SELECT DISTINCT organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer_email LIKE ':searchEmail' AND organizer!=:currentUser";

	String EVENT_SELECT_KNOWN_ATTENDEE_LOOKUP = "SELECT DISTINCT email, email FROM EVENT WHERE organizer=:currentUser AND guest_id!=:currentUser <key> AND email=:key</key><text> AND email LIKE :text</text> <all></all>";
	String EVENT_SELECT_KNOWN_HOST_LOOKUP = "SELECT DISTINCT organizer_email, organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer!=:currentUser <key> AND organizer_email=:key</key><text> AND organizer_email LIKE :text</text> <all></all>";

	String EVENT_SELECT_VENUE_LOOKUP = "SELECT DISTINCT venue, venue FROM EVENT WHERE VENUE IS NOT NULL AND (organizer=:currentUser OR guest_id=:currentUser) <key> AND venue=:key</key><text> AND venue LIKE :text</text> <all></all>";

	String EVENT_SELECT_KNOWN_ATTENDEE_STRICT = "SELECT DISTINCT email FROM EVENT WHERE organizer=:currentUser AND email=:searchEmail";
	String EVENT_SELECT_KNOWN_HOST_STRICT = "SELECT DISTINCT organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer_email=:searchEmail";

	String EVENT_INSERT_SAMPLE = "INSERT INTO EVENT (event_id, organizer, organizer_email, duration, slot, email, guest_id, state, subject)";
	String EVENT_VALUES_01 = " VALUES  (nextval('EVENT_ID_SEQ'), 1, 'djer13@gmail.com', 15, 1, 'jeremie.deruette@gmail.com', 2, 'ASKED', 'Prendre le thé')";
	String EVENT_VALUES_02 = " VALUES  (nextval('EVENT_ID_SEQ'), 2,'jeremie.deruette@gmail.com', 120, 3, 'djer13@gmail.com', 1, 'ASKED', 'Do Something')";
	String EVENT_VALUES_03 = " VALUES  (nextval('EVENT_ID_SEQ'), 2,'jeremie.deruette@gmail.com', 120, 3, 'djer13@gmail.com', 1, 'REFUSED', 'Do Something else')";

	String EVENT_DROP_TABLE = "DROP TABLE EVENT CASCADE";

	String EVENT_ALTER_TABLE_ADD_REASON = "ALTER TABLE EVENT ADD COLUMN " + PatchEventRejectReason.PATCHED_COLUMN
			+ " VARCHAR(250)";

	String EVENT_ALTER_TABLE_ADD_VENUE = "ALTER TABLE EVENT ADD COLUMN " + PatchCreateVenue.EVENT_PATCHED_COLUMN
			+ " VARCHAR(250)";

	String EVENT_ALTER_TABLE_ADD_CREATED_DATE = "ALTER TABLE EVENT ADD COLUMN "
			+ PatchEventAddCreatedDate.PATCHED_COLUMN + " TIMESTAMP";

	String EVENT_ALTER_TABLE_ADD_MINIMAL_DATE = "ALTER TABLE EVENT ADD COLUMN "
			+ PatchAddEventMinAndMaxDate.PATCHED_ADDED_MIN_DATE_COLUMN + " TIMESTAMP";
	String EVENT_ALTER_TABLE_ADD_MAXIMAL_DATE = "ALTER TABLE EVENT ADD COLUMN "
			+ PatchAddEventMinAndMaxDate.PATCHED_ADDED_MAX_DATE_COLUMN + " TIMESTAMP";

	/**
	 * OAuth credential
	 */
	String OAUHTCREDENTIAL_CREATE_TABLE = "CREATE TABLE OAUHTCREDENTIAL (api_credential_id INTEGER NOT NULL CONSTRAINT OAUHTCREDENTIAL_PK PRIMARY KEY, user_id INTEGER, access_token VARCHAR(200), expiration_time_milliseconds BIGINT, refresh_token VARCHAR(200), provider INTEGER, repository_id VARCHAR(200), provider_data __blobType__)";

	String OAUHTCREDENTIAL_PAGE_SELECT = "SELECT api_credential_id, access_token, expiration_time_milliseconds, refresh_token, user_id, provider, "
			+ PatchAddEmailToApi.PATCHED_ADDED_COLUMN + ", "
			+ PatchManageMicrosoftCalendars.PATCHED_ADDED_COLUMN_TENANT_ID + " FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_PAGE_SELECT_FILTER_USER = " AND user_id = :currentUser";
	String OAUHTCREDENTIAL_PAGE_SELECT_FILTER_API_CREDENTIAL_ID = " AND api_credential_id = :apiCredentialId";
	String OAUHTCREDENTIAL_PAGE_DATA_SELECT_INTO = " INTO :{page.apiCredentialId}, :{page.accessToken}, :{page.expirationTimeMilliseconds}, :{page.refreshToken}, :{page.userId}, :{page.provider}, :{page.accountEmail}, :{page.tenantId}";

	String OAUHTCREDENTIAL_INSERT = "INSERT INTO OAUHTCREDENTIAL (api_credential_id, user_id) VALUES (:apiCredentialId, :userId)";

	String OAUHTCREDENTIAL_UPDATE_WITHOUT_ACCOUNT_EMAIL = "UPDATE OAUHTCREDENTIAL SET user_id=:userId,  access_token=:accessToken, expiration_time_milliseconds=:expirationTimeMilliseconds, refresh_token=:refreshToken, provider=:provider, repository_id=:repositoryId, provider_data=:providerData WHERE api_credential_id=:apiCredentialId";
	String OAUHTCREDENTIAL_UPDATE = "UPDATE OAUHTCREDENTIAL SET user_id=:userId,  access_token=:accessToken, expiration_time_milliseconds=:expirationTimeMilliseconds, refresh_token=:refreshToken, provider=:provider, repository_id=:repositoryId, provider_data=:providerData, account_email=:accountEmail WHERE api_credential_id=:apiCredentialId";

	String OAUHTCREDENTIAL_SELECT_OWNER = "SELECT user_id FROM OAUHTCREDENTIAL WHERE api_credential_id=:apiCredentialId INTO :userId";

	String OAUHTCREDENTIAL_SELECT = "SELECT api_credential_id, access_token, expiration_time_milliseconds, refresh_token, user_id, provider, repository_id, provider_data, "
			+ PatchAddEmailToApi.PATCHED_ADDED_COLUMN + ", "
			+ PatchManageMicrosoftCalendars.PATCHED_ADDED_COLUMN_TENANT_ID + " FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_SELECT_API_ID = "SELECT api_credential_id FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_SELECT_GOOGLE_DATA = "SELECT google_data FROM OAUHTCREDENTIAL WHERE provider=1";

	String OAUHTCREDENTIAL_SELECT_BY_ACCOUNT_EMAIL = "SELECT api_credential_id FROM OAUHTCREDENTIAL WHERE user_id=:userId AND account_email=:accountEmail";

	String OAUHTCREDENTIAL_SELECT_INTO = " INTO :apiCredentialId, :accessToken, :expirationTimeMilliseconds, :refreshToken, :userId, :provider, :repositoryId, :providerData, :accountEmail, :tenantId";
	String OAUHTCREDENTIAL_SELECT_INTO_API_ID = " INTO :apiCredentialId";

	String OAUHTCREDENTIAL_SELECT_PROVIDER_DATA_ONLY = "SELECT provider_data FROM OAUHTCREDENTIAL WHERE 1=1";

	String OAUHTCREDENTIAL_FILTER_OAUTH_ID = " AND api_credential_id = :apiCredentialId";
	String OAUHTCREDENTIAL_FILTER_USER_ID = " AND user_id= :userId";
	String OAUHTCREDENTIAL_FILTER_ACESS_TOKEN = " AND access_token= :accessToken";
	String OAUHTCREDENTIAL_FILTER_ACCOUNTS_EMAIL = " AND " + PatchAddEmailToApi.PATCHED_ADDED_COLUMN
			+ "= :accountEmail";

	String OAUHTCREDENTIAL_SELECT_ALL_USER_IDS = "select user_id FROM OAUHTCREDENTIAL";

	String OAUHTCREDENTIAL_LOOKUP = "SELECT DISTINCT api_credential_id, " + PatchAddEmailToApi.PATCHED_ADDED_COLUMN
			+ " FROM OAUHTCREDENTIAL WHERE 1=1 " + "<key>   AND api_credential_id = :key </key>" + " <text> AND UPPER("
			+ PatchAddEmailToApi.PATCHED_ADDED_COLUMN + ") LIKE UPPER('%'||:text||'%') </text> <all> </all>";

	String OAUHTCREDENTIAL_INSERT_SAMPLE_WITHOUT_ACCOUNT_EMAIL = "INSERT INTO OAUHTCREDENTIAL (api_credential_id, user_id, access_token, expiration_time_milliseconds, refresh_token, provider, repository_id, provider_data)";
	String OAUHTCREDENTIAL_INSERT_SAMPLE = "INSERT INTO OAUHTCREDENTIAL (api_credential_id, user_id, access_token, expiration_time_milliseconds, refresh_token, provider, repository_id, provider_data, "
			+ PatchAddEmailToApi.PATCHED_ADDED_COLUMN + ")";

	String OAUHTCREDENTIAL_VALUES_01 = " VALUES  (nextval('OAUHTCREDENTIAL_ID_SEQ'), 0, 'testAccessToken', 1514568455, 'testRefreshToken', 1, 'testRepo', null)";

	String OAUHTCREDENTIAL_DROP_TABLE = "DROP TABLE OAUHTCREDENTIAL";

	String OAUHTCREDENTIAL_DELETE = "DELETE FROM OAUHTCREDENTIAL where 1=1" + OAUHTCREDENTIAL_FILTER_OAUTH_ID;

	String OAUHTCREDENTIAL_PATCH_ADD_EMAIL_COLUMN = "ALTER TABLE " + PatchAddEmailToApi.PATCHED_TABLE + " ADD COLUMN "
			+ PatchAddEmailToApi.PATCHED_ADDED_COLUMN + " VARCHAR(120)";

	String OAUHTCREDENTIAL_PATCH_ALTER_ACCES_TOKEN_COLUMN_LENGHT = "ALTER TABLE "
			+ PatchManageMicrosoftCalendars.PATCHED_TABLE + " ALTER COLUMN "
			+ PatchManageMicrosoftCalendars.PATCHED_COLUMN + " TYPE VARCHAR(1500)";

	String OAUHTCREDENTIAL_PATCH_ALTER_REFRESH_TOKEN_COLUMN_LENGHT = "ALTER TABLE "
			+ PatchManageMicrosoftCalendars.PATCHED_TABLE + " ALTER COLUMN "
			+ PatchManageMicrosoftCalendars.PATCHED_COLUMN_REFRESH_TOKEN + " TYPE VARCHAR(1500)";

	String OAUHTCREDENTIAL_PATCH_ALTER_ADD_TENANT_ID_COLUMN = "ALTER TABLE "
			+ PatchManageMicrosoftCalendars.PATCHED_TABLE + " ADD COLUMN "
			+ PatchManageMicrosoftCalendars.PATCHED_ADDED_COLUMN_TENANT_ID + " VARCHAR(512)";

	/**
	 * Roles and permissions
	 */

	String ROLE_CREATE_TABLE = "CREATE TABLE ROLE (role_id INTEGER NOT NULL CONSTRAINT ROLE_PK PRIMARY KEY, name VARCHAR(100))";

	String ROLE_PAGE_SELECT = "SELECT role_id, name, type FROM ROLE";

	String ROLE_PAGE_DATA_SELECT_INTO = " INTO :{role.roleId}, :{role.roleName}, :{role.type}";

	String ROLE_INSERT_WITHOUT_TYPE = "INSERT INTO ROLE (role_id) VALUES (:roleId)";
	String ROLE_INSERT = "INSERT INTO ROLE (role_id, type) VALUES (:roleId, :type)";

	String ROLE_UPDATE = "UPDATE ROLE SET name=:roleName, type=:type WHERE role_id=:roleId";
	String ROLE_UPDATE_WITHOUT_TYPE = "UPDATE ROLE SET name=:roleName WHERE role_id=:roleId";

	String ROLE_SELECT = "SELECT role_id, name, type FROM ROLE WHERE 1=1 AND role_id = :roleId";
	String ROLE_SELECT_WITHOUT_TYPE = "SELECT role_id, name FROM ROLE WHERE 1=1 AND role_id = :roleId";
	String ROLE_SELECT_BY_NAME_WITHOUT_TYPE = "SELECT role_id, name FROM ROLE WHERE 1=1 AND name = :roleName";
	String ROLE_SELECT_BY_NAME = "SELECT role_id, name, type FROM ROLE WHERE 1=1 AND name = :roleName";
	String ROLE_SELECT_INTO_WITHOUT_TYPE = " INTO :roleId, :roleName";
	String ROLE_SELECT_INTO = " INTO :roleId, :roleName, :type";

	String ROLE_LOOKUP = "SELECT role_id, name FROM ROLE WHERE 1=1 <key>   AND role_id = :key </key>"
			+ " <text> AND UPPER(name) LIKE UPPER('%'||:text||'%') </text> <all> </all>";

	String ROLE_LOOKUP_WITHOUT_SUBSCRIPTION = "SELECT role_id, name FROM ROLE WHERE type <> '"
			+ IRoleTypeLookupService.TYPE_SUBSCRIPTION + "' <key> AND role_id = :key </key>"
			+ " <text> AND UPPER(name) LIKE UPPER('%'||:text||'%') </text> <all> </all>";

	String ROLE_LOOKUP_SUBSCRIPTION = "SELECT role_id, name FROM ROLE WHERE type = '"
			+ IRoleTypeLookupService.TYPE_SUBSCRIPTION + "' <key> AND role_id = :key </key>"
			+ " <text> AND UPPER(name) LIKE UPPER('%'||:text||'%') </text> <all> </all>";

	String ROLE_SELECT_TYPE_FOR_SMART_FIELD = "SELECT DISTINCT type, type FROM ROLE WHERE 1=1";
	String ROLE_SELECT_FILTER_LOOKUP_TYPE = " <key> AND type=:key</key><text> AND type LIKE :text</text> <all></all>";

	String ROLE_INSERT_NEW_LINKED_DOC = "INSERT INTO " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " (role_id, document_id, start_date) VALUES (:roleId, :documentId, :startDate)";
	String ROLE_UPDATE_LINKED_DOC = "UPDATE " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " SET start_date=:startDate where role_id=:roleId AND document_id=:documentId";

	String ROLE_DELETE_LINKED_DOC = "DELETE FROM " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " WHERE role_id=:roleId AND document_id=:documentId";

	String ROLE_DELETE_LINKED_DOC_BY_ROLE = "DELETE FROM " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " WHERE role_id=:roleId";

	String ROLE_INSERT_SAMPLE = "INSERT INTO ROLE (role_id, name)";
	String ROLE_INSERT_SAMPLE_WITH_TYPE = "INSERT INTO ROLE (role_id, name, type)";
	String ROLE_VALUES_01 = " VALUES(1, 'Administrator')";
	String ROLE_VALUES_02 = " VALUES(2, 'Standard')";

	String ROLE_VALUES_SUB_FREE = " VALUES(3, 'zc.user.role.free', '" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "')";
	String ROLE_VALUES_SUB_PRO = " VALUES(4, 'zc.user.role.pro', '" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "')";
	String ROLE_VALUES_SUB_BUSINESS = " VALUES(5, 'zc.user.role.business', '" + IRoleTypeLookupService.TYPE_SUBSCRIPTION
			+ "')";

	String ROLE_DELETE = "DELETE FROM ROLE WHERE role_id=:roleId";

	String ROLE_DROP_TABLE = "DROP TABLE ROLE CASCADE";

	String ROLE_ADD_TYPE = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_ROLE + " ADD COLUMN "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + " VARCHAR(100)";
	String ROLE_ADD_DEFAULT_TYPE_TO_EXISTING = "UPDATE " + PatchCreateSubscription.PATCHED_TABLE_ROLE + " SET "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + "='" + IRoleTypeLookupService.TYPE_BUSINESS + "' WHERE "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + " IS NULL";
	String ROLE_ALTER_TYPE_NOT_NULL = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_ROLE + " ALTER COLUMN "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + " SET NOT NULL";

	/**
	 * --- Permission
	 */

	// String PERMISSION_CREATE_TABLE = "CREATE TABLE PERMISSION (permission_id
	// INTEGER NOT NULL CONSTRAINT ROLE_PK PRIMARY KEY, name VARCHAR(100))";
	// String PERMISSION_SELECT = "SELECT permission_id, name FROM PERMISSION;";

	// String PERMISSION_DROP_TABLE = "DROP TABLE PERMISSION";

	/**
	 * --- Role Permission mapping
	 */

	String ROLE_PERMISSION_CREATE_TABLE = "CREATE TABLE ROLE_PERMISSION (role_id INTEGER NOT NULL, permission varchar(500) NOT NULL, level INTEGER NOT NULL, CONSTRAINT ROLE_PERMISSION_PK PRIMARY KEY (role_id, permission), "
			+ "CONSTRAINT ROLE_PERMISSION_ROLE_FK FOREIGN KEY (role_id) REFERENCES ROLE(role_id))";

	String ROLE_PERMISSION_PAGE_SELECT = "SELECT permission, level FROM ROLE_PERMISSION WHERE 1=1";

	String ROLE_PERMISSION_PAGE_DATA_SELECT_INTO = " INTO :{rolePermission.permissionName}, :{rolePermission.level}";

	String ROLE_PERMISSION_FILTER_ROLE = " AND role_id = :{role.roleId}";

	String ROLE_PERMISSION_INSERT = "INSERT INTO ROLE_PERMISSION (role_id, permission, level) VALUES (:roleId, :{permission}, :level)";

	String ROLE_PERMISSION_DELETE = "DELETE FROM ROLE_PERMISSION WHERE role_id = :roleId AND permission = :{permissions}";
	String ROLE_PERMISSION_DELETE_BY_ROLE = "DELETE FROM ROLE_PERMISSION WHERE role_id = :roleId";

	String ROLE_GENERIC_VALUES_ADD = " VALUES(__roleId__, '__permissionName__', __level__)";

	String ROLE_PERMISSION_INSERT_SAMPLE = "INSERT INTO ROLE_PERMISSION (role_id, permission, level)";
	// -- Admin Role
	String ROLE_PERMISSION_VALUES_01 = " VALUES(1, 'org.zeroclick.meeting.shared.calendar.CreateApiPermission', 100)";
	String ROLE_PERMISSION_VALUES_02 = " VALUES(1, 'org.zeroclick.configuration.shared.role.CreateAssignToRolePermission', 100)";
	String ROLE_PERMISSION_VALUES_04 = " VALUES(1, 'org.zeroclick.meeting.shared.event.CreateEventPermission', 100)";
	String ROLE_PERMISSION_VALUES_05 = " VALUES(1, 'org.zeroclick.configuration.shared.role.CreatePermissionPermission', 100)";
	String ROLE_PERMISSION_VALUES_06 = " VALUES(1, 'org.zeroclick.configuration.shared.role.CreateRolePermission', 100)";
	String ROLE_PERMISSION_VALUES_07 = " VALUES(1, 'org.zeroclick.meeting.shared.calendar.DeleteApiPermission', 100)";
	String ROLE_PERMISSION_VALUES_08 = " VALUES(1, 'org.zeroclick.meeting.shared.calendar.UpdateApiPermission', 100)";
	String ROLE_PERMISSION_VALUES_09 = " VALUES(1, 'org.zeroclick.configuration.shared.role.UpdateAssignToRolePermission', 100)";
	String ROLE_PERMISSION_VALUES_11 = " VALUES(1, 'org.zeroclick.meeting.shared.event.UpdateEventPermission', 100)";
	String ROLE_PERMISSION_VALUES_12 = " VALUES(1, 'org.zeroclick.configuration.shared.role.UpdatePermissionPermission', 100)";
	String ROLE_PERMISSION_VALUES_13 = " VALUES(1, 'org.zeroclick.configuration.shared.role.UpdateRolePermission', 100)";
	String ROLE_PERMISSION_VALUES_14 = " VALUES(1, 'org.zeroclick.meeting.shared.calendar.ReadApiPermission', 100)";
	String ROLE_PERMISSION_VALUES_17 = " VALUES(1, 'org.zeroclick.meeting.shared.event.ReadEventPermission', 100)";
	String ROLE_PERMISSION_VALUES_18 = " VALUES(1, 'org.zeroclick.configuration.shared.role.ReadPermissionPermission', 100)";
	String ROLE_PERMISSION_VALUES_19 = " VALUES(1, 'org.zeroclick.configuration.shared.role.ReadRolePermission', 100)";
	String ROLE_PERMISSION_VALUES_20 = " VALUES(1, 'org.zeroclick.configuration.shared.user.CreateUserPermission', 100)";
	String ROLE_PERMISSION_VALUES_21 = " VALUES(1, 'org.zeroclick.configuration.shared.user.ReadUserPermission', 100)";
	String ROLE_PERMISSION_VALUES_22 = " VALUES(1, 'org.zeroclick.configuration.shared.user.UpdateUserPermission', 100)";
	String ROLE_PERMISSION_VALUES_23 = " VALUES(1, 'org.zeroclick.meeting.shared.event.ReadEventExtendedPropsPermission', 100)";

	// -- Standard Role
	String ROLE_PERMISSION_VALUES_100 = " VALUES(2, 'org.zeroclick.meeting.shared.calendar.CreateApiPermission', 100)";
	String ROLE_PERMISSION_VALUES_101 = " VALUES(2, 'org.zeroclick.meeting.shared.event.CreateEventPermission', 100)";
	String ROLE_PERMISSION_VALUES_102 = " VALUES(2, 'org.zeroclick.meeting.shared.calendar.DeleteApiPermission', 10)";
	String ROLE_PERMISSION_VALUES_103 = " VALUES(2, 'org.zeroclick.meeting.shared.calendar.UpdateApiPermission', 15)";
	String ROLE_PERMISSION_VALUES_104 = " VALUES(2, 'org.zeroclick.meeting.shared.event.UpdateEventPermission', 10)";
	String ROLE_PERMISSION_VALUES_105 = " VALUES(2, 'org.zeroclick.meeting.shared.calendar.ReadApiPermission', 15)";
	String ROLE_PERMISSION_VALUES_106 = " VALUES(2, 'org.zeroclick.meeting.shared.event.ReadEventPermission', 10)";
	String ROLE_PERMISSION_VALUES_107 = " VALUES(2, 'org.zeroclick.configuration.shared.user.ReadUserPermission', 10)";
	String ROLE_PERMISSION_VALUES_108 = " VALUES(2, 'org.zeroclick.configuration.shared.user.UpdateUserPermission', 10)";

	String ROLE_PERMISSION_DROP_TABLE = "DROP TABLE ROLE_PERMISSION CASCADE";

	String ROLE_PERMISSION_CHANGE_EVENT_TO_HIERARCHIC = "UPDATE ROLE_PERMISSION SET level=0 WHERE permission='org.zeroclick.meeting.shared.event.CreateEventPermission'";

	/**
	 * User Role mapping
	 */
	String USER_ROLE_CREATE_TABLE = "CREATE TABLE USER_ROLE (user_id INTEGER NOT NULL, role_id INTEGER NOT NULL, CONSTRAINT USER_ROLE_PK PRIMARY KEY (user_id, role_id))";

	String USER_ROLE_SELECT = "SELECT user_id, role_id FROM USER_ROLE WHERE 1=1";
	String USER_ROLE_SELECT_ROLE_ID = "SELECT USER_ROLE.role_id FROM USER_ROLE INNER JOIN ROLE on USER_ROLE.role_id = ROLE.role_id WHERE "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + " NOT IN('" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "')";
	String USER_ROLE_SELECT_FILTER_ROLE_ID = " AND role_id=:roleId";
	String USER_ROLE_SELECT_FILTER_USER = " AND USER_ROLE.user_id=:userId";

	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_ROLE_ID_FILED = "SELECT USER_ROLE.role_id";
	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_ALL_FILED = "SELECT USER_ROLE.role_id, USER_ROLE.user_id, USER_ROLE.start_date, "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".accepted_CPS_date, "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".accepted_withdrawal_date";
	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FROM = " FROM USER_ROLE INNER JOIN ROLE ON USER_ROLE.role_id = ROLE.role_id";
	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FILTER = " WHERE " + " USER_ROLE.user_id=:userId AND "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + "='" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "'"
			+ " AND USER_ROLE." + PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + "="
			+ "(SELECT MAX(start_date) FROM USER_ROLE INNER JOIN ROLE ON USER_ROLE.role_id = ROLE.role_id WHERE "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + "='" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "'"
			+ " AND " + PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + " <= NOW() AND user_id=:userId" + " GROUP BY  "
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + ")";

	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FROM_ADD_METADATA = " LEFT OUTER JOIN "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + " ON USER_ROLE.role_id="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".role_id AND USER_ROLE.user_id="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".user_id AND USER_ROLE.start_date="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".start_date";

	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION = USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_ROLE_ID_FILED
			+ USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FROM + USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FILTER;

	String USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_DETAILS = USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_ALL_FILED
			+ USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FROM + USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FROM_ADD_METADATA
			+ USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION_FILTER;

	String USER_ROLE_SELECT_ALL = "SELECT user_id, role_id, start_date FROM USER_ROLE WHERE 1=1";

	String USER_ROLE_INSERT = "INSERT INTO USER_ROLE (user_id, role_id) VALUES (:userId, :{rolesBox})";
	String USER_ROLE_INSERT_WITH_START_DATE = "INSERT INTO USER_ROLE (user_id, role_id, start_date) VALUES (:userId, :{rolesBox}, :{startDate})";

	String USER_ROLE_UPDATE_MASS_CHANGE_ROLE_ID = "UPDATE USER_ROLE SET role_id=:newRoleId WHERE role_id=:oldRoleId";

	// Avoid deleting role of kind "subscription" !!

	String USER_ROLE_REMOVE = "DELETE FROM USER_ROLE WHERE user_id=:userId AND role_id=:{rolesBox}";
	String USER_ROLE_REMOVE_BY_ROLE = "DELETE FROM USER_ROLE WHERE role_id=:{roleId}";
	String USER_ROLE_REMOVE_BY_USER = "DELETE FROM USER_ROLE WHERE user_id=:{userId}";

	String USER_ROLE_INSERT_SAMPLE = "INSERT INTO USER_ROLE (user_id, role_id)";
	String USER_ROLE_VALUES_01 = " VALUES(1, 1)";
	String USER_ROLE_VALUES_02 = " VALUES(2, 2)";

	String USER_ROLE_DROP_TABLE = "DROP TABLE USER_ROLE";

	String USER_ROLE_ADD_START_DATE = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_USER_ROLE + " ADD COLUMN "
			+ PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + " TIMESTAMP";
	String USER_ROLE_START_DATE_NOW_TO_EXISTING = "UPDATE " + PatchCreateSubscription.PATCHED_TABLE_USER_ROLE + " set "
			+ PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + "=now() WHERE "
			+ PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + " IS NULL";
	String USER_ROLE_START_DATE_ADD_DEFAULT = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
			+ "  ALTER COLUMN start_date SET DEFAULT now()";
	String USER_ROLE_UPDATE_START_DATE_BEFORE_NEW_PK = "UPDATE USER_ROLE set start_date=:startDate where user_id=:userId AND role_id=:roleId";

	String USER_ROLE_PK_DROP = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
			+ " DROP CONSTRAINT USER_ROLE_PK";
	String USER_ROLE_PK_ADD_START_DATE = "ALTER TABLE " + PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
			+ " ADD CONSTRAINT USER_ROLE_PK PRIMARY KEY (user_id, role_id, "
			+ PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + ")";

	// String USER_ROLE_START_DATE_ADD_UNIQUE = "ALTER TABLE " +
	// PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
	// + " ADD CONSTRAINT USER_ROLE_START_DATE UNIQUE (" +
	// PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + ")";
	// String USER_ROLE_ROLE_ID_ADD_FK = "ALTER TABLE " +
	// PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
	// + " ADD CONSTRAINT USER_ROLE_ROLE_ID_FK FOREIGN KEY (role_id) REFERENCES
	// ROLE(role_id)";
	// String USER_ROLE_USER_ID_ADD_FK = "ALTER TABLE " +
	// PatchCreateSubscription.PATCHED_TABLE_USER_ROLE
	// + " ADD CONSTRAINT USER_ROLE_USER_ID FOREIGN KEY (user_id) REFERENCES
	// APP_USER(user_id)";

	/**
	 * Users permissions
	 */
	String USER_PERMISSIONS_SELECT_ACTIVE_ROLE_BEFORE_SUB_PATCH = "SELECT P.permission, MAX(P.level) FROM ROLE_PERMISSION P INNER JOIN USER_ROLE UR ON P.role_id = UR.role_id INNER JOIN ROLE R on UR.role_id = R.role_id WHERE 1=1";
	String USER_PERMISSIONS_SELECT_ACTIVE_ROLE = "SELECT P.permission, MAX(P.level) FROM ROLE_PERMISSION P INNER JOIN USER_ROLE UR ON P.role_id = UR.role_id INNER JOIN ROLE R on UR.role_id = R.role_id WHERE ("
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + "!='" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "'"
			+ " OR UR.role_id IN (" + USER_ROLE_SELECT_ACTIVE_SUBSCRIPTION + "))";
	String USER_PERMISSIONS_SELECT_FILTER_USER_ID = " AND UR.user_id = :userId";
	String USER_PERMISSIONS_SELECT_GROUP_BY = " GROUP BY P.permission";

	/**
	 * Users
	 */
	String USER_CREATE_TABLE = "CREATE TABLE APP_USER (user_id INTEGER NOT NULL, login VARCHAR(50), email VARCHAR(120), password VARCHAR(256), time_zone VARCHAR(120), CONSTRAINT USER_PK PRIMARY KEY (user_id), CONSTRAINT USER_UNIQUE_EMAIL UNIQUE (email))";

	String USER_FROM = " FROM APP_USER";

	String USER_PAGE_SELECT = "select user_id, login, email, time_zone, invited_by, language, "
			+ PatchAddLastLogin.PATCHED_ADDED_COLUMN;
	String USER_PAGE_DATA_SELECT_INTO = " INTO :{page.userId}, :{page.login}, :{page.email}, :{page.timeZone}, :{page.invitedBy}, :{page.language},  :{page.lastLogin}";

	String USER_PAGE_ADD_STATS_SELECT = ", nbOrganizedWaitingEvent, nbInvetedWaitingEvent";
	String USER_PAGE_ADD_STATS = " left outer join (select organizer, count(event_id) as nbOrganizedWaitingEvent FROM EVENT WHERE state='"
			+ StateCodeType.AskedCode.ID + "' GROUP BY organizer) as stat2 ON stat2.organizer=user_id"
			+ " left outer join (select guest_id, count(event_id) as nbInvetedWaitingEvent FROM EVENT WHERE state='"
			+ StateCodeType.AskedCode.ID + "' GROUP BY guest_id) as stat3 ON stat3.guest_id=user_id";
	String USER_PAGE_ADD_STATS_INTO = ", :{page.NbOrganizedEventWaiting}, :{page.NbInvitedEventWaiting}";

	String USER_STATS_NB_PROCESSED_EVENT = "select count(event_id) FROM EVENT WHERE organizer=:userId OR guest_id=:userId INTO :{nbProcessedEvent}";

	String USER_SELECT = "SELECT user_id, login, email, password, time_zone, invited_by, language FROM APP_USER WHERE 1=1";
	String USER_SELECT_ID_ONLY = "SELECT user_id FROM APP_USER WHERE 1=1";

	String USER_SELECT_FILTER_ID = " AND user_id=:currentUser";
	String USER_SELECT_FILTER_EMAIL = " AND email=:email";
	String USER_SELECT_FILTER_LOGIN = " AND login=:login";
	String USER_SELECT_INTO = " INTO :userId, :login, :email, :hashedPassword, :timeZone, :invitedBy, :language";
	String USER_SELECT_INTO_ID_ONLY = " INTO :userId";

	String USER_SELECT_INTO_ROLES = " INTO :{rolesBox}";
	String USER_SELECT_INTO_SUBSCRIPTION = " INTO :subscriptionBox";

	String USER_SELECT_PASSWORD_FILTER_LOGIN = "select user_id, password FROM APP_USER where login=:login INTO :userId, :password";
	String USER_SELECT_PASSWORD_FILTER_EMAIL = "select user_id, password FROM APP_USER where email=:email INTO :userId, :password";

	String USER_INSERT = "INSERT INTO APP_USER (user_id) VALUES (:userId)";

	String USER_UPDATE_LAST_LOGIN = "UPDATE APP_USER set " + PatchAddLastLogin.PATCHED_ADDED_COLUMN
			+ "=:currentDate WHERE user_id=:userId";

	String USER_ALTER_TABLE_INVITED_BY = "ALTER TABLE APP_USER ADD COLUMN invited_by INTEGER";
	String USER_ALTER_TABLE_INVITED_BY_CONSTRAINT = "ALTER TABLE APP_USER ADD CONSTRAINT FK_INVITED_BY FOREIGN KEY (invited_by) REFERENCES APP_USER(user_id)";
	String USER_ALTER_TABLE_REMOVE_INVITED_BY = "ALTER TABLE APP_USER DROP COLUMN invited_by";

	String USER_ALTER_TABLE_LANGUAGE = "ALTER TABLE APP_USER ADD COLUMN language VARCHAR(5)";
	String USER_ALTER_TABLE_REMOVE_LANGUAGE = "ALTER TABLE APP_USER DROP COLUMN language";

	String USER_ALTER_TABLE_ADD_LAST_LOGIN = "ALTER TABLE APP_USER ADD COLUMN last_login TIMESTAMP";

	/**
	 * Password not updated use USER_UPDATE_PASSWORD
	 */
	String USER_UPDATE = "UPDATE APP_USER SET email=:email, login=:login, time_zone=:timeZone, invited_by=:invitedBy, language=:language WHERE user_id=:userId ";
	String USER_UPDATE_ONBOARDING = "UPDATE APP_USER SET login=:login, time_zone=:timeZone, language=:language WHERE user_id=:userId ";
	String USER_UPDATE_PASSWORD = "UPDATE APP_USER SET password=:hashedPassword WHERE user_id=:userId";
	String USER_UPDATE_INVITED_BY_BY_USER = "UPDATE APP_USER SET invited_by=:invitedBy WHERE invited_by=:userId";

	String USER_INSERT_SAMPLE = "INSERT INTO APP_USER (user_id, login, email, time_zone, password)";
	String USER_VALUES_01 = " VALUES(nextval('USER_ID_SEQ'), 'djer13', 'djer13@gmail.com', 'Europe/Paris', 'kv6kmSYn4jnCyoQK/4cQjA==.7bXNiiq6QcbGKFge/UdQ7T5cFud69Wp+qRBGZLnMU8VZ3UMgFuWtb/BpVsFBlpUSfYBd8t06uOkmHAliGKisOA==')"; // Djer13
	String USER_VALUES_02 = " VALUES(nextval('USER_ID_SEQ'), 'jeremie', 'jeremie.deruette@gmail.com', null, 'I/ocgG3Cp6QhLzIkrmYOQg==.GIxlDVNe8rl4r8WnnhT197qSBWaQIRvKnn4lNt6dqVWJ/aHDBCyxltXCNuWjYyyaynI34FM5x9Uz4hBBWMjYZw==')"; // Bob001

	String USER_DELETE = "DELETE FROM APP_USER WHERE user_id=:userId";

	String USER_CREATE_DROP = "CREATE TABLE APP_USE CASCADE";

	/**
	 * App params table
	 */

	String PARAMS_CREATE_TABLE = "CREATE TABLE APP_PARAMS (param_id INTEGER NOT NULL, key VARCHAR(150), value VARCHAR(250), CONSTRAINT APP_PARAMS_PK PRIMARY KEY (param_id), CONSTRAINT APP_PARAMS_UNIQUE_KEY UNIQUE (key))";

	String PARAMS_PAGE_SELECT = "select param_id, key, value, " + PatchCreateVenue.APP_PARAMS_PATCHED_COLUMN
			+ " FROM APP_PARAMS WHERE 1=1";
	String PARAMS_PAGE_DATA_SELECT_INTO = " INTO :{page.paramId}, :{page.key}, :{page.value}, :{page.category}";

	String PARAMS_SELECT = "SELECT param_id, key, value FROM APP_PARAMS WHERE 1=1";
	String PARAMS_SELECT_WITH_CATEGORY = "SELECT param_id, key, category, value FROM APP_PARAMS WHERE 1=1";
	String PARAMS_SELECT_FOR_SMART_FIELD = "SELECT key, value FROM APP_PARAMS WHERE 1=1";
	String PARAMS_SELECT_CATEGORY_FOR_SMART_FIELD = "SELECT DISTINCT category, category FROM APP_PARAMS WHERE 1=1";
	String PARAMS_SELECT_FILTER_KEY = " AND key=:key";
	String PARAMS_SELECT_FILTER_ID = " AND param_id=:paramId";
	String PARAMS_SELECT_FILTER_CATEGORY = " AND category=:category";
	String PARAMS_SELECT_FILTER_LOOKUP = " <key> AND key=:key</key><text> AND value LIKE :text</text> <all></all>";
	String PARAMS_SELECT_FILTER_LOOKUP_CATEGORY = " <key> AND category=:key</key><text> AND category LIKE :text</text> <all></all>";

	String PARAMS_SELECT_INTO_WITH_CATEGORY = " INTO :paramId, :key, :category, :value";
	String PARAMS_SELECT_INTO = " INTO :paramId, :key, :value";

	String PARAMS_INSERT = "INSERT INTO APP_PARAMS (param_id) VALUES (:paramId)";

	String PARAMS_UPDATE = "UPDATE APP_PARAMS SET key=:key, value=:value WHERE param_id=:paramId";
	String PARAMS_UPDATE_WITH_CATEGORY = "UPDATE APP_PARAMS SET key=:key, category=:category, value=:value WHERE param_id=:paramId";

	String PARAMS_INSERT_SAMPLE = "INSERT INTO APP_PARAMS (param_id, key, value)";
	String PARAMS_INSERT_SAMPLE_WITH_CATEGORY = "INSERT INTO APP_PARAMS (param_id, key, category, value)";
	String PARAMS_INSERT_VALUES_DATAVERSION = " VALUES(nextval('" + PatchCreateParamsTable.APP_PARAMS_ID_SEQ
			+ "'), 'dataVersion', '1.0.0')";

	String PARAMS_DELETE = "DELETE FROM APP_PARAMS WHERE param_id=:paramId";

	String PARAMS_DROP_TABLE = "DROP TABLE APP_PARAMS";

	String PARAMS_ALTER_TABLE_ADD_CATEGORY = "ALTER TABLE APP_PARAMS ADD COLUMN "
			+ PatchCreateVenue.APP_PARAMS_PATCHED_COLUMN + " VARCHAR(100)";
	String PARAMS_INSERT_VALUES_SKYPE = " VALUES(nextval('" + PatchCreateParamsTable.APP_PARAMS_ID_SEQ
			+ "'), 'zc.meeting.venue.skype', 'venue', 'zc.meeting.venue.skype')";
	String PARAMS_INSERT_VALUES_PHONE = " VALUES(nextval('" + PatchCreateParamsTable.APP_PARAMS_ID_SEQ
			+ "'), 'zc.meeting.venue.phone', 'venue', 'zc.meeting.venue.phone')";

	// String PARAMS_INSERT_VALUES_SUB_FREE_EVENT_LIMIT = "
	// VALUES(nextval('APP_PARAMS_ID_SEQ'), 'subFreeEventLimit', 'subscription',
	// 10)";
	// String PARAMS_INSERT_VALUES_SUB_INFO_EMAIL = "
	// VALUES(nextval('APP_PARAMS_ID_SEQ'), 'subInfoEmail', 'subscription',
	// 'djer13@gmail.com')";

	/**
	 * Slot (and dayDuration) table
	 */
	String SLOT_CREATE_TABLE = "CREATE TABLE SLOT (slot_id INTEGER NOT NULL, name VARCHAR(50), user_id INTEGER, CONSTRAINT SLOT_PK PRIMARY KEY (slot_id)"
			+ ", CONSTRAINT SLOT_USER_FK FOREIGN KEY (user_id) REFERENCES APP_USER(user_id))";
	String DAY_DURATION_CREATE_TABLE = "CREATE TABLE DAY_DURATION (day_duration_id INTEGER NOT NULL, name VARCHAR(50), slot_start TIME, slot_end TIME"
			+ ", monday BOOLEAN, tuesday BOOLEAN, wednesday BOOLEAN, thursday BOOLEAN, friday BOOLEAN, saturday BOOLEAN, sunday BOOLEAN"
			+ ", weekly_perpetual BOOLEAN, order_in_slot INTEGER NOT NULL, slot_id INTEGER,"
			+ " CONSTRAINT DAY_DURATION_PK PRIMARY KEY (day_duration_id), CONSTRAINT DAY_DURATION_SLOT_FK FOREIGN KEY (slot_id) REFERENCES SLOT(slot_id))";

	String SLOT_SELECT_FILEDS = "SLOT.slot_id, SLOT.name, false, SLOT.user_id";
	String SLOT_SELECT = "SELECT " + SLOT_SELECT_FILEDS + " FROM SLOT WHERE 1=1";
	String SLOT_SELECT_FILTER_USER_ID = " AND user_id=:currentUser";
	String SLOT_SELECT_FILTER_SLOT_ID = " AND slot_id=:slotId";
	String SLOT_SELECT_INTO = " INTO :slotId, :name, :isDefault, :userId";

	String SLOT_PAGE_SELECT = "SELECT " + SLOT_SELECT_FILEDS + " FROM SLOT WHERE 1=1";
	String SLOT_PAGE_SELECT_INTO = " INTO :{page.slotId}, :{page.name}, :{page.userId}, :{page.default}";

	String SLOT_SELECT_OWNER = "SELECT user_id FROM SLOT WHERE slot_id=:slotId";
	String SLOT_SELECT_ID_BY_NAME = "SELECT SLOT.slot_id FROM SLOT WHERE SLOT.name=:slotName AND SLOT.user_id=:userId";

	String SLOT_INSERT_SAMPLE = "INSERT INTO SLOT (slot_id, name,  user_id)";
	String SLOT_VALUES_GENERIC = " VALUES (__slotId__, '__slotName__',  __userId__)";

	String SLOT_INSERT_SAMPLE_WITH_CODE = "INSERT INTO SLOT (slot_id, name, slot_code, user_id)";
	String SLOT_VALUES_GENERIC_WITH_CODE = " VALUES (__slotId__, '__slotName__', __slotCode__, __userId__)";
	// String SLOT_VALUES_DAY = " VALUES (nextval('" +
	// PatchSlotTable.SLOT_ID_SEQ + "'), 'zc.meeting.slot.1', 1)";
	// String SLOT_VALUES_LUNCH = " VALUES (nextval('" +
	// PatchSlotTable.SLOT_ID_SEQ + "'), 'zc.meeting.slot.2', 1)";
	// String SLOT_VALUES_EVENING = " VALUES (nextval('" +
	// PatchSlotTable.SLOT_ID_SEQ + "'), 'zc.meeting.slot.3', 1)";
	// String SLOT_VALUES_WEEK_END = " VALUES (nextval('" +
	// PatchSlotTable.SLOT_ID_SEQ + "'), 'zc.meeting.slot.4', 1)";

	String DAY_DURATION_PAGE_SELECT = "SELECT DAY_DURATION.day_duration_id, DAY_DURATION.name, slot_start, slot_end, SLOT.slot_code, SLOT.slot_id, order_in_slot, SLOT.user_id, "
			+ "monday, tuesday, wednesday, thursday, friday, saturday, sunday, weekly_perpetual FROM DAY_DURATION INNER JOIN SLOT ON DAY_DURATION.slot_id = SLOT.slot_id";
	String DAY_DURATION_PAGE_SELECT_INTO = " INTO :{page.dayDurationId}, :{page.name}, :{page.slotStart}, :{page.slotEnd}, :{page.slot}, :{page.slotId}, :{page.orderInSlot}, :{page.userId}, :{page.monday}, :{page.tuesday}, :{page.wednesday}, :{page.thursday}, :{page.friday}, :{page.saturday}, :{page.sunday}, :{page.weeklyPerpetual}";

	String DAY_DURATION_SELECT = "SELECT DAY_DURATION.day_duration_id, DAY_DURATION.name, slot_start, slot_end, "
			+ "monday, tuesday, wednesday, thursday, friday, saturday, sunday, weekly_perpetual, order_in_slot, DAY_DURATION.slot_id, SLOT.slot_code";
	String DAY_DURATION_SELECT_LIGHT = "SELECT day_duration_id, name, slot_id FROM DAY_DURATION WHERE 1=1";
	String DAY_DURATION_SELECT_SLOT_USER_ID = "SLOT.user_id";
	String DAY_DURATION_SELECT_FILTER_SLOT_ID = " AND slot_id=:slotId";
	String DAY_DURATION_SELECT_FILTER_SLOT_NAME = " AND SLOT.name=:slotName";
	String DAY_DURATION_SELECT_FILTER_SLOT_USER_ID = " AND SLOT.user_id=:userId";
	String DAY_DURATION_SELECT_FILTER_DAY_DURATION_ID = " AND day_duration_id=:dayDurationId";
	String DAY_DURATION_SELECT_ORDER = " ORDER BY order_in_slot";
	String DAY_DURATION_SELECT_INTO = " INTO :{dayDurationId}, :{name}, :{slotStart}, :{slotEnd}, :{monday}, :{tuesday}, :{wednesday}, :{thursday}, :{friday}, :{saturday}, :{sunday}, :{weeklyPerpetual}, :{orderInSlot}, :{slotId}, :{slotCode}";
	String DAY_DURATION_SELECT_INTO_SLOT_USER_ID = ", :{userId}";
	String DAY_DURATION_SELECT_FROM = " FROM DAY_DURATION";
	String DAY_DURATION_SELECT_FROM_PLUS_GENERIC_WHERE = " FROM DAY_DURATION" + GENERIC_WHERE_FOR_SECURE_AND;

	String DAY_DURATION_JOIN_SLOT = " JOIN SLOT on DAY_DURATION.slot_id = SLOT.slot_id";

	String DAY_DURATION_UPDATE = "UPDATE DAY_DURATION SET name=:name, slot_start=:slotStart, slot_end=:slotEnd"
			+ ", monday=:monday, tuesday=:tuesday, wednesday=:wednesday, thursday=:thursday, friday=:friday, saturday=:saturday, sunday=:sunday, weekly_perpetual=:weeklyPerpetual, order_in_slot=:orderInSlot"
			+ " WHERE day_duration_id=:dayDurationId";

	String DAY_DURATION_INSERT_SAMPLE = "INSERT INTO DAY_DURATION (day_duration_id, name, slot_start, slot_end, "
			+ "monday, tuesday, wednesday, thursday, friday, saturday, sunday, weekly_perpetual, order_in_slot, slot_id)";
	String DAY_DURATION_VALUES_MORNING = " VALUES (nextval('" + PatchSlotTable.DAY_DURATION_ID_SEQ
			+ "'), 'zc.meeting.dayDuration.morning', '08:00:00', '12:00:00', 'true', 'true', 'true', 'true', 'true', 'false', 'false', 'true', 0, __slotId__)";
	String DAY_DURATION_VALUES_AFTERNOON = " VALUES (nextval('" + PatchSlotTable.DAY_DURATION_ID_SEQ
			+ "'), 'zc.meeting.dayDuration.afternoon', '14:00:00', '18:00:00', 'true', 'true', 'true', 'true', 'true', 'false', 'false', 'true', 1, __slotId__)";
	String DAY_DURATION_VALUES_LUNCH = " VALUES (nextval('" + PatchSlotTable.DAY_DURATION_ID_SEQ
			+ "'), 'zc.meeting.dayDuration.default', '12:00:00', '14:00:00', 'true', 'true', 'true', 'true', 'true', 'false', 'false', 'true', 0, __slotId__)";
	String DAY_DURATION_VALUES_EVENING = " VALUES (nextval('" + PatchSlotTable.DAY_DURATION_ID_SEQ
			+ "'),'zc.meeting.dayDuration.default', '19:00:00', '21:00:00', 'true', 'true', 'true', 'true', 'true', 'false', 'false', 'true', 0, __slotId__)";
	String DAY_DURATION_VALUES_WWEEKEND = " VALUES (nextval('" + PatchSlotTable.DAY_DURATION_ID_SEQ
			+ "'), 'zc.meeting.dayDuration.default', '08:00:00', '18:00:00', 'false', 'false', 'false', 'false', 'false', 'true', 'true', 'true', 0, __slotId__)";

	String SLOT_DROP_TABLE = "DROP TABLE SLOT";
	String DAY_SURATION_DROP_TABLE = "DROP TABLE DAY_SURATION";

	String SLOT_ALTER_TABLE_ADD_CODE = "ALTER TABLE SLOT ADD COLUMN " + PatchAddSlotCode.SLOT_PATCHED_COLUMN
			+ " INTEGER NOT NULL DEFAULT 0";
	String SLOT_UPDATE_CODE = "UPDATE SLOT SET slot_code = :slotCode WHERE slot_id=:slotId";

	/**
	 * Subscription table
	 */
	String SUBSCRIPTION_CREATE_TABLE = "CREATE TABLE " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ "(user_id INTEGER NOT NULL, role_id INTEGER NOT NULL, start_date TIMESTAMP NOT NULL, accepted_CPS_date TIMESTAMP, accepted_withdrawal_date TIMESTAMP,"
			+ " CONSTRAINT " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ "_PK PRIMARY KEY (user_id, role_id, start_date)," + " CONSTRAINT "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ "_USER_ROLE_FK FOREIGN KEY (user_id, role_id, start_date)" + " REFERENCES USER_ROLE(user_id, role_id,"
			+ PatchCreateSubscription.ADDED_USER_ROLE_COLUMN + "))";

	String SUBSCRIPTION_SELECT_ALL = "SELECT user_id, role_id, start_date, accepted_CPS_date, accepted_withdrawal_date FROM "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + " WHERE 1=1";

	String USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS = "SELECT USER_ROLE.role_id, USER_ROLE.user_id, USER_ROLE.start_date, "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".accepted_CPS_date, "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ ".accepted_withdrawal_date FROM USER_ROLE INNER JOIN ROLE ON USER_ROLE.role_id = ROLE.role_id LEFT OUTER JOIN "
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + " ON USER_ROLE.role_id="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".role_id AND USER_ROLE.user_id="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".user_id AND USER_ROLE.start_date="
			+ PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".start_date WHERE ROLE."
			+ PatchCreateSubscription.ADDED_ROLE_COLUMN + "='" + IRoleTypeLookupService.TYPE_SUBSCRIPTION + "'";
	String USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS_INTO = " INTO :{subscriptionDetails.subscriptionId}, :{subscriptionDetails.userId}, :{subscriptionDetails.startDate}, :{subscriptionDetails.acceptedCpsDate}, :{subscriptionDetails.acceptedWithdrawalDate}";
	String USER_ROLE_SELECT_SUBSCRIPTIONS_DETAILS_INTO_CPS = " INTO :{subscriptionId}, :{acceptedCpsDate}, :{acceptedWithdrawalDate}";

	String USER_ROLE_FILTER_USER_ID = " AND " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME + ".user_id=:userId";
	String USER_ROLE_FILTER_SUBSCRIPTION_ID = " AND " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ ".role_id=:subscriptionId";
	String USER_ROLE_FILTER_START_DATE = " AND " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ ".start_date=:startDate";

	String USER_ROLE_UPDATE_CPS = "UPDATE " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ " SET accepted_CPS_date=:{acceptedCpsDate}, accepted_withdrawal_date=:{acceptedWithdrawalDate} WHERE 1=1 "
			+ USER_ROLE_FILTER_USER_ID + USER_ROLE_FILTER_SUBSCRIPTION_ID + USER_ROLE_FILTER_START_DATE;

	String SUBSCRIPTION_INSERT = "INSERT INTO " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ " (user_id, role_id, start_date) VALUES (:userId, :subscriptionId, :startDate)";

	String SUBSCRIPTION_DELETE_BY_ROLE = "DELETE FROM " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ " WHERE role_id=:roleId";
	String SUBSCRIPTION_DELETE_BY_USER_ID = "DELETE FROM " + PatchCreateSubscription.SUBSCRIPTION_TABLE_NAME
			+ " WHERE user_id=:userId";

	/**
	 * Documents table
	 */

	String DOCUMENT_CREATE = "CREATE TABLE " + PatchCreateSubscription.DOCUMENT_TABLE_NAME
			+ " (document_id INTEGER NOT NULL, name VARCHAR(250) NOT NULL, content __blobType__, last_modification_date TIMESTAMP NOT NULL,"
			+ " CONSTRAINT " + PatchCreateSubscription.DOCUMENT_TABLE_NAME + "_PK PRIMARY KEY (document_id))";

	String DOCUMENT_PAGE_SELECT = "select document_id, name, last_modification_date FROM "
			+ PatchCreateSubscription.DOCUMENT_TABLE_NAME + " WHERE 1=1";
	String DOCUMENT_PAGE_DATA_SELECT_INTO = " INTO :{page.documentId}, :{page.name}, :{page.lastModificationDate}";

	String DOCUMENT_SELECT = "SELECT document_id, name, content, last_modification_date FROM "
			+ PatchCreateSubscription.DOCUMENT_TABLE_NAME + " WHERE document_id=:documentId";
	String DOCUMENT_SELECT_INTO = " INTO :documentId, :name, :contentData, :lastModificationDate";

	String DOCUMENT_SELECT_LINKED_ROLE = "SELECT document_id, role_id, start_date FROM "
			+ PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " WHERE document_id=:selectedDocumentId INTO :{table.documentId}, :{table.roleId}, :{table.startDate}";

	String DOCUMENT_INSERT = "INSERT INTO " + PatchCreateSubscription.DOCUMENT_TABLE_NAME
			+ " (document_id, name, last_modification_date) " + "VALUES (:documentId, :name, :lastModificationDate)";

	String DOCUMENT_UPDATE = "UPDATE " + PatchCreateSubscription.DOCUMENT_TABLE_NAME
			+ " SET name=:name, content=:contentData, last_modification_date=:lastModificationDate WHERE document_id=:documentId";

	/**
	 * -- Link between documents and Roles (for CPS's subscription)
	 */

	String ROLE_DOCUMENT_CREATE = "CREATE TABLE " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " (role_id INTEGER NOT NULL, document_id INTEGER NOT NULL, start_date TIMESTAMP NOT NULL DEFAULT now())";
	String ROLE_DOCUMENT_ADD_PK = "ALTER TABLE " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME + " ADD CONSTRAINT "
			+ PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME + "_PK PRIMARY KEY (role_id, document_id, start_date)";

	String ROLE_DOCUMENT_ADD_PK_WITHOUT_START_DATE = "ALTER TABLE " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " ADD CONSTRAINT " + PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ "_PK PRIMARY KEY (role_id, document_id)";

	String ROLE_DOCUMENT_SELECT_ACTIVE_DOCUMENT = "SELECT document_id FROM "
			+ PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME
			+ " WHERE role_id=:roleId AND start_date=(SELECT MAX(start_date) FROM "
			+ PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME + "  WHERE role_id=:roleId AND start_date <= NOW())";

	String ROLE_DOCUMENT_SELECT_ROLE_DOCUMENT = "SELECT document_id, role_id, start_date FROM "
			+ PatchCreateSubscription.ROLE_DOCUMENT_TABLE_NAME + " WHERE role_id=:roleId AND document_id=:documentId";

	String ROLE_DOCUMENT_SELECT_ROLE_DOCUMENT_INTO = " INTO :documentId, :roleId, :startDate";

	/**
	 * User custom configuration for his agendas
	 */

	String CALENDAR_CONFIG_CREATE = "CREATE TABLE " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ "(agenda_config_id INTEGER NOT NULL, external_id VARCHAR(250) NOT NULL, name VARCHAR(250), read_only BOOLEAN, main BOOLEAN, process BOOLEAN, add_event_to_calendar BOOLEAN, process_full_day_event BOOLEAN, process_busy_event BOOLEAN, process_not_registred_on_event BOOLEAN, oAuth_credential_id INTEGER NOT NULL,"
			+ " CONSTRAINT " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME + "_PK PRIMARY KEY (agenda_config_id),"
			+ " CONSTRAINT " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ "_OAUHTCREDENTIAL_FK FOREIGN KEY (oAuth_credential_id) REFERENCES OAUHTCREDENTIAL(api_credential_id))";
	String CALENDAR_CONFIG_JOIN_OAUTH = " INNER JOIN OAUHTCREDENTIAL ON oAuth_credential_id=api_credential_id";

	String CALENDAR_CONFIG_PAGE_SELECT = "select agenda_config_id, external_id, name, read_only, main, process, add_event_to_calendar, process_full_day_event, process_busy_event, process_not_registred_on_event, oAuth_credential_id, user_id FROM "
			+ PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME + CALENDAR_CONFIG_JOIN_OAUTH + " WHERE 1=1";
	String CALENDAR_CONFIG_PAGE_SELECT_INTO = " INTO :{page.calendarConfigurationId}, :{page.externalId}, :{page.name}, :{page.readOnly}, :{page.main}, :{page.process}, :{page.addEventToCalendar}, :{page.processFullDayEvent}, :{page.ProcessFreeEvent}, :{page.processNotRegistredOnEvent}, :{page.OAuthCredentialId}, :{page.userId}";

	String CALENDAR_CONFIG_FILTER_ID = " AND agenda_config_id = :calendarConfigurationId";
	String CALENDAR_CONFIG_FILTER_CURRENT_USER = " AND user_id = :currentUser";
	String CALENDAR_CONFIG_FILTER_USER_ID = " AND user_id = :userId";
	String CALENDAR_CONFIG_FILTER_EXTERNAL_ID = " AND external_id = :externalId";
	String CALENDAR_CONFIG_FILTER_OAUTH_CREDENTIAL_ID_ID = "  AND oAuth_credential_id = :oAuthCredentialId";
	String CALENDAR_CONFIG_FILTER_ADD_EVENT = " AND add_event_to_calendar='true'";
	String CALENDAR_CONFIG_FILTER_PROCESSED = " AND process = 'true'";

	String CALENDAR_CONFIG_SELECT = "select agenda_config_id, external_id, name, main, read_only, process, add_event_to_calendar, process_full_day_event, process_busy_event, process_not_registred_on_event, oAuth_credential_id, user_id FROM "
			+ PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME + CALENDAR_CONFIG_JOIN_OAUTH;

	String CALENDAR_CONFIG_SELECT_ID = "select agenda_config_id FROM "
			+ PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME + CALENDAR_CONFIG_JOIN_OAUTH;

	String CALENDAR_CONFIG_SELECT_INTO = " INTO :calendarConfigurationId, :externalId, :name, :main, :readOnly, :process, :addEventToCalendar, :processFullDayEvent, :processFreeEvent, :processNotRegistredOnEvent, :oAuthCredentialId, :userId";

	String CALENDAR_CONFIG_SELECT_OWNER = "SELECT user_id FROM " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ CALENDAR_CONFIG_JOIN_OAUTH + " WHERE agenda_config_id=:calendarConfigurationId";

	String CALENDAR_CONFIG_INSERT = "INSERT INTO " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ " (agenda_config_id, external_id, oAuth_credential_id) VALUES(:calendarConfigurationId, :externalId, :OAuthCredentialId)";
	String CALENDAR_CONFIG_UPDATE = "UPDATE " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ " SET name=:name, main=:main, read_only=:readOnly, process=:process, add_event_to_calendar=:addEventToCalendar, process_full_day_event=:processFullDayEvent, process_busy_event=:ProcessFreeEvent, process_not_registred_on_event=:processNotRegistredOnEvent";
	String CALENDAR_CONFIG_DELETE_BY_API_ID = "DELETE FROM " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ " WHERE oAuth_credential_id=:oAuthCredentialId";
	String CALENDAR_CONFIG_DELETE_BY_EXTERNAL_ID = "DELETE FROM " + PatchConfigureCalendar.CALENDAR_CONFIG_TABLE_NAME
			+ " WHERE external_id=:externalId AND oAuth_credential_id=:oAuthCredentialId";

}
