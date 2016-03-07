/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.brokenpartsanalyzers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Helper class to check broken parts.
 *
 * @author Peter Olah
 */
public final class BrokenPartsChecker {

	private final SubProgressMonitor semanticMonitor;

	private final IProgressMonitor monitor;

	private final CompilationTimeStamp compilationCounter;

	private final SelectionMethodBase selectionMethod;

	public BrokenPartsChecker(final IProgressMonitor monitor, final CompilationTimeStamp compilationCounter, final SelectionMethodBase selectionMethod) {
		this.compilationCounter = compilationCounter;
		this.selectionMethod = selectionMethod;
		this.monitor = monitor;
		semanticMonitor = new SubProgressMonitor(this.monitor, 1);
	}

	public void doChecking() {
		monitor.subTask("Semantic checking");
		
		switch (selectionMethod.getSelectionAlgorithm()) {
			case MODULESELECTIONORIGINAL:
				generalChecker();
				break;
			case BROKENREFERENCESINVERTED:
				BrokenPartsViaReferences brokenParts = (BrokenPartsViaReferences)selectionMethod;
				Map<Module, List<Assignment>> moduleAndBrokenDefinitions = brokenParts.getModuleAndBrokenDefs();
				if (brokenParts.getAnalyzeOnlyDefinitions()) {
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
		
		semanticMonitor.done();
	}
	//TODO check if this can be merged with the following one
	private void generalChecker() {
		semanticMonitor.beginTask("Semantic check", selectionMethod.getModulesToCheck().size());
		// process the modules one-by-one
		for (final Module module : selectionMethod.getModulesToCheck()) {
			semanticMonitor.subTask("Semantically checking module: " + module.getName());
			module.check(compilationCounter);
			semanticMonitor.worked(1);
		}
	}

	private void definitionsChecker(Map<Module, List<Assignment>> moduleAndBrokenDefs) {
		semanticMonitor.beginTask("Semantic check", moduleAndBrokenDefs.size());

		for (Map.Entry<Module, List<Assignment>> entry : moduleAndBrokenDefs.entrySet()) {
			Module module = entry.getKey();

			semanticMonitor.subTask("Semantically checking broken parts in module: " + module.getName());

			if (module instanceof TTCN3Module) {
				((TTCN3Module) module).checkWithDefinitions(compilationCounter, entry.getValue());
			} else {
				module.check(compilationCounter);
			}
			semanticMonitor.worked(1);
		}
	}
}
