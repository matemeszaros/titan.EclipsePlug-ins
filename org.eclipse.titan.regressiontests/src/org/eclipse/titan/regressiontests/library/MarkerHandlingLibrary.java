/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;

/**
 * This class stores library functions to help writing test that involve markers without actual Eclipse knowledge.
 * */
public final class MarkerHandlingLibrary {

	private MarkerHandlingLibrary() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Does a blind equivalency check of the provided marker attribute maps, meaning that all "known to be usefully" attributes are compared.
	 * <p>
	 * If the attributes don't match exactly it is considered to be an assertion failure.
	 * 
	 * @param attributeMap1 the first map of attributes
	 * @param attributeMap2 the second map of attributes
	 * */
	public static void blindMarkerEquivencyCheck(final Map<?, ?> attributeMap1, final Map<?, ?> attributeMap2) {
		assertNotNull(attributeMap1);
		assertNotNull(attributeMap2);
		
		try {
			assertEquals(attributeMap1.get(IMarker.SEVERITY), attributeMap2.get(IMarker.SEVERITY));
			assertEquals(attributeMap1.get(IMarker.MESSAGE), attributeMap2.get(IMarker.MESSAGE));
			assertEquals(attributeMap1.get(IMarker.LOCATION), attributeMap2.get(IMarker.LOCATION));
			assertEquals(attributeMap1.get(IMarker.PRIORITY), attributeMap2.get(IMarker.PRIORITY));
			assertEquals(attributeMap1.get(IMarker.LINE_NUMBER), attributeMap2.get(IMarker.LINE_NUMBER));
			assertEquals(attributeMap1.get(IMarker.CHAR_START), attributeMap2.get(IMarker.CHAR_START));
			assertEquals(attributeMap1.get(IMarker.CHAR_END), attributeMap2.get(IMarker.CHAR_END));
			assertEquals(attributeMap1.get(IMarker.TRANSIENT), attributeMap2.get(IMarker.TRANSIENT));
		} catch (AssertionFailedError e) {
			System.out.println("Not equivalent markers: ");
			System.out.println(MarkerHandlingLibrary.printErrorMessageAndAttributes(null, "  ", attributeMap1));
			System.out.println(MarkerHandlingLibrary.printErrorMessageAndAttributes(null, "  ", attributeMap2));
			throw e;
		}
	}
	
	/**
	 * Compares the second provided attribute map the first.
	 * The comparison asserts an error if the second attribute map does not have an attribute present in the first one.
	 * Or when the value of the attributes is not the same. 
	 * <p>
	 * If the attributes don't match exactly it is considered to be an assertion failure.
	 * 
	 * @param attributeMap1 the first map of attributes
	 * @param attributeMap2 the second map of attributes
	 * */
	public static void controlledMarkerEquivencyCheck(final Map<?, ?> attributeMap1, final Map<?, ?> attributeMap2) {
		assertNotNull(attributeMap1);
		assertNotNull(attributeMap2);
		
		if (attributeMap1.containsKey(IMarker.SEVERITY)) {
			assertEquals(attributeMap1.get(IMarker.SEVERITY), attributeMap2.get(IMarker.SEVERITY));
		}
		if (attributeMap1.containsKey(IMarker.MESSAGE)) {
			assertEquals(attributeMap1.get(IMarker.MESSAGE), attributeMap2.get(IMarker.MESSAGE));
		}
		if (attributeMap1.containsKey(IMarker.LOCATION)) {
			assertEquals(attributeMap1.get(IMarker.LOCATION), attributeMap2.get(IMarker.LOCATION));
		}
		if (attributeMap1.containsKey(IMarker.PRIORITY)) {
			assertEquals(attributeMap1.get(IMarker.PRIORITY), attributeMap2.get(IMarker.PRIORITY));
		}
		if (attributeMap1.containsKey(IMarker.LINE_NUMBER)) {
			assertEquals(attributeMap1.get(IMarker.LINE_NUMBER), attributeMap2.get(IMarker.LINE_NUMBER));
		}
		if (attributeMap1.containsKey(IMarker.CHAR_START)) {
			assertEquals(attributeMap1.get(IMarker.CHAR_START), attributeMap2.get(IMarker.CHAR_START));
		}
		if (attributeMap1.containsKey(IMarker.CHAR_END)) {
			assertEquals(attributeMap1.get(IMarker.CHAR_END), attributeMap2.get(IMarker.CHAR_END));
		}
		if (attributeMap1.containsKey(IMarker.TRANSIENT)) {
			assertEquals(attributeMap1.get(IMarker.TRANSIENT), attributeMap2.get(IMarker.TRANSIENT));
		}
	}
	
