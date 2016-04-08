/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ControlPart;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * The Scope class represents general visibility scopes.
 * 
 * @author Kristof Szabados
 * */
public abstract class Scope implements INamedNode, IIdentifierContainer, IVisitableNode {
	private static final String RUNSONREQUIRED = "A definition without `runs on'' clause cannot {0} {1}, which runs on component type `{2}''";
	private static final String RUNSONREQUIRED2 = "A definition without `runs on'' clause cannot {0} a value of {1} type `{2}'',"
			+ " which runs on component type `{3}''";
	private static final String RUNSONMISSMATCH = "Runs on clause mismatch: A definition that runs on component type `{0}'' cannot {1} {2},"
			+ " which runs on `{3}''";
	private static final String RUNSONMISSMATCH2 = "Runs on clause mismatch: A definition that runs on component type `{0}'' cannot {1}"
			+ " a value of {2} type `{3}'', which runs on `{4}''";

	private static final String GENERAL_SCOPE_NAME = "general scope";
	protected Scope parentScope;
	protected List<Location> subScopeLocations;
	protected List<Scope> subScopes;

	protected String scopeName;

	/** The name of the scope as returned by the __SCOPE__ macro */
	private String scopeMacroName;

	/** the naming parent of the scope. */
	private WeakReference<INamedNode> nameParent;

	public Scope() {
		parentScope = null;
		scopeName = GENERAL_SCOPE_NAME;
	}

	public Location getCommentLocation() {
		return null;
	}

	public T3Doc getT3Doc(final Location location) {

		if (this.getCommentLocation() != null) {
			return new T3Doc(this.getCommentLocation());
		}

		return new T3Doc(getCommentLocation());

	}

	public void setScopeName(final String name) {
		scopeName = name;
	}

	public String getScopeName() {
		return scopeName;
	}

	/**
	 * Sets the name for this scope it should be referred to by the
	 * __SCOPE__ macro.
	 *
	 * @param name
	 *                the name to be set
	 * */
	public final void setScopeMacroName(final String name) {
		scopeMacroName = name;
	}

	/**
	 * Calculates the name for this scope as required by the __SCOPE__
	 * macro.
	 *
	 * @return the __SCOPE__ macro name fo this scope.
	 * */
	public final String getScopeMacroName() {
		if (scopeMacroName != null) {
			return scopeMacroName;
		}

		if (parentScope != null) {
			return parentScope.getScopeMacroName();
		}

		return "unknown scope";
	}

	@Override
	public String getFullName() {
		return getFullName(null).toString();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		if (nameParent == null) {
			return new StringBuilder();
		}

		INamedNode tempParent = nameParent.get();
		// //to avoid infinite loop
		if (tempParent == null || tempParent == this) {
			return new StringBuilder();
		}

		return tempParent.getFullName(this);
	}

	@Override
	public final void setFullNameParent(final INamedNode nameParent) {
		this.nameParent = new WeakReference<INamedNode>(nameParent);
	}

	@Override
	public INamedNode getNameParent() {
		if (nameParent == null) {
			return null;
		}

		return nameParent.get();
	}

	public final void setParentScope(final Scope parentScope) {
		this.parentScope = parentScope;
	}

	public final Scope getParentScope() {
		return parentScope;
	}

	/**
	 * Search in the scope hierarchy for the innermost enclosing
	 * statementblock scope unit.
	 *
	 * @return the innermost enclosing statement block, or null if none.
	 * */
	public StatementBlock getStatementBlockScope() {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getStatementBlockScope();
	}

	/**
	 * Return the module this scope is contained in. Can return
	 * <code>null</code>, it indicates a FATAL ERROR.
	 *
	 * @return the module of the scope, or null in case of error.
	 * */
	public Module getModuleScope() {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getModuleScope();
	}

