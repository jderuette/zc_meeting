package org.zeroclick.meeting.shared.event;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.zeroclick.meeting.shared.Icons;

@IgnoreBean
@SuppressWarnings("PMD.ShortVariable")
public class StateCodeType extends AbstractCodeType<Long, String> {

	private static final long serialVersionUID = 1L;
	public static final long ID = 10000L;

	@Override
	public Long getId() {
		return ID;
	}

	@Override
	protected String getConfiguredText() {
		return TEXTS.get("zc.meeting.state");
	}

	public class AskedCode extends AbstractCode<String> {

		/** serialVersionUID */
		private static final long serialVersionUID = 7182523415617846648L;
		public static final String ID = "ASKED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.state.asked");
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.Clock;
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	public class AcceptedCode extends AbstractCode<String> {

		/** serialVersionUID */
		private static final long serialVersionUID = -3203867626399103671L;
		public static final String ID = "ACCEPTED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.state.accepted");
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.Checked;
		}

		@Override
		protected java.lang.String getConfiguredBackgroundColor() {
			return "#4b8e34";
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	public class RefusededCode extends AbstractCode<String> {

		/** serialVersionUID */
		private static final long serialVersionUID = -1974787711008790961L;
		public static final String ID = "REFUSED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.state.refused");
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.Remove;
		}

		@Override
		protected java.lang.String getConfiguredBackgroundColor() {
			return "#db4a15";
		}

		@Override
		public String getId() {
			return ID;
		}
	}
}
