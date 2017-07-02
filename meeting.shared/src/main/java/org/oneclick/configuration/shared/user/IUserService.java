package org.oneclick.configuration.shared.user;

import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@ApplicationScoped
@TunnelToServer
public interface IUserService extends IService {

	UserFormData prepareCreate(UserFormData formData);

	UserFormData create(UserFormData formData);

	UserFormData load(UserFormData formData);

	UserFormData store(UserFormData formData);

	boolean isOwn(Long userId);

	UserTablePageData getUserTableData(SearchFilter filter);

	UserFormData getCurrentUserDetails();

	public String getUserTimeZone(Long userId);

	UserFormData getPassword(String userName);

	Long getUserIdByEmail(String email);

	boolean isEmailAlreadyUsed(String email);

	Long getUserId(String loginOrEmail);

	Set<String> getUserNotificationIds(Long userId);

	boolean isLoginAlreadyUsed(String login);

}