	/**
	 * Return the control part this scope is contained in. Can return
	 * <code>null</code>, it indicates that the scope is not inside a
	 * control part..
	 *
	 * @return the control part of the scope, or null if the scope is not in
	 *         a control part.
	 * */
	public ControlPart getControlPart() {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getControlPart();
	}

	/**
	 * @return the scope unit of the hierarchy that belongs to a `runs on'
	 *         clause, or null.
	 * */
	public RunsOnScope getScopeRunsOn() {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getScopeRunsOn();
	}

	/**
	 * Collects the TTCN-3 component type associated with the mtc or system
	 * component.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param isSystem
	 *                true if searching for the system component, false
	 *                otherwise.
	 *
	 * @return the system or mtc component type, or null if it cannot be
	 *         determined (outside testcase definitions - in functions and in altsteps ) 
	 * */
	public Component_Type getMtcSystemComponentType(final CompilationTimeStamp timestamp, final boolean isSystem) {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getMtcSystemComponentType(timestamp, isSystem);
	}

	/**
	 * @return the assignments/module definitions scope, or null.
	 * */
	public Assignments getAssignmentsScope() {
		if (parentScope == null) {
			return null;
		}

		return parentScope.getAssignmentsScope();
	}

	/**
	 * Checks that operations that can only be executed in definitions that
	 * have a runs on scope, are really executed in such a definition. And
	 * that the required runs on component is compatible with the runs on
	 * component of the definition.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param assignment
	 *                the assignment to check (used or referred to by the
	 *                statement).
	 * @param errorLocation
	 *                the location to report the error to.
	 * @param operation
	 *                the name of the operation.
	 * */
	public void checkRunsOnScope(final CompilationTimeStamp timestamp, final Assignment assignment, final ILocateableNode errorLocation,
			final String operation) {
		Component_Type referencedComponent;
		switch (assignment.getAssignmentType()) {
		case A_ALTSTEP:
			referencedComponent = ((Def_Altstep) assignment).getRunsOnType(timestamp);
			break;
		case A_FUNCTION:
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			referencedComponent = ((Def_Function) assignment).getRunsOnType(timestamp);
			break;
		case A_TESTCASE:
			referencedComponent = ((Def_Testcase) assignment).getRunsOnType(timestamp);
			break;
		default:
			return;
		}

		if (referencedComponent == null) {
			return;
		}

		RunsOnScope runsOnScope = getScopeRunsOn();
		if (runsOnScope == null) {
			errorLocation.getLocation().reportSemanticError(
					MessageFormat.format(RUNSONREQUIRED, operation, assignment.getDescription(),
							referencedComponent.getTypename()));
		} else {
			Component_Type localType = runsOnScope.getComponentType();
			if (!referencedComponent.isCompatible(timestamp, localType, null, null, null)) {
				errorLocation.getLocation().reportSemanticError(
						MessageFormat.format(RUNSONMISSMATCH, localType.getTypename(), operation,
								assignment.getDescription(), referencedComponent.getTypename()));
			}
		}
	}

	/**
	 * Checks that operations that can only be executed in definitions that
	 * have a runs on scope, are really executed in such a definition. And
	 * that the required runs on component is compatible with the runs on
	 * component of the definition.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param type
	 *                the fat type to check (used or referred to by the
	 *                location).
	 * @param errorLocation
	 *                the location to report the error to.
	 * @param operation
	 *                the name of the operation.
	 * */
	public void checkRunsOnScope(final CompilationTimeStamp timestamp, final IType type, final ILocateableNode errorLocation,
			final String operation) {
		if (type == null) {
			return;
		}

		Component_Type referencedComponent;
		String typename;
		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			referencedComponent = ((Function_Type) type).getRunsOnType(timestamp);
			typename = "function";
			break;
		case TYPE_ALTSTEP:
			referencedComponent = ((Altstep_Type) type).getRunsOnType(timestamp);
			typename = "altstep";
			break;
		default:
			return;
		}

		if (referencedComponent == null) {
			return;
		}

