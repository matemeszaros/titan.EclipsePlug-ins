/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.single;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.GeneralConstants;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.TITANDebugConsole;
import org.eclipse.titan.executor.designerconnection.DynamicLinkingHelper;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ExecuteDialog;
import org.eclipse.titan.executor.executors.ExecuteDialog.ExecutableType;
import org.eclipse.titan.executor.views.executormonitor.ExecutorStorage;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.LaunchStorage;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.titan.executor.views.testexecution.ExecutedTestcase;
import org.eclipse.titan.executor.views.testexecution.TestExecutionView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Kristof Szabados
 * */
public final class SingleExecutor extends BaseExecutor {
	private static final String EXECUTION_FINISHED = "^Test case (.*) finished\\. Verdict: (.*)$";
	private static final Pattern EXECUTION_FINISHED_PATTERN = Pattern.compile(EXECUTION_FINISHED);
	private final Matcher executionFinished = EXECUTION_FINISHED_PATTERN.matcher("");
	private static final String REASON = "^(.*) reason: (.*)$";
	private static final Pattern REASON_PATTERN = Pattern.compile(REASON);
	private final Matcher reasonMatcher = REASON_PATTERN.matcher("");

	private final Action startExecutionAction;
	private IProcess process;
	private Process proc;
	private boolean isrunning;
	private final StringBuilder builder = new StringBuilder();
	private String fastLine;
	private int fastOffset;

	public SingleExecutor(final ILaunchConfiguration configuration) throws CoreException {
		super(configuration);

		if (null == configFilePath) {
			ErrorReporter.INTERNAL_ERROR(CONFIGFILEPATH_NULL);
		}

		isrunning = true;
		startExecutionAction = new Action("start execution") {
			@Override
			public void run() {
				startExecution(false);
			}
		};
		startExecutionAction.setToolTipText("start execution");
		startExecutionAction.setEnabled(true);
	}

	@Override
	public void startSession(final ILaunch arg2) {
		super.startSession(arg2);

		if (automaticExecuteSectionExecution) {
			if (!LaunchStorage.getLaunchElementMap().containsKey(arg2)) {
				ILaunchConfiguration launchConfiguration = arg2.getLaunchConfiguration();
				LaunchElement launchElement = new LaunchElement(launchConfiguration.getName(), arg2);
				LaunchStorage.registerLaunchElement(launchElement);
				ExecutorStorage.registerExecutorStorage(launchElement);
			}

			startExecution(true);
		}
	}

	/**
	 * Initializes and displays a dialog to the user. If the user selected an executable element, it is also started here.
	 *
	 * @param automaticExecution if this option is set the execution automatically starts executing the configuration file once.
	 *
	 * @see #createProcess(String)
	 * */
	private void startExecution(final boolean automaticExecution) {
		if (!isrunning) {
			return;
		}

		boolean invalidSelection = false;
		StringBuilder builder = new StringBuilder();
		do {
			if (automaticExecution && null != configFilePath && 0 != configFilePath.length() && !invalidSelection) {
				lastTimeSelection = "configuration file";
				lastTimeSelectionTime = 1;
				lastTimeSelectionType = ExecutableType.CONFIGURATIONFILE;
			} else {
				final MyBoolean temp = new MyBoolean(true);

				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						ExecuteDialog dialog = new ExecuteDialog(null);
						dialog.setControlparts(availableControlParts);
						dialog.setTestsets(availableTestSetNames);
						dialog.setTestcases(availableTestcases);
						dialog.setConfigurationFile(configFilePath);
						dialog.setSelection(lastTimeSelection, lastTimeSelectionTime, lastTimeSelectionType);

						if (dialog.open() != Window.OK) {
							temp.setValue(false);
							return;
						}

						lastTimeSelection = dialog.getSelectedElement();
						lastTimeSelectionTime = dialog.getSelectionTimes();
						lastTimeSelectionType = dialog.getSelectionType();
					}
				});

