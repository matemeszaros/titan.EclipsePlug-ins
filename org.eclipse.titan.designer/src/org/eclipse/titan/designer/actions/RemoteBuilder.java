/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.core.CompilerVersionInformationCollector;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANJob;
import org.eclipse.titan.designer.decorators.TITANDecorator;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.properties.data.BuildLocation;
import org.eclipse.titan.designer.properties.data.ProjectRemoteBuildPropertyData;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This class takes care of the remote builds of projects.
 *
 * @author Kristof Szabados
 */
public final class RemoteBuilder extends AbstractHandler implements IObjectActionDelegate {

	/**
	 * This Job type handles the execution of the remotely building commands.
	 *
	 */
	private static final class RemoteBuilderJob extends Job {
		static final String TTCN3_LICENSE_FILE = "TTCN3_LICENSE_FILE";
		static final String TTCN3_DIR = "TTCN3_DIR";

		private final IProject project;
		private final List<String> hostnames;
		private final List<String> commands;
		private final List<String> descriptions;
		
		private final JobChangeAdapter decoratorUpdater = new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				try {
					if (event.getResult().isOK()) {
						project.setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, true);
					} else {
						project.setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, false);
					}
					TITANDecorator.refreshSelectively(project);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		};

		public RemoteBuilderJob(final String name, final IProject project, final String hostName,
				final String command, final String description) {
			super(name);
			this.project = project;
			this.hostnames = new ArrayList<String>();
			this.hostnames.add(hostName);
			this.commands = new ArrayList<String>();
			this.commands.add(command);
			this.descriptions = new ArrayList<String>();
			this.descriptions.add(description);
			addJobChangeListener(decoratorUpdater);
		}

		public RemoteBuilderJob(final String name, final IProject project, final List<String> hostnames,
				final List<String> commands, final List<String> descriptions) {
			super(name);
			this.project = project;
			this.hostnames = hostnames;
			this.commands = commands;
			this.descriptions = descriptions;
			addJobChangeListener(decoratorUpdater);
		}

		/**
		 * Executes each remote build command set for the remote build hosts.
		 *
		 * The outputs of these processes are redirected into the TITAN Console, each line being prefixed with the remote hosts name.
		 *
		 * @param monitor the progress monitor to report errors to.
		 * @return the status of the operation when it finshes.
		 */
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;

			internalMonitor.beginTask(getName(), commands.size());

			ProcessBuilder pb = new ProcessBuilder();
			Map<String, String> env = pb.environment();
			env.put(TTCN3_LICENSE_FILE, LicenseValidator.getResolvedLicenseFilePath(false));
			env.put(TTCN3_DIR, CompilerVersionInformationCollector.getResolvedInstallationPath(false));
			pb.redirectErrorStream(true);
			Process proc = null;
			BufferedReader stdout;
			
			MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream(); 
       
			String actualCommand;
			for (int i = 0; i < commands.size(); i++) {
				actualCommand = commands.get(i);
				setName(descriptions.get(i));
				internalMonitor.subTask("on " + hostnames.get(i));

				List<String> finalCommand = new ArrayList<String>();
				finalCommand.add(ExternalTitanAction.SHELL);
				finalCommand.add("-c");
				finalCommand.add(actualCommand);

				StringBuilder builder = new StringBuilder();
				for (String c : finalCommand) {
					builder.append(c + ' ');
				}
				TITANConsole.println(builder.toString(),stream); 

				pb.command(finalCommand);
				try {
					proc = pb.start();
				} catch (IOException e) {
					TITANConsole.println(ExternalTitanAction.EXECUTION_FAILED,stream);
					ErrorReporter.logExceptionStackTrace(e);
					TITANJob.reportExecutionProblem(project, Platform.getPreferencesService(), getName(), finalCommand, null, false);
					internalMonitor.done();
					MarkerHandler.removeAllMarkedMarkers(project);
					return Status.CANCEL_STATUS;
				}

				stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

				try {
					String line = stdout.readLine();
					while (line != null) {
						if (internalMonitor.isCanceled()) {
							internalMonitor.done();
							MarkerHandler.removeAllMarkedMarkers(project);
							return Status.CANCEL_STATUS;
						}
						TITANConsole.println(hostnames.get(i) + ": " + line,stream);
						line = stdout.readLine();
					}
					int exitval = proc.waitFor();
					if (exitval == 0) {
						TITANConsole.println("The process on " + hostnames.get(i) + " finished without indicating an error.",stream);
					} else {
						TITANConsole.println(hostnames.get(i) + " finished with return value: " + exitval,stream);

						if (stderr.ready()) {
							StringBuilder builder2 = new StringBuilder();
							line = stdout.readLine();
							while (line != null) {
								builder2.append(line);
								line = stdout.readLine();
							}
							TITANJob.reportExecutionProblem(project, Platform.getPreferencesService(), getName(), finalCommand, builder2.toString(), false);
						} else {
							TITANJob.reportExecutionProblem(project, Platform.getPreferencesService(), getName(), finalCommand, null, false);
						}
						internalMonitor.done();
						MarkerHandler.removeAllMarkedMarkers(project);
						return Status.CANCEL_STATUS;
					}
				} catch (IOException e) {
					TITANConsole.println(ExternalTitanAction.EXECUTION_FAILED,stream);
					ErrorReporter.logExceptionStackTrace(e);
				} catch (InterruptedException e) {
					TITANConsole.println(ExternalTitanAction.INTERRUPTION,stream);
					ErrorReporter.logExceptionStackTrace(e);
				} finally {
					try {
						stdout.close();
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
					try {
						stderr.close();
					} catch (IOException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
				internalMonitor.worked(1);
			}
			internalMonitor.done();

			MarkerHandler.removeAllMarkedMarkers(project);
			return Status.OK_STATUS;
		}
	}

	private ISelection selection;

	/**
	 * This method tries to remotely build every selected project. The commands are grouped in sequential executing groups, and executed by the
	 * remoteBuilderJob.
	 * <ul>
	 * <li>if the commands must be executed sequentially, they are given to 1 remoteBuilderJob to execute.
	 * <li>if the commands must be executed paralelly, each of the is given to a separate remoteBuildJob to execute.
	 * </ul>
	 * This action only clears the markers from the involved projects, as it is not able to tell which files are involved in the remote build.
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * @see RemoteBuilderJob
	 *
	 * @param action the action proxy that would handle the presentation portion of the action.
	 *   Not used.
	 */
	@Override
	public void run(final IAction action) {
		doRemoteBuilde();
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		doRemoteBuilde();

		return null;
	}

	private void doRemoteBuilde() {
		if (!LicenseValidator.check()) {
			return;
		}

		/**
		 * This is needed because AbstractHandler does not deal with
		 * selection, and selectionChanged is not called.
		 */
		IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		selection = iwPage.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			List<String> hostNames;
			List<String> commands;
			List<String> descriptions;

			for (Object selected : structSelection.toList()) {
				if (selected instanceof IProject && TITANBuilder.isBuilderEnabled((IProject) selected)) {
					IProject tempProject = (IProject) selected;
					BuildLocation[] locations = ProjectRemoteBuildPropertyData.getBuildLocations(tempProject);

					String parallelExecutionRequested = "false";
					try {
						parallelExecutionRequested = tempProject.getPersistentProperty(new QualifiedName(ProjectRemoteBuildPropertyData.QUALIFIER,
								ProjectRemoteBuildPropertyData.PARALLEL_COMMAND_EXECUTION));
					} catch (CoreException ce) {
						ErrorReporter.logExceptionStackTrace(ce);
						parallelExecutionRequested = "false";
					}

					hostNames = new ArrayList<String>();
					commands = new ArrayList<String>();
					descriptions = new ArrayList<String>();

					for (BuildLocation location : locations) {
						if (location.getActive()) {
							hostNames.add(location.getName());
							commands.add(location.getCommand());
							descriptions.add("Building remotely on host: " + location.getName());
						}
					}

					MarkerHandler.markAllMarkersForRemoval(tempProject);

					if ("true".equals(parallelExecutionRequested)) {
						for (int i = 0; i < commands.size(); i++) {
							RemoteBuilderJob job = new RemoteBuilderJob("Build remotely", tempProject, hostNames.get(i), commands.get(i),
									descriptions.get(i));
							job.setPriority(Job.LONG);
							job.setUser(true);
							job.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
							job.schedule();
						}
					} else {
						RemoteBuilderJob job = new RemoteBuilderJob("Build remotely", tempProject, hostNames, commands, descriptions);
						job.setPriority(Job.LONG);
						job.setUser(true);
						job.setRule(tempProject);
						job.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
						job.schedule();
					}
				}
			}
		}
	}
}
