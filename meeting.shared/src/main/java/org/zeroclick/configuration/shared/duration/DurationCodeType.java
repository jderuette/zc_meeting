package org.zeroclick.configuration.shared.duration;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

@SuppressWarnings("PMD.ShortVariable")
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

	public static String getText(final Long durationCodeTypeId) {
		String text = null;
		final ICode<Long> durationCode = new DurationCodeType().getCode(durationCodeTypeId);
		if (null != durationCode) {
			text = durationCode.getText();
		}
		return text;
	}

	public static String getText(final Double nbMinutes) {
		final StringBuilder builder = new StringBuilder();
		builder.append(convertMinuteToHours(nbMinutes)).append(' ').append(getTimeUnit(nbMinutes));
		return builder.toString();
	}

	private static Integer convertMinuteToHours(final Double nbMinutes) {
		int minutesToHours = nbMinutes.intValue();
		if (minutesToHours >= 60) {
			minutesToHours = minutesToHours / 60;
		}
		return minutesToHours;
	}

	public static Double getValue(final Long durationId) {
		Double value = null;
		final ICode<Long> durationCode = new DurationCodeType().getCode(durationId);
		if (null != durationCode) {
			value = durationCode.getValue().doubleValue();
		}
		return value;
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
