/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

/**
 * This class is a holder for the TITAN Log Viewer preferences
 *
 */
public class PreferencesHolder {

	private boolean verbosePrintouts;
	private String selectedValueContentType;
	private String sutName;
	private Map<String, Integer> ignoredComponents;
	private Map<String, Integer> ignoredSignals;
	private Map<String, Integer> ignoredFunctions;
	private List<String> visualOrderComponents;
	private Map<String, Boolean> filteredSilentEvents;
	private boolean replaceCompVisOrder;
	private boolean openPropAfterCompExt;
	private int mscViewOpen;
	private int projectTabDefault;
	private int testcaseTabDefault;
	private int mscViewDefault;
	private boolean setverdictError;
	private boolean setverdictFail;
	private boolean setverdictInconc;
	private boolean setverdictNone;
	private boolean setverdictPass;
	private List<String> errorCausedBy;
	private List<String> failCausedBy;
	private Map<String, RGB> coloringKeywords;
	private boolean useColoringKeywords;
	private boolean filteredConnectingPorts;
	private boolean filteredMappingPorts;
	/**
	 * Constructor
	 * 
	 * @param verbosePrintouts a flag indicating if verbose printouts should be used
	 * @param selectedValueContentType the way values should be displayed (as tree or as text)
	 * @param sutName the name of SUT
	 * @param ignoredComponents a list of ignored components
	 * @param ignoredSignals a list of ignored signals
	 * @param visualOrderComponents a list of the preferred visual order of components
	 * @param replaceCompVisOrder a flag indicating if the components visual order should be replaced with extracted components
	 * @param openPropAfterCompExt a flag indicating if the project properties should be opened after extracting components
	 */
	public PreferencesHolder(final boolean verbosePrintouts,
			final String selectedValueContentType,
			final String sutName,
			final Map<String, Integer> ignoredComponents,
			final Map<String, Integer> ignoredSignals,
			final Map<String, Integer> ignoredFunctions,
			final List<String> visualOrderComponents,
			final Map<String, Boolean> filteredSilentEvents,
			final boolean replaceCompVisOrder,
			final boolean openPropAfterCompExt,
			final int mscViewOpen,
			final int projectTabDefault,
			final int testcaseTabDefault,
			final int mscViewDefault,
			final boolean setverdictError,
			final boolean setverdictFail,
			final boolean setverdictInconc,
			final boolean setverdictNone,
			final boolean setverdictPass,
			final List<String> errorCausedBy,
			final List<String> failCausedBy,
			final boolean useColoringKeywords,
			final Map<String, RGB> coloringKeywords,
			final boolean filteredConnectingPorts,
			final boolean filteredMappingPorts) {

		this.verbosePrintouts = verbosePrintouts;
		this.selectedValueContentType = selectedValueContentType;
		this.sutName = sutName;
		this.ignoredComponents = ignoredComponents;
		this.ignoredSignals = ignoredSignals;
		this.ignoredFunctions = ignoredFunctions;
		this.visualOrderComponents = visualOrderComponents;
		this.filteredSilentEvents = filteredSilentEvents;
		this.replaceCompVisOrder = replaceCompVisOrder;
		this.openPropAfterCompExt = openPropAfterCompExt;
		this.mscViewOpen = mscViewOpen;
		this.projectTabDefault = projectTabDefault;
		this.testcaseTabDefault = testcaseTabDefault;
		this.mscViewDefault = mscViewDefault;
		this.setverdictError = setverdictError;
		this.setverdictFail = setverdictFail;
		this.setverdictInconc = setverdictInconc;
		this.setverdictNone = setverdictNone;
		this.setverdictPass  = setverdictPass;
		this.errorCausedBy = errorCausedBy;
		this.failCausedBy = failCausedBy;
		this.useColoringKeywords = useColoringKeywords;
		this.coloringKeywords = coloringKeywords;
		this.filteredConnectingPorts = filteredConnectingPorts;
		this.filteredMappingPorts = filteredMappingPorts;
	}
	
