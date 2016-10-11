/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the property settings that drives the build environment (currently
 * make). Also loading, saving of these properties from and into external
 * formats.
 * 
 * @author Kristof Szabados
 * */
public final class MakeAttributesData {
	public static final String LOCALBUILDSETTINGS = "LocalBuildSettings";

	public static final String BUILD_LEVEL_0 = "Level 0 - Semantic Check";
	public static final String BUILD_LEVEL_1 = "Level 1 - TTCN3 -> C++ compilation";
	public static final String BUILD_LEVEL_2 = "Level 2 - Creating object files";
	public static final String BUILD_LEVEL_2_5 = "Level 2.5 - Creating object files with heuristical dependency update";
	public static final String BUILD_LEVEL_3 = "Level 3 - Creating object files with dependency update";
	public static final String BUILD_LEVEL_4 = "Level 4 - Creating Executable Test Suite";
	public static final String BUILD_LEVEL_4_5 = "Level 4.5 - Creating Executable Test Suite with heuristical dependency update";
	public static final String BUILD_LEVEL_5 = "Level 5 - Creating Executable Test Suite with dependency update";

	public static final String TEMPORAL_MAKEFILE_SCRIPT_PROPERTY = "makefileUpdateScript";
	public static final String TEMPORAL_MAKEFILE_FLAGS_PROPERTY = "makefileFlags";
	public static final String TEMPORAL_WORKINGDIRECTORY_PROPERTY = "workingDir";
	public static final String BUILD_LEVEL_PROPERTY = "buildLevel";

	// makefile properties to be saved into the XML
	private static final String[] MAKEFILE_PROPERTIES = new String[] { BUILD_LEVEL_PROPERTY };

	// XML tag names corresponding to the makefile properties
	private static final String[] MAKEFILE_TAGS = new String[] { "buildLevel" };
	private static final String[] DEFAULT_VALUES = { BUILD_LEVEL_5 };

	private MakeAttributesData() {
		// Do nothing
	}

	/**
	 * gets the build level and maps it for the form defined in constants BUILD_LEVEL_xx 
	 * Checks only the level N part and does not bother with the rest. Corrects the missing spaces.
	 * If s null returns MakeAttributesData.BUILD_LEVEL_5
	 * @param s the original build level string
	 * @return the corrected build level string
	 */
	public static String getBuildLevel(final String s) {
		if (s == null) {
			return MakeAttributesData.BUILD_LEVEL_5;
		}
		// get build level for 2.5, 4.5 etc ;
		String s1 = s.replace(" ", "");
		if (s1.startsWith("Level2.5")) {
			return MakeAttributesData.BUILD_LEVEL_2_5;
		}
		if (s1.startsWith("Level4.5")) {
			return MakeAttributesData.BUILD_LEVEL_4_5;
		}
		if (s1.startsWith("Level0")) {
			return MakeAttributesData.BUILD_LEVEL_0;
		}
		if (s1.startsWith("Level1")) {
			return MakeAttributesData.BUILD_LEVEL_1;
		}
		if (s1.startsWith("Level2")) {
			return MakeAttributesData.BUILD_LEVEL_2;
		}
		if (s1.startsWith("Level3")) {
			return MakeAttributesData.BUILD_LEVEL_3;
		}
		if (s1.startsWith("Level4")) {
			return MakeAttributesData.BUILD_LEVEL_4;
		}
		if (s1.startsWith("Level5")) {
			return MakeAttributesData.BUILD_LEVEL_5;
		}

		if (s1.startsWith("2.5")) {
			return MakeAttributesData.BUILD_LEVEL_2_5;
		}
		if (s1.startsWith("4.5")) {
			return MakeAttributesData.BUILD_LEVEL_4_5;
		}
		if (s1.startsWith("0")) {
			return MakeAttributesData.BUILD_LEVEL_0;
		}
		if (s1.startsWith("1")) {
			return MakeAttributesData.BUILD_LEVEL_1;
		}
		if (s1.startsWith("2")) {
			return MakeAttributesData.BUILD_LEVEL_2;
		}
		if (s1.startsWith("3")) {
			return MakeAttributesData.BUILD_LEVEL_3;
		}
		if (s1.startsWith("4")) {
			return MakeAttributesData.BUILD_LEVEL_4;
		}
		if (s1.startsWith("5")) {
			return MakeAttributesData.BUILD_LEVEL_5;
		}
		return MakeAttributesData.BUILD_LEVEL_5;
	}

