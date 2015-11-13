/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.titan.designer.preferences.pages.ComboFieldEditor;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * On this preference page settings of module import organization can be
 * changed.
 * 
 */
public final class OrganizeImportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences for organizing imports of a module";

	private static final String AUTOACTION_GROUP = "The organize imports action should:";
	private static final String ADD_IMPORTS = "Add the necessary module where a missing reference is found";
	private static final String REMOVE_IMPORTS = "Remove unused imports";

	private static final String SORT_GROUP = "Sorting of the imports";
	private static final String ENABLE_IMPORT_SORTING = "Enable sorting";

	public static final String COMMENT_THEM = "comment_them";
	public static final String JUST_CHANGE = "just_change";
	private static final String METHOD_GROUP = "Further subtleties";
	private static final String METHOD_OF_CHANGE = "Method of change";
	private static final String METHOD_OF_CHANGE_TOOLTIP = "Here you can set the change policy of the action.\n"
			+ "Comment: Unnecessary import are just commented out; added imports are annotated to be automatically generated.\n"
			+ "Simple: Unnecessary import are deleted; added imports are not distinguished";
	private static final String[][] METHOD_OPTIONS = new String[][] { { "Comment", COMMENT_THEM }, { "Simple", JUST_CHANGE } };

	private Composite pageComposite;

	private Group autoActionGroup;
	private Composite autoActionComposite;
	private BooleanFieldEditor addImports;
	private BooleanFieldEditor removeImports;

	private Group sortGroup;
	private Composite sortComposite;
	private BooleanFieldEditor enableImportSorting;

	private Group methodGroup;
	private Composite methodComposite;
	private ComboFieldEditor methodOption;

	public OrganizeImportPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected Control createContents(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		pageComposite.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		pageComposite.setLayoutData(gridData);

		autoActionGroup = new Group(pageComposite, SWT.NONE);
		autoActionGroup.setText(AUTOACTION_GROUP);
		layout = new GridLayout();
		layout.numColumns = 2;
		autoActionGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		autoActionGroup.setLayoutData(gridData);

		autoActionComposite = new Composite(autoActionGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		autoActionComposite.setLayoutData(gridData);

		addImports = new BooleanFieldEditor(PreferenceConstants.ORG_IMPORT_ADD, ADD_IMPORTS, autoActionComposite);
		addField(addImports);
		removeImports = new BooleanFieldEditor(PreferenceConstants.ORG_IMPORT_REMOVE, REMOVE_IMPORTS, autoActionComposite);
		addField(removeImports);

		sortGroup = new Group(pageComposite, SWT.NONE);
		sortGroup.setText(SORT_GROUP);
		layout = new GridLayout();
		layout.numColumns = 2;
		sortGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		sortGroup.setLayoutData(gridData);

		sortComposite = new Composite(sortGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		sortComposite.setLayoutData(gridData);

		enableImportSorting = new BooleanFieldEditor(PreferenceConstants.ORG_IMPORT_SORT, ENABLE_IMPORT_SORTING, sortComposite);
		addField(enableImportSorting);

		methodGroup = new Group(pageComposite, SWT.NONE);
		methodGroup.setText(METHOD_GROUP);
		layout = new GridLayout();
		layout.numColumns = 2;
		methodGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		methodGroup.setLayoutData(gridData);

		methodComposite = new Composite(methodGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.horizontalIndent = 3;
		methodComposite.setLayoutData(gridData);

		methodOption = new ComboFieldEditor(PreferenceConstants.ORG_IMPORT_METHOD, METHOD_OF_CHANGE, METHOD_OPTIONS, methodComposite);
		methodOption.getLabelControl(methodComposite).setToolTipText(METHOD_OF_CHANGE_TOOLTIP);
		addField(methodOption);

		initialize();

		return pageComposite;
	}

	@Override
	protected void createFieldEditors() {
		//Do nothing
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		pageComposite.dispose();
		autoActionComposite.dispose();
		autoActionGroup.dispose();
		addImports.dispose();
		removeImports.dispose();

		sortComposite.dispose();
		sortGroup.dispose();
		enableImportSorting.dispose();

		methodOption.dispose();
		methodComposite.dispose();
		methodGroup.dispose();

		super.dispose();
	}
}
