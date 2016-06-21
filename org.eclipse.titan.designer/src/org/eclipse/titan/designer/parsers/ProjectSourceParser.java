/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.OutOfMemoryCheck;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.editors.EditorTracker;
import org.eclipse.titan.designer.editors.ISemanticTITANEditor;
import org.eclipse.titan.designer.extensions.ExtensionHandler;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstantValues;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.IPropertyChangeListener;
import org.eclipse.titan.designer.properties.PropertyNotificationManager;
import org.eclipse.titan.designer.properties.data.ProjectConfigurationsPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * This is project level root of all parsing related activities. Every data that
 * was extracted from files while parsing them has its root here.
 * <p>
 * Out dated modules are kept around till the next semantic checking because,
 * when a file is being edited its AST although being outdated might be quarried
 * by code completion.
 * <p>
 * In not noted elsewhere all operations that modify the internal states are
 * executed in a parallel WorkspaceJob, which will have scheduling rules
 * required to access related resources.
 * 
 * TODO: I believe it would be enough to store a single workspacejob which was
 * last started (whether full analysis, syntax analysis, or outdate report).
 * Beside the immediately returned workspacejob the users should only need that
 * one to synchronize to, if they need to synchronize to something.
 * 
 * @author Kristof Szabados
 * */
public final class ProjectSourceParser {
	public static final String REQUIREDPROJECTNOTACCESSIBLE = "The project {0} is not accessible but is required to analyze the project {1}";
	public static final String REQUIREDPROJECTNOTTITANPROJECT = "The project {0} is not a TITAN project but is required to analyze the project {1}";

	private static final String SOURCE_ANALYSING = "Analysing the source code";

	private IProject project;

	private ProjectSourceSyntacticAnalyzer syntacticAnalyzer;
	private ProjectSourceSemanticAnalyzer semanticAnalyzer;

	/**
	 * Counts how many parallel full analyzer threads are running. Should
	 * not be more than 2. It can be 2 if there were changes while the
	 * existing analyzes run, which have to be checked by a subsequent
	 * check.
	 * */
	private AtomicInteger fullAnalyzersRunning = new AtomicInteger();
	// The workspacejob of the last registered full analysis. External users
	// might need this to synchronize to.
	private volatile WorkspaceJob lastFullAnalyzes = null;
	// Internal variable to mark when project is being analyzed. Might not
	// be useful any longer, but hard to check.
	// TODO check if still required
	private volatile boolean analyzesRunning = false;

	/**
	 * Counts how many parallel syntax only analyzer threads are running.
	 * Should not be more than 2. It can be 2 if there were changes while
	 * the existing analyzes run, which have to be checked by a subsequent
	 * check.
	 * */
	private AtomicInteger syntaxAnalyzersRunning = new AtomicInteger();
	// The workspacejob of the last registered full analysis. External users
	// might need this to synchronize to.
	private volatile WorkspaceJob lastSyntaxAnalyzes = null;

	private CompilationTimeStamp lastTimeChecked;

	private static IPropertyChangeListener listener = new IPropertyChangeListener() {

		@Override
		public void propertyChanged(final IResource resource) {
			ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(resource.getProject());
			if (resource instanceof IFile) {
				projectSourceParser.reportOutdating((IFile) resource);
			} else if (resource instanceof IFolder) {
				projectSourceParser.reportOutdating((IFolder) resource);
			}

			projectSourceParser.analyzeAll();
		}
	};
	static {
		PropertyNotificationManager.addListener(listener);
	}

	/**
	 * Basic constructor initializing the class's members, for the given
	 * project.
	 * 
	 * @param project
	 *                the project for which this instance will be
	 *                responsible for.
	 * */
	public ProjectSourceParser(final IProject project) {
		this.project = project;
		syntacticAnalyzer = new ProjectSourceSyntacticAnalyzer(project, this);
		semanticAnalyzer = new ProjectSourceSemanticAnalyzer(this);
	}

	ProjectSourceSyntacticAnalyzer getSyntacticAnalyzer() {
		return syntacticAnalyzer;
	}

