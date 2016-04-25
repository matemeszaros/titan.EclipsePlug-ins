/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.Activator;

/**
 * Base class for jobs that requires analyzing the project.
 * <p>
 * This class simplifies executing asynchronous jobs, that are somehow dependent
 * on the up-to-date semantic analysis of the project.
 * </p>
 * <p>
 * Upon starting this job, a new semantic analyzer job is scheduled on a
 * specified project. After scheduling, the overridable method
 * {@link #doPreWork()} is called, then the analyzer job is joined, then
 * {@link #doPostWork()} is called.
 * </p>
 * 
 * @author poroszd
 * 
 */
public class ProjectAnalyzerJob extends WorkspaceJob {
	private IProject project;

	/**
	 * Constructs a project analyzer workspace job
	 * 
	 * @param name
	 *            the name of the job
	 */
	public ProjectAnalyzerJob(final String name) {
		super(name);
	}

	/**
	 * Runs the operation, which actually starts an other job for analyzing the
	 * specified project. Before joining and after joining this analyzer job one
	 * can execute operations by overriding the appropriate methods.
	 */
	@Override
	public final IStatus runInWorkspace(final IProgressMonitor monitor) {
		final SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (project == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Project not specified for ProjectAnalyzerJob");
		}

		final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		final WorkspaceJob job = parser.analyzeAll();
		if (job == null) {
			// maybe parsing is disabled
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					final Display disp = Display.getDefault();
					final Shell shell = new Shell(disp, SWT.SHELL_TRIM);
					shell.setText("Unavailable operation");
					final String errorMessage = "This operation can not be executed while project parsing is disabled.\n\n"
							+ "Please enable parsing on the preference page: Window/Preferences/ TITAN Preferences/"
							+ "On-the-fly checker/ Enable parsing of TTCN-3, ASN.1 and Runtime Configuration files";
					MessageDialog.openInformation(shell, "Confronting settings", errorMessage);
				}
			});
			return Status.CANCEL_STATUS;
		}

		final IStatus preStatus = doPreWork(progress.newChild(20));
		progress.setWorkRemaining(80);
		if (!preStatus.isOK()) {
			return preStatus;
		}

		try {
			job.join();
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace("Error while analyzing", e);
		}
		progress.setWorkRemaining(30);

		return doPostWork(progress.newChild(30));
	}

	/**
	 * Hook operation that runs in parallel with the analyzis.
	 * <p>
	 * If you need it, override to do what you want. The default implementation
	 * does nothing, and returns {@link Status.OK_STATUS}
	 * </p>
	 * 
	 * @return the result of running the operation
	 */
	public IStatus doPreWork(final IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

	/**
	 * Hook operation that runs after the analyzis has completed.
	 * <p>
	 * If you need it, override to do what you want. The default implementation
	 * does nothing, and returns {@link Status.OK_STATUS}
	 * </p>
	 * 
	 * @return the result of running the operation
	 */
	public IStatus doPostWork(final IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

	/**
	 * Get the project on which the analysis will be executed.
	 * 
	 * @return the specified project
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Set the project on which the analysis will be executed.
	 * <p>
	 * NOTE: unless you start this job by {@link #quickSchedule(IProject)}, you
	 * must use this method to specify a valid project to work on, or else an
	 * error will occur.
	 * </p>
	 * 
	 * @param p
	 *            the project to analyze
	 */
	public void setProject(final IProject p) {
		project = p;
	}

	/**
	 * Convenience method to schedule this job.
	 * <p>
	 * The job is scheduled with the following settings:
	 * <ul>
	 * <li>system = false</li>
	 * <li>user = true</li>
	 * </ul>
	 * 
	 * Using this method, you can also omit explicitly setting the project (by
	 * {@link #setProject(IProject)}).
	 * 
	 * @param project
	 */
	public ProjectAnalyzerJob quickSchedule(final IProject project) {
		this.project = project;
		setSystem(false);
		setUser(true);
		schedule();
		return this;
	}
}
