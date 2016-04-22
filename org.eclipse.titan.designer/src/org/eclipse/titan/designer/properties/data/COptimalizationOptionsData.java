/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
 * A class for handling the C/C++ (GCC) optimization related property settings
 * on resources. Also loading, saving of these properties from and into external
 * formats.
 * 
 * @author Kristof Szabados
 * */
public final class COptimalizationOptionsData {
	public static final String OPTIMIZATION_LEVEL_PROPERTY = "optimizationLevel";
	public static final String PTIMIZATION_LEVEL_TAG = "optimizationLevel";
	public static final String OTHER_OPTIMIZATION_FLAGS_PROPERTY = "otherOptimizationFlags";
	public static final String OTHER_OPTIMIZATION_FLAGS_TAG = "otherOptimizationFlags";

	public static final String[] PROPERTIES = { OPTIMIZATION_LEVEL_PROPERTY, OTHER_OPTIMIZATION_FLAGS_PROPERTY };
	public static final String[] TAGS = { PTIMIZATION_LEVEL_TAG, OTHER_OPTIMIZATION_FLAGS_TAG };
	public static final String[] DEFAULT_VALUES = { "Commonoptimizations", "" };

	private COptimalizationOptionsData() {
		// Do nothing
	}

	public static String getCxxOptimizationFlags(final IProject project) {
		final StringBuilder builder = new StringBuilder();
		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OPTIMIZATION_LEVEL_PROPERTY));
			if (temp != null) {
				if ("None".equals(temp)) {
					builder.append(" -O0");
				} else if ("Minoroptimizations".equals(temp)) {
					builder.append(" -O1");
				} else if ("Commonoptimizations".equals(temp)) {
					builder.append(" -O2");
				} else if ("Optimizeforspeed".equals(temp)) {
					builder.append(" -O3");
				} else if ("Optimizeforsize".equals(temp)) {
					builder.append(" -Os");
				}
			}

			temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					COptimalizationOptionsData.OTHER_OPTIMIZATION_FLAGS_PROPERTY));
			builder.append(' ').append(temp);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting the optimization flags of `" + project.getName() + "'", e);
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

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < TAGS.length; j++) {
				if (TAGS[j].equals(name)) {
					newValues[j] = node.getTextContent();
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
				ErrorReporter.logExceptionStackTrace("While loading property `" + PROPERTIES[i] + "' of `" + project.getName() + "'",
						e);
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
					Element element = document.createElement(TAGS[i]);
					element.appendChild(document.createTextNode(temp));
					makefileSettings.appendChild(element);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While saving property `" + PROPERTIES[i] + "' of `" + project.getName() + "'",
						e);
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
