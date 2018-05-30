package org.zeroclick.configuration.shared.user;

import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.zeroclick.configuration.shared.onboarding.OnBoardingUserFormData;

@ApplicationScoped
@TunnelToServer
public interface IUserService extends IService {

	UserFormData prepareCreate(UserFormData formData);

	UserFormData create(UserFormData formData);

	UserFormData load(UserFormData formData);

	UserFormData store(UserFormData formData);

	void delete(UserFormData userFormData);

	boolean isOwn(Long userId);

	UserTablePageData getUserTableData(SearchFilter filter);

	UserFormData getCurrentUserDetails();

	UserFormData getUserDetails(Long userId);

	public String getUserTimeZone(Long userId);

	UserFormData getPassword(String userName);

	UserFormData getPassword(UserFormData userPassFormData);

	Long getUserIdByEmail(String email);

	boolean isEmailAlreadyUsed(String email);

	Long getUserId(String loginOrEmail);

	Set<String> getUserNotificationIds(Long userId);

	boolean isLoginAlreadyUsed(String login);

	OnBoardingUserFormData load(OnBoardingUserFormData formData);

	OnBoardingUserFormData store(OnBoardingUserFormData formData);

	String getUserLanguage(Long guestId);

	String getUserEmail(Long userId);

	Set<Long> getAllUserId();

	/**
	 * For data migration
	 */
	void addSubFreeToAllUsers();

	ValidateCpsFormData load(ValidateCpsFormData formData);

	ValidateCpsFormData store(ValidateCpsFormData formData);

	ValidateCpsFormData create(ValidateCpsFormData formData);

	ValidateCpsFormData getActiveSubscriptionDetails(Long userId);

	/**
	 * Use to indicate this user just logged In
	 *
	 * @param savedPassword
	 */
	UserFormData loggedIn(UserFormData userData);

	/**
	 * Clear userCache and User permission cache
	 */
	void clearCache(Long userId);

}
