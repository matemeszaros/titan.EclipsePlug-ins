/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the file specific property settings (excludedness only). Also
 * loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class FileBuildPropertyData {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.File";
	public static final String FILERESOURCEXMLNODE = "FileResource";
	public static final String FILEPATHXMLNODE = "FilePath";
	public static final String FILEPROPERTIESXMLNODE = "FileProperties";
	public static final String EXCLUDEFROMBUILDXMLNODE = "ExcludeFromBuild";

	public static final String TRUE_STRING = "true";
	public static final String FALSE_STRING = "false";

	public static final String EXCLUDE_FROM_BUILD_PROPERTY = "excludeFromBuild";

	private FileBuildPropertyData() {
		// Do nothing
	}

	/**
	 * Removes the TITAN related attributes from the provided file.
	 * 
	 * @param file
	 *                the file whose attributes are to be cleaned
	 * */
	public static void removeTITANAttributes(final IFile file) {
		try {
			file.setPersistentProperty(new QualifiedName(QUALIFIER, EXCLUDE_FROM_BUILD_PROPERTY), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing properties of `" + file.getName() + "'", e);
		}
	}

	/**
	 * Returns the value of the provided property for the provided file.
	 * 
	 * @param file
	 *                the file to operate on.
	 * @param property
	 *                the property to check.
	 * 
	 * @return the found value of the property.
	 * */
	private static String getPropertyValue(final IFile file, final String property) {
		String temp = null;
		try {
			temp = file.getPersistentProperty(new QualifiedName(QUALIFIER, property));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting property `" + property + "' of `" + file.getName() + "'", e);
		}

		if (!TRUE_STRING.equals(temp)) {
			temp = FALSE_STRING;
		}

		return temp;
	}

	/**
	 * Sets the provided value, on the provided resource, for the provided
	 * property.
	 * 
	 * @param file
	 *                the file to work on.
	 * @param property
	 *                the property to change.
	 * @param value
	 *                the value to set.
	 * */
	private static void setPropertyValue(final IFile file, final String property, final String value) {
		try {
			file.setPersistentProperty(new QualifiedName(QUALIFIER, property), TRUE_STRING.equals(value) ? TRUE_STRING : FALSE_STRING);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting property `" + property + "' of `" + file.getName() + "'", e);
		}
	}

	/**
	 * Checks if the provided file has it settings at the default value or
	 * not.
	 * 
	 * @param file
	 *                the file to check.
	 * 
	 * @return true if all properties have their default values, false
	 *         otherwise.
	 * */
	public static boolean hasDefaultProperties(final IFile file) {
		return FALSE_STRING.equals(getPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY));
	}

	/**
	 * Creates an XML tree from a files properties.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *                the Document to be used to create the XML nodes
	 * @param file
	 *                the file whose attributes are to be used
	 * 
	 * @return the root of the created XML tree
	 * */
	public static Element saveFileProperties(final Document document, final IFile file) {
		final Element root = document.createElement(FILERESOURCEXMLNODE);

		final Element filePath = document.createElement(FILEPATHXMLNODE);
		filePath.appendChild(document.createTextNode(file.getProjectRelativePath().toPortableString()));
		root.appendChild(filePath);

		final Element fileProperties = document.createElement(FILEPROPERTIESXMLNODE);

		final Element excludeFromBuild = document.createElement(EXCLUDEFROMBUILDXMLNODE);
		excludeFromBuild.appendChild(document.createTextNode(getPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY)));

		fileProperties.appendChild(excludeFromBuild);

		root.appendChild(fileProperties);

		return root;
	}

	/**
	 * Load file related attributes from the provided XML tree.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param node
	 *                the root of the XML tree containing file attribute
	 *                related informations.
	 * @param project
	 *                the project to be used for finding the file
	 * @param notYetReachedFiles
	 *                the set of files not yet reached. The file found at
	 *                this node will be removed from this set.
	 * @param changedResources
	 *                a map of resources that changed while loading the
	 *                settings
	 * */
	public static void loadFileProperties(final Node node, final IProject project, final Set<IFile> notYetReachedFiles,
			final Set<IResource> changedResources) {
		final NodeList resourceList = node.getChildNodes();
		int filePathIndex = -1;
		int filePropertiesIndex = -1;
		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final String nodeName = resourceList.item(i).getNodeName();
			if (FILEPATHXMLNODE.equals(nodeName)) {
				filePathIndex = i;
			} else if (FILEPROPERTIESXMLNODE.equals(nodeName)) {
				filePropertiesIndex = i;
			}
		}

		if (filePathIndex == -1 || filePropertiesIndex == -1) {
			return;
		}

		final String filePath = resourceList.item(filePathIndex).getTextContent();

		if (!project.exists(new Path(filePath))) {
			return;
		}

		final IFile file = project.getFile(filePath);
		if (notYetReachedFiles.contains(file)) {
			notYetReachedFiles.remove(file);
		}

		final NodeList fileProperties = resourceList.item(filePropertiesIndex).getChildNodes();
		final String oldValue = getPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY);

		for (int i = 0, size = fileProperties.getLength(); i < size; i++) {
			final Node property = fileProperties.item(i);
			if (EXCLUDEFROMBUILDXMLNODE.equals(property.getNodeName())) {
				final String value = property.getTextContent();
				if (oldValue == null || !oldValue.equals(value)) {
					changedResources.add(file);
					setPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY, value);
				}
			}
		}
	}

	/**
	 * Sets the properties of the provided file to their default values.
	 * 
	 * @param file
	 *                the file whose properties should be set.
	 * @param changedResources
	 *                a map of resources that changed while loading the
	 *                settings.
	 * */
	public static void loadDefaultProperties(final IFile file, final Set<IResource> changedResources) {
		final String oldValue = getPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY);
		if (oldValue == null || !oldValue.equals(FALSE_STRING)) {
			changedResources.add(file);
			setPropertyValue(file, EXCLUDE_FROM_BUILD_PROPERTY, FALSE_STRING);
		}
	}

	/**
	 * Copies the project information related to files from the source node
	 * to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param sourceNode
	 *                the node used as the source of the information.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param project
	 *                the project to be worked on, used to identify the
	 *                folders.
	 * @param notYetReachedFiles
	 *                the files not yet processed.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyFileProperties(final Node sourceNode, final Document document, final IProject project,
			final Set<String> notYetReachedFiles, final boolean saveDefaultValues) {
		String filePath = null;
		String excludeValue = FALSE_STRING;

		if (sourceNode != null) {
			final NodeList resourceList = sourceNode.getChildNodes();
			int filePathIndex = -1;
			int filePropertiesIndex = -1;
			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final String nodeName = resourceList.item(i).getNodeName();
				if (FILEPATHXMLNODE.equals(nodeName)) {
					filePathIndex = i;
				} else if (FILEPROPERTIESXMLNODE.equals(nodeName)) {
					filePropertiesIndex = i;
				}
			}

			if (filePathIndex == -1) {
				return null;
			}

			filePath = resourceList.item(filePathIndex).getTextContent();
			if (!project.exists(new Path(filePath))) {
				return null;
			}

			notYetReachedFiles.remove(filePath);
			if (filePropertiesIndex != -1) {
				final NodeList fileProperties = resourceList.item(filePropertiesIndex).getChildNodes();
				for (int i = 0, size = fileProperties.getLength(); i < size; i++) {
					final Node property = fileProperties.item(i);
					if (EXCLUDEFROMBUILDXMLNODE.equals(property.getNodeName())) {
						excludeValue = property.getTextContent();
						if (!FALSE_STRING.equals(excludeValue) && !TRUE_STRING.equals(excludeValue)) {
							excludeValue = FALSE_STRING;
						}
					}
				}
			}
		}

		if (!saveDefaultValues && FALSE_STRING.equals(excludeValue)) {
			return null;
		}

		final Element root = document.createElement(FILERESOURCEXMLNODE);

		final Element filePathNode = document.createElement(FILEPATHXMLNODE);
		filePathNode.appendChild(document.createTextNode(filePath));
		root.appendChild(filePathNode);

		final Element fileProperties = document.createElement(FILEPROPERTIESXMLNODE);

		final Element excludeFromBuild = document.createElement(EXCLUDEFROMBUILDXMLNODE);
		excludeFromBuild.appendChild(document.createTextNode(excludeValue));
		fileProperties.appendChild(excludeFromBuild);

		root.appendChild(fileProperties);

		return root;
	}

	/**
	 * Copies the project information related to folders from the source
	 * node to the target node. As in this special case there is no need for
	 * a source node, it purely creational operation.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param file
	 *                the file to use.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyDefaultFileProperties(final Document document, final IFile file) {
		final Element root = document.createElement(FILERESOURCEXMLNODE);

		final Element filePath = document.createElement(FILEPATHXMLNODE);
		filePath.appendChild(document.createTextNode(file.getProjectRelativePath().toPortableString()));
		root.appendChild(filePath);

		final Element fileProperties = document.createElement(FILEPROPERTIESXMLNODE);

		final Element excludeFromBuild = document.createElement(EXCLUDEFROMBUILDXMLNODE);
		excludeFromBuild.appendChild(document.createTextNode(FALSE_STRING));
		fileProperties.appendChild(excludeFromBuild);

		root.appendChild(fileProperties);

		return root;
	}
}
