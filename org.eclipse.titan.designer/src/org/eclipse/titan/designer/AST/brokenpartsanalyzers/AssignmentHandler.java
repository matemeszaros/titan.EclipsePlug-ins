/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Peter Olah
 */
public abstract class AssignmentHandler extends ASTVisitor implements IAssignmentChecker {
	// The assignment this definition is representing
	protected Assignment assignment;
	
	/**
	 * An infected assignment is an assignment that needs to be re-analyzed.
	 * 
	 * It is important to note that some assignments can be infected without spreading it.
	 */
	private boolean isInfected;

	/**
	 * A contagious assignment is infected and all assignments referencing it need to be re-analyzed too.
	 * 
	 * It is important to note that some assignments can be infected without spreading it.
	 */
	private boolean isContagious;

	/**
	 * The list of reasons an assignment needs to be re-analyzed.
	 * 
	 * Servers debugging purpose.
	 * */
	private final List<String> reasons;

	/**
	 * References within the assignment, that are not able to spread the need for analysis.
	 * */
	private final Set<String> nonContagiousReferences;

	/**
	 * References within the assignment, that are able to spread the need for analysis.
	 * */
	private final Set<String> contagiousReferences;

	/**
	 * References within the assignment, that are referring to an infected and contagious assignment.
	 * */
	private final Set<String> infectedReferences;

	protected AssignmentHandler(final Assignment assignment) {
		this.assignment = assignment;
		reasons = new ArrayList<String>();
		nonContagiousReferences = new HashSet<String>();
		contagiousReferences = new HashSet<String>();
		infectedReferences = new HashSet<String>();
		isInfected = assignment.isCheckRoot();
		isContagious = false;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public boolean getIsInfected() {
		return isInfected;
	}

	public void setIsInfected(final boolean isBroken) {
		this.isInfected = isBroken;
	}

	public boolean getIsContagious() {
		return isContagious;
	}

	public void setIsContagious(final boolean isAbleToSpread) {
		this.isContagious = isAbleToSpread;
	}

	public void initStartParts() {
		setIsInfected(true);
		setIsContagious(true);
	}

	public void addReason(final String reason) {
		reasons.add(reason);
	}

	public List<String> getReasons() {
		return reasons;
	}

	public Set<String> getNonContagiousReferences() {
		return nonContagiousReferences;
	}

	public boolean doNonContagiousReferencesContains(final String name) {
		return nonContagiousReferences.contains(name);
	}

	public void addNonContagiousReferences(final Collection<? extends String> references) {
		nonContagiousReferences.addAll(references);
	}

	public void addNonContagiousReference(final String reference) {
		nonContagiousReferences.add(reference);
	}

	public Set<String> getContagiousReferences() {
		return contagiousReferences;
	}

	public boolean isContagiousReferencesContains(final String name) {
		return contagiousReferences.contains(name);
	}

	public void addContagiousReferences(final Collection<? extends String> references) {
		contagiousReferences.addAll(references);
	}

	public void addContagiousReference(final String reference) {
		contagiousReferences.add(reference);
	}

	public void removeFromContagiousReferences(final String reference) {
		contagiousReferences.remove(reference);
	}

	public Set<String> getInfectedReferences() {
		return infectedReferences;
	}

	public void addInfectedReferences(final Collection<? extends String> references) {
		infectedReferences.addAll(references);
	}

	public void addInfectedReference(final String reference) {
		infectedReferences.add(reference);
	}

	@Override
	public String toString() {
		return assignment.getIdentifier().getDisplayName();
	}

	public boolean isVisibleIn(final Module dependentModule) {
		return assignment.getMyScope().getModuleScope().isVisible(CompilationTimeStamp.getBaseTimestamp(), dependentModule.getIdentifier(), assignment);
	}

	@Override
	public void check(final AssignmentHandler definitionHandler) {
		checkIsInfected(definitionHandler);
		checkIsContagious(definitionHandler);
	}

	public void checkIsInfected(final AssignmentHandler other) {
		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getIsInfected() && (doNonContagiousReferencesContains(otherName) || isContagiousReferencesContains(otherName))) {
			addInfectedReference(otherName);
			setIsInfected(true);
			addReason("It uses " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}

	public void checkIsContagious(final AssignmentHandler other) {
		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getIsInfected() && isContagiousReferencesContains(otherName)) {
			addInfectedReference(otherName);
			setIsContagious(true);
			addReason("It uses contagious reference " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}
}