	/**
	 * Transform the provided markers from their IMarker form (as seen in Eclipse), into a map of lists that can be used easier and safer in the testing process.
	 * The actual steps:
	 * <ul>
	 * <li> puts the provided markers into a list.
	 * <li> sorts this list.
	 * <li> splits the list into a map according the IResource the markers are assigned to, but places only the attributes of the elements in this map.
	 * </ul>
	 * 
	 * @param inputMarkers the list of markers used as input.
	 * 
	 * @return 
	 * */
	public static Map<IResource, List<Map<?, ?>>> transformMarkers(final IMarker[] inputMarkers) throws CoreException {
		assertNotNull(inputMarkers);
		
		Map<IResource, List<Map<?, ?>>> resultMap = new HashMap<IResource, List<Map<?, ?>>>();
		
		IMarker tempMarker;
		IResource tempresource;

		List<IMarker> tempSortingList = Arrays.asList(inputMarkers);
		
		Collections.sort(tempSortingList, new Comparator<IMarker>() {

			@Override
			public int compare(final IMarker o1, final IMarker o2) {
				Map<?, ?> attributeMap1;
				Map<?, ?> attributeMap2;

				try {
					attributeMap1 = o1.getAttributes();
					attributeMap2 = o2.getAttributes();
				} catch (CoreException e) {
					return 0;
				}
					
					if (attributeMap1.containsKey(IMarker.LINE_NUMBER) && attributeMap2.containsKey(IMarker.LINE_NUMBER)) {
						int lineNumber1 = ((Integer) attributeMap1.get(IMarker.LINE_NUMBER)).intValue();
						int lineNumber2 = ((Integer) attributeMap2.get(IMarker.LINE_NUMBER)).intValue();
						if (lineNumber1 < lineNumber2) {
							return -1;
						} else if (lineNumber1 > lineNumber2) {
							return 1;
						}
					}

					String message1 = (String) attributeMap1.get(IMarker.MESSAGE);
					String message2 = (String) attributeMap2.get(IMarker.MESSAGE);
					int result = message1.compareTo(message2);
					if (result != 0) {
						return result;
					}

					if (attributeMap1.containsKey(IMarker.CHAR_START) && attributeMap2.containsKey(IMarker.CHAR_START)) {
						int lineNumber1 = ((Integer) attributeMap1.get(IMarker.CHAR_START)).intValue();
						int lineNumber2 = ((Integer) attributeMap2.get(IMarker.CHAR_START)).intValue();
						if (lineNumber1 < lineNumber2) {
							return -1;
						} else if (lineNumber1 > lineNumber2) {
							return 1;
						}
					}
					
					if (attributeMap1.containsKey(IMarker.CHAR_END) && attributeMap2.containsKey(IMarker.CHAR_END)) {
						int lineNumber1 = ((Integer) attributeMap1.get(IMarker.CHAR_END)).intValue();
						int lineNumber2 = ((Integer) attributeMap2.get(IMarker.CHAR_END)).intValue();
						if (lineNumber1 < lineNumber2) {
							return -1;
						} else if (lineNumber1 > lineNumber2) {
							return 1;
						}
					}
					return 0;
			}
		});

		List<Map<?, ?>> tempArray;
		for (int i = 0; i < tempSortingList.size(); i++) {
			tempMarker = tempSortingList.get(i);
			tempresource = tempMarker.getResource();
			if (tempMarker.exists()) {
				if (resultMap.containsKey(tempresource)) {
					tempArray = resultMap.get(tempresource);
					tempArray.add(tempMarker.getAttributes());
				} else {
					tempArray = new ArrayList<Map<?, ?>>();
					tempArray.add(tempMarker.getAttributes());
					resultMap.put(tempresource, tempArray);
				}
			}
		}
		
		return resultMap;
	}
	
