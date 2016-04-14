/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titanium.organize.OrganizeImports;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The organize imports action, which can be called on files and folders.
 * 
 * @author poroszd
 * 
 */
public final class OrganizeFromBrowser extends AbstractHandler implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public void run(final IAction action) {
		doOrganizeFromBrowser();
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

		doOrganizeFromBrowser();

		return null;
	}

	/**
	 * Do the actual work of organizing the imports.
	 * Called from the file navigator/browser.
	 * */
	private void doOrganizeFromBrowser() {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return;
		}

		final List<IFile> files = new ArrayList<IFile>();
		final Deque<IResource> res = new LinkedList<IResource>();
		for (Object o : structSelection.toList()) {
			if (o instanceof IResource) {
				res.add((IResource) o);
			}
		}

		while (!res.isEmpty()) {
			final IResource r = res.pollFirst();
			if (r instanceof IProject) {
				final IProject proj = (IProject) r;
				try {
					for (IResource r2 : proj.members()) {
						res.addLast(r2);
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("Error while collecting members of project " + proj.getName(),e);
				}
			}
			if (r instanceof IFolder) {
				try {
					final IResource[] resInFolder = ((IFolder) r).members();
					for (IResource r2 : resInFolder) {
						res.addLast(r2);
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("Error while collecting members of folder " + r.getName(),e);
				}
			} else if (r instanceof IFile) {
				final IFile file = (IFile) r;
				final String extension = file.getFileExtension();
				if ("ttcn".equals(extension) || "ttcn3".equals(extension)) {
					files.add((IFile) r);
				}
			}
		}
 
		final boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
		if (reportDebugInformation) {
			TITANDebugConsole.println("These files will be organized: " + files.toString());
		}

		final OrganizeImportsOp op = new OrganizeImportsOp(files);
		final ProgressMonitorDialog pmd = new ProgressMonitorDialog(null);

		try {
			pmd.run(true, true, op);
		} catch (InvocationTargetException e) {
			ErrorReporter.logExceptionStackTrace("Error while organizing imports",e);
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace("Error while organizing imports",e);
			return;
		}
	}
}

class OrganizeImportsOp implements IRunnableWithProgress {
	private final List<IFile> files;

	public OrganizeImportsOp(final List<IFile> files) {
		this.files = files;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		final Set<IProject> projects = new HashSet<IProject>();
		for (IFile f : files) {
			projects.add(f.getProject());
		}

		monitor.beginTask("Organize imports", files.size() + projects.size() * 20);

		for (IProject project : projects) {
			final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
			final WorkspaceJob job = parser.analyzeAll();

			monitor.subTask("Waiting for semantic analysis on project " + project.getName());
			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("Error while waiting for the analysis to finish",e);
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(20);
		}

		final CompositeChange compChange = new CompositeChange("Organize imports");
		for (IFile f : files) {
			monitor.subTask("Organizing " + f.getName());
			try {
				final TextFileChange change = OrganizeImports.organizeImportsChange(f);
				compChange.add(change);
				compChange.perform(new SubProgressMonitor(monitor, 1));
				final MultiTextEdit edit = (MultiTextEdit) change.getEdit();
				if (edit != null && edit.getChildrenSize() > 0) {
					final WorkspaceJob job = GlobalParser.getProjectSourceParser(f.getProject()).reportOutdating(f);
					try {
						job.join();
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace("Error while waiting for the outdating report to finish",e);
					}
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Error while organizing file " + f.getName(),e);
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			monitor.worked(1);
		}

		for (IProject project : projects) {
			GlobalParser.getProjectSourceParser(project).analyzeAll();
		}

		monitor.done();
	}
}