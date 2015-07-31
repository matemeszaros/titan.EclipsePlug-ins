/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.properties.data.DOMErrorHandlerImpl;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author Kristof Szabados
 * */
public class TITANProjectExportWizard extends Wizard implements IExportWizard {

	private static final String NEWPROJECT_WINDOWTITLE = "Import new TITAN Project from TITAN Project descriptor file";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Project";
	private static final String NEWPROJECT_DESCRIPTION = "Create a new TITAN project in the workspace or in an external location";

	private IStructuredSelection selection;
	private IProject project = null;
	private String projectFile;

	private TITANProjectExportMainPage mainPage;
	private TITANProjectExportOptionsPage optionsPage;

	public static final class ExportResourceVisitor implements IResourceVisitor {
		private static final String DOT = ".";

		private final IContainer[] workingDirectories;
		private final boolean excludeWorkingdirectoryContents;
		private final boolean excludeDotResources;
		private final boolean excludeLinkedContents;

		private final Map<String, IFolder> visitedFolders = new TreeMap<String, IFolder>();
		private final Map<String, IFile> visitedFiles = new TreeMap<String, IFile>();

		public ExportResourceVisitor(final IContainer[] workingDirectories, final boolean excludeWorkingdirectoryContents,
				final boolean excludeDotResources, final boolean excludeLinkedContents) {
			this.workingDirectories = workingDirectories;
			this.excludeWorkingdirectoryContents = excludeWorkingdirectoryContents;
			this.excludeDotResources = excludeDotResources;
			this.excludeLinkedContents = excludeLinkedContents;
		}

		public Map<String, IFile> getFiles() {
			return visitedFiles;
		}

		public Map<String, IFolder> getFolders() {
			return visitedFolders;
		}

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource == null) {
				return false;
			}

			String resourcename = resource.getName();
			if (resourcename == null) {
				return false;
			}

			if (excludeDotResources && resourcename.startsWith(DOT)) {
				return false;
			}

			switch (resource.getType()) {
			case IResource.FILE:
				visitedFiles.put(resource.getProjectRelativePath().toPortableString(), (IFile) resource);
				break;
			case IResource.FOLDER:
				if (excludeWorkingdirectoryContents) {
					for (IContainer workingDirectory : workingDirectories) {
						if (workingDirectory.equals(resource)) {
							return false;
						}
					}
				}
				visitedFolders.put(resource.getProjectRelativePath().toPortableString(), (IFolder) resource);
				break;
			default:
				break;
			}

			if (excludeLinkedContents && resource.isLinked()) {
				return false;
			}

