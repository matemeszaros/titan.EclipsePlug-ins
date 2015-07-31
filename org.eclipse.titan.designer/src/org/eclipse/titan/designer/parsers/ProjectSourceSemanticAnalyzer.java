/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportationChain;
import org.eclipse.titan.designer.AST.ASN1.Ass_pard;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Helper class to separate the responsibility of the source parser into smaller
 * parts. This class is responsible for handling the semantic checking of the
 * source code of the projects
 * 
 * @author Kristof Szabados
 * */
public class ProjectSourceSemanticAnalyzer {
	private static final String DUPLICATEMODULE = "Modules must be unique, but `{0}'' was declared multiple times";
	public static final String CIRCULARIMPORTCHAIN = "Circular import chain is not recommended: {0}";

	private final IProject project;
	private final ProjectSourceParser sourceParser;

	// module name, module
	private Map<String, Module> moduleMap;
	// module name, module
	private Map<String, Module> outdatedModuleMap;

	/** The names of the modules, which were checked the last time. */
	private Set<String> semanticallyUptodateModules;

	/**
	 * The list of modules that should be re-analyzed if incremental
	 * semantic processing is enabled.
	 */
	private Set<String> moduleToBeReanalysed = new HashSet<String>();

	/**
	 * tells whether a full analysis is required even if incremental
	 * processing is enabled.
	 */
	private boolean fullAnalysisNeeded = true;

	/** holds the list of modules which had the same module identifiers */
	private final Set<IFile> duplicationHolders = new HashSet<IFile>();

	public ProjectSourceSemanticAnalyzer(final IProject project, final ProjectSourceParser sourceParser) {
		this.project = project;
		this.sourceParser = sourceParser;

		moduleMap = new ConcurrentHashMap<String, Module>();
		outdatedModuleMap = new HashMap<String, Module>();
		semanticallyUptodateModules = new HashSet<String>();
	}

	boolean getFullAnalysisNeeded() {
		return fullAnalysisNeeded;
	}

	/** Sets whether full analysis is needed or not. */
	public void setFullAnalysisNeeded() {
		fullAnalysisNeeded = true;
	}

	/**
	 * Adds a list of modules that must be analyzed in case of incremental
	 * analysis.
	 * 
	 * @param modules
	 *                the list of modules to be added.
	 * */
	public void addModulesToBeReanalyzed(final Set<String> modules) {
		moduleToBeReanalysed.addAll(modules);
	}

	/**
	 * Checks whether the internal data belonging to the provided file is
	 * semantically out-dated.
	 * 
	 * @param file
	 *                the file to check.
	 * 
	 * @return true if the data was reported to be out-dated since the last
	 *         analysis.
	 * */
	public boolean isOutdated(final IFile file) {
		String moduleName = sourceParser.containedModule(file);
		if (moduleName == null) {
			return true;
		}

		synchronized (semanticallyUptodateModules) {
			return !semanticallyUptodateModules.contains(moduleName);
		}
	}

	/**
	 * Returns the module with the provided name, or null.
	 * 
	 * @param name
	 *                the name of the module to return.
	 * @param uptodateOnly
	 *                allow finding only the up-to-date modules.
	 * 
	 * @return the module having the provided name
	 * */
	Module internalGetModuleByName(final String name, final boolean uptodateOnly) {
		if (moduleMap.containsKey(name)) {
			return moduleMap.get(name);
		}

		if (!uptodateOnly && outdatedModuleMap.containsKey(name)) {
			return outdatedModuleMap.get(name);
		}

		return null;
	}

	/**
	 * Returns the actually known module's names.
	 * 
	 * @return a set of the module names known in this project or in the
	 *         ones referenced.
	 * */
	Set<String> internalGetKnownModuleNames() {
		Set<String> temp = new HashSet<String>();
		temp.addAll(moduleMap.keySet());
		return temp;
	}

	Collection<Module> internalGetModules() {
		return moduleMap.values();
	}

