package org.zeroclick.configuration.shared.user;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class LanguageCodeType extends AbstractCodeType<String, String> {

	private static final long serialVersionUID = 1L;
	public static final String ID = "en";

	@Override
	public String getId() {
		return ID;
	}

	@Order(1000)
	public static class FrenchCode extends AbstractCode<String> {
		private static final long serialVersionUID = 1L;
		public static final String ID = "fr";

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.user.language.fr");
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	@Order(2000)
	public static class EnglishCode extends AbstractCode<String> {
		private static final long serialVersionUID = 1L;
		public static final String ID = "en";

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.user.language.en");
		}

		@Override
		public String getId() {
			return ID;
		}
	}
}
