/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.Activator;
import org.eclipse.titan.executor.properties.FieldEditorPropertyPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Szabolcs Beres
 * */
public final class ExecutorPreferencePage extends FieldEditorPropertyPage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor setLogFolder;
	private StringFieldEditor logFolderPath;
	private BooleanFieldEditor deleteLogFiles;
	private BooleanFieldEditor automaticMerge;
	
	public ExecutorPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		super.initialize();
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		setLogFolder = new BooleanFieldEditor(PreferenceConstants.SET_LOG_FOLDER, PreferenceConstants.SET_LOG_FOLDER_LABEL, parent);
		addField(setLogFolder);
		
		logFolderPath = new StringFieldEditor(
				PreferenceConstants.LOG_FOLDER_PATH_NAME, PreferenceConstants.LOG_FOLDER_PATH_LABEL, StringFieldEditor.UNLIMITED, parent);
		addField(logFolderPath);

		deleteLogFiles = new BooleanFieldEditor(PreferenceConstants.DELETE_LOG_FILES_NAME, PreferenceConstants.DELETE_LOG_FILES_LABEL, parent);
		addField(deleteLogFiles);

		automaticMerge = new BooleanFieldEditor(PreferenceConstants.AUTOMATIC_MERGE_NAME, PreferenceConstants.AUTOMATIC_MERGE_LABEL, parent);
		addField(automaticMerge);
	}

	@Override
	protected void updateFieldEditors() {
		final Composite fieldEditorParent = getFieldEditorParent();
		
		if (isPropertyPage() && !isProjectSettingsSelected()) {
			for (FieldEditor editor : getFieldEditors()) {
				editor.setEnabled(false, fieldEditorParent);
			}
			return;
		}
		
		setLogFolder.setEnabled(true, fieldEditorParent);
		boolean setLogFolderValue = setLogFolder.getBooleanValue();
		logFolderPath.setEnabled(setLogFolderValue, fieldEditorParent);
		deleteLogFiles.setEnabled(setLogFolderValue, fieldEditorParent);
		automaticMerge.setEnabled(setLogFolderValue, fieldEditorParent);
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		
		IEclipsePreferences node = (IEclipsePreferences) Platform.getPreferencesService().getRootNode()
				.node(InstanceScope.SCOPE).node(org.eclipse.titan.executor.Activator.PLUGIN_ID);
		if (node != null) {
			try {
				node.flush();
			} catch (Exception e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
		}

		return result;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);
		
		if (event.getSource() == setLogFolder) {
			updateFieldEditors();
		}
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.EXECUTOR_PREFERENCE_PAGE_ID;
	}

	@Override
	public IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
