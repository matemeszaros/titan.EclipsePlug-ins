/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.MarkerHandler;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.ASN1.Undefined_Assignment;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author Peter Olah
 * @author Jeno Attila Balasko
 */
//FIXME clean up selection methods if this way of working is tested to be OK
public final class BrokenPartsViaReferences extends SelectionMethodBase implements IBaseAnalyzer {
	// when the definition based search for parts to be analyzed exceeds this limit we switch back to the import based method.
	// 1 second in nanoseconds
	private final static long TIMELIMIT = 10 * (long)1e+9;
	
	/**
	 *  When the percentage of broken modules is bigger than this limit
	 *   the module level selection shall be used.
	 * 
	 * Otherwise the assignment level detection would take too long.
	 */
	private final static float BROKEN_MODULE_LIMIT = 10;

	private final CompilationTimeStamp timestamp;
	private final Map<Module, List<Assignment>> moduleAndBrokenAssignments;
	private boolean analyzeOnlyAssignments;
	
	public BrokenPartsViaReferences(final SelectionAlgorithm selectionAlgorithm, final CompilationTimeStamp timestamp) {
		super(selectionAlgorithm);
		moduleAndBrokenAssignments = new HashMap<Module, List<Assignment>>();
		analyzeOnlyAssignments = false;
		this.timestamp = timestamp;
		header = "\n**Selection with Broken parts via references is started at:";
		footer = "**Selection with Broken parts via references is ended at:  ";
	}

	public Map<Module, List<Assignment>> getModuleAndBrokenDefs() {
		return moduleAndBrokenAssignments;
	}

	public boolean getAnalyzeOnlyDefinitions() {
		return analyzeOnlyAssignments;
	}

	@Override
	public void execute() {
		if (writeDebugInfo) {
			TITANDebugConsole.println(String.format(format, header, simpleDateFormat.format(new Date())));
		}
		start = System.nanoTime();

		final List<Module> startModules = collectStartModules(allModules);
		final Map<Module, List<Module>> invertedImports = buildInvertedImportStructure(allModules, startModules);

		computeAnalyzeOnlyDefinitionsFlag(allModules, startModules);

		if (analyzeOnlyAssignments) {
			final Map<Module, List<AssignmentHandler>> result = collectBrokenParts(startModules, invertedImports);
			if (writeDebugInfo && !isTooSlow()) {
				writeDebugInfo(result);
			}
			collectRealBrokenParts(result);
		}

		if(writeDebugInfo && isTooSlow()) {
			TITANDebugConsole.println("  Switching back to old selection format");
		}
		// if we need to use the old selection or the new selection method took too long
		if(!analyzeOnlyAssignments || isTooSlow()) {
			analyzeOnlyAssignments = false;
			modulesToCheck.clear();
			moduleAndBrokenAssignments.clear();
			final List<Module> modules = collectBrokenModulesViaInvertedImports(startModules, invertedImports);
			modulesToCheck.addAll(modules);
		}

		afterExecute();

		end = System.nanoTime() - start;
		if (writeDebugInfo) {
			TITANDebugConsole.println(String.format(format, footer, simpleDateFormat.format(new Date())));
			infoAfterExecute();
		}
	}

	/**
	 * Sets the flag "analyzeOnlyAssignments" to true, if the size of the broken modules is not too high 
	 * 
	 * @param allModules
	 * @param startModules - the broken modules
	 */
	public void computeAnalyzeOnlyDefinitionsFlag(final List<Module> allModules, final List<Module> startModules) {
		float brokenModulesRatio = (float) ((startModules.size() * 100.0) / allModules.size());
//		if (Float.compare(brokenModulesRatio, (float) BROKEN_MODULE_LIMIT) < 0) {
//			analyzeOnlyAssignments = true;
//		}
		analyzeOnlyAssignments = true;
	}
	
