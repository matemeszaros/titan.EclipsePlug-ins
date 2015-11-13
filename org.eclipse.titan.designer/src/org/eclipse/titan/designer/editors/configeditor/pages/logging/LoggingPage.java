/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor.pages.logging;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.parsers.CommonHiddenStreamToken;
import org.eclipse.titan.common.parsers.LocationAST;
import org.eclipse.titan.common.parsers.cfg.ConfigTreeNodeUtilities;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler;
import org.eclipse.titan.common.parsers.cfg.indices.LoggingSectionHandler.LogParamEntry;
import org.eclipse.titan.designer.editors.configeditor.ConfigEditor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Kristof Szabados
 * */
public final class LoggingPage extends FormPage {

	private ScrolledForm form;
	private ConfigEditor editor;
	private LoggingSectionHandler loggingSectionHandler;

	private GeneralOptionsSubPage generalOptions;
	private LoggingTreeSubPage componentpluginSection;
	private LoggingBitsSubPage loggingBitsSubPage;

	public LoggingPage(final ConfigEditor editor) {
		super(editor, "Logging", "Logging");

		this.editor = editor;
		generalOptions = new GeneralOptionsSubPage(editor, this);
		componentpluginSection = new LoggingTreeSubPage(editor, this);
		loggingBitsSubPage = new LoggingBitsSubPage(editor, this);
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Logging section");
		form.setBackgroundImage(ImageCache.getImage("form_banner.gif"));

		GridLayout layoutParent = new GridLayout();
		form.getBody().setLayout(layoutParent);

		Composite composite = toolkit.createComposite(form.getBody());

		GridLayout layout = new GridLayout(2, true);
		layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		componentpluginSection.createSectionComponent(toolkit, composite);
		generalOptions.createSectionGeneral(toolkit, composite);
		loggingBitsSubPage.createSectionLoggingBits(toolkit, managedForm.getForm(), composite);

		setErrorMessage();
	}

	public void refreshData(final LoggingSectionHandler loggingSectionHandler) {
		this.loggingSectionHandler = loggingSectionHandler;
		componentpluginSection.refreshData(loggingSectionHandler);
		generalOptions.refreshData(loggingSectionHandler, null);
		loggingBitsSubPage.refreshData(loggingSectionHandler, null);
	}

	public void refreshData(final LoggingSectionHandler loggingSectionHandler, final LogParamEntry logentry) {
		this.loggingSectionHandler = loggingSectionHandler;
		generalOptions.refreshData(loggingSectionHandler, logentry);
		loggingBitsSubPage.refreshData(loggingSectionHandler, logentry);
	}

	public LoggingSectionHandler.LoggerTreeElement getSelectedTreeElement() {
		return componentpluginSection.getSelection();
	}

	public void treeElementAdded(final LoggingSectionHandler.LoggerTreeElement lte, final LogParamEntry logentry) {
		this.loggingSectionHandler = lte.getLsh();
		generalOptions.initializeEntry(lte, logentry);
		generalOptions.refreshData(loggingSectionHandler, logentry);
		loggingBitsSubPage.initializeEntry(lte, logentry);
		loggingBitsSubPage.refreshData(loggingSectionHandler, logentry);
	}

	/*
	 * public void pluginRenamed() { generalOptions.pluginRenamed();
	 * loggingBitsSubPage.pluginRenamed(); }
	 */

	public void treeElementSelected(final LoggingSectionHandler.LoggerTreeElement lte) {
		LogParamEntry logentry = loggingSectionHandler.componentPlugin(lte.getComponentName(), lte.getPluginName());
		generalOptions.refreshData(loggingSectionHandler, logentry);
		loggingBitsSubPage.refreshData(loggingSectionHandler, logentry);
	}

	public void createLoggingSection() {
		if (loggingSectionHandler == null || loggingSectionHandler.getLastSectionRoot() != null) {
			return;
		}
		loggingSectionHandler.setLastSectionRoot(new LocationAST("[LOGGING]"));
		loggingSectionHandler.getLastSectionRoot().setHiddenBefore(new CommonHiddenStreamToken("\n"));
		LocationAST sectionRoot = new LocationAST("");
		sectionRoot.setFirstChild(loggingSectionHandler.getLastSectionRoot());
		LocationAST root = editor.getParseTreeRoot();
		if (root != null) {
			root.addChild(sectionRoot);
		}
	}

	public void removeLoggingSection() {
		if (loggingSectionHandler == null || loggingSectionHandler.getLastSectionRoot() == null) {
			return;
		}
		if (loggingSectionHandler.getComponents().isEmpty()) {
			ConfigTreeNodeUtilities.removeFromChain(editor.getParseTreeRoot().getFirstChild(), loggingSectionHandler.getLastSectionRoot()
					.getParent());
			loggingSectionHandler.setLastSectionRoot(null);
		}
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
