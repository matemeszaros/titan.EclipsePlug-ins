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

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportationChain;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignments;
import org.eclipse.titan.designer.AST.ASN1.Defined_Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titan.designer.parsers.ProjectStructureDataCollector;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * Class to represent ASN-modules.
 * 
 * @author Kristof Szabados
 */
public final class ASN1Module extends Module {
	private static final String FULLNAMEPART1 = ".<exports>";
	private static final String FULLNAMEPART2 = ".<imports>";

	private static final String NOASSIGNMENT = "There is no assignment with name `{0}'' in module `{1}''";
	private static final String NOASSIGNMENTORSYMBOL = "There is no assignment or imported symbol with name `{0}'' in module `{1}''";
	private static final String MORESYMBOLS = "There are more imported symbols with name `{0}'' in module `{1}''";
	private static final String NOIMPORTEDMODULE = "There is no imported module with name `{0}''";
	private static final String NOSYMBOLSIMPORTED = "There is no symbol with name `{0}'' imported from module `{1}''";

	/** default tagging. */
	private final Tag_types tagdef;

	private final boolean extensibilityImplied;
	/** exported stuff. */
	private Exports exports;
	/** imported stuff. */
	private Imports imports;

	private ASN1Assignments assignments;

	public ASN1Module(final Identifier identifier, final IProject project, final Tag_types tagdef, final boolean extensibilityImplied) {
		super(identifier, project);
		this.tagdef = tagdef;
		this.extensibilityImplied = extensibilityImplied;
		exports = new Exports(false);
		exports.setMyModule(this);
		exports.setFullNameParent(this);
		imports = new Imports();
		imports.setMyModule(this);
		imports.setFullNameParent(this);
		assignments = new ASN1Assignments();
		assignments.setParentScope(this);
		assignments.setFullNameParent(this);
	}

	@Override
	public module_type getModuletype() {
		return module_type.ASN_MODULE;
	}

	/**
	 * Sets the export list of the module.
	 * 
	 * @param exports
	 *                the exports to be set.
	 * */
	public void setExports(final Exports exports) {
		this.exports = exports;
		this.exports.setMyModule(this);
		exports.setFullNameParent(this);
	}

	/**
	 * Sets the import list of the module.
	 * 
	 * @param imports
	 *                the imports to be set.
	 * */
	public void setImports(final Imports imports) {
		this.imports = imports;
		this.imports.setMyModule(this);
		imports.setProject(project);
		imports.setFullNameParent(this);
	}

	/**
	 * Sets the assignment list of the module.
	 * 
	 * @param assignments
	 *                the assignments to be set.
	 * */
	public void setAssignments(final ASN1Assignments assignments) {
		this.assignments = assignments;
		this.assignments.setParentScope(this);
		addSubScope(assignments.getLocation(), assignments);
		assignments.setFullNameParent(this);
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = new StringBuilder();
		builder.append(INamedNode.MODULENAMEPREFIX).append(getIdentifier().getDisplayName());

		if (exports == child) {
			return builder.append(FULLNAMEPART1);
		} else if (imports == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public Assignments getAssignmentsScope() {
		return assignments;
	}

	@Override
	public Def_Type getAnytype() {
		return null;
	}

	@Override
	public Object[] getOutlineChildren() {
		return new Object[] { imports, assignments };
	}

	@Override
	public String getOutlineText() {
		return "";
	}

	@Override
	public String getOutlineIcon() {
		return "asn.gif";
	}

	public boolean hasImportedAssignmentWithId(final Identifier identifier) {
		return imports.hasImportedSymbolWithId(identifier);
	}

	@Override
	public void checkImports(final CompilationTimeStamp timestamp, final ModuleImportationChain referenceChain, final List<Module> moduleStack) {
		if (null != lastImportCheckTimeStamp && !lastImportCheckTimeStamp.isLess(timestamp)) {
			return;
		}

		assignments.checkUniqueness(timestamp);
		imports.checkImports(timestamp, referenceChain, moduleStack);
		exports.check(timestamp);

		lastImportCheckTimeStamp = timestamp;
		// TODO compiler: method named collect_visible_mods should be
		// here
	}

	@Override
	public Assignment importAssignment(final CompilationTimeStamp timestamp, final Identifier moduleId, final Reference reference) {
		final Identifier id = reference.getId();
		return assignments.getLocalAssignmentByID(timestamp, id);
	}

	@Override
	public boolean isVisible(final CompilationTimeStamp timestamp, final Identifier moduleId, final Assignment assignment) {
		return true;
	}

	@Override
	public List<Module> getImportedModules() {
		return imports.getImportedModules();
	}

	@Override
	public boolean hasUnhandledImportChanges() {
		return imports.hasUnhandledImportChanges();
	}

	@Override
	public void postCheck() {
		if (!getReportUnusedModuleImportationProblems()) {
			imports.checkImportedness();
		}

		assignments.postCheck();
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastCompilationTimeStamp && !lastCompilationTimeStamp.isLess(timestamp)) {
			return;
		}

		if (!SpecialASN1Module.INTERNAL_MODULE.equals(identifier.getAsnName())) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_ASN1MODULE, identifier, "ASN.1 module");
		}

		imports.check(timestamp);
		assignments.check(timestamp);

