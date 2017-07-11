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

//tag::createDB[]
//tag::organizationListing[]
public interface SQLs {

	String SELECT_TABLE_NAMES_DERBY = "SELECT UPPER(tablename) FROM sys.systables INTO :result";
	String SELECT_TABLE_NAMES_POSTGRESQL = "select UPPER(tables.table_name) from information_schema.tables INTO :result";

	String GENERIC_DROP_TABLE = "DROP TABLE __tableName__ CASCADE";
	String GENERIC_CREATE_SEQUENCE = "CREATE SEQUENCE __seqName__ START __seqStart__";
	String GENERIC_DROP_SEQUENCE = "DROP SEQUENCE __seqName__";

	String GENERIC_SEQUENCE_EXISTS = "SELECT to_regclass('__seqName__')";

	/**
	 * EVENT
	 */

	String EVENT_CREATE_TABLE = ""
			+ "CREATE TABLE EVENT (event_id INTEGER NOT NULL CONSTRAINT EVENT_PK PRIMARY KEY, organizer INTEGER, organizer_email VARCHAR(120), duration INTEGER, slot INTEGER, email VARCHAR(120), guest_id INTEGER, state VARCHAR(50), subject VARCHAR(250), startDate TIMESTAMP, endDate TIMESTAMP, externalIdRecipient VARCHAR(250), externalIdOrganizer VARCHAR(250))";

	String ORGANIZATION_LOOKUP = "" + "SELECT   organization_id, " + "         name " + "FROM     ORGANIZATION "
			+ "WHERE    1 = 1 " + "<key>    AND organization_id = :key</key> "
			+ "<text>   AND UPPER(name) LIKE UPPER(:text||'%') </text> " + "<all></all>";

	String AND_LIKE_CAUSE = "AND LOWER(%s) LIKE LOWER(:%s || '%%') ";

	String EVENT_PAGE_SELECT = "SELECT event_id, organizer, organizer_email, duration, slot, email, guest_id, state, subject, startDate, endDate, externalIdRecipient, externalIdOrganizer  FROM EVENT WHERE 1=1";
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
	String EVENT_PAGE_SELECT_FILTER_USER_OR_RECIPIENT = " AND (organizer = :currentUser OR email = :currentUserEmail)";

	String EVENT_PAGE_DATA_SELECT_INTO = " INTO :{page.eventId}, :{page.organizer}, :{page.organizerEmail}, :{page.duration}, :{page.slot}, :{page.email}, :{page.guestId}, :{page.state}, :{page.subject}, :{page.startDate}, :{page.endDate}, :{page.externalIdRecipient}, :{page.externalIdOrganizer}";

	String EVENT_SELECT_USERS_PENDING_EVENT_GUEST = "SELECT event_id, organizer FROM EVENT WHERE guest_id=:currentUser";
	String EVENT_SELECT_USERS_PENDING_EVENT_HOST = "SELECT event_id, guest_id FROM EVENT WHERE organizer=:currentUser";

	String EVENT_INSERT = "INSERT INTO EVENT (event_id, organizer) " + "VALUES (:eventId, :organizer)";

	String EVENT_UPDATE = "UPDATE EVENT SET organizer_email=:organizerEmail, duration=:duration, slot=:slot, email=:email, guest_id=:guestId, state=:state, subject=:subject, startDate=:startDate, endDate=:endDate, externalIdRecipient=:externalIdRecipient, externalIdOrganizer=:externalIdOrganizer WHERE event_id=:eventId";
	String EVENT_UPDATE_STATE = "UPDATE EVENT SET state=:state WHERE event_id=:eventId";

	String EVENT_SELECT = "SELECT duration, slot, email, guest_id, state, subject, startDate, endDate, externalIdRecipient, externalIdOrganizer, organizer, organizer_email FROM EVENT"
			+ " WHERE event_id=:eventId"
			+ " INTO :duration, :slot, :email, :guestId, :state, :subject, :startDate, :endDate, :externalIdRecipient, :externalIdOrganizer, :organizer, :organizerEmail";

	String EVENT_SELECT_REJECT = "SELECT organizer_email, email, subject, organizer, guest_id, externalIdOrganizer, externalIdRecipient FROM EVENT WHERE event_id=:eventId INTO :organizerEmail, :email, :subject, :organizer, :guestId, :externalIdOrganizer, :externalIdRecipient";