	/**
	 * Adds modules from allModules to a list  which
	 * - have null CompilationTimestamp or
	 * - canbeCheckRoot or
	 * - have not been checked semantically
	 * 
	 * @param allModules
	 * @param startModules
	 */
	protected List<Module> collectStartModules(final List<Module> allModules) {
		final List<Module> startModules = new ArrayList<Module>();
		for (Module actualModule : allModules) {
			// Collect injured modules directly into startModules, we will start the checking from these modules.
			// Collect modules which have not been checked semantically.
			if ((actualModule.getLastCompilationTimeStamp() == null || actualModule.isCheckRoot() || !semanticallyChecked.contains(actualModule.getName())) && !startModules.contains(actualModule) ) {
				startModules.add(actualModule);
			}
		}
		return startModules;
	}

	/**
	 * It is build an inverted import structure and identify startmodules whose CompilationTimeStamp is null.
	 * 
	 * @param allModules
	 *            the list of modules to be check. Initially all modules.
	 * @param startModules
	 *            the list of modules to be check. Initially all modules, but the function will remove those that can be skipped.
	 * 
	 * @return invertedImports contains the next:<br>
	 *         - key: a module.<br>
	 *         - values: in these modules the key module is used, so all values imported this module, it is an inverted "imported" connection.<br>
	 *         If module A import B, C, D and module B import D, E then the next structure will be built:<br>
	 *         A = [],<br>
	 *         B = [A],<br>
	 *         C = [A],<br>
	 *         D = [A, B],<br>
	 *         E = [B]<br>
	 */
	protected Map<Module, List<Module>> buildInvertedImportStructure(final List<Module> allModules, final List<Module> startModules) {

		final Map<Module, List<Module>> invertedImports = new HashMap<Module, List<Module>>();

		for (Module actualModule : allModules) {
			// We have to add all module to get a correct inverted import structure.
			// It covers the case when a module is a top-level module, so it has't got any import.
			if (!invertedImports.containsKey(actualModule)) {
				invertedImports.put(actualModule, new ArrayList<Module>());
			}

			for (Module actualImportedModule : actualModule.getImportedModules()) {
				if (invertedImports.containsKey(actualImportedModule)) {
					final List<Module> dependentModules = invertedImports.get(actualImportedModule);

					if (!dependentModules.contains(actualModule)) {
						dependentModules.add(actualModule);
					}
				} else {
					final List<Module> temp = new ArrayList<Module>();
					temp.add(actualModule);
					invertedImports.put(actualImportedModule, temp);
				}
			}
		}

		return invertedImports;
	}

	protected List<Module> collectBrokenModulesViaInvertedImports(final List<Module> startModules, final Map<Module, List<Module>> invertedImports) {
		
		final List<Module> startModulesCopy = new ArrayList<Module>(startModules);

		final List<Module> result = new ArrayList<Module>();
		final MessageConsoleStream stream = TITANDebugConsole.getConsole().newMessageStream();
		if (writeDebugInfo) {
			for (Module startModule: startModules) {
				TITANDebugConsole.println("  ** Module " + startModule.getName() + " can not be skipped as it was not yet analyzed.", stream);
			}
		}

		for (int s = 0; s < startModulesCopy.size(); ++s) {
			final Module startModule = startModulesCopy.get(s);
			if (!result.contains(startModule)) {
				result.add(startModule);
			}

			final List<Module> whereStartModuleUsed = invertedImports.get(startModule);
			for (int d = 0; d < whereStartModuleUsed.size(); ++d) {
				final Module dependentModule = whereStartModuleUsed.get(d);
				if (!startModulesCopy.contains(dependentModule)) {
					startModulesCopy.add(dependentModule);
					if (writeDebugInfo) {
						TITANDebugConsole.println("  ** Module " + dependentModule.getName() + " can not be skipped as it depends on " + startModule.getName() + " which needs to be checked.", stream);
					}
				}
			}

			startModule.notCheckRoot();
			final Assignments assignments = startModule.getAssignments();
			for (int d = 0; d < assignments.getNofAssignments(); ++d) {
				final Assignment assignment = assignments.getAssignmentByIndex(d);
				assignment.notCheckRoot();
			}
		}
		return result;
	}

