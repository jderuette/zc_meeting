package org.zeroclick.test;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;

public abstract class AbstractTestNodePage extends AbstractPageWithNodes {

	@Override
	protected String getConfiguredTitle() {
		// TODO [djer] verify translation
		return "AbstractTestNodePage";
	}
}