		lastCompilationTimeStamp = timestamp;
	}

	@Override
	public Assignments getAssignments() {
		return assignments;
	}

	public boolean exportsSymbol(final CompilationTimeStamp timestamp, final Identifier identifier) {
		return exports.exportsSymbol(timestamp, identifier);
	}

	@Override
	public boolean hasImportedAssignmentWithID(final CompilationTimeStamp timestamp, final Identifier identifier) {
		return imports.singularImportedSymbols_map.containsKey(identifier.getName());
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		Identifier moduleId = reference.getModuleIdentifier();
		final Identifier id = reference.getId();

		if (null == id) {
			return null;
		}

		Module module = null;

		if (null == moduleId || moduleId.getName().equals(identifier.getName())) {
			if (assignments.hasLocalAssignmentWithID(timestamp, id)) {
				return assignments.getLocalAssignmentByID(timestamp, id);
			}
			if (null != moduleId) {
				id.getLocation().reportSemanticError(
						MessageFormat.format(NOASSIGNMENT, id.getDisplayName(), identifier.getDisplayName()));
				return null;
			}
			if (imports.singularImportedSymbols_map.containsKey(id.getName())) {
				module = imports.singularImportedSymbols_map.get(id.getName());
				moduleId = module.getIdentifier();
				imports.getImportedModuleById(moduleId).setUsedForImportation();
			} else if (imports.pluralImportedSymbols.contains(id.getName())) {
				id.getLocation().reportSemanticError(
						MessageFormat.format(MORESYMBOLS, id.getDisplayName(), identifier.getDisplayName()));
				return null;
			} else {
				id.getLocation().reportSemanticError(
						MessageFormat.format(NOASSIGNMENTORSYMBOL, id.getDisplayName(), identifier.getDisplayName()));
				return null;
			}
		}

		if (null == module) {
			if (!imports.hasImportedModuleWithId(moduleId)) {
				moduleId.getLocation().reportSemanticError(MessageFormat.format(NOIMPORTEDMODULE, moduleId.getDisplayName()));
				return null;
			}
			if (!imports.getImportedModuleById(moduleId).hasSymbol(id)) {
				id.getLocation().reportSemanticError(
						MessageFormat.format(NOSYMBOLSIMPORTED, id.getDisplayName(), moduleId.getDisplayName()));
				return null;
			}

			imports.getImportedModuleById(moduleId).setUsedForImportation();
			final ProjectSourceParser parser = GlobalParser.getProjectSourceParser(project);
			if (null == parser) {
				return null;
			}

			module = parser.getModuleByName(moduleId.getName());
		}

		final List<ISubReference> newSubreferences = new ArrayList<ISubReference>();
		newSubreferences.add(new FieldSubReference(id));
		final Defined_Reference finalSeference = new Defined_Reference(null, newSubreferences);

		if (this == module || null == module) {
			return null;
		}

		// FIXME add semantic check guard on project level.
		return module.getAssBySRef(timestamp, finalSeference);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("module: ").append(name);
		return builder.toString();
	}

	@Override
	public void addProposal(final ProposalCollector propCollector) {
		final Identifier moduleId = propCollector.getReference().getModuleIdentifier();
		if (null == moduleId) {
			imports.addProposal(propCollector);
		} else if (null != identifier && moduleId.getName().equals(identifier.getName())) {
			for (int i = 0; i < assignments.getNofAssignments(); i++) {
				assignments.getAssignmentByIndex(i).addProposal(propCollector, 0);
			}
		} else {
			if (imports.hasImportedModuleWithId(moduleId)) {
				final ImportModule importation = imports.getImportedModuleById(moduleId);

				if (null != importation) {
					importation.addProposal(propCollector, identifier);
				}
			}
		}

		super.addProposal(propCollector);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		final Identifier moduleId = declarationCollector.getReference().getModuleIdentifier();
		if (null == moduleId) {
			final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
			if (1 == subrefs.size() && null != identifier && identifier.getName().equals(subrefs.get(0).getId().getName())) {
				declarationCollector.addDeclaration(name, identifier.getLocation(), this);
			}
			imports.addDeclaration(declarationCollector);
		} else if (null != identifier && moduleId.getName().equals(identifier.getName())) {
			for (int i = 0; i < assignments.getNofAssignments(); i++) {
				assignments.getAssignmentByIndex(i).addDeclaration(declarationCollector, 0);
			}
		} else {
			if (imports.hasImportedModuleWithId(moduleId)) {
				final ImportModule importation = imports.getImportedModuleById(moduleId);

				if (null != importation) {
					importation.addDeclaration(declarationCollector, identifier);
				}
			}
		}

		super.addDeclaration(declarationCollector);
	}

	@Override
	public void extractStructuralInformation(final ProjectStructureDataCollector collector) {
		imports.extractStructuralInformation(identifier, collector);
	}

	@Override
	public Assignment getEnclosingAssignment(final int offset) {
		if (assignments == null) {
			return null;
		}
		return assignments.getEnclosingAssignment(offset);
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (assignments != null) {
			assignments.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (exports != null) {
			if (!exports.accept(v)) {
				return false;
			}
		}
		if (imports != null) {
			if (!imports.accept(v)) {
				return false;
			}
		}
		if (assignments != null) {
			if (!assignments.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
