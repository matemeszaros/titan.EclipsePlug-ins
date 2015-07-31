/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;

public class ComponentExtractor extends Extractor {
	

	// Variables
	private int currentProgress;
	private int currentCompRef;
	private String currentCompName;
	private Map<Integer, String> components;
	
	// Constants for ComponentExtractor
	private static final char[] PTC_CREATION = "PTC was created. Component reference: ".toCharArray(); //$NON-NLS-1$
	private static final int PTC_CREATION_LENGTH = PTC_CREATION.length;
	private static final char[] COMP_NAME_START = "component name: ".toCharArray(); //$NON-NLS-1$
	private static final int COMP_NAME_START_LENGTH = COMP_NAME_START.length;
	private static final char[] COMP_NAME_END = ", process id: ".toCharArray(); //$NON-NLS-1$
	private static final char[] COMP_REF_END = ", component type: ".toCharArray(); //$NON-NLS-1$
	private static final char[] ALT_COMP_REF_END = ".".toCharArray(); //$NON-NLS-1$
	private static final char[] TTCN_3_PARALLEL_TEST_COMPONENT_STARTED_ON = "TTCN-3 Parallel Test Component started on ".toCharArray(); //$NON-NLS-1$
	private static final char[] COMP_REF = "Component reference:".toCharArray(); //$NON-NLS-1$
	private static final int COMP_REF_LENGTH = COMP_REF.length;
	private static final char[] TTCN_3_COMP_NAME_END = ". Version: ".toCharArray(); //$NON-NLS-1$
	private static final char[] COMP_TYPE = ", component type:".toCharArray(); //$NON-NLS-1$
	private static final char[] PTC_ALIVE = ", alive:".toCharArray(); //$NON-NLS-1$
	private static final char[] TESTCASE_NAME = ", testcase name:".toCharArray(); //$NON-NLS-1$
	/**
	 * Constructor 
	 */
	public ComponentExtractor() {
		this.currentProgress = 0;
		this.components = new HashMap<Integer, String>();
	}
	
	/**
	 * Extracts Components from a Log File
	 * @param logFileMetaData meta data about the log file
	 * @throws IOException if log file not found or error while extracting
	 */
	public void extractComponentsFromLogFile(final LogFileMetaData logFileMetaData, final IProgressMonitor monitor) throws IOException {
		extractFromLogFile(logFileMetaData, monitor);
	}
	
	/**
	 * Return  an array list with the name of all (no duplicates) the found components
	 * @return an array list with the name of all (no duplicates) the found components
	 */
	public List<String> getComponents() {
		List<String> componentsArray = new ArrayList<String>();
		Set<Integer> keys = this.components.keySet();
		List<Integer> sortedKeys = new ArrayList<Integer>(keys);
		Collections.sort(sortedKeys);

		for (Integer sortedKey : sortedKeys) {
			String compName = this.components.get(sortedKey);
			if ((compName != null) && !componentsArray.contains(compName)) {
				componentsArray.add(compName);
			}
		}

		return componentsArray;
	}

