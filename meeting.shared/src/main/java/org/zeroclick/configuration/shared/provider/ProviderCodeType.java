package org.zeroclick.configuration.shared.provider;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class ProviderCodeType extends AbstractCodeType<Long, Long> {

	private static final long serialVersionUID = 1L;
	public static final long ID = 0L;

	@Override
	public Long getId() {
		return ID;
	}

	@Order(1000)
	public static class GoogleCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final Long ID = 1L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.api.provider.google");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

	@Order(2000)
	public static class TestProviderCode extends AbstractCode<Long> {
		private static final long serialVersionUID = 1L;
		public static final long ID = 2L;

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.api.provider.testProvider");
		}

		@Override
		public Long getId() {
			return ID;
		}
	}

}
