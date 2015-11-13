/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

/**
 * @author Kristof Szabados
 * */
public final class ProjectFormatConstants {
	public static final String DOM_IMPLEMENTATION_SOURCE = "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl";
	public static final String LOAD_SAVE_VERSION = "LS 3.0";
	public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	public static final String PROJECTNAME_NODE = "ProjectName";
	public static final String PATH_VARIABLES = "PathVariables";
	public static final String PATH_VARIABLE = "PathVariable";
	public static final String FOLDERS_NODE = "Folders";
	public static final String FOLDER_NODE = "FolderResource";
	public static final String FOLDER_ECLIPSE_LOCATION_NODE = "projectRelativePath";
	public static final String FOLDER_RAW_LOCATION = "rawURI";
	public static final String FOLDER_RELATIVE_LOCATION = "relativeURI";
	public static final String FILES_NODE = "Files";
	public static final String FILE_NODE = "FileResource";
	public static final String FILE_ECLIPSE_LOCATION_NODE = "projectRelativePath";
	public static final String FILE_RAW_LOCATION = "rawURI";
	public static final String FILE_RELATIVE_LOCATION = "relativeURI";
	public static final String ACTIVE_CONFIGURATION_NODE = "ActiveConfiguration";
	public static final String CONFIGURATIONS_NODE = "Configurations";
	public static final String CONFIGURATION_NODE = "Configuration";
	public static final String CONFIGURATION_NAME_ATTRIBUTE = "name";
	public static final String DEFAULT_CONFIGURATION_NAME = "Default";
	public static final String REFERENCED_PROJECTS_NODE = "ReferencedProjects";
	public static final String REFERENCED_PROJECT_NODE = "ReferencedProject";
	public static final String REFERENCED_PROJECT_NAME_ATTRIBUTE = "name";
	public static final String REFERENCED_PROJECT_LOCATION_ATTRIBUTE = "projectLocationURI";
	public static final String PACKED_REFERENCED_PROJECTS_NODE = "PackedReferencedProjects";
	public static final String PACKED_REFERENCED_PROJECT_NODE = "PackedReferencedProject";

	private ProjectFormatConstants() {
	}
}
