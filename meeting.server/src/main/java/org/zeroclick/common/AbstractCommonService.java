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
package org.zeroclick.common;

import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.context.ServerRunContextProducer;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.zeroclick.comon.text.UserHelper;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;
import org.zeroclick.meeting.server.sql.DatabaseProperties.SuperUserSubjectProperty;
import org.zeroclick.meeting.shared.event.IEventService;
import org.zeroclick.meeting.shared.security.IAccessControlServiceHelper;

/**
 * @author djer
 *
 */
@ApplicationScoped
public abstract class AbstractCommonService {

	protected UserHelper userHelper;

	@PostConstruct
	public void init() {
		final UserHelper userHelper = BEANS.get(UserHelper.class);
		this.userHelper = userHelper;
	}

	protected abstract Logger getLog();

	public void throwAuthorizationFailed() throws VetoException {
		throw new VetoException(TEXTS.get("AuthorizationFailed"));
	}

	public void throwAuthorizationFailed(final Permission permission) throws VetoException {
		this.getLog().warn("Permission denied, requested permisison : " + permission.getName() + " ("
				+ permission.getClass().toGenericString() + ")");
		if (this.getLog().isDebugEnabled()) {
			this.getLog().debug("Current users roles : " + BEANS.get(IAccessControlService.class).getPermissions());
		}
		this.throwAuthorizationFailed();
	}

	public void checkPermission(final Permission permission) throws VetoException {
		if (!ACCESS.check(permission)) {
			this.throwAuthorizationFailed(permission);
		}
	}

	protected Set<String> buildNotifiedUsers(final Long ownerObjectId, final Boolean addPendingMeetingUsers) {
		final HashSet<String> notifiedUsers = new HashSet<>();
		if (null != ownerObjectId) {
			notifiedUsers.addAll(this.getUserNotificationIds(ownerObjectId));
		}

		if (addPendingMeetingUsers) {
			final Set<Long> pendingUsers = this.getUserWithPendingEvent(ownerObjectId);
			if (null != pendingUsers) {
				for (final Long userId : pendingUsers) {
					notifiedUsers.addAll(this.getUserNotificationIds(userId));
				}
			}
		}
		return notifiedUsers;
	}

	/**
	 * Get all UserId having a pending event with current connected user
	 *
	 * @return
	 */
	protected Set<Long> getUserWithPendingEvent() {
		return this.getUserWithPendingEvent(null);
	}

	protected Set<Long> getUserWithPendingEvent(final Long forUserId) {
		Set<Long> pendingMeetingUser = new HashSet<>();

		final IEventService eventService = BEANS.get(IEventService.class);
		final Map<Long, Integer> pendingUsers = eventService.getUsersWithPendingMeeting(forUserId);

		if (null != pendingUsers) {
			pendingMeetingUser = pendingUsers.keySet();
		}

		return pendingMeetingUser;
	}

	protected Set<String> getUserNotificationIds(final Long ownerObjectId) {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return acsHelper.getUserNotificationIds(ownerObjectId);
	}

	protected Set<String> getCurrentUserNotificationIds() {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return acsHelper.getUserNotificationIds(acsHelper.getZeroClickUserIdOfCurrentSubject());
	}

	protected String getCurrentUserEmail() {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();

		return userDetails.getEmail().getValue();
	}

	protected String getCurrentUserLogin() {
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();

		return userDetails.getLogin().getValue();
	}

	protected String getSuperUserPrincipal() {
		final Set<Principal> superUserPrincipals = CONFIG.getPropertyValue(SuperUserSubjectProperty.class)
				.getPrincipals();
		String firstSuperUserPrincipal = null;
		for (final Principal principal : superUserPrincipals) {
			if (null == firstSuperUserPrincipal || "".equals(firstSuperUserPrincipal)) {
				firstSuperUserPrincipal = principal.getName().toLowerCase();
			}
		}

		return firstSuperUserPrincipal;
	}

	protected Boolean isMyself(final Long userId) {
		final IAccessControlServiceHelper acsHelper = BEANS.get(IAccessControlServiceHelper.class);
		return acsHelper.getZeroClickUserIdOfCurrentSubject().equals(userId);
	}

	protected Boolean isCurrentUserSuperUser() {
		final Subject currentUserSubject = Subject.getSubject(AccessController.getContext());
		final String superUserPrincipal = this.getSuperUserPrincipal();

		if (null != currentUserSubject && currentUserSubject.getPrincipals().size() > 0) {
			for (final Principal principal : currentUserSubject.getPrincipals()) {
				final String name = principal.getName();
				if (superUserPrincipal.equalsIgnoreCase(name)) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	protected void insertInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			@SuppressWarnings("PMD.SignatureDeclareThrowsException")
			public void run() throws Exception {
				SQL.insert(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	protected void updateInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			@SuppressWarnings("PMD.SignatureDeclareThrowsException")
			public void run() throws Exception {
				SQL.update(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	protected void selectIntoInsideNewTransaction(final String sql, final Object... bindBases) {
		Jobs.schedule(new IRunnable() {

			@Override
			@SuppressWarnings("PMD.SignatureDeclareThrowsException")
			public void run() throws Exception {
				SQL.selectInto(sql, bindBases);
			}
		}, Jobs.newInput().withRunContext(this.buildNewTransactionRunContext()));
	}

	private RunContext buildNewTransactionRunContext() {
		return new ServerRunContextProducer().produce(this.userHelper.getCurrentUserSubject());
	}
}