	/**
	 * Sets a flag indicating if verbose printouts should be used
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param verbosePrintouts the verbosePrintouts to set
	 */
	protected void setVerbosePrintoutsEnabled(final boolean verbosePrintouts) {
		this.verbosePrintouts = verbosePrintouts;
	}
	
	/**
	 * Returns a flag indicating if verbose printouts should be used
	 * @return the verbosePrintouts
	 */
	public boolean getVerbosePrintoutsEnabled() {
		return this.verbosePrintouts;
	}

	/**
	 * Sets the way values should be displayed (as tree or as text)
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param selectedValueContentType the selectedValueContentType to set
	 */
	protected void setSelectedValueContentType(final String selectedValueContentType) {
		this.selectedValueContentType = selectedValueContentType;
	}
	
	/**
	 * Returns the way values should be displayed (as tree or as text)
	 * @return the selectedValueContentType
	 */
	public String getSelectedValueContentType() {
		return this.selectedValueContentType;
	}

	/**
	 * Sets the name of SUT
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param sutName the sutName to set
	 */
	protected void setSutName(final String sutName) {
		this.sutName = sutName;
	}
	
	/**
	 * Returns the name of SUT
	 * @return the sutName
	 */
	public String getSutName() {
		return this.sutName;
	}
	
	/**
	 * Sets a list of ignored components
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param ignoredComponents the ignoredComponents to set
	 */
	protected void setIgnoredComponents(final Map<String, Integer> ignoredComponents) {
		this.ignoredComponents = ignoredComponents;
	}
	
	/**
	 * Returns a list of ignored components
	 * @return the ignoredComponents
	 */
	public Map<String, Integer> getIgnoredComponents() {
		return this.ignoredComponents;
	}
	
	/**
	 * Sets a list of ignored signals
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param ignoredSignals the ignoredSignals to set
	 */
	protected void setIgnoredSignals(final Map<String, Integer> ignoredSignals) {
		this.ignoredSignals = ignoredSignals;
	}
	
	/**
	 * Returns a list of ignored signals
	 * @return the ignoredSignals
	 */
	public Map<String, Integer> getIgnoredSignals() {
		return this.ignoredSignals;
	}
	
	/**
	 * Sets a list of ignored functions
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param ignoredFunctions the ignoredFunctions to set
	 */
	protected void setIgnoredFunctions(final Map<String, Integer> ignoredFunctions) {
		this.ignoredFunctions = ignoredFunctions;
	}
	
	/**
	 * Returns a list of ignored functions
	 * @return the ignoredFunctions
	 */
	public Map<String, Integer> getIgnoredFunctions() {
		return this.ignoredFunctions;
	}
	
	/**
	 * Sets a list of the preferred visual order of components 
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param visualOrderComponents the visualOrderComponents to set
	 */
	protected void setVisualOrderComponents(final List<String> visualOrderComponents) {
		this.visualOrderComponents = visualOrderComponents;
	}

	/**
	 * Returns a list of the preferred visual order of components 
	 * @return the visualOrderComponents
	 */
	public List<String> getVisualOrderComponents() {
		return this.visualOrderComponents;
	}

	/**
	 * Sets an boolean array indicating which silent events that should be filtered
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param filteredSilentEvents the hash map indicating which silent events that should be filtered
	 */
	protected void setFilteredSilentEvents(final Map<String, Boolean> filteredSilentEvents) {
		this.filteredSilentEvents = filteredSilentEvents;
	}
	
	/**
	 * Returns an hash map indicating which silent events that should be filtered
	 * @return an hash map indicating which silent events that should be filtered
	 */
	public Map<String, Boolean> getFilteredSilentEvents() {
		return this.filteredSilentEvents;
	}

	/**
	 * Sets a flag indicating if the components visual order should be replaced with extracted components
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param replaceCompVisOrder a flag indicating if the components visual order should be replaced with extracted components
	 */
	protected void setReplaceCompVisOrder(final boolean replaceCompVisOrder) {
		this.replaceCompVisOrder = replaceCompVisOrder;
	}
	
	/**
	 * Returns a flag indicating if the components visual order should be replaced with extracted components
	 * @return a flag indicating if the components visual order should be replaced with extracted components
	 */
	public boolean getReplaceCompVisOrder() {
		return this.replaceCompVisOrder;
	}
	
