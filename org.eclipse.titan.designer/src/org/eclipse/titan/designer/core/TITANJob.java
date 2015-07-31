/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.Cygwin;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.actions.ExternalTitanAction;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This class takes care of every job that involves calling parts of the TITAN
 * build environment.
 * 
 * @author Kristof Szabados
 */
public class TITANJob extends WorkspaceJob {
	protected static final String ERROR = "error";
	protected static final String FAILED = " ...failed";
	protected static final String CHECK_PERMISSION = "When trying to execute the `{0}'' shell command an error occured.\n"
			+ "Possible reasons are:\n" + "- the command does not exist\n" + "- you have no right to execute the command\n"
			+ "- the error occured during executing the command\n" + "and many more.\n"
			+ "Please check the TITAN console to identify the problem.";
	protected static final String ERRORS_FOUND = "The execution of the `{0}'' command reported the following errors:\n{1}";
	protected static final String TTCN3_LICENSE_FILE_KEY = "TTCN3_LICENSE_FILE";
	protected static final String TTCN3_DIR_KEY = "TTCN3_DIR";
	protected static final String LD_LIBRARY_PATH_KEY = "LD_LIBRARY_PATH";
	protected static final String LIBRARY_SUB_DIR = "/lib";
	protected static final String SUCCESS = "Operation finished successfully.";
	protected static final String FAILURE = "Operation failed with return value: ";
	protected static final String SPACE = " ";
	protected static final String EMPTY_STRING = "";
	protected static final String CYGWIN = "No cygwin installation found.\nPlease make sure that cygwin is installed properly.";

	private Map<String, IFile> files;
	private File workingDir;
	private IProject project;
	private boolean removeCompilerMarkers = false;
	private boolean removeOnTheFlyMarkers = false;

	private List<List<String>> commands;
	private List<String> descriptions;

	private boolean foundErrors = false;
	/* It is already reported that cygwin is not installed: */
	private static boolean reportedNoCygwin = false;

	public TITANJob(final String name, final Map<String, IFile> files, final File workingDir, final IProject project) {
		super(name);
		this.files = new HashMap<String, IFile>();
		this.files.putAll(files);
		this.workingDir = workingDir;
		this.project = project;
		this.commands = new ArrayList<List<String>>();
		this.descriptions = new ArrayList<String>();

		setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
	}

	/**
	 * Call this function to make the job delete all compiler markers, that
	 * were present before it was executed, after its execution.
	 * */
	public final void removeCompilerMarkers() {
		removeCompilerMarkers = true;
	}

	/**
	 * Call this function to make the job delete all on-the-fly markers,
	 * that were present before it was executed, after its execution.
	 * */
	public final void removeOnTheFlyMarkers() {
		removeOnTheFlyMarkers = true;
	}

	public final void addCommand(final List<String> command, final String description) {
		commands.add(command);
		descriptions.add(description);
	}

	/**
	 * Sets the required environmental variables for the ProcessBuilder
	 * passed in as argument. This must be separated from the general
	 * behavior, because different operating systems might require different
	 * environmental variables.
	 * <p>
	 * This version is for cygwin and so resolves the win32 paths to their
	 * cygwin counterpart.
	 * 
	 * @param pb
	 *                the ProcessBuilder whose environmental variables need
	 *                to be set.
	 * 
	 * @see #runInWorkspace(IProgressMonitor)
	 * */
	protected void setEnvironmentalVariables(final ProcessBuilder pb) {
		Map<String, String> env = pb.environment();

		String ttcn3Dir = CompilerVersionInformationCollector.getResolvedInstallationPath(false);
		env.put(TTCN3_LICENSE_FILE_KEY, LicenseValidator.getResolvedLicenseFilePath(false));
		env.put(TTCN3_DIR_KEY, ttcn3Dir);
		String temp = env.get(LD_LIBRARY_PATH_KEY);
		if (temp == null) {
			env.put(LD_LIBRARY_PATH_KEY, ttcn3Dir + LIBRARY_SUB_DIR);
		} else {
			env.put(LD_LIBRARY_PATH_KEY, temp + ":" + ttcn3Dir + LIBRARY_SUB_DIR);
		}
	}

	/**
	 * Creates the final command from the one actually selected for
	 * execution from the list of commands. This must be separated from the
	 * general behaviour, because in different operating systems processes
	 * must be started differently
	 * <p>
	 * This version is for cygwin and so 'prefixes' the actual command with
	 * sh -c.
	 * 
	 * @param actualCommand
	 *                the command selected for execution.
	 * @return the final command that can be passed to the operating system.
	 * 
	 * @see #runInWorkspace(IProgressMonitor)
	 * */
	protected List<String> getFinalCommand(final List<String> actualCommand) {
		StringBuilder tempCommand = new StringBuilder();
		for (String c : actualCommand) {
			tempCommand.append(c).append(SPACE);
		}

		List<String> finalCommand = new ArrayList<String>();
		finalCommand.add(ExternalTitanAction.SHELL);
		finalCommand.add("-c");
		finalCommand.add(tempCommand.toString());

		return finalCommand;
	}

