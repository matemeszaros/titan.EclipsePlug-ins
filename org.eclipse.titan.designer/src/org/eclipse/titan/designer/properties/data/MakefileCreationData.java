/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.io.File;
import java.util.HashSet;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.properties.pages.ProjectBuildPropertyPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles all of the property data that is used by Titan's command line
 * makefile generator, to generate makefile. The Designer's internal makefile
 * generator also uses these properties, and much more.
 * 
 * Also loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class MakefileCreationData {
	public static final String USE_ABSOLUTEPATH_PROPERTY = "useAbsolutePath";
	public static final String GNU_MAKE_PROPERTY = "GNUMake";
	public static final String INCREMENTAL_DEPENDENCY_PROPERTY = "IncrementalDependency";
	public static final String DYNAMIC_LINKING_PROPERTY = "DynamicLinkingProperty";
	public static final String FUNCTIONTESTRUNTIME_PROPERTY = "Function test runtime";
	public static final String SINGLEMODE_PROPERTY = "singleMode";
	public static final String TARGET_EXECUTABLE_PROPERTY = "targetExecutable";
	public static final String CODE_SPLITTING_PROPERTY = "codeSplitting";
	public static final String DEFAULT_TARGET_PROPERTY = "defaultTarget";

	public static final boolean USE_ABSOLUTEPATH_DEFAULT_VALUE       = false;
	public static final boolean GNU_MAKE_DEFAULT_VALUE               = true;
	public static final boolean INCREMENTAL_DEPENDENCY_DEFAULT_VALUE = true;
	public static final boolean DYNAMIC_LINKING_DEFAULT_VALUE        = false;
	public static final boolean FUNCTIONTESTRUNTIME_DEFAULT_VALUE    = false;
	public static final boolean SINGLEMODE_DEFAULT_VALUE	         = false;
	public static final String  CODE_SPLITTING_DEFAULT_VALUE         = GeneralConstants.NONE;
	public static final String  DEFAULT_TARGET_DEFAULT_VALUE         = DefaultTarget.getDefault().toString();

	// makefile properties to be saved into the XML
	public static final String[] MAKEFILE_PROPERTIES = new String[] { USE_ABSOLUTEPATH_PROPERTY, GNU_MAKE_PROPERTY,
			INCREMENTAL_DEPENDENCY_PROPERTY, DYNAMIC_LINKING_PROPERTY, FUNCTIONTESTRUNTIME_PROPERTY, SINGLEMODE_PROPERTY,
			CODE_SPLITTING_PROPERTY, DEFAULT_TARGET_PROPERTY };

	// XML tag names corresponding to the makefile properties
	public static final String[] MAKEFILE_TAGS = new String[] { "useAbsolutePath", "GNUMake", "incrementalDependencyRefresh", "dynamicLinking",
			"functiontestRuntime", "singleMode", "codeSplitting", "defaultTarget" };

	private static final String[] DEFAULT_VALUES = new String[] { String.valueOf(USE_ABSOLUTEPATH_DEFAULT_VALUE),
			String.valueOf(GNU_MAKE_DEFAULT_VALUE), String.valueOf(INCREMENTAL_DEPENDENCY_DEFAULT_VALUE),
			String.valueOf(DYNAMIC_LINKING_DEFAULT_VALUE), String.valueOf(FUNCTIONTESTRUNTIME_DEFAULT_VALUE),
			String.valueOf(SINGLEMODE_DEFAULT_VALUE), CODE_SPLITTING_DEFAULT_VALUE, DEFAULT_TARGET_DEFAULT_VALUE };

	public enum DefaultTarget {
		EXECUTABLE, LIBRARY;

		public static DefaultTarget getDefault() {
			return EXECUTABLE;
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		/**
		 * This works like {@link #valueOf(String)} except it is case
		 * insensitive. This and {@link #toString()} should be kept
		 * consistent.
		 */
		public static DefaultTarget createInstance(final String str) {
			return DefaultTarget.valueOf(str.toUpperCase());
		}

		public static String[][] getDisplayNamesAndValues() {
			return new String[][] { { "Executable", DefaultTarget.EXECUTABLE.toString() },
					{ "Library", DefaultTarget.LIBRARY.toString() } };
		}
	}

	private MakefileCreationData() {
		// Do nothing
	}

	public static void removeTITANAttributes(final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, USE_ABSOLUTEPATH_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, GNU_MAKE_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, INCREMENTAL_DEPENDENCY_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, DYNAMIC_LINKING_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, FUNCTIONTESTRUNTIME_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, SINGLEMODE_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, CODE_SPLITTING_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, DEFAULT_TARGET_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TARGET_EXECUTABLE_PROPERTY), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Loads and sets the Makefile related settings contained in the XML
	 * tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * @see ProjectBuildPropertyPage#loadMakefileSettings(Node, IProject,
	 *      HashSet)
	 * 
	 * @param root
	 *                the root of the subtree containing Makefile related
	 *                attributes
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadMakefileSettings(final Node root, final IProject project) {
		final NodeList resourceList = root.getChildNodes();

		final String[] newValues = new String[MAKEFILE_TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, MAKEFILE_TAGS.length);

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < MAKEFILE_TAGS.length; j++) {
				if (MAKEFILE_TAGS[j].equals(name)) {
					newValues[j] = node.getTextContent();
				}
			}

			if ("targetExecutable".equals(name)) {
				String temp = node.getTextContent();
				if (temp == null || temp.length() == 0) {
					temp = "bin/" + project.getName();
				}

				try {
					project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
							TARGET_EXECUTABLE_PROPERTY), temp);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While loading targetExecutable attribute of `" + project.getName()
							+ "'", e);
				}
			}
		}

		for (int i = 0; i < MAKEFILE_TAGS.length; i++) {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MAKEFILE_PROPERTIES[i]);
			try {
				final String oldValue = project.getPersistentProperty(qualifiedName);
				if (newValues[i] != null && !newValues[i].equals(oldValue)) {
					project.setPersistentProperty(qualifiedName, newValues[i]);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(
						"While loading attribute `" + MAKEFILE_PROPERTIES[i] + "' of `" + project.getName() + "'", e);
			}
		}
	}

	/**
	 * Creates an XML tree from the Makefile related settings of the
	 * project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * @see ProjectBuildPropertyPage#saveMakefileSettings(Document,
	 *      IProject)
	 * 
	 * @param makefileSettings
	 *                the node to use as root node
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * */
	public static void saveMakefileSettings(final Element makefileSettings, final Document document, final IProject project) {
		Element node;
		String temp = null;
		for (int i = 0; i < MAKEFILE_PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MAKEFILE_PROPERTIES[i]));
				if (temp != null && !DEFAULT_VALUES[i].equals(temp)) {
					node = document.createElement(MAKEFILE_TAGS[i]);
					node.appendChild(document.createTextNode(temp));
					makefileSettings.appendChild(node);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(
						"While saving attribute `" + MAKEFILE_PROPERTIES[i] + "' of `" + project.getName() + "'", e);
			}
		}
		// save the properties of the target executable
		node = saveMakefileExecutableSettings(document, project);
		if (node != null) {
			makefileSettings.appendChild(node);
		}
	}

	/**
	 * Creates an XML tree from the name of the executable of the local
	 * build related settings of the project.
	 * 
	 * @see ProjectBuildPropertyPage#saveMakefileSettings(Document,
	 *      IProject)
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	private static Element saveMakefileExecutableSettings(final Document document, final IProject project) {
		String temp;
		Element node = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TARGET_EXECUTABLE_PROPERTY));
			if (temp == null) {
				temp = "bin/" + project.getName();
			}

			node = document.createElement("targetExecutable");
			node.appendChild(document.createTextNode(temp));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving executable settings of `" + project.getName() + "'", e);
		}

		return node;
	}

	/**
	 * Copies the project information related to Makefiles from the source
	 * node to the target makefileSettings node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param source
	 *                the node used as the source of the information.
	 * @param makefileSettings
	 *                the node used as the target of the operation.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * */
	public static void copyMakefileSettings(final Node source, final Node makefileSettings, final Document document,
			final boolean saveDefaultValues) {
		final NodeList resourceList = source.getChildNodes();

		final String[] newValues = new String[MAKEFILE_TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, MAKEFILE_TAGS.length);
		String targetExecutable = "";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < MAKEFILE_TAGS.length; j++) {
				if (MAKEFILE_TAGS[j].equals(name)) {
					newValues[j] = node.getTextContent();
				}
			}

			if ("targetExecutable".equals(name)) {
				targetExecutable = node.getTextContent();
			}
		}

		for (int i = 0; i < MAKEFILE_PROPERTIES.length; i++) {
			final String temp = newValues[i];
			if (saveDefaultValues || (temp != null && !DEFAULT_VALUES[i].equals(temp))) {
				final Element node = document.createElement(MAKEFILE_TAGS[i]);
				node.appendChild(document.createTextNode(temp));
				makefileSettings.appendChild(node);
			}
		}
		// save the properties of the target executable
		final Element node = document.createElement("targetExecutable");
		node.appendChild(document.createTextNode(targetExecutable));
		makefileSettings.appendChild(node);
	}

	public static String getDefaultTargetExecutableName(final IProject project) {
		return getDefaultTargetExecutableName(project, false);
	}

	public static String getDefaultTargetExecutableName(final IProject project, boolean isAbsolute) {
		StringBuilder name = new StringBuilder();

		//Absolute part of path
		if (isAbsolute) {
			try {
				String location = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY));

				if (location != null) {
					name.append(location);
					name.append(File.separator);
				} else {
					TITANDebugConsole.println("Unable to get project location, returning relative path to executable");
				}
			}
			catch(CoreException ce) {
				ce.printStackTrace();
				TITANDebugConsole.println("Exception while determining project location, returning relative path to executable");
			}
		}

		// Relative part of path
		name.append("bin");
		name.append(File.separator);
		name.append(project.getName().replace(' ', '_'));

		// Append ".exe" on windows platform
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			name.append(".exe");
		}
		return name.toString();
	}
}
