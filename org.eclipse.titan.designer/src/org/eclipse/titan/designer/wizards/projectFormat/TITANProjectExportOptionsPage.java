/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
class TITANProjectExportOptionsPage extends WizardPage {
	private static final String TITLE = "TITAN Project exportation options";

	private Button excludeWorkingDirectoryContents;
	private boolean isExcludedWorkingDirectoryContents = true;
	private Button excludeDotResources;
	private boolean isExcludedDotResources = true;
	private Button excludeLinkedContents;
	private boolean isExcludeLinkedContents = false;
	private Button saveDefaultValues;
	private boolean isSaveDefaultValues = false;
	private Button packAllProjectsIntoOne;
	private boolean isPackAllProjectsIntoOne = false;
	private Button useTpdNameAttribute;
	private boolean isUseTpdNameAttribute = false;
	private IPreferencesService preferenceService = null;

	public TITANProjectExportOptionsPage(final boolean useTpdName) {
		super(TITLE);
		preferenceService = Platform.getPreferencesService();
		setExportPreferences();
		isUseTpdNameAttribute = useTpdName;
	}
	
	private void setExportPreferences(){
		isExcludedWorkingDirectoryContents =
				preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.EXPORT_EXCLUDE_WORKING_DIRECTORY_CONTENTS, true, null);
		isExcludedDotResources =
				preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.EXPORT_EXCLUDE_DOT_RESOURCES, true, null);
		isExcludeLinkedContents =
				preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.EXPORT_EXCLUDE_LINKED_CONTENTS, true, null);
		isSaveDefaultValues =
				preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.EXPORT_SAVE_DEFAULT_VALUES, true, null);
		isPackAllProjectsIntoOne = 
				preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.EXPORT_PACK_ALL_PROJECTS_INTO_ONE, true, null);
	}

	@Override
	public String getDescription() {
		return "Fine tune the amount of data saved about the project";
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	public boolean isExcludedWorkingDirectoryContents() {
		return isExcludedWorkingDirectoryContents;
	}

	public boolean isExcludedDotResources() {
		return isExcludedDotResources;
	}

	public boolean isExcludeLinkedContents() {
		return isExcludeLinkedContents;
	}

	public boolean isSaveDefaultValues() {
		return isSaveDefaultValues;
	}

	public boolean isPackAllProjectsIntoOne() {
		return isPackAllProjectsIntoOne;
	}
	
	public boolean isUseTpdNameAttribute() {
		return isUseTpdNameAttribute;
	}

	@Override
	public void createControl(final Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		pageComposite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		pageComposite.setLayoutData(data);

		excludeWorkingDirectoryContents = new Button(pageComposite, SWT.CHECK);
		excludeWorkingDirectoryContents.setText("Do not generate information on the contents of the working directory");
		excludeWorkingDirectoryContents.setEnabled(true);
		excludeWorkingDirectoryContents.setSelection(isExcludedWorkingDirectoryContents);
		excludeWorkingDirectoryContents.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludedWorkingDirectoryContents = excludeWorkingDirectoryContents.getSelection();
			}
		});

		excludeDotResources = new Button(pageComposite, SWT.CHECK);
		excludeDotResources.setText("Do not generate information on resources whose name starts with a `.'");
		excludeDotResources.setEnabled(true);
		excludeDotResources.setSelection(isExcludedDotResources);
		excludeDotResources.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludedDotResources = excludeDotResources.getSelection();
			}
		});

		excludeLinkedContents = new Button(pageComposite, SWT.CHECK);
		excludeLinkedContents.setText("Do not generate information on resources which are contained in a linked resource.");
		excludeLinkedContents.setEnabled(true);
		excludeLinkedContents.setSelection(isExcludeLinkedContents);
		excludeLinkedContents.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isExcludeLinkedContents = excludeLinkedContents.getSelection();
			}
		});

		saveDefaultValues = new Button(pageComposite, SWT.CHECK);
		saveDefaultValues.setText("Save default values");
		saveDefaultValues.setEnabled(true);
		saveDefaultValues.setSelection(isSaveDefaultValues);
		saveDefaultValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isSaveDefaultValues = saveDefaultValues.getSelection();
			}
		});

		packAllProjectsIntoOne = new Button(pageComposite, SWT.CHECK);
		packAllProjectsIntoOne.setText("Pack all data of related projects in this descriptor");
		packAllProjectsIntoOne.setEnabled(true);
		packAllProjectsIntoOne.setSelection(isPackAllProjectsIntoOne);
		packAllProjectsIntoOne.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isPackAllProjectsIntoOne = packAllProjectsIntoOne.getSelection();
			}
		});
		
		useTpdNameAttribute = new Button(pageComposite, SWT.CHECK);
		useTpdNameAttribute.setText("Export tpdName attribute to referenced projects");
		useTpdNameAttribute.setToolTipText("See Referred project usage with –I switch section in the TITAN Reference guide");
		useTpdNameAttribute.setEnabled(true);
		useTpdNameAttribute.setSelection(isUseTpdNameAttribute);
		useTpdNameAttribute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				isUseTpdNameAttribute = useTpdNameAttribute.getSelection();
			}
		});

		setControl(pageComposite);
	}

}
