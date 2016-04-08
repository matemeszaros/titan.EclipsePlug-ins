 /******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados, Jeno Balasko
 * */
public abstract class NewModuleOptionsWizardPage extends WizardPage {

	private static final String GEN_EXCLUDED = "Generate as excluded from build";
	
	//Module generation
	private static final String GEN_WITH = "Generate module with this content:";
	private static final String GEN_EMPTY_CONT 	= "Empty module";
	private static final String GEN_MODULE_NAME = "Module name and empty body";
	private static final String GEN_MODULE_SKEL = "Module skeleton";
	private static final String GEN_MODULE_TOOLTIP = "What will be in the module";
	/**
	 * The items of the combo box.
	 */
	private static final String[] GEN_MODULE_OPTIONS = new String[] {
		GEN_EMPTY_CONT,
		GEN_MODULE_NAME,
		GEN_MODULE_SKEL
	};
		
	public static enum GeneratedModuleType { 
		EMPTY, NAME_AND_EMPTY_BODY, SKELETON
	}
	
	private static GeneratedModuleType[] GeneratedModuleTypeValues = GeneratedModuleType.values();
	
	private Composite pageComposite;

	private Button excludeFromBuildButton;
	private boolean isExcludedFromBuildSelected = false;
	
	private Label generateModuleLabel;
	
	private Combo generateModuleCombo;
	private GeneratedModuleType generatedModuleType = GeneratedModuleType.NAME_AND_EMPTY_BODY;
	
	public NewModuleOptionsWizardPage(final String title) {
		super(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
	 * .widgets.Composite)
	 */
	@Override
	public void createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);
	
		excludeFromBuildButton = new Button(pageComposite, SWT.CHECK);
		excludeFromBuildButton.setText(GEN_EXCLUDED);
		excludeFromBuildButton.setEnabled(true);
		excludeFromBuildButton.setSelection(false);
		isExcludedFromBuildSelected = false;
		excludeFromBuildButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludedFromBuildSelected = excludeFromBuildButton.getSelection();
			}
		});
		
		generateModuleLabel = new org.eclipse.swt.widgets.Label(pageComposite, SWT.READ_ONLY|SWT.LEFT);
		generateModuleLabel.setText(GEN_WITH);
		
		generateModuleCombo = new Combo(pageComposite,SWT.READ_ONLY|SWT.RIGHT);	
		generateModuleCombo.setEnabled(true);
		generateModuleCombo.setItems(GEN_MODULE_OPTIONS);
		generateModuleCombo.setText(GEN_MODULE_NAME);
		generateModuleCombo.setToolTipText(GEN_MODULE_TOOLTIP);
		generateModuleCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				setGeneratedModuleType(generateModuleCombo.getSelectionIndex());
				return;
			}
		});
		setControl(pageComposite);
	}

	public boolean isExcludeFromBuildSelected() {
		return isExcludedFromBuildSelected;
	}

	public GeneratedModuleType getGeneratedModuleType() {
		return generatedModuleType;
	}
	
	private void setGeneratedModuleType(final int index) {
		generatedModuleType = GeneratedModuleTypeValues[index];
	}

	@Override
	public void setWizard(final IWizard newWizard) {
		super.setWizard(newWizard);
	}
}
