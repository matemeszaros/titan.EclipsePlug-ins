/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kristof Szabados
 * */
class NewTITANProjectImportMainPage extends WizardPage {

	protected class BasicProjectSelectorListener implements ModifyListener, SelectionListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			Object source = e.getSource();
			if (source == projectFileText) {
				handleProjectFileModified();
			}
		}

		@Override
		public void widgetDefaultSelected(final SelectionEvent e) {
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			Object source = e.getSource();
			if (source == projectFileSelectionButton) {
				handleProjectFileButtonSelected();
			}
		}
	}

	private BasicProjectSelectorListener generalListener = new BasicProjectSelectorListener();
	private Composite pageComposite;
	private Text projectFileText;
	private Button projectFileSelectionButton;
	/**
	 * The project file name path, e.g  C:/MyFolder/MyProject.tpd or /MyFolder/MyProject.tpd
	 */
	private String projectFile;

	public NewTITANProjectImportMainPage(final String name) {
		super(name);
	}

	public String getProjectFile() {
		return projectFile;
	}

	@Override
	public void createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		createProjectFileEditor(pageComposite);

		setControl(pageComposite);
		setPageComplete(false);
	}

	protected void createProjectFileEditor(final Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText("Original project file:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		projectFileText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectFileText.setLayoutData(gd);
		projectFileText.setFont(font);
		projectFileText.addModifyListener(generalListener);
		projectFileSelectionButton = new Button(group, SWT.PUSH);
		projectFileSelectionButton.setText("Browse..");
		projectFileSelectionButton.setLayoutData(new GridData());
		projectFileSelectionButton.addSelectionListener(generalListener);
	}

	protected void handleProjectFileButtonSelected() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterPath(projectFileText.getText());
		dialog.setFilterExtensions(new String[] { "*.tpd", "*.Tpd", "*.TITAN_Project_Format" });
		String file = dialog.open();
		if (file != null && !file.equals(projectFileText.getText())) {
			projectFileText.setText(file);
		}
	}

	protected void handleProjectFileModified() {
		projectFile = projectFileText.getText();
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				checkProjectFile();
			}
		});
	}

	/**
	 * Check the validity of the file set as project file. And also extracts
	 * all data from the file if it is a correct project file.
	 * */
	private void checkProjectFile() {
		setPageComplete(projectFile != null && !"".equals(projectFile.trim()));
		// TODO implement checks here if needed
	}
}
