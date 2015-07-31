/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.model;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.Vector;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.log.viewer.models.FilterPattern;
import org.eclipse.titan.log.viewer.parsers.Parser;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.utils.Constants;

/**
 * This class is the data model for an extracted test case 
 *
 */
public class ExecutionModel extends Observable {
	private FilterPattern filterPattern;
	private Map<String, Integer> ignoredComponents;
	private Map<String, Integer> ignoredSignals;
	private Map<String, Integer> ignoredFunctions;
	/** map of components we deal with. */
	private Map<String, TestComponent> components;
	private TestCase containedTestCase;
	/** Hash map with all component names and ref */
	private Map<String, String> componentNames;
	private int[] setverdict;
	private String sutName;
	private Parser parser;
	private List<EventObject> lifeLineInfo;

	public ExecutionModel(final Parser parser) {
		this.components        = new HashMap<String, TestComponent>();
		this.componentNames	  = new HashMap<String, String>();
		this.parser = parser;
		lifeLineInfo = new Vector<EventObject>();
	}

	/**
	 * @return the test case
	 */
	public TestCase getTestCase() {
		return this.containedTestCase;
	}
	
	/**
	 * @return the components in this model
	 */
	public Map<String, TestComponent> getComponents() {
		return this.components;
	}

	/**
	 * Calculates the given event.
	 *
	 * @param index the index of the needed event.
	 * @return the event in this model
	 */
	public IEventObject getEvent(final int index) {
		List<IEventObject> result = getEvents(index, index);
		return result.get(3);
	}
	
	/**
	 * Calculates the records position in the events vector.
	 * The events are ordered by the record numbers so binary search can be used.
	 * @param recordNumber The record number of the given record
	 * @return The position in the events vector or -1 if not found.
	 */
	public int getRecordsPosition(final int recordNumber) {
		if (recordNumber < 0 || recordNumber > containedTestCase.getEndRecordNumber()) {
			return -1;
		}
		
		IEventObject element;
		for (int min = 0, max = getNumberOfEvents() - 1, mid = (min + max) / 2;
				min <= max;
				mid = (min + max) / 2) {
			
			element = getEvent(mid);
			if (recordNumber > element.getRecordNumber()) {
				min = mid + 1;
			} else if (recordNumber < element.getRecordNumber()) {
				max = mid - 1;
			} else {
				return mid;
			}
		}

		return -1;
	}

	/**
	 * @return whether the MTC has terminated correctly according to the log or it is not mentioned.
	 * */
	public boolean getMTCTerminated() {
		return parser.getMTCTerminated();
	}

	public void addLifeLineInfo(final EventObject event) {
		lifeLineInfo.add(event);
	}

	/**
	 * Returns only information that is related to lifelines, component creation and termination.
	 * Basically this is a preprocess step, to create the structure of the MSC chart.
	 *
	 * */
	public List<EventObject> getLifelineInformation() {
		return lifeLineInfo;
	}

	/**
	 * Calculates a list of events that might belong to the provided index range.
	 * The SUT, MTC and System components are always added.
	 *
	 * @param startIndex the index of the event to be displayed.
	 * @param endIndex the index of the last event to be displayed.
	 *
	 * @return the events parsed from the given region.
	 * */
	public List<IEventObject> getEvents(final int startIndex, final int endIndex) {
		int end = endIndex > getNumberOfEvents() ? getNumberOfEvents() : endIndex;
		try {
			return new ArrayList<IEventObject>(parser.parseRegion(startIndex, end, null));
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ArrayList<IEventObject>();
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return new ArrayList<IEventObject>();
		}
	}

	/**
	 * @return the number of events in this model
	 */
	public int getNumberOfEvents() {
		if (parser == null) {
			return 0;
		}

		return parser.getNumberOfEvents();
	}
	
	/**
	 * @return the active filter pattern (can be null)
	 */
	public FilterPattern getFilterPattern() {
		return filterPattern;
	}
	
	public void setFilterPattern(final FilterPattern filterPattern) {
		this.filterPattern = filterPattern;
	}


	/**
	 * If a component is ignored, this method returns the component
	 * that acts as alternative component in the graph.
	 */
	private String getAlternativeComponent(final String name) {
		if (this.ignoredComponents == null) {
			return null;
		}
		
		// SUT name can not be filtered
		if (name.contentEquals(this.sutName) || name.contentEquals(Constants.MTC)) {
			return null;
		}
		
		Set<String> filteredComp = this.ignoredComponents.keySet();
		for (String currComp : filteredComp) {
			int filterType = this.ignoredComponents.get(currComp);
			switch (filterType) {
				case PreferenceConstants.FILTER_EQUALS:
					if (name.contentEquals(currComp)) {
						return Constants.SUT_REFERENCE;
					}
					break;
				case PreferenceConstants.FILTER_START_WITH:
					if (name.startsWith(currComp)) {
						return Constants.SUT_REFERENCE;
					}
					break;
				case PreferenceConstants.FILTER_END_WITH:
					if (name.endsWith(currComp)) {
						return Constants.SUT_REFERENCE;
					}
					break;
				case PreferenceConstants.FILTER_CONTAINS:
					if (name.contains(currComp)) {
						return Constants.SUT_REFERENCE;
					}
					break;
				default:
					break;
			}
		}
		return null;
	}

	/**
	 * This predicate is used to check if a component is ignored or not
	 * @param reference component
	 * @return true or false
	 */
	public boolean isComponentIgnored(final String reference) {
		if (this.filterPattern != null) {
			return false;
		}

		if (this.components == null) {
			return false;
		}

		TestComponent component = this.components.get(reference);

		return component != null
				&& component.getAlternative() != null;

	}
	