	/**
	 * Sets a flag indicating if the project properties should be opened after extracting components
	 * NOTE: this method is only intended for PreferencesHandler
	 * @param openPropAfterCompExt a flag indicating if the project properties should be opened after extracting components
	 */
	protected void setOpenPropAfterCompExt(final boolean openPropAfterCompExt) {
		this.openPropAfterCompExt = openPropAfterCompExt;
	}
	
	/**
	 * Returns a flag indicating if the project properties should be opened after extracting components
	 * @return a flag indicating if the project properties should be opened after extracting components
	 */
	public boolean getOpenPropAfterCompExt() {
		return this.openPropAfterCompExt;
	}

	/**
	 * Returns how to open the MSCView
	 * @return how to open the MSCView
	 */
	public int getMscViewOpen() {
		return this.mscViewOpen;
	}

	/**
	 * Sets how to open the MSCView
	 * @param mscViewOpen integer flag indicating how to open the MSCView
	 */
	public void setMscViewOpen(final int mscViewOpen) {
		this.mscViewOpen = mscViewOpen;
	}

	public int getMscViewDefault() {
		return this.mscViewDefault;
	}

	public void setMscViewDefault(final int mscViewDefault) {
		this.mscViewDefault = mscViewDefault;
	}

	public int getProjectTabDefault() {
		return this.projectTabDefault;
	}

	public void setProjectTabDefault(final int projectTabDefault) {
		this.projectTabDefault = projectTabDefault;
	}

	public int getTestcaseTabDefault() {
		return this.testcaseTabDefault;
	}

	public void setTestcaseTabDefault(final int testcaseTabDefault) {
		this.testcaseTabDefault = testcaseTabDefault;
	}

	public boolean getSetverdictError() {
		return this.setverdictError;
	}

	public void setSetverdictError(final boolean setverdictError) {
		this.setverdictError = setverdictError;
	}

	public boolean getSetverdictFail() {
		return this.setverdictFail;
	}

	public void setSetverdictFail(final boolean setverdictFail) {
		this.setverdictFail = setverdictFail;
	}

	public boolean getSetverdictInconc() {
		return this.setverdictInconc;
	}

	public void setSetverdictInconc(final boolean setverdictInconc) {
		this.setverdictInconc = setverdictInconc;
	}

	public boolean getSetverdictNone() {
		return this.setverdictNone;
	}

	public void setSetverdictNone(final boolean setverdictNone) {
		this.setverdictNone = setverdictNone;
	}

	public boolean getSetverdictPass() {
		return this.setverdictPass;
	}

	public void setSetverdictPass(final boolean setverdictPass) {
		this.setverdictPass = setverdictPass;
	}

	public List<String> getErrorCausedBy() {
		return this.errorCausedBy;
	}

	public void setErrorCausedBy(final List<String> errorCausedBy) {
		this.errorCausedBy = errorCausedBy;
	}

	public List<String> getFailCausedBy() {
		return this.failCausedBy;
	}

	public void setFailCausedBy(final List<String> failCausedBy) {
		this.failCausedBy = failCausedBy;
	}

	public Map<String, RGB> getColoringKeywords() {
		return this.coloringKeywords;
	}

	public void setColoringKeywords(final Map<String, RGB> coloringKeywords) {
		this.coloringKeywords = coloringKeywords;
	}

	public boolean getUseColoringKeywords() {
		return this.useColoringKeywords;
	}

	public void setUseColoringKeywords(final boolean useColoringKeywords) {
		this.useColoringKeywords = useColoringKeywords;
	}

	public boolean getFilteredConnectingPorts() {
		return filteredConnectingPorts;
	}

	public void setFilteredConnectingPorts(final boolean filteredConnectingPorts) {
		this.filteredConnectingPorts = filteredConnectingPorts;
	}

	public boolean getFilteredMappingPorts() {
		return filteredMappingPorts;
	}

	public void setFilteredMappingPorts(final boolean filteredMappingPorts) {
		this.filteredMappingPorts = filteredMappingPorts;
	}

}
