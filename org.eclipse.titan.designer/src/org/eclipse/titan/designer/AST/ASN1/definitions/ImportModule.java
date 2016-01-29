/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportation;
import org.eclipse.titan.designer.AST.ModuleImportationChain;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * Import module.
 * Models an asn.1 module of the section IMPORTS in the parent asn1.module
 * 
 * @author Kristof Szabados
 */
public final class ImportModule extends ModuleImportation {
	public static final String MISSINGMODULE = "There is no ASN.1 module with name `{0}''";
	private static final String NOTASN1MODULE = "The module referred by `{0}'' is not an ASN.1 module";
	private static final String SYMBOLNOTEXPORTED = "Symbol `{0}'' is not exported from module `{1}''";

	/** imported symbols FROM this module */
	private final Symbols symbols;

	public ImportModule(final Identifier identifier, final Symbols symbols) {
		super(identifier);
		this.identifier = identifier;
		this.symbols = (null == symbols) ? new Symbols() : symbols;
	}

	@Override
	public String chainedDescription() {
		return (null != identifier) ? identifier.getDisplayName() : null;
	}

	/** @return the symbols to be imported from this imported module */
	public Symbols getSymbols() {
		return symbols;
	}

	/**
	 * Checks if a given symbol is imported through this importation.
	 * 
	 * @param identifier
	 *                the identifier to search for.
	 * 
	 * @return true if a symbol with this identifier is imported, false
	 *         otherwise.
	 * */
	public boolean hasSymbol(final Identifier identifier) {
		return symbols.hasSymbol(identifier.getName());
	}

	@Override
	public void checkImports(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain, final List<Module> moduleStack) {
		if (null != lastImportCheckTimeStamp && !lastImportCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		Module temp = referredModule;
		referredModule = null;

		symbols.checkUniqueness(timestamp);

		final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
		if (null == parser || null == identifier) {
			lastImportCheckTimeStamp = timestamp; 
			//FIXME: is it correct? lastImportCheckTimeStamp will be set in extreme case only - very early running
			return;
		}

		referredModule = parser.getModuleByName(identifier.getName());
		if (temp != referredModule) {
			setUnhandledChange(true);
		}
		if (referredModule == null) {
			identifier.getLocation().reportSemanticError(MessageFormat.format(MISSINGMODULE, identifier.getDisplayName()));
		} else {
			if (!(referredModule instanceof ASN1Module)) {
				identifier.getLocation().reportSemanticError(MessageFormat.format(NOTASN1MODULE, identifier.getDisplayName()));
				lastImportCheckTimeStamp = timestamp;
				referredModule = null;
				return;
			}

			moduleStack.add(referredModule);
			if (!referenceChain.add(this)) {
				moduleStack.remove(moduleStack.size() - 1);
				lastImportCheckTimeStamp = timestamp;
				return; 
			}

			referredModule.checkImports(timestamp, referenceChain, moduleStack);

			for (int i = 0; i < symbols.size(); i++) {
				Identifier id = symbols.getNthElement(i);
				List<ISubReference> list = new ArrayList<ISubReference>();
				list.add(new FieldSubReference(id));
				Defined_Reference reference = new Defined_Reference(null, list);
				reference.setLocation(identifier.getLocation());

				if (null != referredModule.getAssBySRef(timestamp, reference)) {
					if (!((ASN1Module) referredModule).exportsSymbol(timestamp, id)) {
						identifier.getLocation().reportSemanticError(
								MessageFormat.format(SYMBOLNOTEXPORTED, id.getDisplayName(), referredModule
										.getIdentifier().getDisplayName()));
					}
				}
			}

			moduleStack.remove(moduleStack.size() - 1);
		}

		lastImportCheckTimeStamp = timestamp;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastImportCheckTimeStamp != null && !lastImportCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		usedForImportation = false;
	}

	@Override
	public boolean hasImportedAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (referredModule == null) {
			return false;
		}

		final Assignments assignments = referredModule.getAssignments();
		if (assignments != null && assignments.hasLocalAssignmentWithID(timestamp, identifier)) {
			return true;
		}

		return false;
	}

	@Override
	public Assignment importAssignment(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain,
			final Identifier moduleId, final Reference reference, final List<ModuleImportation> usedImports) {
		// referenceChain is not used since this can only be the
		// endpoint of an importation chain.
		if (referredModule == null) {
			return null;
		}

		final Assignment result = referredModule.importAssignment(timestamp, moduleId, reference);
		if (result != null) {
			usedImports.add(this);
			setUsedForImportation();
		}

		return result;
	}

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
	@Override
	public void addProposal(final ProposalCollector propCollector, final Identifier targetModuleId) {
		List<ISubReference> subrefs = propCollector.getReference().getSubreferences();

		if (propCollector.getReference().getModuleIdentifier() == null && subrefs.size() == 1) {
			propCollector.addProposal(identifier, ImageCache.getImage(getOutlineIcon()), KIND);
		}

		final Module savedReferedModule = referredModule;
		if (savedReferedModule != null) {
			Assignments assignments = savedReferedModule.getAssignments();
			for (int i = 0, size = assignments.getNofAssignments(); i < size; i++) {
				Assignment temporalAssignment = assignments.getAssignmentByIndex(i);
				if (savedReferedModule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, temporalAssignment)) {
					temporalAssignment.addProposal(propCollector, 0);
				}
			}
		}
	}

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
	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final Identifier targetModuleId) {
		final Module savedReferedModule = referredModule;
		if (savedReferedModule != null) {
			Assignments assignments = savedReferedModule.getAssignments();
			for (int i = 0; i < assignments.getNofAssignments(); i++) {
				Assignment temporalAssignment = assignments.getAssignmentByIndex(i);
				if (savedReferedModule.isVisible(CompilationTimeStamp.getBaseTimestamp(), targetModuleId, temporalAssignment)) {
					temporalAssignment.addDeclaration(declarationCollector, 0);
				}
			}
			Identifier moduleId = declarationCollector.getReference().getModuleIdentifier();
			List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
			if (moduleId == null && subrefs.size() == 1 && identifier.getName().equals(subrefs.get(0).getId().getName())) {
				declarationCollector.addDeclaration(savedReferedModule.getIdentifier().getDisplayName(), savedReferedModule
						.getIdentifier().getLocation(), (Scope) null);
			}
		}
	}

	@Override
	public boolean accept(ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (symbols != null && !symbols.accept(v)) {
			return false;
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