	public ProjectSourceSemanticAnalyzer getSemanticAnalyzer() {
		return semanticAnalyzer;
	}

	public CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	void setLastTimeChecked(final CompilationTimeStamp lastTimeChecked) {
		this.lastTimeChecked = lastTimeChecked;
	}

	/**
	 * Checks whether the internal data belonging to the provided file is
	 * out-dated.
	 * 
	 * @param file
	 *                the file to check.
	 * 
	 * @return true if the data was reported to be out-dated since the last
	 *         analysis.
	 * */
	public boolean isOutdated(final IFile file) {
		return syntacticAnalyzer.isOutdated(file) || semanticAnalyzer.isOutdated(file);
	}

	/**
	 * Returns the module with the provided name, or null
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @param name
	 *                the name of the module to return
	 * 
	 * @return the module having the provided name
	 * */
	public Module getModuleByName(final String name) {
		if (name == null) {
			return null;
		}

		return internalGetModuleByName(name, null, false);
	}

	/**
	 * Returns the TTCN-3 include file with the provided name, or null
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @param name
	 *                the name of the include file to return
	 * @param uptodateOnly
	 *                allow finding only the up-to-date modules.
	 * 
	 * @return the TTCN-3 include file having the provided name
	 * */
	public IFile getTTCN3IncludeFileByName(final String name) {
		if (name == null) {
			return null;
		}

		return internalTTCN3IncludeFileByName(name, null);
	}

	/**
	 * Returns the TTCN-3 include file with the provided name, or null.
	 * 
	 * @param name
	 *                the name of the inlude file to return.
	 * @param visitedprojects
	 *                the list of project already visited, to break infinite
	 *                loops.
	 * @param uptodateOnly
	 *                allow finding only the up-to-date modules.
	 * 
	 * @return the TTCN-3 include file having the provided name
	 * */
	private IFile internalTTCN3IncludeFileByName(final String name, final List<IProject> visitedprojects) {
		IFile tempFile;
		tempFile = syntacticAnalyzer.internalGetTTCN3IncludeFileByName(name);
		if (tempFile != null) {
			return tempFile;
		}

		List<IProject> visited = visitedprojects;
		if (visited == null) {
			visited = new ArrayList<IProject>();
		}
		visited.add(project);

		for (IProject tempProject : ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects()) {
			if (!visited.contains(tempProject)) {
				tempFile = GlobalParser.getProjectSourceParser(tempProject).internalTTCN3IncludeFileByName(name, visited);
				if (tempFile != null) {
					return tempFile;
				}
			}
		}

		return null;
	}

	public Collection<Module> getModules() {
		return semanticAnalyzer.internalGetModules();
	}

