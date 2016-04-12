/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.PathUtil;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.CCompilerOptionsData;
import org.eclipse.titan.designer.properties.data.DOMErrorHandlerImpl;
import org.eclipse.titan.designer.properties.data.FileBuildPropertyData;
import org.eclipse.titan.designer.properties.data.MakeAttributesData;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;
import org.eclipse.titan.designer.properties.data.TTCN3PreprocessorOptionsData;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * Helper class to load the data of mctr_gui project files, create the linked
 * resources and apply the loaded settings to the created project.
 * 
 * @author Kristof Szabados
 * */
public final class GUIProjectImporter {
	private static final String CREATING_PROJECT = "creating project";
	private static final String CREATION_FAILED = "Project creation failed";

	private static final String DOM_IMPLEMENTATION_SOURCE = "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl";
	private static final String LOAD_SAVE_VERSION = "LS 3.0";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String TRUE = "true";

	private static final class TestSet {
		private String name;
		private List<String> testcases = new ArrayList<String>();

		private TestSet() {
		}
	}

	public static final class IncludedProject {
		private String files;
		private String path;
		private IPath absolutePath;

		private IncludedProject() {
		}

		public String getPath() {
			return path;
		}

		public IPath getAbsolutePath() {
			return absolutePath;
		}
	}

	private static final class FileGroup {
		private String name;
		private List<String> files = new ArrayList<String>();
		private List<FileGroup> groups = new ArrayList<FileGroup>();

		private FileGroup() {
		}
	}

	enum ExecutionModes {
		SINGLE, PARALLEL
	}

	private static enum CodeSplittingModes {
		NONE, TYPE
	}

	static final class ProjectInformation {
		private String sourceFile;
		private String name;
		private String executablePath;
		private String executableName;
		private String workingDir;
		private String buildHost;
		private ExecutionModes executionMode;
		private CodeSplittingModes codeSplittingMode = CodeSplittingModes.NONE;
		private String scriptAfterMake;
		private String gnuMake;
		private String useFunctionTestRuntime;
		private String useDynamicLinking;
		private String useAbsoluteReferencesInMakefile;
		private String localHostExecute;
		private String executeCommand;
		private String executeHosts;
		private List<String> logDirs = new ArrayList<String>();
		private List<String> unUsedList = new ArrayList<String>();
		private List<String> modules = new ArrayList<String>();
		private List<String> testports = new ArrayList<String>();
		private List<String> otherSources = new ArrayList<String>();
		private List<String> configs = new ArrayList<String>();
		private List<String> testcases = new ArrayList<String>();
		private List<TestSet> testsets = new ArrayList<TestSet>();
		private List<String> others = new ArrayList<String>();
		private List<IncludedProject> includeProjects = new ArrayList<IncludedProject>();
		private FileGroup fileGroupRoot = null;

		private ProjectInformation() {
		}

		public String getName() {
			return name;
		}

		public String getSourceFile() {
			return sourceFile;
		}

		public String getWorkingDir() {
			return workingDir;
		}

		public List<IncludedProject> getIncludedProjects() {
			return includeProjects;
		}
	}