	public static void removeTITANAttributes(final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, BUILD_LEVEL_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_SCRIPT_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_FLAGS_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_WORKINGDIRECTORY_PROPERTY), null);
		} catch (CoreException ce) {
			ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "'", ce);
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
		}

		for (int i = 0; i < MAKEFILE_TAGS.length; i++) {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MAKEFILE_PROPERTIES[i]);
			try {
				final String oldValue = project.getPersistentProperty(qualifiedName);
				if (newValues[i] != null && !newValues[i].equals(oldValue)) {
					project.setPersistentProperty(qualifiedName, newValues[i]);
				}
			} catch (CoreException ce) {
				ErrorReporter.logExceptionStackTrace("While setting the property `" + MAKEFILE_PROPERTIES[i] + "' on project `"
						+ project.getName() + "'", ce);
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
			} catch (CoreException ce) {
				ErrorReporter.logExceptionStackTrace("While saving the property `" + MAKEFILE_PROPERTIES[i] + "' of project `"
						+ project.getName() + "'", ce);
			}
		}
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

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < MAKEFILE_TAGS.length; j++) {
				if (MAKEFILE_TAGS[j].equals(name)) {
					newValues[j] = node.getTextContent();
				}
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
	}

	/**
	 * Creates an XML tree from the local build related settings of the
	 * project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * @see ProjectBuildPropertyPage#saveLocalBuildSettings(Document,
	 *      IProject)
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	public static Element saveLocalBuildSettings(final Document document, final IProject project) {
		Element localBuildSettings = document.createElement(LOCALBUILDSETTINGS);

		// Makefile updater script
		Element node = saveLocalBuildUpdaterScriptSettings(document, project);
		if (node != null) {
			localBuildSettings.appendChild(node);
		}

		// Makefile flags settings
		node = saveLocalBuildMakefileFlagsSettings(document, project);
		if (node != null) {
			localBuildSettings.appendChild(node);
		}

		// workingdir
		node = saveLocalBuildWorkingDirectorySettings(document, project);
		localBuildSettings.appendChild(node);

		return localBuildSettings;
	}

	/**
	 * Creates an XML tree from the makefile updater script of the local
	 * build related settings of the project.
	 * 
	 * @see ProjectBuildPropertyPage#saveLocalBuildSettings()
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	private static Element saveLocalBuildUpdaterScriptSettings(final Document document, final IProject project) {
		String temp = null;
		Element node = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_SCRIPT_PROPERTY));
			if (temp == null || "".equals(temp)) {
				temp = "";
			}

			node = document.createElement("MakefileScript");
			node.appendChild(document.createTextNode(temp));

		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving build updater settings of `" + project.getName() + "'", e);
		}

		return node;
	}

	/**
	 * Creates an XML tree from the makefile flags of the local build
	 * related settings of the project.
	 * 
	 * @see ProjectBuildPropertyPage#saveLocalBuildSettings()
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	private static Element saveLocalBuildMakefileFlagsSettings(final Document document, final IProject project) {
		String temp = null;
		Element node = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_FLAGS_PROPERTY));
			if (temp != null && !"".equals(temp)) {
				node = document.createElement("MakefileFlags");
				node.appendChild(document.createTextNode(temp));
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving makefile settings of `" + project.getName() + "'", e);
		}

		return node;
	}

	/**
	 * Creates an XML tree from the working directory of the local build
	 * related settings of the project.
	 * 
	 * @see ProjectBuildPropertyPage#saveLocalBuildSettings()
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	private static Element saveLocalBuildWorkingDirectorySettings(final Document document, final IProject project) {
		String temp = null;
		Element node = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_WORKINGDIRECTORY_PROPERTY));
			if (temp == null) {
				temp = "bin";
			}

			node = document.createElement("workingDirectory");
			node.appendChild(document.createTextNode(temp));

		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving working directory settings of `" + project.getName() + "'", e);
		}

		return node;
	}

	/**
	 * Loads and sets the local build related settings contained in the XML
	 * tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * @see ProjectBuildPropertyPage#loadLocalBuildSettings(Node, IProject,
	 *      HashSet)
	 * 
	 * @param root
	 *                the root of the subtree containing Makefile related
	 *                attributes
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadLocalBuildSettings(final Node root, final IProject project) {
		final NodeList resourceList = root.getChildNodes();

		String newWorkingDirectory = "bin";
		String newMakefileFlags = "";
		String newMakefileUpdaterScript = "";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String nodeName = node.getNodeName();

			if ("workingDirectory".equals(nodeName)) {
				newWorkingDirectory = node.getTextContent();
			} else if ("MakefileScript".equals(nodeName)) {
				newMakefileUpdaterScript = node.getTextContent();
			} else if ("MakefileFlags".equals(nodeName)) {
				newMakefileFlags = node.getTextContent();
			}
		}

		// set the working directory
		QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_WORKINGDIRECTORY_PROPERTY);
		try {
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newWorkingDirectory != null && !newWorkingDirectory.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newWorkingDirectory);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting working directory settings of `" + project.getName() + "'", e);
		}

		// set the makefile updater script
		qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_SCRIPT_PROPERTY);
		try {
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newMakefileUpdaterScript != null && !newMakefileUpdaterScript.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newMakefileUpdaterScript);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting updater script settings of `" + project.getName() + "'",e);
		}

		// set the makefile flags
		qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, TEMPORAL_MAKEFILE_FLAGS_PROPERTY);
		try {
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newMakefileFlags != null && !newMakefileFlags.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newMakefileFlags);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting updater flag settings of `" + project.getName() + "'",e);
		}
	}

	/**
	 * Copies the project information related to local build settings from
	 * the source node to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param source
	 *                the node used as the source of the information.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Element copyLocalBuildSettings(final Node source, final Document document, final boolean saveDefaultValues) {
		final NodeList resourceList = source.getChildNodes();

		String newWorkingDirectory = "bin";
		String newMakefileFlags = "";
		String newMakefileUpdaterScript = "";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String nodeName = node.getNodeName();

			if ("workingDirectory".equals(nodeName)) {
				newWorkingDirectory = node.getTextContent();
			} else if ("MakefileScript".equals(nodeName)) {
				newMakefileUpdaterScript = node.getTextContent();
			} else if ("MakefileFlags".equals(nodeName)) {
				newMakefileFlags = node.getTextContent();
			}
		}

		final Element localBuildSettings = document.createElement(LOCALBUILDSETTINGS);
		Node node;

		// Makefile flags settings
		if (saveDefaultValues || newMakefileFlags.length() > 0) {
			node = document.createElement("MakefileFlags");
			node.appendChild(document.createTextNode(newMakefileFlags));
			localBuildSettings.appendChild(node);
		}

		// Makefile updater script
		if (saveDefaultValues || newMakefileUpdaterScript.length() > 0) {
			node = document.createElement("MakefileScript");
			node.appendChild(document.createTextNode(newMakefileUpdaterScript));
			localBuildSettings.appendChild(node);
		}

		// workingdir
		node = document.createElement("workingDirectory");
		node.appendChild(document.createTextNode(newWorkingDirectory));
		localBuildSettings.appendChild(node);

		return localBuildSettings;
	}
}
