/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

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
class NewTITANProjectImportOptionsPage extends WizardPage {
	private static final String TITLE = "TITAN Project importation options";

	private Button openPropertiesForAllImports;
	private boolean isOpenPropertiesForAllImports = false;
	private Button skipExistingProjects;
	private boolean isSkipExistingProjects = true;

	public NewTITANProjectImportOptionsPage() {
		super(TITLE);
	}

	@Override
	public String getDescription() {
		return "Finetune the ammount of data saved about the project";
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	public boolean isOpenPropertiesForAllImports() {
		return isOpenPropertiesForAllImports;
	}

	public boolean isSkipExistingProjects() {
		return isSkipExistingProjects;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		openPropertiesForAllImports = new Button(pageComposite, SWT.CHECK);
		openPropertiesForAllImports.setText("Open the preference page for all imported sub projects");
		openPropertiesForAllImports.setEnabled(true);
		openPropertiesForAllImports.setSelection(false);
		isOpenPropertiesForAllImports = false;
		openPropertiesForAllImports.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isOpenPropertiesForAllImports = openPropertiesForAllImports.getSelection();
			}
		});

		skipExistingProjects = new Button(pageComposite, SWT.CHECK);
		skipExistingProjects.setText("Skip existing projects on import");
		skipExistingProjects.setEnabled(true);
		skipExistingProjects.setSelection(true);
		isSkipExistingProjects = true;
		skipExistingProjects.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isSkipExistingProjects = skipExistingProjects.getSelection();
			}
		});

		setControl(pageComposite);
	}
}
