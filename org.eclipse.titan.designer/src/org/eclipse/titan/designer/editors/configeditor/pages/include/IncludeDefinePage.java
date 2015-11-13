/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.include;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.parsers.cfg.indices.DefineSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.IncludeSectionHandler;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Kristof Szabados
 * */
public final class IncludeDefinePage extends FormPage {

	private ConfigEditor editor;
	private ScrolledForm form;
	private IncludeSubPage includeSubPage;
	private DefineSubPage defineSubPage;

	public IncludeDefinePage(final ConfigEditor editor) {
		super(editor, "Include_Define_section_page", "Include and Define");
		this.editor = editor;
		includeSubPage = new IncludeSubPage(editor);
		defineSubPage = new DefineSubPage(editor);
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Include and Define sections");
		form.setBackgroundImage(ImageCache.getImage("form_banner.gif"));

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		form.getBody().setLayout(layout);

		Composite componentsMC = toolkit.createComposite(form.getBody());
		layout = new GridLayout();
		layout.numColumns = 1;
		componentsMC.setLayout(layout);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessVerticalSpace = true;
		componentsMC.setLayoutData(gd);

		includeSubPage.createIncludeSection(componentsMC, form, toolkit);
		defineSubPage.createDefinitionSection(componentsMC, form, toolkit);

		setErrorMessage();
	}

	public void refreshData(final IncludeSectionHandler includeSectionHandler, final DefineSectionHandler defineSectionHandler) {
		includeSubPage.refreshData(includeSectionHandler);
		defineSubPage.refreshData(defineSectionHandler);
	}

	@Override
	public void setActive(final boolean active) {
		setErrorMessage();
		super.setActive(active);
	}

	public void setErrorMessage() {
		if (form != null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (editor.getErrorMessage() == null) {
						form.getForm().setMessage(null, IMessageProvider.NONE);
					} else {
						form.getForm().setMessage(editor.getErrorMessage(), IMessageProvider.ERROR);
					}
					form.getForm().getHead().layout();
					form.getForm().getHead().redraw();
				}
			});
		}
	}
}
