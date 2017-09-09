
/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *
 *     Djer13 : Originale source : https://github.com/BSI-Business-Systems-Integration-AG/org.eclipse.scout.docs/blob/f3dccbc934bacb0ad71d2b5cba0aebdb6402ef6d/code/widgets/org.eclipse.scout.widgets.client/src/main/java/org/eclipse/scout/widgets/client/ui/forms/IPageForm.java
 ******************************************************************************/
package org.zeroclick.common.ui.form;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;

public interface IPageForm extends IForm {

	/**
	 * start the PageFormHandler
	 */
	void startPageForm();

	AbstractCloseButton getCloseButton();

}
