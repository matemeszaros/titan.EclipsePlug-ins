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
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.titan.designer.AST.brokenpartsanalyzers.BrokenPartsChecker;
import org.eclipse.titan.designer.AST.brokenpartsanalyzers.BrokenPartsViaReferences;
import org.eclipse.titan.designer.AST.brokenpartsanalyzers.IBaseAnalyzer;
import org.eclipse.titan.designer.AST.brokenpartsanalyzers.SelectionAlgorithm;
import org.eclipse.titan.designer.AST.brokenpartsanalyzers.SelectionMethodBase;
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

	/** holds the list of modules which had the same module identifiers */
	private final Set<IFile> duplicationHolders = new HashSet<IFile>();

	public ProjectSourceSemanticAnalyzer(final IProject project, final ProjectSourceParser sourceParser) {
		this.project = project;
		this.sourceParser = sourceParser;

		moduleMap = new ConcurrentHashMap<String, Module>();
		outdatedModuleMap = new HashMap<String, Module>();
		semanticallyUptodateModules = new HashSet<String>();
	}

	/**
	 * Checks whether the internal data belonging to the provided file is
	 * semantically out-dated.
	 * 
	 * @param file
	 *            the file to check.
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
	 *            the name of the module to return.
	 * @param uptodateOnly
	 *            allow finding only the up-to-date modules.
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
	 *            the name of the module to be checked.
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
	 *            the old name of the module.
	 * @param newName
	 *            the new name of the module.
	 * @param file
	 *            the file in which the change happened.
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

		ProjectBasedBuilder.setForcedMakefileRebuild(project);
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
	 *            the file which seems to have changed
	 * @param useOnTheFlyParsing
	 *            true if on-the-fly parsing is enabled.
	 * */
	public void reportOutdating(final IFile outdatedFile, final boolean useOnTheFlyParsing) {
		if (!useOnTheFlyParsing) {
			return;
		}

		outdateDuplicationHolders();

		String moduleName = sourceParser.containedModule(outdatedFile);
		if (moduleName == null) {
			// The module was just added
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
	}

	/**
	 * Reports that the semantic meaning of the provided file might have
	 * changed and so it's stored information became out of date.
	 * <p>
	 * Stores that this file is semantically out of date for later
	 * <p>
	 * 
	 * @param outdatedFile
	 *            the file which seems to have changed
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
	}

	/**
	 * Force the next semantic analysis to reanalyze everything.
	 * */
	void clearSemanticInformation() {
		synchronized (semanticallyUptodateModules) {
			semanticallyUptodateModules.clear();
		}
		duplicationHolders.clear();
	}

	/**
	 * Removes data related to modules, that were deleted or moved.
	 * 
	 * @param file
	 *            the file that was changed.
	 * @param moduleName
	 *            the name of the module in that file.
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
	}

	/**
	 * Removes a module from the set of semantically analyzed modules.
	 * 
	 * @param moduleName
	 *            the name of the module to be removed.
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
	 *            the module to be added.
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
	 * Does the semantic checking of the modules located in multiple projects.
	 * It is important to call this function after the
	 * {@link #internalDoAnalyzeSyntactically(IProgressMonitor, CompilationTimeStamp)}
	 * function was executed on all involved projects, as the out-dated markers will be cleared here.
	 * 
	 * @param tobeSemanticallyAnalyzed the list of projects to be analyzed.
	 * @param monitor
	 *                the progress monitor to provide feedback to the user
	 *                about the progress.
	 * @param compilationCounter
	 *            the timestamp of the actual build cycle.
	 * 
	 * @return the status of the operation when it finished.
	 * */
	static IStatus analyzeMultipleProjectsSemantically(final List<IProject> tobeSemanticallyAnalyzed, final IProgressMonitor monitor, final CompilationTimeStamp compilationCounter) {
		for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
			if (!tobeSemanticallyAnalyzed.get(i).isAccessible() || !TITANNature.hasTITANNature(tobeSemanticallyAnalyzed.get(i))) {
				return Status.CANCEL_STATUS;
			}
		}
		
		final long semanticCheckStart = System.nanoTime();
		final IPreferencesService preferenceService = Platform.getPreferencesService();

		for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
			ProjectSourceSemanticAnalyzer semanticAnalyzer = GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).getSemanticAnalyzer();
			synchronized (semanticAnalyzer.outdatedModuleMap) {
				semanticAnalyzer.outdatedModuleMap.clear();
			}
		}
		
		// Semantic checking starts here
		monitor.beginTask("On-the-fly semantic checking of everything ", 1);
		monitor.subTask("Checking the importations of the modules");
		
		try{
			final String option = preferenceService.getString(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, GeneralConstants.ERROR, null);
			for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
				// report the unsupported constructs in the project
				ProjectSourceSyntacticAnalyzer syntacticAnalyzer = GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).getSyntacticAnalyzer();
				for (IFile file : syntacticAnalyzer.unsupportedConstructMap.keySet()) {
					List<TITANMarker> markers = syntacticAnalyzer.unsupportedConstructMap.get(file);
					if (markers != null && file.isAccessible()) {
						for (TITANMarker marker : markers) {
							Location location = new Location(file, marker.getLine(), marker.getOffset(), marker.getEndOffset());
							location.reportConfigurableSemanticProblem(option, marker.getMessage());
						}
					}
				}
			}
			
			// clean the instantiated parameterized assignments,
			// from their instances
			Ass_pard.resetAllInstanceCounters();

			// collect all modules and semantically checked modules to work on.
			final List<Module> allModules = new ArrayList<Module>();
			final List<String> semanticallyChecked = new ArrayList<String>();
			for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
				ProjectSourceSemanticAnalyzer semanticAnalyzer = GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).getSemanticAnalyzer();
				for (Module moduleToCheck : semanticAnalyzer.moduleMap.values()) {
					if (moduleToCheck != null) {
						allModules.add(moduleToCheck);
					}
				}
				synchronized (semanticAnalyzer.semanticallyUptodateModules) {
					for (String modulename : semanticAnalyzer.semanticallyUptodateModules) {
						if (semanticAnalyzer.moduleMap.containsKey(modulename)) {
							Module module = semanticAnalyzer.moduleMap.get(modulename);
							if (module != null) {
								semanticallyChecked.add(module.getName());
							}
						}
					}
				}
			}

			int nofModulesTobeChecked = 0;
			if(allModules.size() > semanticallyChecked.size()) {
				// check and build the import hierarchy of the modules
				ModuleImportationChain referenceChain = new ModuleImportationChain(CIRCULARIMPORTCHAIN, false);
				for(Module module : allModules) {
					module.checkImports(compilationCounter, referenceChain, new ArrayList<Module>());
					referenceChain.clear();
				}

				monitor.subTask("Calculating the list of modules to be checked");

				IBaseAnalyzer selectionMethod = new BrokenPartsViaReferences(SelectionAlgorithm.BROKENREFERENCESINVERTED, compilationCounter);
				SelectionMethodBase selectionMethodBase = (SelectionMethodBase)selectionMethod;
				selectionMethodBase.setModules(allModules, semanticallyChecked);
				selectionMethod.execute();

				selectionMethodBase.setSkippedFromSemanticChecking();

				BrokenPartsChecker brokenPartsChecker = new BrokenPartsChecker(monitor, compilationCounter, selectionMethodBase);
				brokenPartsChecker.doChecking();
				
				// re-enable the markers on the skipped modules.
				for (Module module2 : selectionMethodBase.getModulesToSkip()) {
					MarkerHandler.reEnableAllMarkers((IFile) module2.getLocation().getFile());
				}

				nofModulesTobeChecked = selectionMethodBase.getModulesToCheck().size();
			} else {
				//re-enable all markers
				for (Module module2 : allModules) {
					MarkerHandler.reEnableAllMarkers((IFile) module2.getLocation().getFile());
				}
			}

			if (preferenceService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.DISPLAYDEBUGINFORMATION, true, null)) {
				MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
				TITANDebugConsole.println("  ** Had to start checking at " + nofModulesTobeChecked + " modules. ", stream);
				TITANDebugConsole.println("  **On-the-fly semantic checking of projects (" + allModules.size() + " modules) took " + (System.nanoTime() - semanticCheckStart) * (1e-9) + " seconds", stream);
			}
			monitor.subTask("Cleanup operations");
			
			for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
				ProjectSourceSemanticAnalyzer semanticAnalyzer = GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).getSemanticAnalyzer();
				synchronized (semanticAnalyzer.semanticallyUptodateModules) {
					semanticAnalyzer.semanticallyUptodateModules.clear();
					semanticAnalyzer.semanticallyUptodateModules.addAll(semanticAnalyzer.moduleMap.keySet());
				}
			}
		} catch (Exception e) {
			// This catch is extremely important, as it is supposed
			// to protect the project parser, from whatever might go
			// wrong inside the analysis.
			ErrorReporter.logExceptionStackTrace(e);
		}
		monitor.done();

		for (int i = 0; i < tobeSemanticallyAnalyzed.size(); i++) {
			GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).setLastTimeChecked(compilationCounter);
			
			ProjectStructureDataCollector collector = GlobalProjectStructureTracker.getDataCollector(tobeSemanticallyAnalyzed.get(i));
			for (Module module : GlobalParser.getProjectSourceParser(tobeSemanticallyAnalyzed.get(i)).getSemanticAnalyzer().moduleMap.values()) {
				collector.addKnownModule(module.getIdentifier());
				module.extractStructuralInformation(collector);
			}
			
			MarkerHandler.removeAllOnTheFlyMarkedMarkers(tobeSemanticallyAnalyzed.get(i));
		}
		
		return Status.OK_STATUS;
	}
}