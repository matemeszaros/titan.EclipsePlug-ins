/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.editors.configeditor.ConfigTextEditor;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Kristof Szabados
 * */
public final class NewConfigFileWizard extends BasicNewResourceWizard {

	public static final String NEWCONFIGFILEWIZARD = ProductConstants.PRODUCT_ID_DESIGNER + ".wizards.NewConfigFileWizard";
	private static final String WIZARD_TITLE = "New Configuration File";
	private NewConfigFileCreationWizardPage mainPage;
	private NewConfigFileOptionsWizardPage optionsPage;

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#init(org
	 * .eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(WIZARD_TITLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		mainPage = new NewConfigFileCreationWizardPage(getSelection(), this);
		addPage(mainPage);

		optionsPage = new NewConfigFileOptionsWizardPage();
		addPage(optionsPage);
	}

	NewConfigFileOptionsWizardPage getOptionsPage() {
		return optionsPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (mainPage.getContainerFullPath().append(mainPage.getFileName()).getFileExtension() == null) {
			mainPage.setFileName(mainPage.getFileName() + '.' + GlobalParser.SUPPORTED_CONFIG_FILE_EXTENSIONS[0]);
		}
		IFile newConfigFile = mainPage.createNewFile();
		if (newConfigFile != null) {
			try {
				ProjectFileHandler pfHandler = new ProjectFileHandler(newConfigFile.getProject());
				pfHandler.saveProjectSettings();
				newConfigFile.touch(new NullProgressMonitor());

				PropertyNotificationManager.firePropertyChange(newConfigFile);

				selectAndRevealNewModule(newConfigFile);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		return true;
	}

	/**
	 * Opens the new module in an editor, plus selects and reveals it in
	 * every open windows.
	 * 
	 * @param newModule
	 *                the module to be revealed
	 * */
	private void selectAndRevealNewModule(final IFile newModule) {
		IWorkbench workbench = getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			IEditorDescriptor desc = ConfigTextEditor.findCFGEditor(workbench);
			IWorkbenchPage page = window.getActivePage();
			try {
				page.openEditor(new FileEditorInput(newModule), desc.getId());
			} catch (PartInitException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		// select and reveal the new file in every window open
		selectAndReveal(newModule);
	}
}
