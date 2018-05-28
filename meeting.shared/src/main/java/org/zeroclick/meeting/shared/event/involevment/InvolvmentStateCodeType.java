package org.zeroclick.meeting.shared.event.involevment;

import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.zeroclick.meeting.shared.Icons;

public class InvolvmentStateCodeType extends AbstractCodeType<Long, String> {

	private static final long serialVersionUID = 1L;
	public static final Long ID = 4000L;

	@Override
	public Long getId() {
		return ID;
	}

	public class ProposedCode extends AbstractCode<String> {
		/** serialVersionUID */
		private static final long serialVersionUID = 8406863452376914473L;

		public static final String ID = "PROPOSED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.state.proposed");
		}

		@Override
		protected java.lang.String getConfiguredIconId() {
			return Icons.Person;
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	public class AskedCode extends AbstractCode<String> {
		/** serialVersionUID */
		private static final long serialVersionUID = 7862974337614224789L;

		public static final String ID = "ASKED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.state.asked");
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
		private static final long serialVersionUID = -3914328502697862795L;

		public static final String ID = "ACCEPTED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.state.accepted");
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

	public class RefusedCode extends AbstractCode<String> {
		/** serialVersionUID */
		private static final long serialVersionUID = -5705330921475894878L;

		public static final String ID = "REFUSED";

		@Override
		protected java.lang.String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.involevment.state.refused");
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