	/**
	 * Prints the messages contained in the provided markers into a StringBuffer.
	 * 
	 * @param markers the markers containing the messages to be printed.
	 * 
	 * @return the StringBuffer object containing the extracted messages.
	 * */
	public static StringBuilder printMarkerMessages(final HashMap<IResource, ArrayList<Map<?, ?>>> markers) {
		StringBuilder builder = new StringBuilder();
		
		assertNotNull(markers);
		
		for (IResource resource : markers.keySet()) {
			builder.append("In resource `").append(resource.toString()).append("':\n");
			
			ArrayList<Map<?, ?>> markerList = markers.get(resource);
			assertNotNull(markerList);

			Map<?, ?> tempAttributes;
			for (int i = 0; i < markerList.size(); i++) {
				tempAttributes = markerList.get(i);
				
				builder.append("  ").append(tempAttributes.get(IMarker.MESSAGE)).append("\n");
			}
		}
		
		return builder;
	}
	
	/**
	 * Prints attributes contained in the provided markers and believed to be of importance into a StringBuffer.
	 * 
	 * @param the prefix used to indent the output
	 * @param marker the marker to be printed.
	 * 
	 * @return the StringBuffer object containing the extracted messages.
	 * */
	public static StringBuilder printErrorMessageAndAttributes(final StringBuilder builder, final String prefix, final Map<?, ?> marker) {
		StringBuilder internalBuilder;
		
		if (builder == null) {
			internalBuilder = new StringBuilder();
		} else {
			internalBuilder = builder;
		}

		assertNotNull(marker);

		internalBuilder.append(prefix);

		if (marker.containsKey(IMarker.SEVERITY)) {
			internalBuilder.append("severity: ");
			switch(((Integer) marker.get(IMarker.SEVERITY)).intValue()) {
			case IMarker.SEVERITY_ERROR:
				internalBuilder.append("ERROR");
				break;
			case IMarker.SEVERITY_WARNING:
				internalBuilder.append("WARNING");
				break;
			case IMarker.SEVERITY_INFO:
				internalBuilder.append("INFORMATION");
				break;
			default:
				fail("Unknown severity value: " + ((Integer) marker.get(IMarker.SEVERITY)).intValue());
			}
			internalBuilder.append("\t ");
		}
		if (marker.containsKey(IMarker.PRIORITY)) {
			internalBuilder.append("priority: ");
			switch(((Integer) marker.get(IMarker.PRIORITY)).intValue()) {
			case IMarker.PRIORITY_HIGH:
				internalBuilder.append("HIGH");
				break;
			case IMarker.PRIORITY_NORMAL:
				internalBuilder.append("NORMAL");
				break;
			case IMarker.PRIORITY_LOW:
				internalBuilder.append("LOW");
				break;
			default:
				fail("Unknown priority value: " + ((Integer) marker.get(IMarker.PRIORITY)).intValue());
			}
			internalBuilder.append("\t ");
		}
		if (marker.containsKey(IMarker.LINE_NUMBER)) {
			internalBuilder.append("in line: ").append(marker.get(IMarker.LINE_NUMBER)).append("\t ");
		}
		if (marker.containsKey(IMarker.CHAR_START) && marker.containsKey(IMarker.CHAR_END)) {
			internalBuilder.append("in position: ").append(marker.get(IMarker.CHAR_START)).append(" - ").append(marker.get(IMarker.CHAR_END)).append("\t  ");
		}

		internalBuilder.append(marker.get(IMarker.MESSAGE));

		return internalBuilder;
	}
	
	
	/**
	 * Prints attributes contained in the provided markers and believed to be of importance into a StringBuffer.
	 * 
	 * @param markers the markers containing the messages to be printed.
	 * 
	 * @return the StringBuffer object containing the extracted messages.
	 * */
	public static StringBuilder printErrorMessagesAndAttributes(final Map<IResource, ArrayList<Map<?, ?>>> markers) {
		StringBuilder builder = new StringBuilder();
		
		assertNotNull(markers);
		
		for (IResource resource : markers.keySet()) {
			builder.append("In resource `").append(resource.toString()).append("':\n");
			
			ArrayList<Map<?, ?>> markerList = markers.get(resource);
			assertNotNull(markerList);

			Map<?, ?> tempAttributes;
			for (int i = 0; i < markerList.size(); i++) {
				tempAttributes = markerList.get(i);
				
				builder.append("  ");
				
				if (tempAttributes.containsKey(IMarker.SEVERITY)) {
					builder.append("severity: ");
					switch(((Integer) tempAttributes.get(IMarker.SEVERITY)).intValue()) {
					case IMarker.SEVERITY_ERROR:
						builder.append("ERROR");
						break;
					case IMarker.SEVERITY_WARNING:
						builder.append("WARNING");
						break;
					case IMarker.SEVERITY_INFO:
						builder.append("INFORMATION");
						break;
					default:
						fail("Unknown severity value: " + ((Integer) tempAttributes.get(IMarker.SEVERITY)).intValue());
					}
					builder.append("\t ");
				}
				if (tempAttributes.containsKey(IMarker.PRIORITY)) {
					builder.append("priority: ");
					switch(((Integer) tempAttributes.get(IMarker.PRIORITY)).intValue()) {
					case IMarker.PRIORITY_HIGH:
						builder.append("HIGH");
						break;
					case IMarker.PRIORITY_NORMAL:
						builder.append("NORMAL");
						break;
					case IMarker.PRIORITY_LOW:
						builder.append("LOW");
						break;
					default:
						fail("Unknown priority value: " + ((Integer) tempAttributes.get(IMarker.PRIORITY)).intValue());
					}
					builder.append("\t ");
				}
				if (tempAttributes.containsKey(IMarker.LINE_NUMBER)) {
					builder.append("in line: ").append(tempAttributes.get(IMarker.LINE_NUMBER)).append("\t ");
				}
				if (tempAttributes.containsKey(IMarker.CHAR_START) && tempAttributes.containsKey(IMarker.CHAR_END)) {
					builder.append("in position: ").append(tempAttributes.get(IMarker.CHAR_START)).append(" - ").append(tempAttributes.get(IMarker.CHAR_END)).append("\t  ");
				}
				
				builder.append(tempAttributes.get(IMarker.MESSAGE)).append("\n");
			}
		}
		
		return builder;
	}
	