	String EVENT_SELECT_RECIPIENT = "SELECT email FROM EVENT WHERE event_id=:eventId INTO :email";
	String EVENT_SELECT_OWNER = "SELECT organizer FROM EVENT WHERE event_id=:eventId INTO :organizer";

	String EVENT_SELECT_KNOWN_ATTENDEE = "SELECT DISTINCT email FROM EVENT WHERE organizer=:currentUser AND email LIKE :searchEmail AND guest_id!=:currentUser";
	String EVENT_SELECT_KNOWN_HOST = "SELECT DISTINCT organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer_email LIKE ':searchEmail' AND organizer!=:currentUser";

	String EVENT_SELECT_KNOWN_ATTENDEE_LOOKUP = "SELECT DISTINCT email, email FROM EVENT WHERE organizer=:currentUser  AND guest_id!=:currentUser <key> AND email=:key</key><text> AND email LIKE :text</text> <all></all>";
	String EVENT_SELECT_KNOWN_HOST_LOOKUP = "SELECT DISTINCT organizer_email, organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer!=:currentUser <key> AND organizer_email=:key</key><text> AND organizer_email LIKE :text</text> <all></all>";

	String EVENT_SELECT_KNOWN_ATTENDEE_STRICT = "SELECT DISTINCT email FROM EVENT WHERE organizer=:currentUser AND email=:searchEmail";
	String EVENT_SELECT_KNOWN_HOST_STRICT = "SELECT DISTINCT organizer_email FROM EVENT WHERE guest_id=:currentUser AND organizer_email=:searchEmail";

	String EVENT_INSERT_SAMPLE = "INSERT INTO EVENT (event_id, organizer, organizer_email, duration, slot, email, guest_id, state, subject)";
	String EVENT_VALUES_01 = " VALUES  (nextval('EVENT_ID_SEQ'), 1, 'djer13@gmail.com', 15, 1, 'bob1@gmail.com', 2, 'ASKED', 'Prendre le thé')";
	String EVENT_VALUES_02 = " VALUES  (nextval('EVENT_ID_SEQ'), 2,'bob1@gmail.com', 120, 3, 'bob2@entreporise.com', 1, 'ASKED', 'Do Something')";

	String EVENT_DROP_TABLE = "DROP TABLE EVENT CASCADE";

	/**
	 * OAuth credential
	 */
	String OAUHTCREDENTIAL_CREATE_TABLE = "CREATE TABLE OAUHTCREDENTIAL (api_credential_id INTEGER NOT NULL CONSTRAINT OAUHTCREDENTIAL_PK PRIMARY KEY, user_id INTEGER, access_token VARCHAR(200), expiration_time_milliseconds BIGINT, refresh_token VARCHAR(200), provider INTEGER, repository_id VARCHAR(200), provider_data __blobType__)";

	String OAUHTCREDENTIAL_PAGE_SELECT = "SELECT api_credential_id, access_token, expiration_time_milliseconds, refresh_token, user_id, provider FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_PAGE_SELECT_FILTER_USER = " AND user_id = :currentUser";
	String OAUHTCREDENTIAL_PAGE_DATA_SELECT_INTO = " INTO :{page.apiCredentialId}, :{page.accessToken}, :{page.expirationTimeMilliseconds}, :{page.refreshToken}, :{page.userId}, :{page.provider}";

	String OAUHTCREDENTIAL_INSERT = "INSERT INTO OAUHTCREDENTIAL (api_credential_id, user_id) VALUES (:apiCredentialId, :userId)";

	String OAUHTCREDENTIAL_UPDATE = "UPDATE OAUHTCREDENTIAL SET user_id=:userId,  access_token=:accessToken, expiration_time_milliseconds=:expirationTimeMilliseconds, refresh_token=:refreshToken, provider=:provider, repository_id=:repositoryId, provider_data=:providerData WHERE api_credential_id=:apiCredentialId";

	String OAUHTCREDENTIAL_SELECT_OWNER = "SELECT user_id FROM OAUHTCREDENTIAL WHERE api_credential_id=:apiCredentialId INTO :userId";

	String OAUHTCREDENTIAL_SELECT = "SELECT api_credential_id, access_token, expiration_time_milliseconds, refresh_token, provider, repository_id, provider_data FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_SELECT_API_ID = "SELECT api_credential_id FROM OAUHTCREDENTIAL WHERE 1=1";
	String OAUHTCREDENTIAL_SELECT_GOOGLE_DATA = "SELECT google_data FROM OAUHTCREDENTIAL WHERE provider=1";

