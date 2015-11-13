/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.pages.LogViewerPreferenceRootPage;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.ResourcePropertyHandler;

/**
 * Preferences handler, a singleton for handling TITAN Log Viewer 
 * related preferences 
 * 
 */
//FIXME should listen to preference changes
public final class PreferencesHandler implements IPropertyChangeListener {
	private static PreferencesHandler preferencesHandler;
	private IPreferenceStore preferenceStore;
	private String valueContentType;
	private String sutName;
	private boolean verbosePrintout;
	private boolean replaceCompVisOrder;
	private boolean openPropAfterExtComp;
	private Map<String, Integer> filteredComponents;
	private Map<String, Integer> filteredSignals;
	private Map<String, Integer> filteredFunctions;
	private List<String> componentVisualOrder;
	private Map<String, Boolean> filteredSilentEvents;
	private int mscViewOpen;
	private int projectTabDefault;
	private int testcaseTabDefault;
	private int mscViewDefault;
	
	private boolean setverdictError;
	private boolean setverdictFail;
	private boolean setverdictInconc;
	private boolean setverdictPass;
	private boolean setverdictNone;
	private List<String> errorCausedBy;
	private List<String> failCausedBy;
	private Map<String, RGB> coloringKeywords;
	private boolean useColoringKeywords;
	private boolean filteredConnectingPorts;
	private boolean filteredMappingPorts;
	
	
	
