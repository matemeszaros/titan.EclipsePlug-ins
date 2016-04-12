/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.core.CompilerVersionInformationCollector;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.SymbolicLinkHandler;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.extensions.ExtensionHandler;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in's life cycle.
 * 
 * @author Kristof Szabados
 */
public final class Activator extends AbstractUIPlugin {
	private static final String PLUGIN_ID = ProductConstants.PRODUCT_ID_DESIGNER;

	private static Activator plugin = null;
	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	private static final List<IResourceChangeEvent> BATCHED_EVENTS = new CopyOnWriteArrayList<IResourceChangeEvent>();

	private ResourceBundle resourceBundle;
	private Map<IProject, IResourceDelta> projects;
	private boolean handleResourceChanges = true;

	private static final class DeltaVisitor implements IResourceDeltaVisitor {
		private boolean hasNewOrRemovedResources = false;

		public boolean hasNewOrRemovedResources() {
			return hasNewOrRemovedResources;
		}

		@Override
		public boolean visit(final IResourceDelta delta) {
			final IResource resource = delta.getResource();
			switch (resource.getType()) {
			case IResource.PROJECT:
				if (delta.getKind() == IResourceDelta.REMOVED) {
					return false;
				}
				break;
			case IResource.FOLDER:
			case IResource.FILE:
				if (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED) {
					hasNewOrRemovedResources = true;
					return false;
				}
				break;
			default:
				break;
			}
			return true;
		}
	}

	private IResourceDeltaVisitor projectAdder = new IResourceDeltaVisitor() {

		@Override
		public boolean visit(final IResourceDelta delta) {
			switch (delta.getResource().getType()) {
			case IResource.ROOT:
				return true;
			case IResource.PROJECT:
				final IProject project = delta.getResource().getProject();
				if (TITANNature.hasTITANNature(project)) {
					if (!projects.containsKey(project)) {
						projects.put(project, delta);
					}
				}
				return false;
			default:
				return false;
			}
		}

	};

	private class ContentChangedFileFinder implements IResourceDeltaVisitor {
		private static final String TRUE_STRING = "true";
		private static final String LOG_FILE_EXTENSION = "log";
		private boolean changedNotExcludedResourceFound = false;
		private List<IContainer> workingDirectories;
		
		@Override
		public boolean visit(final IResourceDelta delta) throws CoreException {
			if (changedNotExcludedResourceFound) {
				return false;
			}

			switch (delta.getResource().getType()) {
			case IResource.ROOT:
				return true;
			case IResource.PROJECT:
				final IProject project = delta.getResource().getProject();
				if (TITANNature.hasTITANNature(project)) {
					workingDirectories = Arrays.asList(ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(false));
					return true;
				}
				return false;
			case IResource.FOLDER: 
				final IFolder folder = (IFolder) delta.getResource();
				if (!folder.exists()) {
					return false;
				}

				final String isExcludedFolder = folder.getPersistentProperty(GeneralConstants.EXCLUDED_FOLDER_QUALIFIER);
				final boolean isWorkingDir = workingDirectories == null ? false : workingDirectories.contains(folder);
				if (isWorkingDir || TRUE_STRING.equals(isExcludedFolder)) {
					return false;
				}
				return true;
			case IResource.FILE: 
				if (delta.getFlags() == IResourceDelta.NO_CHANGE
						|| (delta.getFlags() & IResourceDelta.MARKERS) == IResourceDelta.MARKERS
						|| (delta.getFlags() & IResourceDelta.OPEN) == IResourceDelta.OPEN) {
					return false;
				}
				final IFile file = (IFile) delta.getResource();
				if (!file.exists()) {
					return false;
				}
				if (LOG_FILE_EXTENSION.equals(file.getFileExtension())) {
					return false;
				}
				final String isExcludedFile = file.getPersistentProperty(GeneralConstants.EXCLUDED_FILE_QUALIFIER);
				if (!TRUE_STRING.equals(isExcludedFile)) {
					changedNotExcludedResourceFound = true;
				}
				return false;
			default:
				return false;
			}
		}
		
		public boolean getChangeFound() {
			return changedNotExcludedResourceFound;
		}
	}
	
