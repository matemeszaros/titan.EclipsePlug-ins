/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.GlobalProjectStructureTracker;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This action allows the user to toggle the exclusion from build flag on
 * several files at the same time.
 * 
 * @author Kristof Szabados
 */
public final class ExcludeFromBuild extends AbstractHandler implements IObjectActionDelegate {
	static final String TRUE_STRING = "true";
	static final String FALSE_STRING = "false";
	private ISelection selection;
	private List<IProject> projects;

	/**
	 * This method traverses the list of selected files and folders and
	 * toggles the excluded from build flag on each of them.
	 * <p>
	 * <ul>
	 * <li>After setting the properties the decorator is refreshed to
	 * represent the actual state
	 * <li>The project settings are saved.
	 * <li>In case the project has the {@link TITANBuilder} enabled:
	 * <ul>
	 * <li>The symbolic links are updated in every involved project
	 * <li>The makefile is removed (as it is out dated) in every involved
	 * project
	 * <li>The builder is invoked (if automatic build is set) in every
	 * involved project
	 * </ul>
	 * The execution of these items stops if any of them fails to execute
	 * </ul>
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 * 
	 * @param action
	 *                the action proxy that handles the presentation portion
	 *                of the action (not used here)
	 */
	@Override
	public void run(final IAction action) {
		doExclusion(selection);
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// Do nothing
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		doExclusion(selection);

		return null;
	}

	/**
	 * do the actual exclusion.
	 * 
	 * @param selection
	 *                the objects selected to be excluded or included
	 * */
	private void doExclusion(final ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;
		if (structSelection.isEmpty()) {
			return;
		}

		WorkspaceJob op = new ExcluderWorkspaceJob(structSelection);
		op.setPriority(Job.LONG);
		op.setSystem(false);
		op.setUser(true);
		op.setRule(ResourcesPlugin.getWorkspace().getRoot());
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();

	}

	private class ExcluderWorkspaceJob extends WorkspaceJob {
		private final IStructuredSelection structSelection;

		public ExcluderWorkspaceJob(IStructuredSelection structSelection) {
			super("Changing the excludedness of resources");
			this.structSelection = structSelection;
		}

		@Override
		public IStatus runInWorkspace(final IProgressMonitor monitor) {
			projects = new ArrayList<IProject>();
			final Vector<WorkspaceJob> exclusionJobs = new Vector<WorkspaceJob>(structSelection.size());

			final Set<IResource> resourcesToRefresh = new HashSet<IResource>();
			final Set<IProject> projectsToRefresh = new HashSet<IProject>();

			for (Object selected : structSelection.toList()) {
				if (selected instanceof IFile) {
					final IFile file = (IFile) selected;
					resourcesToRefresh.add(file);
					try {
						final String mode = file.getPersistentProperty(ResourceExclusionHelper.EXCLUDED_FILE_QUALIFIER);
						file.setPersistentProperty(ResourceExclusionHelper.EXCLUDED_FILE_QUALIFIER,
								TRUE_STRING.equals(mode) ? FALSE_STRING : TRUE_STRING);

						WorkspaceJob op = new WorkspaceJob("Changing the excludedness of file: " + file.getName()) {
							@Override
							public IStatus runInWorkspace(final IProgressMonitor monitor) {
								if (!TRUE_STRING.equals(mode)) {
									MarkerHandler.markAllMarkersForRemoval(file);
									MarkerHandler.removeAllMarkedMarkers(file);
								}

								exclusionJobs.add(GlobalParser.getProjectSourceParser(file.getProject())
										.reportOutdating(file));

								return Status.OK_STATUS;
							}
						};
						op.setPriority(Job.SHORT);
						op.setSystem(true);
						op.setUser(false);
						op.setRule(file);
						op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
						exclusionJobs.add(op);
						op.schedule();

						if (!projects.contains(file.getProject())) {
							projects.add(file.getProject());
							projectsToRefresh.add(file.getProject());
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				} else if (selected instanceof IFolder) {
					final IFolder folder = (IFolder) selected;
					resourcesToRefresh.add(folder);
					try {
						final String mode = folder.getPersistentProperty(ResourceExclusionHelper.EXCLUDED_FOLDER_QUALIFIER);
						folder.setPersistentProperty(ResourceExclusionHelper.EXCLUDED_FOLDER_QUALIFIER,
								TRUE_STRING.equals(mode) ? FALSE_STRING : TRUE_STRING);
						if (!TRUE_STRING.equals(mode)) {
							WorkspaceJob op = new WorkspaceJob("Changing the excludedness of folder: " + folder.getName()) {
								@Override
								public IStatus runInWorkspace(final IProgressMonitor monitor) {
									MarkerHandler.markAllMarkersForRemoval(folder);
									MarkerHandler.removeAllMarkedMarkers(folder);

									return Status.OK_STATUS;
								}
							};
							op.setPriority(Job.SHORT);
							op.setSystem(true);
							op.setUser(false);
							op.setRule(folder);
							op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
							op.schedule();
						}

						exclusionJobs.add(GlobalParser.getProjectSourceParser(folder.getProject()).reportOutdating(folder));

						if (!projects.contains(folder.getProject())) {
							projects.add(folder.getProject());
							projectsToRefresh.add(folder.getProject());
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}

			WorkspaceJob op = new WorkspaceJob("Waiting for exclusion threads to finish.") {
				@Override
				public IStatus runInWorkspace(final IProgressMonitor monitor) {
					for (int i = 0; i < exclusionJobs.size(); i++) {
						WorkspaceJob job = exclusionJobs.get(i);
						try {
							if (job != null) {
								job.join();
							}
						} catch (InterruptedException e) {
							ErrorReporter.logExceptionStackTrace(e);
						}
					}

					for (final IProject project : projects) {
						ProjectFileHandler projectFileHandler = new ProjectFileHandler(project);
						projectFileHandler.saveProjectSettings();
						GlobalProjectStructureTracker.projectChanged(project);

						PropertyNotificationManager.firePropertyChange(project);
					}

					return Status.OK_STATUS;
				}
			};
			op.setPriority(Job.LONG);
			op.setSystem(true);
			op.setUser(false);
			op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
			op.schedule();

			return Status.OK_STATUS;
		}
	}
}
