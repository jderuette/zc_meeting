package org.zeroclick.meeting.shared.event.involevment;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.zeroclick.meeting.shared.Icons;

public class EventRoleCodeType extends AbstractCodeType<Long, String> {

	private static final long serialVersionUID = 1L;
	public static final Long ID = 5000L;

	@Override
	public Long getId() {
		return ID;
	}

	public class OrganizerCode extends AbstractCode<String> {
		/** serialVersionUID */
		private static final long serialVersionUID = -4359963789539694386L;

		public static final String ID = "ORGANIZER";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.role.organizer");
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.Person;
		}

		@Override
		protected java.lang.String getConfiguredBackgroundColor() {
			return "#2525b0";
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	public class RequiredGuestCode extends AbstractCode<String> {
		/** serialVersionUID */
		private static final long serialVersionUID = 5893291485629518581L;

		public static final String ID = "REQUIRED_GUEST";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.role.requiredGuest");
		}

		@Override
		protected java.lang.String getConfiguredBackgroundColor() {
			return "#e28b0f";
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.ExclamationMark;
		}

		@Override
		public String getId() {
			return ID;
		}
	}

}