	private IResourceChangeListener decoratorUpdater = new IResourceChangeListener() {
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			if (!handleResourceChanges) {
				return;
			}

			final IResourceDelta eventDelta = event.getDelta();
			if (eventDelta == null) {
				return;
			}

			for (final IResourceDelta delta  : eventDelta.getAffectedChildren()) {
				final IResource changedResource = delta.getResource();
				final ContentChangedFileFinder changeFinder = new ContentChangedFileFinder();
				
				if (changedResource.getType() == IResource.PROJECT && TITANNature.hasTITANNature((IProject) changedResource)) {
					try {
						if (!Boolean.TRUE.equals(changedResource.getSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE))) {
							return;
						}
						
						delta.accept(changeFinder);
						if (changeFinder.getChangeFound()) {
							changedResource.setSessionProperty(GeneralConstants.PROJECT_UP_TO_DATE, false);
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}
		}
	};

	private IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			BATCHED_EVENTS.add(event);
			if (getDefault().handleResourceChanges) {
				batchedEventHandler();
			}
		}
	};


	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * Handle the resource changes that has been batched.
	 */
	protected void batchedEventHandler() {
		projects = new HashMap<IProject, IResourceDelta>();
		for (final IResourceChangeEvent event : BATCHED_EVENTS) {
			final IResourceDelta resourceDelta = event.getDelta();
			if (resourceDelta == null) {
				switch (event.getType()) {
				case IResourceChangeEvent.PRE_CLOSE:
				case IResourceChangeEvent.PRE_DELETE:
					GlobalParser.clearAllInformation(event.getResource().getProject());
					break;
				default:
					break;
				}
			} else {
				try {
					resourceDelta.accept(projectAdder);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
				MarkerHandler.handleResourceChanges(event);
			}
		}
		BATCHED_EVENTS.clear();

		final IPreferencesService prefs = Platform.getPreferencesService();

		// for every project check if the TITANProperties file was changed or need to be changed
		for (final IProject project : projects.keySet()) {
			// which can be handled
			if (project != null && project.isAccessible() && projects.get(project) != null) {
				final IResourceDelta projectDelta = projects.get(project);
				if (projectDelta.getKind() == IResourceDelta.ADDED) {
					// new project appeared load the settings
					final ProjectFileHandler pfHandler = new ProjectFileHandler(project);
					pfHandler.loadProjectSettings();
					PropertyNotificationManager.firePropertyChange(project);
					continue;
				}

				try {
					final IResourceDelta propertiesDelta = projectDelta.findMember(new Path(ProjectFileHandler.XML_TITAN_PROPERTIES_FILE));
					final DeltaVisitor visitor = new DeltaVisitor();
					projectDelta.accept(visitor);
					if (visitor.hasNewOrRemovedResources()) {
						final ProjectFileHandler pfHandler = new ProjectFileHandler(project);
						if (propertiesDelta == null || propertiesDelta.getKind() == IResourceDelta.REMOVED) {
							// project setting file does not exist
							pfHandler.saveProjectSettings();
						} else {
							// the project settings have changed and we have new resources, the project was updated by the user
							pfHandler.loadProjectSettings();
							PropertyNotificationManager.firePropertyChange(project);
						}
					} else {
						if (propertiesDelta != null) {
							final ProjectFileHandler pfHandler = new ProjectFileHandler(project);
							if (propertiesDelta.getKind() == IResourceDelta.REMOVED) {
								// save the settings if the only change was that it was removed externally
								pfHandler.saveProjectSettings();
							} else {
								// load the settings if it changed
								pfHandler.loadProjectSettings();
								PropertyNotificationManager.firePropertyChange(project);
							}
						}
					}
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}

				// check if it needs to be built, if full analysis is needed, and that the working directory is always set as derived
				final IProject[] referencingProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencingProjects();
				for (int i = 0; i < referencingProjects.length; i++) {
					ProjectBasedBuilder.setForcedBuild(referencingProjects[i]);
				}

				if ((projectDelta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
					TITANBuilder.markProjectForRebuild(project);
				}

				if (projectDelta.getKind() == IResourceDelta.ADDED) {
					final WorkspaceJob derivedSetter = new WorkspaceJob("Derived flag setter") {
						@Override
						public IStatus runInWorkspace(final IProgressMonitor monitor) {
							final IContainer[] workingDirectories = ProjectBasedBuilder.getProjectBasedBuilder(project).getWorkingDirectoryResources(true);
							for (IContainer workingDirectory : workingDirectories) {
								if (workingDirectory != null && workingDirectory.isAccessible()) {
									try {
										workingDirectory.setDerived(true, monitor);
									} catch (CoreException e) {
										ErrorReporter.logExceptionStackTrace(e);
									}
								}
							}

							return Status.OK_STATUS;
						}
					};
					derivedSetter.setPriority(Job.LONG);
					derivedSetter.setUser(false);
					derivedSetter.setSystem(true);
					derivedSetter.setRule(project.getWorkspace().getRuleFactory().modifyRule(project));
					derivedSetter.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
					derivedSetter.schedule();
				}
			}
		}

		// if on-the-fly analysis is enabled,
		// for every project where there was some change
		// we need to invoke the analyzer to refresh the information databases.
		if (prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			for (final IProject project : projects.keySet()) {
				// which can be handled
				if (project != null && project.isAccessible() && projects.get(project) != null) {
					final IResourceDelta projectDelta = projects.get(project);
					final DeltaVisitor visitor = new DeltaVisitor();
					try {
						projectDelta.accept(visitor);
						if (!visitor.hasNewOrRemovedResources() && (projectDelta.getFlags() & IResourceDelta.DESCRIPTION) == 0) {
							continue;
						}
					} catch (CoreException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
					
					final WorkspaceJob buildStarter = new WorkspaceJob("Build starter") {
						@Override
						public IStatus runInWorkspace(final IProgressMonitor monitor) {
							GlobalDeltaVisitor tempVisitor = new GlobalDeltaVisitor(project);
							try {
								projectDelta.accept(tempVisitor);
							} catch (CoreException e) {
								ErrorReporter.logExceptionStackTrace(e);
							}
							final WorkspaceJob[] outdatingJobs = tempVisitor.reportOutdatedFiles();

							WorkspaceJob analyzeAfterOutdating = new WorkspaceJob("analyzeAfterOutdating") {
								@Override
								public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
									//Wait for reportOutdatedFiles() to finish
									try {
										for (WorkspaceJob job : outdatingJobs) {
											if(job != null) {
												job.join();
											}
										}
									} catch (InterruptedException e) {
										ErrorReporter.logExceptionStackTrace(e);
									}

									GlobalParser.getProjectSourceParser(project).analyzeAll(false);
									// It is of no importance when this analysis will run, or end for that matter.

									boolean generateMakefile;
									try {
										generateMakefile = "true".equals(project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
												ProjectBuildPropertyData.GENERATE_MAKEFILE_PROPERTY)));
									} catch (CoreException e) {
										generateMakefile = false;
									}
									if (generateMakefile) {
										TITANBuilder.markProjectForRebuild(project);
										SymbolicLinkHandler.createSymlinks(project);
										TITANBuilder.regenerateMakefile(project);
									}

									return Status.OK_STATUS;
								}
							};
							analyzeAfterOutdating.setPriority(Job.LONG);
							analyzeAfterOutdating.setUser(false);
							analyzeAfterOutdating.setSystem(true);
							analyzeAfterOutdating.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
							analyzeAfterOutdating.schedule();

							return Status.OK_STATUS;
						}
					};
					buildStarter.setPriority(Job.LONG);
					buildStarter.setUser(false);
					buildStarter.setSystem(true);
					buildStarter.setRule(project.getWorkspace().getRuleFactory().refreshRule(project));
					buildStarter.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
					buildStarter.schedule();
				}
			}
		}
	}

	/**
	 * Turn off the handling of resource changes for a period of time.
	 *
	 * @see #resumeHandlingResourceChanges()
	 */
	public void pauseHandlingResourceChanges() {
		handleResourceChanges = false;
	}

	/**
	 * Turn back on the handling of resource changes. If there were any changes while the handling was turned off, they are applied.
	 *
	 * @see #pauseHandlingResourceChanges()
	 */
	public void resumeHandlingResourceChanges() {
		handleResourceChanges = true;
		batchedEventHandler();
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(listener);
		workspace.addResourceChangeListener(decoratorUpdater);
		final ISaveParticipant participant = new SaveParticipant();
		final ISavedState lastState = workspace.addSaveParticipant(ProductConstants.PRODUCT_ID_DESIGNER, participant);
		if (lastState != null) {
			// TODO check if this should be run in a separate thread
			lastState.processResourceChangeEvents(listener);
		}

		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			ErrorReporter.logExceptionStackTrace(e);
			resourceBundle = null;
		}

		// initialize extension handler
		ExtensionHandler.INSTANCE.registerContributors();

		final WorkspaceJob initializer = new WorkspaceJob("Initializing the TITAN toolset") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				//TODO: preload if needed
				// preload some of the heavier classes, and do the analysis of the static ASN.1 module
				
				//The existence of the compiler must not be checked, as it is not a required component.
				CompilerVersionInformationCollector.collectInformation();

				return Status.OK_STATUS;
			}
		};
		initializer.setPriority(Job.LONG);
		if (GeneralConstants.DEBUG
				&& Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
						PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
			initializer.setSystem(false);
			initializer.setUser(true);
		} else {
			initializer.setSystem(true);
			initializer.setUser(false);
		}
		initializer.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		initializer.schedule();
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(listener);
		workspace.removeResourceChangeListener(decoratorUpdater);
		plugin = null;

		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Activates or deactivates the given activity
	 * @param activityId The activity
	 * @param enable <code>true</code> if the given activity should be enabled, <code>false</code> otherwise
	 * @return <code>true</code> if an activity change is performed, <code>false</code> otherwise
	 */
	@SuppressWarnings("unchecked")
	public static final boolean switchActivity(final String activityId, final boolean enable) {
		final IWorkbenchActivitySupport as = PlatformUI.getWorkbench().getActivitySupport();
		final IActivityManager am = as.getActivityManager();
		@SuppressWarnings({ "rawtypes" })
		final Set enabledActivities = new HashSet(am.getEnabledActivityIds());

		final boolean activityChange = enable ? enabledActivities.add(activityId) : enabledActivities.remove(activityId);
		if (!activityChange) {
			return false;
		}

		as.setEnabledActivityIds(enabledActivities);
		return true;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
