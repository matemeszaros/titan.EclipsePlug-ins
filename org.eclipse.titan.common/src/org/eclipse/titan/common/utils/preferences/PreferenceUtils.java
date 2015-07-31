/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.utils.Joiner;
import org.eclipse.titan.common.utils.StringUtils;

public final class PreferenceUtils {

	private PreferenceUtils() {
		// Hide constructor
	}

	/**
	 * Combines the given list of items into a single string.
	 * This method is the converse of {@link #deserializeFromString(String, char, char)}.
	 *
	 * @param items the list of items
	 * @param delimiter the delimiter to use
	 * @param escape the escape character to use if the delimiter is found in the input strings
	 * @return the combined string
	 * @see #deserializeFromString
	 */
	private static String serializeToString(final Iterable<String> items, String delimiter, String escape) {
		Joiner joiner = new Joiner(delimiter);
		for (String item : items) {
			joiner.join(escape(item, delimiter, escape));
		}
		return joiner.toString();
	}

	private static String escape(String original, String delimiter, String escape) {
		return original.replace(escape, escape + escape).replace(delimiter, escape + delimiter);
	}

	/**
	 * Combines the given list of items into a single string.
	 * This method is the converse of {@link #deserializeFromString(String)}.
	 *
	 * @param items the list of items
	 * @return the string serialized form of the list
	 * @see #deserializeFromString
	 */
	public static String serializeToString(final Iterable<String> items) {
		return serializeToString(items, ";", "#");
	}

	/**
	 * Splits the given string into a list of strings.
	 * This method is the converse of <code>createList</code>.
	 *
	 * @param stringList the string
	 * @return an array of <code>String</code>
	 * @see #serializeToString
	 */
	public static List<String> deserializeFromString(final String stringList) {
		return deserializeFromString(stringList, ';', '#');
	}

	/**
	 * Splits the input string into a list of strings.
	 *
	 * @param input the input string to split.
	 * @param delimiter the delimiter according which to split.
	 * @param escape the escape character noting the delimiters not to be used as delimiters.
	 * */
	private static List<String> deserializeFromString(final String input, final char delimiter, final char escape) {
		List<String> results = new ArrayList<String>();
		if (StringUtils.isNullOrEmpty(input)) {
			return results;
		}
		StringBuilder tempResult = new StringBuilder();
		char c;
		for (int i = 0; i < input.length(); ++i) {
			c = input.charAt(i);
			if (escape == c) {
				if (input.length() > i + 1) {
					tempResult.append(input.charAt(i + 1));
					i += 1;
				}
			} else if (delimiter == c) {
				results.add(tempResult.toString());
				tempResult = new StringBuilder();
			} else {
				tempResult.append(c);
			}
		}
		results.add(tempResult.toString());
		return results;
	}
}
