package org.zeroclick.configuration.shared.slot;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

@SuppressWarnings("PMD.ShortVariable")
public class SlotCodeType extends AbstractCodeType<Long, Long> {

	private static final long serialVersionUID = 1L;
	public static final long ID = 0L;

	@Override
	public Long getId() {
		return ID;
	}

	@Order(1000)
	public static class DayCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final Long ID = 1L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.slot.1");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(2000)
	public static class LunchCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final Long ID = 2L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.slot.2");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(3000)
	public static class EveningCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final Long ID = 3L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.slot.3");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(4000)
	public static class WeekendCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final Long ID = 4L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.slot.4");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

}