	protected Map<Module, List<AssignmentHandler>> collectBrokenParts(final List<Module> startModules, final Map<Module, List<Module>> invertedImports) {

		final List<Module> startModulesCopy = new ArrayList<Module>(startModules);

		final Map<Module, List<AssignmentHandler>> moduleAndBrokenAssignments = new HashMap<Module, List<AssignmentHandler>>();

		processStartModules(startModulesCopy, moduleAndBrokenAssignments);

		for (int i = 0; i < startModulesCopy.size() && !isTooSlow(); ++i) {
			final Module startModule = startModulesCopy.get(i);
			List<AssignmentHandler> startAssignments;
			if (moduleAndBrokenAssignments.containsKey(startModule)) {
				startAssignments = moduleAndBrokenAssignments.get(startModule);
			} else {
				startAssignments = getAssignmentsFrom(startModule); //<<<<<< getAssignments() used in collectBrokenModulesViaInvertedImports, too
				moduleAndBrokenAssignments.put(startModule, startAssignments);
			}

			if (!startAssignments.isEmpty()) {
				final List<Module> whereStartModuleUsed = invertedImports.get(startModule);
				for (int j = 0; j < whereStartModuleUsed.size(); ++j) {
					final Module dependentModule = whereStartModuleUsed.get(j);
					List<AssignmentHandler> dependentAssignments;
					if (moduleAndBrokenAssignments.containsKey(dependentModule)) {
						dependentAssignments = moduleAndBrokenAssignments.get(dependentModule);
					} else {
						dependentAssignments = getAssignmentsFrom(dependentModule);
						moduleAndBrokenAssignments.put(dependentModule, dependentAssignments);
					}

					// We have to separate broken and not broken definition, because of postcheck.
					final List<AssignmentHandler> brokens = new ArrayList<AssignmentHandler>();
					final List<AssignmentHandler> notBrokens = new ArrayList<AssignmentHandler>();

					for (int s = 0; s < startAssignments.size(); ++s) {
						final AssignmentHandler startAssignment = startAssignments.get(s);
						if (startAssignment.getIsContagious()) {
							for (int d = 0; d < dependentAssignments.size(); ++d) {
								final AssignmentHandler dependentAssignment = dependentAssignments.get(d);
								dependentAssignment.check(startAssignment); //only infection and contagion are checked
								if (dependentAssignment.getIsInfected()) {
									if (!startModulesCopy.contains(dependentModule)) {
										startModulesCopy.add(dependentModule);
									}
									if( !brokens.contains(dependentAssignment) ) {
										brokens.add(dependentAssignment);
									}
								}
							}
						}
					}
					
					for (int d = 0; d < dependentAssignments.size(); ++d) {
						final AssignmentHandler dependentAssignment = dependentAssignments.get(d);
						if (!dependentAssignment.getIsInfected()) {
							notBrokens.add(dependentAssignment);
						}
					}

					// Have to post check of local definition of modules.
					// A definition can reference an other definition too.
					checkLocalAssignments(brokens, notBrokens);

					// If dependent module not added startModules,
					// it means it has not got broken definition,
					// so we have to delete it from moduleAndBrokenDefs.
					if (!startModulesCopy.contains(dependentModule)) {
						moduleAndBrokenAssignments.remove(dependentModule);
					}

				}
			}

		}

		return moduleAndBrokenAssignments;
	}

	protected void collectRealBrokenParts(final Map<Module, List<AssignmentHandler>> moduleAndAssignments ) {
		for (Map.Entry<Module, List<AssignmentHandler>> entry : moduleAndAssignments.entrySet()) {

			List<Assignment> assignments = new ArrayList<Assignment>();
			for (AssignmentHandler assignmentHandler : entry.getValue()) {
				if (assignmentHandler.getIsInfected()) {
					assignments.add(assignmentHandler.getAssignment());
				}
				assignmentHandler.assignment.notCheckRoot();
			}
			final Module module = entry.getKey();
			if (!assignments.isEmpty() || !module.getSkippedFromSemanticChecking()) {;
				moduleAndBrokenAssignments.put(module, assignments);
				modulesToCheck.add(module);
			}
		}
	}