	/**
	 * Traverses the list of attributes provided to find the first one that can match a given pattern.
	 * If it is found it will be removed from the list.
	 * 
	 * @param markers the list of marker attributes to traverse.
	 * @param marker the marker that is expected to be on the list at the right place
	 * @param hardCheck indicates if the function stops the execution or not if the required marker is found
	 * 
	 * */
	public static void searchNDestroyFittingMarker(final List<Map<?, ?>> markers, final Map<?, ?> marker, final boolean hardCheck) {
		assertNotNull(markers);
		assertNotNull(marker);

		String message = (String) marker.get(IMarker.MESSAGE);
		int lineNum = (Integer) marker.get(IMarker.LINE_NUMBER);
		int severity = (Integer) marker.get(IMarker.SEVERITY); 
		
		Map<?, ?> attributes;
		for (int i = markers.size() - 1; i >= 0; i--) {
			attributes = markers.get(i);
			
			assertNotNull(attributes);
			
			if ((attributes.containsKey(IMarker.LINE_NUMBER) && (Integer) attributes.get(IMarker.LINE_NUMBER) == lineNum)
					&& ((String) attributes.get(IMarker.MESSAGE)).equals(message)
					&& ((Integer) attributes.get(IMarker.SEVERITY) == severity)
			   ) {
				markers.remove(i);
				return;
			}
		}
		
		if (hardCheck) {
			fail("The required Marker - ".concat(" (").concat(message).concat(")").concat(" - was not found at the expected location: ".concat(new Integer(lineNum).toString())));
		} else {
			TITANDebugConsole.println(
					"The required Marker - ".concat(" (").concat(message).concat(")").concat(" - was not found at the expected location: ".concat(new Integer(lineNum).toString())));
		}
		
	}
	
	
	/**
	 * Prints a non empty marker array to the standard output. Can be used to detect unchecked markers.
	 * 
	 * @param source the source file name of the markers in the array
	 * @param markerArray the markers to be left
	 * 
	 * @return none
	 * */
	public static void printMarkerArray(final String source, final List<Map<?, ?>> markerArray) {
		if (markerArray.isEmpty()) {
			return;
		}
		
		System.out.println("The following messages did not checked in " + source + " :");
		
		for (Iterator<Map<?, ?>> iterator = markerArray.iterator(); iterator.hasNext();) {
			System.out.println(printErrorMessageAndAttributes(null, "  ", iterator.next()));
		}
	}
}