	/**
	 * Returns the module with the provided name, or null.
	 * 
	 * @param name
	 *                the name of the module to return.
	 * @param visitedprojects
	 *                the list of project already visited, to break infinite
	 *                loops.
	 * @param uptodateOnly
	 *                allow finding only the up-to-date modules.
	 * 
	 * @return the module having the provided name
	 * */
	private Module internalGetModuleByName(final String name, final List<IProject> visitedprojects, final boolean uptodateOnly) {
		Module tempModule;
		tempModule = semanticAnalyzer.internalGetModuleByName(name, uptodateOnly);
		if (tempModule != null) {
			return tempModule;
		}

		List<IProject> visited = visitedprojects;
		if (visited == null) {
			visited = new ArrayList<IProject>();
		}
		visited.add(project);

		for (IProject tempProject : ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects()) {
			if (!visited.contains(tempProject)) {
				tempModule = GlobalParser.getProjectSourceParser(tempProject).internalGetModuleByName(name, visited, uptodateOnly);
				if (tempModule != null) {
					return tempModule;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the actually known module's names.
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @return a set of the module names known in this project or in the
	 *         ones referenced.
	 * */
	public Set<String> getKnownModuleNames() {
		return internalGetKnownModuleNames(new ArrayList<IProject>());
	}

	/**
	 * Returns the actually known module's names.
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @param visitedprojects
	 *                the list of project already visited, to break infinite
	 *                loops.
	 * 
	 * @return a set of the module names known in this project or in the
	 *         ones referenced.
	 * */
	private Set<String> internalGetKnownModuleNames(final List<IProject> visitedprojects) {
		Set<String> temp = new HashSet<String>();

		if (visitedprojects.contains(project)) {
			return temp;
		}

		temp.addAll(semanticAnalyzer.internalGetKnownModuleNames());

		visitedprojects.add(project);

		for (IProject tempProject : ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects()) {
			temp.addAll(GlobalParser.getProjectSourceParser(tempProject).internalGetKnownModuleNames(visitedprojects));
		}

		return temp;
	}

	/**
	 * Returns weather the provided module is set to be semantically checked
	 * right now.
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @param module
	 *                the module to be checked
	 * 
	 * @return true if the module is checked, false otherwise
	 * */
	public boolean isSemanticallyChecked(final Module module) {
		return internalIsSemanticallyChecked(module);
	}

	/**
	 * Returns weather the provided module is set to be semantically checked
	 * right now.
	 * <p>
	 * Does check not only the actual project, but all referenced ones too.
	 * 
	 * @param module
	 *                the module to be checked
	 * 
	 * @return true if the module is checked, false otherwise
	 * */
	private boolean internalIsSemanticallyChecked(final Module module) {
		if (module == null || module.getLocation() == null) {
			return false;
		}

		if (semanticAnalyzer.semanticallyUptodateContains(module.getName())) {
			return true;
		}

		ProjectSourceParser parser = GlobalParser.getProjectSourceParser(module.getLocation().getFile().getProject());
		if (parser.semanticAnalyzer.semanticallyUptodateContains(module.getName())) {
			return true;
		}

		return false;
	}

	/**
	 * Returns the name of the module contained in the provided file, or
	 * null.
	 * 
	 * @param file
	 *                the file whose module we are interested in
	 * 
	 * @return the name of the module found in the file, or null
	 * */
	public String containedModuleName(final IFile file) {
		Module module = semanticAnalyzer.getModulebyFile(file);
		if (module != null) {
			return module.getName();
		}

		return null;
	}

	/**
	 * Returns the module contained in the provided file, or
	 * null.
	 * 
	 * @param file
	 *                the file whose module we are interested in
	 * 
	 * @return the module found in the file, or null
	 * */
	public Module containedModule(final IFile file) {
		return semanticAnalyzer.getModulebyFile(file);
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <p>
	 * Stores that this file is out of date for later
	 * <p>
	 * 
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportOutdating(final IFile outdatedFile) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for: " + outdatedFile.getName()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				syntacticAnalyzer.reportOutdating(outdatedFile);
				semanticAnalyzer.reportSemanticOutdating(outdatedFile);

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(outdatedFile);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date. This version does not mark the
	 * semantic data out-of-date.
	 * <p>
	 * Stores that this file is out of date for later usage
	 * <p>
	 * 
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportSyntacticOutdatingOnly(final IFile outdatedFile) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for: " + outdatedFile.getName()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				syntacticAnalyzer.reportSyntacticOutdatingOnly(outdatedFile);

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(outdatedFile);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <p>
	 * Stores that this file is out of date for later
	 * <p>
	 * 
	 * @param outdatedFiles
	 *                the file which seems to have changed
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportOutdating(final List<IFile> outdatedFiles) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for " + outdatedFiles.size() + " files") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				for (IFile file : outdatedFiles) {
					syntacticAnalyzer.reportOutdating(file);
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(getSchedulingRule());
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Reports that the semantic meaning of the provided file might have
	 * changed and so it's stored information became out of date.
	 * <p>
	 * Stores that this file is semantically out of date for later
	 * <p>
	 * 
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportSemanticOutdating(final List<IFile> outdatedFiles) {
		WorkspaceJob op = new WorkspaceJob("Reporting semantic outdate for " + outdatedFiles.size() + " files") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				for (IFile outdatedFile : outdatedFiles) {
					semanticAnalyzer.reportSemanticOutdating(outdatedFile);
				}

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(getSchedulingRule());
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Force the next semantic analyzation to reanalyze everything.
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob clearSemanticInformation() {
		WorkspaceJob op = new WorkspaceJob("Clearing all semantic information") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				semanticAnalyzer.clearSemanticInformation();

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(getSchedulingRule());
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Reports that the contents of the provided folder has changed and so
	 * it's stored information became out of date.
	 * <p>
	 * Stores that every file in this folder is out of date for later
	 * <p>
	 * 
	 * @param outdatedFolder
	 *                the folder whose files seems to have changed
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob reportOutdating(final IFolder outdatedFolder) {
		WorkspaceJob op = new WorkspaceJob("Reporting outdate for: " + outdatedFolder.getName()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				syntacticAnalyzer.reportOutdating(outdatedFolder);

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(outdatedFolder);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * The entry point of incremental parsing.
	 * <p>
	 * Handles the data storages, calls the module level incremental parser
	 * on the file, and if everything fails does a full parsing to correct
	 * possibly invalid states.
	 * 
	 * @param file
	 *                the edited file
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob updateSyntax(final IFile file, final TTCN3ReparseUpdater reparser) {
		WorkspaceJob op = new WorkspaceJob("Updating the syntax incremantally for: " + file.getName()) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				syntacticAnalyzer.updateSyntax(file, reparser);

				return Status.OK_STATUS;
			}
		};
		op.setPriority(Job.SHORT);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setRule(file);
		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.schedule();
		return op;
	}

	/**
	 * Internal function.
	 * <p>
	 * Builds the walking order of the projects from their referencing
	 * graph, and analyzes all found to be related to the actual.
	 * 
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 * 
	 * @return status information on exit.
	 * */
	private IStatus internalDoAnalyzeWithReferences(final IProgressMonitor monitor) {
		if (!checkConfigurationRequirements(project, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER)) {
			return Status.OK_STATUS;
		}
		
		if (OutOfMemoryCheck.isOutOfMemoryAlreadyReported()) {
			return Status.CANCEL_STATUS;
		}
				
		MarkerHandler.markAllMarkersForRemoval(project, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
		MarkerHandler.markAllMarkersForRemoval(project, GeneralConstants.ONTHEFLY_MIXED_MARKER);

		List<IProject> tobeAnalyzed = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();

		// analyze the project referencing the just now analyzed
		// projects in a bottom up order.
		Deque<IProject> temporalList = new LinkedList<IProject>();
		temporalList.addLast(project);
		tobeAnalyzed.remove(project);
		while (!temporalList.isEmpty()) {
			IProject tempProject = temporalList.getFirst();
			temporalList.removeFirst();
			if (!tobeAnalyzed.contains(tempProject)) {
				tobeAnalyzed.add(tempProject);

				IProject[] tempProjects = ProjectBasedBuilder.getProjectBasedBuilder(tempProject).getReferencingProjects();
				for (IProject tempProject2 : tempProjects) {
					if (!GlobalParser.getProjectSourceParser(tempProject2).analyzesRunning) {
						if (tempProject2.isAccessible()) {
							temporalList.addLast(tempProject2);
						} else {
							Location location = new Location(project);
							location.reportExternalProblem(MessageFormat.format(
									"The project {0} is not accessible but requires to analyze the project {1}",
									tempProject2.getName(), tempProject.getName()), IMarker.SEVERITY_ERROR,
									GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
						}
					}
				}
			}
		}

		// Collect those projects that might be needed to do the correct
		// analysis.
		List<IProject> additionalRequired = new ArrayList<IProject>();
		for (IProject project : tobeAnalyzed) {
			List<IProject> temp = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
			for (IProject temp2 : temp) {
				if (!tobeAnalyzed.contains(temp2) && !additionalRequired.contains(temp2)
						&& GlobalParser.getProjectSourceParser(temp2).getLastTimeChecked() == null) {
					if (temp2.isAccessible()) {
						additionalRequired.add(temp2);
					} else {
						Location location = new Location(project);
						location.reportExternalProblem(MessageFormat.format(REQUIREDPROJECTNOTACCESSIBLE, temp2.getName(),
								project.getName()), IMarker.SEVERITY_ERROR, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
					}
				}
			}
		}

		tobeAnalyzed.addAll(additionalRequired);

		// Do the analyzes in the determined order.
		CompilationTimeStamp compilationCounter = CompilationTimeStamp.getNewCompilationCounter();

		IProgressMonitor internalMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		internalMonitor.beginTask("Analysis of projects", tobeAnalyzed.size() * 2);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
		if (reportDebugInformation) {
			TITANDebugConsole.println("On-the-fly analyzation of project " + project.getName() + " started");
		}

		for (IProject project : tobeAnalyzed) {
			MarkerHandler.markAllMarkersForRemoval(project, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
			MarkerHandler.markAllMarkersForRemoval(project, GeneralConstants.ONTHEFLY_MIXED_MARKER);
		}

		List<IProject> tobeSemanticallyAnalyzed = new ArrayList<IProject>();

		try {
			for (int i = 0; i < tobeAnalyzed.size(); i++) {
				internalMonitor.setTaskName("Analyzing project " + tobeAnalyzed.get(i).getName());
				GlobalParser.getProjectSourceParser(tobeAnalyzed.get(i)).analyzesRunning = true;

				if (tobeAnalyzed.get(i).isAccessible()) {
					if (TITANNature.hasTITANNature(tobeAnalyzed.get(i))) {
						GlobalParser.getProjectSourceParser(tobeAnalyzed.get(i)).syntacticAnalyzer
								.internalDoAnalyzeSyntactically(new SubProgressMonitor(internalMonitor, 1));
						tobeSemanticallyAnalyzed.add(tobeAnalyzed.get(i));
					} else {
						Location location = new Location(project, 0, 0, 0);
						location.reportExternalProblem(MessageFormat.format(REQUIREDPROJECTNOTTITANPROJECT,
								tobeAnalyzed.get(i).getName(), project.getName()), IMarker.SEVERITY_ERROR,
								GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
						internalMonitor.worked(1);
					}
				} else {
					Location location = new Location(project);
					location.reportExternalProblem(
							MessageFormat.format(REQUIREDPROJECTNOTACCESSIBLE, tobeAnalyzed.get(i).getName(),
									project.getName()), IMarker.SEVERITY_ERROR,
							GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
					internalMonitor.worked(1);
				}
			}

			ProjectSourceSemanticAnalyzer.analyzeMultipleProjectsSemantically(tobeSemanticallyAnalyzed, internalMonitor, compilationCounter);
		} finally {
			for (int i = 0; i < tobeAnalyzed.size(); i++) {
				GlobalParser.getProjectSourceParser(tobeAnalyzed.get(i)).analyzesRunning = false;
			}
		}

		internalMonitor.done();
		lastTimeChecked = compilationCounter;

		return Status.OK_STATUS;
	}

	/**
	 * Analyzes all of the files which are in the project, but only
	 * syntactically.
	 * <ul>
	 * <li>the files possibly needed to analyze are collected first
	 * <li>those files, which are known to be up-to-date are filtered from
	 * this list
	 * <li>the files left in the list are analyzed in a new workspace job
	 * </ul>
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob analyzeAllOnlySyntactically() {
		IPreferencesService prefs = Platform.getPreferencesService();
		if (!prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			return null;
		}

		final WorkspaceJob op = new WorkspaceJob(SOURCE_ANALYSING) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;

				if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
					syntaxAnalyzersRunning.decrementAndGet();
					return Status.CANCEL_STATUS;
				}

				if (!LicenseValidator.check()) {
					syntaxAnalyzersRunning.decrementAndGet();
					return Status.CANCEL_STATUS;
				}

				if (monitor.isCanceled()) {
					syntaxAnalyzersRunning.decrementAndGet();
					return Status.CANCEL_STATUS;
				}

				final int priority = getThread().getPriority();
				try {
					getThread().setPriority(Thread.MIN_PRIORITY);

					long absoluteStart = System.nanoTime();

					IPreferencesService preferenceService = Platform.getPreferencesService();
					String compilerOption = preferenceService.getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.COMPILERMARKERSAFTERANALYZATION,
							PreferenceConstantValues.COMPILEROPTIONOUTDATE, null);
					if (PreferenceConstantValues.COMPILEROPTIONREMOVE.equals(compilerOption)) {
						ParserMarkerSupport.removeAllCompilerMarkers(project);
					} else if (PreferenceConstantValues.COMPILEROPTIONOUTDATE.equals(compilerOption)) {
						for (Iterator<IFile> iterator = EditorTracker.keyset().iterator(); iterator.hasNext();) {
							IFile file = iterator.next();
							if (file.getProject() == project) {
								ISemanticTITANEditor editor = EditorTracker.getEditor(file).get(0);
								MarkerHandler.deprecateMarkers(editor, ParserMarkerSupport.getAllCompilerMarkers(project));
							}
						}
					}

					syntacticAnalyzer.internalDoAnalyzeSyntactically2(monitor);

					boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
					if (reportDebugInformation) {
						//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream(); //only once called
						TITANDebugConsole.println("The whole analysis block took " + (System.nanoTime() - absoluteStart)
										* (1e-9) + " seconds to complete");
					}
				} catch (Exception e) {
					ErrorReporter.logExceptionStackTrace(e);
				} finally {
					syntaxAnalyzersRunning.decrementAndGet();
					getThread().setPriority(priority);
				}

				return result;
			}
		};
		op.setPriority(Job.LONG);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			op.setSystem(false);
			op.setUser(true);
		} else {
			op.setSystem(true);
			op.setUser(false);
		}

		op.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		op.setRule(getSchedulingRule());
		if (syntaxAnalyzersRunning.get() > 0) {
			if (lastSyntaxAnalyzes != null && lastSyntaxAnalyzes.getState() != Job.RUNNING) {
				lastSyntaxAnalyzes.cancel();
			}
		}
		op.schedule();

		lastSyntaxAnalyzes = op;
		syntaxAnalyzersRunning.incrementAndGet();

		return op;
	}

	/**
	 * Analyzes all of the files which are in the project.
	 * <ul>
	 * <li>the files possibly needed to analyze are collected first
	 * <li>those files, which are known to be up-to-date are filtered from
	 * this list
	 * <li>the files left in the list are analyzed in a new workspace job
	 * </ul>
	 * 
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob analyzeAll() {
		return analyzeAll(true);
	}

	/**
	 * Analyzes all of the files which are in the project.
	 * <ul>
	 * <li>the files possibly needed to analyze are collected first
	 * <li>those files, which are known to be up-to-date are filtered from
	 * this list
	 * <li>the files left in the list are analyzed in a new workspace job
	 * </ul>
	 * 
	 * @param allowQuickExit
	 *                if set to true and there is an analysis going on
	 *                already for the same project, the new analysis will
	 *                quit immediately.
	 * @return the WorkspaceJob in which the operation is running
	 * */
	public WorkspaceJob analyzeAll(final boolean allowQuickExit) {
		IPreferencesService prefs = Platform.getPreferencesService();
		if (!prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, true, null)) {
			return null;
		}

		WorkspaceJob analyzes = new WorkspaceJob(SOURCE_ANALYSING) {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;

				if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
					return Status.CANCEL_STATUS;
				}

				if (!LicenseValidator.check()) {
					return Status.CANCEL_STATUS;
				}

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

				final int priority = getThread().getPriority();
				try {
					getThread().setPriority(Thread.MIN_PRIORITY);

					long absoluteStart = System.nanoTime();

					IPreferencesService preferenceService = Platform.getPreferencesService();
					String compilerOption = preferenceService.getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.COMPILERMARKERSAFTERANALYZATION,
							PreferenceConstantValues.COMPILEROPTIONOUTDATE, null);
					if (PreferenceConstantValues.COMPILEROPTIONREMOVE.equals(compilerOption)) {
						ParserMarkerSupport.removeAllCompilerMarkers(project);
					} else if (PreferenceConstantValues.COMPILEROPTIONOUTDATE.equals(compilerOption)) {
						for (Iterator<IFile> iterator = EditorTracker.keyset().iterator(); iterator.hasNext();) {
							IFile file = iterator.next();
							if (file.getProject() == project) {
								ISemanticTITANEditor editor = EditorTracker.getEditor(file).get(0);
								MarkerHandler.deprecateMarkers(editor, ParserMarkerSupport.getAllCompilerMarkers(project));
							}
						}
					}

					result = internalDoAnalyzeWithReferences(monitor);

					boolean reportDebugInformation = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);
					if (reportDebugInformation) {
						//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream(); //used only once
						TITANDebugConsole.println("The whole analysis block took " + (System.nanoTime() - absoluteStart)
										* (1e-9) + " seconds to complete");
					}
				} catch (Exception e) {
					ErrorReporter.logExceptionStackTrace(e);
				} finally {
					getThread().setPriority(priority);
				}

				return result;
			}
		};
		analyzes.setPriority(Job.LONG);

		IPreferencesService preferenceService = Platform.getPreferencesService();
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			analyzes.setSystem(false);
			analyzes.setUser(true);
		} else {
			analyzes.setSystem(true);
			analyzes.setUser(false);
		}

		analyzes.setProperty(IProgressConstants.ICON_PROPERTY, ImageCache.getImageDescriptor("titan.gif"));
		final ISchedulingRule rule = getSchedulingRule();
		analyzes.setRule(rule);

		if (fullAnalyzersRunning.get() > 0) {
			if (lastFullAnalyzes != null && lastFullAnalyzes.getState() != Job.RUNNING) {
				lastFullAnalyzes.cancel();
			}
			lastFullAnalyzes = analyzes;
		}
		analyzes.schedule();
		fullAnalyzersRunning.incrementAndGet();

		final WorkspaceJob temp = analyzes;

		WorkspaceJob extensions = new WorkspaceJob("Executing Titan extensions") {
			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				final IProgressMonitor pm = (monitor == null) ? new NullProgressMonitor() : monitor;

				final int priortity = getThread().getPriority();
				try {
					getThread().setPriority(Thread.MIN_PRIORITY);
					pm.beginTask("Executing Titan extensions", 100);
					pm.subTask("Waiting for semantic analysis");
					try {
						temp.join();

					} catch (Exception e) {
						ErrorReporter.logExceptionStackTrace(e);
					}

					pm.subTask("Executing extensions");
					if (Status.OK_STATUS.equals(temp.getResult())) {
						ExtensionHandler.INSTANCE.executeContributors(new SubProgressMonitor(pm, 100), project);
					}
				} finally {
					pm.done();
					fullAnalyzersRunning.decrementAndGet();
					getThread().setPriority(priortity);
				}

				return Status.OK_STATUS;
			}
		};
		if (GeneralConstants.DEBUG
				&& preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION,
						true, null)) {
			extensions.setSystem(false);
			extensions.setUser(true);
		} else {
			extensions.setSystem(true);
			extensions.setUser(false);
		}
		extensions.setRule(rule);
		extensions.schedule();

		return extensions;
	}

	private ISchedulingRule getSchedulingRule() {
		Deque<IProject> temporalList = new LinkedList<IProject>();

		IProject[] tempProjects;
		IProject tempProject;

		List<IProject> projectTobeLocked = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
		for (Iterator<IProject> iterator = projectTobeLocked.iterator(); iterator.hasNext();) {
			IProject temp = iterator.next();
			if (GlobalParser.getProjectSourceParser(temp).getLastTimeChecked() != null) {
				iterator.remove();
			}
		}

		// analyze the project referencing the just now analyzed
		// projects in a bottom up order.
		temporalList.addLast(project);
		projectTobeLocked.remove(project);
		while (!temporalList.isEmpty()) {
			tempProject = temporalList.getFirst();
			temporalList.removeFirst();
			if (!projectTobeLocked.contains(tempProject)) {
				projectTobeLocked.add(tempProject);

				tempProjects = ProjectBasedBuilder.getProjectBasedBuilder(tempProject).getReferencingProjects();
				for (IProject tempProject2 : tempProjects) {
					if (!GlobalParser.getProjectSourceParser(tempProject2).analyzesRunning) {
						temporalList.addLast(tempProject2);
					}
				}
			}
		}

		// Collect those projects that might be needed to do the correct
		// analysis.
		List<IProject> additionalRequired = new ArrayList<IProject>();
		for (IProject project : projectTobeLocked) {
			List<IProject> temp = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
			for (IProject temp2 : temp) {
				// TODO this a quick hack, when we have time
				// check if the commented requirement is valid
				// or not.
				if (!projectTobeLocked.contains(temp2) && !additionalRequired.contains(temp2)) {
					additionalRequired.add(temp2);
				}
			}
		}

		projectTobeLocked.addAll(additionalRequired);

		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule combinedRule = null;
		for (IProject project : projectTobeLocked) {
			combinedRule = MultiRule.combine(ruleFactory.createRule(project), combinedRule);
		}

		return combinedRule;
	}

	/**
	 * Checks that all directly referenced projects are using the
	 * configuration required by the actual one, if set.
	 * 
	 * @param project
	 *                the actual project.
	 * @param markerType
	 *                the type of the marker to report the error with.
	 * 
	 * @return true if there were not error, false otherwise.
	 * */
	public static boolean checkConfigurationRequirements(final IProject project, final String markerType) {
		IProject[] referencedProjects;
		try {
			referencedProjects = project.getReferencedProjects();
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return false;
		}

		List<ProjectConfigurationsPropertyData.ConfigurationRequirement> requirements = ProjectConfigurationsPropertyData
				.getConfigurationRequirements(project);
		for (int i = 0, size = requirements.size(); i < size; i++) {
			ProjectConfigurationsPropertyData.ConfigurationRequirement temp = requirements.get(i);
			if (temp.getConfiguration() == null || "".equals(temp.getConfiguration())) {
				continue;
			}
			for (IProject referencedProject : referencedProjects) {
				String name = referencedProject.getName();
				if (name.equals(temp.getProjectName())) {
					String tempActiveConfiguration = ProjectFileHandler.getActiveConfigurationName(referencedProject);
					if (!temp.getConfiguration().equals(tempActiveConfiguration)) {
						Location location = new Location(project);
						location.reportExternalProblem("In order to build project `" + project.getName() + "' project `"
								+ name + "' must be using the `" + temp.getConfiguration()
								+ "' configuration, but right now it is using `" + tempActiveConfiguration + "'",
								IMarker.SEVERITY_ERROR, markerType);
						return false;
					}
					break;
				}
			}
		}

		return true;
	}

	/**
	 * This function behaves just like the {@link #analyzeAll(IFile)}
	 * function. With the only difference being that it does not have a
	 * scheduling rule and does not can not run in parallel with the calling
	 * party..
	 * <p>
	 * This function should only be called if no on-the-fly check was run
	 * previously, it is required to have one run, but the scheduling rules
	 * of the actual job does not allow it to run with its own scheduling
	 * rules. In such cases the external scheduling rules must provide the
	 * protection, that is usually provided by its own rules.
	 * */
	// FIXME this function is only temporary, it should be removed once its
	// functionality is available on any other way
	public void makefileCreatingAnalyzeAll() {
		fullAnalyzersRunning.incrementAndGet();
		ISchedulingRule rule = getSchedulingRule();
		Job.getJobManager().beginRule(rule, new NullProgressMonitor());

		try {
			internalDoAnalyzeWithReferences(new NullProgressMonitor());
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		} finally {
			Job.getJobManager().endRule(rule);
			fullAnalyzersRunning.decrementAndGet();
		}
	}
}