	@Override
	protected void processRow(final int offsetStart, final int offsetEnd, final int recordNumber) throws IOException {
		// Reset comp ref and name
		this.currentCompRef = 0;
		this.currentCompName = null;
		
		int pos = findPos(PTC_CREATION, offsetStart, offsetEnd);
		if (pos > 0) {
			// Component creation found, calculate start position for component reference
			int compRefStartPos = pos + PTC_CREATION_LENGTH + 1;
			
			// Check if component name is defined
			int compName = findPos(COMP_NAME_START, offsetStart, offsetEnd);
			if (compName > 0) {
				// Component name found, calculate start position and length
				int compNameStartPos = compName + COMP_NAME_START_LENGTH + 1;
				int compNameLength = findPos(TESTCASE_NAME, offsetStart, offsetEnd) + 1 - compNameStartPos;
				if (compNameLength < 0) {
					compNameLength = findPos(COMP_NAME_END, offsetStart, offsetEnd) + 1 - compNameStartPos;
					if (compNameLength < 0) {
						return;
					}
				}
				this.currentCompName = new String(this.buffer, compNameStartPos, compNameLength);
				// Calculate component reference length
				int compRefLength = findPos(COMP_REF_END, offsetStart, offsetEnd) + 1 - compRefStartPos;
				if (compRefLength < 0) {
					return;
				}
				try {
					this.currentCompRef = Integer.parseInt(new String(this.buffer, compRefStartPos, compRefLength));
				} catch (NumberFormatException nfe) {
					// Illegal comp ref -> return
					return;
				}
			} else {
				// Component name not defined, use component reference as name. Log Format TITAN R7B 1.7pl1
				int compRefLength = findPos(PTC_ALIVE, compRefStartPos, offsetEnd) - compRefStartPos + 1;
				if (compRefLength > 0) {
					this.currentCompName = new String(this.buffer, compRefStartPos, compRefLength);
					try {
						this.currentCompRef = Integer.parseInt(this.currentCompName);

					} catch (NumberFormatException nfe) {
						// Illegal comp ref -> return
						return;
					}
				} else {
					// Component name not defined, use component reference as name
					compRefLength = findPos(ALT_COMP_REF_END, compRefStartPos, offsetEnd) - compRefStartPos + 1;
					if (compRefLength > 0) {
						this.currentCompName = new String(this.buffer, compRefStartPos, compRefLength);
						try {
							this.currentCompRef = Integer.parseInt(this.currentCompName);
						} catch (NumberFormatException nfe) {
							// Illegal comp ref -> return
							return;
						}
					}
				}
			}
		}
		// find if the component has been created by the message 
		//"TTCN-3 Parallel Test Component started on %s. Component reference: %d, component type: %s.%s. Version: %s"
		
		pos = findPos(TTCN_3_PARALLEL_TEST_COMPONENT_STARTED_ON, offsetStart, offsetEnd);
		if (pos > 0) {
			// TTCN-3 Component creation found, calculate start position for component reference
			int compRef = findPos(COMP_REF, offsetStart, offsetEnd);

			if (compRef > 0) {

				//Calculate component reference length
				int compRefStartPos = compRef + COMP_REF_LENGTH + 2;
				int compRefLength = findPos(COMP_TYPE, offsetStart, offsetEnd) + 1 - compRefStartPos; 
				if (compRefLength < 0) {
					return;
				}
				try {
					this.currentCompRef = Integer.parseInt(new String(this.buffer, compRefStartPos, compRefLength));
				} catch (NumberFormatException nfe) {
					// Illegal comp ref -> return
					return;
				}
			
				// Check if component name is defined
				int compName = findPos(COMP_NAME_START, offsetStart, offsetEnd);
				if (compName > 0) {
					// Component name found, calculate start position and length
					int compNameStartPos = compName + COMP_NAME_START_LENGTH + 1;
					int compNameLength = findPos(TTCN_3_COMP_NAME_END, offsetStart, offsetEnd) + 1 - compNameStartPos;
					if (compNameLength < 0) {
						// Length Error
						return;
					}
					this.currentCompName = new String(this.buffer, compNameStartPos, compNameLength);
				} else {				
						this.currentCompName = Integer.toString(this.currentCompRef);				
				}
			}
		}
//		 Comp ref and name found, add component
		if ((this.currentCompRef > 0) && (this.currentCompName != null)) {
			this.currentProgress = (int) (this.filePointer * (100.0 / this.fileSize));
			addComponent();
		}
	}
	
	@Override
	protected void processRowsFinished(final int offsetStart, final int offsetEnd, final int recordNumber) throws IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Adds a test case to the test case vector and notifies the observers 
	 */
	private void addComponent() {
		if (this.components.containsKey(this.currentCompRef)) {
			// Previously value exists, if value = ref, then replace
			if (this.components.get(this.currentCompRef).contentEquals(String.valueOf(this.currentCompRef))) {
				this.components.put(this.currentCompRef, this.currentCompName);
				notifyAddedComponent();
			}
		} else {
			this.components.put(this.currentCompRef, this.currentCompName);
			notifyAddedComponent();
		}
	}
	
	/**
	 * Notifies listeners that a new component was added 
	 */
	private void notifyAddedComponent() {
		setChanged();
		notifyObservers(new ComponentEvent(this.currentCompName, this.currentProgress));
	}
}
