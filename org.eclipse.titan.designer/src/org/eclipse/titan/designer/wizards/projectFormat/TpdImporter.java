/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TitanURIUtil;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.DOMErrorHandlerImpl;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectDocumentHandlingUtility;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.Bundle;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.SAXException;

/**
 * This class should be the importation of modules described in a provided Tpd
 * file.
 * 
 * @author Kristof Szabados
 * */
public class TpdImporter {
	private static final String CREATING_PROJECT = "creating project";
	private static final String CREATION_FAILED = "Project creation failed";
	private static final String TPD_XSD = "schema/TPD.xsd";

	private DOMImplementationLS domImplLS;
	private LSParser parser;
	private DOMConfiguration config;

	private Map<String, String> finalProjectNames = new HashMap<String, String>();
	private Map<URI, Document> projectsToImport = new HashMap<URI, Document>();
	private List<URI> importChain = new ArrayList<URI>();
	private boolean wasAutoBuilding;
	private Shell shell;
	private final boolean headless;

	public TpdImporter(final Shell shell, final boolean headless) {
		this.shell = shell;
		this.headless = headless;
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		wasAutoBuilding = description.isAutoBuilding();
		description.setAutoBuilding(false);
		try {
			ResourcesPlugin.getWorkspace().setDescription(description);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("while disabling autobuild on the workspace", e);
		}
		Activator.getDefault().pauseHandlingResourceChanges();
	}

