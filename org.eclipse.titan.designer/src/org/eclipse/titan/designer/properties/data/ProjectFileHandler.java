/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import static org.eclipse.titan.designer.properties.data.FileBuildPropertyData.FILERESOURCEXMLNODE;
import static org.eclipse.titan.designer.properties.data.FolderBuildPropertyData.FOLDERRESOURCEXMLNODE;
import static org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData.LOCALBUILDSETTINGS;
import static org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData.MAKEFILESETTINGSXMLNODE;
import static org.eclipse.titan.designer.properties.data.ProjectConfigurationsPropertyData.CONFIGURATION_REQUIREMENTS_NODE;
import static org.eclipse.titan.designer.properties.data.ProjectNamingConventionPropertyData.NAMINGCONVENTIONS_XMLNODE;
import static org.eclipse.titan.designer.properties.data.ProjectRemoteBuildPropertyData.REMOTEBUILDPROPERTIES_XMLNODE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * This class handles the loading and saving of data in the .TITAN_properties
 * file.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectFileHandler {
	private static final String DOM_IMPLEMENTATION_SOURCE = "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl";
	private static final String LOAD_SAVE_VERSION = "LS 3.0";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final String XSD_PATH = "/project.xsd";
	public static final String XML_TITAN_PROPERTIES_FILE = ".TITAN_properties";

	private static final String DOT = ".";
	public static final String PROJECTPROPERTIESXMLNODE = "ProjectProperties";
	public static final String FILEPROPERTIESXMLNODE = "FileProperties";
	public static final String FOLDERPROPERTIESXMLNODE = "FolderProperties";
	public static final String CONFIGURATIONSXMLNODE = "Configurations";
	public static final String CONFIGURATIONXMLNODE = "Configuration";
	public static final String ACTIVECONFIGURATIONXMLNODE = "ActiveConfiguration";
	public static final String DEFAULTCONFIGURATIONNAME = "Default";

	private static final String SAVING_PROPERTIES = "saving TITAN properties";
	private static final String LOADING_PROPERTIES = "loading TITAN properties";
	private static final String ENCODING = "UTF-8";

	private static StringBuilder xmlFormatterString = new StringBuilder("\n           ");

	private final IProject project;
	private DOMImplementation domImpl;
	private DOMImplementationLS domImplLS;
	private LSParser parser;
	private DOMConfiguration config;

	private static final List<IProject> PROJECTS_LOCKED_FOR_LOADING = new ArrayList<IProject>();

	/**
	 * Inner class for visiting file and folder resources during project
	 * file handling.
	 * 
	 */
	private static class ResourceVisitor implements IResourceVisitor {
		private final IContainer[] workingDirectories;
		private final Map<String, IFolder> visitedFolders;
		private final Map<String, IFile> visitedFiles;

		public ResourceVisitor(final IContainer[] workingDirectories) {
			this.workingDirectories = workingDirectories;
			this.visitedFiles = new TreeMap<String, IFile>();
			this.visitedFolders = new TreeMap<String, IFolder>();
		}

		public Map<String, IFile> getFiles() {
			return visitedFiles;
		}

		public Map<String, IFolder> getFolders() {
			return visitedFolders;
		}

		@Override
		public boolean visit(final IResource resource) {
			if (resource == null || !resource.isAccessible()) {
				return false;
			}
			switch (resource.getType()) {
			case IResource.FILE:
				if (resource.getProjectRelativePath().lastSegment().startsWith(DOT)) {
					return false;
				}
				visitedFiles.put(resource.getProjectRelativePath().toPortableString(), (IFile) resource);
				break;
			case IResource.FOLDER:
				if (resource.getProjectRelativePath().lastSegment().startsWith(DOT)) {
					return false;
				}
				for (IContainer workingDirectory : workingDirectories) {
					if (workingDirectory.equals(resource)) {
						return false;
					}
				}
				visitedFolders.put(resource.getProjectRelativePath().toPortableString(), (IFolder) resource);
				break;
			default:
				break;
			}

			return true;
		}
	}

	/**
	 * Constructs the object, saves the project to work on, and initializes
	 * the needed structures.
	 * 
	 * @param project
	 *                the project whose data we want to work with
	 * */
	public ProjectFileHandler(final IProject project) {
		this.project = project;
		// getting XML schema definition from the plugin jar

		// DOMImplementationRegistry is a factory that enables
		// applications to
		// obtain instances of a DOMImplementation.
		System.setProperty(DOMImplementationRegistry.PROPERTY, DOM_IMPLEMENTATION_SOURCE);
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("While create project handler for `" + project.getName() + "'", e);
			return;
		}
		// Specifying "LS 3.0" in the features list ensures that the
		// DOMImplementation
		// object implements the load and save features of the DOM 3.0
		// specification.
		domImpl = registry.getDOMImplementation(LOAD_SAVE_VERSION);
		domImplLS = (DOMImplementationLS) domImpl;
		// If the mode is MODE_SYNCHRONOUS, the parse and parseURI
		// methods of
		// the LSParser
		// object return the org.w3c.dom.Document object. If the mode is
		// MODE_ASYNCHRONOUS,
		// the parse and parseURI methods return null.
		parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, XML_SCHEMA);
		config = parser.getDomConfig();
		DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("validate", Boolean.TRUE);
		config.setParameter("schema-type", XML_SCHEMA);
		config.setParameter("validate-if-schema", Boolean.TRUE);
	}

	/**
	 * Returns the indentation to be used in the property files at a given
	 * indentation level.
	 * 
	 * @param level
	 *                the indentation level
	 * 
	 * @return the indentation string to use
	 * */
	private static String getXMLIndentation(final int level) {
		while (xmlFormatterString.length() < level + 2) {
			xmlFormatterString.append(xmlFormatterString.substring(2));
		}

		return xmlFormatterString.substring(0, level);
	}

	/**
	 * Checks if the .TITAN_properties file exists within the actual
	 * project.
	 * 
	 * @return true if the .TITAN_properties file exists
	 * */
	public boolean projectFileExists() {
		final IFile fileResource = project.getFile('/' + XML_TITAN_PROPERTIES_FILE);
		return fileResource.isAccessible();
	}

	/**
	 * Creates a xml subtree containing the actual values of the project
	 * level settings. To achieve this it builds the tree using other
	 * property pages representing project level settings.
	 * 
	 * @see ProjectBuildPropertyData#saveMakefileSettings(Document,
	 *      IProject)
	 * @see ProjectBuildPropertyData#saveLocalBuildSettings(Document,
	 *      IProject)
	 * @see ProjectRemoteBuildPropertyData#saveRemoteBuildProperties(Document,
	 *      IProject)
	 * @see ProjectNamingConventionPropertyPage#saveProperties(Document,
	 *      int, IProject);
	 * 
	 * @see #saveProjectSettings()
	 * 
	 * @param document
	 *                the Document class used to create the tree nodes
	 * 
	 * @return the root node created
	 * */
	private static Node saveProjectProperties(final IProject project, final Document document) {
		final Node root = document.createElement(PROJECTPROPERTIESXMLNODE);

		root.appendChild(ProjectBuildPropertyData.saveMakefileSettings(document, project));
		root.appendChild(ProjectBuildPropertyData.saveLocalBuildSettings(document, project));

		Element element = ProjectRemoteBuildPropertyData.saveRemoteBuildProperties(document, project);
		if (element != null) {
			root.appendChild(element);
		}

		element = ProjectNamingConventionPropertyData.saveProperties(document, project);
		if (element != null) {
			root.appendChild(element);
		}

		element = ProjectConfigurationsPropertyData.saveProperties(document, project);
		if (element != null) {
			root.appendChild(element);
		}

		return root;
	}

	/**
	 * Creates a xml subtree containing the actual values of every folder in
	 * the actual project. For every folder in a loop it calls the folder's
	 * property page to create a subtree, which it uses to build its own
	 * tree. Information on a folder is only saved if its properties are not
	 * set to their default values.
	 * 
	 * @see FolderBuildPropertyData#saveFolderProperties(Document, IFolder)
	 * 
	 * @see #saveProjectSettings()
	 * 
	 * @param document
	 *                the Document class used to create the tree nodes
	 * @param workingDirectories
	 *                the working directories of the project
	 * @param folders
	 *                the folders to process
	 * 
	 * @return the tree containing the data
	 * */
	private static Element saveFolderProperties(final Document document, final IContainer[] workingDirectories, final Map<String, IFolder> folders) {
		Element root = null;

		for (Iterator<Map.Entry<String, IFolder>> iterator = folders.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry<String, IFolder> entry = iterator.next();
			final IFolder folder = entry.getValue();
			for (IContainer workingDirectory : workingDirectories) {
				if (workingDirectory.equals(folder)) {
					continue;
				}
			}

			if (!FolderBuildPropertyData.hasDefaultProperties(folder)) {
				if (root == null) {
					root = document.createElement(FOLDERPROPERTIESXMLNODE);
				}

				root.appendChild(FolderBuildPropertyData.saveFolderProperties(document, folder));
			}
		}

		return root;
	}

	/**
	 * Creates a xml subtree containing the actual values of every file in
	 * the actual project. For every file in a loop it calls the file's
	 * property page to create a subtree, which it uses to build its own
	 * tree. Information on a file is only saved if its properties are not
	 * set to their default values.
	 * 
	 * @see FileBuildPropertyData#saveFileProperties(Document, IFile)
	 * 
	 * @see #saveProjectSettings()
	 * 
	 * @param document
	 *                the Document class used to create the tree nodes
	 * @param workingDirectories
	 *                the working directories of the project
	 * @param files
	 *                the files to process.
	 * 
	 * @return the tree containing the data
	 * */
	private static Element saveFileProperties(final Document document, final IContainer[] workingDirectories, final Map<String, IFile> files) {
		Element root = null;

		for (Iterator<Map.Entry<String, IFile>> iterator = files.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry<String, IFile> entry = iterator.next();
			final IFile file = entry.getValue();
			for (IContainer workingDirectory : workingDirectories) {
				if (workingDirectory.equals(file.getParent())) {
					continue;
				}
			}

			if (!FileBuildPropertyData.hasDefaultProperties(file)) {
				if (root == null) {
					root = document.createElement(FILEPROPERTIESXMLNODE);
				}
				root.appendChild(FileBuildPropertyData.saveFileProperties(document, file));
			}
		}

		return root;
	}

	/**
	 * Saves out the settings stored in the actual project into its
	 * .TITAN_properties file.
	 * <p>
	 * <ul>
	 * <li>Collects the files and folders contained in this project.
	 * <li>Creates the subtrees for the project, folder and file level
	 * settings, and from those build its own tree.
	 * <li>If the .TITAN_properties file does not exist or contains
	 * different data, then the XML tree is saved into it.
	 * 
	 * @see #saveFileProperties(Document)
	 * @see #saveFolderProperties(Document)
	 * @see #saveProjectProperties(Document) </ul>
	 * 
	 * @return the job doing the actual work, so that one can join it from
	 *         the outside.
	 * */
	public WorkspaceJob saveProjectSettingsJob() {
		if (PROJECTS_LOCKED_FOR_LOADING.contains(project)) {
			return null;
		}

		WorkspaceJob saveJob = new WorkspaceJob(SAVING_PROPERTIES) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				if (!project.isAccessible()) {
					return Status.OK_STATUS;
				}

				Document document = ProjectDocumentHandlingUtility.getDocument(project);
				if (document == null) {
					document = ProjectDocumentHandlingUtility.createDocument(project);
				}

				saveProjectInfoToDocument(document);

				clearNode(document.getDocumentElement());
				indentNode(document, document.getDocumentElement(), 1);

				ProjectDocumentHandlingUtility.saveDocument(project);

				return Status.OK_STATUS;
			}
		};
		saveJob.setPriority(Job.LONG);
		saveJob.setUser(false);
		saveJob.setSystem(true);
		saveJob.setRule(project.getWorkspace().getRuleFactory().refreshRule(project));
		saveJob.schedule();

		return saveJob;
	}

	/**
	 * Save the Titan related informations of the project into the provided
	 * document.
	 * 
	 * @param document
	 *                the document to store the information.
	 * */
	public void saveProjectInfoToDocument(final Document document) {
		saveActualConfigurationInfoToNode(project, document);
		String activeConfigurationName = getActiveConfigurationName(project);

		if (DEFAULTCONFIGURATIONNAME.equals(activeConfigurationName)) {
			saveProjectInfoToNode(project, document.getDocumentElement(), document);
		} else {

			Node configurationsRoot = getNodebyName(document.getDocumentElement().getChildNodes(), CONFIGURATIONSXMLNODE);
			if (configurationsRoot == null) {
				configurationsRoot = document.createElement(CONFIGURATIONSXMLNODE);
				document.getDocumentElement().appendChild(configurationsRoot);
			}

			Node configurationRoot = findConfigurationNode(document.getDocumentElement(), activeConfigurationName);
			if (configurationRoot == null) {
				final Element temp = document.createElement(CONFIGURATIONXMLNODE);

				temp.setAttribute("name", activeConfigurationName);
				configurationsRoot.appendChild(temp);
				configurationRoot = temp;
			}

			saveProjectInfoToNode(project, configurationRoot, document);
		}
	}

	/**
	 * Creates a new configuration node with the given name if it does not
	 * already exist and returns with it. If it already exists, just returns
	 * it. If the configurations or the configuration does not exist, they
	 * are created.
	 * 
	 * @param document
	 *                the document to store the information.
	 * @param configurationName
	 *                the name of the configuration to create.
	 * */
	public static Node createConfigurationNode(final Document document, final String configurationName) {
		if (DEFAULTCONFIGURATIONNAME.equals(configurationName)) {
			return document.getDocumentElement();
		}

		Node configurationsRoot = getNodebyName(document.getDocumentElement().getChildNodes(), CONFIGURATIONSXMLNODE);
		if (configurationsRoot == null) {
			configurationsRoot = document.createElement(CONFIGURATIONSXMLNODE);
			document.getDocumentElement().appendChild(configurationsRoot);
		}

		Node configurationRoot = findConfigurationNode(document.getDocumentElement(), configurationName);
		if (configurationRoot == null) {
			final Element temp = document.createElement(CONFIGURATIONXMLNODE);

			temp.setAttribute("name", configurationName);
			configurationsRoot.appendChild(temp);
			configurationRoot = temp;
		}

		return configurationRoot;
	}

	/**
	 * Saves out the settings stored in the actual project into its
	 * .TITAN_properties file.
	 * <p>
	 * <ul>
	 * <li>Collects the files and folders contained in this project.
	 * <li>Creates the subtrees for the project, folder and file level
	 * settings, and from those build its own tree.
	 * <li>If the .TITAN_properties file does not exist or contains
	 * different data, then the XML tree is saved into it.
	 * 
	 * @see #saveFileProperties(Document)
	 * @see #saveFolderProperties(Document)
	 * @see #saveProjectProperties(Document) </ul>
	 * */
	public void saveProjectSettings() {
		saveProjectSettingsJob();
	}

	/**
	 * Loads and sets the project level properties using the specific
	 * property pages.
	 * 
	 * @see ProjectBuildPropertyData#loadMakefileSettings(Document, Node,
	 *      IProject, Set)
	 * @see ProjectBuildPropertyData#loadLocalBuildSettings(Document, Node,
	 *      IProject, Set)
	 * @see ProjectRemoteBuildPropertyData#loadRemoteBuildProperties(Document,
	 *      Node, IProject, Set)
	 * 
	 * @param node
	 *                the actual tree node to operate on
	 * @param changedResources
	 *                contains the list of resources that were actually
	 *                changed by the settings loaded.
	 * */
	private void loadProjectProperties(final Node node, final Set<IResource> changedResources) {
		final NodeList resourceList = node.getChildNodes();

		Node namingConventionsNode = null;

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			final Node tempNode = resourceList.item(i);
			final String nodeName = tempNode.getNodeName();
			if (MAKEFILESETTINGSXMLNODE.equals(nodeName)) {
				ProjectBuildPropertyData.loadMakefileSettings(tempNode, project, changedResources);
			} else if (LOCALBUILDSETTINGS.equals(nodeName)) {
				ProjectBuildPropertyData.loadLocalBuildSettings(tempNode, project, changedResources);
			} else if (REMOTEBUILDPROPERTIES_XMLNODE.equals(nodeName)) {
				ProjectRemoteBuildPropertyData.loadRemoteBuildProperties(tempNode, project, changedResources);
			} else if (CONFIGURATION_REQUIREMENTS_NODE.equals(nodeName)) {
				ProjectConfigurationsPropertyData.loadProperties(tempNode, project, changedResources);
			} else if (NAMINGCONVENTIONS_XMLNODE.equals(nodeName)) {
				namingConventionsNode = tempNode;
			}
		}

		ProjectNamingConventionPropertyData.loadProperties(namingConventionsNode, project);
	}

	/**
	 * Loads and sets the file level properties using the files property
	 * page.
	 * 
	 * @see FileBuildPropertyData#loadFileProperties(Node, IProject, Set)
	 * 
	 * @param node
	 *                the actual tree node to operate on, or null to set the
	 *                properties of every file to default.
	 * @param files
	 *                the list of files present in the project.
	 * @param changedResources
	 *                contains the list of resources that were actually
	 *                changed by the settings loaded.
	 * */
	private void loadFileProperties(final Node node, final Collection<IFile> files, final Set<IResource> changedResources) {
		final Set<IFile> notYetReachedFiles = new HashSet<IFile>();
		notYetReachedFiles.addAll(files);

		if (node != null) {
			final NodeList resourceList = node.getChildNodes();
			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final Node tempNode = resourceList.item(i);
				if (FILERESOURCEXMLNODE.equals(tempNode.getNodeName())) {
					FileBuildPropertyData.loadFileProperties(tempNode, project, notYetReachedFiles, changedResources);
				}
			}
		}

		for (IFile file : notYetReachedFiles) {
			FileBuildPropertyData.loadDefaultProperties(file, changedResources);
		}
	}

	/**
	 * Loads and sets the folder level properties using the folders property
	 * page.
	 * 
	 * @see FileBuildPropertyData#loadFileProperties(Node, IProject, Set)
	 * 
	 * @param node
	 *                the actual tree node to operate on, or null to set the
	 *                properties of every file to default.
	 * @param folders
	 *                the list of folder present in the project.
	 * @param changedResources
	 *                contains the list of resources that were actually
	 *                changed by the settings loaded.
	 * */
	private void loadFolderProperties(final Node node, final Collection<IFolder> folders, final Set<IResource> changedResources) {
		final Set<IFolder> notYetReachedFolders = new HashSet<IFolder>();
		notYetReachedFolders.addAll(folders);

		if (node != null) {
			NodeList resourceList = node.getChildNodes();
			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final Node tempNode = resourceList.item(i);
				if (FOLDERRESOURCEXMLNODE.equals(tempNode.getNodeName())) {
					FolderBuildPropertyData.loadFolderProperties(tempNode, project, notYetReachedFolders, changedResources);
				}
			}
		}

		for (IFolder folder : notYetReachedFolders) {
			FolderBuildPropertyData.loadDefaultProperties(folder, changedResources);
		}
	}

	/**
	 * Loads in the settings stored in the actual project into its
	 * .TITAN_properties file.
	 * <p>
	 * 
	 * @see #saveFileProperties(Document)
	 * @see #saveFolderProperties(Document)
	 * @see #saveProjectProperties(Document)
	 * */
	public void loadProjectSettings() {
		if (PROJECTS_LOCKED_FOR_LOADING.contains(project)) {
			return;
		}

		PROJECTS_LOCKED_FOR_LOADING.add(project);

		if (!project.isAccessible()) {
			PROJECTS_LOCKED_FOR_LOADING.remove(project);
			return;
		}

		WorkspaceJob loadJob = new WorkspaceJob(LOADING_PROPERTIES) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				if (!project.isAccessible()) {
					PROJECTS_LOCKED_FOR_LOADING.remove(project);
					return Status.OK_STATUS;
				}

				try {
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While refreshing `" + project.getName() + "'",e);
				}

				ProjectDocumentHandlingUtility.clearDocument(project);
				final Document document = ProjectDocumentHandlingUtility.getDocument(project);
				PROJECTS_LOCKED_FOR_LOADING.remove(project);
				if (document != null) {
					loadProjectSettingsFromDocument(document);
				}

				return Status.OK_STATUS;
			}
		};

		loadJob.setPriority(Job.LONG);
		loadJob.setUser(false);
		loadJob.setSystem(true);
		loadJob.setRule(project.getWorkspace().getRuleFactory().refreshRule(project));
		loadJob.schedule();
	}

	/**
	 * Loads and sets the projects Titan related attributes from the
	 * provided document.
	 * 
	 * @param document
	 *                the document holding the informations.
	 * */
	public void loadProjectSettingsFromDocument(final Document document) {
		Node activeConfiguration = getNodebyName(document.getDocumentElement().getChildNodes(), ACTIVECONFIGURATIONXMLNODE);
		String activeConfigurationName;
		if (activeConfiguration == null) {
			activeConfigurationName = DEFAULTCONFIGURATIONNAME;
		} else {
			activeConfigurationName = activeConfiguration.getTextContent();
		}

		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.ACTIVECONFIGURATION), activeConfigurationName);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting configuration property for `" + project.getName() + "'", e);
		}

		final Set<IResource> changedResources = new HashSet<IResource>();
		final Node configuration = findConfigurationNode(document.getDocumentElement(), activeConfigurationName);
		if (configuration == null) {
			ErrorReporter.logError("The configuration `" + activeConfigurationName + "' for project `" + project.getName()
					+ "' does not exist.");
		} else {
			loadProjectInfoFromNode(configuration, changedResources);
		}
	}

	/**
	 * Saves a XML document to a file.
	 * 
	 * @param file
	 *                the file to save the information into.
	 * @param document
	 *                the document to save
	 * 
	 * @return IStatus object to indicate if the operation was successful or
	 *         not.
	 * */
	public void saveDocumentToFile(final IFile file, final Document document) {
		LSSerializer dom3Writer = domImplLS.createLSSerializer();
		LSOutput output = domImplLS.createLSOutput();

		File localFile = file.getLocation().toFile();
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
			final StringWriter sw = new StringWriter();
			output.setCharacterStream(sw);
			output.setEncoding(ENCODING);
			dom3Writer.write(document, output);
			final String temporaloutput = sw.getBuffer().toString();

			// temporalStorage will hold the contents of the
			// existing file
			String temporalStorage = null;

			if (file.isAccessible() && file.exists() && localFile.canRead()) {
				final InputStream is = file.getContents(true);
				final BufferedReader br = new BufferedReader(new InputStreamReader(is));
				final StringBuilder sb = new StringBuilder();
				String line;
				boolean firstLine = true;
				while ((line = br.readLine()) != null) {
					if (firstLine) {
						firstLine = false;
					} else {
						sb.append('\n');
					}
					sb.append(line);

				}
				temporalStorage = sb.toString();
				br.close();
			}

			// If there is a difference between the old
			// file and the one to be written, the old
			// one will be overwritten by the new one.
			if (temporalStorage == null || !temporalStorage.equals(temporaloutput)) {
				if (file.exists()) {
					file.setContents(new ByteArrayInputStream(temporaloutput.getBytes()), IResource.FORCE
							| IResource.KEEP_HISTORY, null);
				} else {
					file.create(new ByteArrayInputStream(temporaloutput.getBytes()), IResource.FORCE, null);
				}
				try {
					file.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While refreshing `" + file.getName() + "'", e);
				}
			}

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("The properties of project \'"
					+ project.getName() + "\' could not be saved", e);
		}
	}

	/**
	 * Extracts an XML document from the provided file.
	 * 
	 * @param file
	 *                the file to read from.
	 * @return the extracted XML document, or null if there were some error.
	 * */
	public Document getDocumentFromFile(final IFile file) {
		if (!file.isAccessible()) {
			return null;
		}

		final LSInput lsInput = domImplLS.createLSInput();
		Document document = null;
		try {
			final InputStream istream = file.getContents(true);
			lsInput.setByteStream(istream);
			document = parser.parse(lsInput);
			istream.close();
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("While parsing `" + file.getName() + "'", e);
		}

		return document;
	}

	/**
	 * Searches for the node with the given name, and if found returns it.
	 * 
	 * @param nodeList
	 *                the list of node to search.
	 * @param name
	 *                the name to search for.
	 * 
	 * @return the node with the provided name, or null if not found.
	 * */
	public static Node getNodebyName(final NodeList nodeList, final String name) {
		for (int i = 0, size = nodeList.getLength(); i < size; i++) {
			final Node temp = nodeList.item(i);
			if (temp.getNodeName().equals(name)) {
				return temp;
			}
		}

		return null;
	}

	/**
	 * Saves the name of the actual configuration of a project into an XML
	 * document node.
	 * 
	 * @param project
	 *                the project to work on.
	 * @param document
	 *                the document, used to create text nodes.
	 * */
	public static void saveActualConfigurationInfoToNode(final IProject project, final Document document) {
		Node activeConfiguration = ProjectFileHandler
				.getNodebyName(document.getDocumentElement().getChildNodes(), ACTIVECONFIGURATIONXMLNODE);
		if (activeConfiguration == null) {
			activeConfiguration = document.createElement(ACTIVECONFIGURATIONXMLNODE);
			document.getDocumentElement().appendChild(activeConfiguration);
		} else {
			Node firstChild = activeConfiguration.getFirstChild();
			while (firstChild != null) {
				activeConfiguration.removeChild(firstChild);
				firstChild = activeConfiguration.getFirstChild();
			}
		}

		String activeConfigurationName = getActiveConfigurationName(project);
		activeConfiguration.appendChild(document.createTextNode(activeConfigurationName));
	}

	/**
	 * Saves the informations related to a configuration of a project into
	 * an XML document node.
	 * 
	 * @param project
	 *                the project to work on.
	 * @param root
	 *                the root node for saving the data.
	 * @param document
	 *                the document, used to create text nodes.
	 * */
	public static void saveProjectInfoToNode(final IProject project, final Node root, final Document document) {
		final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);
		final ResourceVisitor saveVisitor = new ResourceVisitor(workingDirectories);
		// collect the resources
		try {
			project.accept(saveVisitor);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While collecting resources of `" + project.getName() + "'", e);
		}
		final Map<String, IFile> files = saveVisitor.getFiles();
		final Map<String, IFolder> folders = saveVisitor.getFolders();

		final Node oldProjectRoot = getNodebyName(root.getChildNodes(), PROJECTPROPERTIESXMLNODE);
		final Node newProjectRoot = saveProjectProperties(project, document);
		if (oldProjectRoot == null) {
			root.appendChild(newProjectRoot);
		} else {
			root.replaceChild(newProjectRoot, oldProjectRoot);
		}

		final Node oldFolderRoot = getNodebyName(root.getChildNodes(), FOLDERPROPERTIESXMLNODE);
		final Node newFolderRoot = saveFolderProperties(document, workingDirectories, folders);
		if (newFolderRoot != null) {
			if (oldFolderRoot == null) {
				root.appendChild(newFolderRoot);
			} else {
				root.replaceChild(newFolderRoot, oldFolderRoot);
			}
		} else if (oldFolderRoot != null) {
			root.removeChild(oldFolderRoot);
		}

		final Node oldFileRoot = getNodebyName(root.getChildNodes(), FILEPROPERTIESXMLNODE);
		final Node newFileRoot = saveFileProperties(document, workingDirectories, files);
		if (newFileRoot != null) {
			if (oldFileRoot == null) {
				root.appendChild(newFileRoot);
			} else {
				root.replaceChild(newFileRoot, oldFileRoot);
			}
		} else if (oldFileRoot != null) {
			root.removeChild(oldFileRoot);
		}
	}

	/**
	 * Loads an sets the projects information from an XML document node.
	 * This will also traverse all files and folders in the project, setting
	 * the needed properties.
	 * 
	 * @param node
	 *                the node to use as root when reading from.
	 * @param changedResources
	 *                contains the list of resources that were actually
	 *                changed by the settings loaded.
	 * */
	public void loadProjectInfoFromNode(final Node node, final Set<IResource> changedResources) {
		final NodeList rootList = node.getChildNodes();
		Node filePropertiesNode = null;
		Node folderPropertiesNode = null;
		for (int i = 0, size = rootList.getLength(); i < size; i++) {
			final Node tempNode = rootList.item(i);
			final String nodeName = tempNode.getNodeName();
			if (PROJECTPROPERTIESXMLNODE.equals(nodeName)) {
				loadProjectProperties(tempNode, changedResources);
			} else if (FOLDERPROPERTIESXMLNODE.equals(nodeName)) {
				folderPropertiesNode = tempNode;
			} else if (FILEPROPERTIESXMLNODE.equals(nodeName)) {
				filePropertiesNode = tempNode;
			}
		}

		final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false);
		final ResourceVisitor saveVisitor = new ResourceVisitor(workingDirectories);
		// collect the resources
		try {
			project.accept(saveVisitor);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While collecting project resources of `" + project.getName() + "'", e);
		}

		final Map<String, IFile> files = saveVisitor.getFiles();
		final Map<String, IFolder> folders = saveVisitor.getFolders();

		loadFileProperties(filePropertiesNode, files.values(), changedResources);
		loadFolderProperties(folderPropertiesNode, folders.values(), changedResources);
	}

	/**
	 * Copies the project information related to files from the source node
	 * to the target node.
	 * 
	 * @see ProjectFileHandler#copyProjectInfo(Node, Node, IProject, Map,
	 *      Map, boolean)
	 * 
	 * @param sourceNode
	 *                the node used as the source of the information.
	 * @param document
	 *                the document to contain the result, used to create the
	 *                XML nodes.
	 * @param project
	 *                the project to be worked on, used to identify the
	 *                folders.
	 * @param files
	 *                the files to be worked on.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyFileProperties(final Node sourceNode, final Document document, final IProject project, final Map<String, IFile> files,
			final boolean saveDefaultValues) {
		final Set<String> notYetReachedFiles = new HashSet<String>();
		notYetReachedFiles.addAll(files.keySet());

		Element root = null;

		if (sourceNode != null) {
			final NodeList resourceList = sourceNode.getChildNodes();
			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final Node tempNode = resourceList.item(i);
				if (FILERESOURCEXMLNODE.equals(tempNode.getNodeName())) {
					final Node newNode = FileBuildPropertyData.copyFileProperties(tempNode, document, project,
							notYetReachedFiles, saveDefaultValues);
					if (newNode != null) {
						if (root == null) {
							root = document.createElement(FILEPROPERTIESXMLNODE);
						}
						root.appendChild(newNode);
					}
				}
			}
		}

		if (saveDefaultValues) {
			for (String fileName : notYetReachedFiles) {
				final Node newNode = FileBuildPropertyData.copyDefaultFileProperties(document, files.get(fileName));
				if (newNode != null) {
					if (root == null) {
						root = document.createElement(FILEPROPERTIESXMLNODE);
					}
					root.appendChild(newNode);
				}
			}
		}

		return root;
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
	 * @param folders
	 *                the folder to be worked on.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * 
	 * @return the resulting target node.
	 * */
	public static Node copyFolderProperties(final Node sourceNode, final Document document, final IProject project,
			final Map<String, IFolder> folders, final boolean saveDefaultValues) {
		final Set<String> notYetReachedFolders = new HashSet<String>();
		notYetReachedFolders.addAll(folders.keySet());

		Element root = null;

		if (sourceNode != null) {
			final NodeList resourceList = sourceNode.getChildNodes();
			for (int i = 0, size = resourceList.getLength(); i < size; i++) {
				final Node tempNode = resourceList.item(i);
				if (FOLDERRESOURCEXMLNODE.equals(tempNode.getNodeName())) {
					final Node newNode = FolderBuildPropertyData.copyFolderProperties(tempNode, document, project,
							notYetReachedFolders, saveDefaultValues);
					if (newNode != null) {
						if (root == null) {
							root = document.createElement(FOLDERPROPERTIESXMLNODE);
						}
						root.appendChild(newNode);
					}
				}
			}
		}

		if (saveDefaultValues) {
			for (String folderName : notYetReachedFolders) {
				final Node newNode = FolderBuildPropertyData.copyDefaultFolderProperties(document, folders.get(folderName));
				if (newNode != null) {
					if (root == null) {
						root = document.createElement(FOLDERPROPERTIESXMLNODE);
					}
					root.appendChild(newNode);
				}
			}
		}

		return root;
	}

	/**
	 * Copies the project properties information from the source node to the
	 * target node which is returned.
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
	public static Node copyProjectProperties(final Node sourceNode, final Document document, final boolean saveDefaultValues) {
		if (sourceNode == null) {
			return null;
		}

		final NodeList resourceList = sourceNode.getChildNodes();

		Node namingConventionsNode = null;
		Node makefileSettingsNode = null;
		Node localBuildSettingsNode = null;
		Node remoteBuildSettingsNode = null;
		Node configurationPropertiesNode = null;

		for (int i = 0, size = resourceList.getLength(); i < size; i++) {
			Node tempNode = resourceList.item(i);
			String nodeName = tempNode.getNodeName();
			if (MAKEFILESETTINGSXMLNODE.equals(nodeName)) {
				makefileSettingsNode = tempNode;
			} else if (LOCALBUILDSETTINGS.equals(nodeName)) {
				localBuildSettingsNode = tempNode;
			} else if (REMOTEBUILDPROPERTIES_XMLNODE.equals(nodeName)) {
				remoteBuildSettingsNode = tempNode;
			} else if (CONFIGURATION_REQUIREMENTS_NODE.equals(nodeName)) {
				configurationPropertiesNode = tempNode;
			} else if (NAMINGCONVENTIONS_XMLNODE.equals(nodeName)) {
				namingConventionsNode = tempNode;
			}
		}

		Node root = document.createElement(PROJECTPROPERTIESXMLNODE);

		root.appendChild(ProjectBuildPropertyData.copyMakefileSettings(makefileSettingsNode, document, saveDefaultValues));
		root.appendChild(ProjectBuildPropertyData.copyLocalBuildSettings(localBuildSettingsNode, document, saveDefaultValues));

		Element element = ProjectRemoteBuildPropertyData.copyRemoteBuildProperties(remoteBuildSettingsNode, document, saveDefaultValues);
		if (element != null) {
			root.appendChild(element);
		}

		element = ProjectNamingConventionPropertyData.copyProperties(namingConventionsNode, document, saveDefaultValues);
		if (element != null) {
			root.appendChild(element);
		}

		element = ProjectConfigurationsPropertyData.copyProperties(configurationPropertiesNode, document);
		if (element != null) {
			root.appendChild(element);
		}

		return root;
	}

	/**
	 * Copies the project related information from the source node to the
	 * target node. This operation is almost like a load & save as
	 * operation, with the following differences:
	 * <ul>
	 * <li>the data is never set to or read from the resources, the
	 * resources stay in the same state during the operation.
	 * <li>the data might be transformed, for example default values can be
	 * added/removed.
	 * </ul>
	 * 
	 * @param sourceNode
	 *                the node used as the source of the information.
	 * @param targetNode
	 *                the node to be used as the target node for the
	 *                resulting information.
	 * @param project
	 *                the project on which we are working.
	 * @param files
	 *                the list of files to be worked on, inserted in a
	 *                treemap to provide sorted access.
	 * @param folders
	 *                the list of folders to be worked on, inserted in a
	 *                treemap to provide sorted access.
	 * @param saveDefaultValues
	 *                whether the default values should be forced to be
	 *                added to the output.
	 * */
	public static void copyProjectInfo(final Node sourceNode, final Node targetNode, final IProject project, final Map<String, IFile> files,
			final Map<String, IFolder> folders, final boolean saveDefaultValues) {
		final NodeList rootList = sourceNode.getChildNodes();
		Node projectPropertiesNode = null;
		Node filePropertiesNode = null;
		Node folderPropertiesNode = null;
		for (int i = 0, size = rootList.getLength(); i < size; i++) {
			final Node tempNode = rootList.item(i);
			final String nodeName = tempNode.getNodeName();
			if (PROJECTPROPERTIESXMLNODE.equals(nodeName)) {
				projectPropertiesNode = tempNode;
			} else if (FOLDERPROPERTIESXMLNODE.equals(nodeName)) {
				folderPropertiesNode = tempNode;
			} else if (FILEPROPERTIESXMLNODE.equals(nodeName)) {
				filePropertiesNode = tempNode;
			}
		}

		Document document = targetNode.getOwnerDocument();
		Node newNode = copyProjectProperties(projectPropertiesNode, document, saveDefaultValues);
		if (newNode != null) {
			targetNode.appendChild(newNode);
		}
		newNode = copyFolderProperties(folderPropertiesNode, document, project, folders, saveDefaultValues);
		if (newNode != null) {
			targetNode.appendChild(newNode);
		}
		newNode = copyFileProperties(filePropertiesNode, document, project, files, saveDefaultValues);
		if (newNode != null) {
			targetNode.appendChild(newNode);
		}
	}

	/**
	 * Checks the name of the active configuration for the given project.
	 * 
	 * @param project
	 *                the project to check.
	 * 
	 * @return the name of the active configuration.
	 * */
	public static String getActiveConfigurationName(final IProject project) {
		String activeConfigurationName = null;
		try {
			activeConfigurationName = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.ACTIVECONFIGURATION));
			if (activeConfigurationName == null || activeConfigurationName.length() == 0) {
				activeConfigurationName = DEFAULTCONFIGURATIONNAME;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While getting the active configuration of `" + project.getName() + "'", e);
			activeConfigurationName = DEFAULTCONFIGURATIONNAME;
		}

		return activeConfigurationName;
	}

	/**
	 * Collects the names of the available configurations from the provided
	 * document.
	 * 
	 * @param document
	 *                the document to use.
	 * 
	 * @return the list of available configuration names.
	 * */
	public static List<String> getConfigurations(final Document document) {
		final List<String> configurationNames = new ArrayList<String>();
		configurationNames.add(DEFAULTCONFIGURATIONNAME);

		if (document != null) {
			final NodeList rootList = document.getDocumentElement().getChildNodes();
			for (int i = 0, size = rootList.getLength(); i < size; i++) {
				final Node temp = rootList.item(i);
				if (CONFIGURATIONSXMLNODE.equals(temp.getNodeName())) {
					final NodeList configurationList = temp.getChildNodes();
					for (int j = 0, size2 = configurationList.getLength(); j < size2; j++) {
						final Node temp2 = configurationList.item(j);
						if (CONFIGURATIONXMLNODE.equals(temp2.getNodeName())) {
							Node attribute = temp2.getAttributes().getNamedItem("name");
							configurationNames.add(attribute.getTextContent());
						}
					}
				}
			}
		}

		return configurationNames;
	}

	/**
	 * Searches for and returns the node responsible for storing the
	 * configuration data for a project.
	 * 
	 * @param root
	 *                the root node to search, this should be the root node
	 *                of the whole document.
	 * @param configurationName
	 *                the name of the configuration.
	 * 
	 * @return the node for the configuration with the given name, or null
	 *         if no such configuration could be found.
	 * */
	public static Node findConfigurationNode(final Node root, final String configurationName) {
		if (configurationName == null || DEFAULTCONFIGURATIONNAME.equals(configurationName)) {
			return root;
		}

		final NodeList rootList = root.getChildNodes();
		for (int i = 0, size = rootList.getLength(); i < size; i++) {
			final Node temp = rootList.item(i);
			if (CONFIGURATIONSXMLNODE.equals(temp.getNodeName())) {
				final NodeList configurationList = temp.getChildNodes();
				for (int j = 0, size2 = configurationList.getLength(); j < size2; j++) {
					final Node temp2 = configurationList.item(j);
					if (CONFIGURATIONXMLNODE.equals(temp2.getNodeName())) {
						final Node attribute = temp2.getAttributes().getNamedItem("name");
						if (attribute != null && configurationName.equals(attribute.getTextContent())) {
							return temp2;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Searches for and removes the node responsible for storing the
	 * configuration data for a project.
	 * 
	 * @param root
	 *                the root node to search, this should be the root node
	 *                of the whole document.
	 * @param configurationName
	 *                the name of the configuration.
	 * */
	public void removeConfigurationNode(final Node root, final String configurationName) {
		if (configurationName == null || DEFAULTCONFIGURATIONNAME.equals(configurationName)) {
			return;
		}

		final NodeList rootList = root.getChildNodes();
		for (int i = 0, size = rootList.getLength(); i < size; i++) {
			final Node temp = rootList.item(i);
			if (CONFIGURATIONSXMLNODE.equals(temp.getNodeName())) {
				final NodeList configurationList = temp.getChildNodes();
				for (int j = 0, size2 = configurationList.getLength(); j < size2; j++) {
					final Node temp2 = configurationList.item(j);
					if (CONFIGURATIONXMLNODE.equals(temp2.getNodeName())) {
						final Node attribute = temp2.getAttributes().getNamedItem("name");
						if (attribute != null && configurationName.equals(attribute.getTextContent())) {
							temp.removeChild(temp2);
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * Clear the children of a node recursively from all textnodes, that
	 * were just indenting the output.
	 * 
	 * @param root
	 *                the root node to use.
	 * */
	public static void clearNode(final Node root) {
		if (root == null) {
			return;
		}

		Node child = root.getFirstChild();
		while (child != null) {
			Node sibling = child.getNextSibling();
			if (Node.TEXT_NODE == child.getNodeType()) {
				final String content = child.getNodeValue().trim();
				if (content.isEmpty()) {
					root.removeChild(child);
				}
			} else {
				clearNode(child);
			}
			child = sibling;
		}
	}

	/**
	 * Indents the children of the provided note recursively.
	 * 
	 * @param document
	 *                the document to be used to create the textnodes doing
	 *                the indentations.
	 * @param root
	 *                the root node.
	 * @param level
	 *                the indentation level to start at.
	 * */
	public static void indentNode(final Document document, final Node root, final int level) {
		Node child = root.getFirstChild();

		if (child != null && Node.COMMENT_NODE == child.getNodeType()) {
			// Do nothing
		} else {
			if (child != null && Node.TEXT_NODE != child.getNodeType()) {
				root.insertBefore(document.createTextNode(ProjectFileHandler.getXMLIndentation(level + 2)), child);
			}
		}

		while (child != null) {
			final Node sibling = child.getNextSibling();

			if (Node.TEXT_NODE == child.getNodeType()) {
				child.setTextContent(child.getNodeValue().replaceAll("\\s", ""));
			} else {
				if (Node.COMMENT_NODE == child.getNodeType()) {
					root.removeChild(child);
				} else {
					if (sibling == null) {
						root.appendChild(document.createTextNode(ProjectFileHandler.getXMLIndentation(level)));
					} else if (Node.TEXT_NODE != sibling.getNodeType()) {
						root.insertBefore(document.createTextNode(ProjectFileHandler.getXMLIndentation(level + 2)), sibling);
					}
				}
				indentNode(document, child, level + 2);
			}

			child = sibling;
		}

	}
}
