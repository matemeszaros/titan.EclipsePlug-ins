/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.actions;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.utils.Analyzer;
import org.eclipse.titanium.markers.utils.AnalyzerCache;
import org.eclipse.titanium.preferences.PreferenceConstants;
import org.eclipse.titanium.utils.ProjectAnalyzerJob;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Action delegate of code smell searching.
 * <p>
 * This action works on the current structured selections, most notably when the
 * active view is the Project Explorer.
 * <p>
 * If a project is selected, an analyzer job is scheduled for the project as a
 * whole, while for single files, only those files are analyzed.
 * 
 * @author poroszd
 * 
 */
public class CheckCodeSmells extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPage iwPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final ISelection selection = iwPage.getSelection();

		final List<IResource> res = org.eclipse.titan.common.utils.SelectionUtils.getResourcesFromSelection(selection);

		final Map<IProject, List<IFile>> files = new HashMap<IProject, List<IFile>>();
		final List<IProject> projects = new ArrayList<IProject>();
		collectResourcesToBeAnalyzed(new LinkedList<IResource>(res), files, projects);

		final String titaniumId = Activator.PLUGIN_ID;
		final String onTheFlyPref = PreferenceConstants.ON_THE_FLY_SMELLS;
		final boolean onTheFlyEnabled = Platform.getPreferencesService().getBoolean(titaniumId, onTheFlyPref, false, null);
		final Analyzer analyzer = AnalyzerCache.withPreference();

		//Clear the files, that will be analyzed inside projects.
		for (final IProject project : projects) {
			files.remove(project);
		}
		// check projects
		checkProjects(projects, onTheFlyEnabled, analyzer);

		// check separate files of project
		checkSeparateFiles(files, onTheFlyEnabled, analyzer);

		return null;
	}

	/**
	 * @param res the resources that have to be checked for potential files and projects
	 * @param files the files found for analyzes
	 * @param projects the projects found for analyzes
	 */
	private void collectResourcesToBeAnalyzed(final Deque<IResource> res, final Map<IProject, List<IFile>> files, final List<IProject> projects) {
		while (!res.isEmpty()) {
			final IResource resource = res.pollFirst();
			if (resource instanceof IProject) {
				final IProject project = (IProject) resource;
				projects.add(project);
			} else if (resource instanceof IFolder) {
				try {
					for (IResource r2 : ((IFolder) resource).members()) {
						res.addLast(r2);
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace("Error while collecting resources", e);
				}
			} else if (resource instanceof IFile) {
				final IFile file = (IFile) resource;
				final String extension = file.getFileExtension();
				if ("ttcn".equals(extension) || "ttcn3".equals(extension)) {
					final IProject project = file.getProject();
					List<IFile> filesInProject = files.get(project);
					if (filesInProject == null) {
						filesInProject = new ArrayList<IFile>();
						files.put(project, filesInProject);
					}
					filesInProject.add(file);
				}
			}
		}
	}

	/**
	 * @param projects the projects to be analyzed
	 * @param onTheFlyEnabled whether on-the-fly analysis is enabled or not
	 * @param analyzer the analyzer to be used to analyze the projects
	 */
	private void checkProjects(final List<IProject> projects, final boolean onTheFlyEnabled, final Analyzer analyzer) {
		for (final IProject project : projects) {

			new ProjectAnalyzerJob("Check " + project.getName() + " for code smells") {
				@Override
				public IStatus doPostWork(final IProgressMonitor monitor) {
					final SubMonitor progress = SubMonitor.convert(monitor, 100);
					if (!onTheFlyEnabled) {
						MarkerHandler handler;
						synchronized (project) {
							handler = analyzer.analyzeProject(progress.newChild(100), project);
						}
						handler.showAll();
					}
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
		}
	}

	/**
	 * @param files the files to be checked outside of projects.
	 * @param onTheFlyEnabled whether on-the-fly analysis is enabled or not
	 * @param analyzer the analyzer to be used to analyze the projects
	 */
	private void checkSeparateFiles(final Map<IProject, List<IFile>> files, final boolean onTheFlyEnabled, final Analyzer analyzer) {
		for (final IProject project : files.keySet()) {
			new ProjectAnalyzerJob("Check some files in " + project.getName() + " for code smells") {
				@Override
				public IStatus doPostWork(final IProgressMonitor monitor) {
					final SubMonitor progress = SubMonitor.convert(monitor, files.get(project).size());
					if (!onTheFlyEnabled) {
						final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
						for (final IFile file : files.get(project)) {
							final String actualModuleName = projectSourceParser.containedModule(file);
							final Module module = projectSourceParser.getModuleByName(actualModuleName);
							if (module != null) {
								progress.subTask("Analyzing module " + module.getName());
								MarkerHandler handler;
								synchronized (project) {
									handler = analyzer.analyzeModule(progress.newChild(1), module);
								}
								handler.showAll();
							}
						}
					}
					return Status.OK_STATUS;
				}
			}.quickSchedule(project);
		}
	}
}
