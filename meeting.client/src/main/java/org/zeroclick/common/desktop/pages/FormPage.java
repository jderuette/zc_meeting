/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.zeroclick.common.desktop.pages;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.TEXTS;
//import org.eclipse.scout.widgets.client.deeplink.WidgetsDeepLinkHandler;
//import org.eclipse.scout.widgets.client.ui.desktop.menu.AbstractViewSourceOnGitHubMenu;
//import org.eclipse.scout.widgets.client.ui.forms.IPageForm;
import org.zeroclick.common.ui.form.IPageForm;

public class FormPage extends AbstractPageWithNodes implements IFormPage {

	private final Class<? extends IPageForm> m_formType;
	private boolean m_enabled = true;

	public FormPage(final Class<? extends IPageForm> formType) {
		super(false, formType.getName());
		this.m_formType = formType;
		this.callInitializer();
	}

	public FormPage(final Class<? extends IPageForm> formType, final boolean enabled) {
		super(false, formType.getName());
		this.m_formType = formType;
		this.m_enabled = enabled;
		this.callInitializer();
	}

	public FormPage(final Class<? extends IPageForm> formType, final boolean enabled, final String menuTitle) {
		super(false, formType.getName());
		this.m_formType = formType;
		this.m_enabled = enabled;
		this.getCellForUpdate().setText(menuTitle);
		this.callInitializer();
	}

	@Override
	protected boolean getConfiguredEnabled() {
		return this.m_enabled;
	}

	@Override
	protected boolean getConfiguredLeaf() {
		return true;
	}

	@Override
	protected void execInitPage() {
		this.ensureText();
		this.setTableVisible(false);
	}

	protected void ensureText() {
		if (this.getCellForUpdate().getText() == null) {
			String s = this.m_formType.getSimpleName();
			s = s.replaceAll("Form$", "");
			final String t = TEXTS.getWithFallback(s, null);
			if (t == null) {
				s = s.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
			}
			this.getCellForUpdate().setText(s);
		}
	}

	@Override
	public String getText() {
		this.ensureText();
		return this.getCell().getText();
	}

	@Override
	protected void execPageActivated() {
		// final WidgetsDeepLinkHandler deepLinkHandler =
		// BEANS.get(WidgetsDeepLinkHandler.class);
		// final IDesktop desktop =
		// ClientSessionProvider.currentSession().getDesktop();
		// desktop.setBrowserHistoryEntry(deepLinkHandler.createBrowserHistoryEntry(this));
	}

	@Override
	protected void ensureDetailFormCreated() {
		if (this.m_enabled) {
			super.ensureDetailFormCreated();
		}
	}

	@Override
	public IPageForm getDetailForm() {
		return (IPageForm) super.getDetailForm();
	}

	@Override
	protected Class<? extends IForm> getConfiguredDetailForm() {
		return this.m_formType;
	}

	@Override
	public Class<? extends IPageForm> getFormType() {
		return this.m_formType;
	}

	@Override
	protected void execInitDetailForm() {
		if (this.getDetailForm() != null) {
			this.getDetailForm().getCloseButton().setVisibleGranted(false);
		}
	}

	@Override
	protected void startDetailForm() {
		this.getDetailForm().startPageForm();
	}
}