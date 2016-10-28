/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Helper class to check broken parts.
 *
 * @author Peter Olah
 * @author Jeno Attila Balasko
 */
public final class BrokenPartsChecker {

	private final SubMonitor progress;

	private final IProgressMonitor monitor;

	private final CompilationTimeStamp compilationCounter;

	private final SelectionMethodBase selectionMethod;

	public BrokenPartsChecker(final SubMonitor monitor, final CompilationTimeStamp compilationCounter, final SelectionMethodBase selectionMethod) {
		this.compilationCounter = compilationCounter;
		this.selectionMethod = selectionMethod;
		this.monitor = monitor;

		progress = SubMonitor.convert(monitor, 100);
	}

	public void doChecking() {
		monitor.subTask("Semantic checking");
		
		switch (selectionMethod.getSelectionAlgorithm()) {
			case MODULESELECTIONORIGINAL:
				generalChecker();
				break;
			case BROKENREFERENCESINVERTED:
				BrokenPartsViaReferences brokenParts = (BrokenPartsViaReferences)selectionMethod;
				if (brokenParts.getAnalyzeOnlyDefinitions()) {
					Map<Module, List<Assignment>> moduleAndBrokenDefinitions = brokenParts.getModuleAndBrokenDefs();
					definitionsChecker(moduleAndBrokenDefinitions);
				} else {
					generalChecker();
				}
				break;
			default:
				generalChecker();
				break;
		}
		
		
		
		monitor.subTask("Doing post semantic checks");

		for (Module module : selectionMethod.getModulesToCheck()) {
			module.postCheck();
		}
		
		progress.done();
	}
	//TODO check if this can be merged with the following one
	private void generalChecker() {
		progress.setTaskName("Semantic check");
		progress.setWorkRemaining(selectionMethod.getModulesToCheck().size());
		
		for (Module module : selectionMethod.getModulesToSkip()) {
			module.setSkippedFromSemanticChecking(true);
		}
		for (Module module : selectionMethod.getModulesToCheck()) {
			module.setSkippedFromSemanticChecking(false);
		}

		// process the modules one-by-one
		for (final Module module : selectionMethod.getModulesToCheck()) {
			progress.subTask("Semantically checking module: " + module.getName());

			module.check(compilationCounter);

			progress.worked(1);
		}

		for (final Module module : selectionMethod.getModulesToSkip()) {
			module.setSkippedFromSemanticChecking(false);
		}
	}

	private void definitionsChecker(final Map<Module, List<Assignment>> moduleAndBrokenDefs) {
		progress.setTaskName("Semantic check");
		progress.setWorkRemaining(moduleAndBrokenDefs.size());

		for (Map.Entry<Module, List<Assignment>> entry : moduleAndBrokenDefs.entrySet()) {
			Module module = entry.getKey();

			progress.subTask("Semantically checking broken parts in module: " + module.getName());

			if (module instanceof TTCN3Module) {
				((TTCN3Module) module).checkWithDefinitions(compilationCounter, entry.getValue());
			} else {
				module.check(compilationCounter);
			}

			progress.worked(1);
		}
	}

	//Not used, perhaps it can be removed
//	private void markSemanticMarkersForRemoval(final Map<Module, List<Assignment>> moduleAndBrokenDefs) {
//		progress.setTaskName("Removing old semantic markers");
//		progress.setWorkRemaining(moduleAndBrokenDefs.size());
//		for (Map.Entry<Module, List<Assignment>> entry : moduleAndBrokenDefs.entrySet()) {
//			Module module = entry.getKey();
//
//			progress.subTask("Removing semantic markers of broken parts in module: " + module.getName());
//
//			List<Assignment> assignments = entry.getValue();
//			if(assignments.isEmpty()) {
//				continue;
//			}
//			MarkerHandler.markMarkersForRemoval(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, assignments);
//
//			progress.worked(1);
//		}
//	}
}
