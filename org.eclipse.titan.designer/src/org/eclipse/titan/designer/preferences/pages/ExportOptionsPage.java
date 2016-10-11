/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Project Exportation Option Page on Workspace Level
 * 
 * @author Jeno Balasko
 * 
 * */
public final class ExportOptionsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences of exporting project information into tpd file";

	private Composite pageComposite;

	private Group defaultExportOptionsGroup;
	private Composite exportationComposite;

	private BooleanFieldEditor excludeWorkingDirectoryContents; // true
	private BooleanFieldEditor excludeDotResources; // true
	private BooleanFieldEditor excludeLinkedContents; // false
	private BooleanFieldEditor saveDefaultValues; // false
	private BooleanFieldEditor packAllProjectsIntoOne; // false
	//
	private Composite automaticExportComposite;
	private Group automaticExportGroup;
	private BooleanFieldEditor automaticExport; // false
	private BooleanFieldEditor requestLocation; // false

	public ExportOptionsPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		pageComposite = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		pageComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(gridData);

		// ======== defaultExportOptionsGroup ====
		defaultExportOptionsGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		defaultExportOptionsGroup.setText("Fine tune the amount of data saved about the project");
		defaultExportOptionsGroup.setLayout(new GridLayout(2, false));
		defaultExportOptionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		exportationComposite = new Composite(defaultExportOptionsGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		exportationComposite.setLayoutData(gridData);

		excludeWorkingDirectoryContents = new BooleanFieldEditor(PreferenceConstants.EXPORT_EXCLUDE_WORKING_DIRECTORY_CONTENTS,
				"Do not generate information on the contents of the working directory", exportationComposite);
		addField(excludeWorkingDirectoryContents);

		excludeDotResources = new BooleanFieldEditor(PreferenceConstants.EXPORT_EXCLUDE_DOT_RESOURCES,
				"Do not generate information on resources whose name starts with a `.'", exportationComposite);
		addField(excludeDotResources);

		excludeLinkedContents = new BooleanFieldEditor(PreferenceConstants.EXPORT_EXCLUDE_LINKED_CONTENTS,
				"Do not generate information on resources which are contained in a linked resource.", exportationComposite);
		addField(excludeLinkedContents);

		saveDefaultValues = new BooleanFieldEditor(PreferenceConstants.EXPORT_SAVE_DEFAULT_VALUES, "Save default values",
				exportationComposite);
		addField(saveDefaultValues);

		packAllProjectsIntoOne = new BooleanFieldEditor(PreferenceConstants.EXPORT_PACK_ALL_PROJECTS_INTO_ONE,
				"Pack all data of related projects in this descriptor", exportationComposite);
		addField(packAllProjectsIntoOne);

		// ========== Automatic export=====
		automaticExportGroup = new Group(pageComposite, SWT.SHADOW_ETCHED_OUT);
		automaticExportGroup.setText("Automatic export");
		automaticExportGroup.setLayout(new GridLayout(2, false));
		automaticExportGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		automaticExportComposite = new Composite(automaticExportGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		automaticExportComposite.setLayoutData(gridData);

		automaticExport = new BooleanFieldEditor(PreferenceConstants.EXPORT_AUTOMATIC_EXPORT,
				"Refresh tpd file automatically on adding/deleting/renaming file/folder and on modifying project properties",
				automaticExportComposite);
		addField(automaticExport);

		requestLocation = new BooleanFieldEditor(PreferenceConstants.EXPORT_REQUEST_LOCATION,
				"Request new location for the tpds at the first automatic save.", automaticExportComposite);
		addField(requestLocation);

	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		automaticExport.dispose();
		defaultExportOptionsGroup.dispose();
		exportationComposite.dispose();
		excludeWorkingDirectoryContents.dispose();
		excludeDotResources.dispose();
		automaticExportComposite.dispose();
		automaticExportGroup.dispose();
		automaticExport.dispose();
		requestLocation.dispose();
		super.dispose();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ProductConstants.PRODUCT_ID_DESIGNER);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		return result;
	}
}
