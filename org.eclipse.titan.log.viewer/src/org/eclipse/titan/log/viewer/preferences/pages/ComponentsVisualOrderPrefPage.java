/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.exceptions.UserException;
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
public class ComponentsVisualOrderPrefPage extends LogViewerPreferenceRootPage {
	
	private StringListEditor compVisOrderEditor;

	/**
	 * Constructor 
	 */
	public ComponentsVisualOrderPrefPage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("VisualComponentsOrderPrefPage.0")); //$NON-NLS-1$
	}
	
	@Override
	public void createFieldEditors() {
		this.compVisOrderEditor = new StringListEditor(PreferenceConstants.PREF_COMPONENT_ORDER_ID,
												  "", //$NON-NLS-1$
												  getFieldEditorParent(),
												  true);
		this.compVisOrderEditor.setPreferenceStore(getPreferenceStore());
		addField(this.compVisOrderEditor);
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences() {
		Map<String, String> currentPrefs = new HashMap<String, String>();
		String signalFilter = ""; //$NON-NLS-1$
		String[] elements = this.compVisOrderEditor.getElements();
		for (String element : elements) {
			signalFilter = signalFilter.concat(element + PreferenceConstants.PREFERENCE_DELIMITER);
		}
		currentPrefs.put(this.compVisOrderEditor.getPreferenceName(), signalFilter);
		return currentPrefs;
	}
	
	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String[]> getCurrentPreferencesSeparated() {
		Map<String, String[]> currentPrefs = new HashMap<String, String[]>();
		currentPrefs.put(this.compVisOrderEditor.getPreferenceName(), this.compVisOrderEditor.getElements());
		return currentPrefs;
	}
	
	@Override
	protected void exportPreferences() {
		ImportExportUtils.exportSettings(getPageId(), getCurrentPreferencesSeparated(), true);
	}

	@Override
	protected void importPreferences() {
		Map<String, String> prop = ImportExportUtils.importSettings(PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE);
		//if cancel
		if (prop == null) {
			return;
		}
		String propertyValues = prop.get(PreferenceConstants.PREF_COMPONENT_ORDER_ID);
		if (propertyValues == null) {
			return;
		}
		
		boolean sutFound = false;
		boolean mtcFound = false;
		String[] propertyValuesSeparated = propertyValues.split(File.pathSeparator);
		for (int i = 0; (i < propertyValuesSeparated.length) && !(sutFound && mtcFound); i++) {
			String currValue = propertyValuesSeparated[i];
			if (currValue.contentEquals(PreferenceConstants.SUT_DESCRIPTION)) {
				sutFound = true;
			} else if (currValue.contentEquals(PreferenceConstants.MTC_DESCRIPTION)) {
				mtcFound = true;
			}
		}
		
		// sut is missing (mandatory)	
		if (!sutFound) {
			TitanLogExceptionHandler.handleException(new UserException(Messages.getString("ComponentsVisualOrderPrefPage.0"))); //$NON-NLS-1$
		} else if (!mtcFound) {
			// mtc is missing (mandatory)
			TitanLogExceptionHandler.handleException(new UserException(Messages.getString("ComponentsVisualOrderPrefPage.1"))); //$NON-NLS-1$
		} else {
			setOldPreferences(getCurrentPreferences());
			setProperties(prop);
			// Settings changed -> Enable apply button
			getApplyButton().setEnabled(true);
		}
	}

	@Override
	protected void updatePage() {
		// this method is used so that order of the mandatory field (sut,mtc)
		// is not changed
		this.compVisOrderEditor.clear();
		this.compVisOrderEditor.load();
	}
	
	/**
	 * Clears the component visual order list 
	 */
	public void clearList() {
		this.compVisOrderEditor.clear();
		getApplyButton().setEnabled(true);
	}
	
	/**
	 * Adds a component to the component visual order list 
	 * @param componentName the name of the component
	 */
	public void addComponent(final String componentName) {
		this.compVisOrderEditor.addElementToList(componentName);
		getApplyButton().setEnabled(true);
	}
	
}
