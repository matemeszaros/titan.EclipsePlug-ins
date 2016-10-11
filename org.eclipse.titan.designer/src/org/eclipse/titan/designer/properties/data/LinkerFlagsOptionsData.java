/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.HashSet;
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
 * Handles the property settings of projects related to the options linker should
 * be called with. Also loading, saving of these properties from and into
 * external formats.
 * 
 * @author Jeno Balasko
 * */
public final class LinkerFlagsOptionsData {
	public static final String USE_GOLD_LINKER_PROPERTY = "useGoldLinker";
	public static final String FREE_TEXT_OPTIONS_PROPERTY = "freeTextLinkerOptions";

	public static final String[] PROPERTIES = { USE_GOLD_LINKER_PROPERTY, FREE_TEXT_OPTIONS_PROPERTY };
	public static final String[] TAGS = PROPERTIES;
	public static final String[] DEFAULT_VALUES = {"false", ""};

	private LinkerFlagsOptionsData() {
		// Do nothing
	}

	public static String getLinkerFlags(final IProject project) {
		final StringBuilder builder = new StringBuilder(30);
		String temp;

		try {
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerFlagsOptionsData.USE_GOLD_LINKER_PROPERTY));
			builder.append("true".equals(temp) ? "-fuse-ld=gold " : "");
			
			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					LinkerFlagsOptionsData.FREE_TEXT_OPTIONS_PROPERTY));
			builder.append(temp);
			
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting Linker options of `" + project.getName() + "'", e);
		}

		return builder.toString();
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
				ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "'", e);
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
		NodeList resourceList = root.getChildNodes();

		final String[] newValues = new String[TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, TAGS.length);

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final String name = resourceList.item(i).getNodeName();
			for (int j = 0; j < TAGS.length; j++) {
				if (TAGS[j].equals(name)) {
					newValues[j] = resourceList.item(i).getTextContent();
				}
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
				if (temp != null && !DEFAULT_VALUES[i].equals(temp)) {
					final Element element = document.createElement(TAGS[i]);
					makefileSettings.appendChild(element);
					element.appendChild(document.createTextNode(temp));
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While saving tag `" + TAGS[i] + "' of `" + project.getName() + "'", e);
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
		final String[] newValues = new String[TAGS.length];
		System.arraycopy(DEFAULT_VALUES, 0, newValues, 0, TAGS.length);

		if (source != null) {
			final NodeList resourceList = source.getChildNodes();

			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final String name = resourceList.item(i).getNodeName();
				for (int j = 0; j < TAGS.length; j++) {
					if (TAGS[j].equals(name)) {
						newValues[j] = resourceList.item(i).getTextContent();
					}
				}
			}
		}

		for (int i = 0; i < TAGS.length; i++) {
			final String temp = newValues[i];
			if (temp != null && (saveDefaultValues || !DEFAULT_VALUES[i].equals(temp))) {
				final Element node = document.createElement(TAGS[i]);
				node.appendChild(document.createTextNode(temp));

				makefileSettings.appendChild(node);
			}
		}
	}
}
