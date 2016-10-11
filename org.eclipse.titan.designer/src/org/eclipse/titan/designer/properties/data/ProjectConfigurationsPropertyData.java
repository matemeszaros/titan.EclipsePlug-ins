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
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the configuration related property settings of projects. Also
 * loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public class ProjectConfigurationsPropertyData {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Project";
	public static final String REFERENCED_CONFIGURATION_INFO = ProductConstants.PRODUCT_ID_DESIGNER + ".referecedConfigurationInfo";
	public static final String CONFIGURATION_REQUIREMENTS_NODE = "ConfigurationRequirements";

	public static class ConfigurationRequirement {
		private String projectName;
		private String configuration;

		public ConfigurationRequirement(final String projectName, final String configuration) {
			this.projectName = projectName;
			this.configuration = configuration;
		}

		public String getProjectName() {
			return projectName;
		}

		public String getConfiguration() {
			return configuration;
		}

		public void setConfiguration(final String configuration) {
			this.configuration = configuration;
		}
	}

	private ProjectConfigurationsPropertyData() {
		// Do nothing
	}

	public static List<ConfigurationRequirement> getConfigurationRequirements(final IProject project) {
		String temp = null;
		try {
			temp = project.getPersistentProperty(new QualifiedName(QUALIFIER, REFERENCED_CONFIGURATION_INFO));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While loading requirements of `" + project.getName() + "'", e);
		}
		final List<String> splittedList = ProjectRemoteBuildPropertyData.intelligentSplit(temp, '#', '\\');
		final List<ConfigurationRequirement> result = new ArrayList<ConfigurationRequirement>(splittedList.size());

		for (String split : splittedList) {
			final List<String> requirement = ProjectRemoteBuildPropertyData.intelligentSplit(split, ':', '\\');
			if (requirement != null && requirement.size() == 2) {
				result.add(new ConfigurationRequirement(requirement.get(0), requirement.get(1)));
			}
		}

		final IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects();
		for (IProject referencedProject : referencedProjects) {
			boolean found = false;
			final String projectName = referencedProject.getName();
			for (int i = 0, size = result.size(); i < size && !found; i++) {
				if (projectName.equals(result.get(i).projectName)) {
					found = true;
				}
			}

			if (!found) {
				final ConfigurationRequirement requirement = new ConfigurationRequirement(projectName, "");
				result.add(requirement);
			}
		}

		return result;
	}

	/**
	 * Sets the list of configuration requirements for a project.
	 * 
	 * @param project
	 *                the project set them on.
	 * @param requirements
	 *                the requirements to set.
	 **/
	public static void setConfigurationRequirements(final IProject project, final List<ConfigurationRequirement> requirements) {
		final StringBuilder builder = new StringBuilder();
		int i = 0;

		for (ConfigurationRequirement requirement : requirements) {
			if (i != 0) {
				builder.append('#');
			}

			final String tempString1 = requirement.getProjectName().replace("\\", "\\\\").replace(":", "\\:");
			final String tempString2 = requirement.getConfiguration().replace("\\", "\\\\").replace(":", "\\:");
			final String tempString = tempString1 + ":" + tempString2;
			builder.append(tempString.replace("#", "\\#"));
			i++;
		}

		final QualifiedName qualifiedName = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, REFERENCED_CONFIGURATION_INFO);
		final String newValue = builder.toString();
		try {
			final String oldValue = project.getPersistentProperty(qualifiedName);
			if (newValue != null && !newValue.equals(oldValue)) {
				project.setPersistentProperty(qualifiedName, newValue);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting requirements of `" + project.getName() + "'", e);
		}
	}

	/**
	 * Save the configuration related properties of the provided project
	 * into a new XML node element.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	public static Element saveProperties(final Document document, final IProject project) {
		Element root = null;

		final List<ConfigurationRequirement> requirements = getConfigurationRequirements(project);
		if (requirements.isEmpty()) {
			return root;
		}

		for (ConfigurationRequirement requirement : requirements) {
			if (requirement.configuration == null || requirement.configuration.isEmpty()) {
				continue;
			}

			final Element configurationRequirement = document.createElement("configurationRequirement");

			final Element projectName = document.createElement("projectName");
			projectName.appendChild(document.createTextNode(requirement.getProjectName()));
			configurationRequirement.appendChild(projectName);

			final Element configuration = document.createElement("rerquiredConfiguration");
			configuration.appendChild(document.createTextNode(requirement.configuration));
			configurationRequirement.appendChild(configuration);

			if (root == null) {
				root = document.createElement(CONFIGURATION_REQUIREMENTS_NODE);
			}
			root.appendChild(configurationRequirement);
		}

		return root;
	}

	/**
	 * Loads and sets the configuration related settings contained in the
	 * XML tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param root
	 *                the root of the subtree containing configuration
	 *                related attributes, or null if no such was loaded
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadProperties(final Node node, final IProject project, final Set<IResource> changedResources) {
		final NodeList configurationRequirementList = node.getChildNodes();
		NodeList requirementInternalsList;
		String projectName;
		String configuration;
		final List<ConfigurationRequirement> requirements = new ArrayList<ConfigurationRequirement>();

		changedResources.add(project);
		for (int i = 0; i < configurationRequirementList.getLength(); i++) {
			final Node temp = configurationRequirementList.item(i);
			if ("configurationRequirement".equals(temp.getNodeName())) {
				requirementInternalsList = temp.getChildNodes();
				projectName = null;
				configuration = null;
				for (int j = 0; j < requirementInternalsList.getLength(); j++) {
					if ("projectName".equals(requirementInternalsList.item(j).getNodeName())) {
						projectName = requirementInternalsList.item(j).getTextContent();
					} else if ("rerquiredConfiguration".equals(requirementInternalsList.item(j).getNodeName())) {
						configuration = requirementInternalsList.item(j).getTextContent();
					}
				}
				if (projectName != null && configuration != null) {
					requirements.add(new ConfigurationRequirement(projectName, configuration));
				}
			}
		}

		setConfigurationRequirements(project, requirements);
	}

	/**
	 * Copies the project information related to project configurations from
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
	 * 
	 * @return the resulting target node.
	 * */
	public static Element copyProperties(final Node source, final Document document) {
		if (source == null) {
			return null;
		}

		final NodeList configurationRequirementList = source.getChildNodes();
		NodeList requirementInternalsList;
		final List<ConfigurationRequirement> requirements = new ArrayList<ConfigurationRequirement>();

		for (int i = 0; i < configurationRequirementList.getLength(); i++) {
			final Node temp = configurationRequirementList.item(i);
			if ("configurationRequirement".equals(temp.getNodeName())) {
				requirementInternalsList = temp.getChildNodes();
				String projectName = null;
				String configuration = null;
				for (int j = 0; j < requirementInternalsList.getLength(); j++) {
					if ("projectName".equals(requirementInternalsList.item(j).getNodeName())) {
						projectName = requirementInternalsList.item(j).getTextContent();
					} else if ("rerquiredConfiguration".equals(requirementInternalsList.item(j).getNodeName())) {
						configuration = requirementInternalsList.item(j).getTextContent();
					}
				}
				if (projectName != null && configuration != null) {
					requirements.add(new ConfigurationRequirement(projectName, configuration));
				}
			}
		}

		Element root = null;

		if (requirements.isEmpty()) {
			return root;
		}

		for (ConfigurationRequirement requirement : requirements) {
			if (requirement.configuration == null || requirement.configuration.isEmpty()) {
				continue;
			}

			final Element configurationRequirement = document.createElement("configurationRequirement");

			final Element projectName = document.createElement("projectName");
			projectName.appendChild(document.createTextNode(requirement.getProjectName()));
			configurationRequirement.appendChild(projectName);

			final Element configuration = document.createElement("rerquiredConfiguration");
			configuration.appendChild(document.createTextNode(requirement.configuration));
			configurationRequirement.appendChild(configuration);

			if (root == null) {
				root = document.createElement(CONFIGURATION_REQUIREMENTS_NODE);
			}
			root.appendChild(configurationRequirement);
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
			project.setPersistentProperty(new QualifiedName(QUALIFIER, REFERENCED_CONFIGURATION_INFO), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "'", e);
		}
	}
}