	/** @return true if problems were found during the build, false otherwise */
	public final boolean foundErrors() {
		return foundErrors;
	}

	/**
	 * Runs the list of commands that it has to process.
	 * <p>
	 * <ul>
	 * <li>switch to the working directory.
	 * <li>in cycle it executes the actual command, and parses the output
	 * for errors.
	 * </ul>
	 * 
	 * @see OutputAnalyzer
	 * 
	 * @param monitor
	 *                the progress monitor to report progress on.
	 * 
	 * @return the status of the operation when it finishes.
	 */
	@Override
	public final IStatus runInWorkspace(final IProgressMonitor monitor) {
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;

		if (commands == null || descriptions == null || commands.size() != descriptions.size()) {
			return Status.CANCEL_STATUS;
		}

		IPreferencesService prefs = Platform.getPreferencesService();

		// If we are on win32 and we do not have cygwin -> cancel

		if ( Cygwin.isMissingInOSWin32()) {
			if(!reportedNoCygwin) {
				ErrorReporter.logError(CYGWIN);
				List<String> al = new ArrayList<String>();
				al.add(CYGWIN);
				reportExecutionProblem(project, prefs, getName(), al, null, true);
				reportedNoCygwin = true; //do not report it next time!
			}
			return Status.CANCEL_STATUS;
		}
		

		internalMonitor.beginTask(getName(), commands.size());
		Activator.getDefault().pauseHandlingResourceChanges();

		ProcessBuilder pb = new ProcessBuilder();
		setEnvironmentalVariables(pb);
		pb.directory(workingDir);
		pb.redirectErrorStream(true);
		Process proc = null;

		MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream(); //ethbaat
		
		String line;
		BufferedReader stdout;

		if (removeCompilerMarkers || removeOnTheFlyMarkers) {
			if (removeCompilerMarkers) {
				MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_ERRORMARKER, project);
				MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_WARNINGMARKER, project);
				MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_INFOMARKER, project);
			}
			if (removeOnTheFlyMarkers) {
				MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, project);
				MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, project);
				MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, project);
			}

			for (IFile file : files.values()) {
				if (removeCompilerMarkers) {
					MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_ERRORMARKER, file);
					MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_WARNINGMARKER, file);
					MarkerHandler.markMarkersForRemoval(GeneralConstants.COMPILER_INFOMARKER, file);
				}
				if (removeOnTheFlyMarkers) {
					MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, file);
					MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER, file);
					MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_MIXED_MARKER, file);
				}
			}
		}

		OutputAnalyzer analyzer = new OutputAnalyzer(files, project);
		for (int i = 0; i < commands.size(); i++) {
			if (internalMonitor.isCanceled()) {
				internalMonitor.done();
				analyzer.dispose();
				analyzer = null;
				clearBeforeFinish();
				return Status.CANCEL_STATUS;
			}

			setName(descriptions.get(i));
			List<String> finalCommand = getFinalCommand(commands.get(i));
			StringBuilder builder = new StringBuilder();

			for (String c : finalCommand) {
				builder.append(c + SPACE);
			}
			//TITANConsole.getConsole().newMessageStream().println(builder.toString());
			TITANConsole.println(builder.toString(),stream);

			pb.command(finalCommand);
			try {
				proc = pb.start();
			} catch (IOException e) {
				//TITANConsole.getConsole().newMessageStream().println(ExternalTitanAction.EXECUTION_FAILED);
				TITANConsole.println(ExternalTitanAction.EXECUTION_FAILED,stream);
				ErrorReporter.logExceptionStackTrace(e);
				reportExecutionProblem(project, prefs, getName(), finalCommand, null, false);
				foundErrors = true;
				internalMonitor.done();
				analyzer.dispose();
				analyzer = null;
				if (proc != null) {
					proc.destroy();
				}
				clearBeforeFinish();
				return Status.CANCEL_STATUS;
			}
			stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			try {
				while ((line = stdout.readLine()) != null) {
					if (internalMonitor.isCanceled()) {
						internalMonitor.done();
						analyzer.dispose();
						analyzer = null;
						proc.destroy();
						clearBeforeFinish();
						return Status.CANCEL_STATUS;
					}
					analyzer.parseTitanErrors(line);
					//TITANConsole.getConsole().newMessageStream().println(line);
					TITANConsole.println(line,stream);
				}
				int exitval = proc.waitFor();
				if (exitval != 0) {
					//TITANConsole.getConsole().newMessageStream().println(FAILURE + exitval);
					TITANConsole.println(FAILURE + exitval,stream);
					if (!analyzer.hasProcessedErrorMessages()) {
						if (stderr.ready()) {
							StringBuilder builder2 = new StringBuilder();
							while ((line = stderr.readLine()) != null) {
								builder2.append(line);
							}
							reportExecutionProblem(project, prefs, getName(), finalCommand, builder2.toString(), false);
						} else {
							reportExecutionProblem(project, prefs, getName(), finalCommand, null, false);
						}
					}
					foundErrors = true;
					internalMonitor.done();
					analyzer.dispose();
					analyzer = null;
					proc.destroy();
					clearBeforeFinish();
					return Status.CANCEL_STATUS;
				}

				//TITANConsole.getConsole().newMessageStream().println(SUCCESS);
				TITANConsole.println(SUCCESS,stream);
			} catch (IOException e) {
				//TITANConsole.getConsole().newMessageStream().println(ExternalTitanAction.EXECUTION_FAILED);
				TITANConsole.println(ExternalTitanAction.EXECUTION_FAILED,stream);
				ErrorReporter.logExceptionStackTrace(ExternalTitanAction.EXECUTION_FAILED, e);
			} catch (InterruptedException e) {
				//TITANConsole.getConsole().newMessageStream().println(ExternalTitanAction.INTERRUPTION);
				TITANConsole.println(ExternalTitanAction.INTERRUPTION,stream);
				ErrorReporter.logExceptionStackTrace(ExternalTitanAction.INTERRUPTION, e);
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
		analyzer.dispose();

		clearBeforeFinish();
		return Status.OK_STATUS;
	}

	/**
	 * Does the final clearing before quitting the job. - Activates the
	 * resource change handling. - removes all markers marked for removal. -
	 * refreshes the resources in the project.
	 * */
	private void clearBeforeFinish() {
		Activator.getDefault().resumeHandlingResourceChanges();
		if (removeCompilerMarkers || removeOnTheFlyMarkers) {
			MarkerHandler.removeAllMarkedMarkers(project);
		}

		if (project == null) {
			return;
		}

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Reports a problem either as a pop-up window or as an error marker
	 * placed on the project, depending on user preference.
	 * 
	 * @param project
	 *                the project to report the problem to.
	 * @param prefs
	 *                user preferences.
	 * @param name
	 *                the name of the failed operation.
	 * @param command
	 *                the command executed.
	 * @param errorOutput
	 *                the error output reported by the problem, or null if
	 *                none available.
	 */
	public static void reportExecutionProblem(final IProject project, final IPreferencesService prefs, final String name,
			final List<String> command, final String errorOutput, final boolean cygwin) {
		boolean useMarker = prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORTPROGRAMERRORWITHMARKER, false,
				null);
		if (useMarker && project != null) {
			createProblemMarker(project, name + FAILED);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					StringBuilder builder = new StringBuilder();
					for (String c : command) {
						builder.append(c).append(SPACE);
					}
					if (errorOutput == null || errorOutput.length() == 0) {
						if (cygwin) {
							MessageDialog.openError(new Shell(Display.getDefault()), name + FAILED, builder.toString());
							ErrorReporter.logError(builder.toString());
						} else {
							MessageDialog.openError(new Shell(Display.getDefault()), name + FAILED,
									MessageFormat.format(CHECK_PERMISSION, builder.toString()));
							ErrorReporter.logError(MessageFormat.format(CHECK_PERMISSION, builder.toString()));
						}
					} else {
						if (cygwin) {
							MessageDialog.openError(new Shell(Display.getDefault()), name + FAILED, builder.toString());
							ErrorReporter.logError(builder.toString());
						} else {
							MessageDialog.openError(new Shell(Display.getDefault()), name + FAILED,
									MessageFormat.format(ERRORS_FOUND, builder.toString(), errorOutput));
							ErrorReporter.logError(MessageFormat.format(ERRORS_FOUND, builder.toString(), errorOutput));
						}
					}
				}
			});
		}
	}

	/**
	 * Creates a problem marker on a given resource.
	 * 
	 * @param resource
	 *                The resource to put the marker on.
	 * @param message
	 *                The message of the error.
	 */
	private static void createProblemMarker(final IResource resource, final String message) {
		Location location = new Location(resource);

		location.reportExternalProblem(message, IMarker.SEVERITY_ERROR, GeneralConstants.COMPILER_ERRORMARKER);
	}
}
