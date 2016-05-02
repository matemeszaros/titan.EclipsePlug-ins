/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.mctr.cli;

import static org.eclipse.titan.executor.GeneralConstants.MCSTATEREFRESHTIMEOUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.utils.StringUtils;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.GeneralConstants;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.TITANDebugConsole;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ExecuteDialog;
import org.eclipse.titan.executor.executors.jni.JniExecutor;
import org.eclipse.titan.executor.preferences.PreferenceConstants;
import org.eclipse.titan.executor.properties.FieldEditorPropertyPage;
import org.eclipse.titan.executor.views.executormonitor.ComponentElement;
import org.eclipse.titan.executor.views.executormonitor.HostControllerElement;
import org.eclipse.titan.executor.views.executormonitor.InformationElement;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.titan.executor.views.testexecution.ExecutedTestcase;
import org.eclipse.titan.executor.views.testexecution.TestExecutionView;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * This executor handles the execution of tests compiled in a parallel mode, connecting to the MainController via command line.
 * 
 * @author Kristof Szabados
 * */
public final class CliExecutor extends BaseExecutor {
	
	private static final String EXTERNAL_TERMINATION = "Execution terminated from outside";

	private Action automaticExecution, startHC, cmtc, smtc, emtc, exit, info;
	private String fastLine;
	private int fastOffset;
	private boolean simpleExecutionRunning = false;
	private boolean running = false;
	private int suspectedLastState;

	private BackgroundThread thread;

	private boolean createMTCRequested = false;
	private boolean executeRequested = false;
	private List<String> executeList = new ArrayList<String>();
	private boolean shutdownRequested = false;
	private boolean executingConfigFile = false;

	private ConfigFileHandler configHandler;
	//private File temporalConfigFile;

	// ^ beginning of line
	// $ end of line
	// patterns to detect state changes based on the experienced output.

	// MTC output matchers
	private static final Pattern EXECUTION_FINISHED_PATTERN = Pattern.compile("^MTC@(.+): Test case (.*) finished\\. Verdict: (.*)$");
	private final Matcher executionFinishedMatcher = EXECUTION_FINISHED_PATTERN.matcher("");

	private static final Pattern REASON_PATTERN = Pattern.compile("^(.*) reason: (.*)$");
	private final Matcher reasonMatcher = REASON_PATTERN.matcher("");

	// MC output matchers
	private static final Pattern SUCCESSFUL_STARTUP_PATTERN = Pattern.compile("(\\w+)@(.+): Listening on TCP port (\\d+).");
	private final Matcher successfulStartUpMatcher = SUCCESSFUL_STARTUP_PATTERN.matcher("");

	private static final Pattern FULL_SUCCESSFUL_STARTUP_PATTERN = Pattern.compile("(\\w+)@(.+): Listening on IP address (.+) and TCP port (\\d+).");
	private final Matcher fullSuccessfulStartUpMatcher = FULL_SUCCESSFUL_STARTUP_PATTERN.matcher("");

	private static final Pattern HC_CONNECTED_PATTERN = Pattern.compile(" New HC connected from (.+)");
	private final Matcher hcConnectedMatcher = HC_CONNECTED_PATTERN.matcher("");

	private static final Pattern ERROR_STARTUP_PATTERN = Pattern.compile("Error: (.+)");
	
	private static final String MTC_CREATED = " MTC is created.";
	private static final String TEST_EXECUTION_FINISHED = " Test execution finished.";
	private static final String EXECUTE_SECTION_FINISHED = " Execution of [EXECUTE] section finished.";
	private static final String TERMINATING_MTC = " Terminating MTC.";
	private static final String MTC_TERMINATED = " MTC terminated.";
	private static final String SHUTDOWN_COMPLETE = " Shutdown complete.";

	// patterns used to analyze the actual state information
	private static final Pattern MC_STATE_PATTERN = Pattern.compile("^ MC state: (.+)$");
	private static final Pattern PAUSE_PATTERN = Pattern.compile("^ pause function: (.+)$");
	private static final Pattern CONSOLE_LOGGING_PATTERN = Pattern.compile("^ console logging: (.+)$");
	private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^  - (\\S*)( \\[.*\\])?( \\(.*\\))?:$");
	private static final Pattern HC_STATE_PATTERN = Pattern.compile("^     HC state: (.*)$");
	private static final Pattern HC_NAME_COMPONENT_PATTERN = Pattern.compile("^      - name: (.*), component reference: (.*)$");
	private static final Pattern HC_COMPONENT_PATTERN = Pattern.compile("^      - component reference: (.*)$");
	private static final Pattern HC_COMPONENT_TYPE_PATTERN = Pattern.compile("^         component type: (.*)$");
	private static final Pattern HC_COMPONENT_STATE_PATTERN = Pattern.compile("^         state: (.*)$");
	private static final Pattern HC_EXECUTED_PATTERN = Pattern.compile("^         executed (.*): (.*)$");
	private static final Pattern HC_LOCAL_VERDICT_PATTERN = Pattern.compile("^         local verdict: (.*)$");

