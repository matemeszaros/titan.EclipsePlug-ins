/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.TitanRadioGroupFieldEditor;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.ImportExportUtils;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class GeneralSettingsPrefPage extends LogViewerPreferenceRootPage {

	private StringFieldEditor systemUnderTestFieldEditor;
	private TitanRadioGroupFieldEditor defaultFormatFieldEditor;
	private TitanRadioGroupFieldEditor valueContentFieldEditor;
	private BooleanFieldEditor verboseFieldEditor;
	private BooleanFieldEditor addingComponentFieldEditor;
	private TitanRadioGroupFieldEditor mscViewOpenerFieldEditor;
	private TitanRadioGroupFieldEditor mscViewDefaultFieldEditor;
	private TitanRadioGroupFieldEditor testCaseTabDefaultFieldEditor;
	private TitanRadioGroupFieldEditor projectTabDefaultFieldEditor;

	/**
	 * Constructor
	 */
	public GeneralSettingsPrefPage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GeneralSettingsPrefPage.0")); //$NON-NLS-1$
	}

	@Override
	public void createFieldEditors() {

		new Label(getFieldEditorParent(), SWT.NONE);
		
		this.verboseFieldEditor = new BooleanFieldEditor(
				PreferenceConstants.PREF_VERBOSE_ID,
				Messages.getString("GeneralSettingsPrefPage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.verboseFieldEditor);

		new Label(getFieldEditorParent(), SWT.NONE);
		new Label(getFieldEditorParent(), SWT.NONE);

		this.systemUnderTestFieldEditor = new StringFieldEditor(PreferenceConstants.PREF_SUT_ID, 
				Messages.getString("GeneralSettingsPrefPage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		this.systemUnderTestFieldEditor.setTextLimit(Constants.MAX_COMP_NAME);

		addField(this.systemUnderTestFieldEditor);

		new Label(getFieldEditorParent(), SWT.NONE);
		this.defaultFormatFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT,
				Messages.getString("GeneralSettingsPrefPage.8"), //$NON-NLS-1$
				2,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.9"), PreferenceConstants.PREF_ASN1_TREEVIEW }, //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.10"), PreferenceConstants.PREF_ASN1_TEXTVIEW } //$NON-NLS-1$
				},
				getFieldEditorParent(),
				true);
		addField(this.defaultFormatFieldEditor);

		new Label(getFieldEditorParent(), SWT.NONE);
		this.valueContentFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID,
				Messages.getString("GeneralSettingsPrefPage.11"), //$NON-NLS-1$
				2,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.13"), PreferenceConstants.PREF_ADD_COMP_TO_VIS_ORDER_APPEND }, //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.12"), PreferenceConstants.PREF_ADD_COMP_TO_VIS_ORDER_REPLACE }  //$NON-NLS-1$
				},
				getFieldEditorParent(),
				true);
		addField(this.valueContentFieldEditor);

		new Label(getFieldEditorParent(), SWT.NONE);
		this.addingComponentFieldEditor = new BooleanFieldEditor(
				PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID,
				Messages.getString("GeneralSettingsPrefPage.14"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.addingComponentFieldEditor);
		
		new Label(getFieldEditorParent(), SWT.NONE);
		this.mscViewOpenerFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY,
				Messages.getString("GeneralSettingsPrefPage.15"), //$NON-NLS-1$
				3,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.16"), PreferenceConstants.PREF_MSCVIEW_TOP}, //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.17"), PreferenceConstants.PREF_MSCVIEW_BOTTOM}, //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.18"), PreferenceConstants.PREF_MSCVIEW_FIRST_SETVERDICT } //$NON-NLS-1$

				},
				
				getFieldEditorParent(),
				true);
		addField(this.mscViewOpenerFieldEditor);
		
		new Label(getFieldEditorParent(), SWT.NONE);
		this.projectTabDefaultFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_PROJECTTAB_DEFAULT,
				Messages.getString("GeneralSettingsPrefPage.28"),  //$NON-NLS-1$
				2,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.25"), PreferenceConstants.PREF_PROJECTTAB_DEFAULT_EXTRACT_TESTCASES},  //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.19"), PreferenceConstants.PREF_PROJECTTAB_DEFAULT_TEXT }  //$NON-NLS-1$

				},
				
				getFieldEditorParent(),
				true);
		addField(this.projectTabDefaultFieldEditor);
		
		new Label(getFieldEditorParent(), SWT.NONE);
		this.testCaseTabDefaultFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_TESTCASETAB_DEFAULT,
				Messages.getString("GeneralSettingsPrefPage.27"),  //$NON-NLS-1$
				2,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.22"), PreferenceConstants.PREF_TESTCASETAB_DEFAULT_OPEN_MSCVIEW},  //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.19"), PreferenceConstants.PREF_TESTCASETAB_DEFAULT_TEXT }  //$NON-NLS-1$

				},
				
				getFieldEditorParent(),
				true);
		addField(this.testCaseTabDefaultFieldEditor);
		
		new Label(getFieldEditorParent(), SWT.NONE);
		this.mscViewDefaultFieldEditor = new TitanRadioGroupFieldEditor(
				PreferenceConstants.PREF_MSCVIEW_DEFAULT,
				Messages.getString("GeneralSettingsPrefPage.26"), //$NON-NLS-1$
				2,
				new String[][] {
					{Messages.getString("GeneralSettingsPrefPage.24"), PreferenceConstants.PREF_MSCVIEW_DEFAULT_VALUEVIEW},  //$NON-NLS-1$
					{Messages.getString("GeneralSettingsPrefPage.19"), PreferenceConstants.PREF_MSCVIEW_DEFAULT_TEXT } //$NON-NLS-1$

				},
				
				getFieldEditorParent(),
				true);
		addField(this.mscViewDefaultFieldEditor);
		

	}
	
	@Override
	public boolean performOk() {
		String stringValue = this.systemUnderTestFieldEditor.getStringValue();
		if (stringValue != null) {
			// removes any leading and trailing white space 
			stringValue = stringValue.trim();
			this.systemUnderTestFieldEditor.setStringValue(stringValue);
		}	

		return super.performOk();		
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_GENERAL_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences() {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		String verbose = String.valueOf(this.verboseFieldEditor.getBooleanValue());
		String sut = this.systemUnderTestFieldEditor.getStringValue();
		String valueContent = this.defaultFormatFieldEditor.getSelectedLabelValue();
		String addingComponent = this.valueContentFieldEditor.getSelectedLabelValue();	
		String openProperty = String.valueOf(this.addingComponentFieldEditor.getBooleanValue());
		String openMSCView = this.mscViewOpenerFieldEditor.getSelectedLabelValue();
		
		String mscViewDefault = this.mscViewDefaultFieldEditor.getSelectedLabelValue();
		String testcaseTabDefault = this.testCaseTabDefaultFieldEditor.getSelectedLabelValue();
		String projectTabDefault = this.projectTabDefaultFieldEditor.getSelectedLabelValue();
		
		currentPrefs.put(PreferenceConstants.PREF_VERBOSE_ID, verbose);
		currentPrefs.put(PreferenceConstants.PREF_SUT_ID, sut);
		currentPrefs.put(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT, valueContent);
		currentPrefs.put(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID, addingComponent);
		currentPrefs.put(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID, openProperty);
		currentPrefs.put(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY, openMSCView);
		currentPrefs.put(PreferenceConstants.PREF_MSCVIEW_DEFAULT, mscViewDefault);
		currentPrefs.put(PreferenceConstants.PREF_PROJECTTAB_DEFAULT, projectTabDefault);
		currentPrefs.put(PreferenceConstants.PREF_TESTCASETAB_DEFAULT, testcaseTabDefault);
		return currentPrefs;
	}
	
	@Override
	protected void exportPreferences() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		String[] verbose = new String[] {String.valueOf(this.verboseFieldEditor.getBooleanValue())};
		String[] sut = new String[] {this.systemUnderTestFieldEditor.getStringValue()};
		String[] valueContent = new String[] {this.defaultFormatFieldEditor.getSelectedLabelValue()};
		String[] addingComponent = new String[] {this.valueContentFieldEditor.getSelectedLabelValue()};	
		String[] openProperty = new String[] {String.valueOf(this.addingComponentFieldEditor.getBooleanValue())};
		String[] openMSCView = new String[] {this.mscViewOpenerFieldEditor.getSelectedLabelValue()};	
		
		String[] mscViewDefault = new String[] {this.mscViewDefaultFieldEditor.getSelectedLabelValue()};
		String[] testcaseTabDefault = new String[] {this.testCaseTabDefaultFieldEditor.getSelectedLabelValue()};
		String[] projectTabDefault = new String[] {this.projectTabDefaultFieldEditor.getSelectedLabelValue()};
		
		currentPrefs.put(PreferenceConstants.PREF_VERBOSE_ID, verbose);
		currentPrefs.put(PreferenceConstants.PREF_SUT_ID, sut);
		currentPrefs.put(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT, valueContent);
		currentPrefs.put(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID, addingComponent);
		currentPrefs.put(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID, openProperty);
		currentPrefs.put(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY, openMSCView);
		currentPrefs.put(PreferenceConstants.PREF_MSCVIEW_DEFAULT, mscViewDefault);
		currentPrefs.put(PreferenceConstants.PREF_PROJECTTAB_DEFAULT, projectTabDefault);
		currentPrefs.put(PreferenceConstants.PREF_TESTCASETAB_DEFAULT, testcaseTabDefault);
		ImportExportUtils.exportSettings(getPageId(), currentPrefs, true);
	}

	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(getPageId());
		//if cancel
		if (prop == null) {
			return;
		}
		setOldPreferences(getCurrentPreferences());
		setProperties(prop);
		// Settings changed -> Enable apply button
		getApplyButton().setEnabled(true);
	}

	@Override
	protected void updatePage() {
		this.systemUnderTestFieldEditor.load();
		this.defaultFormatFieldEditor.load();
		this.valueContentFieldEditor.load();
		this.verboseFieldEditor.load();
		this.addingComponentFieldEditor.load();	
		this.mscViewDefaultFieldEditor.load();
		this.testCaseTabDefaultFieldEditor.load();
		this.projectTabDefaultFieldEditor.load();
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}
}
