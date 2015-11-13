/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards.projectFormat;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kristof Szabados
 * */
public class TITANProjectExportWizard extends Wizard implements IExportWizard {

	private static final String NEWPROJECT_WINDOWTITLE = "Export TITAN Project to a TITAN Project descriptor file";
	private static final String NEWPROJECT_TITLE = "Create a TITAN Project Descriptor File";
	private static final String NEWPROJECT_DESCRIPTION = "Create a TITAN Project Descriptor (tpd) file in the workspace or in an external location";

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

		TITANProjectExporter exporter = new TITANProjectExporter(project, projectFile);

		exporter.setIsExcludedWorkingDirectoryContents(optionsPage.isExcludedWorkingDirectoryContents());
		exporter.setIsExcludedDotResources(optionsPage.isExcludedDotResources());
		exporter.setExcludeLinkedContents(optionsPage.isExcludeLinkedContents());
		exporter.setSaveDefaultValues(optionsPage.isSaveDefaultValues());
		exporter.setPackAllProjectsIntoOne(optionsPage.isPackAllProjectsIntoOne());

		return exporter.saveAll();
		
	}


}
