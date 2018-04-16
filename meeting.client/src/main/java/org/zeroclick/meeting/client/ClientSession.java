package org.zeroclick.meeting.client;

import java.util.Locale;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.zeroclick.configuration.shared.user.IUserService;
import org.zeroclick.configuration.shared.user.UserFormData;

/**
 * <h3>{@link ClientSession}</h3>
 *
 * @author djer
 */
public class ClientSession extends AbstractClientSession {

	public ClientSession() {
		super(true);
	}

	/**
	 * @return The {@link IClientSession} which is associated with the current
	 *         thread, or <code>null</code> if not found.
	 */
	public static IClientSession get() {
		return ClientSessionProvider.currentSession(IClientSession.class);
	}

	@Override
	protected void execLoadSession() {
		// pre-load all known code types
		CODES.getAllCodeTypes("org.zeroclick.meeting.shared");

		// retrieve user local from dataBase
		final IUserService userService = BEANS.get(IUserService.class);
		final UserFormData userDetails = userService.getCurrentUserDetails();
		if (null != userDetails.getLanguage().getValue() && !userDetails.getLanguage().getValue().isEmpty()) {
			this.setLocale(new Locale(userDetails.getLanguage().getValue()));
		}

		this.setDesktop(new Desktop());
	}

}
