/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

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
 * Handles the property settings of project, which drive who and how generates
 * the makefiles, if any.
 * 
 * Also loading, saving of these properties from and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectBuildPropertyData {
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Project";
	public static final String MAKEFILESETTINGSXMLNODE = "MakefileSettings";
	public static final String LOCALBUILDSETTINGS = "LocalBuildSettings";
	public static final String ACTIVECONFIGURATION = ProductConstants.PRODUCT_ID_DESIGNER + ".activeConfiguration";
	public static final String EMPTY_STRING = "";
	public static final String TRUE_STRING = "true";
	public static final String FALSE_STRING = "false";
	public static final String USE_TPD_NAME = "useTpdName";
	public static final String ORIG_TPD_URI = "origTpdURI";

	public static final String GENERATE_MAKEFILE_PROPERTY = "generateMakefile";
	public static final String GENERATE_INTERNAL_MAKEFILE_PROPERTY = "generateInternalMakefile";
	public static final String SYMLINKLESS_BUILD_PROPERTY = "symboliclinklessBuild";

	// makefile properties to be saved into the XML
	private static final String[] MAKEFILE_PROPERTIES = new String[] { GENERATE_MAKEFILE_PROPERTY, GENERATE_INTERNAL_MAKEFILE_PROPERTY,
			SYMLINKLESS_BUILD_PROPERTY };

	// XML tag names corresponding to the makefile properties
	private static final String[] MAKEFILE_TAGS = new String[] { "generateMakefile", "generateInternalMakefile", "symboliclinklessBuild" };
	private static final String[] MAKEFILE_DEFAULTS = new String[] { TRUE_STRING, TRUE_STRING, TRUE_STRING };

	// TODO: The following attributes are not saved yet into .TITAN_properties (only by Eclipse internal persistent storage)
	/**
	 * Stores the URI string of the tpd file which the project is loaded from,
	 * with resolved environment variables
	 */
	public static final String LOAD_LOCATION = "loadLocation";
	/**
	 * Stores the state if the project was already exported to tpd from the
	 * current workspace It is used at automatic export. In that case the first
	 * automatic export can start with the TITANProjectExportWizard if it is
	 * ordered. True if it is exported, otherwise false.
	 */
	public static final String ALREADY_EXPORTED = "alreadyExported";

	private ProjectBuildPropertyData() {
		// Do nothing
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param project
	 *            the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IProject project) {
		try {
			project.setPersistentProperty(new QualifiedName(QUALIFIER, GENERATE_MAKEFILE_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(QUALIFIER, GENERATE_INTERNAL_MAKEFILE_PROPERTY), null);
			project.setPersistentProperty(new QualifiedName(QUALIFIER, SYMLINKLESS_BUILD_PROPERTY), null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While removing attributes of `" + project.getName() + "'", e);
		}
		MakefileCreationData.removeTITANAttributes(project);
		InternalMakefileCreationData.removeTITANAttributes(project);
		MakeAttributesData.removeTITANAttributes(project);
	}

	/**
	 * Creates an XML tree from the Makefile related settings of the project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *            the document used for creating the tree nodes
	 * @param project
	 *            the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	public static Element saveMakefileSettings(final Document document, final IProject project) {
		final Element makefileSettings = document.createElement(MAKEFILESETTINGSXMLNODE);

		for (int i = 0; i < MAKEFILE_PROPERTIES.length; i++) {
			try {
				final String temp = project.getPersistentProperty(new QualifiedName(QUALIFIER, MAKEFILE_PROPERTIES[i]));
				if (temp != null && !MAKEFILE_DEFAULTS[i].equals(temp)) {
					final Element node = document.createElement(MAKEFILE_TAGS[i]);
					node.appendChild(document.createTextNode(temp));
					makefileSettings.appendChild(node);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While saving property `" + MAKEFILE_PROPERTIES[i] + "' of `" + project.getName()
						+ "'", e);
			}
		}

		MakefileCreationData.saveMakefileSettings(makefileSettings, document, project);
		InternalMakefileCreationData.saveMakefileSettings(makefileSettings, document, project);
		MakeAttributesData.saveMakefileSettings(makefileSettings, document, project);

		return makefileSettings;
	}

	/**
	 * Loads and sets the Makefile related settings contained in the XML tree
	 * for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param root
	 *            the root of the subtree containing Makefile related attributes
	 * @param project
	 *            the project to set the found attributes on.
	 * @param changedResources
	 *            the set of resources whose attributes have changed
	 * */
	public static void loadMakefileSettings(final Node root, final IProject project, final Set<IResource> changedResources) {
		final NodeList resourceList = root.getChildNodes();

		changedResources.add(project);
		MakefileCreationData.loadMakefileSettings(root, project);
		InternalMakefileCreationData.loadMakefileSettings(root, project);
		MakeAttributesData.loadMakefileSettings(root, project);

		final String[] tempValues = new String[MAKEFILE_TAGS.length];
		System.arraycopy(MAKEFILE_DEFAULTS, 0, tempValues, 0, MAKEFILE_TAGS.length);

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < MAKEFILE_TAGS.length; j++) {
				if (MAKEFILE_TAGS[j].equals(name)) {
					final String newValue = node.getTextContent();
					tempValues[j] = newValue;
				}
			}
		}

		for (int i = 0, size = MAKEFILE_PROPERTIES.length; i < size; i++) {
			final QualifiedName qualifiedName = new QualifiedName(QUALIFIER, MAKEFILE_PROPERTIES[i]);
			final String newValue = tempValues[i];
			try {
				final String oldValue = project.getPersistentProperty(qualifiedName);
				if (newValue != null && !newValue.equals(oldValue)) {
					project.setPersistentProperty(qualifiedName, newValue);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While loading property `" + MAKEFILE_PROPERTIES[i] + "' of `" + project.getName()
						+ "'", e);
			}
		}
	}

	/**
	 * Copies the project information related to Makefile settings from the
	 * source node to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject, TreeMap,
	 *      TreeMap, boolean)
	 * 
	 * @param source
	 *            the node used as the source of the information.
	 * @param document
	 *            the document to contain the result, used to create the XML
	 *            nodes.
	 * @param saveDefaultValues
	 *            whether the default values should be forced to be added to the
	 *            output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Element copyMakefileSettings(final Node source, final Document document, final boolean saveDefaultValues) {
		final NodeList resourceList = source.getChildNodes();

		final String[] tempValues = new String[MAKEFILE_TAGS.length];
		System.arraycopy(MAKEFILE_DEFAULTS, 0, tempValues, 0, MAKEFILE_TAGS.length);

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node node = resourceList.item(i);
			final String name = node.getNodeName();
			for (int j = 0; j < MAKEFILE_TAGS.length; j++) {
				if (MAKEFILE_TAGS[j].equals(name)) {
					String newValue = node.getTextContent();
					tempValues[j] = newValue;
				}
			}
		}

		final Element makefileSettings = document.createElement(MAKEFILESETTINGSXMLNODE);

		for (int i = 0; i < MAKEFILE_TAGS.length; i++) {
			final String temp = tempValues[i];
			if (saveDefaultValues || (temp != null && !MAKEFILE_DEFAULTS[i].equals(temp))) {
				final Element node = document.createElement(MAKEFILE_TAGS[i]);
				node.appendChild(document.createTextNode(temp));
				makefileSettings.appendChild(node);
			}
		}

		MakefileCreationData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		InternalMakefileCreationData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		MakeAttributesData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);

		return makefileSettings;
	}

	/**
	 * Creates an XML tree from the local build related settings of the project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * 
	 * @param document
	 *            the document used for creating the tree nodes
	 * @param project
	 *            the project to work on
	 * 
	 * @return the created XML tree's root node
	 * */
	public static Element saveLocalBuildSettings(final Document document, final IProject project) {
		return MakeAttributesData.saveLocalBuildSettings(document, project);
	}

	/**
	 * Loads and sets the local build related settings contained in the XML tree
	 * for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param node
	 *            the root of the subtree containing Makefile related attributes
	 * @param project
	 *            the project to set the found attributes on.
	 * @param changedResources
	 *            the set of resources whose attributes have changed
	 * */
	public static void loadLocalBuildSettings(final Node node, final IProject project, final Set<IResource> changedResources) {
		changedResources.add(project);

		MakeAttributesData.loadLocalBuildSettings(node, project);
	}

	/**
	 * Copies the project information related to local build settings from the
	 * source node to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject, TreeMap,
	 *      TreeMap, boolean)
	 * 
	 * @param source
	 *            the node used as the source of the information.
	 * @param document
	 *            the document to contain the result, used to create the XML
	 *            nodes.
	 * @param saveDefaultValues
	 *            whether the default values should be forced to be added to the
	 *            output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Element copyLocalBuildSettings(final Node source, final Document document, final boolean saveDefaultValues) {
		return MakeAttributesData.copyLocalBuildSettings(source, document, saveDefaultValues);
	}

	// ===== Get/Set functions ====
	/**
	 * Sets  "loadLoacation" of the `project' for 'value`. No semantic check on value
	 * 
	 * @param project
	 * @param value
	 */
	public static void setLoadLocation(final IProject project, final String value) {
		if (project == null || value == null) {
			return;
		}

		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, ProjectBuildPropertyData.LOAD_LOCATION),
					value);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting `loadLocation' for project `" + project.getName() + "'", e);
		}

	}

	/**
	 * Sets the status flag "alreadyExported" of the `project' for 'value`
	 * 
	 * @param project
	 * @param value
	 */
	public static void setProjectAlreadyExported(final IProject project, final boolean value) {
		if (project == null)
			return;

		String exported = value ? ProjectBuildPropertyData.TRUE_STRING : ProjectBuildPropertyData.FALSE_STRING;
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, ProjectBuildPropertyData.ALREADY_EXPORTED),
					exported);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting `alreadyExported' for project `" + project.getName() + "'", e);
		}

	}
	
	//************ Getting the state of makefile generating switches on project level*****
	/**
	 * Gets if automatic makefile generation is switched on in the given project.
	 * @param project
	 * @return
	 */
	public static boolean useAutomaticMakefilegeneration(final IProject project) {
		if( project == null) {
			return false;
		}
		String automaticMakefileGeneration = FALSE_STRING;
		try{
			automaticMakefileGeneration = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, 
							ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false; //?
		}
		return TRUE_STRING.equals(automaticMakefileGeneration);
	}
	/**
	 * Gets if internal makefile generation is switched on in the given project.
	 * 
	 * @param project
	 * @return true if internal makefile generation is switched on
	 */
	public static boolean useInternalMakefilegeneration(final IProject project) {
		if( project == null) {
			return false;
		}
		String useInternalMakefileGeneration = FALSE_STRING;
		try{
			useInternalMakefileGeneration = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, 
							ProjectBuildPropertyData.GENERATE_INTERNAL_MAKEFILE_PROPERTY));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false; //?
		}
		return TRUE_STRING.equals(useInternalMakefileGeneration);
	}
	
	/**
	 * Gets if symbolic link switch is switched on in the given project.
	 * <p>
	 * Do not use this function to decide if symbolic linking shall be performed
	 * because this condition is necessary but not sufficient.
	 * Use the function useSymbolicLinks() instead of this function
	 * </p>
	 * @param project
	 * @return true if symlinkless build switch is set
	 */
	public static boolean isSymlinklessBuildSwitchedOn(final IProject project) {
		if( project == null) {
			return false;
		}
		String symlinklessBuild = FALSE_STRING;
		try{
			symlinklessBuild = project.getPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, 
							ProjectBuildPropertyData.SYMLINKLESS_BUILD_PROPERTY));
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false; //?
		}
		return TRUE_STRING.equals(symlinklessBuild);
	}
	
	
	/**
	 * Gets if symbolic links shall be generated or not in the given project.
	 * 
	 * @param project - the project reference
	 * @return 	 false if the project reference is null, or
	 * automatic makefile handling and 
	 * internal makefilegen generation and 
	 * symlinkless build are switched on, otherwise
	 * true
	 */
	public static boolean useSymbolicLinks(final IProject project) {
		
		if(useAutomaticMakefilegeneration(project)) {
			if(useInternalMakefilegeneration(project) && isSymlinklessBuildSwitchedOn(project)) {
				return false;
			} else {
				return true; 
			}
		} else {
			return !isSymlinklessBuildSwitchedOn(project);
		}

	}
}
