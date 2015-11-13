/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.common.fieldeditors.TITANResourceLocator;

/**
 * @author Kristof Szabados
 * */
public final class NewTITANProjectOptionsWizardPage extends WizardPage {
	private static final String SOURCEDIR_TOOLTIP = "The folder in which you want to save the sources of this project";
	private static final String WORKINGDIR_TOOLTIP = "The folder in which you want all build operations to run.\n"
			+ "Please note, that all files in the working directory are assumed to be generated, and will not be analyzed on-the-fly.";

	private final class BasicProjectSelectorListener implements ModifyListener {
		@Override
		public void modifyText(final ModifyEvent e) {
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	}

	private static final String TITLE = "TITAN settings";
	private static final String DESCRIPTION = "Define the main TITAN folders";
	// problems
	private static final String WORKINGDIR_EMPTY = "The name of the working directory folder must be filled out";
	private static final String SOURCEWORKSAME = "The source and the working directory can not be the same";

	private final BasicProjectSelectorListener generalListener;
	private Composite pageComposite;

	private Text sourceText;
	private Button excludeFromBuildButton;
	private boolean isExcludedFromBuildSelected = false;
	private TITANResourceLocator workingDirectory;

	private String sourceFolder = "src";
	private String workingFolder = "bin";

	public NewTITANProjectOptionsWizardPage() {
		super(TITLE);
		generalListener = new BasicProjectSelectorListener();
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
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		pageComposite.dispose();
		super.dispose();
	}

	/** @return the source folder set on this page */
	public String getSourceFolder() {
		return sourceFolder;
	}

	/**
	 * @return true if the source folder should be generated as excluded from
	 *         build.
	 */
	public boolean isExcludeFromBuildSelected() {
		return isExcludedFromBuildSelected;
	}

	/** @return the working directory set on this page */
	public String getWorkingFolder() {
		return workingFolder;
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

		Label sourceLabel = new Label(pageComposite, SWT.NONE);
		sourceLabel.setText("source folder:");
		sourceLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		sourceLabel.setToolTipText(SOURCEDIR_TOOLTIP);
		sourceText = new Text(pageComposite, SWT.SINGLE | SWT.BORDER);
		sourceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceText.setText(sourceFolder);
		sourceText.setToolTipText(SOURCEDIR_TOOLTIP);
		sourceText.addModifyListener(generalListener);

		excludeFromBuildButton = new Button(pageComposite, SWT.CHECK);
		excludeFromBuildButton.setText("The source folder should be generated as excluded from build");
		excludeFromBuildButton
				.setToolTipText("Useful if the set up of the project takes a long time, and so the project should not be analyzed by default.");
		excludeFromBuildButton.setEnabled(true);
		excludeFromBuildButton.setSelection(false);
		isExcludedFromBuildSelected = false;
		excludeFromBuildButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludedFromBuildSelected = excludeFromBuildButton.getSelection();
			}
		});

		Composite workingDirComposite = new Composite(pageComposite, SWT.NONE);
		layout = new GridLayout();
		workingDirComposite.setLayout(layout);
		data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		workingDirComposite.setLayoutData(data);

		IPath projectPath = ((NewTITANProjectWizard) getWizard()).getProjectPath();
		workingDirectory = new TITANResourceLocator("working directory:", workingDirComposite, IResource.FOLDER, projectPath.toOSString());
		workingDirectory.setStringValue("bin");
		workingDirectory.getTextControl(workingDirComposite).setToolTipText(WORKINGDIR_TOOLTIP);
		workingDirectory.addModifyListener(generalListener);

		setPageComplete(validatePage());
		setErrorMessage(null);
		setControl(pageComposite);
	}

	@Override
	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		IPath location = ((NewTITANProjectWizard) getWizard()).getProjectPath();
		if (location == null) {
			return;
		}

		IPath projectPath = ((NewTITANProjectWizard) getWizard()).getProjectPath();
		workingDirectory.setRootPath(projectPath.toOSString());
	}

	@Override
	public boolean isPageComplete() {
		if (!validatePage()) {
			return false;
		}

		return super.isPageComplete();
	}

	private boolean validatePage() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String sourceName = sourceText.getText();
		if (!"".equals(sourceName)) {
			IStatus nameStatus = workspace.validateName(sourceName, IResource.FOLDER);
			if (!nameStatus.isOK()) {
				setErrorMessage(nameStatus.getMessage());
				return false;
			}
		}

		String workName = workingDirectory.getStringValue();
		if ("".equals(workName)) {
			setErrorMessage(WORKINGDIR_EMPTY);
			return false;
		}

		if (sourceName.equals(workName)) {
			setErrorMessage(SOURCEWORKSAME);
			return false;
		}

		this.sourceFolder = sourceName;
		this.workingFolder = workName;

		setErrorMessage(null);
		return true;
	}
}
