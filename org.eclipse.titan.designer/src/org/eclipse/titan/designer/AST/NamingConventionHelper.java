/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.FolderNamingConventionPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectNamingConventionPropertyData;

/**
 * @author Kristof Szabados
 * */
public final class NamingConventionHelper {
	private static final String INCORRECTWORKSPACELEVEL = "At least one of the naming convention checking regular expressions"
			+ " set on workspace level is incorrect, please correct it.";
	private static final Pattern VISIBILITY_PATTERN = Pattern.compile(".*(?:public|private|friend).*");

	private static final QualifiedName FOLDER_SPECIFIC_CONVENTIONS_ENABLED = new QualifiedName(FolderNamingConventionPropertyData.QUALIFIER,
			PreferenceConstants.ENABLEFOLDERSPECIFICNAMINGCONVENTIONS);
	private static final QualifiedName PROJECT_SPECIFIC_CONVENTIONS_ENABLED = new QualifiedName(ProjectNamingConventionPropertyData.QUALIFIER,
			PreferenceConstants.ENABLEPROJECTSPECIFICNAMINGCONVENTIONS);

	/**
	 * Cache variables holding the location checked last, and the resource
	 * on which the naming convention to be used was found. As there is
	 * usually more than 1 definition in a file, this might save lots of
	 * upward going tree traversal.
	 */
	private static IResource lastSource = null;
	private static IResource lastResult = null;

	// The actual severity to be used for reporting naming convention
	// related problems.
	private static String namingSeverity;
	private static String containsModuleNameSeverity;
	private static String containsVisibilitySeverity;

	// true if naming convention related problems should not be reported.
	private static boolean silentMode = true;

