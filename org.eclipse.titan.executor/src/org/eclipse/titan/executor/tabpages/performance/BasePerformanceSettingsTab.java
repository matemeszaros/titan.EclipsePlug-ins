/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.performance;

import static org.eclipse.titan.executor.GeneralConstants.CONSOLELOGGING;
import static org.eclipse.titan.executor.GeneralConstants.KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES;
import static org.eclipse.titan.executor.GeneralConstants.MAXIMUMNOTIFICATIONLINECOUNT;
import static org.eclipse.titan.executor.GeneralConstants.MCSTATEREFRESHTIMEOUT;
import static org.eclipse.titan.executor.GeneralConstants.SEVERITYLEVELEXTRACTION;
import static org.eclipse.titan.executor.GeneralConstants.TESTCASEREFRESHONSTART;
import static org.eclipse.titan.executor.GeneralConstants.VERDICTEXTRACTION;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public abstract class BasePerformanceSettingsTab extends AbstractLaunchConfigurationTab {
	private static final String TITLE = "Basic performance settings";
	private static final String IMAGE = "progress_spinner.gif";

	protected static final String CONSOLE_LOGGING_TEXT = "Enable logging to the console";
	protected static final String CONSOLE_LOGGING_TOOLTIP = "If this option is turned off messages are not redirected to the console";
	protected static final String TESTCASE_REFRESH_TEXT = "Refresh the list of testcases on launch";
	protected static final String TESTCASE_REFRESH_TOOLTIP = "If this option is turned off the list of testcases will not be refreshed on launch";
	protected static final String SEVERITY_EXTRACTION_TEXT = "Enable severity level extraction";
	protected static final String SEVERITY_EXTRACTION_TOOLTIP =
			"If this option is turned off severity levels will not be displayed in the notifications";
	protected static final String LINECOUNT_TEXT = "Limit the amount of notifications in the Notifications view to:";
	protected static final String LINECOUNT_TOOLTIP = "0 means unlimited";
	protected static final String LINECOUNTSPINNER_TOOLTIP = "The maximum ammount of notifications in the Notifications view";
	protected static final String REFRESHTIMEOUT_TEXT = "State information shall be automatically refreshed in seconds:";
	protected static final String REFRESHTIMEOUT_TOOLTIP = "A high value might mean out of sync issues, while a low value can cause performane loss";
	protected static final String REFRESHTIMEOUTSPINNER_TOOLTIP = "The maximum ammount of time without knowing the state of the Main Controller";
	protected static final String VERDICTEXTRACTION_TEXT = "Enable verdict extaction from messages";
	protected static final String VERDICTEXTRACTION_TOOLTIP = "If this option is set verdicts are extracted to the Test Execution view";
	protected static final String KEEPCONFIG_TEXT = "Keep the temporary configuration files in single mode execution";
	protected static final String KEEPCONFIG_TOOLTIP = "In single mode execution, the executable is driven by temporarily generated configuration files";

	private final class BasicSelectorListener extends SelectionAdapter implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}

	}

	private BasicSelectorListener selectorListener;

	private Button consoleLogging;
	private Button testcaseRefreshOnStart;
	private Button severityLevelExtraction;
	private Label prefixLabel;
	private Spinner lineCountSpinner;
	private Spinner refreshTimeoutSpinner;
	private Button verdictExtraction;
	private Button keepConfigFile;

	public BasePerformanceSettingsTab() {
		selectorListener = new BasicSelectorListener();
	}

	public abstract void executorSpecificControls(final Composite pageComposite);

	@Override
	public final void createControl(final Composite parent) {
		final Composite pageComposite = new Composite(parent, SWT.NONE);
		final GridLayout pageCompositeLayout = new GridLayout();
		pageCompositeLayout.numColumns = 1;
		pageComposite.setLayout(pageCompositeLayout);
		final GridData pageCompositeGridData = new GridData();
		pageCompositeGridData.horizontalAlignment = GridData.FILL;
		pageCompositeGridData.grabExcessHorizontalSpace = true;
		pageCompositeGridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(pageCompositeGridData);

		createLineCountArea(pageComposite);
		createVerdictExtractionArea(pageComposite);
		createTestcaseRefreshArea(pageComposite);

		executorSpecificControls(pageComposite);

		setControl(pageComposite);
	}

	protected final void createConsoleLoggingArea(final Composite parent) {
		consoleLogging = new Button(parent, SWT.CHECK);
		consoleLogging.setText(CONSOLE_LOGGING_TEXT);
		consoleLogging.setToolTipText(CONSOLE_LOGGING_TOOLTIP);
		consoleLogging.setSelection(true);
		consoleLogging.addSelectionListener(selectorListener);
	}

	protected final void createTestcaseRefreshArea(final Composite parent) {
		testcaseRefreshOnStart = new Button(parent, SWT.CHECK);
		testcaseRefreshOnStart.setText(TESTCASE_REFRESH_TEXT);
		testcaseRefreshOnStart.setToolTipText(TESTCASE_REFRESH_TOOLTIP);
		testcaseRefreshOnStart.setSelection(true);
		testcaseRefreshOnStart.addSelectionListener(selectorListener);
	}

	protected final void createSeverityLevelExtractionArea(final Composite parent) {
		severityLevelExtraction = new Button(parent, SWT.CHECK);
		severityLevelExtraction.setText(SEVERITY_EXTRACTION_TEXT);
		severityLevelExtraction.setToolTipText(SEVERITY_EXTRACTION_TOOLTIP);
		severityLevelExtraction.setSelection(true);
		severityLevelExtraction.addSelectionListener(selectorListener);
	}

	protected final void createLineCountArea(final Composite parent) {
		Composite lineCountComposite = new Composite(parent, SWT.NONE);
		lineCountComposite.setLayout(new GridLayout(2, false));

		prefixLabel = new Label(lineCountComposite, SWT.NONE);
		prefixLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		prefixLabel.setText(LINECOUNT_TEXT);
		prefixLabel.setToolTipText(LINECOUNT_TOOLTIP);

		lineCountSpinner = new Spinner(lineCountComposite, SWT.NONE);
		lineCountSpinner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		lineCountSpinner.setMinimum(0);
		lineCountSpinner.setMaximum(1000000);
		lineCountSpinner.setIncrement(100);
		lineCountSpinner.setPageIncrement(10000);
		lineCountSpinner.setToolTipText(LINECOUNTSPINNER_TOOLTIP);
		lineCountSpinner.setSelection(1000);
		lineCountSpinner.addSelectionListener(selectorListener);
		lineCountSpinner.addModifyListener(selectorListener);
	}

	protected final void createRefreshTimeoutArea(final Composite parent) {
		Composite lineCountComposite = new Composite(parent, SWT.NONE);
		lineCountComposite.setLayout(new GridLayout(2, false));

		prefixLabel = new Label(lineCountComposite, SWT.NONE);
		prefixLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		prefixLabel.setText(REFRESHTIMEOUT_TEXT);
		prefixLabel.setToolTipText(REFRESHTIMEOUT_TOOLTIP);

		refreshTimeoutSpinner = new Spinner(lineCountComposite, SWT.NONE);
		refreshTimeoutSpinner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		refreshTimeoutSpinner.setMinimum(2);
		refreshTimeoutSpinner.setMaximum(1000);
		refreshTimeoutSpinner.setIncrement(1);
		refreshTimeoutSpinner.setPageIncrement(10);
		refreshTimeoutSpinner.setToolTipText(REFRESHTIMEOUT_TOOLTIP);
		refreshTimeoutSpinner.setSelection(5);
		refreshTimeoutSpinner.addSelectionListener(selectorListener);
		refreshTimeoutSpinner.addModifyListener(selectorListener);
	}

	protected final void createVerdictExtractionArea(final Composite parent) {
		verdictExtraction = new Button(parent, SWT.CHECK);
		verdictExtraction.setText(VERDICTEXTRACTION_TEXT);
		verdictExtraction.setToolTipText(VERDICTEXTRACTION_TOOLTIP);
		verdictExtraction.setSelection(true);
		verdictExtraction.addSelectionListener(selectorListener);
	}

	protected final void createKeepConfigfileArea(final Composite parent) {
		keepConfigFile = new Button(parent, SWT.CHECK);
		keepConfigFile.setText(KEEPCONFIG_TEXT);
		keepConfigFile.setToolTipText(KEEPCONFIG_TOOLTIP);
		keepConfigFile.setSelection(false);
		keepConfigFile.addSelectionListener(selectorListener);
	}

	@Override
	public final String getName() {
		return TITLE;
	}

	@Override
	public final Image getImage() {
		return ImageCache.getImage(IMAGE);
	}

	@Override
	public final void initializeFrom(final ILaunchConfiguration configuration) {
		try {
			boolean tempBool = configuration.getAttribute(CONSOLELOGGING, true);
			if (null != consoleLogging && consoleLogging.getSelection() != tempBool) {
				consoleLogging.setSelection(tempBool);
			}
			tempBool = configuration.getAttribute(TESTCASEREFRESHONSTART, true);
			if (null != testcaseRefreshOnStart && testcaseRefreshOnStart.getSelection() != tempBool) {
				testcaseRefreshOnStart.setSelection(tempBool);
			}
			tempBool = configuration.getAttribute(SEVERITYLEVELEXTRACTION, true);
			if (null != severityLevelExtraction && severityLevelExtraction.getSelection() != tempBool) {
				severityLevelExtraction.setSelection(tempBool);
			}
			int tempInt = configuration.getAttribute(MAXIMUMNOTIFICATIONLINECOUNT, 1000);
			if (null != lineCountSpinner && lineCountSpinner.getSelection() != tempInt) {
				lineCountSpinner.setSelection(tempInt);
			}
			tempInt = configuration.getAttribute(MCSTATEREFRESHTIMEOUT, 5);
			if (null != refreshTimeoutSpinner && refreshTimeoutSpinner.getSelection() != tempInt) {
				refreshTimeoutSpinner.setSelection(tempInt);
			}
			tempBool = configuration.getAttribute(VERDICTEXTRACTION, true);
			if (null != verdictExtraction && verdictExtraction.getSelection() != tempBool) {
				verdictExtraction.setSelection(tempBool);
			}
			tempBool = configuration.getAttribute(KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES, true);
			if (null != keepConfigFile && keepConfigFile.getSelection() != tempBool) {
				keepConfigFile.setSelection(tempBool);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	@Override
	public final void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(CONSOLELOGGING, (null != consoleLogging) ? consoleLogging.getSelection() : true);
		configuration.setAttribute(TESTCASEREFRESHONSTART, (testcaseRefreshOnStart != null) ? testcaseRefreshOnStart.getSelection() : true);
		configuration.setAttribute(SEVERITYLEVELEXTRACTION, (severityLevelExtraction != null) ? severityLevelExtraction.getSelection() : true);
		configuration.setAttribute(MAXIMUMNOTIFICATIONLINECOUNT, (lineCountSpinner != null) ? lineCountSpinner.getSelection() : 1000);
		configuration.setAttribute(MCSTATEREFRESHTIMEOUT, (refreshTimeoutSpinner != null) ? refreshTimeoutSpinner.getSelection() : 5);
		configuration.setAttribute(VERDICTEXTRACTION, (verdictExtraction != null) ? verdictExtraction.getSelection() : true);
		configuration.setAttribute(KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES, (keepConfigFile != null) ? keepConfigFile.getSelection() : true);
	}

	@Override
	public final void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(CONSOLELOGGING, true);
		configuration.setAttribute(TESTCASEREFRESHONSTART, true);
		configuration.setAttribute(SEVERITYLEVELEXTRACTION, true);
		configuration.setAttribute(MAXIMUMNOTIFICATIONLINECOUNT, 1000);
		configuration.setAttribute(MCSTATEREFRESHTIMEOUT, 5);
		configuration.setAttribute(VERDICTEXTRACTION, true);
		configuration.setAttribute(KEEPTEMPORARILYGENERATEDCONFIGURATIONFILES, true);
	}
}
