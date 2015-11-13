/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

/**
 * A list conversion utility class used by property handling.
 * 
 * In some cases we have to store a list of items in a single property, or load
 * from there. We also use the fact, that line breaks are not allowed characters
 * in these lists. So it can be easily used for separating elements.
 * 
 * @author Kristof Szabados
 * */
public final class ListConverter {

	private ListConverter() {
		// Do nothing
	}

	/**
	 * Converts the provided string into an array of sub-strings.
	 * 
	 * @param value
	 *                the value to be converted.
	 * 
	 * @return the array of substrings or an empty array.
	 * */
	public static String[] convertToList(final String value) {
		if (value == null || value.length() == 0) {
			return new String[] {};
		}

		return value.split("\n");
	}

	/**
	 * Converts the provided list of strings into a single string, where
	 * each substring is separated by a \n character.
	 * 
	 * @param values
	 *                the values to be converted.
	 * 
	 * @return the string to hold the converted value, or an empty string.
	 * */
	public static String convertFromList(final String[] values) {
		if (values == null || values.length == 0) {
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			builder.append(values[i]);
			if (i < values.length) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}
}