	/**
	 * Helper function to convert the file provided into an XML document.
	 * 
	 * @param file
	 *                the project file to be read.
	 * 
	 * @return the XML document read from the file or null if there was an
	 *         error.
	 * */
	private Document getDocumentFromFile(final String file) {
		// DOMImplementationRegistry is a factory that enables
		// applications to obtain instances of a DOMImplementation.
		System.setProperty(DOMImplementationRegistry.PROPERTY, DOM_IMPLEMENTATION_SOURCE);
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return null;
		} catch (InstantiationException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
			return null;
		} catch (IllegalAccessException iae) {
			ErrorReporter.logExceptionStackTrace(iae);
			return null;
		}
		// Specifying "LS 3.0" in the features list ensures that the
		// DOMImplementation
		// object implements the load and save features of the DOM 3.0
		// specification.
		DOMImplementation domImpl = registry.getDOMImplementation(LOAD_SAVE_VERSION);
		DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl;
		// If the mode is MODE_SYNCHRONOUS, the parse and parseURI
		// methods of the LSParser
		// object return the org.w3c.dom.Document object. If the mode is
		// MODE_ASYNCHRONOUS, the parse and parseURI methods return null.
		LSParser parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, XML_SCHEMA);
		DOMConfiguration config = parser.getDomConfig();
		DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("validate", Boolean.TRUE);
		config.setParameter("schema-type", XML_SCHEMA);
		config.setParameter("validate-if-schema", Boolean.TRUE);

		final LSInput lsInput = domImplLS.createLSInput();
		try {
			InputStream istream = new FileInputStream(file);
			lsInput.setByteStream(istream);
			Document document = parser.parse(lsInput);
			istream.close();
			return document;
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		} catch (DOMException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		} catch (LSException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return null;
		}
	}

	/**
	 * Loads the project file provided in the parameter.
	 * 
	 * @param projectFile
	 *                the file to load.
	 * @param monitor
	 *                the monitor used to report progress.
	 * 
	 * @return the read project data, or null if there was an error.
	 * */
	public ProjectInformation loadProjectFile(final String projectFile, final IProgressMonitor monitor, final boolean headless) {
		Document document = getDocumentFromFile(projectFile);
		if (document == null) {
			return null;
		}

		String documentType = document.getDoctype().getNodeName();
		if (!"TITAN_GUI_project_file".equals(documentType)) {
			reportError("Incorrect document type `" + documentType + "' in file `" + projectFile + "'. Expected: `TITAN_GUI_project_file'", headless);
			return null;
		}

		String rootNodeName = document.getDocumentElement().getNodeName();
		if (!"Project".equals(rootNodeName)) {
			reportError("Incorrect root node name `" + rootNodeName + "' in file `" + projectFile + "'. Expected root node name: `Project'", headless);
			return null;
		}

		ProjectInformation projectInformation = new ProjectInformation();
		projectInformation.sourceFile = projectFile;
		IPath path = new Path(projectFile);
		path = path.removeFileExtension();
		projectInformation.name = path.lastSegment();

		final NodeList rootList = document.getDocumentElement().getChildNodes();
		int size = rootList.getLength();
		IProgressMonitor internalMonitor = new SubProgressMonitor(monitor, 1);
		internalMonitor.beginTask("Loading data", size);
		internalMonitor.subTask("Loading Data");
		for (int j = 0; j < size; j++) {
			Node node = rootList.item(j);
			String nodeName = node.getNodeName();
			
			if("#text".equals(nodeName)) {
				//do nothing, empty node because found a "\n"
			} else	if ("General".equals(nodeName)) {
				loadGeneralSettings(node, projectInformation);
			} else if ("Modules".equals(nodeName)) {
				loadListSettings(node, "Module", projectInformation.modules);
			} else if ("TestPorts".equals(nodeName)) {
				loadListSettings(node, "TestPort", projectInformation.testports);
			} else if ("Other_Sources".equals(nodeName)) {
				loadListSettings(node, "Other_Source", projectInformation.otherSources);
			} else if ("Configs".equals(nodeName)) {
				loadListSettings(node, "Config", projectInformation.configs);
			} else if ("Test_Cases".equals(nodeName)) {
				loadListSettings(node, "Test_Case", projectInformation.testcases);
			} else if ("Test_Sets".equals(nodeName)) {
				loadTestSets(node, projectInformation);
			} else if ("Others".equals(nodeName)) {
				loadListSettings(node, "Other", projectInformation.others);
			} else if ("Included_Projects".equals(nodeName)) {
				loadIncludedProjects(node, projectInformation, internalMonitor);
			} else if ("File_Group".equals(nodeName)) {
				projectInformation.fileGroupRoot = new FileGroup();
				loadFileGroups(node, projectInformation.fileGroupRoot, projectFile, internalMonitor,headless);
			} else {
				reportError("Incorrect node name `" + nodeName + "' within node `Project' in file `" + projectFile + "'", headless);
			}
			internalMonitor.worked(1);
		}
		internalMonitor.done();

		return projectInformation;
	}
	
	private void reportError(final String message, final boolean headless) {
		ErrorReporter.logError("Import from prj error: " + message);
		//		if(!headless) {
		//			ErrorReporter.parallelErrorDisplayInMessageDialog("Import from Prj Error", message + " in file `" + projectFile + "'");
		//		}
	}

	/**
	 * Load the general settings from the provided XML node into the
	 * provided project information structure.
	 * 
	 * @param generalNode
	 *                the node toe load the data from.
	 * @param projectInformation
	 *                the structure to fill.
	 * */
	private void loadGeneralSettings(final Node generalNode, final ProjectInformation projectInformation) {
		final NodeList nodeList = generalNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeName = node.getNodeName();
			if("#text".equals(nodeName)) {
				//do nothing, empty node because found a "\n"
			} else if ("Project_Name".equals(nodeName)) {
				projectInformation.name = node.getTextContent();
			} else if ("Executable_Path".equals(nodeName)) {
				projectInformation.executablePath = node.getTextContent();
			} else if ("Executable_Name".equals(nodeName)) {
				projectInformation.executableName = node.getTextContent();
			} else if ("Working_Dir".equals(nodeName)) {
				projectInformation.workingDir = node.getTextContent();
			} else if ("Execution_Mode".equals(nodeName)) {
				final String context = node.getTextContent();
				projectInformation.executionMode = "Single".equals(context) ? ExecutionModes.SINGLE : ExecutionModes.PARALLEL;
			} else if ("Code_Splitting_Mode".equals(nodeName)) {
				String temp = node.getTextContent();
				if ("Type".equals(temp)) {
					projectInformation.codeSplittingMode = CodeSplittingModes.TYPE;
				}
			} else if ("ScriptFile_AfterMake".equals(nodeName)) {
				projectInformation.scriptAfterMake = node.getTextContent();
			} else if ("GNU_Make".equals(nodeName)) {
				projectInformation.gnuMake = node.getTextContent();
			} else if ("Generate_Absolute_references_In_Makefile".equals(nodeName)) {
				projectInformation.useAbsoluteReferencesInMakefile = node.getTextContent();
			} else if ("Use_FunctionTest_Runtime_In_Makefile".equals(nodeName)) {
				projectInformation.useFunctionTestRuntime = node.getTextContent();
			} else if ("Use_Dynamic_Linking_In_Makefile".equals(nodeName)) {
				projectInformation.useDynamicLinking = node.getTextContent();
			} else if ("Build_Host".equals(nodeName)) {
				projectInformation.buildHost = node.getTextContent();
			} else if ("Localhost_Execute".equals(nodeName)) {
				projectInformation.localHostExecute = node.getTextContent();
			} else if ("Execute_Command".equals(nodeName)) {
				projectInformation.executeCommand = node.getTextContent();
			} else if ("Execute_Hosts".equals(nodeName)) {
				projectInformation.executeHosts = node.getTextContent();
			} else if ("Log_Dir".equals(nodeName)) {
				projectInformation.logDirs.add(node.getTextContent());
			} else if ("UnUsed_List".equals(nodeName)) {
				String temp = node.getTextContent();
				String[] temp2 = temp.split(",");
				Collections.addAll(projectInformation.unUsedList, temp2);
			}
			// Log_Format, Update_Symlinks,
			// Create_Absolute_Symlinks, Update_Makefile are not
			// supported as their meaning is hard to interpret.
		}
	}

	/**
	 * Applies the read settings to the newly created project, also creating
	 * all of the needed links.
	 * 
	 * @param project
	 *                the project to be set up.
	 * @param info
	 *                the information to be used to set up the project.
	 * @param monitor
	 *                the monitor to report progress info on.
	 * 
	 * @throws CoreException
	 *                 in case of problems.
	 * */
	public static void applySettings(final IProject project, final ProjectInformation info, final IProgressMonitor monitor) throws CoreException {
		IProgressMonitor internalMonitor = new SubProgressMonitor(monitor, 1);
		internalMonitor.beginTask("Creating settings", 9);
		internalMonitor.subTask("Creating settings");
		setGeneralSettings(project, info, internalMonitor);
		internalMonitor.worked(1);

		// modules
		setListSettings(project, info.sourceFile, info.modules, internalMonitor);
		// test ports
		setListSettings(project, info.sourceFile, info.testports, internalMonitor);
		// other source files
		setListSettings(project, info.sourceFile, info.otherSources, internalMonitor);
		// configuration files
		setListSettings(project, info.sourceFile, info.configs, internalMonitor);
		// other files
		setListSettings(project, info.sourceFile, info.others, internalMonitor);

		setGroupFileSettings(project, info.sourceFile, info.fileGroupRoot, internalMonitor);
		setIncludedProjects(project, info);
		internalMonitor.worked(1);
		setUnusedFiles(project, info, internalMonitor);
		internalMonitor.worked(1);
		internalMonitor.done();
	}

	/**
	 * Applies the general part of the settings to the newly created
	 * project.
	 * 
	 * @param project
	 *                the project to be set up.
	 * @param info
	 *                the information to be used to set up the project.
	 * @param monitor
	 *                the monitor to report progress on.
	 * */
	public static void setGeneralSettings(final IProject project, final ProjectInformation info, final IProgressMonitor monitor) {
		try {
			// these always need to be set.
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY), TRUE);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TTCN3PreprocessorOptionsData.TTCN3_PREPROCESSOR_PROPERTY), "cpp");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					CCompilerOptionsData.CXX_COMPILER_PROPERTY), "g++");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					TITANFlagsOptionsData.ADD_SOURCELINEINFO_PROPERTY), TRUE);

			monitor.subTask("Resolving the path `" + info.workingDir + "'");
			String absolutePath = PathConverter.getAbsolutePath(project.getLocation().toOSString(), info.workingDir);
			String relativePath;
			if (absolutePath == null) {
				relativePath = "bin";
			} else {
				relativePath = PathUtil.getRelativePath(project.getLocation().toOSString(), absolutePath);
			}
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakeAttributesData.TEMPORAL_WORKINGDIRECTORY_PROPERTY), relativePath);

			// read settings
			if (info.executablePath != null) {
				monitor.subTask("Resolving the path `" + info.executablePath + "'");
				absolutePath = PathConverter.getAbsolutePath(project.getLocation().toOSString(), info.executablePath);
			} else {
				monitor.subTask("Resolving the path `" + info.workingDir + "'");
				absolutePath = PathConverter.getAbsolutePath(project.getLocation().toOSString(), info.workingDir);
				absolutePath = absolutePath + File.separatorChar + info.executableName;
			}
			if (absolutePath == null) {
				absolutePath = "bin" + File.separatorChar + info.executableName;
			}
			relativePath = PathUtil.getRelativePath(project.getLocation().toOSString(), absolutePath);
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.TARGET_EXECUTABLE_PROPERTY), relativePath);
			project.setPersistentProperty(
					new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakefileCreationData.SINGLEMODE_PROPERTY),
					ExecutionModes.SINGLE.equals(info.executionMode) ? "true" : "false");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.CODE_SPLITTING_PROPERTY),
					CodeSplittingModes.TYPE.equals(info.codeSplittingMode) ? "type" : "none");

			if (info.scriptAfterMake != null) {
				monitor.subTask("Resolving the path `" + info.scriptAfterMake + "'");
				absolutePath = PathConverter.getAbsolutePath(info.sourceFile, info.scriptAfterMake);
				if (absolutePath == null) {
					relativePath = null;
				} else {
					relativePath = PathUtil.getRelativePath(project.getLocation().toOSString(), absolutePath);
				}
				project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						MakeAttributesData.TEMPORAL_MAKEFILE_SCRIPT_PROPERTY), relativePath);
			}

			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, MakefileCreationData.GNU_MAKE_PROPERTY),
					"yes".equals(info.gnuMake) ? "true" : "false");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.USE_ABSOLUTEPATH_PROPERTY), "yes".equals(info.useAbsoluteReferencesInMakefile) ? "true"
					: "false");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.FUNCTIONTESTRUNTIME_PROPERTY), "yes".equals(info.useFunctionTestRuntime) ? "true"
					: "false");
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.DYNAMIC_LINKING_PROPERTY), "yes".equals(info.useDynamicLinking) ? "true" : "false");

			for (int i = 0, size = info.logDirs.size(); i < size; i++) {
				monitor.subTask("Resolving the path `" + info.logDirs.get(i) + "'");
				absolutePath = PathConverter.getAbsolutePath(info.sourceFile, info.logDirs.get(i));
				if (absolutePath == null) {
					continue;
				}

				IPath path = new Path(absolutePath);
				IPath locationPath = project.getLocation();
				if (!locationPath.isPrefixOf(path)) {
					IFolder folder = project.getFolder("Log_Dir" + (i + 1));
					URI linkTarget = URIUtil.toURI(absolutePath);
					folder.createLink(linkTarget, IResource.ALLOW_MISSING_LOCAL, null);
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * The originally unused files are searched out and set as excluded from
	 * build in the newly created project.
	 * 
	 * @param project
	 *                the project to be set up.
	 * @param info
	 *                the information to be used to set up the project.
	 * @param monitor
	 *                the monitor to report progress on.
	 * 
	 * @throws CoreException
	 *                 in case of problems.
	 * */
	public static void setUnusedFiles(final IProject project, final ProjectInformation info, final IProgressMonitor monitor) throws CoreException {
		for (int i = 0, size = info.unUsedList.size(); i < size; i++) {
			String temp = info.unUsedList.get(i);
			monitor.subTask("Resolving the path `" + temp + "'");
			String absolutePath = PathConverter.getAbsolutePath(info.sourceFile, temp);
			if (absolutePath == null) {
				continue;
			}

			IPath path = new Path(absolutePath);
			IFile file = project.getFile(path.lastSegment());
			if (file.isAccessible()) {
				QualifiedName excludedFileQualifier = new QualifiedName(FileBuildPropertyData.QUALIFIER,
						FileBuildPropertyData.EXCLUDE_FROM_BUILD_PROPERTY);
				file.setPersistentProperty(excludedFileQualifier, "true");
			}
		}
	}

	/**
	 * Creates a linked resource in the provided project, if it would be
	 * outside the location of the project, and does not already exists.
	 * 
	 * @param project
	 *                the project where the link should be created.
	 * @param absolutePath
	 *                the absolute path where the linked resource should
	 *                point to.
	 * */
	private static void createLink(final IProject project, final String absolutePath) {
		if (absolutePath == null) {
			return;
		}

		IPath path = new Path(absolutePath);
		IPath locationPath = project.getLocation();
		if (!locationPath.isPrefixOf(path)) {
			IFile file = project.getFile(path.lastSegment());
			URI linkTarget = URIUtil.toURI(absolutePath);
			if (file.exists()) {
				URI fileLocation = file.getLocationURI().normalize();
				if (!fileLocation.equals(linkTarget.normalize()) && !fileLocation.equals(linkTarget)) {
					ErrorReporter.logWarning("The file `" + file.getName() + "' could not be created pointing to location `"
							+ linkTarget + "' as it already exists and is pointing to `" + fileLocation + "'");
				}
			} else {
				try {
					file.createLink(linkTarget, IResource.ALLOW_MISSING_LOCAL, null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While creating link `" + file.getName() + "' pointing to `"
							+ linkTarget + "'", e);
				}
			}
		}
	}

	/**
	 * Load the list like settings where every element is a string, from the
	 * provided XML node, into the provided project information structure.
	 * 
	 * @param generalNode
	 *                the node toe load the data from.
	 * */
	private void loadListSettings(final Node generalNode, final String expectedNodeName, final List<String> list) {
		final NodeList nodeList = generalNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeName = node.getNodeName();
			if (expectedNodeName.equals(nodeName)) {
				list.add(node.getTextContent());
			}
		}
	}

	/**
	 * Creates the link for those file type lists, where the relative path
	 * is provided as a simple string.
	 * 
	 * @param project
	 *                the project, where the links should be created.
	 * @param basePath
	 *                the path that should be used as base when calculating
	 *                the absolute path.
	 * @param list
	 *                the list of relative paths, for which links should be
	 *                created.
	 * @param monitor
	 *                the monitor to report progress on.
	 * */
	private static void setListSettings(final IProject project, final String basePath, final List<String> list, final IProgressMonitor monitor) {
		IProgressMonitor internalMonitor = new SubProgressMonitor(monitor, 1);
		internalMonitor.beginTask("Creating links", list.size());
		for (String element : list) {
			internalMonitor.subTask("Resolving the path `" + element + "'");
			String absolutePath = PathConverter.getAbsolutePath(basePath, element);
			if (absolutePath != null) {
				internalMonitor.subTask("creating link for `" + absolutePath + "'");
				createLink(project, absolutePath);
			}
			internalMonitor.worked(1);
		}
		internalMonitor.done();
	}

	/**
	 * Load the test set related information from the provided XML node into
	 * the provided project information structure.
	 * 
	 * @param generalNode
	 *                the node toe load the data from.
	 * @param projectInformation
	 *                the structure to fill.
	 * */
	private void loadTestSets(final Node generalNode, final ProjectInformation projectInformation) {
		final NodeList nodeList = generalNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeName = node.getNodeName();
			if ("Test_Set".equals(nodeName)) {
				String name;
				List<String> testcases = new ArrayList<String>();
				Node namedAttribute = node.getAttributes().getNamedItem("name");
				if (namedAttribute != null) {
					name = namedAttribute.getTextContent();
					final NodeList nodeList2 = node.getChildNodes();
					for (int j = 0; j < nodeList2.getLength(); j++) {
						Node node2 = nodeList2.item(j);
						String nodeName2 = node2.getNodeName();
						if ("TC".equals(nodeName2)) {
							testcases.add(node2.getTextContent());
						}
					}
					TestSet set = new TestSet();
					set.name = name;
					set.testcases = testcases;
					projectInformation.testsets.add(set);
				} else {
					reportError("One of the Test sets does not have the mandatory `name' attribute", false);
				}
			}
		}
	}

	/**
	 * Load the included files related settings from the provided XML node
	 * into the provided project information structure.
	 * 
	 * @param generalNode
	 *                the node toe load the data from.
	 * @param projectInformation
	 *                the structure to fill.
	 * @param monitor
	 *                the monitor to report progress on.
	 * */
	private void loadIncludedProjects(final Node generalNode, final ProjectInformation projectInformation, final IProgressMonitor monitor) {
		final NodeList nodeList = generalNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeName = node.getNodeName();
			if ("Included_Project".equals(nodeName)) {
				Node filesAttribute = node.getAttributes().getNamedItem("included_files");
				Node pathAttribute = node.getAttributes().getNamedItem("path");
				if (filesAttribute != null && pathAttribute != null) {
					IncludedProject included = new IncludedProject();
					included.files = filesAttribute.getTextContent();
					included.path = pathAttribute.getTextContent();

					monitor.subTask("Resolving the path `" + included.path + "'");
					String absolutePath = PathConverter.getAbsolutePath(projectInformation.sourceFile, included.path);
					if (absolutePath == null) {
						included.absolutePath = null;
					} else {
						included.absolutePath = new Path(absolutePath);
					}

					projectInformation.includeProjects.add(included);
				} else {
					reportError("One of the included projects is specified incorrectly", false);
				}
			}
		}
	}

	/**
	 * Applies the included project settings to the newly created project as
	 * references to other projects..
	 * 
	 * @param project
	 *                the project to be set up.
	 * @param info
	 *                the information to be used to set up the project.
	 * 
	 * @throws CoreException
	 *                 in case of problems.
	 * */
	public static void setIncludedProjects(final IProject project, final ProjectInformation info) throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProjectDescription description = project.getDescription();
		IProject[] referencedProjects = description.getReferencedProjects();
		List<IProject> references = Arrays.asList(referencedProjects);

		for (int i = 0, size = info.includeProjects.size(); i < size; i++) {
			IPath path = info.includeProjects.get(i).absolutePath;
			if (path == null) {
				continue;
			}

			path = path.removeFileExtension();
			String includedProjectName = path.lastSegment();

			IProject newProject = workspace.getRoot().getProject(includedProjectName);
			references.add(newProject);
		}

		description.setReferencedProjects(references.toArray(new IProject[references.size()]));
		project.setDescription(description, null);
	}

	/**
	 * Load the information regard file groups and the groups themselves,
	 * recursively starting from the provided XML node into the provided
	 * project information structure.
	 * 
	 * @param generalNode
	 *                the node toe load the data from.
	 * @param fileGroup
	 *                the group to serve as the base one.
	 * @param basePath
	 *                the actual base path, the information is stored
	 *                relative to in the group file.
	 * @param monitor
	 *                the monitor to report progress to.
	 * */
	private void loadFileGroups(final Node generalNode, final FileGroup fileGroup, final String basePath, final IProgressMonitor monitor, final boolean headless) {
		Node pathAttribute = generalNode.getAttributes().getNamedItem("path");
		Node nameAttribute = generalNode.getAttributes().getNamedItem("name");
		boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		if (pathAttribute != null) {
			String path = pathAttribute.getTextContent();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Read the file group path `" + path + "'");
			}
			monitor.subTask("Resolving the path `" + path + "'");
			path = PathConverter.getAbsolutePath(basePath, path);
			if (reportDebugInformation) {
				TITANDebugConsole.println("The absolute file group path relative to `" + basePath + "' is `" + path + "'");
			}
			if (path == null) {
				return;
			}

			File file = new File(path);
			if (!file.exists()) {
				reportError("Could not load the group file `" + path + "' referred by `" + basePath + "' as `"
						+ pathAttribute.getTextContent() + "'", headless);
				return;
			}

			Document document = getDocumentFromFile(path);
			if (document == null) {
				reportError("Could not load the group from the group file `" + path + "'", headless);
				return;
			}

			FileGroup newGroup = new FileGroup();
			IProgressMonitor groupMonitor = new SubProgressMonitor(monitor, 1);
			groupMonitor.beginTask("Loading group: " + file.getAbsolutePath(), 1);
			groupMonitor.subTask("Loading group: " + file.getAbsolutePath());
			loadFileGroups(document.getDocumentElement(), newGroup, path, groupMonitor,headless);
			groupMonitor.done();
			fileGroup.groups.add(newGroup);
			if (reportDebugInformation) {
				TITANDebugConsole.println("Added file group `" + newGroup.name + "'");
			}
			return;
		}

		if (nameAttribute != null) {
			fileGroup.name = nameAttribute.getTextContent();
			if (reportDebugInformation) {
				TITANDebugConsole.println("Read the file group name `" + fileGroup.name + "'");
			}
		} else {
			fileGroup.name = "<nameless>";
		}

		final NodeList nodeList = generalNode.getChildNodes();
		IProgressMonitor groupMonitor = new SubProgressMonitor(monitor, 1);
		groupMonitor.beginTask("Loading group: " + fileGroup.name, nodeList.getLength());
		groupMonitor.subTask("Loading group: " + fileGroup.name);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeName = node.getNodeName();
			if ("File".equals(nodeName)) {
				Node pathAttribute2 = node.getAttributes().getNamedItem("path");
				if (pathAttribute2 == null) {
					reportError("One of the `File' nodes in the group `" + fileGroup.name + "' does not have a `path' attribute",headless);
					continue;
				}
				if (reportDebugInformation) {
					TITANDebugConsole.println("Read the named file group path `" + pathAttribute2.getTextContent() + "'");
				}
				groupMonitor.subTask("Resolving the path `" + pathAttribute2.getTextContent() + "'");
				String path = PathConverter.getAbsolutePath(basePath, pathAttribute2.getTextContent());
				if (reportDebugInformation) {
					TITANDebugConsole.println("The named absolute file group path relative to `" + basePath + "' is `" + path + "'");
				}
				if (path == null) {
					continue;
				}

				fileGroup.files.add(path);
				if (reportDebugInformation) {
					TITANDebugConsole.println("Added file `" + path + "'");
				}
			} else if ("File_Groups".equals(nodeName)) {
				final NodeList nodeList2 = node.getChildNodes();
				IProgressMonitor groupMonitor2 = new SubProgressMonitor(groupMonitor, 1);
				groupMonitor2.beginTask("Loading internal group nodes", nodeList2.getLength());
				for (int j = 0; j < nodeList2.getLength(); j++) {
					Node node2 = nodeList2.item(j);
					String nodeName2 = node2.getNodeName();
					if ("File_Group".equals(nodeName2)) {
						FileGroup newGroup = new FileGroup();
						loadFileGroups(node2, newGroup, basePath, groupMonitor2,headless);
						fileGroup.groups.add(newGroup);
					}
					groupMonitor2.worked(1);
				}
				groupMonitor2.done();
			} else if ("File_Group".equals(nodeName)) {
				FileGroup newGroup = new FileGroup();
				loadFileGroups(node, newGroup, basePath, groupMonitor,headless);
				fileGroup.groups.add(newGroup);
			}
			groupMonitor.worked(1);
		}
		groupMonitor.done();
	}

	/**
	 * Applies the group files related settings to the newly created
	 * project, also creating all of the needed links.
	 * 
	 * @param project
	 *                the project to be set up.
	 * @param monitor
	 *                the monitor to report progress to.
	 * 
	 * @throws CoreException
	 *                 in case of problems.
	 * */
	public static void setGroupFileSettings(final IProject project, final String sourceFile, final FileGroup group, final IProgressMonitor monitor)
			throws CoreException {
		if (group == null) {
			return;
		}

		IProgressMonitor internalMonitor = new SubProgressMonitor(monitor, 1);
		internalMonitor.beginTask("Creating settings", group.files.size() + group.groups.size());
		for (int i = 0, size = group.files.size(); i < size; i++) {
			String temp = group.files.get(i);
			internalMonitor.subTask("Resolving the path `" + temp + "'");
			String absolutePath = PathConverter.getAbsolutePath(sourceFile, temp);
			if (absolutePath != null) {
				internalMonitor.subTask("creating link for `" + absolutePath + "'");
				createLink(project, absolutePath);
			}
			internalMonitor.worked(1);
		}

		for (int i = 0, size = group.groups.size(); i < size; i++) {
			setGroupFileSettings(project, sourceFile, group.groups.get(i), internalMonitor);
		}
		internalMonitor.done();
	}

	/**
	 * Creating a new project.
	 * 
	 * @param description
	 *                - IProjectDescription that belongs to the newly
	 *                created project.
	 * @param projectHandle
	 *                - a project handle that is used to create the new
	 *                project.
	 * @param monitor
	 *                - reference to the monitor object
	 * @exception CoreException
	 *                    thrown if access to the resources throws a
	 *                    CoreException.
	 * @exception OperationCanceledException
	 *                    if the operation was canceled by the user.
	 */
	protected static void createProject(final IProjectDescription description, final IProject projectHandle, final IProgressMonitor monitor)
			throws CoreException {
		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		try {
			internalMonitor.beginTask(CREATING_PROJECT, 2000);

			projectHandle.create(description, new SubProgressMonitor(internalMonitor, 1000));

			if (internalMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(internalMonitor, 1000));

			projectHandle.refreshLocal(IResource.DEPTH_ONE, internalMonitor);
		} finally {
			internalMonitor.done();
		}
	}

		
	/**
	 * Creating a new project. FIXME document
	 * 
	 * @param newProjectHandle
	 * 			Project information will be stored in this structure in Eclipse-logic
	 * @param info
	 *   		project information gathered from the .prj project descriptor file
	 *   
	 * @param targetLocation
	 * 			the location where the new project shall be created,
	 * 			or null if in the workspace
	 * 
	 * @return the new project created.
	 */
	static IProject createNewProject(final IProject newProjectHandle, final ProjectInformation info, final URI targetLocation) {
		if (info == null) {
			return null;
		}

		IProject newProject;

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(info.getName());

		description.setLocationURI(targetLocation);
		TITANNature.addTITANNatureToProject(description);

		try {
			createProject(description, newProjectHandle, new NullProgressMonitor());
			GUIProjectImporter.applySettings(newProjectHandle, info, new NullProgressMonitor());
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		newProject = newProjectHandle;

		return newProject;
	}

	/**
	 * Imports a project from .prj file in headless mode
	 * 
	 * @param projectFile
	 *      file name with full path
	 * @return true if the import was successful
	 */
	public static boolean importProjectfromPrj(final String projectFile) {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		List<String> processedProjectFiles = new ArrayList<String>();
		List<IPath> projectFilesToBeProcessed = new ArrayList<IPath>();
		projectFilesToBeProcessed.add(new Path(projectFile));

		while (!projectFilesToBeProcessed.isEmpty()) {
			IPath tempPath = projectFilesToBeProcessed.remove(projectFilesToBeProcessed.size() - 1);
			if (processedProjectFiles.contains(tempPath.toOSString())) {
				continue;
			}

			processedProjectFiles.add(tempPath.toOSString());

			GUIProjectImporter importer = new GUIProjectImporter();
			ProjectInformation tempProjectInformation = importer.loadProjectFile(tempPath.toOSString(), new NullProgressMonitor(),true); //true: headless
			if(tempProjectInformation == null) {
				continue;
			}
			
			IPath tempPath2 = tempPath.removeFileExtension();
			String includedProjectName = tempPath2.lastSegment();

			IProject tempProject = workspace.getRoot().getProject(includedProjectName);
			if (tempProject.exists()) {
				continue;
			}

			tempProject = createNewProject(tempProject, tempProjectInformation, null);
			if (tempProject == null) {
				continue;
			}

			try {
				TITANNature.addTITANBuilderToProject(tempProject);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}

			ProjectFileHandler pfHandler = new ProjectFileHandler(tempProject);
			pfHandler.saveProjectSettings();

			try {
				tempProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}

			List<IncludedProject> includedProjects = tempProjectInformation.getIncludedProjects();
			for (IncludedProject includedProject : includedProjects) {
				IPath temp = includedProject.getAbsolutePath();
				if (temp != null) {
					projectFilesToBeProcessed.add(temp);
				}
			}
		}

		return true;
	}
}