	String OAUHTCREDENTIAL_SELECT_INTO = " INTO :apiCredentialId, :accessToken, :expirationTimeMilliseconds, :refreshToken, :provider, :repositoryId, :providerData";
	String OAUHTCREDENTIAL_SELECT_INTO_API_ID = " INTO :apiCredentialId";

	String OAUHTCREDENTIAL_SELECT_PROVIDER_DATA_ONLY = "SELECT provider_data FROM OAUHTCREDENTIAL WHERE 1=1";

	String OAUHTCREDENTIAL_FILTER_OAUTH_ID = " AND api_credential_id = :apiCredentialId";
	String OAUHTCREDENTIAL_FILTER_USER_ID = " AND user_id= :userId";

	String OAUHTCREDENTIAL_SELECT_ALL_USER_IDS = "select user_id FROM OAUHTCREDENTIAL";

	String OAUHTCREDENTIAL_INSERT_SAMPLE = "INSERT INTO OAUHTCREDENTIAL (api_credential_id, user_id, access_token, expiration_time_milliseconds, refresh_token, provider, repository_id, provider_data) ";

	String OAUHTCREDENTIAL_VALUES_01 = " VALUES  (nextval('OAUHTCREDENTIAL_ID_SEQ'), 0, 'testAccessToken', 1514568455, 'testRefreshToken', 1, 'testRepo', null)";

	String OAUHTCREDENTIAL_DROP_TABLE = "DROP TABLE OAUHTCREDENTIAL";

	String OAUHTCREDENTIAL_DELETE = "DELETE FROM OAUHTCREDENTIAL where 1=1" + OAUHTCREDENTIAL_FILTER_OAUTH_ID;

	/**
	 * Roles and permissions
	 */

	String ROLE_CREATE_TABLE = "CREATE TABLE ROLE (role_id INTEGER NOT NULL CONSTRAINT ROLE_PK PRIMARY KEY, name VARCHAR(100))";

	String ROLE_PAGE_SELECT = "SELECT role_id, name FROM ROLE";

	String ROLE_PAGE_DATA_SELECT_INTO = " INTO :{role.roleId}, :{role.roleName}";

	String ROLE_INSERT = "INSERT INTO ROLE (role_id) VALUES (:roleId)";

	String ROLE_UPDATE = "UPDATE ROLE SET name=:roleName WHERE role_id=:roleId";

	String ROLE_SELECT = "SELECT role_id, name FROM ROLE WHERE 1=1";

	String ROLE_LOOKUP = "SELECT role_id,  name FROM ROLE WHERE 1=1 <key>   AND role_id = :key </key>"
			+ " <text>  AND UPPER(name) LIKE UPPER('%'||:text||'%') </text> <all> </all>";

	String ROLE_INSERT_SAMPLE = "INSERT INTO ROLE (role_id, name)";
	String ROLE_VALUES_01 = " VALUES(1, 'Administrator')";
	String ROLE_VALUES_02 = " VALUES(2, 'Standard')";

	String ROLE_DROP_TABLE = "DROP TABLE ROLE CASCADE";

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

	String ROLE_PERMISSION_DELETE = "DELETE FROM ROLE_PERMISSION WHERE role_id = :roleId AND permission = :{permissions} ";

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

	/**
	 * User Role mapping
	 */
	String USER_ROLE_CREATE_TABLE = "CREATE TABLE USER_ROLE (user_id INTEGER NOT NULL, role_id INTEGER NOT NULL, CONSTRAINT USER_ROLE_PK PRIMARY KEY (user_id, role_id))";

	String USER_ROLE_SELECT = "SELECT user_id, role_id FROM USER_ROLE WHERE 1=1";
	String USER_ROLE_SELECT_ROLES = "SELECT role_id FROM USER_ROLE WHERE 1=1";
	String USER_ROLE_SELECT_FILTER_ROLE = " AND role_id=:roleId";
	String USER_ROLE_SELECT_FILTER_USER = " AND user_id=:currentUser";

	String USER_ROLE_INSERT = "INSERT INTO USER_ROLE (user_id, role_id) VALUES (:userId, :{rolesBox})";

	String USER_ROLE_REMOVE_ALL = "DELETE FROM USER_ROLE WHERE user_id=:userId";

