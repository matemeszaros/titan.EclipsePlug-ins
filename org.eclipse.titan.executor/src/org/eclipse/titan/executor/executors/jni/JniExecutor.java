/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.executors.jni;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.CfgLexer;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.GeneralConstants;
import org.eclipse.titan.executor.TITANConsole;
import org.eclipse.titan.executor.designerconnection.EnvironmentHelper;
import org.eclipse.titan.executor.executors.BaseExecutor;
import org.eclipse.titan.executor.executors.ExecuteDialog;
import org.eclipse.titan.executor.executors.ExecuteDialog.ExecutableType;
import org.eclipse.titan.executor.executors.SeverityResolver;
import org.eclipse.titan.executor.jni.ComponentStruct;
import org.eclipse.titan.executor.jni.HostStruct;
import org.eclipse.titan.executor.jni.IJNICallback;
import org.eclipse.titan.executor.jni.JNIMiddleWare;
import org.eclipse.titan.executor.jni.McStateEnum;
import org.eclipse.titan.executor.jni.QualifiedName;
import org.eclipse.titan.executor.jni.Timeval;
import org.eclipse.titan.executor.jni.VerdictTypeEnum;
import org.eclipse.titan.executor.views.executormonitor.ComponentElement;
import org.eclipse.titan.executor.views.executormonitor.ExecutorStorage;
import org.eclipse.titan.executor.views.executormonitor.HostControllerElement;
import org.eclipse.titan.executor.views.executormonitor.InformationElement;
import org.eclipse.titan.executor.views.executormonitor.LaunchElement;
import org.eclipse.titan.executor.views.executormonitor.LaunchStorage;
import org.eclipse.titan.executor.views.executormonitor.MainControllerElement;
import org.eclipse.titan.executor.views.notification.Notification;
import org.eclipse.titan.executor.views.testexecution.ExecutedTestcase;
import org.eclipse.titan.executor.views.testexecution.TestExecutionView;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * This executor handles the execution of tests compiled in a parallel mode, via directly connecting to the MainController written in C++.
 * 
 * @author Kristof Szabados
 * */
public final class JniExecutor extends BaseExecutor implements IJNICallback {
	public static final int MC_INACTIVE = 0;
	public static final int MC_LISTENING = 1;
	public static final int MC_LISTENING_CONFIGURED = 2;
	public static final int MC_HC_CONNECTED = 3;
	public static final int MC_CONFIGURING = 4;
	public static final int MC_ACTIVE = 5;
	public static final int MC_SHUTDOWN = 6;
	public static final int MC_CREATING_MTC = 7;
	public static final int MC_READY = 8;
	public static final int MC_TERMINATING_MTC = 9;
	public static final int MC_EXECUTING_CONTROL = 10;
	public static final int MC_EXECUTING_TESTCASE = 11;
	public static final int MC_TERMINATING_TESTCASE = 12;
	public static final int MC_PAUSED = 13;

	private boolean startHCRequested = false;
	private boolean configureRequested = false;
	private int configFileExecutionRequestCounter = -1;
	private boolean createMTCRequested = false;
	private boolean executeRequested = false;
	private List<String> executeList = new ArrayList<String>();
	private boolean shutdownRequested = false;
	private JNIMiddleWare jnimw;

	private boolean simpleExecutionRunning = false;
	private boolean isTerminated = false;

	private static boolean isRunning = false;
	private boolean loggingIsEnabled = true;
	private static final String EXECUTION_FINISHED = "^Test case (.*) finished\\. Verdict: (.*)$";
	private static final Pattern EXECUTION_FINISHED_PATTERN = Pattern.compile(EXECUTION_FINISHED);
	private final Matcher executionFinishedMatcher = EXECUTION_FINISHED_PATTERN.matcher("");
	private static final String REASON = "^(.*) reason: (.*)$";
	private static final Pattern REASON_PATTERN = Pattern.compile(REASON);
	private final Matcher reasonMatcher = REASON_PATTERN.matcher("");
	private static final String EMPTY_STRING = "";