	private PreferencesHandler() {
		this.preferenceStore = Activator.getDefault().getPreferenceStore();
		// Initialize from preference store
		this.verbosePrintout = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_VERBOSE_ID));
		this.valueContentType = getValueContentType(this.preferenceStore.getString(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT));
		this.filteredComponents = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_COMPONENT_ID));
		this.filteredSignals = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_SIGNAL_ID));
		this.filteredFunctions = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_FUNCTION_ID));
		this.componentVisualOrder = stringListToArray(this.preferenceStore.getString(PreferenceConstants.PREF_COMPONENT_ORDER_ID));
		this.filteredSilentEvents = silentEventsStringToArray(this.preferenceStore.getString(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES));
		this.replaceCompVisOrder = replaceCompVisOrdWhenAdding(this.preferenceStore.getString(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID));
		this.openPropAfterExtComp = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID));
		this.mscViewOpen = getMSCViewOpenType(this.preferenceStore.getString(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY));
		this.projectTabDefault = getProjectTabDefault(this.preferenceStore.getString(PreferenceConstants.PREF_PROJECTTAB_DEFAULT));
		this.testcaseTabDefault = getTestcaseTabDefault(this.preferenceStore.getString(PreferenceConstants.PREF_PROJECTTAB_DEFAULT));
		this.mscViewDefault = getMSCViewDefault(this.preferenceStore.getString(PreferenceConstants.PREF_PROJECTTAB_DEFAULT));
 
		//Setverdict
		this.setverdictError = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_ERROR_ID));
		this.setverdictFail = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_FAIL_ID));
		this.setverdictInconc = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_INCONC_ID));
		this.setverdictNone = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_NONE_ID));
		this.setverdictPass = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_PASS_ID));
		this.componentVisualOrder = stringListToArray(this.preferenceStore.getString(PreferenceConstants.PREF_COMPONENT_ORDER_ID));
		this.componentVisualOrder = stringListToArray(this.preferenceStore.getString(PreferenceConstants.PREF_COMPONENT_ORDER_ID));

		this.coloringKeywords = getColors(this.preferenceStore.getString(PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID));
		this.useColoringKeywords = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_USE_HIGHLIGHT_ID));
		this.filteredConnectingPorts = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_CONNECTING_PORTS_ID));
		this.filteredMappingPorts = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_MAPPING_PORTS_ID));

	}
	

	/**
	 * Returns an instance of the PreferencesHandler
	 * @return the preferences handler instance 
	 */
	public static synchronized PreferencesHandler getInstance() {
		if (preferencesHandler == null) {
			preferencesHandler = new PreferencesHandler();
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(preferencesHandler);
		}
		return preferencesHandler;
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		String property = event.getProperty();
		if (!property.startsWith(Constants.PLUGIN_ID)) {
			return;
		}

		if (property.equals(PreferenceConstants.PREF_VERBOSE_ID)) {
			this.verbosePrintout = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_VERBOSE_ID));
		} else if (property.equals(PreferenceConstants.PREF_SUT_ID)) {
			this.sutName = this.preferenceStore.getString(PreferenceConstants.PREF_SUT_ID);
		} else if (property.equals(PreferenceConstants.PREF_FILTER_COMPONENT_ID)) {
			this.filteredComponents = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_COMPONENT_ID));
		} else if (property.equals(PreferenceConstants.PREF_FILTER_SIGNAL_ID)) {
			this.filteredSignals = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_SIGNAL_ID));
		} else if (property.equals(PreferenceConstants.PREF_FILTER_FUNCTION_ID)) {
			this.filteredFunctions = getFilters(this.preferenceStore.getString(PreferenceConstants.PREF_FILTER_FUNCTION_ID));
		} else if (property.equals(PreferenceConstants.PREF_COMPONENT_ORDER_ID)) {
			this.componentVisualOrder = stringListToArray(this.preferenceStore.getString(PreferenceConstants.PREF_COMPONENT_ORDER_ID));
		} else if (property.equals(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT)) {
			this.valueContentType = getValueContentType(this.preferenceStore.getString(PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT));
		} else if (property.equals(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES)) {
			this.filteredSilentEvents = silentEventsStringToArray(this.preferenceStore.getDefaultString(PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES));
		} else if (property.equals(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID)) {
			this.replaceCompVisOrder = replaceCompVisOrdWhenAdding(this.preferenceStore.getString(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID));
		} else if (property.equals(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID)) {
			this.openPropAfterExtComp = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID));
		} else if (property.equals(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY)) {
			this.mscViewOpen = getMSCViewOpenType(this.preferenceStore.getString(PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY));
		} else if (property.equals(PreferenceConstants.PREF_SETVERDICT_ERROR_ID)) {
			this.setverdictError = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_ERROR_ID));
		} else if (property.equals(PreferenceConstants.PREF_SETVERDICT_FAIL_ID)) {
			this.setverdictFail = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_FAIL_ID));
		} else if (property.equals(PreferenceConstants.PREF_SETVERDICT_INCONC_ID)) {
			this.setverdictInconc = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_INCONC_ID));
		} else if (property.equals(PreferenceConstants.PREF_SETVERDICT_NONE_ID)) {
			this.setverdictNone = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_NONE_ID));
		} else if (property.equals(PreferenceConstants.PREF_SETVERDICT_PASS_ID)) {
			this.setverdictPass = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_SETVERDICT_PASS_ID));
		} else if (property.equals(PreferenceConstants.PREF_USE_HIGHLIGHT_ID)) {
			this.useColoringKeywords = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_USE_HIGHLIGHT_ID));
		} else if (property.equals(PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID)) {
			this.coloringKeywords = getColors(this.preferenceStore.getString(PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID));
		} else if (property.equals(PreferenceConstants.PREF_CONNECTING_PORTS_ID)) {
			this.filteredConnectingPorts = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_CONNECTING_PORTS_ID));
		} else if (property.equals(PreferenceConstants.PREF_MAPPING_PORTS_ID)) {
			this.filteredMappingPorts = stringToBoolean(this.preferenceStore.getString(PreferenceConstants.PREF_MAPPING_PORTS_ID));
		}
	}
	
	/**
	 * Deregister the preference handler as a listener on the preference store 
	 */
	public void removeListener() {
		this.preferenceStore.removePropertyChangeListener(this);
	}
	
	/**
	 * Returns the current preferences
	 * @param project the current project
	 * @return the current preferences
	 */
	public PreferencesHolder getPreferences(final String project) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject iProject = workspaceRoot.getProject(project);
		if (iProject.exists()) {
			return getOverlayedPreferencesHolder(iProject);
		}

		return getPreferencesHolder();
	}
	
	/**
	 * Returns the last directory for dialog when adding a log file
	 * @return the last directory
	 */
	public String getLogFileLastDir() {
		return this.preferenceStore.getString(PreferenceConstants.PREF_ADD_LOG_FILE_LAST_DIR);
	}
	
	/**
	 * Sets a new "last directory" for dialog when adding a log file
	 * @param lastDir the new directory to set as last directory
	 */
	public void setLogFileLastDir(final String lastDir) {
		this.preferenceStore.setValue(PreferenceConstants.PREF_ADD_LOG_FILE_LAST_DIR, lastDir);
	}
	
	/**
	 * Returns the last directory for dialog when adding a log folder
	 * @return the last directory
	 */
	public String getLogFolderLastDir() {
		return this.preferenceStore.getString(PreferenceConstants.PREF_ADD_LOG_FOLDER_LAST_DIR);
	}
	
	/**
	 * Sets a new "last directory" for dialog when adding a log folder
	 * @param lastDir the new directory to set as last directory
	 */
	public void setLogFolderLastDir(final String lastDir) {
		this.preferenceStore.setValue(PreferenceConstants.PREF_ADD_LOG_FOLDER_LAST_DIR, lastDir);
	}
	
	/**
	 * Returns the last directory for dialog when importing preferences
	 * @return the last directory
	 */
	public String getImportLastDir() {
		return this.preferenceStore.getString(PreferenceConstants.PREF_IMPORT_LAST_DIR);
	}
	
	/**
	 * Sets a new "last directory" for import preferences dialog
	 * @param lastDir the new directory to set as last directory 
	 */
	public void setImportLastDir(final String lastDir) {
		this.preferenceStore.setValue(PreferenceConstants.PREF_IMPORT_LAST_DIR, lastDir);
	}
	
	/**
	 * Returns the last directory for dialog when exporting preferences
	 * @return the last directory
	 */
	public String getExportLastDir() {
		return this.preferenceStore.getString(PreferenceConstants.PREF_EXPORT_LAST_DIR);
	}
	
	/**
	 * Sets a new "last directory" for export preferences dialog
	 * @param lastDir the new directory to set as last directory 
	 */
	public void setExportLastDir(final String lastDir) {
		this.preferenceStore.setValue(PreferenceConstants.PREF_EXPORT_LAST_DIR, lastDir);
	}

	/**
	 * Returns the selected Value Content (tree or text)
	 * @return a string representing the selected value content type
	 */
	private String getValueContentType(final String valueContentType) {
		if (valueContentType.contentEquals(PreferenceConstants.PREF_ASN1_TEXTVIEW)) {
			return PreferenceConstants.ASN1_TEXTVIEW;
		}

		// -> Tree (which is also default)
		return PreferenceConstants.ASN1_TREEVIEW;
	}
	
	/**
	 * Returns the selected Value Content (tree or text)
	 * @return a string representing the selected value content type
	 */
	private int getMSCViewOpenType(final String mscViewOpen) {
		if (mscViewOpen.contentEquals(PreferenceConstants.PREF_MSCVIEW_FIRST_SETVERDICT)) {
			return PreferenceConstants.MSCVIEW_FIRST_VERDICT;
		} else if (mscViewOpen.contentEquals(PreferenceConstants.PREF_MSCVIEW_BOTTOM)) {
			return PreferenceConstants.MSCVIEW_BOTTOM;
		} else {
			// -> Top (which is also default)
			return PreferenceConstants.MSCVIEW_TOP;
		}
	}
	
	/**
	 * Returns true if replace is selected, otherwise false
	 * @return true if replace is selected, otherwise false
	 */
	private boolean replaceCompVisOrdWhenAdding(final String choice) {
		return choice.contentEquals(PreferenceConstants.PREF_ADD_COMP_TO_VIS_ORDER_REPLACE);

	}
	
	/**
	 * Returns a boolean value from the boolean string representation
	 * @param booleanString the boolean string ("true" / "false")
	 * @return true if the boolean value is true, otherwise false
	 */
	private boolean stringToBoolean(final String booleanString) {
		return booleanString.contentEquals(PreferenceConstants.TRUE);

	}
	
	/**
	 * Creates and returns an string array list using "path separator" as separator 
	 * @param stringList the string list (separated with "path separator")
	 * @return the new string array list (converted string list)
	 */
	private List<String> stringListToArray(final String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator);
        List<String> array = new ArrayList<String>();
        while (st.hasMoreElements()) {
            array.add((String) st.nextElement());
        }
        return array;
	}
	
	private PreferencesHolder getPreferencesHolder() {
		return new PreferencesHolder(
				 this.verbosePrintout, 
				 this.valueContentType,
				 this.sutName,
				 this.filteredComponents,
				 this.filteredSignals,
				 this.filteredFunctions,
				 this.componentVisualOrder,
				 this.filteredSilentEvents,
				 this.replaceCompVisOrder,
				 this.openPropAfterExtComp,
				 this.mscViewOpen,
				 this.projectTabDefault,
				 this.testcaseTabDefault,
				 this.mscViewDefault,
				 this.setverdictError,
				 this.setverdictFail,
				 this.setverdictInconc,
				 this.setverdictNone,
				 this.setverdictPass,
				 this.errorCausedBy,
				 this.failCausedBy,
				 this.useColoringKeywords,
				 this.coloringKeywords,
				 this.filteredConnectingPorts,
				 this.filteredMappingPorts);
	}
	
	private PreferencesHolder getOverlayedPreferencesHolder(final IResource resource) {
		// set filter for silent events
		return new PreferencesHolder(
			stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_VERBOSE_ID)), 
			getValueContentType(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_ASN1_DEFAULT_FORMAT)),
			getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_SUT_ID),
			getFilters(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_COMP_PAGE, PreferenceConstants.PREF_FILTER_COMPONENT_ID)),
			getFilters(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_SIGNALS_PAGE, PreferenceConstants.PREF_FILTER_SIGNAL_ID)),
			getFilters(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_FUNCTIONS_PAGE, PreferenceConstants.PREF_FILTER_FUNCTION_ID)),
			stringListToArray(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_COMP_VIS_ORDER_PAGE, PreferenceConstants.PREF_COMPONENT_ORDER_ID)),
			silentEventsStringToArray(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_SILENTEVENT_PAGE, PreferenceConstants.PREF_SILENT_EVENTS_CATEGORIES)),
			replaceCompVisOrdWhenAdding(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_REP_APP_ID)),
			stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_COMP_VISUAL_ORDER_ADD_OPEN_PROP_ID)),
			getMSCViewOpenType(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_OPEN_MSCVIEW_DISPLAY)),
			getProjectTabDefault(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_PROJECTTAB_DEFAULT)),
			getTestcaseTabDefault(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_TESTCASETAB_DEFAULT)),
			getMSCViewDefault(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_MSCVIEW_DEFAULT)),	
		    stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_SETVERDICT_ERROR_ID)),
		    stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_SETVERDICT_FAIL_ID)),
		    stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_SETVERDICT_INCONC_ID)),
		    stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_SETVERDICT_NONE_ID)),
		    stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_SETVERDICT_PASS_ID)),
			stringListToArray(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_ERROR_CAUSED_BY_ID)),
			stringListToArray(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_SETVERDICT_PAGE, PreferenceConstants.PREF_FAIL_CAUSED_BY_ID)),
			stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_USE_HIGHLIGHT_ID)), 
			getColors(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_GENERAL_PAGE, PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID)),
			stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_PORTS_PAGE, PreferenceConstants.PREF_CONNECTING_PORTS_ID)), 
			stringToBoolean(getOverlayedPreferenceValue(resource, PreferenceConstants.PAGE_ID_FILTERED_PORTS_PAGE, PreferenceConstants.PREF_MAPPING_PORTS_ID)));
	}
	
	private String getOverlayedPreferenceValue(final IResource resource, final String pageId, final String keyName) {
		IProject project = resource.getProject();
		String value = null;
		if (useProjectSettings(project, pageId)) {
			value = ResourcePropertyHandler.getProperty(resource, pageId, keyName);
		}
		if (value != null) {
			// property set on resource
			return value;
		}

		// no property set on resource, use preference store
		return this.preferenceStore.getString(keyName);
	}
	
	private boolean useProjectSettings(final IResource resource, final String pageId) {
		String usingProjectSetting = ResourcePropertyHandler.getProperty(resource, pageId, LogViewerPreferenceRootPage.USEPROJECTSETTINGS);
		return usingProjectSetting != null && "true".equals(usingProjectSetting);
	}
	
	/**
	 * Disposes the console writer 
	 */
	public synchronized void dispose() {
		try {
			preferencesHandler = null;
		} catch (Exception e) {
			// Do nothing
		}
	}
	
	private Map<String, Boolean> silentEventsStringToArray(final String prefValues) {
		String[] categories = prefValues.split(PreferenceConstants.PREFERENCE_DELIMITER);
		Map<String, Boolean> silentEvents = new HashMap<String, Boolean>();
		String[] currCategory;
		for (String category : categories) {
			currCategory = category.split(PreferenceConstants.SILENT_EVENTS_KEY_VALUE_DELIM);
			if (currCategory.length > 1) {
				silentEvents.put(currCategory[0], Boolean.valueOf(currCategory[1]));
			}
		}
		return silentEvents;
	}
	
	/**
	 * Returns the selected default behavior for the Project tab( extract test cases or text)
	 * @return a integer representing the selected value content type
	 */
	private int getProjectTabDefault(final String projectTabDefault) {
		if (projectTabDefault.contentEquals(PreferenceConstants.PREF_PROJECTTAB_DEFAULT_EXTRACT_TESTCASES)) {
			return PreferenceConstants.PROJECTTAB_DEFAULT_EXTRACT_TESTCASES;
		} else if (projectTabDefault.contentEquals(PreferenceConstants.PREF_PROJECTTAB_DEFAULT_TEXT)) {
			return PreferenceConstants.DEFAULT_TEXT;
		} else {
			// -> Extract Test cases (which is also default)
			return PreferenceConstants.PROJECTTAB_DEFAULT_EXTRACT_TESTCASES;
		}
	}
	
	/**
	 * Returns the selected default behavior for the test cases tab( open MSCView cases or text)
	 * @return a integer representing the selected value content type
	 */
	private int getTestcaseTabDefault(final String testcaseTabDefault) {
		if (testcaseTabDefault.contentEquals(PreferenceConstants.PREF_TESTCASETAB_DEFAULT_OPEN_MSCVIEW)) {
			return PreferenceConstants.TESTCASETAB_DEFAULT_OPEN_MSCVIEW;
		} else if (testcaseTabDefault.contentEquals(PreferenceConstants.PREF_TESTCASETAB_DEFAULT_TEXT)) {
			return PreferenceConstants.DEFAULT_TEXT;
		} else {
			// -> Open MSCView (which is also default)
			return PreferenceConstants.TESTCASETAB_DEFAULT_OPEN_MSCVIEW;
		}
	}
	/**
	 * Returns the selected default behavior for the mscView( valueView cases or text)
	 * @return a integer representing the selected value content type
	 */
	private int getMSCViewDefault(final String mscViewDefault) {
		if (mscViewDefault.contentEquals(PreferenceConstants.PREF_MSCVIEW_DEFAULT_VALUEVIEW)) {
			return PreferenceConstants.MSCVIEW_DEFAULT_VALUEVIEW;
		} else if (mscViewDefault.contentEquals(PreferenceConstants.PREF_MSCVIEW_DEFAULT_TEXT)) {
			return PreferenceConstants.DEFAULT_TEXT;
		} else {
			// -> Extract Test cases (which is also default)
			return PreferenceConstants.MSCVIEW_DEFAULT_VALUEVIEW;
		}
	}
	
	private Map<String, Integer> getFilters(final String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator);
        Map<String, Integer> filters = new HashMap<String, Integer>();
        while (st.hasMoreElements()) {
        	String filter = (String) st.nextElement();
        	if ((filter.length() >= 3) && filter.startsWith("*") && filter.endsWith("*")) { //$NON-NLS-1$ //$NON-NLS-2$
        		filters.put(filter.substring(1, filter.length() - 1), PreferenceConstants.FILTER_CONTAINS);
        	} else if ((filter.length() >= 2) && filter.startsWith("*")) { //$NON-NLS-1$
        		filters.put(filter.substring(1, filter.length()), PreferenceConstants.FILTER_END_WITH);
        	} else if ((filter.length() >= 2) && filter.endsWith("*")) { //$NON-NLS-1$
        		filters.put(filter.substring(0, filter.length() - 1), PreferenceConstants.FILTER_START_WITH);
        	} else {
        		filters.put(filter, PreferenceConstants.FILTER_EQUALS);
        	}
        }
        return filters;
	}
	
	private Map<String, RGB> getColors(final String stringList) {
		StringTokenizer stringColors = new StringTokenizer(stringList, File.pathSeparator);
		Map<String, RGB> keyWordColors = new HashMap<String, RGB>();
		String item = null;
		String color = null;
		RGB rgb = null;
		while (stringColors.hasMoreElements()) {
			String stringColor = (String) stringColors.nextElement();
			String[] sc = stringColor.split(PreferenceConstants.KEYWORD_COLOR_SEPARATOR);
			if (sc.length > 1) {
				item = sc[0];
				color = sc[1];
			}
			if ((color != null) && (color.trim().length() > 0)) {
				String[] splitColor = color.split(PreferenceConstants.RGB_COLOR_SEPARATOR);
				int red = Integer.parseInt(splitColor[0]);
				int green = Integer.parseInt(splitColor[1]);
				int blue = Integer.parseInt(splitColor[2]);
				rgb = new RGB(red, green, blue);
			}
			if ((item != null) && (rgb != null)) {
				keyWordColors.put(item, rgb);
			}
		}
		return keyWordColors;
	}
}
