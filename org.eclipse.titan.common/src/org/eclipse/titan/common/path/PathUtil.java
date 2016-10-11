/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.path;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.Joiner;

/**
 * This is a small utility to create a relative pathname from a path and a base
 * location, to what the result should be relative to.
 * 
 * @author Kristof Szabados
 */
public final class PathUtil {

	/** private class to disable instantiation */
	private PathUtil() {
	}

	/**
	 * Retrieves the path of a file as a list of Strings.
	 * 
	 * @param file
	 *                the file whose path we want to know.
	 * @return the path converted to a list of Strings, holding the elements
	 *         of the path in reverse order.
	 */
	private static List<String> getPathList(final File file) {
		final List<String> temp = new ArrayList<String>();

		try {
			File result = file.getCanonicalFile();
			while (result != null) {
				temp.add(result.getName());
				result = result.getParentFile();
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace("The canonical path of " + file.getName() + "could not be calculated", e);
		}

		Collections.reverse(temp);
		return temp;
	}

	/**
	 * This is the function that does the actual work of creating the
	 * absolute path.
	 * <p>
	 * Behavior:
	 * <ul>
	 * <li>as long as the paths are the same we jump over them (throw them
	 * out).
	 * <li>the relative path must contain exactly as many "../" parts as
	 * many part there are left in the base list, because we must step out
	 * of so many directories.
	 * <p>
	 * For example: if base = /mnt/c/ and what = /home/, this means that we
	 * must step out of 2 directories to reach the common root of the paths.
	 * <li>what is left from the target path, is added to the relative path,
	 * because we must enter exactly those directories.
	 * </ul>
	 * 
	 * @see #getAbsolutePath(String, String)
	 * 
	 * @param baseList
	 *                the base path as a list of Strings.
	 * @param relativeList
	 *                the target path as a list of Strings.
	 * @return the absolute path as a String.
	 */
	private static String calculateAbsolutePath(final List<String> baseList, final List<String> relativeList) {
		int index = 0;
		final StringBuilder builder = new StringBuilder();
		final List<String> result = new ArrayList<String>(baseList);

		while (index < relativeList.size()) {
			final String temp = relativeList.get(index);
			index++;
			if ("..".equals(temp)) {
				if (!result.isEmpty()) {
					result.remove(result.size() - 1);
				}
			} else {
				result.add(temp);
			}
		}

		new Joiner(File.separator).join(result).appendTo(builder);

		return builder.toString();
	}

	/**
	 * Tries to create a absolute path.
	 * 
	 * @see #calculateAbsolutePath(List, List)
	 * 
	 * @param base
	 *                the base file to which the path is relative to.
	 * @param relative
	 *                the file to which which the result should point to
	 *                viewed from the base file's path.
	 * @return the absolute path.
	 */
	public static String getAbsolutePath(final String base, final String relative) {
		final Path basePath = new Path(base);
		final Path relativePath = new Path(relative);
		final String baseDevice = basePath.getDevice();
		final String relativeDevice = relativePath.getDevice();

		if (baseDevice != null && relativeDevice != null && !basePath.getDevice().equals(relativePath.getDevice())) {
			return relative;
		}

		final List<String> baseList = Arrays.asList(basePath.segments());
		final List<String> relativeList = Arrays.asList(relativePath.segments());

		final Path calculatedPath = new Path(calculateAbsolutePath(baseList, relativeList));
		final IPath temp = calculatedPath.setDevice(baseDevice);
		final IPath absolutePath = temp.makeAbsolute();

		return absolutePath.toOSString();
	}

	/**
	 * This is the function that does the actual work of creating the
	 * relative path.
	 * <p>
	 * Behavior:
	 * <ul>
	 * <li>as long as the paths are the same we jump over them (throw them
	 * out).
	 * <li>the relative path must contain exactly as many "../" parts as
	 * many part there are left in the base list, because we must step out
	 * of so many directories.
	 * <p>
	 * For example: if base = /mnt/c/ and what = /home/, this means that we
	 * must step out of 2 directories to reach the common root of the paths.
	 * <li>what is left from the target path, is added to the relative path,
	 * because we must enter exactly those directories.
	 * </ul>
	 * 
	 * @see #getPathList(File)
	 * 
	 * @param baseList
	 *                the base path as a list of Strings.
	 * @param whatList
	 *                the target path as a list of Strings.
	 * @return the relative path as.
	 */
	private static String calculateRelativePath(final List<String> baseList, final List<String> whatList) {
		if (baseList.isEmpty()) {
			return File.separator + new Joiner(File.separator).join(whatList).toString();
		}

		int baseIndex = 0;
		int whatIndex = 0;

		while ((baseIndex < baseList.size()) && (whatIndex < whatList.size()) && (baseList.get(baseIndex).equals(whatList.get(whatIndex)))) {
			baseIndex++;
			whatIndex++;
		}

		final StringBuilder builder = new StringBuilder();
		for (; baseIndex < baseList.size(); baseIndex++) {
			builder.append("..").append(File.separator);
		}

		for (; whatIndex < whatList.size(); whatIndex++) {
			builder.append(whatList.get(whatIndex));
			if (whatIndex != whatList.size() - 1) {
				builder.append(File.separator);
			}
		}

		return builder.toString();
	}

	/**
	 * Tries to create a relative path.
	 * 
	 * @see #calculateRelativePath(List, List)
	 * 
	 * @param base
	 *                the base file to which the result should be relative
	 *                to.
	 * @param what
	 *                the file to which which the result should point to
	 *                viewed from the base file's path.
	 * @return the relative path.
	 */
	public static String getRelativePath(final File base, final File what) {
		final List<String> baseList = getPathList(base);
		final List<String> whatList = getPathList(what);

		return calculateRelativePath(baseList, whatList);
	}

	/**
	 * Tries to create a relative path.
	 * 
	 * @see #calculateRelativePath(List, List)
	 * 
	 * @param base
	 *                the base path to which the result should be relative
	 *                to.
	 * @param what
	 *                the path to which which the result should point to
	 *                from the base path.
	 * @return the relative path.
	 */
	public static String getRelativePath(final String base, final String what) {
		if (base.equals(what)) {
			return ".";
		}

		if (base.length() == 0) {
			return what;
		}

		final Path basePath = new Path(base);
		final Path whatPath = new Path(what);
		final String baseDevice = basePath.getDevice();
		final String whatDevice = whatPath.getDevice();

		if (baseDevice == null || whatDevice == null) {
			if (baseDevice != null && !baseDevice.equals(whatDevice)) {
				return what;
			}
		} else if (!baseDevice.equals(whatDevice)) {
			return what;
		}

		return calculateRelativePath(Arrays.asList(basePath.segments()), Arrays.asList(whatPath.segments()));
	}
}