	/**
	 * Internal function used to do the import job. It is needed to extract
	 * this functionality i order to be able to handle erroneous situations.
	 *
	 * @param projectsCreated
	 *                the list of projects created so far. In case of
	 *                problems we will try to delete them.
	 * @param monitor
	 *                the monitor used to report progress.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	public boolean internalFinish(final String projectFile, final boolean isSkipExistingProjects, final boolean isOpenPropertiesForAllImports,
			final List<IProject> projectsCreated, final IProgressMonitor monitor) {
		if (projectFile == null || "".equals(projectFile.trim())) {
			return false;
		}

		System.setProperty(DOMImplementationRegistry.PROPERTY, ProjectFormatConstants.DOM_IMPLEMENTATION_SOURCE);
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("While importing from `" + projectFile + "'", e);
			activatePreviousSettings();
			return false;
		}

		// Specifying "LS 3.0" in the features list ensures that the
		// DOMImplementation
		// object implements the load and save features of the DOM 3.0
		// specification.
		final DOMImplementation domImpl = registry.getDOMImplementation(ProjectFormatConstants.LOAD_SAVE_VERSION);
		domImplLS = (DOMImplementationLS) domImpl;
		// If the mode is MODE_SYNCHRONOUS, the parse and parseURI
		// methods of the LSParser
		// object return the org.w3c.dom.Document object. If the mode is
		// MODE_ASYNCHRONOUS, the parse and parseURI methods return null.
		parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, ProjectFormatConstants.XML_SCHEMA);

		config = parser.getDomConfig();
		DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("validate", Boolean.TRUE);
		config.setParameter("schema-type", ProjectFormatConstants.XML_SCHEMA);
		config.setParameter("well-formed", Boolean.TRUE);
		config.setParameter("validate-if-schema", Boolean.TRUE);

		Validator tpdValidator = null;
		try {
			final Schema tpdXsd = getTPDSchema();
			tpdValidator = tpdXsd.newValidator();
		} catch (Exception e) {
			ErrorReporter.INTERNAL_ERROR(e.getMessage());//Hint: cp $TTCN3_DIR/etc/xsd/TPD.xsd designer/schema/
		}

		IPath projectFilePath = Path.fromOSString(projectFile);
		URI projectFileURI = URIUtil.toURI(projectFilePath);

		if (!loadURIDocuments(projectFileURI, null, tpdValidator)) {
			return false;
		}

		IProgressMonitor internalMonitor = new SubProgressMonitor(monitor, 1);
		internalMonitor.beginTask("Loading data", 3);
		IProgressMonitor projectCreationMonitor = new SubProgressMonitor(internalMonitor, 1);
		projectCreationMonitor.beginTask("Creating required projects", projectsToImport.size());

		Map<URI, IProject> projectMap = new HashMap<URI, IProject>();
		for (URI file : projectsToImport.keySet()) {
			Document actualDocument = projectsToImport.get(file);

			IProject project = createProject(actualDocument.getDocumentElement(), file.equals(projectFileURI) || !isSkipExistingProjects);
			if (project == null) {
				projectCreationMonitor.worked(1);
				continue;
			}
			projectsCreated.add(project);
			projectMap.put(file, project);
			try {
				project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.LOAD_LOCATION), file.toString());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While loading referenced project from `" + file.getPath() + "'", e);
			}
			projectCreationMonitor.worked(1);
		}
		projectCreationMonitor.done();

		IProgressMonitor normalInformationLocadingMonitor = new SubProgressMonitor(internalMonitor, 1);
		normalInformationLocadingMonitor.beginTask("Loading directly stored project information", projectsToImport.size());

		for (URI file : projectsToImport.keySet()) {
			if (!projectMap.containsKey(file)) {
				normalInformationLocadingMonitor.worked(1);
				continue;
			}

			IProject project = projectMap.get(file);
			IPath projectFileFolderPath = new Path(file.getPath()).removeLastSegments(1);
			URI projectFileFolderURI = URIUtil.toURI(projectFileFolderPath);
			Document actualDocument = projectsToImport.get(file);

			Element mainElement = actualDocument.getDocumentElement();
			if (!loadProjectDataFromNode(mainElement, project, projectFileFolderURI)) {
				return false;
			}

			normalInformationLocadingMonitor.worked(1);
		}
		normalInformationLocadingMonitor.done();

		IPath mainProjectFileFolderPath = new Path(projectFileURI.getPath()).removeLastSegments(1);
		URI mainProjectFileFolderURI = URIUtil.toURI(mainProjectFileFolderPath);

		List<Node> packedProjects = loadPackedProjects(projectsToImport.get(projectFileURI));
		IProgressMonitor packedInformationLocadingMonitor = new SubProgressMonitor(internalMonitor, 1);
		packedInformationLocadingMonitor.beginTask("Loading packed project information", packedProjects.size());
		for (Node node : packedProjects) {
			IProject project = createProject(node, false);
			if (project == null) {
				packedInformationLocadingMonitor.worked(1);
				continue;
			}
			projectsCreated.add(project);

			try {
				project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						ProjectBuildPropertyData.LOAD_LOCATION), projectFileURI.toString());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While loading packed project `" + project.getName() + "'",e);
			}

			if (!loadProjectDataFromNode(node, project, mainProjectFileFolderURI)) {
				return false;
			}

			packedInformationLocadingMonitor.worked(1);
		}
		packedInformationLocadingMonitor.done();

		IProject mainProject = projectMap.get(projectFileURI);
		if (mainProject == null) {
			internalMonitor.done();
			return false;
		}

		List<WorkspaceJob> jobs = new ArrayList<WorkspaceJob>();
		List<IProject> projectsToBeConfigured;
		if (isOpenPropertiesForAllImports) {
			projectsToBeConfigured = projectsCreated;
		} else {
			projectsToBeConfigured = new ArrayList<IProject>();
			projectsToBeConfigured.add(mainProject);
		}

		if (!headless) {
			for (final IProject project : projectsToBeConfigured) {
				WorkspaceJob loadJob = new WorkspaceJob("Property initilizer for " + project.getName()) {
					@Override
					public IStatus runInWorkspace(final IProgressMonitor monitor) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								Shell shell = new Shell(Display.getDefault());
								PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(shell, project,
										GeneralConstants.PROJECT_PROPERTY_PAGE, null, null);
								if (dialog != null) {
									dialog.open();
								}
							}
						});
						return Status.OK_STATUS;
					}
				};
				loadJob.setUser(false);
				loadJob.setSystem(true);
				loadJob.setRule(project.getWorkspace().getRuleFactory().refreshRule(project));
				loadJob.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
				loadJob.schedule();
				jobs.add(loadJob);
			}

			for (WorkspaceJob job : jobs) {
				try {
					job.join();
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace("Interrupted while performing: " + job.getName(),e);
				}
			}
		}

		activatePreviousSettings();

		internalMonitor.done();
		return true;
	}

	public static void validateTpd(final File tpdFile) throws IOException, SAXException {
		final Schema tpdXsd = getTPDSchema();
		Validator validator = tpdXsd.newValidator();
		validator.validate(new StreamSource(tpdFile));
	}

	public static Schema getTPDSchema() throws IOException, SAXException {
		Bundle bundle = Platform.getBundle(ProductConstants.PRODUCT_ID_DESIGNER);
		InputStream xsdInputStream = null;
		try {
			xsdInputStream = FileLocator.openStream(bundle, new Path(TPD_XSD), false);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema result = factory.newSchema(new StreamSource(xsdInputStream));
			xsdInputStream.close();
			return result;
		} finally {
			IOUtils.closeQuietly(xsdInputStream);
		}
	}

	private void activatePreviousSettings() {
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		if (description.isAutoBuilding() != wasAutoBuilding) {
			description.setAutoBuilding(wasAutoBuilding);
			try {
				ResourcesPlugin.getWorkspace().setDescription(description);
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("Resetting autobuild settings to" + wasAutoBuilding,e);
			}
		}
		Activator.getDefault().resumeHandlingResourceChanges();
	}

	/**
	 * Collects the list of packed projects from the provided document.
	 *
	 * @param document
	 *                the document to check.
	 *
	 * @return the list of found packed projects, an empty list if none.
	 * */
	private List<Node> loadPackedProjects(final Document document) {
		NodeList referencedProjectsList = document.getDocumentElement().getChildNodes();
		Node packed = ProjectFileHandler.getNodebyName(referencedProjectsList, ProjectFormatConstants.PACKED_REFERENCED_PROJECTS_NODE);
		if (packed == null) {
			return new ArrayList<Node>();
		}

		List<Node> result = new ArrayList<Node>();
		NodeList projects = packed.getChildNodes();
		for (int i = 0, size = projects.getLength(); i < size; i++) {
			Node referencedProjectNode = projects.item(i);
			if (ProjectFormatConstants.PACKED_REFERENCED_PROJECT_NODE.equals(referencedProjectNode.getNodeName())) {
				result.add(referencedProjectNode);
			}
		}

		return result;
	}