	/**
	 * This predicate is used to check if a signal is ignored or not.
	 */
	public boolean isSignalIgnored(final String signal) {
		if (filterPattern != null) {
			return false;
		}
		
		if (this.ignoredSignals == null) {
			return false;
		}
		String tempSignal = signal.trim();
		Set<String> filteredSignal = this.ignoredSignals.keySet();
		for (String currSignal : filteredSignal) {
			int filterType = this.ignoredSignals.get(currSignal);

			switch (filterType) {
				case PreferenceConstants.FILTER_EQUALS:
					if (tempSignal.contentEquals(currSignal)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_START_WITH:
					if (tempSignal.startsWith(currSignal)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_END_WITH:
					if (tempSignal.endsWith(currSignal)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_CONTAINS:
					if (tempSignal.contains(currSignal)) {
						return true;
					}
					break;
				default:
					break;
			}
		}
		return false;
	}
	
	public boolean isEventIgnored(final String event) {
		return this.filterPattern != null
				&& filterPattern.isEventIgnored(event);

	}
	
	/**
	 * This predicate is used to check if a function is ignored or not.
	 */
	public boolean isFunctionIgnored(final String function) {
		if (this.filterPattern != null) {
			return false;
		}
		
		if (this.ignoredFunctions == null) {
			return false;
		}

		Set<String> filteredFunc = this.ignoredFunctions.keySet();
		for (String currFunction : filteredFunc) {
			int filterType = this.ignoredFunctions.get(currFunction);

			switch (filterType) {
				case PreferenceConstants.FILTER_EQUALS:
					if (function.contentEquals(currFunction)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_START_WITH:
					if (function.startsWith(currFunction)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_END_WITH:
					if (function.endsWith(currFunction)) {
						return true;
					}
					break;
				case PreferenceConstants.FILTER_CONTAINS:
					if (function.contains(currFunction)) {
						return true;
					}
					break;
				default:
					break;
			}
		}
		return false;
	}

	/**
	 * This method simply adds a new component to the model when a
	 * component creation if found in the log file text.
	 */
	public void addComponent(final EventObject event) {
		String reference = event.getReference();
		String name = event.getName();
		if (name.length() == 0) {
			name = reference;
		}
		String alternative = getAlternativeComponent(name);
		this.components.put(reference, new TestComponent(event, alternative));
		// add component name to vector if name exists in event
		String componentName = event.getName();
		if ((componentName != null) && (componentName.trim().length() > 0)) {
			this.componentNames.put(event.getName(), reference);
		} 
	}

	/**
	 * Port mapping events do not result in new events in the vector
	 * of log events. The information in port mapping event lines
	 * is stored in internal component tables that describe how
	 * components are connected with each other.
	 * 
	 * @param sourceRef the source
	 * @param sourcePort the source port
	 * @param targetRef the target
	 * @param targetPort the target port
	 */
	public void addPortMapping(final String sourceRef, final String sourcePort, final String targetRef, final String targetPort) {
			if (this.components != null) {
				TestComponent sourceComponent = this.components.get(sourceRef);

				if (sourceComponent != null) {
					sourceComponent.addFromEntry(targetPort, targetRef);
				}

				TestComponent targetComponent = this.components.get(targetRef);
				// FIXME: sourcePort can be null at this point
				if (targetComponent != null) {
					targetComponent.addFromEntry(sourcePort, sourceRef);
				}
			}
	}

	/**
	 * This method is used to notify changes to the registered observers.
	 */
	public void notifyChange() {
		setChanged();
		notifyObservers();
	}

	public TestCase getContainedTestCase() {
		return this.containedTestCase;
	}

	public void setContainedTestCase(final TestCase containedTestCase) {
		this.containedTestCase = containedTestCase;
	}
	
	/**
	 * Method for adding components that should be filtered
	 */
	public void addIgnoredComponent(final Map<String, Integer> ignoredComponents) {
		this.ignoredComponents = ignoredComponents;
	}
	
	/**
	 * Method for adding signals that should be filtered
	 */
	public void addIgnoredSignals(final Map<String, Integer> ignoredSignals) {
		this.ignoredSignals = ignoredSignals;
	}

	/**
	 * Method for adding functions that should be filtered
	 */
	public void addIgnoredFunctions(final Map<String, Integer> ignoredFunctions) {
		this.ignoredFunctions = ignoredFunctions;
	}

	/**
	 * Returns true if the given component name exist in the executionmodel
	 * @param componentName
	 * @return
	 */
	public boolean componentNameExists(final String componentName) {
		return this.componentNames != null
				&& this.componentNames.containsKey(componentName);

	}

	/**
	 * Fetches the component reference from a name
	 * @param componentName
	 * @return
	 */
	public String getComponentRefFromName(final String componentName) {
		if (this.componentNames == null) {
			return null;
		}
		return this.componentNames.get(componentName);
	}

	/***
	 * Fetches the array of index of setverdict events
	 * @return int[]
	 */
	public int[] getSetverdict() {
		return this.setverdict;
	}
	/***
	 * Sets the array of index for setverdict events
	 * @param setverdict the setverdict events array
	 */
	public void setSetverdict(final int[] setverdict) {
		this.setverdict = setverdict;
	}

	/**
	 * Sets the name of the SUT
	 * @param sutName the name of the SUT
	 */
	public void setSutName(final String sutName) {
		this.sutName = sutName;
	}

	/**
	 * Sets the parser's deciphering rule set
	 * @see Parser#setDecipheringRuleset(String)
	 */
	public boolean setDecipheringRuleset(final String name) {
		return parser.setDecipheringRuleset(name);
	}
}
