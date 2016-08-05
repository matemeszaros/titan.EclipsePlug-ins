/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * @author Kristof Szabados
 * */
public class NewTITANProjectImportWizard extends BasicNewResourceWizard implements IImportWizard {

	private static final String NEWPROJECT_WINDOWTITLE = "Import new TITAN Project from .tpd file";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Project";
	private static final String NEWPROJECT_DESCRIPTION = "Create a new TITAN project in the workspace or in an external location";

	private NewTITANProjectImportMainPage mainPage;
	private NewTITANProjectImportOptionsPage optionsPage;
	private TpdImporter tpdImporter;

	public NewTITANProjectImportWizard() {
		tpdImporter = new TpdImporter(Display.getDefault().getActiveShell(), false);
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new NewTITANProjectImportMainPage(NEWPROJECT_WINDOWTITLE);
		mainPage.setTitle(NEWPROJECT_TITLE);
		mainPage.setDescription(NEWPROJECT_DESCRIPTION);
		addPage(mainPage);

		optionsPage = new NewTITANProjectImportOptionsPage();
		addPage(optionsPage);
	}

	@Override
	public boolean performFinish() {
		final List<IProject> projectsCreated = new ArrayList<IProject>();
		try {
			new ProgressMonitorDialog(null).run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					boolean result = true;
					monitor.beginTask("Importing the data of the project", 1);

					try {
						result = tpdImporter.internalFinish(mainPage.getProjectFile(), optionsPage.isSkipExistingProjects(),
								optionsPage.isOpenPropertiesForAllImports(), projectsCreated, monitor, mainPage.getSearchPaths());
					} catch (Exception e) {
						ErrorReporter.logExceptionStackTrace(e);
						result = false;
					}

					if (!result) {
						for (IProject project : projectsCreated) {
							try {
								project.delete(true, null);
							} catch (CoreException e) {
								ErrorReporter.logExceptionStackTrace(e);
							}
						}
						ErrorReporter.parallelErrorDisplayInMessageDialog(
								"Import failed",
								"There were some errors during import.\nPlease check the error log for more information.");
					}

					monitor.done();
				}
			});
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return true;
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
		setWindowTitle(NEWPROJECT_WINDOWTITLE);

		super.init(workbench, selection);
	}
}
