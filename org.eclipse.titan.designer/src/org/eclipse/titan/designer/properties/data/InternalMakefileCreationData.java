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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is a high level class serving as entry point for handling property data
 * that is used by the Designer. Also loading, saving of these properties from
 * and into external formats.
 * 
 * @author Kristof Szabados
 * */
public final class InternalMakefileCreationData {

	private InternalMakefileCreationData() {
		// Do nothing
	}

	/**
	 * Remove the TITAN provided attributes from a project.
	 * 
	 * @param project
	 *                the project to remove the attributes from.
	 * */
	public static void removeTITANAttributes(final IProject project) {
		TTCN3PreprocessorOptionsData.removeTITANAttributes(project);
		PreprocessorSymbolsOptionsData.removeTITANAttributes(project);
		PreprocessorIncludedOptionsData.removeTITANAttributes(project);
		TITANFlagsOptionsData.removeTITANAttributes(project);
		CCompilerOptionsData.removeTITANAttributes(project);
		COptimalizationOptionsData.removeTITANAttributes(project);
		PlatformSpecificLibrariesOptionsData.removeTITANAttributes("Solaris", project);
		PlatformSpecificLibrariesOptionsData.removeTITANAttributes("Solaris8", project);
		PlatformSpecificLibrariesOptionsData.removeTITANAttributes("FreeBSD", project);
		PlatformSpecificLibrariesOptionsData.removeTITANAttributes("Linux", project);
		PlatformSpecificLibrariesOptionsData.removeTITANAttributes("Win32", project);
		LinkerLibrariesOptionsData.removeTITANAttributes(project);
		LinkerFlagsOptionsData.removeTITANAttributes(project);
	}

	/**
	 * Loads and sets the Makefile related settings contained in the XML
	 * tree for this project.
	 * 
	 * @see ProjectFileHandler#loadProjectSettings()
	 * @see ProjectBuildPropertyData#loadMakefileSettings(Node, IProject,
	 *      HashSet)
	 * 
	 * @param node
	 *                the root of the subtree containing Makefile related
	 *                attributes
	 * @param project
	 *                the project to set the found attributes on.
	 * */
	public static void loadMakefileSettings(final Node node, final IProject project) {
		TTCN3PreprocessorOptionsData.loadMakefileSettings(node, project);
		PreprocessorSymbolsOptionsData.loadMakefileSettings(node, project);
		PreprocessorIncludedOptionsData.loadMakefileSettings(node, project);
		TITANFlagsOptionsData.loadMakefileSettings(node, project);
		CCompilerOptionsData.loadMakefileSettings(node, project);
		COptimalizationOptionsData.loadMakefileSettings(node, project);
		PlatformSpecificLibrariesOptionsData.loadMakefileSettings("Solaris", node, project);
		PlatformSpecificLibrariesOptionsData.loadMakefileSettings("Solaris8", node, project);
		PlatformSpecificLibrariesOptionsData.loadMakefileSettings("FreeBSD", node, project);
		PlatformSpecificLibrariesOptionsData.loadMakefileSettings("Linux", node, project);
		PlatformSpecificLibrariesOptionsData.loadMakefileSettings("Win32", node, project);
		LinkerLibrariesOptionsData.loadMakefileSettings(node, project);
		LinkerFlagsOptionsData.loadMakefileSettings(node, project);
	}

	/**
	 * Creates an XML tree from the Makefile related settings of the
	 * project.
	 * 
	 * @see ProjectFileHandler#saveProjectSettings()
	 * @see ProjectBuildPropertyData#saveMakefileSettings(Document,
	 *      IProject)
	 * 
	 * @param makefileSettings
	 *                the node to use as root node
	 * @param document
	 *                the document used for creating the tree nodes
	 * @param project
	 *                the project to work on
	 * */
	public static void saveMakefileSettings(final Element makefileSettings, final Document document, final IProject project) {
		TTCN3PreprocessorOptionsData.saveMakefileSettings(makefileSettings, document, project);
		PreprocessorSymbolsOptionsData.saveMakefileSettings(makefileSettings, document, project);
		PreprocessorIncludedOptionsData.saveMakefileSettings(makefileSettings, document, project);
		TITANFlagsOptionsData.saveMakefileSettings(makefileSettings, document, project);
		CCompilerOptionsData.saveMakefileSettings(makefileSettings, document, project);
		COptimalizationOptionsData.saveMakefileSettings(makefileSettings, document, project);
		PlatformSpecificLibrariesOptionsData.saveMakefileSettings("Solaris", makefileSettings, document, project);
		PlatformSpecificLibrariesOptionsData.saveMakefileSettings("Solaris8", makefileSettings, document, project);
		PlatformSpecificLibrariesOptionsData.saveMakefileSettings("FreeBSD", makefileSettings, document, project);
		PlatformSpecificLibrariesOptionsData.saveMakefileSettings("Linux", makefileSettings, document, project);
		PlatformSpecificLibrariesOptionsData.saveMakefileSettings("Win32", makefileSettings, document, project);
		LinkerLibrariesOptionsData.saveMakefileSettings(makefileSettings, document, project);
		LinkerFlagsOptionsData.saveMakefileSettings(makefileSettings, document, project);
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
		TTCN3PreprocessorOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		PreprocessorSymbolsOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		PreprocessorIncludedOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		TITANFlagsOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		CCompilerOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		COptimalizationOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		PlatformSpecificLibrariesOptionsData.copyMakefileSettings("Solaris", source, makefileSettings, document, saveDefaultValues);
		PlatformSpecificLibrariesOptionsData.copyMakefileSettings("Solaris8", source, makefileSettings, document, saveDefaultValues);
		PlatformSpecificLibrariesOptionsData.copyMakefileSettings("FreeBSD", source, makefileSettings, document, saveDefaultValues);
		PlatformSpecificLibrariesOptionsData.copyMakefileSettings("Linux", source, makefileSettings, document, saveDefaultValues);
		PlatformSpecificLibrariesOptionsData.copyMakefileSettings("Win32", source, makefileSettings, document, saveDefaultValues);
		LinkerLibrariesOptionsData.copyMakefileSettings(source, makefileSettings, document, saveDefaultValues);
		LinkerFlagsOptionsData.copyMakefileSettings(source,makefileSettings, document, true);
	}
}
