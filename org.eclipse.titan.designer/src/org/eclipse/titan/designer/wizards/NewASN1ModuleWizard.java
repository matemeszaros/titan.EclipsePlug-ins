/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.SymbolicLinkHandler;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.editors.asn1editor.ASN1Editor;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.FileBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Kristof Szabados
 * */
public final class NewASN1ModuleWizard extends BasicNewResourceWizard {
	public static final String NEWASN1MODULEWIZARD = ProductConstants.PRODUCT_ID_DESIGNER + ".wizards.NewASN1ModuleWizard";
	private static final String WIZARD_TITLE = "New ASN1 Module";
	private static final String TRUE = "true";


	private NewASN1ModuleCreationWizardPage mainPage;
	private NewASN1ModuleOptionsWizardPage optionsPage;

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
		mainPage = new NewASN1ModuleCreationWizardPage(getSelection(), this);
		addPage(mainPage);

		optionsPage = new NewASN1ModuleOptionsWizardPage();
		addPage(optionsPage);
	}

	NewASN1ModuleOptionsWizardPage getOptionsPage() {
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
			mainPage.setFileName(mainPage.getFileName() + '.' + GlobalParser.SUPPORTED_ASN1_EXTENSIONS[1]);
		}
		final IFile newModule = mainPage.createNewFile();
		if (newModule != null) {
			try {
				if (optionsPage.isExcludeFromBuildSelected()) {
					newModule.setPersistentProperty(new QualifiedName(FileBuildPropertyData.QUALIFIER,
							FileBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY), TRUE);
				}

				ProjectFileHandler pfHandler = new ProjectFileHandler(newModule.getProject());
				pfHandler.saveProjectSettings();
				newModule.touch(new NullProgressMonitor());

				WorkspaceJob refreshJob = new WorkspaceJob("Refreshing built resources") {
					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) {
						boolean proceedingOK = SymbolicLinkHandler.createSymlinks(newModule);
						if (proceedingOK) {
							proceedingOK = TITANBuilder.regenerateMakefile(newModule.getProject());
						}
						if (proceedingOK) {
							proceedingOK = TITANBuilder.removeExecutable(newModule.getProject());
						}
						if (proceedingOK) {
							TITANBuilder.invokeBuild(newModule.getProject());
						}

						return Status.OK_STATUS;
					}
				};
				refreshJob.setPriority(Job.LONG);
				refreshJob.setUser(false);
				refreshJob.setSystem(true);
				refreshJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
				refreshJob.schedule();

				selectAndRevealNewModule(newModule);
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
			IEditorDescriptor desc = ASN1Editor.findASN1Editor(workbench);
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