			return true;
		}
	}

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.selection = selection;

		if (selection != null && selection.size() == 1) {
			List<?> selectionList = selection.toList();
			if ((selectionList.get(0) instanceof IProject)) {
				project = (IProject) selectionList.get(0);
			}
		}
	}

	@Override
	public void addPages() {
		super.addPages();

		mainPage = new TITANProjectExportMainPage(NEWPROJECT_WINDOWTITLE, selection);
		mainPage.setTitle(NEWPROJECT_TITLE);
		mainPage.setDescription(NEWPROJECT_DESCRIPTION);
		addPage(mainPage);

		optionsPage = new TITANProjectExportOptionsPage();
		addPage(optionsPage);
	}

	@Override
	public boolean performFinish() {
		if (project == null) {
			ErrorReporter.logError("Trying to use the project information export wizard, without having selected a project to work on");
			return false;
		}

		projectFile = mainPage.getProjectFilePath();
		if(projectFile==null || projectFile.length()==0 ) {
			ErrorReporter.logError("Invalid target tpd file name. Use the Browse button to get a valid file path");
			return false;
		}
		
		URI projectFileURI = TITANPathUtilities.getURI(projectFile, project.getLocation().toOSString());
		IPath projectFilePath = URIUtil.toPath(projectFileURI);
		if( projectFilePath == null ) {
			ErrorReporter.logError("Invalid target tpd file name. Use the Browse button to get a valid file path");
			return false;
		}
		projectFile = projectFilePath.toString(); // FIXME: toOSString() ???
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		DOMImplementation impl = builder.getDOMImplementation();
		final Document document = impl.createDocument(null, "TITAN_Project_File_Information", null);

		Element root = document.getDocumentElement();
		root.setAttribute("version", "1.0");

		boolean result = saveProjectInformation(root, project, optionsPage.ispackAllProjectsIntoOne());
		if (!result) {
			return false;
		}

		ProjectFileHandler.indentNode(document, document.getDocumentElement(), 1);

		System.setProperty(DOMImplementationRegistry.PROPERTY, ProjectFormatConstants.DOM_IMPLEMENTATION_SOURCE);
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return false;
		} catch (InstantiationException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
			return false;
		} catch (IllegalAccessException iae) {
			ErrorReporter.logExceptionStackTrace(iae);
			return false;
		}
		// Specifying "LS 3.0" in the features list ensures that the
		// DOMImplementation
		// object implements the load and save features of the DOM 3.0
		// specification.
		DOMImplementation domImpl = registry.getDOMImplementation(ProjectFormatConstants.LOAD_SAVE_VERSION);
		DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl;
		// If the mode is MODE_SYNCHRONOUS, the parse and parseURI
		// methods of the LSParser
		// object return the org.w3c.dom.Document object. If the mode is
		// MODE_ASYNCHRONOUS,
		// the parse and parseURI methods return null.
		LSParser parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, ProjectFormatConstants.XML_SCHEMA);
		DOMConfiguration config = parser.getDomConfig();
		DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("validate", Boolean.TRUE);
		config.setParameter("schema-type", ProjectFormatConstants.XML_SCHEMA);
		config.setParameter("validate-if-schema", Boolean.TRUE);
		LSSerializer dom3Writer = domImplLS.createLSSerializer();
		LSOutput output = domImplLS.createLSOutput();

		try {
			StringWriter sw = new StringWriter();
			output.setCharacterStream(sw);
			output.setEncoding("UTF-8");
			dom3Writer.write(document, output);
			String temporaloutput = sw.getBuffer().toString();

			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(projectFile));
			bufferedWriter.write(temporaloutput);
			bufferedWriter.flush();
			bufferedWriter.close();
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(projectFileURI);
			for (final IFile file : files) {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (final CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		try {
			String temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.LOAD_LOCATION));
			if (temp == null || temp.length() == 0) {
				project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.LOAD_LOCATION), projectFileURI.toString());
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		return true;
	}

	/**
	 * Does the actual saving of the project's information to the provided
	 * node.
	 * 
	 * @param root
	 *                the node where the project information should be
	 *                saved.
	 * @param project
	 *                the project to be processed.
	 * @param packReferencedProjects
	 *                whether the referenced projects should be packed as
	 *                well.
	 * */
	private boolean saveProjectInformation(final Node root, final IProject project, final boolean packReferencedProjects) {
		final Document document = root.getOwnerDocument();

		// save the name of the project
		Element projectNameNode = document.createElement(ProjectFormatConstants.PROJECTNAME_NODE);
		projectNameNode.appendChild(document.createTextNode(project.getName()));
		root.appendChild(projectNameNode);

		boolean result = saveReferencedProjectsData(root, project);
		if (!result) {
			return false;
		}

		final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);
		ExportResourceVisitor visitor = new ExportResourceVisitor(workingDirectories, optionsPage.isExcludedWorkingDirectoryContents(),
				optionsPage.isExcludedDotResources(), optionsPage.isExcludeLinkedContents());
		try {
			if (project.isAccessible()) {
				project.accept(visitor);
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}

		IPath projectFilePath = new Path(projectFile);
		projectFilePath = projectFilePath.removeLastSegments(1);
		URI projectFileURI = URIUtil.toURI(projectFilePath);

		Map<String, IFolder> folders = visitor.getFolders();
		saveFoldersData(root, folders, projectFileURI);

		Map<String, IFile> files = visitor.getFiles();
		saveFilesData(root, files, projectFileURI);

		savePathVariableData(root);

		saveConfigurationData(root, project, files, folders, optionsPage.isSaveDefaultValues());

		if (packReferencedProjects) {
			List<IProject> referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
			referencedProjects.remove(project);

			if (!referencedProjects.isEmpty()) {
				Element projectsElement = document.createElement(ProjectFormatConstants.PACKED_REFERENCED_PROJECTS_NODE);
				root.appendChild(projectsElement);
				for (IProject tempProject : referencedProjects) {
					Element element = document.createElement(ProjectFormatConstants.PACKED_REFERENCED_PROJECT_NODE);
					projectsElement.appendChild(element);
					saveProjectInformation(element, tempProject, false);
				}
			}
		}

		return true;
	}

	/**
	 * Save data on the project references used by the actual project.
	 * 
	 * @param root
	 *                the node to save the information to.
	 * @param project
	 *                the project to be processed.
	 * */
	private boolean saveReferencedProjectsData(final Node root, final IProject project) {
		IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects();
		if (referencedProjects.length == 0) {
			return true;
		}

		final Document document = root.getOwnerDocument();
		Element projectsElement = document.createElement(ProjectFormatConstants.REFERENCED_PROJECTS_NODE);
		root.appendChild(projectsElement);

		for (final IProject tempProject : referencedProjects) {
			Element element = document.createElement(ProjectFormatConstants.REFERENCED_PROJECT_NODE);
			element.setAttribute(ProjectFormatConstants.REFERENCED_PROJECT_NAME_ATTRIBUTE, tempProject.getName());

			if (!tempProject.isOpen()) {
				ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Export failed",
					"In order to export data on project " + project.getName()
					+ " it's referenced project " + tempProject.getName()
					+ " must be open.");

				ErrorReporter.logError("In order to export data on project " + project.getName() + " it's referenced project "
						+ tempProject.getName() + " must be open.");
				return true;
			}

			try {
				String location = tempProject.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.LOAD_LOCATION));
				if (location == null || location.length() == 0) {
					final IProject tempProject2 = tempProject;
					ErrorReporter.parallelErrorDisplayInMessageDialog(
						"Export failed",
						"In order to export data on project " + project.getName()
							+ " data on project " + tempProject2.getName()
							+ " must be saved first.");

					ErrorReporter.logError("In order to export data on project " + project.getName() 
							+ " data on project " + tempProject.getName() + " must be saved first.");
					return false;
				}

				IPath path = new Path(projectFile);
				URI locationuri = null;
				try {
					locationuri = org.eclipse.core.runtime.URIUtil.fromString(location);
					if (locationuri.getScheme().length() <= 1) {
						Path locationPath = new Path(location);
						locationuri = org.eclipse.core.runtime.URIUtil.fromString("file:/" + locationPath.toString());
					}

				} catch (URISyntaxException e) {
					ErrorReporter.logExceptionStackTrace(e);
					return false;
				}

				path = path.removeLastSegments(1);
				URI projecturi = URIUtil.toURI(path);
				URI result = org.eclipse.core.runtime.URIUtil.makeRelative(locationuri, projecturi);
				element.setAttribute(ProjectFormatConstants.REFERENCED_PROJECT_LOCATION_ATTRIBUTE, result.toString());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}

			projectsElement.appendChild(element);
		}

		return true;
	}

	/**
	 * Saves the folders related data of the project under the provided
	 * node.
	 * 
	 * @param root
	 *                the node to save the information to.
	 * @param folders
	 *                the folders contained in the project.
	 * @param projectFileURI
	 *                the location of the project file (the folder), the
	 *                document will be saved to, in order to calculate
	 *                relative paths.
	 * */
	private void saveFoldersData(final Node root, final Map<String, IFolder> folders, final URI projectFileURI) {
		if (folders.isEmpty()) {
			return;
		}

		final Document document = root.getOwnerDocument();
		final Element foldersRoot = document.createElement(ProjectFormatConstants.FOLDERS_NODE);
		root.appendChild(foldersRoot);
		for (IFolder folder : folders.values()) {
			final Element folderRoot = document.createElement(ProjectFormatConstants.FOLDER_NODE);
			foldersRoot.appendChild(folderRoot);

			folderRoot.setAttribute(ProjectFormatConstants.FOLDER_ECLIPSE_LOCATION_NODE, folder.getProjectRelativePath().toString());
			URI rawURI = folder.getRawLocationURI();
			URI expandedURI = folder.getLocationURI();
			if (rawURI.equals(expandedURI)) {
				if (folder.getLocation() != null) {
					URI result = org.eclipse.core.runtime.URIUtil.makeRelative(expandedURI, projectFileURI);
					folderRoot.setAttribute(ProjectFormatConstants.FOLDER_RELATIVE_LOCATION, result.toString());
				} else {
					folderRoot.setAttribute(ProjectFormatConstants.FOLDER_RAW_LOCATION, folder.getLocationURI().toString());
				}
			} else {
				folderRoot.setAttribute(ProjectFormatConstants.FOLDER_RAW_LOCATION, folder.getRawLocationURI().toString());
			}
		}
	}

	/**
	 * Saves the files related data of the project under the provided node.
	 * 
	 * @param root
	 *                the node to save the information to.
	 * @param files
	 *                the files contained in the project.
	 * @param projectFileURI
	 *                the location of the project file (the folder), the
	 *                document will be saved to, in order to calculate
	 *                relative paths.
	 * */
	private void saveFilesData(final Node root, final Map<String, IFile> files, final URI projectFileURI) {
		if (files.isEmpty()) {
			return;
		}

		final Document document = root.getOwnerDocument();
		final Element filesRoot = document.createElement(ProjectFormatConstants.FILES_NODE);
		root.appendChild(filesRoot);
		for (IFile file : files.values()) {
			final Element fileRoot = document.createElement(ProjectFormatConstants.FILE_NODE);
			filesRoot.appendChild(fileRoot);

			fileRoot.setAttribute(ProjectFormatConstants.FILE_ECLIPSE_LOCATION_NODE, file.getProjectRelativePath().toString());
			URI rawURI = file.getRawLocationURI();
			URI expandedURI = file.getLocationURI();
			if (rawURI.equals(expandedURI)) {
				if (file.getLocation() != null) {
					URI result = org.eclipse.core.runtime.URIUtil.makeRelative(expandedURI, projectFileURI);
					fileRoot.setAttribute(ProjectFormatConstants.FILE_RELATIVE_LOCATION, result.toString());
				} else {
					fileRoot.setAttribute(ProjectFormatConstants.FILE_RAW_LOCATION, file.getLocationURI().toString());
				}
			} else {
				fileRoot.setAttribute(ProjectFormatConstants.FILE_RAW_LOCATION, file.getRawLocationURI().toString());
			}
		}
	}

	/**
	 * Saves the path variables with name and value under the provided node.
	 * 
	 * @param root
	 *                the node to save the data under.
	 * */
	private void savePathVariableData(final Node root) {
		IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		String[] names = pathVariableManager.getPathVariableNames();
		if (names.length == 0) {
			return;
		}

		List<String> namesArray = new ArrayList<String>(names.length);
		for (String name : names) {
			namesArray.add(name);
		}
		Collections.sort(namesArray);
		final Document document = root.getOwnerDocument();
		final Element variablesRoot = document.createElement(ProjectFormatConstants.PATH_VARIABLES);
		root.appendChild(variablesRoot);
		for (String name : namesArray) {
			final Element variableRoot = document.createElement(ProjectFormatConstants.PATH_VARIABLE);
			variablesRoot.appendChild(variableRoot);
			//IPath value = pathVariableManager.getValue(name);
			URI uri = pathVariableManager.getURIValue(name); //FIXME: test it! valid from eclipse 3.6
			//variableRoot.setAttribute("name", name);
			// FIXME use URI as soon as we drop support for older
			// platform versions.
			variableRoot.setAttribute("value", uri.toString());
		}
	}

	/**
	 * Saves the configurations related data of the project under the
	 * provided node.
	 * 
	 * @param root
	 *                the node to save the data under.
	 * @param files
	 *                the files to handle.
	 * @param folders
	 *                the folders to handle.
	 * @param saveDefaultValues
	 *                whether the default values have to be explicitly saved
	 *                or not.
	 * */
	private void saveConfigurationData(final Node root, final IProject project, final Map<String, IFile> files,
			final Map<String, IFolder> folders, final boolean saveDefaultValues) {
		final Document document = root.getOwnerDocument();

		String activeConfigurationName;
		try {
			activeConfigurationName = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.ACTIVECONFIGURATION));
			if (activeConfigurationName == null || activeConfigurationName.length() == 0) {
				activeConfigurationName = ProjectFormatConstants.DEFAULT_CONFIGURATION_NAME;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			activeConfigurationName = ProjectFormatConstants.DEFAULT_CONFIGURATION_NAME;
		}
		Element activeConfigurationNode = document.createElement(ProjectFormatConstants.ACTIVE_CONFIGURATION_NODE);
		activeConfigurationNode.appendChild(document.createTextNode(activeConfigurationName));
		root.appendChild(activeConfigurationNode);

		Node configurationsRoot = document.createElement(ProjectFormatConstants.CONFIGURATIONS_NODE);
		root.appendChild(configurationsRoot);
		final Document configurationDocument = ProjectDocumentHandlingUtility.getDocument(project);
		List<String> configurations = ProjectFileHandler.getConfigurations(configurationDocument);

		for (String config : configurations) {
			Element configurationRoot = document.createElement(ProjectFormatConstants.CONFIGURATION_NODE);
			configurationRoot.setAttribute(ProjectFormatConstants.CONFIGURATION_NAME_ATTRIBUTE, config);
			configurationsRoot.appendChild(configurationRoot);

			Node configurationNode = ProjectFileHandler.findConfigurationNode(configurationDocument.getDocumentElement(), config);
			copyConfigurationData(configurationNode, project, configurationRoot, files, folders, saveDefaultValues);
		}
	}

	/**
	 * Copies the configuration related data from the source node, to the
	 * target node.
	 * 
	 * @param sourceRoot
	 *                the node from where the configuration data is moved.
	 * @param targetRoot
	 *                the node where the configuration data should be moved
	 *                to.
	 * @param files
	 *                the files to handle.
	 * @param folders
	 *                the folders to handle.
	 * @param saveDefaultValues
	 *                whether the default values have to be explicitly saved
	 *                or not.
	 * */
	private void copyConfigurationData(final Node sourceRoot, final IProject project, final Element targetRoot, final Map<String, IFile> files,
			final Map<String, IFolder> folders, final boolean saveDefaultValues) {
		ProjectFileHandler.copyProjectInfo(sourceRoot, targetRoot, project, files, folders, saveDefaultValues);
	}
}
