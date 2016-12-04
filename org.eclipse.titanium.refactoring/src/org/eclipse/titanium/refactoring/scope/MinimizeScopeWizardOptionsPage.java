/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.scope;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page #1: modify the settings for the refactoring operation.
 * 
 * @author Viktor Varga
 */
import org.eclipse.titanium.refactoring.scope.MinimizeScopeRefactoring.Settings;

/**
 * 
 * @author Viktor Varga
 */
public class MinimizeScopeWizardOptionsPage extends UserInputWizardPage {

	private static final String LABEL_PAGECONTENT = "Modify refactoring settings:";
	
	private final Settings settings;

	public MinimizeScopeWizardOptionsPage(final String name, final Settings settings) {
		super(name);
		this.settings = settings;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		initializeDialogUnits(top);
		setControl(top);
		top.setLayout(new GridLayout());
		Label label = new Label(top, SWT.NONE);
		label.setText(LABEL_PAGECONTENT);
		Button chb_option1 = new Button(top, SWT.CHECK);
		chb_option1.setText("Move variable declarations");
		chb_option1.setSelection(settings.getSetting(Settings.MOVE_VARS));
		chb_option1.addSelectionListener(new CHBSelectionListener(Settings.MOVE_VARS));
		Button chb_option2 = new Button(top, SWT.CHECK);
		chb_option2.setText("Move variable declarations when their scope is correct");
		chb_option2.setSelection(settings.getSetting(Settings.MOVE_VARS_IN_CORRECT_SCOPE));
		chb_option2.addSelectionListener(new CHBSelectionListener(Settings.MOVE_VARS_IN_CORRECT_SCOPE));
		chb_option2.setEnabled(settings.getSetting(Settings.MOVE_VARS));
		chb_option1.addSelectionListener(new CHBSelectionListenerDisabler(chb_option2));
		Button chb_option3 = new Button(top, SWT.CHECK);
		chb_option3.setText("Remove unused variables");
		chb_option3.setSelection(settings.getSetting(Settings.REMOVE_UNUSED_VARS));
		chb_option3.addSelectionListener(new CHBSelectionListener(Settings.REMOVE_UNUSED_VARS));
		Button chb_option4 = new Button(top, SWT.CHECK);
		chb_option4.setText("Avoid refactoring variables with function calls in their declaration statements (disabling may alter the refactored code behaviour)");
		chb_option4.setSelection(settings.getSetting(Settings.AVOID_MOVING_WHEN_FUNCCALL));
		chb_option4.addSelectionListener(new CHBSelectionListener(Settings.AVOID_MOVING_WHEN_FUNCCALL));
		Button chb_option5 = new Button(top, SWT.CHECK);
		chb_option5.setText("Avoid moving variables with unchecked references in their declaration statements (disabling may alter the refactored code behaviour)");
		chb_option5.setSelection(settings.getSetting(Settings.AVOID_MOVING_WHEN_UNCHECKED_REF));
		chb_option5.addSelectionListener(new CHBSelectionListener(Settings.AVOID_MOVING_WHEN_UNCHECKED_REF));
		chb_option1.addSelectionListener(new CHBSelectionListenerDisabler(chb_option5));
		Button chb_option6 = new Button(top, SWT.CHECK);
		chb_option6.setText("Avoid moving and/or taking apart declaration lists (unused variables can still be removed from them)");
		chb_option6.setSelection(settings.getSetting(Settings.AVOID_MOVING_MULTIDECLARATIONS));
		chb_option6.addSelectionListener(new CHBSelectionListener(Settings.AVOID_MOVING_MULTIDECLARATIONS));
		chb_option1.addSelectionListener(new CHBSelectionListenerDisabler(chb_option6));

		setPageComplete(true);
		setErrorMessage(null);
	}
	
	/** 
	 * Modifies the {@link MinimizeScopeWizardOptionsPage#settings} field whenever
	 *   the specified check box is selected or deselected.
	 * */
	private class CHBSelectionListener implements SelectionListener {

		private final int setting;
		
		public CHBSelectionListener(final int setting) {
			this.setting = setting;
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (!(e.getSource() instanceof Button)) {
				return;
			}
			Button checkBox = (Button)e.getSource();
			settings.setSetting(setting, checkBox.getSelection());
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {

		}
		
	}

	/** 
	 * Disables the specified check box whenever the source check box is deselected and
	 *  enables it when the source is selected.
	 * */
	private class CHBSelectionListenerDisabler implements SelectionListener {

		private final Button toDisable;
		
		public CHBSelectionListenerDisabler(final Button toDisable) {
			this.toDisable = toDisable;
		}
		
		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (!(e.getSource() instanceof Button)) {
				return;
			}
			Button source = (Button)e.getSource();
			toDisable.setEnabled(source.getSelection());
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
			
		}
		
	}

	

}
