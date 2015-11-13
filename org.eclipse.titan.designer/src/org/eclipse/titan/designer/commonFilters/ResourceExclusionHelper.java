/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.commonFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.FileBuildPropertyData;
import org.eclipse.titan.designer.properties.data.FolderBuildPropertyData;

/**
 * Helper class for checking if a resource is excluded or not.
 * 
 * please note, that propertydata classes are used for backward compatibility
 * reasons.
 * 
 * @author Kristof Szabados
 * */
public final class ResourceExclusionHelper {
	public static final QualifiedName EXCLUDED_FILE_QUALIFIER = new QualifiedName(FileBuildPropertyData.QUALIFIER,
			FileBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY);
	public static final QualifiedName EXCLUDED_FOLDER_QUALIFIER = new QualifiedName(FolderBuildPropertyData.QUALIFIER,
			FolderBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY);

	private List<Matcher> matchers;

	public ResourceExclusionHelper() {
		this.matchers = new ArrayList<Matcher>();
		String stringList = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.EXCLUDED_RESOURCES, "", null);
		List<String> splittedList = intelligentSplit(stringList, '#', '\\');
		boolean reportedError = false;
		for (String item : splittedList) {
			try {
				Pattern pattern = Pattern.compile(item);
				Matcher matcher = pattern.matcher("");
				matchers.add(matcher);
			} catch (PatternSyntaxException e) {
				if (!reportedError) {
					ErrorReporter.logError("At least one of the regular expression used as exclusion filter is not correct."
							+ " Please visit the `Excluded Resources' Preference page to correct it. \n"
							+ "Reason: " + e.getLocalizedMessage());
					reportedError = true;
				}
				// simply skip as it is badly formatted
			}
		}
	}

	/**
	 * Evaluates if a given resource name is matching to one of the resource
	 * exclusion regular expressions.
	 * 
	 * @param resourceName
	 *                the name of the resource to check.
	 * @return true if the resource is to be excluded, false otherwise.
	 * */
	public boolean isExcludedByRegexp(final String resourceName) {
		for (Matcher matcher : matchers) {
			matcher.reset(resourceName);
			if (matcher.matches()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the given folder is excluded from the build.
	 * 
	 * @param folder
	 *                the folder to check
	 * @return {@code true} if the folder is excluded, {@code false}
	 *         otherwise
	 */
	public static boolean isDirectlyExcluded(final IFolder folder) {
		try {
			return "true".equalsIgnoreCase(folder.getPersistentProperty(EXCLUDED_FOLDER_QUALIFIER));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while checking persistent property of folder: " + folder.getName(), e);
			return false;
		}
	}

	/**
	 * Returns true if the given file is excluded from the build.
	 * 
	 * @param file
	 *                the folder to check
	 * @return {@code true} if the file is excluded, {@code false} otherwise
	 */
	public static boolean isDirectlyExcluded(final IFile file) {
		try {
			return "true".equalsIgnoreCase(file.getPersistentProperty(EXCLUDED_FILE_QUALIFIER));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while checking persistent property of file: " + file.getName(), e);
			return false;
		}
	}

	/**
	 * Returns true if the given resource is or one of its parent is
	 * excluded from the build.
	 * 
	 * @param resource
	 * @return <code>true</code> if the resource is excluded,
	 *         <code>false</code> otherwise.
	 * @throws CoreException
	 *                 see
	 *                 {@link IResource#getPersistentProperty(QualifiedName)}
	 */
	public static boolean isExcluded(final IResource resource) {
		if (resource instanceof IFile && isDirectlyExcluded((IFile) resource)) {
			return true;
		} else if (resource instanceof IFolder && isDirectlyExcluded((IFolder) resource)) {
			return true;
		}

		IContainer parent = resource.getParent();
		while (parent != null && parent instanceof IFolder) {
			if (isDirectlyExcluded((IFolder) parent)) {
				return true;
			}

			parent = parent.getParent();
		}

		return false;
	}

	/**
	 * Splits the input string into a list of strings.
	 * 
	 * @param input
	 *                the input string to split.
	 * @param delimiter
	 *                the delimiter according which to split.
	 * @param escape
	 *                the escape character noting the delimiters not to be
	 *                used as delimiters.
	 * */
	// FIXME there is already an implementation for this.
	public static final java.util.List<String> intelligentSplit(final String input, final char delimiter, final char escape) {
		java.util.List<String> results = new ArrayList<String>();
		if (input == null || input.length() == 0) {
			return results;
		}
		StringBuilder tempResult = new StringBuilder();
		int i;
		// no over indexing is possible if the input was converted
		// correctly, as an escape must be escaping something
		char c;
		for (i = 0; i < input.length();) {
			c = input.charAt(i);
			if (escape == c) {
				// this is either a delimiter or an escape
				// character
				tempResult.append(c);
				i++;
			} else if (delimiter == c) {
				results.add(tempResult.toString());
				tempResult = new StringBuilder();
				i++;
			} else {
				tempResult.append(c);
				i++;
			}
		}
		results.add(tempResult.toString());
		return results;
	}
}