		RunsOnScope runsOnScope = getScopeRunsOn();
		if (runsOnScope == null) {
			errorLocation.getLocation().reportSemanticError(
					MessageFormat.format(RUNSONREQUIRED2, operation, typename, type.getTypename(),
							referencedComponent.getTypename()));
		} else {
			Component_Type localType = runsOnScope.getComponentType();
			if (!referencedComponent.isCompatible(timestamp, localType, null, null, null)) {
				errorLocation.getLocation().reportSemanticError(
						MessageFormat.format(RUNSONMISSMATCH2, localType.getTypename(), operation, typename,
								type.getTypename(), referencedComponent.getTypename()));
			}
		}
	}

	/**
	 * Finds and returns the smallest sub-scope of the current scope, which
	 * still physically encloses the provided position.
	 *
	 * @param offset
	 *                to position of which the smallest enclosing scope we
	 *                wish to find.
	 * @return the smallest enclosing sub-scope of the actual scope, or
	 *         itself, if no sub-scope was found.
	 * */
	public Scope getSmallestEnclosingScope(final int offset) {
		if (subScopes == null) {
			return this;
		}

		Location location;
		int size = subScopeLocations.size();
		for (int i = 0; i < size; i++) {
			location = subScopeLocations.get(i);
			if (location.getOffset() < offset && offset < location.getEndOffset()) {
				return subScopes.get(i).getSmallestEnclosingScope(offset);
			}
		}
		return this;
	}

	/**
	 * Adds a subscope to the actual scope, which is physically inside the
	 * actual scope (might not be a child of the scope though).
	 * <p>
	 * This is used to create a scope hierarchy, which can be used to
	 * quickly locate the smallest enclosing scope of a region described
	 * with positions inside a file.
	 *
	 * @param location
	 *                the location of the scope to be added
	 * @param scope
	 *                the scope to be added.
	 * */
	public void addSubScope(final Location location, final Scope scope) {
		if (NULL_Location.INSTANCE.equals(location)) {
			return;
		}

		if (subScopes == null) {
			subScopes = new ArrayList<Scope>(1);
			subScopes.add(scope);
			subScopeLocations = new ArrayList<Location>(1);
			subScopeLocations.add(location);
			return;
		}

		int index = subScopeLocations.size() - 1;
		if (index == -1) {
			subScopeLocations.add(location);
			subScopes.add(scope);
			return;
		}

		// don't add the same subscope more than once
		for (Scope ss : subScopes) {
			if (scope == ss) {
				return;
			}
		}

		Location subScope = subScopeLocations.get(index);
		while (index > 0 && (subScope.getEndOffset() > location.getOffset() || subScope.getEndOffset() == -1)) {
			index--;
			subScope = subScopeLocations.get(index);
		}
		subScopeLocations.add(index, location);
		subScopes.add(index, scope);
	}

	/**
	 * Removes the scope from its parent's lists of sub-scopes.
	 * */
	public void remove() {
		if (subScopeLocations != null) {
			subScopeLocations.clear();
			subScopeLocations = null;
		}
		if (subScopes != null) {
			subScopes.clear();
			subScopes = null;
		}

		if (parentScope != null) {
			int index = parentScope.subScopes.indexOf(this);
			parentScope.subScopes.remove(index);
			parentScope.subScopeLocations.remove(index);
		}
	}

	/**
	 * Detects if an assignment can be reached from the actual scope, using
	 * the provided identifier.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param identifier
	 *                the identifier to be use to search for the assignment.
	 * @return true if an assignment with the same identifier exists, or
	 *         false otherwise.
	 * */
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (parentScope != null) {
			return parentScope.hasAssignmentWithId(timestamp, identifier);
		}
		return false;
	}

	/**
	 * Tells if the provided identifier can be a valid module identifier.
	 * <p>
	 * Used to detect, if a definition could hide other definitions coming
	 * from an imported, or the actual module.
	 *
	 * @param identifier
	 *                the identifier to check.
	 *
	 * @return true if the identifier is a valid module identifier.
	 * */
	public boolean isValidModuleId(final Identifier identifier) {
		if (parentScope != null) {
			return parentScope.isValidModuleId(identifier);
		}
		return false;
	}

	/**
	 * Tries to find the assignment that a given reference refers to.
	 * <p>
	 * This function walks the scope hierarchy in a bottom-up order, for
	 * this reason it must be started in the smallest enclosing scope, or
	 * might miss some assignments.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic checking cycle.
	 * @param reference
	 *                the reference used to describe the assignment to be
	 *                found.
	 *
	 * @return the first assignment that can be referred to be the provided
	 *         reference, or null.
	 * */
	public abstract Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference);

	/**
	 * Searches for elements that could complete the provided prefix, and if
	 * found, they are added to the provided proposal collector.
	 * <p>
	 *
	 * @see ProposalCollector
	 * @param propCollector
	 *                the proposal collector holding the prefix and
	 *                collecting the proposals.
	 * */
	public void addProposal(final ProposalCollector propCollector) {
		propCollector.sortTillMarked();
		propCollector.markPosition();
		if (parentScope != null) {
			parentScope.addProposal(propCollector);
		}
	}

	/**
	 * Adds skeleton proposals valid in the actual scope to the proposal
	 * collector.
	 * <p>
	 *
	 * @see ProposalCollector
	 * @param propCollector
	 *                the proposal collector holding the prefix and
	 *                collecting the proposals.
	 * */
	public void addSkeletonProposal(final ProposalCollector propCollector) {
	}

	/**
	 * Adds keyword proposals valid in the actual scope to the proposal
	 * collector.
	 * <p>
	 *
	 * @see ProposalCollector
	 * @param propCollector
	 *                the proposal collector holding the prefix and
	 *                collecting the proposals.
	 * */
	public void addKeywordProposal(final ProposalCollector propCollector) {
	}

	/**
	 * Searches for elements that could be referred to be the provided
	 * reference, and if found, they are added to the declaration collector.
	 * <p>
	 *
	 * @see DeclarationCollector
	 * @param declarationCollector
	 *                the declaration collector folding the reference and
	 *                collecting the declarations.
	 * */
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (parentScope != null) {
			parentScope.addDeclaration(declarationCollector);
		}
	}

	public abstract Assignment getEnclosingAssignment(final int offset);

	@Override
	public abstract void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers);

	public String getSubScopeAndLocInfo() {
		if (subScopeLocations == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < subScopeLocations.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(subScopes.get(i).getClass().getName()).append(": LOC=[").append(subScopeLocations.get(i).getOffset()).append("-")
					.append(subScopeLocations.get(i).getEndOffset()).append("]");
		}
		return sb.toString();
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		Scope s = this;
		boolean first = true;
		while (s != null) {
			if (first) {
				sb.append("scope: ");
			} else {
				sb.append("\n    -> ");
			}
			sb.append("[").append(s.scopeName).append("]*[").append(s.getFullName()).append("]*[").append(s.getClass().getName())
					.append("] SUBSCOPES: ").append(s.getSubScopeAndLocInfo());
			s = s.parentScope;
			first = false;
		}
		return sb.toString();
	}

	public String getParentInfo() {
		StringBuilder sb = new StringBuilder();
		Scope s = this;
		boolean first = true;
		while (s != null) {
			if (!first) {
				sb.append(" -> ");
			}
			sb.append("[").append(s.getClass().getSimpleName()).append("] ").append(s.getFullName());
			s = s.parentScope;
			first = false;
		}
		return sb.toString();
	}

	public boolean isChildOf(Scope s) {
		Scope tempScope = getParentScope();
		while (tempScope != null) {
			if (tempScope == s) {
				return true;
			}

			tempScope = tempScope.getParentScope();
		}
		return false;
	}
}
