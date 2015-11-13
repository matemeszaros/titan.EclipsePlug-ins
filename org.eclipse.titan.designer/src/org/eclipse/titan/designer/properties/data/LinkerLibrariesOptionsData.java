/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 * Handles the linker related property settings on resources. Also loading,
 * saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class LinkerLibrariesOptionsData {
	public static final String ADDITIONAL_OBJECTS_PROPERTY = "additionalObjects";
	public static final String ADDITIONAL_OBJECTS_TAG = "additionalObjects";
	public static final String LINKER_LIBRARIES_PROPERTY = "linkerLibraries";
	public static final String LINKER_LIBRARIES_TAG = "linkerLibraries";
	public static final String LINKER_LIBRARY_SEARCH_PATH_PROPERTY = "linkerLibrarySearchPath";
	public static final String LINKER_LIBRARY_SEARCH_PATH_TAG = "linkerLibrarySearchPath";
	public static final String DISABLE_EXTERNAL_DIRS_PROPERTY = "disablePredefinedExternalFolder";
	public static final String DISABLE_EXTERNAL_DIRS_TAG = "disablePredefinedExternalFolder";

	public static final String[] PROPERTIES = { ADDITIONAL_OBJECTS_PROPERTY, LINKER_LIBRARIES_PROPERTY, LINKER_LIBRARY_SEARCH_PATH_PROPERTY };
	public static final String[] TAGS = { ADDITIONAL_OBJECTS_TAG, LINKER_LIBRARIES_TAG, LINKER_LIBRARY_SEARCH_PATH_TAG };
	public static final String[] DEFAULT_VALUES = { "", "", "" };

	private LinkerLibrariesOptionsData() {
		// Do nothing
	}

	public static String[] getAdditionalObjects(final IProject project) {
		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.ADDITIONAL_OBJECTS_PROPERTY));
			return ListConverter.convertToList(temp);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting additional library objects of `" + project.getName() + "'", e);
			return new String[] {};
		}
	}

	public static String[] getLinkerLibraries(final IProject project) {
		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.LINKER_LIBRARIES_PROPERTY));
			return ListConverter.convertToList(temp);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting linker libraries of `" + project.getName() + "'", e);
			return new String[] {};
		}
	}

	public static String[] getLinkerSearchPaths(final IProject project) {
		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.LINKER_LIBRARY_SEARCH_PATH_PROPERTY));
			return ListConverter.convertToList(temp);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting linker search paths of `" + project.getName() + "'", e);
			return new String[] {};
		}
	}

	public static boolean getExternalFoldersDisabled(final IProject project) {
		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerLibrariesOptionsData.DISABLE_EXTERNAL_DIRS_PROPERTY));
			return "true".equals(temp) ? true : false;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting external folders of `" + project.getName() + "'", e);
			return false;
		}
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IProject project) {
		for (int i = 0; i < PROPERTIES.length; i++) {
			try {
				project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, PROPERTIES[i]), null);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(
						"While removing property `" + PROPERTIES[i] + "' of `" + project.getName() + "'", e);
			}
		}
	}

	/**
	 * Loads and sets the Makefile related settings contained in the XML
	 * tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * @see ProjectBuildPropertyData#loadMakefileSettings(Node, IProject,
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

		final String[] newValues = new String[TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, TAGS.length);
		String newDisableExternalDirsValue = "false";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node resource = resourceList.item(i);
			final String name = resource.getNodeName();
			for (int j = 0; j < TAGS.length; j++) {
				if (TAGS[j].equals(name)) {
					final NodeList subResources = resource.getChildNodes();
					final List<String> values = new ArrayList<String>();
					for (int i2 = 0; i2 < subResources.getLength(); i2++) {
						Node subResource = subResources.item(i2);
						if ("listItem".equals(subResource.getNodeName())) {
							values.add(subResource.getTextContent());
						}
					}

					newValues[j] = ListConverter.convertFromList(values.toArray(new String[values.size()]));
				}
			}

			if (DISABLE_EXTERNAL_DIRS_TAG.equals(name)) {
				newDisableExternalDirsValue = resourceList.item(i).getTextContent();
			}
		}

		for (int i = 0; i < TAGS.length; i++) {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, PROPERTIES[i]);
			try {
				final String oldValue = project.getPersistentProperty(qualifiedName);
				if (newValues[i] != null && !newValues[i].equals(oldValue)) {
					project.setPersistentProperty(qualifiedName, newValues[i]);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While loading tag `" + TAGS[i] + "' of `" + project.getName() + "'", e);
			}
		}

		try {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, DISABLE_EXTERNAL_DIRS_PROPERTY);
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newDisableExternalDirsValue != null && !newDisableExternalDirsValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newDisableExternalDirsValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While loading external folders of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Creates an XML tree from the Makefile related settings of the
	 * project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * @see ProjectBuildPropertyData#saveMakefileSettings(Document,
	 *      IProject)
	 * @see InternalMakefileCreationData#saveMakefileSettings(Element,
	 *      Document, IProject)
	 * 
	 * @param makefileSettings
	 *                the node to use as root node
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * */
	public static void saveMakefileSettings(final Element makefileSettings, final Document document, final IProject project) {
		for (int i = 0; i < TAGS.length; i++) {
			try {
				final String temp = project
						.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, PROPERTIES[i]));
				final String[] tempList = ListConverter.convertToList(temp);
				if (tempList.length > 0) {
					final Element list = document.createElement(TAGS[i]);
					makefileSettings.appendChild(list);

					for (int j = 0; j < tempList.length; j++) {
						if (tempList[j] != null && tempList[j].length() > 0) {
							final Element item = document.createElement("listItem");
							item.appendChild(document.createTextNode(tempList[j]));

							list.appendChild(item);
						}
					}
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While saving tag `" + TAGS[i] + "' of `" + project.getName() + "'", e);
			}
		}

		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					DISABLE_EXTERNAL_DIRS_PROPERTY));
			if (temp != null && !"false".equals(temp)) {
				Element element = document.createElement(DISABLE_EXTERNAL_DIRS_TAG);
				makefileSettings.appendChild(element);
				element.appendChild(document.createTextNode(temp));
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving external folders of `" + project.getName() + "'", e);
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

		final String[] newValues = new String[TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, TAGS.length);
		String newDisableExternalDirsValue = "false";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node resource = resourceList.item(i);
			final String name = resource.getNodeName();
			for (int j = 0; j < TAGS.length; j++) {
				if (TAGS[j].equals(name)) {
					final NodeList subResources = resource.getChildNodes();
					final List<String> values = new ArrayList<String>();
					for (int i2 = 0; i2 < subResources.getLength(); i2++) {
						final Node subResource = subResources.item(i2);
						if ("listItem".equals(subResource.getNodeName())) {
							values.add(subResource.getTextContent());
						}
					}

					newValues[j] = ListConverter.convertFromList(values.toArray(new String[values.size()]));
				}
			}

			if (DISABLE_EXTERNAL_DIRS_TAG.equals(name)) {
				newDisableExternalDirsValue = resourceList.item(i).getTextContent();
			}
		}

		for (int i = 0; i < TAGS.length; i++) {
			String temp = newValues[i];
			final String[] tempList = ListConverter.convertToList(temp);
			if (tempList.length > 0) {
				final Element list = document.createElement(TAGS[i]);
				makefileSettings.appendChild(list);

				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j] != null && tempList[j].length() > 0) {
						final Element item = document.createElement("listItem");
						item.appendChild(document.createTextNode(tempList[j]));

						list.appendChild(item);
					}
				}
			}
		}

		if (saveDefaultValues || (newDisableExternalDirsValue != null && !"false".equals(newDisableExternalDirsValue))) {
			final Element element = document.createElement(DISABLE_EXTERNAL_DIRS_TAG);
			makefileSettings.appendChild(element);
			element.appendChild(document.createTextNode(newDisableExternalDirsValue));
		}
	}
}
