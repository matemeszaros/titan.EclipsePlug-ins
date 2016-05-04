/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.actions.MergeLog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.HostController;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.designerconnection.DynamicLinkingHelper;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.executors.ExecuteDialog.ExecutableType;
import org.eclipse.titan.executor.graphics.ImageCache;
import org.eclipse.titan.executor.preferences.PreferenceConstants;
import org.eclipse.titan.executor.tabpages.maincontroller.BaseMainControllerTab;
import org.eclipse.titan.executor.tabpages.maincontroller.BaseMainControllerTab.ExecutableCalculationHelper;
import org.eclipse.titan.executor.tabpages.testset.TestSetTab;
import org.eclipse.titan.executor.views.executormonitor.ExecutorStorage;
import org.eclipse.titan.executor.views.executormonitor.LaunchStorage;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.titan.executor.views.testexecution.ExecutedTestcase;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.IProgressConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;
import static org.eclipse.titan.executor.GeneralConstants.CONFIGFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.CONSOLELOGGING;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTABLEFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.EXECUTECONFIGFILEONLAUNCH;
import static org.eclipse.titan.executor.GeneralConstants.HOSTCOMMANDS;
import static org.eclipse.titan.executor.GeneralConstants.HOSTEXECUTABLES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTNAMES;
import static org.eclipse.titan.executor.GeneralConstants.HOSTWORKINGDIRECTORIES;
import static org.eclipse.titan.executor.GeneralConstants.KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES;
import static org.eclipse.titan.executor.GeneralConstants.MAXIMUMNOTIFICATIONLINECOUNT;
import static org.eclipse.titan.executor.GeneralConstants.PROJECTNAME;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTEXECUTABLE;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTNAME;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEHOSTWORKIGNDIRECTORY;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEMCHOST;
import static org.eclipse.titan.executor.GeneralConstants.REPLACEABLEMCPORT;
import static org.eclipse.titan.executor.GeneralConstants.SEVERITYLEVELEXTRACTION;
import static org.eclipse.titan.executor.GeneralConstants.TESTCASEREFRESHONSTART;
import static org.eclipse.titan.executor.GeneralConstants.VERDICTEXTRACTION;
import static org.eclipse.titan.executor.GeneralConstants.WORKINGDIRECTORYPATH;
import static org.eclipse.titan.executor.properties.FieldEditorPropertyPage.getOverlayedPreferenceValue;