				if (!temp.getValue()) {
					executionStarted = false;
					terminate(true);
					return;
				}
			}

			final ConfigFileHandler configHandler;
			if (null != configFilePath && configFilePath.length() > 0) {
				configHandler = readConfigFile();
			} else {
				configHandler = new ConfigFileHandler();
				logFileNameDefined = false;
			}

			if( configHandler.isErroneous() ) {

				if (configHandler.parseExceptions().isEmpty()) {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"An error was found while processing the configuration file",
							"Please refer to the Error Log view for further information.");
				} else {
					Throwable exception = configHandler.parseExceptions().get(configHandler.parseExceptions().size() - 1);
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"Error while processing the configuration file", 
							exception.getMessage() + "\n Please refer to the Error Log view for further information.");
					ErrorReporter.logError(exception.getMessage());
				}
				return;
			}
			
			List<Integer> disallowedNodes = new ArrayList<Integer>(6);
			disallowedNodes.add(CfgLexer.MAIN_CONTROLLER_SECTION);
			disallowedNodes.add(CfgLexer.DEFINE_SECTION);
			disallowedNodes.add(CfgLexer.INCLUDE_SECTION);
			disallowedNodes.add(CfgLexer.COMPONENTS_SECTION);
			disallowedNodes.add(CfgLexer.GROUPS_SECTION);
			disallowedNodes.add(CfgLexer.EXECUTE_SECTION);

			builder = configHandler.toStringResolved(disallowedNodes);
			builder.append("\n[EXECUTE]\n");

			switch (lastTimeSelectionType) {
			case TESTCASE:
			case CONTROLPART:
				invalidSelection = false;
				for (int i = 0; i < lastTimeSelectionTime; i++) {
					builder.append(lastTimeSelection).append('\n');
				}
				break;
			case TESTSET:
				invalidSelection = false;
				for (int i = 0; i < availableTestSetNames.size(); i++) {
					if (availableTestSetNames.get(i).equals(lastTimeSelection)) {
						for (int k = 0; k < lastTimeSelectionTime; k++) {
							builder.append("// testset: ").append(lastTimeSelection).append('\n');
							for (int j = 0; j < availableTestSetContents.get(i).size(); j++) {
								builder.append(availableTestSetContents.get(i).get(j));
								builder.append('\n');
							}
						}
					}
				}
				break;
			case CONFIGURATIONFILE:
				List<String> configurationFileElements = configHandler.getExecuteElements();
				if (configurationFileElements.isEmpty()) {
					invalidSelection = true;
					Display.getDefault().syncExec( new EmptyExecutionRunnable() );
				} else {
					invalidSelection = false;
					for (int i = 0; i < lastTimeSelectionTime; i++) {
						for (String s : configurationFileElements) {
							builder.append(s).append('\n');
						}
					}
				}
				break;
			default:
				break;
			}
		} while (invalidSelection);

		executionStarted = true;
		startExecutionAction.setEnabled(false);

		File cfgFile;
		if ( CREATE_TEMP_CFG ) {
			builder.append(generateCfgString());
			generateTemporalCfgFile(builder.toString());
			cfgFile = temporalConfigFile;
		} else {
			cfgFile = new File ( configFilePath );
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IFile[] outputFiles = root.findFilesForLocationURI(cfgFile.toURI());
		for (IFile outputFile : outputFiles) {
			try {
				outputFile.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		createProcess(cfgFile.getAbsolutePath());
	}

	/**
	 * Executes the executable parameterized with the configuration file describing the test session.
	 *
	 *@param actualConfigPath the path of the configuration file to call the executable with.
	 *
	 * @see #startExecution(boolean)
	 * */
	private void createProcess(final String actualConfigPath) {
		ProcessBuilder pb = new ProcessBuilder();
		Map<String, String> env = pb.environment();
		if (!appendEnvironmentalVariables) {
			env.clear();
		}

		if (null != environmentalVariables) {
			try {
				EnvironmentHelper.resolveVariables(env, environmentalVariables);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		final File executableFile = new File(executablePath);
		if (!executableFile.exists()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(null, "Execution failed", "The executable `" + executableFile + "' does not exist.");
				}
			});
		}

		EnvironmentHelper.setTitanPath(env);
		EnvironmentHelper.set_LICENSE_FILE_PATH(env);
		EnvironmentHelper.set_LD_LIBRARY_PATH(DynamicLinkingHelper.getProject(projectName), env);

		MessageConsole console = TITANDebugConsole.getConsole();

		List<String> command = new ArrayList<String>();
		command.add("sh");
		command.add("-c");
		command.add(" sleep 1; cd '" + PathConverter.convert(workingdirectoryPath, true, console) + "'; '" + PathConverter.convert(executablePath, true, console)
				+ "' '" + PathConverter.convert(actualConfigPath, true, console) + "'");

		MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream();
		for (String c : command) {
			stream.print(c + ' ');
		}
		stream.println();

		pb.command(command);
		pb.redirectErrorStream(true);
		if (null != workingdirectoryPath) {
			File workingDir = new File(workingdirectoryPath);
			if (!workingDir.exists()) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(null,
								"Execution failed", "The working directory `" + workingdirectoryPath + "' does not exist.");
					}
				});
			}
			pb.directory(workingDir);
		}
		try {
			proc = pb.start();

			if (null != mainControllerRoot) {
				ILaunch launch = ((LaunchElement) mainControllerRoot.parent()).launch();
				process = DebugPlugin.newProcess(launch, proc, MAIN_CONTROLLER);
			}

			IStreamsProxy proxy = process.getStreamsProxy();
			if (null != proxy) {
				IStreamMonitor outputstreammonitor = proxy.getOutputStreamMonitor();
				IStreamListener listener = new IStreamListener() {

					@Override
					public void streamAppended(final String text, final IStreamMonitor monitor) {
						processConsoleOutput(text);
					}

				};
				if (null != outputstreammonitor) {
					String temp = outputstreammonitor.getContents();
					processConsoleOutput(temp);
					outputstreammonitor.addListener(listener);
				}
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			proc = null;
		}
	}

	/**
	 * Processes the output of the Main Controller
	 * <p>
	 * Note that the output is not reported in full lines.
	 *
	 * @param text the newly reported text
	 *
	 * @see #readFullLineOnly(BufferedReader)
	 * */
	private void processConsoleOutput(final String text) {
		builder.append(text);
		StringReader reader = new StringReader(builder.toString());
		BufferedReader stdout = new BufferedReader(reader);
		fastOffset = 0;
		readFullLineOnly(stdout);
		while (null != fastLine) {
			if (verdictExtraction && (executionFinished.reset(fastLine).matches())) {
				String reason = executionFinished.group(2);
				if (reasonMatcher.reset(reason).matches()) {
					executedTests.add(new ExecutedTestcase((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), executionFinished
							.group(1), reasonMatcher.group(1), reasonMatcher.group(2)));
				} else {
					executedTests.add(new ExecutedTestcase((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), executionFinished
							.group(1), executionFinished.group(2), ""));
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
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", fastLine));
			builder.delete(0, fastOffset);
			if (Activator.getMainView() != null) {
				Activator.getMainView().refreshIfSelected(mainControllerRoot);
			} else {
				TestExecutionView.refreshInput(this);
			}
			fastOffset = 0;
			readFullLineOnly(stdout);
		}

		try {
			stdout.close();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		reader.close();
	}

	/**
	 * Reads and removes a full line from the output. The read line is reported in the fastLine variable.
	 *
	 * @param stdout the buffered output to be processed.
	 *
	 * @see #processConsoleOutput(String)
	 * */
	public void readFullLineOnly(final BufferedReader stdout) {
		try {
			fastLine = stdout.readLine();
			if (fastLine == null) {
				return;
			}
			fastOffset += fastLine.length();
			if (builder.length() < fastOffset + 1) {
				fastLine = null;
				return;
			}
			char c = builder.charAt(fastOffset);
			if (c == '\n') {
				fastOffset++;
				return;
			} else if (c == '\r') {
				if (builder.length() > fastOffset + 1 && builder.charAt(fastOffset + 1) == '\n') {
					fastOffset++;
				}
				fastOffset++;
				return;
			}
			fastLine = null;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public boolean isTerminated() {
		return !isrunning;
	}

	@Override
	public IProcess getProcess() {
		return process;
	}

	@Override
	public void terminate(final boolean external) {
		isrunning = false;
		startExecutionAction.setEnabled(false);
		if (mainControllerRoot != null) {
			mainControllerRoot.setTerminated();
			LaunchElement launchElement = null;
			boolean terminatedNaturally = false;
			for (Map.Entry<ILaunch, BaseExecutor> entry : ExecutorStorage.getExecutorMap().entrySet()) {
				if (entry.getValue().equals(mainControllerRoot.executor())) {
					terminatedNaturally = entry.getKey().isTerminated();
					if (LaunchStorage.getLaunchElementMap().containsKey(entry.getKey())) {
						launchElement = LaunchStorage.getLaunchElementMap().get(entry.getKey());
					}
				}
			}
			if (launchElement != null
					&& !terminatedNaturally && proc != null) {
				proc.destroy();

				try {
					process.terminate();
				} catch (DebugException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
				launchElement.setTerminated();
			}

		}
		if (!keepTemporarilyGeneratedConfigFiles && temporalConfigFile != null) {
			boolean result = temporalConfigFile.delete();
			if (!result) {
				ErrorReporter.logError("The temporal configuration file " + temporalConfigFile.getName() + " could not be deleted");
				return;
			}
		}

		super.shutdownSession();
		temporalConfigFile = null;
		if (Activator.getMainView() != null) {
			Activator.getMainView().refreshAll();
		}

		saveLastTimeUsageInfo();
	}

	@Override
	public MenuManager createMenu(final MenuManager manager) {
		if (isrunning) {
			manager.add(startExecutionAction);
		}
		return super.createMenu(manager);
	}

	@Override
	protected String getDefaultLogFileName() {
		return GeneralConstants.DEFAULT_LOGFILENAME_SINGLE;
	}
}
