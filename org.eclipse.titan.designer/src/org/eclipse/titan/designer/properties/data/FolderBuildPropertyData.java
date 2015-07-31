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

import org.eclipse.core.resources.IFolder;
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
 * Handles the folder specific property settings of resources. Also loading,
 * saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class FolderBuildPropertyData {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Folder";
	public static final String FOLDERRESOURCEXMLNODE = "FolderResource";
	public static final String FOLDERPATHXMLNODE = "FolderPath";
	public static final String FOLDERPROPERTIESXMLNODE = "FolderProperties";
	public static final String CONTRALSTORAGEXMLNODE = "CentralStorage";
	public static final String EXCLUDEFROMBUILDXMLNODE = "ExcludeFromBuild";

	public static final String TRUE_STRING = "true";
	public static final String FALSE_STRING = "false";

	public static final String CENTRAL_STORAGE_PROPERTY = "centralStorage";
	public static final String EXCLUDE_FROM_BUILD_PROPERTY = "excludeFromBuild";

	private FolderBuildPropertyData() {
		// Do nothing
	}

	/**
	 * Removes the TITAN related attributes from the provided folder.
	 * 
	 * @param folder
	 *                the folder whose attributes are to be cleaned
	 * */
	public static void removeTITANAttributes(final IFolder folder) {
		try {
			folder.setPersistentProperty(new QualifiedName(QUALIFIER, CENTRAL_STORAGE_PROPERTY), null);
			folder.setPersistentProperty(new QualifiedName(QUALIFIER, EXCLUDE_FROM_BUILD_PROPERTY), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing properties of `" + folder.getName() + "'", e);
		}
	}

	/**
	 * Returns the value of the provided property for the provided folder.
	 * 
	 * @param folder
	 *                the folder to operate on.
	 * @param property
	 *                the property to check.
	 * 
	 * @return the found value of the property.
	 * */
	private static String getPropertyValue(final IFolder folder, final String property) {
		String temp = null;
		try {
			temp = folder.getPersistentProperty(new QualifiedName(QUALIFIER, property));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting property `" + property + "' of `" + folder.getName() + "'", e);
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
	 * @param folder
	 *                the folder to work on.
	 * @param property
	 *                the property to change.
	 * @param value
	 *                the value to set.
	 * */
	private static void setPropertyValue(final IFolder folder, final String property, final String value) {
		try {
			folder.setPersistentProperty(new QualifiedName(QUALIFIER, property), TRUE_STRING.equals(value) ? TRUE_STRING : FALSE_STRING);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting property `" + property + "' of `" + folder.getName() + "'", e);
		}
	}

	/**
	 * Checks if the provided folder has it settings at the default value or
	 * not.
	 * 
	 * @param folder
	 *                the folder to check.
	 * 
	 * @return true if all properties have their default values, false
	 *         otherwise.
	 * */
	public static boolean hasDefaultProperties(final IFolder folder) {
		return FALSE_STRING.equals(getPropertyValue(folder, CENTRAL_STORAGE_PROPERTY))
				&& FALSE_STRING.equals(getPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY))
				&& FolderNamingConventionPropertyData.hasDefaultProperties(folder);
	}

	/**
	 * Creates an XML tree from a folders properties.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *                the Document to be used to create the XML nodes
	 * @param folder
	 *                the folder whose attributes are to be used
	 * 
	 * @return the root of the created XML tree
	 * */
	public static Element saveFolderProperties(final Document document, final IFolder folder) {
		final Element root = document.createElement(FOLDERRESOURCEXMLNODE);

		final Element folderPath = document.createElement(FOLDERPATHXMLNODE);
		folderPath.appendChild(document.createTextNode(folder.getProjectRelativePath().toPortableString()));
		root.appendChild(folderPath);

		final Element folderProperties = document.createElement(FOLDERPROPERTIESXMLNODE);

		Element folderProperty = document.createElement(CONTRALSTORAGEXMLNODE);
		folderProperty.appendChild(document.createTextNode(getPropertyValue(folder, CENTRAL_STORAGE_PROPERTY)));
		folderProperties.appendChild(folderProperty);

		folderProperty = document.createElement(EXCLUDEFROMBUILDXMLNODE);
		folderProperty.appendChild(document.createTextNode(getPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY)));
		folderProperties.appendChild(folderProperty);

		root.appendChild(folderProperties);

		if (!FolderNamingConventionPropertyData.hasDefaultProperties(folder)) {
			root.appendChild(FolderNamingConventionPropertyData.saveProperties(document, folder));
		}

		return root;
	}

	/**
	 * Load folder related attributes from the provided XML tree.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param node
	 *                the root of the XML tree containing folder attribute
	 *                related informations.
	 * @param project
	 *                the project to be used for finding the folder
	 * @param notYetReachedFolders
	 *                the set of files not yet reached. The folder found at
	 *                this node will be removed from this set.
	 * @param changedResources
	 *                a map of resources that changed while loading the
	 *                settings
	 * */
	public static void loadFolderProperties(final Node node, final IProject project, final Set<IFolder> notYetReachedFolders,
			final Set<IResource> changedResources) {
		final NodeList resourceList = node.getChildNodes();
		Node folderPathIndex = null;
		Node folderPropertiesIndex = null;
		Node namingConventionIndex = null;
		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			Node tempNode = resourceList.item(i);
			String nodeName = tempNode.getNodeName();
			if (FOLDERPATHXMLNODE.equals(nodeName)) {
				folderPathIndex = tempNode;
			} else if (FOLDERPROPERTIESXMLNODE.equals(nodeName)) {
				folderPropertiesIndex = tempNode;
			} else if (FolderNamingConventionPropertyData.NAMINGCONVENTIONS_XMLNODE.equals(nodeName)) {
				namingConventionIndex = tempNode;
			}
		}

		if (folderPathIndex == null || folderPropertiesIndex == null) {
			return;
		}
		final String folderPath = folderPathIndex.getTextContent();

		if (!project.exists(new Path(folderPath))) {
			return;
		}

		final IFolder folder = project.getFolder(folderPath);
		if (notYetReachedFolders.contains(folder)) {
			notYetReachedFolders.remove(folder);
		}

		final NodeList folderProperties = folderPropertiesIndex.getChildNodes();
		Node property;
		for (int i = 0, size = folderProperties.getLength(); i < size; i++) {
			property = folderProperties.item(i);
			if (CONTRALSTORAGEXMLNODE.equals(property.getNodeName())) {
				final String oldValue = getPropertyValue(folder, CENTRAL_STORAGE_PROPERTY);
				final String value = property.getTextContent();
				if (oldValue == null || !oldValue.equals(value)) {
					changedResources.add(folder);
					setPropertyValue(folder, CENTRAL_STORAGE_PROPERTY, value);
				}
			} else if (EXCLUDEFROMBUILDXMLNODE.equals(property.getNodeName())) {
				final String oldValue = getPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY);
				final String value = property.getTextContent();
				if (oldValue == null || !oldValue.equals(value)) {
					changedResources.add(folder);
					setPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY, value);
				}
			}
		}

		FolderNamingConventionPropertyData.loadProperties(namingConventionIndex, folder);
	}

	/**
	 * Sets the properties of the provided folder to their default values.
	 * 
	 * @param folder
	 *                the folder whose properties should be set.
	 * @param changedResources
	 *                a map of resources that changed while loading the
	 *                settings.
	 * */
	public static void loadDefaultProperties(final IFolder folder, final Set<IResource> changedResources) {
		String oldValue = getPropertyValue(folder, CENTRAL_STORAGE_PROPERTY);
		if (oldValue == null || !oldValue.equals(FALSE_STRING)) {
			changedResources.add(folder);
			setPropertyValue(folder, CENTRAL_STORAGE_PROPERTY, FALSE_STRING);
		}

		oldValue = getPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY);
		if (oldValue == null || !oldValue.equals(FALSE_STRING)) {
			changedResources.add(folder);
			setPropertyValue(folder, EXCLUDE_FROM_BUILD_PROPERTY, FALSE_STRING);
		}

		FolderNamingConventionPropertyData.loadProperties(null, folder);
	}

	/**
	 * Copies the project information related to folders from the source
	 * node to the target node.
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
	 * @param notYetReachedFolders
	 *                the list of folders not yet processed.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyFolderProperties(final Node sourceNode, final Document document, final IProject project,
			final Set<String> notYetReachedFolders, final boolean saveDefaultValues) {
		String folderPath = null;
		String excludeValue = FALSE_STRING;
		String centralStorage = FALSE_STRING;

		Node namingConventionNode = null;
		IFolder folder = null;

		if (sourceNode != null) {
			final NodeList resourceList = sourceNode.getChildNodes();
			int folderPathIndex = -1;
			int folderPropertiesIndex = -1;

			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final String nodeName = resourceList.item(i).getNodeName();
				if (FOLDERPATHXMLNODE.equals(nodeName)) {
					folderPathIndex = i;
				} else if (FOLDERPROPERTIESXMLNODE.equals(nodeName)) {
					folderPropertiesIndex = i;
				} else if (FolderNamingConventionPropertyData.NAMINGCONVENTIONS_XMLNODE.equals(nodeName)) {
					namingConventionNode = resourceList.item(i);
				}
			}

			if (folderPathIndex == -1) {
				return null;
			}

			folderPath = resourceList.item(folderPathIndex).getTextContent();
			if (!project.exists(new Path(folderPath))) {
				return null;
			}
			notYetReachedFolders.remove(folderPath);
			folder = project.getFolder(folderPath);

			if (folderPropertiesIndex != -1) {
				final NodeList folderProperties = resourceList.item(folderPropertiesIndex).getChildNodes();
				for (int i = 0, size = folderProperties.getLength(); i < size; i++) {
					final Node property = folderProperties.item(i);
					if (CONTRALSTORAGEXMLNODE.equals(property.getNodeName())) {
						centralStorage = property.getTextContent();
						if (!FALSE_STRING.equals(centralStorage) && !TRUE_STRING.equals(centralStorage)) {
							centralStorage = FALSE_STRING;
						}
					} else if (EXCLUDEFROMBUILDXMLNODE.equals(property.getNodeName())) {
						excludeValue = property.getTextContent();
						if (!FALSE_STRING.equals(excludeValue) && !TRUE_STRING.equals(excludeValue)) {
							excludeValue = FALSE_STRING;
						}
					}
				}
			}
		}

		if (!saveDefaultValues && FALSE_STRING.equals(excludeValue) && FALSE_STRING.equals(centralStorage) && namingConventionNode == null) {
			return null;
		}

		final Element root = document.createElement(FOLDERRESOURCEXMLNODE);

		final Element filePathNode = document.createElement(FOLDERPATHXMLNODE);
		filePathNode.appendChild(document.createTextNode(folderPath));
		root.appendChild(filePathNode);

		final Element fileProperties = document.createElement(FOLDERPROPERTIESXMLNODE);

		if (saveDefaultValues || !FALSE_STRING.equals(excludeValue)) {
			final Element excludeFromBuild = document.createElement(EXCLUDEFROMBUILDXMLNODE);
			excludeFromBuild.appendChild(document.createTextNode(excludeValue));
			fileProperties.appendChild(excludeFromBuild);
		}

		if (saveDefaultValues || !FALSE_STRING.equals(centralStorage)) {
			final Element centralStorageNode = document.createElement(CENTRAL_STORAGE_PROPERTY);
			centralStorageNode.appendChild(document.createTextNode(centralStorage));
			fileProperties.appendChild(centralStorageNode);
		}

		if (saveDefaultValues || (namingConventionNode != null && folder != null)) {
			final Element tempNode = FolderNamingConventionPropertyData.copyProperties(namingConventionNode, document, saveDefaultValues);
			fileProperties.appendChild(tempNode);
		}

		root.appendChild(fileProperties);

		return root;
	}

	/**
	 * Copies the project information related to files from the source node
	 * to the target node. As in this special case there is no need for a
	 * source node, it purely creational operation.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param folder
	 *                the folder to use.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyDefaultFolderProperties(final Document document, final IFolder folder) {
		final Element root = document.createElement(FOLDERRESOURCEXMLNODE);

		final Element filePath = document.createElement(FOLDERPATHXMLNODE);
		filePath.appendChild(document.createTextNode(folder.getProjectRelativePath().toPortableString()));
		root.appendChild(filePath);

		final Element fileProperties = document.createElement(FOLDERPROPERTIESXMLNODE);

		final Element excludeFromBuild = document.createElement(EXCLUDEFROMBUILDXMLNODE);
		excludeFromBuild.appendChild(document.createTextNode(FALSE_STRING));
		fileProperties.appendChild(excludeFromBuild);

		final Element centralStorage = document.createElement(CENTRAL_STORAGE_PROPERTY);
		centralStorage.appendChild(document.createTextNode(FALSE_STRING));
		fileProperties.appendChild(centralStorage);

		final Element tempNode = FolderNamingConventionPropertyData.copyProperties(null, document, true);
		fileProperties.appendChild(tempNode);

		root.appendChild(fileProperties);

		return root;
	}
}
