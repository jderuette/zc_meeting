package org.zeroclick.meeting.server.security;

import java.lang.reflect.InvocationTargetException;
import java.security.BasicPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroclick.configuration.shared.role.IAppPermissionService;
import org.zeroclick.meeting.shared.security.AccessControlService;

/**
 * <h3>{@link AccessControlService}</h3>
 *
 * @author djer
 */
@Replace
public class ServerAccessControlService extends AccessControlService {

	private static final Logger LOG = LoggerFactory.getLogger(ServerAccessControlService.class);

	public ServerAccessControlService() {
		super();
	}

	@Override
	protected Permissions execLoadPermissions(final String userId) {
		final IAppPermissionService service = BEANS.get(IAppPermissionService.class);

		final Object[][] permissionData = service.getPermissionsByUser(userId);

		final Permissions permissions = this.createPermissions(permissionData);
		permissions.add(new RemoteServiceAccessPermission("*.shared.*", "*"));
		return permissions;

	}

	private Permissions createPermissions(final Object[][] permissionData) {
		final Permissions permissions = new Permissions();

		for (int i = 0; i < permissionData.length; i++) {
			final String name = (String) permissionData[i][0];
			final Integer level = ((Long) permissionData[i][1]).intValue();

			try {
				@SuppressWarnings("unchecked")
				final Class<? extends Permission> clazz = (Class<? extends Permission>) Class.forName(name);
				permissions.add(this.createPermission(clazz, name, level));
			} catch (final ClassNotFoundException cnfe) {
				LOG.error(this.buildErrorMessage(name, level), cnfe);
			}

		}

		return permissions;
	}

	private <T extends Permission> T createPermission(final Class<? extends T> clazz, final String name,
			final Integer level) {
		T permissionObject = null;

		try {
			if (BasicHierarchyPermission.class.isAssignableFrom(clazz)) {
				permissionObject = this.createBasicHierarchyPermission(clazz, level);
			} else if (this.isBasicPermission(clazz)) {
				permissionObject = this.createBasicPermission(clazz);
			} else {
				LOG.error(
						"Cannot create permission from saved data (not a (sub)instance of BasicHierarchyPermission/BasicPermission/Permission). Name : "
								+ name + " level : " + level);
			}
		} catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error(this.buildErrorMessage(name, level), e);
		}
		return permissionObject;
	}

	private Boolean isBasicPermission(final Class<?> clazz) {
		return BasicPermission.class.isAssignableFrom(clazz) || Permission.class.isAssignableFrom(clazz);
	}

	private <T> T createBasicPermission(final Class<? extends T> implType)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return ConstructorUtils.invokeExactConstructor(implType);
	}

	private <T> T createBasicHierarchyPermission(final Class<? extends T> implType, final Integer level)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		return ConstructorUtils.invokeExactConstructor(implType, level);
	}

	/**
	 * Clear the cache with the passed userIds (login and Email)
	 *
	 * @param userId
	 *            login or email to user cache
	 */
	@Override
	public void clearUserCache(final Set<String> userIds) {
		this.clearCache(userIds);
		this.clearUserIdsCache(userIds);
	}

	public void clearCacheOfUsersIds(final Collection<String> cacheKeys) throws ProcessingException {
		this.clearCache();
		if (cacheKeys != null && !cacheKeys.isEmpty()) {
			this.getCache().invalidate(new KeyCacheEntryFilter<String, PermissionCollection>(cacheKeys), true);
		}
	}

	private String buildErrorMessage(final String name, final Integer level) {
		final StringBuilder builder = new StringBuilder(100);
		builder.append("Cannot create permission from saved data. Name : ").append(name).append(", level : ")
				.append(level);

		return builder.toString();
	}
}
