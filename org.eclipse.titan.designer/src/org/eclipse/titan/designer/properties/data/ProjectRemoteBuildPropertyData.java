/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the remote build related property settings of projects. Also loading,
 * saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectRemoteBuildPropertyData {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Project";
	public static final String PARALLEL_COMMAND_EXECUTION = ProductConstants.PRODUCT_ID_DESIGNER + ".parallelCommandExecution";
	public static final String REMOTE_BUILD_HOST_INFO = ProductConstants.PRODUCT_ID_DESIGNER + ".remoteBuildHostInfo";
	public static final String REMOTEBUILDPROPERTIES_XMLNODE = "RemoteBuildProperties";
	public static final String PARALLELCOMMANDEXECUTION_XMLNODE = "ParallelCommandExecution";
	public static final String REMOTEHOSTXMLNODE = "RemoteHost";
	public static final String ACTIVEXMLNODE = "Active";
	public static final String NAMEXMLNODE = "Name";
	public static final String COMMANDXMLNODE = "Command";

	private static final String TRUE = "true";
	private static final String FALSE = "false";

	private ProjectRemoteBuildPropertyData() {
		// Do nothing
	}

	// \\->\ , \#->#
	public static List<String> intelligentSplit(final String input, final char delimeter, final char escape) {
		List<String> results = new ArrayList<String>();
		if (input == null || input.length() == 0) {
			return results;
		}
		StringBuilder tempResult = new StringBuilder();
		int i;
		// no over indexing is possible if the input was converted
		// correctly, as an escape must be escaping something
		char c;
		for (i = 0; i < input.length();) {
			c = input.charAt(i);
			if (escape == c) {
				// this is either a delimiter or an escape
				// character
				tempResult.append(input.charAt(i + 1));
				i += 2;
			} else if (delimeter == c) {
				results.add(tempResult.toString());
				tempResult = new StringBuilder();
				i++;
			} else {
				tempResult.append(c);
				i++;
			}
		}
		results.add(tempResult.toString());
		return results;
	}

	public static BuildLocation[] getBuildLocations(final IProject project) {
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(QUALIFIER, REMOTE_BUILD_HOST_INFO));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting build locations of `" + project.getName() + "'", e);
		}
		final List<String> splittedList = intelligentSplit(temp, '#', '\\');
		final String[] tempArray = splittedList.toArray(new String[splittedList.size()]);
		final BuildLocation[] result = new BuildLocation[tempArray.length];
		for (int i = 0; i < tempArray.length; i++) {
			result[i] = new BuildLocation(tempArray[i]);
		}
		return result;
	}

	public static void setBuildLocations(final IProject project, final BuildLocation[] locations) {
		final StringBuilder builder = new StringBuilder();
		String tempString;
		for (int i = 0; i < locations.length; i++) {
			if (i != 0) {
				builder.append('#');
			}
			tempString = locations[i].getPropertyValueRepresentation().toString();
			tempString = tempString.replace("\\", "\\\\");
			builder.append(tempString.replace("#", "\\#"));
		}

		final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, REMOTE_BUILD_HOST_INFO);
		final String newValue = builder.toString();
		try {
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting build locations of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Creates an XML tree from the provided project's remote build related
	 * properties.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *                the Document to be used to create the XML nodes
	 * @param project
	 *                the project whose attributes are to be used
	 * 
	 * @return the root of the created XML tree
	 * */
	public static Element saveRemoteBuildProperties(final Document document, final IProject project) {
		Element root = null;
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(QUALIFIER, PARALLEL_COMMAND_EXECUTION));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While reading parallel build property of `" + project.getName() + "'", e);
		}

		if (temp != null && !"".equals(temp) && !"false".equals(temp)) {
			final Element parallelExecution = document.createElement(PARALLELCOMMANDEXECUTION_XMLNODE);
			parallelExecution.appendChild(document.createTextNode(temp));

			root = document.createElement(REMOTEBUILDPROPERTIES_XMLNODE);
			root.appendChild(parallelExecution);
		}

		final BuildLocation[] locations = getBuildLocations(project);
		if (locations.length == 0) {
			return root;
		}

		for (BuildLocation location : locations) {
			final Element remoteHost = document.createElement(REMOTEHOSTXMLNODE);

			final Element active = document.createElement(ACTIVEXMLNODE);
			active.appendChild(document.createTextNode(location.getActive() ? TRUE : FALSE));
			remoteHost.appendChild(active);

			final Element name = document.createElement(NAMEXMLNODE);
			name.appendChild(document.createTextNode(location.getName()));
			remoteHost.appendChild(name);

			final Element command = document.createElement(COMMANDXMLNODE);
			command.appendChild(document.createTextNode(location.getCommand()));
			remoteHost.appendChild(command);

			if (root == null) {
				root = document.createElement(REMOTEBUILDPROPERTIES_XMLNODE);
			}
			root.appendChild(remoteHost);
		}

		return root;
	}

	/**
	 * Loads remote build related project attributes from the provided XML
	 * tree.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param node
	 *                the root of the XML tree containing remote build
	 *                attribute related information.
	 * @param project
	 *                the project to be used for finding the file
	 * @param changedResources
	 *                a map of resources that changed while loading the
	 *                settings
	 * */
	public static void loadRemoteBuildProperties(final Node node, final IProject project, final Set<IResource> changedResources) {
		final NodeList remoteBuildSettingsList = node.getChildNodes();
		NodeList possibleHostPropertiesList;
		String active;
		String name;
		String command;
		String parallelRemoteBuild = FALSE;
		final List<BuildLocation> locations = new ArrayList<BuildLocation>();

		changedResources.add(project);
		for (int i = 0; i < remoteBuildSettingsList.getLength(); i++) {
			if (REMOTEHOSTXMLNODE.equals(remoteBuildSettingsList.item(i).getNodeName())) {
				possibleHostPropertiesList = remoteBuildSettingsList.item(i).getChildNodes();
				active = null;
				name = null;
				command = null;
				for (int j = 0; j < possibleHostPropertiesList.getLength(); j++) {
					if (ACTIVEXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						active = possibleHostPropertiesList.item(j).getTextContent();
					} else if (NAMEXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						name = possibleHostPropertiesList.item(j).getTextContent();
					} else if (COMMANDXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						command = possibleHostPropertiesList.item(j).getTextContent();
					}
				}
				if (active != null && name != null && command != null) {
					locations.add(new BuildLocation(TRUE.equals(active) ? true : false, name, command));
				}
			} else if (PARALLELCOMMANDEXECUTION_XMLNODE.equals(remoteBuildSettingsList.item(i).getNodeName())) {
				parallelRemoteBuild = remoteBuildSettingsList.item(i).getTextContent();
			}
		}

		setBuildLocations(project, locations.toArray(new BuildLocation[locations.size()]));

		final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, PARALLEL_COMMAND_EXECUTION);
		final String newValue = TRUE.equals(parallelRemoteBuild) ? TRUE : FALSE;
		try {
			String oldValue = project.getPersistentProperty(qualifiedName);
			if (!newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While loading parallel build property of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Copies the project information related to remote build from the
	 * source node to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param sourceNode
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
	public static Element copyRemoteBuildProperties(final Node sourceNode, final Document document, final boolean saveDefaultValues) {
		if (sourceNode == null) {
			return null;
		}

		final NodeList remoteBuildSettingsList = sourceNode.getChildNodes();
		NodeList possibleHostPropertiesList;
		String parallelRemoteBuild = FALSE;
		final List<BuildLocation> locations = new ArrayList<BuildLocation>();

		for (int i = 0; i < remoteBuildSettingsList.getLength(); i++) {
			if (REMOTEHOSTXMLNODE.equals(remoteBuildSettingsList.item(i).getNodeName())) {
				possibleHostPropertiesList = remoteBuildSettingsList.item(i).getChildNodes();
				String active = null;
				String name = null;
				String command = null;
				for (int j = 0; j < possibleHostPropertiesList.getLength(); j++) {
					if (ACTIVEXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						active = possibleHostPropertiesList.item(j).getTextContent();
					} else if (NAMEXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						name = possibleHostPropertiesList.item(j).getTextContent();
					} else if (COMMANDXMLNODE.equals(possibleHostPropertiesList.item(j).getNodeName())) {
						command = possibleHostPropertiesList.item(j).getTextContent();
					}
				}
				if (active != null && name != null && command != null) {
					locations.add(new BuildLocation(TRUE.equals(active) ? true : false, name, command));
				}
			} else if (PARALLELCOMMANDEXECUTION_XMLNODE.equals(remoteBuildSettingsList.item(i).getNodeName())) {
				parallelRemoteBuild = remoteBuildSettingsList.item(i).getTextContent();
			}
		}

		Element root = null;

		for (BuildLocation location : locations) {
			final Element remoteHost = document.createElement(REMOTEHOSTXMLNODE);

			final Element active = document.createElement(ACTIVEXMLNODE);
			active.appendChild(document.createTextNode(location.getActive() ? TRUE : FALSE));
			remoteHost.appendChild(active);

			final Element name = document.createElement(NAMEXMLNODE);
			name.appendChild(document.createTextNode(location.getName()));
			remoteHost.appendChild(name);

			final Element command = document.createElement(COMMANDXMLNODE);
			command.appendChild(document.createTextNode(location.getCommand()));
			remoteHost.appendChild(command);

			if (root == null) {
				root = document.createElement(REMOTEBUILDPROPERTIES_XMLNODE);
			}
			root.appendChild(remoteHost);
		}

		if (saveDefaultValues || !FALSE.equals(parallelRemoteBuild)) {
			final Element command = document.createElement(PARALLELCOMMANDEXECUTION_XMLNODE);
			command.appendChild(document.createTextNode(parallelRemoteBuild));

			if (root == null) {
				root = document.createElement(REMOTEBUILDPROPERTIES_XMLNODE);
			}
			root.appendChild(command);
		}

		return root;
	}

	/**
	 * Removes the TITAN related attributes from the provided project.
	 * 
	 * @param project
	 *                the project whose attributes are to be cleaned
	 * */
	public static void removeTITANAttributes(final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(QUALIFIER, PARALLEL_COMMAND_EXECUTION), null);
			project.setPersistentProperty(new QualifiedName(QUALIFIER, REMOTE_BUILD_HOST_INFO), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing properties of `" + project.getName() + "'", e);
		}
	}
}