	public void processStartModules(final List<Module> startModules, final Map<Module, List<AssignmentHandler>> moduleAndBrokenAssignments) {
		for (Module startModule : startModules) {
			if(isTooSlow()) {
				return;
			}
			if (startModule instanceof TTCN3Module && startModule.getLastCompilationTimeStamp() != null && !startModule.isCheckRoot()) {
				final Assignments startAssignments = startModule.getAssignments();
				final List<AssignmentHandler> brokens = new ArrayList<AssignmentHandler>();
				final List<AssignmentHandler> notBrokens = new ArrayList<AssignmentHandler>();
				for( Assignment assignment: startAssignments){
					MarkerHandler.markAllSemanticMarkersForRemoval(assignment);
				}
				for (int d = 0; d < startAssignments.getNofAssignments(); ++d) {
					final Assignment startAssignment = startAssignments.getAssignmentByIndex(d);
					final AssignmentHandler assignmentHandler = AssignmentHandlerFactory.getDefinitionHandler(startAssignment);
//					if (startAssignment.getLastTimeChecked() == null) {
						startAssignment.check(timestamp);
//					}

					startAssignment.accept(assignmentHandler);

					if (startAssignment.isCheckRoot()) {
						assignmentHandler.setIsInfected(true);
						startAssignment.notCheckRoot();
						assignmentHandler.addReason("Definition's infected, because of incremental parsing.");
						brokens.add(assignmentHandler);
					} else if (assignmentHandler.getIsInfected()) {
						assignmentHandler.addReason("Definition contains an infected reference.");
						brokens.add(assignmentHandler);
					} else {
						notBrokens.add(assignmentHandler);
					}
				}

				if (!brokens.isEmpty()) {
					checkLocalAssignments(brokens, notBrokens);
					if (moduleAndBrokenAssignments.containsKey(startModule)) {
						moduleAndBrokenAssignments.get(startModule).addAll(brokens);
					} else {
						moduleAndBrokenAssignments.put(startModule, brokens);
					}
				}
			} else {
				if (startModule.getLastCompilationTimeStamp() == null) {
					//The markers have been marked for removal only for ASN1 modules
					startModule.check(timestamp);
				}

				final List<AssignmentHandler> startAssignments = getAssignmentsFrom(startModule);//puts additional markers!
				for (AssignmentHandler assignmentHandler : startAssignments) {
					assignmentHandler.initStartParts();
					assignmentHandler.assignment.notCheckRoot();
					assignmentHandler.addReason("Parent module's CompilationTimeStamp is null.");
				}

				if (moduleAndBrokenAssignments.containsKey(startModule)) {
					moduleAndBrokenAssignments.get(startModule).addAll(startAssignments);
				} else {
					moduleAndBrokenAssignments.put(startModule, startAssignments);
				}
			}

			startModule.notCheckRoot();
		}
	}
	
	public List<AssignmentHandler> getAssignmentsFrom(final Module module) {
		final List<AssignmentHandler> assignmentHandlers = new ArrayList<AssignmentHandler>();
		final Assignments assignments = module.getAssignments();
		for (int d = 0; d < assignments.getNofAssignments(); ++d) {
			final Assignment assignment = assignments.getAssignmentByIndex(d);
			final AssignmentHandler assignmentHandler = AssignmentHandlerFactory.getDefinitionHandler(assignment);
			if(assignment instanceof Undefined_Assignment){
				final ASN1Assignment realAssignment = ((Undefined_Assignment)assignment).getRealAssignment(CompilationTimeStamp.getBaseTimestamp());
				if(realAssignment != null) {
					realAssignment.accept(assignmentHandler);
				} else {
					assignment.accept(assignmentHandler);//TODO: re-fine this branch
				}
			} else {
				assignment.accept(assignmentHandler);
			}
			assignmentHandlers.add(assignmentHandler);
		}

		return assignmentHandlers;
	}

