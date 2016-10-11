/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles the project level naming conventions related property settings of
 * projects. Also loading, saving of these properties from and into external
 * formats.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectNamingConventionPropertyData {
	public static final String NAMINGCONVENTIONS_XMLNODE = "NamingCoventions";
	public static final String QUALIFIER = ProductConstants.PRODUCT_ID_DESIGNER + ".Properties.Project.NamingConventions";

	public static final String[] PROPERTIES = { PreferenceConstants.ENABLEPROJECTSPECIFICNAMINGCONVENTIONS,
			PreferenceConstants.REPORTNAMINGCONVENTION_TTCN3MODULE, PreferenceConstants.REPORTNAMINGCONVENTION_ASN1MODULE,
			PreferenceConstants.REPORTNAMINGCONVENTION_ALTSTEP, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_CONSTANT,
			PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALCONSTANT, PreferenceConstants.REPORTNAMINGCONVENTION_FUNCTION,
			PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, PreferenceConstants.REPORTNAMINGCONVENTION_MODULEPAR,
			PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_PORT, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TEMPLATE,
			PreferenceConstants.REPORTNAMINGCONVENTION_TESTCASE, PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER,
			PreferenceConstants.REPORTNAMINGCONVENTION_TYPE, PreferenceConstants.REPORTNAMINGCONVENTION_GROUP,
			PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_CONSTANT, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARIABLE,
			PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TEMPLATE, PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE,
			PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER,
			PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_CONSTANT, PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_VARIABLE,
			PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER };
	public static final String[] TAGS = { "enableProjectSpecificSettings", "TTCN3ModuleName", "ASN1ModuleName", "altstep", "globalConstant",
			"externalConstant", "function", "externalFunction", "moduleParameter", "globalPort", "globalTemplate", "testcase",
			"globalTimer", "type", "group", "localConstant", "localVariable", "localTemplate", "localVariableTemplate", "localTimer",
			"formalParameter", "componentConstant", "componentVariable", "componentTimer" };
	public static final String[] DEFAULT_VALUES = { "", ".*", ".*", "as_.*", "cg_.*", "ec_.*", "f_.*", "ef_.*", "m.*", ".*_PT", "t.*", "tc_.*",
			"T.*", ".*", "[A-Z].*", "cl.*", "vl.*", "t.*", "vt.*", "TL_.*", "pl_.*", "c_.*", "v_.*", "T_.*" };

	private ProjectNamingConventionPropertyData() {
		// Do nothing
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IProject project) {
		BaseNamingConventionPropertyData.removeTITANAttributes(project, ProjectBuildPropertyData.QUALIFIER, PROPERTIES);
	}

	/**
	 * Loads and sets the naming convention related settings contained in
	 * the XML tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * 
	 * @param root
	 *                the root of the subtree containing naming convention
	 *                related attributes, or null if no such was loaded
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadProperties(final Node root, final IProject project) {
		BaseNamingConventionPropertyData.loadProperties(root, project, QUALIFIER, PROPERTIES, TAGS, DEFAULT_VALUES);
	}

	/**
	 * Creates an XML tree from the naming conventions related settings of
	 * the project.
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
		return BaseNamingConventionPropertyData.saveProperties(document, project, QUALIFIER, PROPERTIES, TAGS, DEFAULT_VALUES,
				NAMINGCONVENTIONS_XMLNODE);
	}

	/**
	 * Copies the project information related to naming conventions from the
	 * source node to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject,
	 *      TreeMap, TreeMap, boolean)
	 * 
	 * @param source
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
	public static Element copyProperties(final Node source, final Document document, final boolean saveDefaultValues) {
		return BaseNamingConventionPropertyData.copyProperties(source, document, saveDefaultValues, NAMINGCONVENTIONS_XMLNODE, PROPERTIES,
				TAGS, DEFAULT_VALUES);
	}
}
