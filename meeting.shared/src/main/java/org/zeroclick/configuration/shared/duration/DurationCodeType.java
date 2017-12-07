package org.zeroclick.configuration.shared.duration;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class DurationCodeType extends AbstractCodeType<Long, Long> {

	private static final long serialVersionUID = 1L;
	public static final long ID = 0L;

	@Override
	public Long getId() {
		return ID;
	}

	protected static String getTimeUnit(final Double nbMinutes) {
		String unit = null;
		if (nbMinutes < 60) {
			unit = TEXTS.get("zc.common.date.minutes");
		} else {
			unit = TEXTS.get("zc.common.date.hours");
		}

		return unit;
	}

	protected static String getText(final Double nbMinutes) {
		return nbMinutes.intValue() + " " + getTimeUnit(nbMinutes);
	}

	@Order(1000)
	public static class QuarterHourCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 1L;

		@Override
		protected String getConfiguredText() {
			return DurationCodeType.getText(this.getConfiguredValue());
		}

		@Override
		protected Double getConfiguredValue() {
			return 15D;
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(2000)
	public static class HalfHourCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 2L;

		@Override
		protected String getConfiguredText() {
			return DurationCodeType.getText(this.getConfiguredValue());
		}

		@Override
		protected Double getConfiguredValue() {
			return 30D;
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(3000)
	public static class ThreeQuarterHourCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 3L;

		@Override
		protected String getConfiguredText() {
			return DurationCodeType.getText(this.getConfiguredValue());
		}

		@Override
		protected Double getConfiguredValue() {
			return 45D;
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(4000)
	public static class OneHourCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 4L;

		@Override
		protected String getConfiguredText() {
			return DurationCodeType.getText(this.getConfiguredValue());
		}

		@Override
		protected Double getConfiguredValue() {
			return 60D;
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(5000)
	public static class TwoHourCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 5L;

		@Override
		protected String getConfiguredText() {
			return DurationCodeType.getText(this.getConfiguredValue());
		}

		@Override
		protected Double getConfiguredValue() {
			return 120D;
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

}
