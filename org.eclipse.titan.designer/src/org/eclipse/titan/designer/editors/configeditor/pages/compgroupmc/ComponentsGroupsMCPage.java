/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.compgroupmc;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.parsers.cfg.indices.ComponentSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.GroupSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.MCSectionHandler;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Kristof Szabados
 * */
public final class ComponentsGroupsMCPage extends FormPage {

	private ConfigEditor editor;
	private ScrolledForm form;
	private ComponentsSubPage componentsSubPage;
	private GroupsSubPage groupsSubPage;
	private MCSubPage mcsubPage;

	public ComponentsGroupsMCPage(final ConfigEditor editor) {
		super(editor, "Components_Groups_MainController_section_page", "Components, Groups and Main Controller");
		this.editor = editor;

		componentsSubPage = new ComponentsSubPage(editor);
		groupsSubPage = new GroupsSubPage(editor);
		mcsubPage = new MCSubPage(editor);
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Components, Groups and main controller sections");
		form.setBackgroundImage(ImageCache.getImage("form_banner.gif"));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		form.getBody().setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite client = toolkit.createComposite(form.getBody(), SWT.WRAP);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		client.setLayout(layout);
		client.setLayoutData(new GridData(GridData.FILL_BOTH));

		mcsubPage.createMainControllerSection(client, form, toolkit);
		componentsSubPage.createComponentsSection(client, form, toolkit);

		groupsSubPage.createGroupsSection(form.getBody(), form, toolkit);

		setErrorMessage();
	}

	public void refreshData(final ComponentSectionHandler componentSectionHandler, final GroupSectionHandler groupSectionHandler,
			final MCSectionHandler mcSectionHandler) {
		componentsSubPage.refreshData(componentSectionHandler);
		groupsSubPage.refreshData(groupSectionHandler);
		mcsubPage.refreshData(mcSectionHandler);
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