/**
 * This is a base class to build the Executors on.
 * <p>
 * It tries to hide all of the launchConfiguration handling and HC starting issues from the specific Executors
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class BaseExecutor {
	
	//TODO: implement
	protected static final boolean CREATE_TEMP_CFG = false;
	
	public static final String PADDEDDATETIMEFORMAT = "%1$tF %1$tH:%1$tM:%1$tS.%1$tL000";
	public static final String DATETIMEFORMAT = "%1$tF %1$tH:%1$tM:%1$tS.%2$06d";

	public static final String MAIN_CONTROLLER = "Main Controller";
	protected static final String CONFIGFILEPATH_NULL = "Could not launch beacuse the configuration file's path is null";
	protected static final String ENVVARS_NULL = "Could not launch beacuse the environmental variables are not available";
	protected static final String NO_HOSTCONTROLLER_SPECIFIED = "No Host Controller was specified on this launch configuration's Host Controllers page";

	/** Error dialog title for the case, when the execution control list is empty */
	private static final String EMPTY_EXECUTION_FAILED_TITLE = "Execution failed";

	/** Error dialog text for the case, when the execution control list is empty */
	private static final String EMPTY_EXECUTION_FAILED_TEXT = "The configuration file selected does not have anything to execute";

	protected Map<String, String> environmentalVariables;
	private List<HostController> hostControllers;
	protected boolean appendEnvironmentalVariables = false;
	protected String projectName;
	protected IProject project;
	protected String workingdirectoryPath;
	protected String executablePath;
	protected String configFilePath;
	protected boolean automaticExecuteSectionExecution;
	protected List<String> availableTestcases;
	protected List<String> availableControlParts;
	protected List<String> availableTestSetNames;
	protected List<List<String>> availableTestSetContents;
	protected boolean executionStarted = false;

	protected boolean consoleLogging;
	protected boolean severityLevelExtraction;
	protected int maximumNotificationCount;
	protected boolean verdictExtraction;
	protected boolean keepTemporarilyGeneratedConfigFiles;
	protected boolean logFileNameDefined = false;
	protected String mLogFileName = null;
	protected boolean logFilesMerged = false;

	protected String mcPort;
	protected String mcHost = "NULL";
	protected String label = "Base Executor";
	protected MainControllerElement mainControllerRoot;
	private ILaunch launchStarted;
	protected ArrayList<ExecutedTestcase> executedTests = new ArrayList<ExecutedTestcase>();
	private LinkedList<Notification> notifications = new LinkedList<Notification>();

	/** handling the execute dialog's remembering feature. */
	protected String lastTimeSelection;
	protected int lastTimeSelectionTime = 1;
	protected ExecutableType lastTimeSelectionType = ExecutableType.NONE;

	protected File temporalConfigFile;

	private List<HostJob> innerHostControllers = new CopyOnWriteArrayList<HostJob>();

	/**
	 * Runnable for the case, when the execution control list is empty.
	 * In this case a dialog pops-up with an error message
	 * @author Arpad Lovassy
	 */
	protected class EmptyExecutionRunnable implements Runnable {
		
		public EmptyExecutionRunnable() {}

		@Override
		public void run() {
			MessageDialog.openError( new Shell( Display.getDefault() ), EMPTY_EXECUTION_FAILED_TITLE, EMPTY_EXECUTION_FAILED_TEXT );
		}
	}
	
	/**
	 * Initializes the Executor with data extracted from the provided launch configuration.
	 *
	 * @param configuration the configuration initializing the executor
	 *
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li>An exception occurs while retrieving the attribute from
	 *  underlying storage.</li>
	 * <li>An attribute with the given name exists, but does not
	 *  have a String value</li>
	 * </ul>
	 * */
	public BaseExecutor(final ILaunchConfiguration configuration) throws CoreException {
		projectName = configuration.getAttribute(PROJECTNAME, "");
		project = DynamicLinkingHelper.getProject(projectName);
		String projectLocation;
		if (null == project) {
			projectLocation = "";
		} else {
			projectLocation = project.getLocation().toOSString();
		}

		workingdirectoryPath = resolvePathFromAttribute(configuration, projectLocation, WORKINGDIRECTORYPATH);
		executablePath = resolvePathFromAttribute(configuration, projectLocation, EXECUTABLEFILEPATH);
		configFilePath = resolvePathFromAttribute(configuration, projectLocation, CONFIGFILEPATH);

		automaticExecuteSectionExecution = configuration.getAttribute(EXECUTECONFIGFILEONLAUNCH, false);
		environmentalVariables = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (HashMap<String, String>) null);
		appendEnvironmentalVariables = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);

		availableControlParts = configuration.getAttribute(TestSetTab.AVAILABLECONTROLPARTS_LABEL, (ArrayList<String>) null);
		availableTestcases = configuration.getAttribute(TestSetTab.AVAILABLETESTCASES_LABEL, (ArrayList<String>) null);
		availableTestSetNames = configuration.getAttribute(TestSetTab.TESTSETNAMES_LABEL, (ArrayList<String>) null);
		if (null != availableTestSetNames) {
			availableTestSetContents = new ArrayList<List<String>>();
			for (String testsetName : availableTestSetNames) {
				availableTestSetContents.add(configuration.getAttribute(TestSetTab.TESTCASESOF_PREFIX + testsetName, (ArrayList<String>) null));
			}
		}

		consoleLogging = configuration.getAttribute(CONSOLELOGGING, true);
		boolean testcaseRefreshOnStart = configuration.getAttribute(TESTCASEREFRESHONSTART, true);
		severityLevelExtraction = configuration.getAttribute(SEVERITYLEVELEXTRACTION, true);
		maximumNotificationCount = configuration.getAttribute(MAXIMUMNOTIFICATIONLINECOUNT, 1000);
		verdictExtraction = configuration.getAttribute(VERDICTEXTRACTION, true);
		keepTemporarilyGeneratedConfigFiles = configuration.getAttribute(KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES, true);

		String nullString = null;
		lastTimeSelection = configuration.getAttribute("lastTimeSelection", nullString);
		lastTimeSelectionTime = configuration.getAttribute("lastTimeSelectionTime", 1);
		int tempLastSelectionType = configuration.getAttribute("lastTimeSelectionType", 0);
		lastTimeSelectionType = ExecutableType.getExecutableType(tempLastSelectionType);
		
		List<String> hostNames = configuration.getAttribute(HOSTNAMES, (ArrayList<String>) null);
		List<String> hostWorkingDirectories = configuration.getAttribute(HOSTWORKINGDIRECTORIES, (ArrayList<String>) null);
		List<String> hostExecutables = configuration.getAttribute(HOSTEXECUTABLES, (ArrayList<String>) null);
		List<String> hostCommands = configuration.getAttribute(HOSTCOMMANDS, (ArrayList<String>) null);
		if (null != hostNames && null != hostWorkingDirectories && null != hostCommands && null != hostExecutables) {
			final int size = hostNames.size();
			if (size > 0 && size == hostWorkingDirectories.size()
					&& size == hostExecutables.size()
					&& size == hostCommands.size()) {
				hostControllers = new ArrayList<HostController>(size);
				for (int i = 0; i < size; i++) {
					hostControllers.add(new HostController(hostNames.get(i), hostWorkingDirectories.get(i), hostExecutables.get(i), hostCommands.get(i)));
				}
			}
		}

		//correct the testcase list
		if (null != executablePath && executablePath.length() > 0 && testcaseRefreshOnStart) {
			ExecutableCalculationHelper helper = BaseMainControllerTab.checkExecutable(configuration, project, new Path(executablePath));
			if (helper.executableFileIsValid && helper.executableIsExecutable) {
				helper.availableTestcases.toArray(new String[helper.availableTestcases.size()]);
			}
			if (null == availableTestcases) {
				availableTestcases = helper.availableTestcases;
			} else {
				for (String testcase : helper.availableTestcases) {
					if (testcase.endsWith(".control")) {
						if (!availableControlParts.contains(testcase)) {
							availableControlParts.add(testcase);
						}
					} else {
						if (!availableTestcases.contains(testcase)) {
							availableTestcases.add(testcase);
						}
					}
					
				}
			}
		}

		label = configuration.getName();
		try {
			label += " [ " + configuration.getType().getName() + " ]";
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	private String resolvePathFromAttribute(ILaunchConfiguration configuration, String projectLocation, String attribute) throws CoreException {
		String path = configuration.getAttribute(attribute, (String) null);
		if (!isNullOrEmpty(path)) {
			path = TITANPathUtilities.resolvePath(path, projectLocation).toOSString();
		}
		return path;
	}

	/**
	 * Frees internal resources of the Executor, destroying the started Host Controllers too.
	 * */
	public void dispose() {
		disposeHostControllers();
	}

	/**
	 * Disposes all the host controllers handled by this executor.
	 * */
	protected final void disposeHostControllers() {
		for (HostJob job : innerHostControllers) {
			job.dispose();
		}
		innerHostControllers.clear();
	}

	/**
	 * Saves the execution information selected last time by the user.
	 * This way the next execution might be able to start earlier.
	 * */
	protected final void saveLastTimeUsageInfo() {
		if (null == launchStarted) {
			return;
		}

		ILaunchConfiguration configuration = launchStarted.getLaunchConfiguration();
		if (null == configuration) {
			return;
		}

		try {
			ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
			copy.setAttribute("lastTimeSelection", lastTimeSelection);
			copy.setAttribute("lastTimeSelectionTime", lastTimeSelectionTime);
			copy.setAttribute("lastTimeSelectionType", lastTimeSelectionType.getValue());
			copy.doSave();
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Adds a list of notification messages to the internally stored ones.
	 *
	 * @param notificationList the list of notification to add
	 * */
	protected final void addNotifications(final List<Notification> notificationList) {
		if (0 != maximumNotificationCount && notifications.size() + notificationList.size() > maximumNotificationCount * 1.2f) {
			if (notificationList.size() > maximumNotificationCount) {
				notifications.clear();
			} else {
				while (notifications.size() + notificationList.size() > maximumNotificationCount - 1) {
					notifications.removeFirst();
				}
			}
		}
		notifications.addAll(notificationList);
	}

	/**
	 * Adds a single notification to the internally stored ones.
	 *
	 * @param notification the notification to add
	 * */
	protected final void addNotification(final Notification notification) {
		notifications.add(notification);
		if (maximumNotificationCount != 0 && notifications.size() > maximumNotificationCount * 1.2f) {
			while (notifications.size() > maximumNotificationCount - 1) {
				notifications.removeFirst();
			}
		}
	}

	/**
	 * This is called as the last command of the launch of an Executor. It tries to connect a main controller root to the launch that started, and the
	 * executor itself.
	 * <p>
	 * This is not a Main Controller start session.
	 *
	 * @param launch the ILaunch instance to start the session with.
	 * */
	public void startSession(final ILaunch launch) {
		launchStarted = launch;

		deleteLogFiles();

		if (LaunchStorage.getLaunchElementMap().containsKey(launch)) {
			mainControllerRoot = new MainControllerElement(MAIN_CONTROLLER, this);
			LaunchStorage.getLaunchElementMap().get(launch).addChildToEnd(mainControllerRoot);
		}

		ExecutorStorage.getExecutorMap().put(launch, this);

		if (Activator.getMainView() != null) {
			Activator.getMainView().expandToLevel(mainControllerRoot, 0);
			Activator.getMainView().refreshAll();
		}

		saveLastTimeUsageInfo();
	}

	/**
	 * @return the root of this Executor
	 * */
	public final MainControllerElement mainControllerRoot() {
		return mainControllerRoot;
	}

	/**
	 * Set Main Controller root of this executor.
	 *
	 * @param element the Main Controller to be set as the root of this Executor.
	 * */
	public final void mainControllerRoot(final MainControllerElement element) {
		mainControllerRoot = element;
	}

	/**
	 * @return the launch of this Executor
	 * */
	public final ILaunch getLaunchStarted() {
		return launchStarted;
	}

	/**
	 * @return the list of executed tests
	 * */
	public final List<ExecutedTestcase> executedTests() {
		return executedTests;
	}

	/**
	 * @return the list of stored notifications
	 * */
	public final LinkedList<Notification> notifications() {
		return notifications;
	}

	/**
	 * Tells whether this Executor is terminated or not.
	 * <p>
	 * The default implementation should always return true.
	 *
	 * @return true the executor is terminated, false otherwise
	 * */
	public abstract boolean isTerminated();

	/**
	 * @return the process of the Main Controller, or null if none.
	 * */
	public abstract IProcess getProcess();

	/**
	 * Terminates the Executor, or if it has terminated in the operating system than adapts the inner representation to this fact.
	 *
	 * @param external true if the termination happened outside Eclipse and false if it should be done inside Eclipse
	 * */
	public abstract void terminate(final boolean external);

	/**
	 * Shuts down the session.
	 * */
	protected void shutdownSession() {
		if (!executionStarted) {
			return;
		}

		mergeLogFiles();

		if (project == null) {
			return;
		}

		WorkspaceJob op = new WorkspaceJob("Refreshing the project `" + project.getName() + "' to discover log files") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);
		op.setSystem(true);
		op.setUser(false);
		op.setRule(project);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
	}

	public final void startHostControllers() {
		if (hostControllers == null || hostControllers.isEmpty()) {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "",
				NO_HOSTCONTROLLER_SPECIFIED));
			return;
		}
		ProcessBuilder pb = new ProcessBuilder();
		Map<String, String> env = pb.environment();
		if (!appendEnvironmentalVariables) {
			env.clear();
		}

		if (environmentalVariables != null) {
			try {
				EnvironmentHelper.resolveVariables(env, environmentalVariables);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		EnvironmentHelper.setTitanPath(env);
		EnvironmentHelper.set_LICENSE_FILE_PATH(env);

		IProject actualProject = DynamicLinkingHelper.getProject(projectName);
		if (actualProject != null) {
			EnvironmentHelper.set_LD_LIBRARY_PATH(actualProject, env);
		}

		Process proc;

		HostController controller;
		List<String> shellCommand;
		MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream();
		String command;

		for (int i = 0; i < hostControllers.size(); i++) {
			StringBuilder hostControllerLabel = new StringBuilder("Host Controller instance " + (i + 1));

			controller = hostControllers.get(i);
			command = controller.command();
			command = command.replace(REPLACEABLEHOSTNAME, controller.host());

			IPath path;
			boolean oldStyleWorkingDir = true;
			if (actualProject == null) {
				path = new Path(controller.workingdirectory());
			} else {
				path = new Path(controller.workingdirectory());
				if (!path.isAbsolute()) {
					oldStyleWorkingDir = false;
					path = TITANPathUtilities.resolvePath(controller.workingdirectory(), actualProject.getLocation().toOSString());
				}
			}
			String workingDirResult = PathConverter.convert(oldStyleWorkingDir ? controller.workingdirectory() : path.toOSString(), true, TITANConsole.getConsole());
			command = command.replace(REPLACEABLEHOSTWORKIGNDIRECTORY, workingDirResult);

			boolean oldStyleExecutable = true;
			if (actualProject == null) {
				path = new Path(controller.executable());
			} else {
				path = new Path(controller.executable());
				if (!path.isAbsolute()) {
					oldStyleExecutable = false;
					path = TITANPathUtilities.resolvePath(controller.executable(), actualProject.getLocation().toOSString());
				}
			}
			String executableResult = PathConverter.convert(oldStyleExecutable ? controller.executable() : path.toOSString(), true, TITANConsole.getConsole());
			String result = PathUtil.getRelativePath(workingDirResult, executableResult);
			if (!result.equals(executableResult)) {
				result = "./" + result;
			}
			command = command.replace(REPLACEABLEHOSTEXECUTABLE, result);

			if ("NULL".equals(mcHost)) {
				command = command.replace(REPLACEABLEMCHOST, "0.0.0.0");
			} else {
				command = command.replace(REPLACEABLEMCHOST, mcHost);
			}
			command = command.replace(REPLACEABLEMCPORT, mcPort);
			shellCommand = new ArrayList<String>();
			shellCommand.add("sh");
			shellCommand.add("-c");
			shellCommand.add(command);

			for (String c : shellCommand) {
				stream.print(c + ' ');
			}
			stream.println();

			pb.command(shellCommand);
			if (workingdirectoryPath != null) {
				pb.directory(new File(workingdirectoryPath));
			}
			try {
				proc = pb.start();
				HostJob job = new HostJob(hostControllerLabel.toString(), proc, this);
				innerHostControllers.add(job);
				job.setPriority(Job.DECORATE);
				job.setUser(true);
				job.schedule();
			} catch (IOException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
	}

	/**
	 * Appends executor dependent actions to the menu that is presented on right click.
	 *
	 * @param manager the manager to be used
	 *
	 * @return the input manager with the added actions.
	 * */
	public MenuManager createMenu(final MenuManager manager) {
		return manager;
	}
	
	protected abstract String getDefaultLogFileName();
	
	/**
	 * @return the relative directory path of the default log file from the preferences
	 */
	private String getDefaultLogFileDir() {
		//TODO
		/*
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String logFolder = getOverlayedPreferenceValue(preferenceStore, project,
				PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID, PreferenceConstants.SET_LOG_FOLDER);
		if (!logFileNameDefined && Boolean.parseBoolean(logFolder)) {
			return getOverlayedPreferenceValue(preferenceStore, project,
					PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID, PreferenceConstants.LOG_FOLDER_PATH_NAME);
		}
		return null;
		*/
		// log files are created in the bin (actual) directory
		// until temporary cfg file creation is fixed:
		//  - ../log/MyExample-%n.log file is set as LogFile if it's not provided by the input cfg file
		return ".";
	}
	
	/**
	 * @return the relative path of the log dir (from the cfg file, or the default from the preferences)
	 */
	private String getLogDir() {
		if ( this.logFileNameDefined && mLogFileName != null ) {
			File file = new File(mLogFileName);
			String parent = file.getParent();
			return parent != null ? parent : "";
		}
		
		return this.getDefaultLogFileDir();
	}
	
	/**
	 * Creates the content of the configuration file
	 * @return the generated cfg string
	 */
	protected String generateCfgString() {
		StringBuilder builder = new StringBuilder();
		String workingDirRelative = getDefaultLogFileDir();
		if (workingDirRelative != null && workingDirRelative.length() != 0) {
			builder.append("\n//This part was added by the TITAN Executor.\n");
			builder.append("[LOGGING]\n");
			builder.append("LogFile := ");
			builder.append("\"" + "." + IPath.SEPARATOR + workingDirRelative + IPath.SEPARATOR);
			builder.append(getDefaultLogFileName());
			builder.append("\"\n\n");
		}
		return builder.toString();
	}
	
	/**
	 * Reads the configuration file producing a configuration file handler.
	 *
	 * @return the configuration file handler
	 * */
	protected ConfigFileHandler readConfigFile() {
		if (isNullOrEmpty(configFilePath)) {
			return null;
		}
		
		final ConfigFileHandler configHandler = new ConfigFileHandler();
		configHandler.readFromFile(configFilePath);
		Map<String, String> env = appendEnvironmentalVariables ? new HashMap<String, String>( System.getenv() ) : new HashMap<String, String>();

		if (environmentalVariables != null) {
			try {
				EnvironmentHelper.resolveVariables(env, environmentalVariables);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}
		
		configHandler.setEnvMap(env);
		configHandler.processASTs();
		logFileNameDefined = configHandler.isLogFileNameDefined();
		mLogFileName = configHandler.getLogFileName();
		return configHandler;
	}

	/**
	 *  Deletes every log files in the log directory if the default log folder has been set.
	 */
	protected void deleteLogFiles() {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if ( !isDeleteLogFilesSet( preferenceStore ) ) {
			return;
		}
		
		String workingDirRelative = getLogDir();
		if ( workingDirRelative == null ) {
			return;
		}
		String logFileFolder = workingdirectoryPath + File.separator + workingDirRelative + File.separator;
		Path path = new Path(logFileFolder);
		
		IContainer folder = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
		if (folder == null || !folder.exists()) {
			return;
		}
	
		final StringBuilder filesThatCanNotBeDeleted = new StringBuilder();
		try {
			for (IResource resource : folder.members()) {
				try {
					if (resource instanceof IFile && "log".equals(resource.getFileExtension())) {
						resource.delete(true, new NullProgressMonitor());
					}
				} catch (CoreException e) {
					filesThatCanNotBeDeleted.append(resource.getName());
					filesThatCanNotBeDeleted.append("\n");
				}
			}
			folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Error while deleting log files", "The log folder is not accessible.");
		}
		
		if (filesThatCanNotBeDeleted.length() > 0) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(
				"Error while deleting log files", 
				"The following log files can not be deleted:\n" + filesThatCanNotBeDeleted.toString());
		}
	}

	private boolean isDeleteLogFilesSet(IPreferenceStore preferenceStore) {
		return Boolean.parseBoolean(getOverlayedPreferenceValue(
				preferenceStore, project, PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID, PreferenceConstants.DELETE_LOG_FILES_NAME));
	}

	private boolean isLogFolderSet(IPreferenceStore preferenceStore) {
		return Boolean.parseBoolean(getOverlayedPreferenceValue(
				preferenceStore,
				project,
				PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID,
				PreferenceConstants.SET_LOG_FOLDER));
	}

	/**
	 * Merges the generated log files together.
	 */
	protected void mergeLogFiles() {
 		if (logFilesMerged) {
			return;
		} else {
			logFilesMerged = true;
		}
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if ( !isAutomaticMergeEnabled( preferenceStore ) ) {
			return;
		}
		
		String workingDirRelative =  getLogDir();
		if ( workingDirRelative == null ) {
			return;
		}
		String logFileFolder = workingdirectoryPath + File.separator + workingDirRelative + File.separator;
		Path path = new Path(logFileFolder);
		
		
		IContainer folder = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
		if (folder == null || !folder.exists()) {
			return;
		}
		
		List<IFile> filesToMerge = new ArrayList<IFile>();
		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			for (IResource resource : folder.members()) {
				if (resource instanceof IFile && "log".equals(resource.getFileExtension())) {
					try {
						if (!Boolean.valueOf(resource.getPersistentProperty(MergeLog.MERGED_FILE_PROPERTY))) {
							filesToMerge.add((IFile) resource);
						}
					} catch (final CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}
		} catch (CoreException e) {
			ErrorReporter.parallelErrorDisplayInMessageDialog( 
				"Error while merging log files", 
				"The log folder "+logFileFolder+ " is not accessible.");
		}

		MergeLog mergeLog = new MergeLog();
		mergeLog.setShowDialog(false);
		mergeLog.run(filesToMerge, false);
	}

	private boolean isAutomaticMergeEnabled(IPreferenceStore preferenceStore) {
		return Boolean.parseBoolean(getOverlayedPreferenceValue(
				preferenceStore, project, PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID, PreferenceConstants.AUTOMATIC_MERGE_NAME));
	}

	/**
	 * Generates a temporal configuration file based on the cfgString added as parameter.
	 * */
	protected void generateTemporalCfgFile(final String cfgString) {
		if (isNullOrEmpty(cfgString)) {
			return;
		}
		
		if (!keepTemporarilyGeneratedConfigFiles && null != temporalConfigFile && temporalConfigFile.exists()) {
			boolean result = temporalConfigFile.delete();
			if (!result) {
				ErrorReporter.logError("The temporal configuration file " + temporalConfigFile.getName() + " could not be deleted");
				return;
			}
		}

		BufferedWriter writer = null;
		try {
			temporalConfigFile = File.createTempFile("temporal_", "_XXXXX.cfg", new File(workingdirectoryPath));
			if (!keepTemporarilyGeneratedConfigFiles) {
				temporalConfigFile.deleteOnExit();
			}
			writer = new BufferedWriter(new java.io.FileWriter(temporalConfigFile));
			writer.write(cfgString);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			temporalConfigFile = null;
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}
