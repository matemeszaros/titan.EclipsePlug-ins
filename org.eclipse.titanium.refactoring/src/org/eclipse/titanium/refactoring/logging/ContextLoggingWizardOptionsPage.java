/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.logging;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titanium.refactoring.logging.ContextLoggingRefactoring.Settings;

/**
 * Wizard page #1: modify the settings for the refactoring operation.
 * 
 * @author Viktor Varga
 */
public class ContextLoggingWizardOptionsPage extends UserInputWizardPage {

	private static final String LABEL_PAGECONTENT = "Modify refactoring settings:";
	
	private final Settings settings;
	
	public ContextLoggingWizardOptionsPage(final String name, final Settings settings) {
		super(name);
		this.settings = settings;
	}

	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		initializeDialogUnits(top);
		setControl(top);
		top.setLayout(new GridLayout());
		Label label = new Label(top, SWT.NONE);
		label.setText(LABEL_PAGECONTENT);
		Button chb_option1 = new Button(top, SWT.CHECK);
		chb_option1.setText("Log function parameters");
		chb_option1.setSelection(settings.getSetting(Settings.SETTING_LOG_FUNCPAR));
		chb_option1.addSelectionListener(new CHBSelectionListener(Settings.SETTING_LOG_FUNCPAR));
		Button chb_option2 = new Button(top, SWT.CHECK);
		chb_option2.setText("Log variables in if conditions");
		chb_option2.setSelection(settings.getSetting(Settings.SETTING_LOG_IF));
		chb_option2.addSelectionListener(new CHBSelectionListener(Settings.SETTING_LOG_IF));
		Button chb_option3 = new Button(top, SWT.CHECK);
		chb_option3.setText("Log local variables before log statement");
		chb_option3.setSelection(settings.getSetting(Settings.SETTING_LOG_LOCAL_VARS));
		chb_option3.addSelectionListener(new CHBSelectionListener(Settings.SETTING_LOG_LOCAL_VARS));
		Button chb_option4 = new Button(top, SWT.CHECK);
		chb_option4.setText("Only log local variables in the parent block of the log statement");
		chb_option4.setSelection(settings.getSetting(Settings.SETTING_LOG_LOCAL_VARS_PARENT_BLOCK_ONLY));
		chb_option4.addSelectionListener(new CHBSelectionListener(Settings.SETTING_LOG_LOCAL_VARS_PARENT_BLOCK_ONLY));
		chb_option4.setEnabled(settings.getSetting(Settings.SETTING_LOG_LOCAL_VARS));
		chb_option3.addSelectionListener(new CHBSelectionListenerDisabler(chb_option4));
		Button chb_option5 = new Button(top, SWT.CHECK);
		chb_option5.setText("Log loop variables");
		chb_option5.setSelection(settings.getSetting(Settings.SETTING_LOG_LOOP));
		chb_option5.addSelectionListener(new CHBSelectionListener(Settings.SETTING_LOG_LOOP));
		Button chb_option6 = new Button(top, SWT.CHECK);
		chb_option6.setText("Modify log statements which already log variables");
		chb_option6.setSelection(settings.getSetting(Settings.SETTING_MODIFY_LOG_STATEMENTS));
		chb_option6.addSelectionListener(new CHBSelectionListener(Settings.SETTING_MODIFY_LOG_STATEMENTS));


		setPageComplete(true);
		setErrorMessage(null);
	}
	
	/** 
	 * Modifies the {@link ContextLoggingWizardOptionsPage#settings} field whenever
	 *   the specified check box is selected or deselected.
	 * */
	private class CHBSelectionListener implements SelectionListener {

		private final int setting;
		
		public CHBSelectionListener(int setting) {
			this.setting = setting;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!(e.getSource() instanceof Button)) {
				return;
			}
			Button checkBox = (Button)e.getSource();
			settings.setSetting(setting, checkBox.getSelection());
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}
		
	}

	/** 
	 * Disables the specified check box whenever the source check box is deselected and
	 *  enables it when the source is selected.
	 * */
	private class CHBSelectionListenerDisabler implements SelectionListener {

		private final Button toDisable;
		
		public CHBSelectionListenerDisabler(Button toDisable) {
			this.toDisable = toDisable;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!(e.getSource() instanceof Button)) {
				return;
			}
			Button source = (Button)e.getSource();
			toDisable.setEnabled(source.getSelection());
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			
		}
		
	}

}
