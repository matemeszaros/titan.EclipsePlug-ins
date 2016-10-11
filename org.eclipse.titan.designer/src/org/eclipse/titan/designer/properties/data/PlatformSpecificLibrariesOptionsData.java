/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
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
 * Handles the platform specific library related property settings of projects.
 * Also loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class PlatformSpecificLibrariesOptionsData {
	public static final String SPECIFIC_LIBRARIES_PROPERTY = "SpecificLibrariesProperty";
	public static final String SPECIFIC_LIBRARIES_TAG = "SpecificLibraries";

	private PlatformSpecificLibrariesOptionsData() {
		// Do nothing
	}

	public static String[] getPlatformSpecificLibraries(final IProject project, final String platform) {
		try {
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform
					+ PlatformSpecificLibrariesOptionsData.SPECIFIC_LIBRARIES_PROPERTY));
			return ListConverter.convertToList(temp);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting libraries of `" + project.getName() + "' for platform " + platform, e);
			return new String[] {};
		}
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param platform
	 *                the platform to set the libraries for.
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final String platform, final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform + SPECIFIC_LIBRARIES_PROPERTY),
					null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "' for platform " + platform, e);
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
	 * @param platform
	 *                the platform to load the libraries for.
	 * @param root
	 *                the root of the subtree containing Makefile related
	 *                attributes
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadMakefileSettings(final String platform, final Node root, final IProject project) {
		final NodeList resourceList = root.getChildNodes();

		String newValue = "";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node resource = resourceList.item(i);
			final String name = resource.getNodeName();
			if ((platform + SPECIFIC_LIBRARIES_TAG).equals(name)) {
				NodeList subResources = resource.getChildNodes();
				List<String> values = new ArrayList<String>();
				for (int i2 = 0; i2 < subResources.getLength(); i2++) {
					Node subResource = subResources.item(i2);
					if ("listItem".equals(subResource.getNodeName())) {
						values.add(subResource.getTextContent());
					}
				}

				newValue = ListConverter.convertFromList(values.toArray(new String[values.size()]));
			}
		}

		try {
			final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform
					+ SPECIFIC_LIBRARIES_PROPERTY);
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While loading settings of `" + project.getName() + "' for platform " + platform, e);
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
	 * @param platform
	 *                the platform whose settings should be saved here.
	 * @param makefileSettings
	 *                the node to use as root node
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * */
	public static void saveMakefileSettings(final String platform, final Element makefileSettings, final Document document, final IProject project) {
		try {
			// TODO check if this is needed when there are no
			// elements
			final Element list = document.createElement(platform + SPECIFIC_LIBRARIES_TAG);
			final String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, platform
					+ SPECIFIC_LIBRARIES_PROPERTY));
			final String[] tempList = ListConverter.convertToList(temp);
			if (tempList.length > 0) {
				makefileSettings.appendChild(list);
				for (int j = 0; j < tempList.length; j++) {
					if (tempList[j] != null && tempList[j].length() > 0) {
						Element item = document.createElement("listItem");
						item.appendChild(document.createTextNode(tempList[j]));
						list.appendChild(item);
					}
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While saving attributes of `" + project.getName() + "' for platform " + platform, e);
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
	public static void copyMakefileSettings(final String platform, final Node source, final Node makefileSettings, final Document document,
			final boolean saveDefaultValues) {
		final NodeList resourceList = source.getChildNodes();

		String newValue = "";

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node resource = resourceList.item(i);
			final String name = resource.getNodeName();
			if ((platform + SPECIFIC_LIBRARIES_TAG).equals(name)) {
				NodeList subResources = resource.getChildNodes();
				List<String> values = new ArrayList<String>();
				for (int i2 = 0; i2 < subResources.getLength(); i2++) {
					Node subResource = subResources.item(i2);
					if ("listItem".equals(subResource.getNodeName())) {
						values.add(subResource.getTextContent());
					}
				}

				newValue = ListConverter.convertFromList(values.toArray(new String[values.size()]));
			}
		}

		if (!saveDefaultValues && "".equals(newValue)) {
			return;
		}

		final Element list = document.createElement(platform + SPECIFIC_LIBRARIES_TAG);
		final String[] tempList = ListConverter.convertToList(newValue);
		if (tempList.length > 0) {
			makefileSettings.appendChild(list);
			for (int j = 0; j < tempList.length; j++) {
				if (saveDefaultValues || (tempList[j] != null && tempList[j].length() > 0)) {
					Element item = document.createElement("listItem");
					item.appendChild(document.createTextNode(tempList[j]));
					list.appendChild(item);
				}
			}
		}
	}
}