	/**
	 * Loads the project data from the provided node onto the provided
	 * project.
	 *
	 * @param mainElement
	 *                the node to load the data from.
	 * @param project
	 *                the project to set the loaded data on.
	 * @param projectFileFolderURI
	 *                the URI of the folder to calculate all paths relative
	 *                to.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadProjectDataFromNode(final Node mainElement, final IProject project, final URI projectFileFolderURI) {
		NodeList mainNodes = mainElement.getChildNodes();

		Node referencedProjectsNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.REFERENCED_PROJECTS_NODE);
		if (referencedProjectsNode != null) {
			if (!loadReferencedProjectsData(referencedProjectsNode, project)) {
				return false;
			}
		}

		Node pathVariablesNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.PATH_VARIABLES);
		if (pathVariablesNode != null) {
			if (!loadPathVariables(pathVariablesNode, project.getName())) {
				return false;
			}
		}

		Node foldersNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.FOLDERS_NODE);
		if (foldersNode != null) {
			if (!loadFoldersData(foldersNode, project, projectFileFolderURI)) {
				return false;
			}
		}

		Node filesNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.FILES_NODE);
		if (filesNode != null) {
			if (!loadFilesData(filesNode, project, projectFileFolderURI)) {
				return false;
			}
		}

		ProjectDocumentHandlingUtility.createDocument(project);

		if (!loadConfigurationData(project, mainNodes)) {
			return false;
		}

		return true;
	}

	/**
	 * Load the data related to project references.
	 *
	 * @param referencedProjectsNode
	 *                the node containing information on referenced
	 *                projects.
	 * @param project
	 *                the project to set the data on.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadReferencedProjectsData(final Node referencedProjectsNode, final IProject project) {
		NodeList referencedProjectsList = referencedProjectsNode.getChildNodes();
		List<IProject> referencedProjects = new ArrayList<IProject>();
		for (int i = 0, size = referencedProjectsList.getLength(); i < size; i++) {
			Node referencedProjectNode = referencedProjectsList.item(i);
			if (referencedProjectNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = referencedProjectNode.getAttributes();
			if (attributeMap == null) {
				continue;
			}
			Node nameNode = attributeMap.getNamedItem(ProjectFormatConstants.REFERENCED_PROJECT_NAME_ATTRIBUTE);
			if (nameNode == null) {
				displayError("Import failed",
						"Error while importing project " + project.getName() + " the name attribute of the "
								+ i + " th referenced project is missing");
				return false;
			}

			String projectName = nameNode.getTextContent();

			String realProjectName = finalProjectNames.get(projectName);
			if (realProjectName != null && realProjectName.length() > 0) {
				IProject tempProject = ResourcesPlugin.getWorkspace().getRoot().getProject(realProjectName);
				referencedProjects.add(tempProject);
			} else {
				IProject tempProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				referencedProjects.add(tempProject);
			}
		}
		try {
			IProjectDescription description = project.getDescription();
			description.setReferencedProjects(referencedProjects.toArray(new IProject[referencedProjects.size()]));
			project.setDescription(description, null);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While setting project references for `" + project.getName() + "'",e);
			return false;
		}

		return true;
	}

	/**
	 * Load the information describing folders.
	 *
	 * @param foldersNode
	 *                the node to load from.
	 * @param project
	 *                the project to set this information on.
	 * @param projectFileFolderURI
	 *                the location of the project file's folder.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadFoldersData(final Node foldersNode, final IProject project, final URI projectFileFolderURI) {
		final URI projectLocationURI = project.getLocationURI();
		NodeList folderNodeList = foldersNode.getChildNodes();

		for (int i = 0, size = folderNodeList.getLength(); i < size; i++) {
			Node folderItem = folderNodeList.item(i);
			if (folderItem.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = folderItem.getAttributes();
			if (attributeMap == null) {
				continue;
			}
			Node projectRelativePathNode = attributeMap.getNamedItem(ProjectFormatConstants.FOLDER_ECLIPSE_LOCATION_NODE);
			if (projectRelativePathNode == null) {
				displayError("Import failed",
						"Error while importing project " + project.getName()
								+ " the project relative path attribute of the " + i
								+ " th folder is missing");
				return false;
			}

			String projectRelativePath = projectRelativePathNode.getTextContent();

			Node relativeURINode = attributeMap.getNamedItem(ProjectFormatConstants.FOLDER_RELATIVE_LOCATION);
			Node rawURINode = attributeMap.getNamedItem(ProjectFormatConstants.FOLDER_RAW_LOCATION);

			IFolder folder = project.getFolder(projectRelativePath);
			try {
				if (relativeURINode != null) {
					String relativeLocation = relativeURINode.getTextContent();

					URI locationuri;
					try {
						locationuri = org.eclipse.core.runtime.URIUtil.fromString(relativeLocation);
					} catch (URISyntaxException e) {
						continue;
					}
					URI absoluteURI = org.eclipse.core.runtime.URIUtil.makeAbsolute(locationuri, projectFileFolderURI);

					if (TitanURIUtil.isPrefix(projectLocationURI, absoluteURI)) {
						folder.create(false, true, null);
					} else {
						folder.createLink(absoluteURI, IResource.ALLOW_MISSING_LOCAL, null);
					}
				} else if (rawURINode != null) {
					String rawURI = rawURINode.getTextContent();
					folder.createLink(URI.create(rawURI), IResource.ALLOW_MISSING_LOCAL, null);
				} else {
					TITANDebugConsole.getConsole().newMessageStream()
							.println("Can not create the resource " + folder.getFullPath().toString()
									+ " the location information is missing or corrupted");
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While creating folder `" + folder.getName() + "'",e);
			}
		}

		return true;
	}

	/**
	 * Load the information describing files.
	 *
	 * @param filesNode
	 *                the node to load from.
	 * @param project
	 *                the project to set this information on.
	 * @param projectFileFolderURI
	 *                the location of the project file's folder.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadFilesData(final Node filesNode, final IProject project, final URI projectFileFolderURI) {
		NodeList fileNodeList = filesNode.getChildNodes();
		for (int i = 0, size = fileNodeList.getLength(); i < size; i++) {
			Node fileItem = fileNodeList.item(i);
			if (fileItem.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = fileItem.getAttributes();
			if (attributeMap == null) {
				// there is no attribute, check next node
				continue;
			}
			Node projectRelativePathNode = attributeMap.getNamedItem(ProjectFormatConstants.FILE_ECLIPSE_LOCATION_NODE);
			if (projectRelativePathNode == null) {
				displayError("Import failed",
						"Error while importing project " + project.getName() + " some attributes of the "
								+ i + " th file are missing");
				return false;
			}

			String projectRelativePath = projectRelativePathNode.getTextContent();

			Node relativeURINode = attributeMap.getNamedItem(ProjectFormatConstants.FILE_RELATIVE_LOCATION);
			Node rawURINode = attributeMap.getNamedItem(ProjectFormatConstants.FILE_RAW_LOCATION);

			IFile targetFile = project.getFile(projectRelativePath);
			if (!targetFile.exists()) {
				try {
					if (relativeURINode != null) {
						String relativeLocation = relativeURINode.getTextContent();

						URI locationuri;
						try {
							locationuri = org.eclipse.core.runtime.URIUtil.fromString(relativeLocation);
						} catch (URISyntaxException e) {
							continue;
						}
						URI absoluteURI = org.eclipse.core.runtime.URIUtil.makeAbsolute(locationuri, projectFileFolderURI);

						targetFile.createLink(absoluteURI, IResource.ALLOW_MISSING_LOCAL, null);
					} else if (rawURINode != null) {
						String rawURI = rawURINode.getTextContent();
						targetFile.createLink(URI.create(rawURI), IResource.ALLOW_MISSING_LOCAL, null);
					} else {
						TITANDebugConsole.getConsole().newMessageStream()
								.println("Can not create the resource " + targetFile.getFullPath().toString()
										+ " the location information is missing or corrupted");
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While creating link for `" + targetFile + "'",e);
				}
			}
		}

		return true;
	}

	/**
	 * Load the information on path variables.
	 *
	 * @param rootNode
	 *                the node to load from.
	 * @param projectName
	 *                the name of the project to be used on the user
	 *                interface.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadPathVariables(final Node rootNode, final String projectName) {
		final IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();

		NodeList variableNodes = rootNode.getChildNodes();
		for (int i = 0, size = variableNodes.getLength(); i < size; i++) {
			Node variable = variableNodes.item(i);
			if (variable.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = variable.getAttributes();
			if (attributeMap == null) {
				continue;
			}

			Node nameNode = attributeMap.getNamedItem("name");
			Node valueNode = attributeMap.getNamedItem("value");

			if (nameNode == null || valueNode == null) {
				displayError("Import failed",
						"Error while importing project " + projectName
								+ " some attributes of a path variable are missing");
				return false;
			}

			final String variableName = nameNode.getTextContent();
			final String variableValue = valueNode.getTextContent();

			if (headless) {
				try {
					pathVariableManager.setValue(variableName, new Path(variableValue));
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("While setting path variable `" + variableName + "' in headless mode",e);
				}
			} else {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							if (pathVariableManager.isDefined(variableName)) {
								IPath path = pathVariableManager.getValue(variableName);
								if (!variableValue.equals(path.toString())) {
									EditPathVariableDialog dialog = new EditPathVariableDialog(shell,
											variableName, path, new Path(variableValue));
									if (Window.OK == dialog.open()) {
										IPath actualValue = dialog.getActualValue();
										pathVariableManager.setValue(variableName, actualValue);
									}
								}
							} else {
								// check whether we have non null shell
								if (shell != null) {
									NewPathVariableDialog dialog = new NewPathVariableDialog(shell, variableName,
											new Path(variableValue));
									if (Window.OK == dialog.open()) {
										IPath actualValue = dialog.getActualValue();
										pathVariableManager.setValue(variableName, actualValue);
									}
								}
							}
						} catch (CoreException e) {
							ErrorReporter.logExceptionStackTrace("While setting path variable `" + variableName + "' in GUI mode",e);
						}
					}
				});
			}
		}

		return true;
	}

	/**
	 * Loads the configuration related options onto the project from the
	 * document being loaded.
	 *
	 * @param project
	 *                the project to load onto.
	 * @param mainNodes
	 *                the mainNodes to check for the configuration related
	 *                options.
	 *
	 * @return true if the import was successful, false otherwise.
	 * */
	private boolean loadConfigurationData(final IProject project, final NodeList mainNodes) {
		final Document targetDocument = ProjectDocumentHandlingUtility.getDocument(project);
		Node activeConfigurationNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.ACTIVE_CONFIGURATION_NODE);
		String activeConfiguration = ProjectFormatConstants.DEFAULT_CONFIGURATION_NAME;
		if (activeConfigurationNode != null) {
			activeConfiguration = activeConfigurationNode.getTextContent();
		} else {
			activeConfiguration = "Default";
		}
		try {
			project.setPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					ProjectBuildPropertyData.ACTIVECONFIGURATION), activeConfiguration);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(
					"While setting `" + activeConfiguration + "' as configuration for project `" + project.getName() + "'", e);
		}

		// Remove possible target configuration nodes in existance
		removeConfigurationNodes(targetDocument.getDocumentElement());

		Node configurationsNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.CONFIGURATIONS_NODE);
		NodeList configurationsNodeList = configurationsNode.getChildNodes();

		Node targetActiveConfiguration = targetDocument.createElement(ProjectFormatConstants.ACTIVE_CONFIGURATION_NODE);
		targetActiveConfiguration.appendChild(targetDocument.createTextNode(activeConfiguration));
		targetDocument.getDocumentElement().appendChild(targetActiveConfiguration);

		Node targetConfigurationsRoot = targetDocument.createElement(ProjectFormatConstants.CONFIGURATIONS_NODE);
		targetDocument.getDocumentElement().appendChild(targetConfigurationsRoot);

		for (int i = 0, size = configurationsNodeList.getLength(); i < size; i++) {
			Node configurationNode = configurationsNodeList.item(i);
			if (configurationNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = configurationNode.getAttributes();
			if (attributeMap == null) {
				continue;
			}
			Node nameNode = attributeMap.getNamedItem(ProjectFormatConstants.CONFIGURATION_NAME_ATTRIBUTE);
			if (nameNode == null) {
				displayError("Import failed",
						"Error while importing project " + project.getName()
								+ " the name attribute of a referenced project is missing");
				return false;
			}

			String configurationName = nameNode.getTextContent();

			if (ProjectFormatConstants.DEFAULT_CONFIGURATION_NAME.equals(configurationName)) {
				copyConfigurationData(targetDocument.getDocumentElement(), configurationNode);
			} else {
				Element targetConfiguration = targetDocument.createElement(ProjectFormatConstants.CONFIGURATION_NODE);
				targetConfiguration.setAttribute(ProjectFormatConstants.CONFIGURATION_NAME_ATTRIBUTE, configurationName);
				targetConfigurationsRoot.appendChild(targetConfiguration);

				copyConfigurationData(targetConfiguration, configurationNode);
			}
		}

		ProjectDocumentHandlingUtility.saveDocument(project);
		ProjectFileHandler handler = new ProjectFileHandler(project);
		handler.loadProjectSettingsFromDocument(targetDocument);

		return true;
	}

	/**
	 * Remove those child nodes of the provided node, which are related to
	 * handling configuration data.
	 *
	 * @param rootNode
	 *                the node to use.
	 * */
	private void removeConfigurationNodes(final Node rootNode) {
		NodeList rootNodeList = rootNode.getChildNodes();

		Node tempNode = ProjectFileHandler.getNodebyName(rootNodeList, ProjectFormatConstants.CONFIGURATIONS_NODE);
		if (tempNode != null) {
			rootNode.removeChild(tempNode);
		}
		tempNode = ProjectFileHandler.getNodebyName(rootNodeList, ProjectFileHandler.PROJECTPROPERTIESXMLNODE);
		if (tempNode != null) {
			rootNode.removeChild(tempNode);
		}
		tempNode = ProjectFileHandler.getNodebyName(rootNodeList, ProjectFileHandler.FOLDERPROPERTIESXMLNODE);
		if (tempNode != null) {
			rootNode.removeChild(tempNode);
		}
		tempNode = ProjectFileHandler.getNodebyName(rootNodeList, ProjectFileHandler.FILEPROPERTIESXMLNODE);
		if (tempNode != null) {
			rootNode.removeChild(tempNode);
		}
		tempNode = ProjectFileHandler.getNodebyName(rootNodeList, ProjectFormatConstants.ACTIVE_CONFIGURATION_NODE);
		if (tempNode != null) {
			rootNode.removeChild(tempNode);
		}
	}

	/**
	 * Copies the configuration related data from the source node, to the
	 * target node.
	 *
	 * @param targetRoot
	 *                the node where the configuration data should be moved
	 *                to.
	 * @param sourceRoot
	 *                the node from where the configuration data is moved.
	 * */
	private void copyConfigurationData(final Element targetRoot, final Node sourceRoot) {
		final Document document = targetRoot.getOwnerDocument();
		final NodeList rootList = sourceRoot.getChildNodes();
		Node targetNode = null;
		for (int i = 0, size = rootList.getLength(); i < size; i++) {
			Node tempNode = rootList.item(i);
			String nodeName = tempNode.getNodeName();
			if (ProjectFileHandler.PROJECTPROPERTIESXMLNODE.equals(nodeName)
					|| ProjectFileHandler.FOLDERPROPERTIESXMLNODE.equals(nodeName)
					|| ProjectFileHandler.FILEPROPERTIESXMLNODE.equals(nodeName)) {
				targetNode = document.importNode(tempNode, true);
				ProjectFileHandler.clearNode(targetNode);
				targetRoot.appendChild(targetNode);
			}
		}
	}

	class ProjectSelector implements Runnable {
		private String projectName;
		private IProject project = null;
		private boolean cancelled = false;

		public ProjectSelector(final String projectName) {
			this.projectName = projectName;
		}

		public String getProjectName() {
			return projectName;
		}

		public IProject getProject() {
			return project;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public void run() {
			NewProjectNameDialog dialog = new NewProjectNameDialog(shell, projectName);
			if (dialog.open() == Window.OK) {
				projectName = dialog.getName();
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if (project.exists()) {
					cancelled = true;
				}
			} else {
				cancelled = true;
			}
		}

	}

	/**
	 * Create a new project based on the information found in the provided
	 * document.
	 * */
	private IProject createProject(final Node mainElement, final boolean treatExistingProjectAsError) {
		NodeList mainNodes = mainElement.getChildNodes();
		Node projectNameNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.PROJECTNAME_NODE);
		if (null == projectNameNode) {
			TITANDebugConsole.getConsole().newMessageStream()
					.println("The name of the project could not be found in the project descriptor, it will not be created.");
			return null;
		}
		String originalProjectName = projectNameNode.getFirstChild().getTextContent();
		String projectName = originalProjectName;
		finalProjectNames.put(originalProjectName, projectName);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			if (!treatExistingProjectAsError || headless) {
				ErrorReporter.logWarning("A project with the name " + projectName + " already exists, skipping it !");
				return null;
			}

			ProjectSelector temp = new ProjectSelector(projectName);
			Display.getDefault().syncExec(temp);
			if (temp.cancelled) {
				return null;
			}

			projectName = temp.getProjectName();
			project = temp.getProject();
		}

		finalProjectNames.put(originalProjectName, projectName);

		project = createNewProject(project, projectName);
		if (project == null) {
			TITANDebugConsole.getConsole().newMessageStream().println("There was an error while creating the project " + projectName);
			return null;
		}

		try {
			TITANNature.addTITANBuilderToProject(project);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("While adding builder to `" + project.getName() + "'",e);
		}

		return project;
	}

	/**
	 * Load the project information document from the provided file and
	 * recursively for all project files mentioned in the referenced
	 * projects section.
	 *
	 * @param file
	 *                the file to load the data from.
	 * @param source
	 *                the source file referencing the target, or null if
	 *                none.
	 * @param validator
	 *                the xml validator. can be <code>null</code>
	 *
	 * @return true if there were no errors, false otherwise.
	 * */
	private boolean loadURIDocuments(final URI file, final URI source, final Validator validator) {
		if (projectsToImport.containsKey(file)) {
			return true;
		}

		if (!"file".equals(file.getScheme()) && !"".equals(file.getScheme())) {
			ErrorReporter.logError("Loading of project information is only supported for local files right now. " + file.toString()
					+ " could not be loaded");
			return false;
		}

		Document document = getDocumentFromFile(file.getPath());
		if (document == null) {
			final StringBuilder builder = new StringBuilder("It was not possible to load the imported project file: '" + file.toString()
					+ "'\n");
			for (int i = importChain.size() - 1; i >= 0; --i) {
				builder.append("imported by: '");
				builder.append(importChain.get(i).toString());
				builder.append("'\n");
			}
			ErrorReporter.logError(builder.toString());
			return false;
		}
		if (validator != null) {
			try {
				validator.validate(new StreamSource(new File(file)));
			} catch (final Exception e) {
				ErrorReporter.logExceptionStackTrace("Error while importing from file " + file + ": " + System.getProperty("line.separator"), e);
				return false;
			}
		}

		ProjectFileHandler.clearNode(document);

		projectsToImport.put(file, document);

		Element mainElement = document.getDocumentElement();
		NodeList mainNodes = mainElement.getChildNodes();
		Node referencedProjectsNode = ProjectFileHandler.getNodebyName(mainNodes, ProjectFormatConstants.REFERENCED_PROJECTS_NODE);
		if (referencedProjectsNode == null) {
			return true;
		}

		final IPath projectFileFolderPath = new Path(file.getPath()).removeLastSegments(1);
		NodeList referencedProjectsList = referencedProjectsNode.getChildNodes();
		boolean result = true;
		for (int i = 0, size = referencedProjectsList.getLength(); i < size; i++) {
			Node referencedProjectNode = referencedProjectsList.item(i);
			if (referencedProjectNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap attributeMap = referencedProjectNode.getAttributes();
			if (attributeMap == null) {
				continue;
			}
			Node nameNode = attributeMap.getNamedItem(ProjectFormatConstants.REFERENCED_PROJECT_NAME_ATTRIBUTE);
			if (nameNode == null) {
				displayError("Import failed",
						"Error while importing from file " + file
								+ " the name attribute of a referenced project is missing");
				return false;
			}

			final String projectName = nameNode.getTextContent();
			Node locationNode = attributeMap.getNamedItem(ProjectFormatConstants.REFERENCED_PROJECT_LOCATION_ATTRIBUTE);
			if (locationNode == null) {
				displayError("Import failed", "Error while importing from file " + file
						+ " the location attribute of the referenced project " + projectName
						+ " is not given.");
				return false;
			}

			String relativeLocation = locationNode.getTextContent();

			URI locationuri = null;
			try {
				locationuri = org.eclipse.core.runtime.URIUtil.fromString(relativeLocation);
			} catch (URISyntaxException e) {
				ErrorReporter.logExceptionStackTrace("While converting relative location from `" + relativeLocation + "'",e);
				return false;
			}
			URI absoluteURI = org.eclipse.core.runtime.URIUtil.makeAbsolute(locationuri, URIUtil.toURI(projectFileFolderPath));
			if (!"file".equals(absoluteURI.getScheme())) {
				final StringBuilder builder = new StringBuilder(
						"Loading of project information is only supported for local files right now. "
								+ absoluteURI.toString() + " could not be loaded\n");
				for (int j = importChain.size() - 1; j >= 0; --j) {
					builder.append("imported by: '");
					builder.append(importChain.get(j).toString());
					builder.append("'\n");
				}
				ErrorReporter.logError(builder.toString());
				continue;
			}

			importChain.add(file);
			result &= loadURIDocuments(absoluteURI, file, validator);
			importChain.remove(importChain.size() - 1);
		}

		return result;
	}

	private void displayError(final String title, final String message) {
		if (!headless) {
			ErrorReporter.parallelErrorDisplayInMessageDialog(title, message);
		}
		ErrorReporter.logError(message);
	}

	/**
	 * Extracts an XML document from the provided file.
	 *
	 * @param file
	 *                the file to read from.
	 * @return the extracted XML document, or null if there were some error.
	 * */
	public Document getDocumentFromFile(final String file) {
		final LSInput lsInput = domImplLS.createLSInput();
		Document document = null;
		try {
			FileInputStream istream = new FileInputStream(file);
			lsInput.setByteStream(istream);
			document = parser.parse(lsInput);
			istream.close();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("While getting the document from `" + file + "'",e);
		}

		return document;
	}

	/**
	 * Creating a new project.
	 *
	 * @return the new project created.
	 */
	IProject createNewProject(final IProject newProjectHandle, final String name) {
		IProject newProject;

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(name);

		/*
		 * A new project description in normal conditions does not
		 * contain any natures but as internal behavior tends to change
		 * without notification we can not rely on it.
		 */
		List<String> newIds = new ArrayList<String>();
		newIds.addAll(Arrays.asList(description.getNatureIds()));
		int index = newIds.indexOf(TITANNature.NATURE_ID);
		if (index == -1) {
			newIds.add(TITANNature.NATURE_ID);
			newIds.add(TITANNature.LOG_NATURE_ID);
		}

		description.setNatureIds(newIds.toArray(new String[newIds.size()]));

		if (headless) {
			try {
				createProject(description, newProjectHandle, new NullProgressMonitor());
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace("While creating project `" + newProjectHandle.getName() + "'", e);
			}
		} else {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
							@Override
							protected void execute(final IProgressMonitor monitor) throws CoreException {
								createProject(description, newProjectHandle, monitor);
							}
						};
						new ProgressMonitorDialog(new Shell(Display.getDefault())).run(true, true, op);
					} catch (InterruptedException e) {
						return;
					} catch (final InvocationTargetException e) {
						displayError(CREATION_FAILED, e.getMessage());
						ErrorReporter.logExceptionStackTrace("While creating project `" + newProjectHandle.getName() + "'", e);
						return;
					}
				}
			});
		}

		newProject = newProjectHandle;

		return newProject;
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
	protected void createProject(final IProjectDescription description, final IProject projectHandle, final IProgressMonitor monitor)
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
}
