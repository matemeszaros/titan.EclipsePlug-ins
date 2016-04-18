/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titanium.markers.export.BaseProblemExporter;
import org.eclipse.titanium.markers.export.XlsProblemExporter;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Export problem markers of a project to an xls.
 * <p>
 * This class handles only the action, the real work is done in
 * {@link XlsProblemExporter}.
 * 
 * @author poroszd
 * 
 */
public final class ExportProblems extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public void run(final IAction action) {
		doExportProblems();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		doExportProblems();

		return null;
	}
	
	private void doExportProblems() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return;
		}

		final Object firstElement = structSelection.getFirstElement();
		if (!(firstElement instanceof IProject)) {
			ErrorReporter.logError("The export problems command needs to be called on a project ");

			return;
		}

		final IProject project = (IProject) firstElement;

		final IPreferencesService preferencesService = Platform.getPreferencesService();
		final boolean reportDebugInformation = 
				preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
		if (reportDebugInformation) {
			TITANDebugConsole.println("Problem markers are to export from " + project.getName());
		}

		boolean write = false;
		String fileName;
		final Shell shell = Display.getCurrent().getActiveShell();
		do {
			final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			dialog.setText("Export problem markers to xls");
			dialog.setFilterExtensions(new String[] { "*.xls" });

			final IPath path = project.getLocation();
			if (path != null) {
				dialog.setFilterPath(path.toPortableString());
			}
			final Calendar now = Calendar.getInstance();
			dialog.setFileName("Problems--" + project.getName() + "--" + now.get(Calendar.YEAR) + "-" + (1 + now.get(Calendar.MONTH)) + "-"
					+ now.get(Calendar.DAY_OF_MONTH));
			fileName = dialog.open();
			if (fileName != null) {
				if (new File(fileName).exists()) {
					write = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "File exist",
							"This file already exists. Please confirm overwrite.");
				} else {
					write = true;
				}
			} else {
				// User cancelled the file dialog, so we have nothing to do
				return;
			}
		} while (!write);

		final String fileName2 = fileName;

		new ProjectAnalyzerJob("Exporting reported code smells") {
			@Override
			public IStatus doPostWork(final IProgressMonitor monitor) {
				final BaseProblemExporter exporter = new XlsProblemExporter(getProject());
				try {
					exporter.exportMarkers(monitor, fileName2, Calendar.getInstance().getTime());
					if (reportDebugInformation) {
						TITANDebugConsole.println("Successfully exported markers to xls");
					}
				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace("Error while exporting", e);
					if (reportDebugInformation) {
						TITANDebugConsole.println("Failed to write xls");
					}
				}
				return Status.OK_STATUS;
			}
		}.quickSchedule(project);
	}
}