	/**
	 * Returns weather a module with the provided name is set to be
	 * semantically checked right now.
	 * <p>
	 * Checks the only actual project.
	 * 
	 * @param moduleName
	 *                the name of the module to be checked.
	 * 
	 * @return true if the module is checked, false otherwise.
	 * */
	boolean semanticallyUptodateContains(final String moduleName) {
		return semanticallyUptodateModules.contains(moduleName);
	}

	/**
	 * Handles the renaming of a module.
	 * 
	 * @param oldname
	 *                the old name of the module.
	 * @param newName
	 *                the new name of the module.
	 * @param file
	 *                the file in which the change happened.
	 * */
	void renameModule(final String oldname, final String newName, final IFile file) {
		if (moduleMap.containsKey(oldname)) {
			Module module = moduleMap.remove(oldname);
			moduleMap.put(newName, module);
		}
		synchronized (outdatedModuleMap) {
			if (outdatedModuleMap.containsKey(oldname)) {
				Module module = outdatedModuleMap.remove(oldname);
				outdatedModuleMap.put(newName, module);
			}
		}

		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.remove(oldname);
		}

		ProjectBasedBuilder.setForcedMakefileRebuild(project, true);
	}

	/**
	 * Reports that the provided file has changed and so it's stored
	 * information became out of date.
	 * <p>
	 * Stores that this file is out of date for later
	 * <p>
	 * 
	 * <p>
	 * Files which are excluded from the build should not be reported.
	 * </p>
	 * 
	 * @param outdatedFile
	 *                the file which seems to have changed
	 * @param useOnTheFlyParsing
	 *                true if on-the-fly parsing is enabled.
	 * */
	public void reportOutdating(final IFile outdatedFile, final boolean useOnTheFlyParsing) {
		if (!useOnTheFlyParsing) {
			return;
		}

		outdateDuplicationHolders();

		String moduleName = sourceParser.containedModule(outdatedFile);
		if (moduleName == null) {
			// The module was just added
			fullAnalysisNeeded = true;
			return;
		}

		Module module = moduleMap.get(moduleName);
		if (module == null) {
			return;
		}

		IResource moduleFile = module.getIdentifier().getLocation().getFile();
		if (!outdatedFile.equals(moduleFile)) {
			return;
		}
		moduleMap.remove(moduleName);

		synchronized (outdatedModuleMap) {
			outdatedModuleMap.put(moduleName, module);
			module.setSkippedFromSemanticChecking(false);
		}

		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.remove(moduleName);
		}
		fullAnalysisNeeded = true;
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
	 * */
	public void reportSemanticOutdating(final IFile outdatedFile) {
		outdateDuplicationHolders();

		String moduleName = sourceParser.containedModule(outdatedFile);
		if (moduleName == null) {
			return;
		}

		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.remove(moduleName);
		}
		fullAnalysisNeeded = true;
	}

	/**
	 * Force the next semantic analysis to reanalyze everything.
	 * */
	void clearSemanticInformation() {
		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.clear();
		}
		duplicationHolders.clear();
		fullAnalysisNeeded = true;
	}

	/**
	 * Removes data related to modules, that were deleted or moved.
	 * 
	 * @param file
	 *                the file that was changed.
	 * @param moduleName
	 *                the name of the module in that file.
	 **/
	void removedReferencestoRemovedFiles(final IFile file, final String moduleName) {
		outdateDuplicationHolders();

		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.remove(moduleName);
		}

		moduleMap.remove(moduleName);

		synchronized (outdatedModuleMap) {
			outdatedModuleMap.remove(moduleName);
		}
		fullAnalysisNeeded = true;
	}

	/**
	 * Removes a module from the set of semantically analyzed modules.
	 * 
	 * @param moduleName
	 *                the name of the module to be removed.
	 * */
	void removeModule(final String moduleName) {
		synchronized (outdatedModuleMap) {
			outdatedModuleMap.remove(moduleName);
		}

		moduleMap.remove(moduleName);
	}

	/**
	 * Outdate all of the modules which define the same module identifier.
	 * */
	private void outdateDuplicationHolders() {
		if (duplicationHolders.isEmpty()) {
			return;
		}

		Set<IFile> temp = new HashSet<IFile>();
		temp.addAll(duplicationHolders);
		duplicationHolders.clear();

		for (IFile file : temp) {
			sourceParser.getSyntacticAnalyzer().reportOutdating(file);
		}
	}

	/**
	 * Adds a module to the set of semantically analyzed modules.
	 * 
	 * @param module
	 *                the module to be added.
	 * @return true if it was successfully added, false otherwise.
	 * */
	public boolean addModule(final Module module) {
		Module temp = sourceParser.getModuleByName(module.getName(), true);
		if (temp != null) {
			Identifier identifier = temp.getIdentifier();
			Location location = null;
			if (identifier == null) {
				location = temp.getLocation();
			} else {
				location = identifier.getLocation();
			}

			identifier = module.getIdentifier();
			Location location2 = null;
			if (identifier == null) {
				location2 = module.getLocation();
			} else {
				location2 = identifier.getLocation();
			}

			if (location != null && location2 != null && !location.getFile().equals(location2.getFile())) {
				duplicationHolders.add((IFile) location.getFile());
				duplicationHolders.add((IFile) location2.getFile());
				location.reportSingularSemanticError(MessageFormat.format(DUPLICATEMODULE, module.getIdentifier().getDisplayName()));
				location2.reportSemanticError(MessageFormat.format(DUPLICATEMODULE, module.getIdentifier().getDisplayName()));
				return false;
			}
		}

		synchronized (outdatedModuleMap) {
			if (outdatedModuleMap.containsKey(module.getName())) {
				Module temp2 = outdatedModuleMap.remove(module.getName());
				temp2.setSkippedFromSemanticChecking(false);
			}
		}

		moduleMap.put(module.getName(), module);

		return true;
	}

	/**
	 * Internal function.
	 * <p>
	 * Does the semantic checking of the modules parsed in this project. It
	 * is important to call this function after the
	 * {@link #internalDoAnalyzeSyntactically(IProgressMonitor, CompilationTimeStamp)}
	 * function, as the out-dated markers will be cleared here.
	 * 
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 * @param compilationCounter
	 *                the timestamp of the actual build cycle.
	 * @param modulesSkippedGlobally
	 *                the modules skipped in any earlier projects.
	 * 
	 * @return the status of the operation when it finished.
	 * */
	synchronized IStatus internalDoAnalyzeSemantically(final IProgressMonitor monitor, final CompilationTimeStamp compilationCounter,
			final List<Module> modulesSkippedGlobally) {
		if (!project.isAccessible() || !TITANNature.hasTITANNature(project)) {
			return Status.CANCEL_STATUS;
		}

		analyzeSemantically(monitor, compilationCounter, modulesSkippedGlobally);

		MarkerHandler.removeAllOnTheFlyMarkedMarkers(project);

		return Status.OK_STATUS;
	}

	/**
	 * Internal function.
	 * <p>
	 * Does the incremental semantic checking of the modules parsed in this
	 * project. It is important to call this function after the
	 * {@link #internalDoAnalyzeSyntactically(IProgressMonitor, CompilationTimeStamp)}
	 * function, as the out-dated markers will be cleared here.
	 * 
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 * @param compilationCounter
	 *                the timestamp of the actual build cycle.
	 * @param modulesSkippedGlobally
	 *                the modules skipped in any earlier projects.
	 * 
	 * @return the status of the operation when it finished.
	 * */
	synchronized IStatus analyzeSemanticallyIncrementally(final IProgressMonitor monitor, final CompilationTimeStamp compilationCounter) {
		long semanticCheckStart = System.nanoTime();

		// remove the out-dated modules, which could not be refreshed
		// (they are most probably excluded from analysis)
		synchronized (outdatedModuleMap) {
			outdatedModuleMap.clear();
		}

		// Semantic checking starts here
		monitor.beginTask("On-the-fly semantic checking of project: " + project.getName(), 5);
		monitor.subTask("Checking the importations of the modules");

		try {
			// report the unsupported constructs in the project,
			// should be empty
			if (!sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.isEmpty()) {
				String option = Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
						PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, GeneralConstants.ERROR, null);
				for (IFile file : sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.keySet()) {
					List<TITANMarker> markers = sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.get(file);
					if (markers != null && file.isAccessible()) {
						for (TITANMarker marker : markers) {
							Location location = new Location(file, marker.getLine(), marker.getOffset(),
									marker.getEndOffset());
							location.reportConfigurableSemanticProblem(option, marker.getMessage());
						}
					}
				}
			}

			// clean the instantiated parameterized assignments,
			// from their instances
			Ass_pard.resetAllInstanceCounters();

			monitor.worked(1);
			monitor.subTask("Calculating the list of modules to be checked");

			List<Module> modulesToCheck = new ArrayList<Module>();
			for (String name : moduleToBeReanalysed) {
				Module module = moduleMap.get(name);
				if (module != null) {
					modulesToCheck.add(module);
				}
			}

			List<Module> modulesToSkip = new ArrayList<Module>();
			for (String modulename : moduleMap.keySet()) {
				if (!moduleToBeReanalysed.contains(modulename)) {
					Module module = moduleMap.get(modulename);
					if (module != null) {
						modulesToSkip.add(module);
					}
				}
			}

			for (Module module2 : modulesToCheck) {
				module2.setSkippedFromSemanticChecking(false);
			}
			for (Module module2 : modulesToSkip) {
				module2.setSkippedFromSemanticChecking(true);
			}

			monitor.worked(1);
			monitor.subTask("Semantic checking");
			final SubProgressMonitor semanticMonitor = new SubProgressMonitor(monitor, 1);
			semanticMonitor.beginTask("Semantic check", modulesToCheck.size());

			// process the modules one-by-one
			for (final Module module2 : modulesToCheck) {
				MarkerHandler.markAllSemanticMarkersForRemoval((IFile) module2.getLocation().getFile());
				semanticMonitor.subTask("Semantically checking module: " + module2.getName());
				module2.check(compilationCounter);
				semanticMonitor.worked(1);
			}

			semanticMonitor.done();
			monitor.subTask("Doing post semantic checks");

			for (Module module2 : modulesToCheck) {
				module2.postCheck();
			}

			monitor.worked(1);
			monitor.subTask("Cleanup operations");

			synchronized (semanticallyUptodateModules) {
				semanticallyUptodateModules.clear();
				semanticallyUptodateModules.addAll(moduleMap.keySet());
			}

			moduleToBeReanalysed.clear();

			for (Module module2 : modulesToCheck) {
				MarkerHandler.removeAllOnTheFlyMarkedMarkers(module2.getLocation().getFile());
			}

			IPreferencesService preferenceService = Platform.getPreferencesService();
			if (preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true,
					null)) {
				MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
				TITANDebugConsole.println("  **Incremental on-the-fly semantic checking of project " + project.getName() + " ("
						+ semanticallyUptodateModules.size() + " modules) took "
						+ (System.nanoTime() - semanticCheckStart) * (1e-9) + " seconds",stream);
				TITANDebugConsole.println("  **Checked " + modulesToCheck.size() + " modules", stream);
			}
			monitor.worked(1);
		} catch (Exception e) {
			// This catch is extremely important, as it is supposed
			// to protect the project parser, from whatever might go
			// wrong inside the analysis.
			ErrorReporter.logExceptionStackTrace(e);
		}

		fullAnalysisNeeded = false;
		sourceParser.setLastTimeChecked(compilationCounter);
		monitor.done();

		ProjectStructureDataCollector collector = GlobalProjectStructureTracker.getDataCollector(project);
		for (Module module : moduleMap.values()) {
			collector.addKnownModule(module.getIdentifier());
			module.extractStructuralInformation(collector);
		}

		return Status.OK_STATUS;
	}

	/**
	 * Internal function. Does the actual semantic checking of the modules.
	 * 
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 * @param compilationCounter
	 *                the timestamp of the actual build cycle.
	 * @param modulesSkippedGlobally
	 *                the modules skipped in any earlier projects.
	 * 
	 * */
	private void analyzeSemantically(final IProgressMonitor monitor, final CompilationTimeStamp compilationCounter,
			final List<Module> modulesSkippedGlobally) {
		long semanticCheckStart = System.nanoTime();
		IPreferencesService preferenceService = Platform.getPreferencesService();

		// remove the out-dated modules, which could not be refreshed
		// (they are most probably excluded from analysis)
		synchronized (outdatedModuleMap) {
			outdatedModuleMap.clear();
		}

		// Semantic checking starts here
		monitor.beginTask("On-the-fly semantic checking of project: " + project.getName(), 1);
		monitor.subTask("Checking the importations of the modules");

		try {
			// report the unsupported constructs in the project
			if (!sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.isEmpty()) {
				String option = preferenceService.getString(ProductConstants.PRODUCT_ID_DESIGNER,
						PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, GeneralConstants.ERROR, null);
				for (IFile file : sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.keySet()) {
					List<TITANMarker> markers = sourceParser.getSyntacticAnalyzer().unsupportedConstructMap.get(file);
					if (markers != null && file.isAccessible()) {
						for (TITANMarker marker : markers) {
							Location location = new Location(file, marker.getLine(), marker.getOffset(),
									marker.getEndOffset());
							location.reportConfigurableSemanticProblem(option, marker.getMessage());
						}
					}
				}
			}

			// clean the instantiated parameterized assignments,
			// from their instances
			Ass_pard.resetAllInstanceCounters();

			Module module;

			// check and build the import hierarchy of the modules
			ModuleImportationChain referenceChain = new ModuleImportationChain(CIRCULARIMPORTCHAIN, false);
			for (String modulename : moduleMap.keySet()) {
				module = moduleMap.get(modulename);
				if (module != null) {
					module.checkImports(compilationCounter, referenceChain, new ArrayList<Module>());
					referenceChain.clear();
				}
			}

			monitor.subTask("Calculating the list of modules to be checked");

			// To know the correct elements
			List<String> semanticallyChecked = new ArrayList<String>();
			synchronized (semanticallyUptodateModules) {
				for (String modulename : semanticallyUptodateModules) {
					if (moduleMap.containsKey(modulename)) {
						module = moduleMap.get(modulename);
						if (module != null) {
							semanticallyChecked.add(module.getName());
						}
					}
				}
			}

			List<Module> modulesToCheck = new ArrayList<Module>();
			for (Module moduleToCheck : moduleMap.values()) {
				if (moduleToCheck != null) {
					modulesToCheck.add(moduleToCheck);
				}
			}

			List<Module> modulesToSkip = new ArrayList<Module>();
			boolean foundSkippable = true;
			while (foundSkippable) {
				foundSkippable = calculateModulesToSkip(modulesToCheck, modulesToSkip, modulesSkippedGlobally);
			}

			if (preferenceService
					.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEINCREMENTALPARSING, false, null)) {
				// the modules that can be reached directly must
				// be checked as the used state of the
				// definitions might have changed
				List<Module> modulesToCheck2 = new ArrayList<Module>();
				for (Module module2 : modulesToCheck) {
					if (module2.getLastCompilationTimeStamp() != null && semanticallyChecked.contains(module2.getName())) {
						List<Module> importedModules = module2.getImportedModules();
						if (importedModules != null && !importedModules.isEmpty()) {
							for (Module module3 : importedModules) {
								if (module3 != null && !modulesToCheck.contains(module3)
										&& !modulesToCheck2.contains(module3)) {
									modulesToCheck2.add(module3);
								}
							}
						}
					}
				}

				modulesToCheck.addAll(modulesToCheck2);
				modulesToSkip.removeAll(modulesToCheck2);
			}

			for (Module module2 : modulesToSkip) {
				module2.setSkippedFromSemanticChecking(true);
			}
			for (Module module2 : modulesSkippedGlobally) {
				module2.setSkippedFromSemanticChecking(true);
			}
			for (Module module2 : modulesToCheck) {
				module2.setSkippedFromSemanticChecking(false);
			}

			monitor.subTask("Semantic checking");
			final SubProgressMonitor semanticMonitor = new SubProgressMonitor(monitor, 1);
			semanticMonitor.beginTask("Semantic check", modulesToCheck.size());

			// process the modules one-by-one
			for (final Module module2 : modulesToCheck) {
				semanticMonitor.subTask("Semantically checking module: " + module2.getName());
				module2.check(compilationCounter);
				semanticMonitor.worked(1);
			}

			semanticMonitor.done();
			monitor.subTask("Doing post semantic checks");

			for (Module module2 : modulesToCheck) {
				module2.postCheck();
			}

			monitor.subTask("Cleanup operations");

			synchronized (semanticallyUptodateModules) {
				semanticallyUptodateModules.clear();
				semanticallyUptodateModules.addAll(moduleMap.keySet());
			}

			// re-enable the markers on the skipped modules.
			for (Module module2 : modulesToSkip) {
				MarkerHandler.reEnableAllMarkers((IFile) module2.getLocation().getFile());
			}

			modulesSkippedGlobally.addAll(modulesToSkip);

			moduleToBeReanalysed.clear();

			if (preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true,
					null)) {
				MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
				TITANDebugConsole.println("  ** Has to start checking at " + modulesToCheck.size() + " modules. ", stream);
				TITANDebugConsole.println("  **On-the-fly semantic checking of project " + project.getName() + " ("
								+ semanticallyUptodateModules.size() + " modules) took "
								+ (System.nanoTime() - semanticCheckStart) * (1e-9) + " seconds", stream);
				TITANDebugConsole.println("  **Checked " + modulesToCheck.size() + " modules",stream);
			}
		} catch (Exception e) {
			// This catch is extremely important, as it is supposed
			// to protect the project parser, from whatever might go
			// wrong inside the analysis.
			ErrorReporter.logExceptionStackTrace(e);
		}

		fullAnalysisNeeded = false;
		sourceParser.setLastTimeChecked(compilationCounter);
		monitor.done();

		ProjectStructureDataCollector collector = GlobalProjectStructureTracker.getDataCollector(project);
		for (Module module : moduleMap.values()) {
			collector.addKnownModule(module.getIdentifier());
			module.extractStructuralInformation(collector);
		}
	}

	/**
	 * Calculates the list of modules that can be skipped when analyzing
	 * this project semantically.
	 * <p>
	 * This function might not be able to detect all modules that can be
	 * skipped at once. It should be run as long as it reports that it could
	 * remove new modules from the list of modules to be checked.
	 * 
	 * @param modulesToCheck
	 *                the list of modules to be check. Initially all
	 *                modules, but the function will remove those that can
	 *                be skipped.
	 * @param modulesToSkip
	 *                the list of modules that can be skipped, to be
	 *                generated by the function.
	 * @param modulesSkippedGlobally
	 *                the list of modules that were already found to not
	 *                need to be checked in this semantic checking cycle, by
	 *                an other project.
	 * 
	 * @return true if there were modules found that can be skipped from
	 *         analyzing, false otherwise.
	 * */
	private boolean calculateModulesToSkip(final List<Module> modulesToCheck, final List<Module> modulesToSkip,
			final List<Module> modulesSkippedGlobally) {
		if (modulesToCheck.isEmpty()) {
			return false;
		}

		IPreferencesService preferenceService = Platform.getPreferencesService();
		boolean displayDebugInfo = preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null);

		boolean foundSkippable = false;
		// proven to need checking
		List<Module> failed = new ArrayList<Module>();
		List<Module> modulesToCheckCopy = new ArrayList<Module>();
		modulesToCheckCopy.addAll(modulesToCheck);
		MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		
		for (Iterator<Module> iterator = modulesToCheckCopy.iterator(); iterator.hasNext();) {
			Module module = iterator.next();
			if (failed.contains(module) || modulesToSkip.contains(module) || modulesSkippedGlobally.contains(module)) {
				continue;
			}

			if (module == null || module.getLastCompilationTimeStamp() == null || !sourceParser.isSemanticallyChecked(module)) {
				if (displayDebugInfo && module != null) {
					TITANDebugConsole.println("  ** Module " + module.getName()
									+ " can not be skipped as it was not yet analyzed.",stream);
				}
				failed.add(module);
				continue;
			}

			/**
			 * The modules that are reachable from the starting one.
			 */
			List<Module> reachableModules = new ArrayList<Module>();
			reachableModules.add(module);

			boolean valid = true;
			for (int i = 0; i < reachableModules.size() && valid; i++) {
				Module module2 = reachableModules.get(i);
				if (module2 == null) {
					valid = false;
					TITANDebugConsole.println("  ** Module " + module.getName()
									+ " can not be skipped as it reaches a module that could not be parsed.", stream);
					continue;
				}
				if (failed.contains(module2)) {
					valid = false;
					continue;
				}
				if (module2.getLastCompilationTimeStamp() != null && sourceParser.isSemanticallyChecked(module2)) {
					if (module2.hasUnhandledImportChanges()) {
						valid = false;
						failed.add(module2);
						if (displayDebugInfo) {
							TITANDebugConsole.println("  ** Module " + module2.getName()
											+ " can not be skipped as it has unhandled import changes.", stream);
							if (module != module2) {
								TITANDebugConsole.println("  ** Module " + module.getName()
												+ " can not be skipped as it depends on "
												+ module2.getName()
												+ " which has unhandled import changes.",stream);
							}
						}
						continue;
					}
					List<Module> importedModules = module2.getImportedModules();
					if (!importedModules.isEmpty()) {
						boolean allElements = true;
						Module module3;
						for (int j = 0; j < importedModules.size() && allElements; j++) {
							module3 = importedModules.get(j);
							if (module3 == null) {
								// erroneous
								// import
								allElements = false;
								if (displayDebugInfo) {
									if (module2 instanceof TTCN3Module) {
										final List<ImportModule> impModules = ((TTCN3Module) module2)
												.getImports();
										if (j < impModules.size()) {
											//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
											TITANDebugConsole.println("  ** Module " + module.getName()
													+ " can not be skipped as it imports `"
													+ impModules.get(j).getName()
													+ "' that could not be parsed.", stream);
										}
									}
									//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
									TITANDebugConsole.println("  ** Module "
											+ module.getName()
											+ " can not be skipped as it imports a module that could not be parsed.",stream);
								}
							} else if (failed.contains(module3)) {
								allElements = false;
								if (displayDebugInfo) {
									//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
									TITANDebugConsole.println("  ** Module " + module.getName()
													+ " can not be skipped as it depends on "
													+ module3.getName()
													+ " which needs to be checked.",stream);
								}
							} else if (modulesToSkip.contains(module3) || modulesSkippedGlobally.contains(module3)) {
								// already decided, that it can be skipped
							} else if (!sourceParser.isSemanticallyChecked(module3)) {
								// imports a module that was not checked, so this must also be re-checked
								allElements = false;
								if (displayDebugInfo) {
									//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
									TITANDebugConsole.println("  ** Module " + module.getName()
											+ " can not be skipped as it depends on " + module3.getName()
											+ " which was not yet analyzed.", stream);
								}
							} else {
								if (!reachableModules.contains(module3)) {
									reachableModules.add(module3);
								}
							}
						}

						if (!allElements) {
							valid = false;
						}
					}
				} else {
					// The module was not yet checked.
					valid = false;
					//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
					TITANDebugConsole.println("  ** Module " + module.getName() + " can not be skipped as it depends on "
							+ module2.getName() + " which was not yet checked.",stream);
				}
			}

			if (valid) {
				modulesToSkip.addAll(reachableModules);
				modulesToCheck.removeAll(reachableModules);
				foundSkippable = true;
			} else {
				failed.add(module);
			}
		}

		if (displayDebugInfo) {
			//MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
			TITANDebugConsole.println("  ** Found " + modulesToCheck.size() + " modules that needs to be checked and " + modulesToSkip.size()
					+ " modules to skip.",stream);
		}

		return foundSkippable;
	}
}
