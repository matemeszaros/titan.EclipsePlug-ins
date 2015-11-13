/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kristof Szabados
 * */
public final class NewConfigFileOptionsWizardPage extends WizardPage {

	private static final String TITLE = "Configuration file creation options";
	private static final String DESCRIPTION = "Create the new configuration file according to these options";
	private static final String GEN_SKELETON = "Generate with configuration file skeleton inserted";

	private Composite pageComposite;
	private Button generateSkeletonButton;
	private boolean isGenerateSkeletonSelected = true;

	public NewConfigFileOptionsWizardPage() {
		super(TITLE);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
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

		generateSkeletonButton = new Button(pageComposite, SWT.CHECK);
		generateSkeletonButton.setText(GEN_SKELETON);
		generateSkeletonButton.setEnabled(true);
		generateSkeletonButton.setSelection(true);
		isGenerateSkeletonSelected = true;
		generateSkeletonButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isGenerateSkeletonSelected = generateSkeletonButton.getSelection();
			}
		});

		setControl(pageComposite);
	}

	public boolean isGenerateSkeletonSelected() {
		return isGenerateSkeletonSelected;
	}
	
	@Override
	public void setWizard(final IWizard newWizard) {
		super.setWizard(newWizard);
	}
}
