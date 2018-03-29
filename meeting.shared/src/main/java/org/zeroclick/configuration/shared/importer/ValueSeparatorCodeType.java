package org.zeroclick.configuration.shared.importer;

import java.util.List;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

@SuppressWarnings("PMD.ShortVariable")
public class ValueSeparatorCodeType extends AbstractCodeType<Long, String> {

	private static final long serialVersionUID = 1L;
	public static final long ID = 0L;

	@Override
	public Long getId() {
		return ID;
	}

	public ICode<String> getCodeByText(final String separator) {
		final List<? extends ICode<String>> codes = this.getCodes();
		for (final ICode<String> code : codes) {
			if (CompareUtility.equals(code.getText(), separator)) {
				return code; // earlyBreak
			}
		}
		return null;
	}

	@Order(1000)
	public static class SemicolonCode extends AbstractCode<String> {
		private static final long serialVersionUID = 1L;
		public static final String ID = ";";

		@Override
		protected String getConfiguredText() {
			return ";";
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	@Order(2000)
	public static class CommaCode extends AbstractCode<String> {
		private static final long serialVersionUID = 1L;
		public static final String ID = ",";

		@Override
		protected String getConfiguredText() {
			return ",";
		}

		@Override
		public String getId() {
			return ID;
		}
	}

	@Order(3000)
	public static class EndOfLineCode extends AbstractCode<String> {
		private static final long serialVersionUID = 1L;
		public static final String ID = "\n";

		@Override
		protected String getConfiguredText() {
			return TEXTS.get("zc.meeting.event.importEmails.separator.OneByLine");
		}

		@Override
		public String getId() {
			return ID;
		}
	}
}
