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
package org.zeroclick.meeting.server.sql.migrate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jdbc.SQL;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.common.VersionHelper;
import org.zeroclick.configuration.shared.params.IAppParamsService;
import org.zeroclick.configuration.shared.role.AssignToRoleFormData;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.configuration.shared.role.IRoleService;
import org.zeroclick.configuration.shared.role.RoleFormData;
import org.zeroclick.meeting.server.security.ServerAccessControlService;
import org.zeroclick.meeting.server.sql.DatabaseHelper;
import org.zeroclick.meeting.server.sql.DatabaseProperties.SuperUserSubjectProperty;
import org.zeroclick.meeting.server.sql.SQLs;
import org.zeroclick.meeting.server.sql.SuperUserRunContextProducer;

import com.github.zafarkhaja.semver.Version;

/**
 * @author djer
 *
 */
public abstract class AbstractDataPatcher implements IDataPatcher {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDataPatcher.class);

	protected static final String SUPER_USER_ROLE_NAME = "SuperUser";

	private final DatabaseHelper databaseHelper;

	private String description;

	public AbstractDataPatcher() {
		this.databaseHelper = DatabaseHelper.get();
	}

	protected abstract void execute();

	protected abstract void undo();

	protected Version getSourceCodeVersion() {
		String mavenVersion = this.getClass().getPackage().getImplementationVersion();
		if (null == mavenVersion) {
			mavenVersion = VersionHelper.get().getSourceVersion();
		}
		return Version.valueOf(mavenVersion);
	}

	protected Version getDataVersion() {
		final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);

		// Table APP_PARAMS may not be patched yet
		String versionTxt;
		if (this.getDatabaseHelper().existTable("APP_PARAMS")) {
			versionTxt = appParamsService.getValue(IAppParamsService.KEY_DATA_VERSION);
		} else {
			LOG.warn("Table APP_PARAMS does NOT exist (yet). Returning default dataVersion");
			versionTxt = "0.0.0";
		}

		return Version.valueOf(versionTxt);
	}

	@Override
	public void apply() {
		LOG.info("Applying patch : " + this.getDescription() + " ===> " + this.getVersion());
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
		try {
			final IRunnable runnable = new IRunnable() {

				@Override
				@SuppressWarnings("PMD.SignatureDeclareThrowsException")
				public void run() throws Exception {
					AbstractDataPatcher.this.execute();
					AbstractDataPatcher.this.refreshSuperUserPermissions();
				}
			};

			context.run(runnable);
		} catch (final RuntimeException e) {
			BEANS.get(ExceptionHandler.class).handle(e);
		}

		LOG.info("Applying patch : END");
	}

	@Override
	public void remove() {
		LOG.info("Removing patch : " + this.getDescription() + " <=== " + this.getVersion());
		final RunContext context = BEANS.get(SuperUserRunContextProducer.class).produce();
		try {
			final IRunnable runnable = new IRunnable() {

				@Override
				@SuppressWarnings("PMD.SignatureDeclareThrowsException")
				public void run() throws Exception {
					AbstractDataPatcher.this.undo();
					AbstractDataPatcher.this.refreshSuperUserPermissions();
					// TODO Djer how to reliably go back to the real previous
					// version ? (storing each patch action a specific table ?)
					// AbstractDataPatcher.this.updateDataVersion(AbstractDataPatcher.this.getVersion());
				}
			};

			context.run(runnable);
		} catch (final RuntimeException e) {
			BEANS.get(ExceptionHandler.class).handle(e);
		}

		LOG.info("Removing patch END");
	}

	@Override
	/**
	 * By Default return TRUE if major/minor sourceCode version is strictly
	 * higher than data version<br />
	 * Follow the semVer standard (see http://semver.org/)<<br />
	 * General semVer format : Major.Minor.Patch-pre-release+Build metadata :
	 * 1.0.0-alpha.1+001<br />
	 * Example :<br />
	 * 1.0.0 vs 1.0.5 => FALSE<br />
	 * 1.0.0 vs 1.0.0 => FALSE<br />
	 * 1.0.1 vs 1.1.0 => TRUE<br />
	 * 1.5.3 vs 1.5.6 => FALSE<br />
	 * 1.5.6 vs 1.5.6-build48+bidule => false
	 *
	 */
	public Boolean isRequired() {
		return this.getSourceCodeVersion().getMajorVersion() >= this.getDataVersion().getMajorVersion()
				&& this.getSourceCodeVersion().getMinorVersion() >= this.getDataVersion().getMinorVersion();
	}

	@Override
	/**
	 * ByDefault return TRUE if the datasourceVersion is strictly higher than
	 * the dataVersion.
	 */
	public Boolean canMigrate() {
		return this.getSourceCodeVersion().greaterThan(this.getDataVersion());
	}

	private void updateDataVersion(final Version version) {
		final IAppParamsService appParamsService = BEANS.get(IAppParamsService.class);

		appParamsService.store(IAppParamsService.KEY_DATA_VERSION, version.toString());
	}

	private void refreshSuperUserPermissions() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Refreshing SuperUser role with current existing permissions");
		}
		final Version sourceCodeVersion = this.getSourceCodeVersion();

		if (sourceCodeVersion.greaterThanOrEqualTo(Version.valueOf("1.1.4"))) {
			final RoleFormData roleformData = this.getSuperUserRole();
			if (null != roleformData.getRoleId()) {
				try {
					this.addAllPermissions(roleformData.getRoleId());
					if (LOG.isDebugEnabled()) {
						LOG.debug(
								"SuperUser refreshed with " + BEANS.get(IAccessControlService.class).getPermissions());
					}
				} catch (final Exception e) {
					LOG.warn("Cannot refresh superUser permisisons : " + e);
				}
			} else {
				LOG.warn("Cannot refresh superUser's role because this role dosen't exists");
			}
		} else {
			LOG.info(
					"Cannot refresh superUser's Role, because sourceCode version is too old to handle roles correctly ("
							+ sourceCodeVersion + ")");
		}
	}

	private RoleFormData getSuperUserRole() {
		final IRoleService roleService = BEANS.get(IRoleService.class);
		final RoleFormData roleFormData = new RoleFormData();
		roleFormData.getRoleName().setValue(SUPER_USER_ROLE_NAME);
		try {
			roleService.load(roleFormData);
		} catch (final Exception e) {
			LOG.warn("Cannot load superUser Role ", e);
		}
		return roleFormData;
	}

	protected void addAllPermissions(final Long roleId) {
		// add "ALL" permission to superUser Role
		final AssignToRoleFormData permissionFormData = new AssignToRoleFormData();
		this.getAllExisitingPermissions(permissionFormData);
		permissionFormData.getRoleId().setValue(roleId);
		permissionFormData.getLevel().setValue(100L);
		SQL.insert(SQLs.ROLE_PERMISSION_DELETE_BY_ROLE, new NVPair("roleId", roleId));
		SQL.insert(SQLs.ROLE_PERMISSION_INSERT, permissionFormData);

		// Clear user Cache
		BEANS.get(ServerAccessControlService.class)
				.clearCacheOfUsersIds(CollectionUtility.arrayList(this.getSuperUserPrincipal()));
		// clear permission cache
		BEANS.get(IAccessControlService.class).clearCache();
	}

	private void getAllExisitingPermissions(final AssignToRoleFormData permissionFormData) {
		final IAppPermissionService appPermissionService = BEANS.get(IAppPermissionService.class);

		final Object[][] allPermissions = appPermissionService.getpermissions();

		permissionFormData.setPermission(new ArrayList<>());
		for (final Object[] permission : allPermissions) {
			final String permissionName = (String) permission[0];
			if (null != permissionName && !"".equals(permissionName)) {
				permissionFormData.getPermission().add(permissionName);
			}
		}
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

	protected DatabaseHelper getDatabaseHelper() {
		return this.databaseHelper;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	protected void setDescription(final String description) {
		this.description = description;
	}

}
