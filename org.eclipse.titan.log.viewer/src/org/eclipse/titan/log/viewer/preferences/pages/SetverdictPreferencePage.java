/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.StringListEditor;
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
public class SetverdictPreferencePage extends LogViewerPreferenceRootPage {

	private StringListEditor errorCausedByEditor;
	private StringListEditor failCausedByEditor;
	private BooleanFieldEditor setverdictError;
	private BooleanFieldEditor setverdictFail;	
	private BooleanFieldEditor setverdictInconc;
	private BooleanFieldEditor setverdictPass;
	private BooleanFieldEditor setverdictNone;
	
	/**
	 * Constructor 
	 */
	public SetverdictPreferencePage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("SetverdictPreferencePage.0")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		
		this.setverdictError = new BooleanFieldEditor(
				PreferenceConstants.PREF_SETVERDICT_ERROR_ID,
				Messages.getString("SetverdictPreferencePage.1"), //$NON-NLS-1$
				getFieldEditorParent());		
		addField(this.setverdictError);
		this.setverdictFail = new BooleanFieldEditor(
				PreferenceConstants.PREF_SETVERDICT_FAIL_ID,
				Messages.getString("SetverdictPreferencePage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.setverdictFail);
		this.setverdictInconc = new BooleanFieldEditor(
				PreferenceConstants.PREF_SETVERDICT_INCONC_ID,
				Messages.getString("SetverdictPreferencePage.3"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.setverdictInconc);
		this.setverdictNone = new BooleanFieldEditor(
				PreferenceConstants.PREF_SETVERDICT_NONE_ID,
				Messages.getString("SetverdictPreferencePage.4"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.setverdictNone);
		this.setverdictPass = new BooleanFieldEditor(
				PreferenceConstants.PREF_SETVERDICT_PASS_ID,
				Messages.getString("SetverdictPreferencePage.5"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.setverdictPass);
		
		
		new Label(getFieldEditorParent(), SWT.NONE);
		this.errorCausedByEditor = new StringListEditor(PreferenceConstants.PREF_ERROR_CAUSED_BY_ID,
									  Messages.getString("SetverdictPreferencePage.6"), //$NON-NLS-1$
									  getFieldEditorParent(),
									  false);
		this.errorCausedByEditor.setPreferenceStore(getPreferenceStore());
		
		new Label(getFieldEditorParent(), SWT.NONE);
		addField(this.errorCausedByEditor);
		
		this.failCausedByEditor = new StringListEditor(PreferenceConstants.PREF_FAIL_CAUSED_BY_ID,
				 Messages.getString("SetverdictPreferencePage.7"), //$NON-NLS-1$
				  getFieldEditorParent(),
				  false);
		this.failCausedByEditor.setPreferenceStore(getPreferenceStore());
		addField(this.failCausedByEditor);
		
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_SETVERDICT_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences(final String separator) {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		String setverdictE = String.valueOf(this.setverdictError.getBooleanValue());
		String setverdictF = String.valueOf(this.setverdictFail.getBooleanValue());
		String setverdictI = String.valueOf(this.setverdictInconc.getBooleanValue());
		String setverdictN = String.valueOf(this.setverdictNone.getBooleanValue());
		String setverdictP = String.valueOf(this.setverdictPass.getBooleanValue());

		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_ERROR_ID, setverdictE);
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_FAIL_ID, setverdictF);
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_INCONC_ID, setverdictI);
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_NONE_ID, setverdictN);
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_PASS_ID, setverdictP);
		
		String keywords = ""; //$NON-NLS-1$
		String[] elements = this.errorCausedByEditor.getElements();
		for (int i = 0; i < elements.length; i++) {
			keywords = keywords.concat(elements[i] + separator);
		}
		currentPrefs.put(PreferenceConstants.PREF_ERROR_CAUSED_BY_ID, keywords);
		
		elements = this.failCausedByEditor.getElements();
		for (int i = 0; i < elements.length; i++) {
			keywords = keywords.concat(elements[i] + separator);
		}
		currentPrefs.put(PreferenceConstants.PREF_FAIL_CAUSED_BY_ID, keywords);
		return currentPrefs;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences 
	 */
	private Map<String, String[]> getCurrentPreferencesSeparated() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		
		String[] setverdictErrors = new String[] {String.valueOf(this.setverdictError.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_ERROR_ID, setverdictErrors);
		String[] setverdictFails = new String[] {String.valueOf(this.setverdictFail.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_FAIL_ID, setverdictFails);
		String[] setverdictInconcs = new String[] {String.valueOf(this.setverdictInconc.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_INCONC_ID, setverdictInconcs);
		String[] setverdictNones = new String[] {String.valueOf(this.setverdictNone.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_NONE_ID, setverdictNones);
		String[] setverdictPasses = new String[] {String.valueOf(this.setverdictPass.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_SETVERDICT_PASS_ID, setverdictPasses);

		currentPrefs.put(this.errorCausedByEditor.getPreferenceName(), this.errorCausedByEditor.getElements());
		currentPrefs.put(this.failCausedByEditor.getPreferenceName(), this.failCausedByEditor.getElements());
		return currentPrefs;
	}

	@Override
	protected void exportPreferences() {	
		ImportExportUtils.exportSettings(getPageId(), getCurrentPreferencesSeparated(), true);
	}

	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(PreferenceConstants.PAGE_ID_SETVERDICT_PAGE);
		//if cancel
		if (prop == null) {
			return;
		}
		setOldPreferences(getCurrentPreferences(File.pathSeparator));
		setProperties(prop);
		// Settings changed -> Enable apply button
		getApplyButton().setEnabled(true);
	}

	@Override
	protected void updatePage() {
	
		this.setverdictError.load();
		this.setverdictFail.load();
		this.setverdictInconc.load();
		this.setverdictNone.load();
		this.setverdictPass.load();
		
		this.errorCausedByEditor.clear();
		this.errorCausedByEditor.load();
		
		this.failCausedByEditor.clear();
		this.failCausedByEditor.load();
	}
	
}