	private Action automaticExecution, startSession, configure, startHCs, cmtc, smtc, generalPause, cont, stop, emtc, generalLogging,
	shutdownSession, info;

	private MessageConsoleStream consoleStream;

	private ConfigFileHandler configHandler = null;

	public JniExecutor(final ILaunchConfiguration configuration) throws CoreException {
		super(configuration);

		if (null == configFilePath) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, CONFIGFILEPATH_NULL, null);
			throw new CoreException(status);
		}

		automaticExecution = new Action("automatic execution") {
			@Override
			public void run() {
				simpleExecutionRunning = true;
				startTest(false);
			}
		};
		automaticExecution.setToolTipText("automatic execution");

		startSession = new Action("Start session") {
			@Override
			public void run() {
				initialization();
			}
		};
		startSession.setToolTipText("Start session");

		startHCs = new Action("Start HCs") {
			@Override
			public void run() {
				startHostControllers();
			}
		};
		startHCs.setToolTipText("Start HCs");

		configure = new Action("Set parameters") {
			@Override
			public void run() {
				configure();
			}
		};
		configure.setToolTipText("Set parameters");

		cmtc = new Action("create MTC") {
			@Override
			public void run() {
				createMTC();
			}
		};
		cmtc.setToolTipText("create MTC");

		smtc = new Action("Execute..") {
			@Override
			public void run() {
				startTest(false);
			}
		};
		smtc.setToolTipText("Execute..");

		generalPause = new Action("Pause execution", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				jnimw.stop_after_testcase(generalPause.isChecked());
			}
		};
		generalPause.setToolTipText("Pause execution");
		generalPause.setChecked(false);

		cont = new Action("Continue execution") {
			@Override
			public void run() {
				jnimw.continue_testcase();
			}
		};
		cont.setToolTipText("Continue execution");

		stop = new Action("Stop execution") {
			@Override
			public void run() {
				stop();
			}
		};
		stop.setToolTipText("Stop execution");

		emtc = new Action("Exit MTC") {
			@Override
			public void run() {
				exitMTC();
			}
		};
		emtc.setToolTipText("Exit MTC");

		generalLogging = new Action("Generate console log") {
			@Override
			public void run() {
				if (generalLogging.isChecked()) {
					loggingIsEnabled = true;
				} else {
					loggingIsEnabled = false;
				}
			}
		};
		generalLogging.setToolTipText("Console logging");
		generalLogging.setChecked(true);

		shutdownSession = new Action("Shutdown session") {
			@Override
			public void run() {
				shutdownSession();
			}
		};
		shutdownSession.setToolTipText("Shutdown session");

		info = new Action("Update status information") {
			@Override
			public void run() {
				updateInfoDisplay();
			}
		};
		info.setToolTipText("Updates the status displaying hierarchy");

		consoleStream = TITANConsole.getConsole().newMessageStream();

		jnimw = new JNIMiddleWare(this);
		jnimw.initialize(1500);

		setRunning(true);

		isTerminated = false;
		loggingIsEnabled = true;
		updateGUI();
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private static void setRunning(final boolean newValue) {
		isRunning = newValue;
	}

	/**
	 * Initializes the Executor.
	 *
	 * @param launch the ILaunch instance to start the session with.
	 * */
	@Override
	public void startSession(final ILaunch launch) {
		super.startSession(launch);

		if (automaticExecuteSectionExecution) {
			if (!LaunchStorage.getLaunchElementMap().containsKey(launch)) {
				ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
				LaunchElement launchElement = new LaunchElement(launchConfiguration.getName(), launch);
				LaunchStorage.registerLaunchElement(launchElement);
				ExecutorStorage.registerExecutorStorage(launchElement);
			}

			simpleExecutionRunning = true;
			startTest(true);
		}
	}

	@Override
	public MenuManager createMenu(final MenuManager manager) {
		if (!isTerminated) {
			manager.add(automaticExecution);
			manager.add(new Separator());
			manager.add(startSession);
			manager.add(configure);
			manager.add(startHCs);
			manager.add(cmtc);
			manager.add(smtc);
			manager.add(generalPause);
			manager.add(cont);
			manager.add(stop);
			manager.add(emtc);
			manager.add(shutdownSession);
			manager.add(generalLogging);
			manager.add(info);
		}
		return super.createMenu(manager);
	}

	/**
	 * Inserts an error message into the notifications view.
	 *
	 * @param severity the severity of the message
	 * @param msg the message to be shown
	 * */
	@Override
	public void insertError(final int severity, final String msg) {
		consoleStream.println("Error: " + msg);

		if (severityLevelExtraction) {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), SeverityResolver
					.getSeverityString(severity), EMPTY_STRING, msg));
		} else {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), EMPTY_STRING, EMPTY_STRING, msg));
		}
		if (simpleExecutionRunning) {
			shutdownRequested = true;
		}
	}

	/**
	 * Inserts an error message into the notifications view.
	 *
	 * @param severity the severity of the message
	 * @param msg the message to be shown
	 * */
	@Override
	public void errorCallback(final int severity, final String msg) {
		insertError(severity, msg);

		if (null != Activator.getMainView()) {
			Activator.getMainView().refreshIfSelected(mainControllerRoot);
		}
	}

	/**
	 * Inserts a lists of messages into the notifications view in a batched manner
	 * <p>
	 * A list of String arrays issued to store every data reported regarding the message in a undecoded way. On this way if a data is not needed we
	 * don't need to decode it.
	 *
	 * @param s the list of String arrays.
	 * */
	@Override
	public void batchedInsertNotify(final ArrayList<String[]> s) {
		if (loggingIsEnabled && consoleLogging) {
			for (String[] sv : s) {
				consoleStream.println(sv[2] + ": " + sv[4]);
			}
		}

		List<String> times = new ArrayList<String>(s.size());
		List<Notification> tempNotifications = new ArrayList<Notification>(s.size());

		if (severityLevelExtraction) {
			int severity;
			for (String[] value : s) {
				severity = Integer.parseInt(value[3]);
				Formatter formatter = new Formatter();
				formatter.format(DATETIMEFORMAT, new Date(Long.parseLong(value[0]) * 1000), Long.valueOf(value[1]));
				times.add(formatter.toString());
				tempNotifications.add(new Notification(formatter.toString(), SeverityResolver.getSeverityString(severity), value[2], value[4]));
				formatter.close();
			}
		} else {
			for (String[] value : s) {
				Formatter formatter = new Formatter();
				formatter.format(DATETIMEFORMAT, new Date(Long.parseLong(value[0]) * 1000), Long.valueOf(value[1]));
				times.add(formatter.toString());
				tempNotifications.add(new Notification(formatter.toString(), EMPTY_STRING, value[2], value[4]));
				formatter.close();
			}
		}
		addNotifications(tempNotifications);
		if (verdictExtraction) {
			for (int i = 0; i < s.size(); i++) {
				if (executionFinishedMatcher.reset(s.get(i)[4]).matches()) {
					String reason = executionFinishedMatcher.group(2);
					if (reasonMatcher.reset(reason).matches()) {
						executedTests.add(new ExecutedTestcase(times.get(i), executionFinishedMatcher.group(1), reasonMatcher.group(1), reasonMatcher.group(2)));
					} else {
						executedTests
						.add(new ExecutedTestcase(times.get(i), executionFinishedMatcher.group(1), executionFinishedMatcher.group(2), ""));
					}
				}
			}
		}
	}

	/**
	 * Inserts a notification message into the notifications view.
	 *
	 * @param time the MainController reported time, when the notification message was created
	 * @param source the source line info of the notification message
	 * @param severity the severity of the message
	 * @param msg the message to be shown
	 * */
	@Override
	public void insertNotify(final Timeval time, final String source, final int severity, final String msg) {
		if (loggingIsEnabled && consoleLogging) {
			consoleStream.println(source + ": " + msg);
		}
		Formatter formatter = new Formatter();
		formatter.format(DATETIMEFORMAT, new Date(time.tv_sec * 1000), time.tv_usec);
		if (severityLevelExtraction) {
			addNotification(new Notification(formatter.toString(), SeverityResolver.getSeverityString(severity), source, msg));
		} else {
			addNotification(new Notification(formatter.toString(), EMPTY_STRING, source, msg));
		}

		if (verdictExtraction
				&& executionFinishedMatcher.reset(msg).matches()) {
			String reason = executionFinishedMatcher.group(2);
			if (reasonMatcher.reset(reason).matches()) {
				executedTests.add(new ExecutedTestcase(formatter.toString(), executionFinishedMatcher.group(1), reasonMatcher.group(1), reasonMatcher.group(2)));
			} else {
				executedTests.add(new ExecutedTestcase(formatter.toString(), executionFinishedMatcher.group(1), executionFinishedMatcher.group(2), ""));
			}
		}
	}

	/**
	 * Inserts a notification message into the notifications view.
	 *
	 * @param time the MainController reported time, when the notification message was created
	 * @param source the source line info of the notification message
	 * @param severity the severity of the message
	 * @param msg the message to be shown
	 * */
	@Override
	public void notifyCallback(final Timeval time, final String source, final int severity, final String msg) {
		insertNotify(time, source, severity, msg);

		if (Activator.getMainView() != null) {
			Activator.getMainView().refreshIfSelected(mainControllerRoot);
		} else {
			TestExecutionView.refreshInput(this);
		}
	}

	/**
	 * Handles a status change reported by the MainController.
	 * */
	@Override
	public void statusChangeCallback() {
		McStateEnum state = jnimw.get_state();
		switch (state.getValue()) {
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
			break;
		case MC_HC_CONNECTED:
			// FIXME per pillanat amig nem megy a get_host_data, feltetelezem, hogy NumHCs() == 1
			// if(!McStateEnum.MC_CONFIGURING.equals(previousState) &&
			// configHandler2.NumHCs() > 0){
			// HostStruct host = jnimw.get_host_data(configHandler2.NumHCs() -
			// 1);
			if (shutdownRequested) {
				shutdownSession();
			} else if (configureRequested) {
				configure();
			}
			break;
		case MC_ACTIVE:
			if (createMTCRequested) {
				createMTC();
			} else if (shutdownRequested) {
				shutdownSession();
			}
			break;
		case MC_READY:
			if (executeList.isEmpty()) {
				executeRequested = false;
			}
			if (executeRequested) {
				executeNextTest();
			} else if (simpleExecutionRunning || shutdownRequested) {
				shutdownSession();
			}
			break;
		case MC_INACTIVE:
			if (shutdownRequested) {
				shutdownRequested = false;
				// session shutdown is finished (requested by jnimw.shutdown_session())
				jnimw.terminate_internal();
				executeList.clear();

				disposeHostControllers();
			}
			break;
		case MC_SHUTDOWN:
			break;
		default:
		}

		updateGUI();
	}

	/**
	 * Initializes the test session loading the configuration file if provided.
	 * <p>
	 * If automatic execution is selected the HostControllers are started as the last step
	 * <p>
	 * This is called startSession in mctr_gui
	 */
	private void initialization() {
		configHandler = null;
		int tcpport = 0;
		String localAddress = null;

		if ((new File(configFilePath)).exists()) {
			configHandler = readConfigFile();

			Map<String, String> env = new HashMap<String, String>(System.getenv());
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

			if (configHandler == null) {
				ErrorReporter.parallelErrorDisplayInMessageDialog(
						"An error was found while processing the configuration file",
						"Please refer to the Error Log view for further information.");
				return;
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
				return;
			}

			tcpport = configHandler.getTcpPort();
			double killTimer = configHandler.getKillTimer();
			localAddress = configHandler.getLocalAddress();

			jnimw.set_kill_timer(killTimer);
			jnimw.destroy_host_groups();

			Map<String, String[]> groups = configHandler.getGroups();
			Map<String, String> components = configHandler.getComponents();

			for (Map.Entry<String, String[]> group : groups.entrySet()) {
				for (String hostName : group.getValue()) {
					jnimw.add_host(group.getKey(), hostName);
				}
			}

			for (Map.Entry<String, String> component : components.entrySet()) {
				jnimw.assign_component(component.getValue(), component.getKey());
			}
		}

		if (localAddress != null && !EMPTY_STRING.equals(localAddress) && 0 == tcpport) {
			addNotification(new Notification((new Formatter()).format(PADDEDDATETIMEFORMAT, new Date()).toString(), EMPTY_STRING, EMPTY_STRING,
					"If LocalAddress is specified you must also set the TCPPort in the configuration file: " + configFilePath));
			
			ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Error in the configuration",
					"If LocalAddress is specified you must also set the TCPPort in the configuration file: " + configFilePath);
			shutdownSession();
			return;
		}

		if (localAddress == null) {
			localAddress = "NULL";
		}
		mcHost = localAddress;
		int port = jnimw.start_session(localAddress, tcpport, (configHandler != null) && configHandler.unixDomainSocketEnabled());
		if (port == 0) {
			// there were some errors starting the session
			shutdownSession();
			return;
		}
		mcPort = EMPTY_STRING + port;

		if (configFileExecutionRequestCounter != -1 && configHandler != null) {
			for (int i = 0; i < configFileExecutionRequestCounter; i++) {
				executeList.addAll(configHandler.getExecuteElements());
			}
			configFileExecutionRequestCounter = -1;
		}

		if (startHCRequested) {
			startHC();
		}
	}

	/**
	 * Starts the HostControllers.
	 * <p>
	 * If the sessions initialization was not done, its done now.
	 * */
	private void startHC() {
		startHCRequested = true;
		int stateValue = jnimw.get_state().getValue();
		if (MC_LISTENING != stateValue && MC_LISTENING_CONFIGURED != stateValue) {
			initialization();
			return;
		}
		startHostControllers();
		startHCRequested = false;
	}

	/**
	 * Configures the HostControllers.
	 * <p>
	 * They are also started if no HostControler was connected.
	 * */
	private void configure() {
		configureRequested = true;
		int stateValue = jnimw.get_state().getValue();
		if (MC_HC_CONNECTED != stateValue && MC_ACTIVE != stateValue) {
			startHC();
			return;
		}

		jnimw.configure(generateCfgString());

		configureRequested = false;
	}

	/**
	 * Creates the MainTestComponent.
	 * <p>
	 * If the HostControllers are not configured that is also done before creating the MainTestComponent.
	 * */
	private void createMTC() {
		createMTCRequested = true;
		int stateValue = jnimw.get_state().getValue();
		if (MC_ACTIVE != stateValue) {
			configure();
			return;
		}
		/* this is not a constant null, it just so happens to trick your eyes */
		jnimw.create_mtc(0);
		createMTCRequested = false;
	}

	/**
	 * Initializes and displays a dialog to the user. If the user selected an executable element, it is also started here.
	 * <p>
	 * If the MainTestComponent is not yet created it is done before the execution starts.
	 *
	 * @param automaticExecution true if the execution should not be done in step-by-step.
	 * */
	private void startTest(final boolean automaticExecution) {
		boolean invalidSelection = false;
		do {
			if (automaticExecution && configFilePath != null && configFilePath.length() != 0 && !invalidSelection) {
				lastTimeSelection = "configuration file";
				lastTimeSelectionTime = 1;
				lastTimeSelectionType = ExecutableType.CONFIGURATIONFILE;
			} else {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						ExecuteDialog dialog = new ExecuteDialog(null);
						dialog.setControlparts(availableControlParts);
						dialog.setTestcases(availableTestcases);
						dialog.setTestsets(availableTestSetNames);
						if (configFilePath != null) {
							dialog.setConfigurationFile(configFilePath);
						}
						dialog.setSelection(lastTimeSelection, lastTimeSelectionTime, lastTimeSelectionType);

						if (dialog.open() != Window.OK) {
							executionStarted = false;
							shutdownSession();
							return;
						}

						lastTimeSelection = dialog.getSelectedElement();
						lastTimeSelectionTime = dialog.getSelectionTimes();
						lastTimeSelectionType = dialog.getSelectionType();
					}
				});
			}

			switch (lastTimeSelectionType) {
			case TESTCASE:
				invalidSelection = false;
				for (int i = 0; i < lastTimeSelectionTime; i++) {
					executeList.add(lastTimeSelection);
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime);
				break;
			case TESTSET:
				invalidSelection = false;
				for (int i = 0, size = availableTestSetNames.size(); i < size; i++) {
					if (availableTestSetNames.get(i).equals(lastTimeSelection)) {
						for (int j = 0; j < lastTimeSelectionTime; j++) {
							executeList.addAll(availableTestSetContents.get(i));
						}
						executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * availableTestSetContents.get(i).size());
					}
				}
				break;
			case CONTROLPART:
				invalidSelection = false;
				for (int i = 0; i < lastTimeSelectionTime; i++) {
					executeList.add(lastTimeSelection);
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime * 5);
				break;
			case CONFIGURATIONFILE:
				if (configHandler == null) {
					configFileExecutionRequestCounter = lastTimeSelectionTime;
					invalidSelection = false;
				} else {
					List<String> configurationFileElements = configHandler.getExecuteElements();
					if (configurationFileElements.isEmpty()) {
						invalidSelection = true;
						Display.getDefault().syncExec( new EmptyExecutionRunnable() );
					} else {
						invalidSelection = false;
						for (int i = 0; i < lastTimeSelectionTime; i++) {
							executeList.addAll(configurationFileElements);
						}
					}
				}
				executedTests.ensureCapacity(executedTests.size() + lastTimeSelectionTime);
				break;
			default:
				break;
			}
		} while (invalidSelection);

		executionStarted = true;
		executeRequested = true;
		if (MC_READY == jnimw.get_state().getValue()) {
			executeNextTest();
		} else {
			createMTC();
		}

		saveLastTimeUsageInfo();
	}

	/**
	 * Executes the next testcase.
	 * <p>
	 * Also creates the MainTestComponent if needed.
	 * */
	private void executeNextTest() {
		if (MC_READY != jnimw.get_state().getValue()) {
			createMTC();
			return;
		}
		String testElement = executeList.remove(0);

		int i = testElement.indexOf('.');
		if (i != -1) {
			if ("control".equals(testElement.substring(i + 1))) {
				jnimw.execute_control(testElement.substring(0, i));
			} else {
				jnimw.execute_testcase(testElement.substring(0, i), testElement.substring(i + 1));
			}
		} else {
			jnimw.execute_control(testElement);
		}
	}

	/**
	 * Stops the execution.
	 * */
	private void stop() {
		executeList.clear();
		executeRequested = false;
		jnimw.stop_execution();
	}

	/**
	 * Exits the MainTestComponent
	 * <p>
	 * Also stops the execution if it was not done yet.
	 * */
	private void exitMTC() {
		int stateValue = jnimw.get_state().getValue();
		if (MC_EXECUTING_CONTROL == stateValue || MC_EXECUTING_TESTCASE == stateValue || MC_PAUSED == stateValue) {
			stop();
			return;
		}
		if (MC_READY != stateValue) {
			return;
		}
		jnimw.exit_mtc();
	}

	/**
	 * Shuts down the session.
	 * <p>
	 * Also exits the MainTestComponent if not yet done
	 * */
	@Override
	protected void shutdownSession() {
		shutdownRequested = true;
		simpleExecutionRunning = false;
		int stateValue = jnimw.get_state().getValue();
		if (MC_LISTENING == stateValue || MC_LISTENING_CONFIGURED == stateValue || MC_HC_CONNECTED == stateValue || MC_ACTIVE == stateValue) {
			jnimw.shutdown_session();
			// jnimw.terminate_internal() must be also called when shutdown is finished, see statusChangeCallback() case MC_INACTIVE
			startHCRequested = false;
			configureRequested = false;
			createMTCRequested = false;
			executeRequested = false;
		} else {
			exitMTC();
		}

		super.shutdownSession();
	}

	/**
	 * Updates the information displayed about the MainController's and HostControllers actual states.
	 * */
	private void updateInfoDisplay() {
		JNIMiddleWare middleware = jnimw;
		McStateEnum mcState = middleware.get_state();
		MainControllerElement tempRoot = new MainControllerElement("Temporal root", this);
		String mcStateName = middleware.get_mc_state_name(mcState);
		tempRoot.setStateInfo(new InformationElement("state: " + mcStateName));

		HostControllerElement tempHost;
		ComponentStruct comp;
		QualifiedName qualifiedName;
		ComponentElement tempComponent;
		StringBuilder builder;

		int nofHosts = middleware.get_nof_hosts();
		HostStruct host;
		for (int i = 0; i < nofHosts; i++) {
			host = middleware.get_host_data(i);

			tempHost = new HostControllerElement("Host Controller: ");
			tempRoot.addHostController(tempHost);
			tempHost.setIPAddressInfo(new InformationElement("IP address: " + host.hostname));
			tempHost.setIPNumberInfo(new InformationElement("IP number: " + host.ip_addr));
			tempHost.setHostNameInfo(new InformationElement("Local host name:" + host.hostname_local));

			tempHost.setOperatingSystemInfo(new InformationElement(host.system_name + " " + host.system_release + " " + host.system_version));
			tempHost.setStateInfo(new InformationElement("State: " + middleware.get_hc_state_name(host.hc_state)));

			int activeComponents = host.n_active_components;

			int[] components = host.components.clone();
			middleware.release_data();
			for (int component_index = 0; component_index < activeComponents; component_index++) {
				comp = middleware.get_component_data(components[component_index]);
				tempComponent = new ComponentElement("Component: " + comp.comp_name, new InformationElement("Component reference: " + comp.comp_ref));
				tempHost.addComponent(tempComponent);

				qualifiedName = comp.comp_type;
				if (qualifiedName != null && qualifiedName.definition_name != null) {
					builder = new StringBuilder("Component type: ");
					if (qualifiedName.module_name != null) {
						builder.append(qualifiedName.module_name).append('.');
					}
					builder.append(qualifiedName.definition_name);
					tempComponent.setTypeInfo(new InformationElement(builder.toString()));
				}

				tempComponent.setStateInfo(new InformationElement(middleware.get_tc_state_name(comp.tc_state)));

				qualifiedName = comp.tc_fn_name;
				if (qualifiedName.definition_name != null) {
					builder = new StringBuilder(comp.comp_ref == 1 ? "test case" : "function");
					if (qualifiedName.module_name != null) {
						builder.append(qualifiedName.module_name).append('.');
					}
					builder.append(qualifiedName.definition_name);
					tempComponent.setExecutedInfo(new InformationElement(builder.toString()));
				}
				VerdictTypeEnum localVerdict = comp.local_verdict;
				if (localVerdict != null) {
					builder = new StringBuilder("local verdict: ");
					builder.append(localVerdict.getName());
				}
			}
		}
		middleware.release_data();

		if (mainControllerRoot != null) {
			mainControllerRoot.children().clear();
			mainControllerRoot.transferData(tempRoot);
		}

		if (Activator.getMainView() != null) {
			Activator.getMainView().refreshAll();
		}
	}

	/**
	 * This function changes the status of the user interface elements.
	 */
	private void updateGUI() {
		int stateValue = jnimw.get_state().getValue();

		automaticExecution.setEnabled(!isTerminated && executeList.isEmpty());
		startSession.setEnabled(!isTerminated && MC_INACTIVE == stateValue);
		configure.setEnabled(MC_LISTENING == stateValue || MC_LISTENING_CONFIGURED == stateValue || MC_HC_CONNECTED == stateValue
				|| MC_ACTIVE == stateValue);
		startHCs.setEnabled(MC_LISTENING == stateValue || MC_LISTENING_CONFIGURED == stateValue);
		cmtc.setEnabled(MC_LISTENING == stateValue || MC_LISTENING_CONFIGURED == stateValue || MC_HC_CONNECTED == stateValue
				|| MC_ACTIVE == stateValue);
		smtc.setEnabled(MC_READY == stateValue);
		cont.setEnabled(MC_PAUSED == stateValue);
		stop.setEnabled(MC_EXECUTING_CONTROL == stateValue || MC_EXECUTING_TESTCASE == stateValue || MC_PAUSED == stateValue
				|| MC_TERMINATING_TESTCASE == stateValue);
		emtc.setEnabled(MC_READY == stateValue);
		shutdownSession.setEnabled(MC_READY == stateValue || MC_LISTENING == stateValue || MC_LISTENING_CONFIGURED == stateValue
				|| MC_HC_CONNECTED == stateValue || MC_ACTIVE == stateValue);

		generalPause.setEnabled(MC_ACTIVE == stateValue || MC_READY == stateValue || MC_EXECUTING_CONTROL == stateValue
				|| MC_EXECUTING_TESTCASE == stateValue || MC_PAUSED == stateValue);
		generalPause.setChecked(jnimw.get_stop_after_testcase());
		generalLogging.setEnabled(MC_ACTIVE == stateValue || MC_READY == stateValue || MC_EXECUTING_CONTROL == stateValue
				|| MC_EXECUTING_TESTCASE == stateValue || MC_PAUSED == stateValue);
		info.setEnabled(MC_ACTIVE == stateValue || MC_READY == stateValue || MC_EXECUTING_CONTROL == stateValue
				|| MC_EXECUTING_TESTCASE == stateValue || MC_PAUSED == stateValue);

		if (Activator.getMainView() != null) {
			Activator.getMainView().refreshIfSelected(mainControllerRoot);
		}
	}

	@Override
	public boolean isTerminated() {
		return isTerminated;
	}

	@Override
	public IProcess getProcess() {
		return null;
	}

	@Override
	public void terminate(final boolean external) {
		McStateEnum state = jnimw.get_state();

		if (MC_INACTIVE == state.getValue()) {
			setRunning(false);
			isTerminated = true;
			if (mainControllerRoot != null) {
				mainControllerRoot.setTerminated();
				LaunchElement launchElement = null;
				for (Map.Entry<ILaunch, BaseExecutor> entry : ExecutorStorage.getExecutorMap().entrySet()) {
					if (entry.getValue().equals(mainControllerRoot.executor())
							&& LaunchStorage.getLaunchElementMap().containsKey(entry.getKey())) {
						launchElement = LaunchStorage.getLaunchElementMap().get(entry.getKey());
					}
				}
				if (launchElement != null) {
					launchElement.setTerminated();
				}
				if (Activator.getMainView() != null) {
					Activator.getMainView().refreshAll();
				}
			}
		} else {
			shutdownSession();
		}
		updateGUI();
	}

	@Override
	protected String getDefaultLogFileName() {
		return GeneralConstants.DEFAULT_LOGFILENAME_PARALLEL;
	}

	@Override
	protected String generateCfgString() {
		String result = super.generateCfgString();

		if (configHandler != null) {
			List<Integer> disallowedNodes = new ArrayList<Integer>();

			disallowedNodes.add(CfgLexer.MAIN_CONTROLLER_SECTION);
			disallowedNodes.add(CfgLexer.DEFINE_SECTION);
			disallowedNodes.add(CfgLexer.INCLUDE_SECTION);
			disallowedNodes.add(CfgLexer.COMPONENTS_SECTION);
			disallowedNodes.add(CfgLexer.GROUPS_SECTION);
			disallowedNodes.add(CfgLexer.EXECUTE_SECTION);

			result += configHandler.toStringResolved(disallowedNodes);
		}
		return result;
	}
}