	private IProcess process;
	private final StringBuilder builder = new StringBuilder();

	public CliExecutor(final ILaunchConfiguration configuration) throws CoreException {
		super(configuration);

		if (null == configFilePath) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, CONFIGFILEPATH_NULL, null);
			throw new CoreException(status);
		}


		running = true;
		suspectedLastState = JniExecutor.MC_INACTIVE;

		createActions();

		updateUI();
		int mcStateRefreshTimeout = configuration.getAttribute(MCSTATEREFRESHTIMEOUT, 5);
		thread = new BackgroundThread("cli state refresher", mcStateRefreshTimeout, this);
	}

	private void createActions() {
		automaticExecution = new Action("automatic execution") {
			@Override
			public void run() {
				simpleExecutionRunning = true;
				startExecution(false);
			}
		};
		automaticExecution.setToolTipText("automatic execution");
		automaticExecution.setEnabled(true);

		startHC = new Action("start Host Controllers") {
			@Override
			public void run() {
				startHostControllers();
			}
		};
		startHC.setToolTipText("start Host Controller");

		cmtc = new Action("create MTC") {
			@Override
			public void run() {
				createMainTestComponent();
			}
		};
		cmtc.setToolTipText("create Main Test Component");

		smtc = new Action("start execution") {
			@Override
			public void run() {
				startExecution(false);
			}
		};
		smtc.setToolTipText("start execution");

		emtc = new Action("terminate the MTC") {
			@Override
			public void run() {
				terminateMainTestComponent();
			}
		};
		emtc.setToolTipText("terminate the Main Test Component");

		exit = new Action("exit") {
			@Override
			public void run() {
				exit();
				disposeHostControllers();
			}
		};
		exit.setToolTipText("exit");
		exit.setEnabled(true);

		info = new Action("update information") {
			@Override
			public void run() {
				info();
			}
		};
		info.setToolTipText("update information");
		info.setEnabled(true);
	}

	@Override
	public void dispose() {
		super.dispose();
		startHC = null;
		cmtc = null;
		smtc = null;
		emtc = null;
		info = null;
	}

	@Override
	public MenuManager createMenu(final MenuManager manager) {
		if (running) {
			manager.add(automaticExecution);
			manager.add(new Separator());
			manager.add(startHC);
			manager.add(cmtc);
			manager.add(smtc);
			manager.add(emtc);
			manager.add(exit);
			manager.add(info);
		}
		return super.createMenu(manager);
	}

	/**
	 * Initializes the Executor by starting the mctr_cli and connecting to it.
	 *
	 * @param arg2 the launch configuration to take the setup data from
	 * */
	@Override
	public void startSession(final ILaunch arg2) {
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

		EnvironmentHelper.setTitanPath(env);
		EnvironmentHelper.set_LICENSE_FILE_PATH(env);

		String mctrCliPath = getMctrPath(env);

		List<String> command = new ArrayList<String>();
		command.add("sh");
		command.add("-c");

		if (addConfigFilePath(mctrCliPath, command)){
			return;
		}

		printCommandToTitanConsole(command);

		pb.command(command);
		pb.redirectErrorStream(true);
		if (null != workingdirectoryPath) {
			File workingDir = new File(workingdirectoryPath);
			if (!workingDir.exists()) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openError(null,
								"Execution failed",
								"The working directory `" + workingdirectoryPath + "' does not exist.");
					}
				});
			}
			pb.directory(workingDir);
		}
		Process proc;
		try {
			proc = pb.start();

			final InputStream inputstream = proc.getInputStream();
			if (inputstream.markSupported()) {
				inputstream.mark(40000);
			}
			BufferedReader stdout = new BufferedReader(new InputStreamReader(inputstream));
			processWelcomeScreen(stdout);
			if (inputstream.markSupported()) {
				inputstream.reset();
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			proc = null;
		}

		if (null != proc) {
			process = DebugPlugin.newProcess(arg2, proc, MAIN_CONTROLLER);

			IStreamsProxy proxy = process.getStreamsProxy();
			if (null != proxy) {
				IStreamMonitor outputStreamMonitor = proxy.getOutputStreamMonitor();
				IStreamListener outputListener = new IStreamListener() {

					@Override
					public void streamAppended(final String text, final IStreamMonitor monitor) {
						processConsoleOutput(text);
					}

				};
				if (null != outputStreamMonitor) {
					processConsoleOutput(outputStreamMonitor.getContents());
					outputStreamMonitor.addListener(outputListener);
				}
			}
			info();
		}

		super.startSession(arg2);

		if (null == proc || null == process || process.isTerminated()) {
			terminate(true);
		}

		if (null != Activator.getMainView()) {
			Activator.getMainView().refreshAll();
		}

		if (null != thread) {
			thread.start();
		}
	}

	private boolean addConfigFilePath(String mctrCliPath, List<String> command) {
		if (isLogFolderSet()) {
			if (!StringUtils.isNullOrEmpty(configFilePath)) {
				configHandler = readConfigFile();
				if (configHandler == null) {
					ErrorReporter.parallelErrorDisplayInMessageDialog(
							"An error was found while processing the configuration file",
							"Please refer to the Error Log view for further information.");
					return true;
				} else if( configHandler.isErroneous() ) {

					if (configHandler.parseExceptions().isEmpty()) {
						ErrorReporter.parallelErrorDisplayInMessageDialog(
								"An error was found while processing the configuration file",
								"Please refer to the Error Log view for further information.");
					} else {
						Throwable exception = configHandler.parseExceptions().get(configHandler.parseExceptions().size() - 1);
						ErrorReporter.parallelErrorDisplayInMessageDialog(
								"Error while processing the configuration file",
								exception.getMessage() + "\n Please refer to the Error Log view for further information.");
					}
					return true;
				}
			} else {
				logFileNameDefined = false;
			}

			//TODO: implement
			if ( !CREATE_TEMP_CFG || logFileNameDefined ) {
				if (!StringUtils.isNullOrEmpty(configFilePath)) {
					command.add(" sleep 1; " + mctrCliPath + " '" + PathConverter.convert(configFilePath, true, TITANDebugConsole.getConsole()) + "'");
				} else {
					command.add(" sleep 1; " + mctrCliPath);
				}
			} else {
				generateTemporalCfgFile(generateCfgString());
				command.add(" sleep 1; " + mctrCliPath + " '" + PathConverter.convert(temporalConfigFile.getName(), true, TITANDebugConsole.getConsole()) + "'");
			}
		} else {
			temporalConfigFile = null;
			if (!StringUtils.isNullOrEmpty(configFilePath)) {
				command.add(" sleep 1; " + mctrCliPath + " '" + PathConverter.convert(configFilePath, true, TITANDebugConsole.getConsole()) + "'");
			} else {
				command.add(" sleep 1; " + mctrCliPath);
			}
		}
		return false;
	}

	private boolean isLogFolderSet() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String preferenceValue = FieldEditorPropertyPage.getOverlayedPreferenceValue(
				preferenceStore, project, PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID, PreferenceConstants.SET_LOG_FOLDER);
		return Boolean.parseBoolean(preferenceValue);
	}

	private void printCommandToTitanConsole(List<String> command) {
		MessageConsoleStream stream = TITANConsole.getConsole().newMessageStream();
		for (String c : command) {
			stream.print(c + ' ');
		}
		stream.println();
	}

	private String getMctrPath(Map<String, String> env) {
		String mctrCliPath;
		if (env.containsKey("TTCN3_DIR")) {
			mctrCliPath = "$TTCN3_DIR/bin/mctr_cli";
		} else {
			TITANDebugConsole.getConsole().newMessageStream().println("warning: TTCN3_DIR environment variable is not set");
			mctrCliPath = "mctr_cli";
		}
		return mctrCliPath;
	}

	/**
	 * Creates the Main Test Component.
	 * */
	private void createMainTestComponent() {
		createMTCRequested = true;
		switch (suspectedLastState) {
		case JniExecutor.MC_INACTIVE:
		case JniExecutor.MC_LISTENING:
		case JniExecutor.MC_LISTENING_CONFIGURED:
			startHostControllers();
			return;
		default:
			break;
		}

		IStreamsProxy proxy = process.getStreamsProxy();
		if (proxy != null) {
			try {
				proxy.write("cmtc \n");
				createMTCRequested = false;
			} catch (IOException e) {
				ErrorReporter.logError(EXTERNAL_TERMINATION);
				terminate(true);
			}
		}
	}

	/**
	 * Initializes and displays a dialog to the user. If the user selected an
	 * executable element, it is also started here.
	 * 
	 * @param automaticExecution tells if the configuration file shall be automatically selected for execution,
	 *                              or shall the user be able to select something.
	 * */
	private void startExecution(final boolean automaticExecution) {
		if (automaticExecution && !StringUtils.isNullOrEmpty(configFilePath)) {
			final ConfigFileHandler tempConfigHandler = readConfigFile();

			if (!tempConfigHandler.isErroneous() && !tempConfigHandler.getExecuteElements().isEmpty()) {
				executionStarted = true;
				executeList.add("smtc \n");
				executeRequested = true;

				if (JniExecutor.MC_READY == suspectedLastState) {
					executeNextTestElement();
				} else {
					createMainTestComponent();
				}
				return;
			}

			if ( CREATE_TEMP_CFG ) {
				Display.getDefault().syncExec( new EmptyExecutionRunnable() );
			}
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				boolean invalidSelection = false;
				do {
					ExecuteDialog dialog = new ExecuteDialog(null);
					dialog.setControlparts(availableControlParts);
					dialog.setTestcases(availableTestcases);
					dialog.setTestsets(availableTestSetNames);
					if (!StringUtils.isNullOrEmpty(configFilePath)) {
						dialog.setConfigurationFile(configFilePath);
					}
					dialog.setSelection(lastTimeSelection, lastTimeSelectionTime, lastTimeSelectionType);

					if (dialog.open() != Window.OK) {
						executionStarted = false;
						shutdownSession();
						return;
					}

					String selectedName = dialog.getSelectedElement();
					lastTimeSelectionType = dialog.getSelectionType();
					lastTimeSelection = selectedName;
					lastTimeSelectionTime = dialog.getSelectionTimes();
					switch (lastTimeSelectionType) {
					case TESTCASE:
						invalidSelection = false;
						for (int i = 0; i < lastTimeSelectionTime; i++) {
							executeList.add("smtc " + selectedName + '\n');
						}
						executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime);
						break;
					case TESTSET:
						invalidSelection = false;
						int index = availableTestSetNames.indexOf(lastTimeSelection);
						List<String> contents = availableTestSetContents.get(index);
						for (int j = 0; j < lastTimeSelectionTime; j++) {
							for (String content : contents) {
								executeList.add("smtc " + content + '\n');
							}
						}
						executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * contents.size());
						break;
					case CONTROLPART:
						invalidSelection = false;
						for (int i = 0; i < lastTimeSelectionTime; i++) {
							executeList.add("smtc " + selectedName + '\n');
						}
						executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * 5);
						break;
					case CONFIGURATIONFILE: {
						final ConfigFileHandler tempConfigHandler = readConfigFile();
						List<String> configurationFileElements = tempConfigHandler.getExecuteElements();
						if (configurationFileElements.isEmpty()) {
							invalidSelection = true;
							Display.getDefault().syncExec( new EmptyExecutionRunnable() );
						} else {
							invalidSelection = false;
							for (int i = 0; i < lastTimeSelectionTime; i++) {
								executeList.add("smtc \n");
							}
						}
						break;
					}
					default:
						break;
					}
					executeRequested = true;

				} while (invalidSelection);

				executionStarted = true;

				if (JniExecutor.MC_READY == suspectedLastState) {
					executeNextTestElement();
				} else {
					createMainTestComponent();
				}

				saveLastTimeUsageInfo();
			}
		});
	}

	/**
	 * Executes the next testcase or control part whose name is stored in the execute list.
	 * */
	private void executeNextTestElement() {
		if (JniExecutor.MC_READY != suspectedLastState) {
			createMainTestComponent();
			return;
		}
		if (executeList.isEmpty()) {
			return;
		}
		String testElement = executeList.remove(0);
		IStreamsProxy proxy = process.getStreamsProxy();
		if (proxy != null) {
			try {
				suspectedLastState = JniExecutor.MC_EXECUTING_TESTCASE;
				proxy.write(testElement);
				if ("smtc \n".equals(testElement)) {
					executingConfigFile = true;
				}
			} catch (IOException e) {
				ErrorReporter.logError(EXTERNAL_TERMINATION);
				terminate(true);
			}
		}
	}

	/**
	 * Teminates the Main Test Component.
	 * */
	private void terminateMainTestComponent() {
		IStreamsProxy proxy = process.getStreamsProxy();
		if (proxy != null) {
			try {
				proxy.write("emtc \n");
			} catch (IOException e) {
				ErrorReporter.logError(EXTERNAL_TERMINATION);
				terminate(true);
			}
		}
	}

	/**
	 * Exits the Executor.
	 * */
	private void exit() {
		IStreamsProxy proxy = process.getStreamsProxy();
		if (proxy != null) {
			try {
				proxy.write("exit \n");
			} catch (IOException e) {
				ErrorReporter.logError(EXTERNAL_TERMINATION);
				terminate(true);
			}
		}
	}

	/**
	 * Prints information to the console about the actual state of the system.
	 * */
	public void info() {
		IStreamsProxy proxy = process.getStreamsProxy();
		if (proxy != null) {
			try {
				proxy.write("info \n");
			} catch (IOException e) {
				ErrorReporter.logError(EXTERNAL_TERMINATION);
				terminate(true);
			}
		}
	}

	@Override
	protected void shutdownSession() {
		shutdownRequested = true;
		switch (suspectedLastState) {
		case JniExecutor.MC_LISTENING:
		case JniExecutor.MC_LISTENING_CONFIGURED:
		case JniExecutor.MC_HC_CONNECTED:
		case JniExecutor.MC_ACTIVE:
			exit();
			disposeHostControllers();
			shutdownRequested = false;
			createMTCRequested = false;
			executeRequested = false;
			break;
		default:
			terminateMainTestComponent();
		}

		super.shutdownSession();
	}

	/**
	 * Tries to parse in mctr_cli's answer to the info command.
	 *
	 * @param stdout the output of the process to be parsed.
	 */
	private void processInfoOutput(final BufferedReader stdout) {
		Matcher matcher;

		MainControllerElement tempRoot = new MainControllerElement("Temporal root", this);
		readFullLineOnly(stdout);
		if (fastLine == null) {
			return;
		}
		matcher = MC_STATE_PATTERN.matcher(fastLine);
		if (matcher.matches()) {
			String mcStateName = matcher.group(1);
			tempRoot.setStateInfo(new InformationElement("State: " + mcStateName));
			readFullLineOnly(stdout);

			suspectedLastState = getMCStateFromName(mcStateName);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && " host information:".equals(fastLine)) {
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null) {
			if (fastLine.startsWith("  -")) {
				// host list
				while (fastLine != null && fastLine.startsWith("  -")) {
					processInfoOutputHC(stdout, tempRoot);
				}
			} else if ("  no HCs are connected".equals(fastLine)) {
				readFullLineOnly(stdout);
			}
		} else {
			fastLine = null;
			return;
		}

		if (fastLine != null && PAUSE_PATTERN.matcher(fastLine).matches()) {
			tempRoot.setPauseInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && CONSOLE_LOGGING_PATTERN.matcher(fastLine).matches()) {
			tempRoot.setConsoleLoggingInfo(new InformationElement(fastLine.trim()));
		} else {
			fastLine = null;
			return;
		}
		if (mainControllerRoot != null) {
			mainControllerRoot.children().clear();
			mainControllerRoot.transferData(tempRoot);
		}

	}

	/**
	 * Processes the host controller related part of the mctr_cli's answer to the info command.
	 * <p>
	 * @see #processInfoOutput(BufferedReader)
	 * 
	 * @param stdout the output of the process to be parsed.
	 * @param root the main controller element to add the host controller data to.
	 * */
	private void processInfoOutputHC(final BufferedReader stdout, final MainControllerElement root) {
		HostControllerElement tempHost;
		Matcher matcher = HOSTNAME_PATTERN.matcher(fastLine);
		if (matcher.matches()) {
			tempHost = new HostControllerElement("Host Controller");
			root.addHostController(tempHost);
			String hostIPAddress = matcher.group(1);
			tempHost.setIPAddressInfo(new InformationElement("IP address: " + hostIPAddress));
			String hostIpNumber = matcher.group(2);
			if (hostIpNumber != null && hostIpNumber.length() > 0) {
				tempHost.setIPNumberInfo(new InformationElement("IP number: " + hostIpNumber.substring(2, hostIpNumber.length() - 1)));
			}
			String localHostName = matcher.group(3);
			if (localHostName != null && localHostName.length() > 0) {
				tempHost.setHostNameInfo(new InformationElement("Local host name: "
						+ localHostName.substring(2, localHostName.length() - 1)));
			}
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && fastLine.startsWith("     operating system")) {
			tempHost.setOperatingSystemInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && HC_STATE_PATTERN.matcher(fastLine).matches()) {
			tempHost.setStateInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && "     test component information:".equals(fastLine)) {
			readFullLineOnly(stdout);
			if (fastLine != null && fastLine.startsWith("      no components on this host")) {
				readFullLineOnly(stdout);
			} else {
				while (fastLine != null && fastLine.startsWith("      - ")) {
					processInfoOutputComponent(stdout, tempHost);
				}
			}
		} else {
			fastLine = null;
		}
	}

	/**
	 * Processes the component related part of the mctr_cli's answer to the info command.
	 * <p>
	 * @see #processInfoOutputHC(BufferedReader, MainControllerElement)
	 * 
	 * @param stdout the output of the process to be parsed.
	 * @param host the host to add the component data to.
	 * */
	private void processInfoOutputComponent(final BufferedReader stdout, final HostControllerElement host) {
		Matcher matcher;
		ComponentElement tempComponent;

		matcher = HC_NAME_COMPONENT_PATTERN.matcher(fastLine);
		if (matcher.matches()) {
			tempComponent = new ComponentElement("Component " + matcher.group(1), new InformationElement(
					"Component reference: " + matcher.group(2)));
			host.addComponent(tempComponent);

			readFullLineOnly(stdout);
		} else {
			matcher = HC_COMPONENT_PATTERN.matcher(fastLine);
			if (matcher.matches()) {
				tempComponent = new ComponentElement("Component " + matcher.group(1));
				host.addComponent(tempComponent);
				readFullLineOnly(stdout);
			} else {
				fastLine = null;
				return;
			}
		}
		if (fastLine != null && HC_COMPONENT_TYPE_PATTERN.matcher(fastLine).matches()) {
			tempComponent.setTypeInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		}
		if (fastLine != null && HC_COMPONENT_STATE_PATTERN.matcher(fastLine).matches()) {
			tempComponent.setStateInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		} else {
			fastLine = null;
			return;
		}
		if (fastLine != null && HC_EXECUTED_PATTERN.matcher(fastLine).matches()) {
			tempComponent.setExecutedInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		}
		if (fastLine != null && HC_LOCAL_VERDICT_PATTERN.matcher(fastLine).matches()) {
			tempComponent.setLocalVerdictInfo(new InformationElement(fastLine.trim()));
			readFullLineOnly(stdout);
		}
	}

	/**
	 * Processes the welcome screen. 
	 * This is needed as sometimes the process already forgets about this information, 
	 * when the listeners get installed.
	 * It waits for main controller output lines and returns if it received any of these messages:
	 * <li>Listening on TCP port ...
	 * <li>Listening on IP address ... and TCP port ...
	 * <li>Error: ...
	 * It stores the IP address and TCP port gathered from the message
	 * 
	 * @param stdout the output of the process to be parsed.
	 * */
	private void processWelcomeScreen(final BufferedReader stdout) {
		String line;
		boolean started = false;
		try {
			line = stdout.readLine();
			while (!started && line != null) {
				Matcher m = SUCCESSFUL_STARTUP_PATTERN.matcher(line);
				if (m.matches()) {
					started = true;
					mcHost = m.group(2);
					mcPort = m.group(3);
					suspectedLastState = JniExecutor.MC_LISTENING;
				} else {
					m = FULL_SUCCESSFUL_STARTUP_PATTERN.matcher(line);
					if (m.matches()) {
						started = true;
						mcHost = m.group(3);
						mcPort = m.group(4);
						suspectedLastState = JniExecutor.MC_LISTENING;
					} else {
						m = ERROR_STARTUP_PATTERN.matcher(line);
						if (m.matches()) {
							started = true;
							suspectedLastState = JniExecutor.MC_LISTENING;
						} else {
						  line = stdout.readLine();
						}
					}
				}
				addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", line));
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Tests if the provided String reports the end of the execution of a testcase. If this is the case and verdict extraction is allowed then a new
	 * item is inserted into the list of extracted testcases
	 * */
	private void testExecution() {
		if (fastLine != null && verdictExtraction && (executionFinishedMatcher.reset(fastLine).matches())) {
			String reason = executionFinishedMatcher.group(3);
			if (reasonMatcher.reset(reason).matches()) {
				executedTests.add(new ExecutedTestcase((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(),
						executionFinishedMatcher.group(2), reasonMatcher.group(1), reasonMatcher.group(2)));
			} else {
				executedTests.add(new ExecutedTestcase((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(),
						executionFinishedMatcher.group(2), executionFinishedMatcher.group(3), ""));
			}
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
	// TODO add support for following batch mode (exit)
	private void processConsoleOutput(final String text) {
		if (thread != null) {
			thread.reset();
		}
		builder.append(text);
		StringReader reader = new StringReader(builder.toString());
		BufferedReader stdout = new BufferedReader(reader);
		fastOffset = 0;
		readFullLineOnly(stdout);
		while (fastLine != null) {
			while (fastLine.startsWith("MC2> ")) {
				fastLine = fastLine.substring(5);
			}

			if (fastLine.startsWith("MC information:")) {
				processInfoOutput(stdout);
				if (fastLine == null) {
					break;
				}
				if (Activator.getMainView() != null) {
					Activator.getMainView().refreshAll();
				}
			} else if (fastLine.startsWith("MTC@")) {
				addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", fastLine));
				testExecution();
			} else if (fastLine.startsWith("MC@")) {
				addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", fastLine));
				int index = fastLine.indexOf(':');
				String shortversion = fastLine.substring(index + 1);
				if (SHUTDOWN_COMPLETE.equals(shortversion)) {
					running = false;
					executeRequested = false;
					createMTCRequested = false;
					if (mainControllerRoot != null) {
						mainControllerRoot.setTerminated();
					}
					if (Activator.getMainView() != null) {
						Activator.getMainView().refreshAll();
					}
					if (thread != null) {
						thread.cancel();
						thread = null;
					}
				} else if (hcConnectedMatcher.reset(shortversion).matches()) {
					suspectedLastState = JniExecutor.MC_HC_CONNECTED;
				} else if (MTC_CREATED.equals(shortversion)) {
					suspectedLastState = JniExecutor.MC_READY;
				} else if (TEST_EXECUTION_FINISHED.equals(shortversion)) {
					if (!executingConfigFile) {
						suspectedLastState = JniExecutor.MC_READY;
					}
				} else if (EXECUTE_SECTION_FINISHED.equals(shortversion)) {
					suspectedLastState = JniExecutor.MC_READY;
					executingConfigFile = false;
				} else if (TERMINATING_MTC.equals(shortversion)) {
					suspectedLastState = JniExecutor.MC_TERMINATING_MTC;
				} else if (MTC_TERMINATED.equals(shortversion)) {
					suspectedLastState = JniExecutor.MC_ACTIVE;
				} else if (successfulStartUpMatcher.reset(fastLine).matches()) {
					mcHost = successfulStartUpMatcher.group(2);
					mcPort = successfulStartUpMatcher.group(3);
					suspectedLastState = JniExecutor.MC_LISTENING;
				} else if (fullSuccessfulStartUpMatcher.reset(shortversion).matches()) {
					mcHost = fullSuccessfulStartUpMatcher.group(1);
					mcPort = fullSuccessfulStartUpMatcher.group(2);
					suspectedLastState = JniExecutor.MC_LISTENING;
				}
			} else {
				addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), "", "", fastLine));
			}
			builder.delete(0, fastOffset);
			if (Activator.getMainView() != null) {
				Activator.getMainView().refreshIfSelected(mainControllerRoot);
			} else {
				TestExecutionView.refreshInput(this);
			}
			fastOffset = 0;
			readFullLineOnly(stdout);

			TITANDebugConsole.getConsole().newMessageStream().println("MC in suspected state: " + suspectedLastState);
		}

		statusChangeHandler();
	}

	/**
	 * Reads and removes a full line from the output. The read line is reported in the fastLine variable.
	 *
	 * @param stdout the buffered output to be processed.
	 *
	 * @see #processConsoleOutput(String)
	 * */
	private void readFullLineOnly(final BufferedReader stdout) {
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

	/**
	 * Tries to find out knowing the actual assumed state and what actions are wished for, which of them should be performed next.
	 * */
	private void statusChangeHandler() {
		switch (suspectedLastState) {
		case JniExecutor.MC_LISTENING:
		case JniExecutor.MC_LISTENING_CONFIGURED:
			if (automaticExecuteSectionExecution) {
				automaticExecuteSectionExecution = false;
				simpleExecutionRunning = true;
				startExecution(true);
			}
			break;
		case JniExecutor.MC_HC_CONNECTED:
		case JniExecutor.MC_ACTIVE:
			if (createMTCRequested) {
				createMainTestComponent();
			} else if (shutdownRequested) {
				shutdownSession();
			}
			break;
		case JniExecutor.MC_READY:
			if (executeList.isEmpty()) {
				executeRequested = false;
			}
			if (executeRequested) {
				executeNextTestElement();
			} else if (simpleExecutionRunning || shutdownRequested) {
				shutdownSession();
			}
			break;
		case JniExecutor.MC_INACTIVE:
			shutdownRequested = false;
			break;
		case JniExecutor.MC_SHUTDOWN:
			break;
		default:
			break;
		}

		updateUI();
	}

	/**
	 * Updates the enabladness of the invokable menu entries.
	 * */
	private void updateUI() {
		if (startHC != null) {
			startHC.setEnabled(JniExecutor.MC_LISTENING == suspectedLastState || JniExecutor.MC_LISTENING_CONFIGURED == suspectedLastState);
			cmtc.setEnabled(JniExecutor.MC_LISTENING == suspectedLastState || JniExecutor.MC_LISTENING_CONFIGURED == suspectedLastState
					|| JniExecutor.MC_HC_CONNECTED == suspectedLastState || JniExecutor.MC_ACTIVE == suspectedLastState);
			smtc.setEnabled(JniExecutor.MC_READY == suspectedLastState);
			emtc.setEnabled(JniExecutor.MC_READY == suspectedLastState);
		}
	}

	@Override
	public boolean isTerminated() {
		return !running;
	}

	@Override
	public IProcess getProcess() {
		return process;
	}

	@Override
	public void terminate(final boolean external) {
		if (external) {
			running = false;
			executeRequested = false;
			createMTCRequested = false;
			if (thread != null) {
				thread.reset();
				thread = null;
			}
			if (mainControllerRoot != null) {
				mainControllerRoot.setTerminated();
			}
		} else {
			exit();
		}

		disposeHostControllers();
	}

	/**
	 * Converts the name of the Main Controller state (reported by itself) into an actual state flag.
	 *
	 * @param mcStateName the name of the Main Controller state
	 * @return the state flag correspondent to the state name, Ready by default.
	 * */
	private static int getMCStateFromName(final String mcStateName) {
		if ("inactive".equals(mcStateName)) {
			return JniExecutor.MC_INACTIVE;
		} else if ("listening".equals(mcStateName)) {
			return JniExecutor.MC_LISTENING;
		} else if ("listening (configured)".equals(mcStateName)) {
			return JniExecutor.MC_LISTENING_CONFIGURED;
		} else if ("HC connected".equals(mcStateName)) {
			return JniExecutor.MC_HC_CONNECTED;
		} else if ("configuring...".equals(mcStateName)) {
			return JniExecutor.MC_CONFIGURING;
		} else if ("active".equals(mcStateName)) {
			return JniExecutor.MC_ACTIVE;
		} else if ("creating MTC...".equals(mcStateName)) {
			return JniExecutor.MC_CREATING_MTC;
		} else if ("terminating MTC...".equals(mcStateName)) {
			return JniExecutor.MC_TERMINATING_MTC;
		} else if ("ready".equals(mcStateName)) {
			return JniExecutor.MC_READY;
		} else if ("executing control part".equals(mcStateName)) {
			return JniExecutor.MC_EXECUTING_CONTROL;
		} else if ("executing testcase".equals(mcStateName)) {
			return JniExecutor.MC_EXECUTING_TESTCASE;
		} else if ("terminating testcase...".equals(mcStateName)) {
			return JniExecutor.MC_TERMINATING_TESTCASE;
		} else if ("paused after testcase".equals(mcStateName)) {
			return JniExecutor.MC_PAUSED;
		} else if ("shutting down...".equals(mcStateName)) {
			return JniExecutor.MC_SHUTDOWN;
		}

		return JniExecutor.MC_READY;
	}

	@Override
	protected String getDefaultLogFileName() {
		return GeneralConstants.DEFAULT_LOGFILENAME_PARALLEL;
	}

	@Override
	protected String generateCfgString() {
		String result = super.generateCfgString();
		if (configHandler != null) {
			result += configHandler.toStringResolved(new ArrayList<Integer>());
		}
		return result;
	}
}