	protected void checkLocalAssignments(final List<AssignmentHandler> brokens, final List<AssignmentHandler> notBrokens) {

		if (brokens.isEmpty() || notBrokens.isEmpty()) {
			return;
		}
		
		final HashMap<String, AssignmentHandler> brokenMap = new HashMap<String, AssignmentHandler>(brokens.size() + notBrokens.size());
		for(AssignmentHandler handler: brokens) {
			brokenMap.put(handler.getAssignment().getIdentifier().getDisplayName(), handler);
		}
		
		boolean proceed = true;
		while (proceed) {
			proceed = false;
			
			
			for (int i = notBrokens.size() -1; i >=0; --i) {
				final AssignmentHandler notBroken = notBrokens.get(i);
				boolean found = false;
				for (String name : notBroken.getContagiousReferences()) {
					if(brokenMap.containsKey(name)) {
						notBroken.check(brokenMap.get(name));
						found = true;
						break;
					}
				}
				if(!found) {
					for (String name : notBroken.getNonContagiousReferences()) {
						if(brokenMap.containsKey(name)) {
							notBroken.check(brokenMap.get(name));
							found = true;
							break;
						}
					}
				}
				
				if(found) {
					proceed = true;
					notBrokens.remove(i);
					brokens.add(notBroken);
					brokenMap.put(notBroken.getAssignment().getIdentifier().getDisplayName(), notBroken);
				}
			}
		}
	}
	
	// Returns true, if the time spent is over a time limit
	// It is used to check if this kind of algorithm is too slow
	// For debugging purposes you can set it for "false"
	private boolean isTooSlow(){
		return false;
		//return ((System.nanoTime()-start) > TIMELIMIT);	
	}

	protected void writeDebugInfo(final Map<Module, List<AssignmentHandler>> moduleAndAssignments) {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				TITANDebugConsole.println("  Detailed info:");

				for (Map.Entry<Module, List<AssignmentHandler>> entry : moduleAndAssignments.entrySet()) {
					final List<AssignmentHandler> values = entry.getValue();

					TITANDebugConsole.println("    module: " + entry.getKey().getIdentifier().getDisplayName());

					for (AssignmentHandler assignmentHandler : values) {
						TITANDebugConsole.println("         " + assignmentHandler + " | " + assignmentHandler.getReasons());
						if(assignmentHandler.getIsInfected() || assignmentHandler.getIsContagious()) {
							TITANDebugConsole.println("            " + (assignmentHandler.getIsInfected() ? "[+]" : "[-]") + " : infected");
							TITANDebugConsole.println("            " + (assignmentHandler.getIsContagious() ? "[+]" : "[-]") + " : contagious");
							TITANDebugConsole.println("            nonContagious references: " + assignmentHandler.getNonContagiousReferences());
							TITANDebugConsole.println("            contagious references: " + assignmentHandler.getContagiousReferences());
							TITANDebugConsole.println("            infected references: " + assignmentHandler.getInfectedReferences());
						}
					}
				}

				TITANDebugConsole.println("  in dot format:");
				TITANDebugConsole.println("  digraph {");
				TITANDebugConsole.println("    rankdir=LR;");

				final ArrayList<Module> modules = new ArrayList<Module>(moduleAndAssignments.keySet());
				Collections.sort(modules, new Comparator<Module>() {

					@Override
					public int compare(final Module o1, final Module o2) {
						return o1.getName().compareTo(o2.getName());
					}
					
				});
				
				for (Module module : modules) {
					final String moduleName = module.getName();
					TITANDebugConsole.println("    subgraph cluster_" + moduleName + " {");
					TITANDebugConsole.println("    label=\" " + module.getIdentifier().getDisplayName() + "\";");

					final List<AssignmentHandler> values = moduleAndAssignments.get(module);

					for (AssignmentHandler assignmentHandler : values) {
						for(String reference: assignmentHandler.getInfectedReferences()) {
							TITANDebugConsole.println("      " + assignmentHandler.getAssignment().getIdentifier().getDisplayName() + "->" + reference + ";");
						}
					}
					TITANDebugConsole.println("    }");
				}
				TITANDebugConsole.println("  }");

			}
		});
	}
}