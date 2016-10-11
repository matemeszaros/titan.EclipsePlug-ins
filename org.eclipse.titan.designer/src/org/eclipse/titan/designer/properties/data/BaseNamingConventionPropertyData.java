/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A base class for handling the naming convention related property settings on
 * resources. Also loading, saving of these properties from and into external
 * formats.
 * 
 * @author Kristof Szabados
 * */
public final class BaseNamingConventionPropertyData {

	private BaseNamingConventionPropertyData() {
		// Do nothing
	}

	/**
	 * Remove the TITAN provided attributes from a resource.
	 * 
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IResource resource, final String qualifier, final String[] properties) {
		for (int i = 0; i < properties.length; i++) {
			try {
				resource.setPersistentProperty(new QualifiedName(qualifier, properties[i]), null);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While removing property `" + properties[i] + "' of `" + resource.getName()
						+ "'", e);
			}
		}
	}

	/**
	 * Loads and sets the naming convention related settings contained in
	 * the XML tree for this resource.
	 * 
	 * @param root
	 *                the root of the subtree containing naming convention
	 *                related attributes, or null if no such was loaded
	 * @param resource
	 *                the resource to work on
	 * @param qualifier
	 *                the qualifier to access the properties
	 * @param properties
	 *                the property list to use
	 * @param tags
	 *                the TAGS to use
	 * @param defaultValues
	 *                the default values to use
	 * */
	public static void loadProperties(final Node root, final IResource resource, final String qualifier, final String[] properties,
			final String[] tags, final String[] defaultValues) {
		String[] newValues = new String[tags.length];
		System.arraycopy(defaultValues, 0, newValues, 0, tags.length);

		if (root != null) {
			final NodeList resourceList = root.getChildNodes();

			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final String name = resourceList.item(i).getNodeName();
				for (int j = 0; j < tags.length; j++) {
					if (tags[j].equals(name)) {
						newValues[j] = resourceList.item(i).getTextContent();
					}
				}
			}
		}

		for (int i = 0; i < properties.length; i++) {
			final QualifiedName qualifiedName = new QualifiedName(qualifier, properties[i]);
			try {
				final String oldValue = resource.getPersistentProperty(qualifiedName);
				if (newValues[i] != null && !newValues[i].equals(oldValue)) {
					resource.setPersistentProperty(qualifiedName, newValues[i]);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(
						"While loading property `" + properties[i] + "' of `" + resource.getName() + "'", e);
			}
		}
	}

	/**
	 * Creates an XML tree from the naming conventions related settings of
	 * the resource.
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param resource
	 *                the resource to work on
	 * @param qualifier
	 *                the qualifier to access the properties
	 * @param properties
	 *                the property names to use
	 * @param tags
	 *                the XML tags to use.
	 * @param defaultValues
	 *                the default values to use
	 * @param xmlNode
	 *                the XML node under which the new nodes should get
	 * 
	 * @return the created XML tree's root node
	 * */
	public static Element saveProperties(final Document document, final IResource resource, final String qualifier, final String[] properties,
			final String[] tags, final String[] defaultValues, final String xmlNode) {
		Element root = null;

		String temp = null;
		for (int i = 0; i < properties.length; i++) {
			try {
				temp = resource.getPersistentProperty(new QualifiedName(qualifier, properties[i]));
				if (temp != null && !defaultValues[i].equals(temp)) {
					final Element node = document.createElement(tags[i]);
					node.appendChild(document.createTextNode(temp));
					if (root == null) {
						root = document.createElement(xmlNode);
					}

					root.appendChild(node);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While saving property `" + properties[i] + "' of `" + resource.getName() + "'",
						e);
			}
		}

		return root;
	}

	/**
	 * Checks if the provided folder has it settings at the default value or
	 * not.
	 * 
	 * @param folder
	 *                the folder to check.
	 * @param qualifier
	 *                the qualifier to use.
	 * @param properties
	 *                the property names to use
	 * @param defaultValues
	 *                the default values to use
	 * 
	 * @return true if all properties have their default values, false
	 *         otherwise.
	 * */
	public static boolean hasDefaultProperties(final IFolder folder, final String qualifier, final String[] properties,
			final String[] defaultValues) {
		String temp = null;
		for (int i = 0; i < properties.length; i++) {
			try {
				temp = folder.getPersistentProperty(new QualifiedName(qualifier, properties[i]));
				if (temp != null && !defaultValues[i].equals(temp)) {
					return false;
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While checking property `" + properties[i] + "' of `" + folder.getName()
						+ "'", e);
			}
		}

		return true;
	}

	/**
	 * Copies the resource information related to naming conventions from
	 * the source node to the target node.
	 * 
	 * @param source
	 *                the node used as the source of the information.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * @param xmlNode
	 *                the XML node under which the new nodes should get
	 * @param properties
	 *                the property names to use
	 * @param tags
	 *                the XML tags to use.
	 * @param defaultValues
	 *                the default values to use
	 * 
	 * @return the resulting target node.
	 * */
	public static Element copyProperties(final Node source, final Document document, final boolean saveDefaultValues, final String xmlNode,
			final String[] properties, final String[] tags, final String[] defaultValues) {
		final String[] newValues = new String[tags.length];
		System.arraycopy(defaultValues, 0, newValues, 0, tags.length);

		if (source != null) {
			final NodeList resourceList = source.getChildNodes();

			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final String name = resourceList.item(i).getNodeName();
				for (int j = 0; j < tags.length; j++) {
					if (tags[j].equals(name)) {
						newValues[j] = resourceList.item(i).getTextContent();
					}
				}
			}
		}

		Element result = null;

		Element node;
		String temp = null;
		for (int i = 0; i < tags.length; i++) {
			temp = newValues[i];
			if (temp != null && (saveDefaultValues || !defaultValues[i].equals(temp))) {
				node = document.createElement(tags[i]);
				node.appendChild(document.createTextNode(temp));
				if (result == null) {
					result = document.createElement(xmlNode);
				}

				result.appendChild(node);
			}
		}

		return result;
	}
}
