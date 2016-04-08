/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Import module statement.
 * 
 * @author Kristof Szabados
 */
public abstract class ModuleImportation implements IReferenceChainElement, IOutlineElement, IVisitableNode {
	protected static final String KIND = "imported module";
	public static final String UNUSEDIMPORTATION = "Possibly unused importation";

	protected Identifier identifier;
	protected Module referredModule = null;

	protected IProject project;

	/**
	 * indicates whether the importation was used to import something or
	 * not.
	 */
	protected boolean usedForImportation = false;

	protected CompilationTimeStamp lastImportCheckTimeStamp;

	/**
	 * Stores whether the module referred by this importation has changed
	 * when the importation check was last done.
	 */
	private boolean hasUnhandledChange = false;

	public ModuleImportation(final Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the identifier of the importation.
	 * */
	@Override
	public final Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the referred module.
	 * 
	 * @return the referred module, might be null.
	 **/
	public final Module getReferredModule() {
		return referredModule;
	}

	/**
	 * @return whether the module referred to by this importation has changed
	 *         to an other one since the last importation check.
	 * */
	public boolean hasUnhandledChange() {
		return hasUnhandledChange;
	}

	/**
	 * Sets whether the module referred to by this importation has changed to
	 * an other one since the last importation check.
	 * 
	 * @param value
	 *                whether it has changed or not.
	 * */
	public void setUnhandledChange(final boolean value) {
		hasUnhandledChange = value;
	}

	/**
	 * Clears the previously accumulated information about this importation.
	 * 
	 * This is usually done, when the referred module clears the references
	 * pointing to itself. This happens usually when the module might become
	 * semantically outdated/renamed.
	 * */
	public void clear() {
		referredModule = null;
		lastImportCheckTimeStamp = null;
	}

	/**
	 * Sets the parser of the project this module importation belongs to.
	 * 
	 * @param project
	 *                the project this module importation belongs to
	 * */
	public final void setProject(final IProject project) {
		this.project = project;
	}

	@Override
	public Location getChainLocation() {
		if (identifier != null && identifier.getLocation() != null) {
			return identifier.getLocation();
		}

		return null;
	}

	@Override
	public final Object[] getOutlineChildren() {
		return new Object[] {};
	}

	@Override
	public final String getOutlineText() {
		return "";
	}

	@Override
	public final String getOutlineIcon() {
		if (referredModule == null) {
			return "titan.gif";
		}

		return referredModule.getOutlineIcon();
	}

	@Override
	public final int category() {
		return 0;
	}

	/**
	 * Call this function to indicate that this importation to was used to
	 * import something.
	 */
	public final void setUsedForImportation() {
		usedForImportation = true;
	}

	/**
	 * @return whether this importation was used to import something or not.
	 * */
	public final boolean getUsedForImportation() {
		return usedForImportation;
	}

	/**
	 * Checks the import hierarchies of this importation (and the ones in
	 * the imported module recursively).
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                a chain of references used to find circularly imported
	 *                modules.
	 * @param moduleStack
	 *                the stack of modules visited so far from the entry
	 *                point.
	 * */
	public abstract void checkImports(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain,
			final List<Module> moduleStack);

	/**
	 * Converts the import statement itself.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 **/
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Checks properties, that can only be checked after the semantic check
	 * was completely run.
	 */
	public void postCheck() {
		if (!usedForImportation) {
			identifier.getLocation().reportConfigurableSemanticProblem(Module.getReportUnusedModuleImportationSeverity(),
					UNUSEDIMPORTATION);
		}
	}

	/**
	 * Checks if there is an assignment that could be imported via this
	 * importation with the provided identifier.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param identifier
	 *                the identifier to search for.
	 * 
	 * @return true if there is an assignment imported with the provided
	 *         name, false otherwise.
	 * */
	public abstract boolean hasImportedAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier);

	/**
	 * Tries to import an assignment from its referred module.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                a chain of references used to find circularly imported
	 *                modules.
	 * @param moduleId
	 *                the identifier of the module, where the assignment
	 *                should be imported into.
	 * @param reference
	 *                the reference of the assignment to import.
	 * @param usedImports
	 *                the list of importations reached when walking back
	 *                after reaching a possibly good assignment
	 * 
	 * @return the assignment, or null in case of errors.
	 * */
	public abstract Assignment importAssignment(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain,
			final Identifier moduleId, final Reference reference, final List<ModuleImportation> usedImports);

	/**
	 * Adds the imported module or definitions contained in it, to the list
	 * completion proposals.
	 * 
	 * @param propCollector
	 *                the proposal collector.
	 * @param targetModuleId
	 *                the identifier of the module where the definition will
	 *                be inserted. It is used to check if it is visible
	 *                there or not.
	 * */
	public abstract void addProposal(final ProposalCollector propCollector, final Identifier targetModuleId);

	/**
	 * Adds the imported module or definitions contained in it, to the list
	 * declaration proposals.
	 * 
	 * @param declarationCollector
	 *                the declaration collector.
	 * @param targetModuleId
	 *                the identifier of the module where the declaration
	 *                should be inserted into.
	 * */
	public abstract void addDeclaration(final DeclarationCollector declarationCollector, final Identifier targetModuleId);
}