	static {
		namingSeverity = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS, GeneralConstants.WARNING, null);
		silentMode = GeneralConstants.IGNORE.equals(namingSeverity);
		containsModuleNameSeverity = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION, GeneralConstants.IGNORE, null);
		containsVisibilitySeverity = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION, GeneralConstants.IGNORE, null);

		final Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					final String property = event.getProperty();
					if (PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS.equals(property)) {
						namingSeverity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS, GeneralConstants.WARNING, null);
						silentMode = GeneralConstants.IGNORE.equals(namingSeverity);
					} else if (PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION.equals(property)) {
						containsModuleNameSeverity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION, GeneralConstants.IGNORE, null);
					} else if (PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION.equals(property)) {
						containsVisibilitySeverity = Platform.getPreferencesService().getString(
								ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION, GeneralConstants.IGNORE, null);
					}
				}
			});
		}
	}

	private NamingConventionHelper() {
	}

	public static void clearCaches() {
		lastSource = null;
		lastResult = null;
	}

	/**
	 * Tries to find out the pattern string to use.
	 * 
	 * @param preference
	 *                the preference key of the naming convention used for
	 *                the check.
	 * @param location
	 *                the location to start searching from.
	 * 
	 * @return the found pattern string or null if it can not be determined.
	 * */
	private static String getPatternText(final String preference, final Location location) {
		IResource resource = location.getFile();
		if (lastSource == resource) {
			resource = lastResult;
		} else {
			lastSource = resource;
			lastResult = null;
		}

		while (resource != null) {
			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;

				try {
					if ("true".equals(folder.getPersistentProperty(FOLDER_SPECIFIC_CONVENTIONS_ENABLED))) {
						lastResult = resource;
						return folder.getPersistentProperty(new QualifiedName(FolderNamingConventionPropertyData.QUALIFIER,
								preference));
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
					return null;
				}
			} else if (resource instanceof IProject) {
				IProject project = (IProject) resource;

				try {
					if ("true".equals(project.getPersistentProperty(PROJECT_SPECIFIC_CONVENTIONS_ENABLED))) {
						lastResult = resource;
						return project.getPersistentProperty(new QualifiedName(ProjectNamingConventionPropertyData.QUALIFIER,
								preference));
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
					return null;
				}
			} else if (resource instanceof IWorkspaceRoot) {
				lastResult = resource;
				return Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER, preference, ".", null);
			}

			resource = resource.getParent();
		}

		return null;
	}

	/**
	 * Report an error naming the location where the naming convention
	 * regular expressions are coming from.
	 * 
	 * @param location
	 *                the location to start out searching from.
	 * */
	private static void reportErrorOnPatternLocation(final Location location) {
		IResource resource = location.getFile();
		if (lastSource == resource) {
			resource = lastResult;
		} else {
			lastSource = resource;
			lastResult = null;
		}

		while (resource != null) {
			if (resource instanceof IFolder) {
				final IFolder folder = (IFolder) resource;

				try {
					if ("true".equals(folder.getPersistentProperty(FOLDER_SPECIFIC_CONVENTIONS_ENABLED))) {
						lastResult = resource;
						ErrorReporter.logError("At least one of the naming convention checking regular expressions set on folder "
								+ folder.getName() + " is incorrect, please correct it.");
						return;
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
					return;
				}
			} else if (resource instanceof IProject) {
				IProject project = (IProject) resource;

				try {
					if ("true".equals(project.getPersistentProperty(PROJECT_SPECIFIC_CONVENTIONS_ENABLED))) {
						lastResult = resource;
						ErrorReporter.logError("At least one of the naming convention checking regular expressions set on project "
								+ project.getName() + " is incorrect, please correct it.");
						return;
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
					return;
				}
			} else if (resource instanceof IWorkspaceRoot) {
				lastResult = resource;
				ErrorReporter.logError(INCORRECTWORKSPACELEVEL);
				return;
			}

			resource = resource.getParent();
		}

		return;
	}

	/**
	 * Checks whether an identifier is breaking a naming convention rule.
	 * 
	 * @param preference
	 *                the preference key of the naming convention used for
	 *                the check.
	 * @param identifier
	 *                the identifier check.
	 * @param checkedItem
	 *                the item being checked, used in error messages.
	 */
	public static void checkConvention(final String preference, final Identifier identifier, final Assignment checkedItem) {
		if (silentMode) {
			return;
		}

		final Location identifierLocation = identifier.getLocation();
		final String patternText = getPatternText(preference, identifierLocation);
		if (patternText == null) {
			return;
		}

		if (internalCheckConvention(preference, identifier, patternText)) {
			final String message = MessageFormat.format("The {0} with name {1} breaks the naming convention  `{2}''",
					checkedItem.getDescription(), identifier.getDisplayName(), patternText);
			identifierLocation.reportConfigurableSemanticProblem(namingSeverity, message);
		}
	}

	/**
	 * Checks whether an identifier is breaking a naming convention rule.
	 * 
	 * @param preference
	 *                the preference key of the naming convention used for
	 *                the check.
	 * @param identifier
	 *                the identifier check.
	 * @param description
	 *                the description of the source of the identifier, used
	 *                in error messages.
	 */
	public static void checkConvention(final String preference, final Identifier identifier, final String description) {
		if (silentMode) {
			return;
		}

		final Location identifierLocation = identifier.getLocation();
		final String patternText = getPatternText(preference, identifierLocation);
		if (patternText == null) {
			return;
		}

		if (internalCheckConvention(preference, identifier, patternText)) {
			identifierLocation.reportConfigurableSemanticProblem(
					namingSeverity,
					MessageFormat.format("The {0} with name {1} breaks the naming convention  `{2}''", description,
							identifier.getDisplayName(), patternText));
		}
	}

	/**
	 * Checks whether an identifier is breaking a naming convention rule.
	 * 
	 * @param preference
	 *                the preference key of the naming convention used for
	 *                the check.
	 * @param identifier
	 *                the identifier check.
	 * @param patternText
	 *                the text to be used as the pattern.
	 * @return true if miss-match was found, otherwise false.
	 */
	private static boolean internalCheckConvention(final String preference, final Identifier identifier, final String patternText) {
		if (patternText == null) {
			return false;
		}

		Pattern pattern;

		try {
			pattern = Pattern.compile(patternText);
		} catch (PatternSyntaxException e) {
			reportErrorOnPatternLocation(identifier.getLocation());
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		final Matcher matcher = pattern.matcher(identifier.getDisplayName());

		if (!matcher.matches()) {
			return true;
		}

		return false;
	}

	/**
	 * Checks whether an identifier contains either the moduleID or a
	 * visibility attribute.
	 * 
	 * @param identifier
	 *                the identifier check.
	 * @param moduleID
	 *                the module identifier to check against
	 * @param description
	 *                the description of the source of the identifier, used
	 *                in error messages.
	 */
	public static void checkNameContents(final Identifier identifier, final Identifier moduleID, final String description) {
		final String displayName = identifier.getDisplayName();
		if (displayName.contains(moduleID.getDisplayName())) {
			final Location identifierLocation = identifier.getLocation();
			identifierLocation.reportConfigurableSemanticProblem(containsModuleNameSeverity, MessageFormat.format(
					"The name {1} of the {0} contains the module name {2} it is located in", description, displayName,
					moduleID.getDisplayName()));
		}
		if (VISIBILITY_PATTERN.matcher(displayName).matches()) {
			final Location identifierLocation = identifier.getLocation();
			identifierLocation.reportConfigurableSemanticProblem(containsVisibilitySeverity,
					MessageFormat.format("The name {1} of the {0} contains visibility attributes", description, displayName));
		}
	}
}