	String USER_ROLE_INSERT_SAMPLE = "INSERT INTO USER_ROLE (user_id, role_id)";
	String USER_ROLE_VALUES_01 = " VALUES(1, 1)";
	String USER_ROLE_VALUES_02 = " VALUES(2, 2)";

	String USER_ROLE_DROP_TABLE = "DROP TABLE USER_ROLE";

	/**
	 * Users permissions
	 */
	String USER_PERMISSIONS_SELECT = "SELECT P.permission, MAX(P.level) FROM ROLE_PERMISSION P INNER JOIN USER_ROLE R ON P.role_id = R.role_id WHERE 1=1";
	String USER_PERMISSIONS_SELECT_FILTER_USER_ID = " AND R.user_id = :userId";
	String USER_PERMISSIONS_SELECT_GROUP_BY = " GROUP BY P.permission";

	/**
	 * Users
	 */
	String USER_CREATE_TABLE = "CREATE TABLE APP_USER (user_id INTEGER NOT NULL, login VARCHAR(50), email VARCHAR(120), password VARCHAR(256), time_zone VARCHAR(120), CONSTRAINT USER_PK PRIMARY KEY (user_id), CONSTRAINT USER_UNIQUE_EMAIL UNIQUE (email))";

	String USER_PAGE_SELECT = "select user_id, login, email, time_zone FROM APP_USER WHERE 1=1";
	String USER_PAGE_DATA_SELECT_INTO = " INTO :{page.userId}, :{page.login}, :{page.email}, :{page.timeZone}";

	String USER_SELECT = "SELECT user_id, login, email, password, time_zone FROM APP_USER WHERE 1=1";

	String USER_SELECT_FILTER_ID = " AND user_id=:currentUser";
	String USER_SELECT_FILTER_EMAIL = " AND email=:email";
	String USER_SELECT_FILTER_LOGIN = " AND login=:login";
	String USER_SELECT_INTO = " INTO :userId, :login, :email, :hashedPassword, :timeZone";

	String USER_SELECT_TIME_ZONE = "SELECT time_zone FROM APP_USER WHERE 1=1";
	String USER_SELECT_INTO_TIME_ZONE = " INTO :timeZone";

	String USER_SELECT_NOTIFICATION_IDS = "SELECT login, email FROM APP_USER WHERE 1=1";

	String USER_SELECT_INTO_ROLE = " INTO :{rolesBox}";

	String USER_SELECT_PASSWORD_FILTER_LOGIN = "select user_id, password FROM APP_USER where login=:login INTO :userId,  :password";
	String USER_SELECT_PASSWORD_FILTER_EMAIL = "select user_id, password FROM APP_USER where email=:email INTO :userId, :password";

	String USER_INSERT = "INSERT INTO APP_USER (user_id) VALUES (:userId)";

	/**
	 * Password not updtaded use USER_UPDATE_PASSWORD
	 */
	String USER_UPDATE = "UPDATE APP_USER SET email=:email, login=:login, time_zone=:timeZone WHERE user_id=:userId ";
	String USER_UPDATE_ONBOARDING = "UPDATE APP_USER SET login=:login, time_zone=:timeZone WHERE user_id=:userId ";
	String USER_UPDATE_PASSWORD = "UPDATE APP_USER SET password=:hashedPassword WHERE user_id=:userId";

	String USER_INSERT_SAMPLE = "INSERT INTO APP_USER (user_id, login, email, time_zone, password)";
	String USER_VALUES_01 = " VALUES(nextval('USER_ID_SEQ'), 'djer13', 'djer13@gmail.com', 'Europe/Paris', 'kv6kmSYn4jnCyoQK/4cQjA==.7bXNiiq6QcbGKFge/UdQ7T5cFud69Wp+qRBGZLnMU8VZ3UMgFuWtb/BpVsFBlpUSfYBd8t06uOkmHAliGKisOA==')"; // Djer13
	String USER_VALUES_02 = " VALUES(nextval('USER_ID_SEQ'), 'bob1', 'bob1@gmail.com', null, 'I/ocgG3Cp6QhLzIkrmYOQg==.GIxlDVNe8rl4r8WnnhT197qSBWaQIRvKnn4lNt6dqVWJ/aHDBCyxltXCNuWjYyyaynI34FM5x9Uz4hBBWMjYZw==')"; // Bob001

	String USER_CREATE_DROP = "CREATE TABLE APP_USE CASCADE";
}
