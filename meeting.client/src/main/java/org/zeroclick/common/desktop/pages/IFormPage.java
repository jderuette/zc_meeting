/**
 * Djer13 : Original source : https://github.com/BSI-Business-Systems-Integration-AG/org.eclipse.scout.docs/blob/f3dccbc934bacb0ad71d2b5cba0aebdb6402ef6d/code/widgets/org.eclipse.scout.widgets.client/src/main/java/org/eclipse/scout/widgets/client/ui/desktop/pages/IFormPage.java
 */
package org.zeroclick.common.desktop.pages;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.zeroclick.common.ui.form.IPageForm;

public interface IFormPage extends IPageWithNodes {

	Class<? extends IPageForm> getFormType();

	String getText();
}
