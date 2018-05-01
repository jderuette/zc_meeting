/**
 * Copyright 2017 Djer13

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
 */
package org.zeroclick.ui.form.fields.button.moreoptions;

import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.zeroclick.meeting.shared.Icons;

/**
 * @author djer
 *
 */
public abstract class AbstractMoreOptionButton extends AbstractButton {

	private static final String ICON_DISABLE = Icons.CaretRight;
	private static final String ICON_ENABLED = Icons.CaretDown;

	@Override
	protected String getConfiguredLabel() {
		return null;
	}

	@Override
	protected int getConfiguredDisplayStyle() {
		return DISPLAY_STYLE_TOGGLE;
	}

	@Override
	protected boolean getConfiguredLabelVisible() {
		return Boolean.FALSE;
	}

	@Override
	protected String getConfiguredIconId() {
		return ICON_DISABLE;
	}

	@Override
	protected boolean getConfiguredProcessButton() {
		return Boolean.FALSE;
	}

	@Override
	protected void execSelectionChanged(final boolean selection) {
		if (this.isSelected()) {
			this.show();
		} else {
			this.hide();
		}
	}

	private void show() {
		this.setIconId(ICON_ENABLED);
		this.showFields();
	}

	private void hide() {
		this.setIconId(ICON_DISABLE);
		this.hideFields();
	}

	protected abstract void showFields();

	protected abstract void hideFields();

}
