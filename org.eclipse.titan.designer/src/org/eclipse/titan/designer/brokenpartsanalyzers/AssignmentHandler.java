/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.brokenpartsanalyzers;

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
	private List<String> reasons;

	/**
	 * References within the assignment, that are not able to spread the need for analysis.
	 * */
	private Set<String> nonContagiousReferences;

	/**
	 * References within the assignment, that are able to spread the need for analysis.
	 * */
	private Set<String> contagiousReferences;

	/**
	 * References within the assignment, that are referring to an infected and contagious assignment.
	 * */
	private Set<String> infectedReferences;

	protected AssignmentHandler(Assignment assignment) {
		this.assignment = assignment;
		reasons = new ArrayList<String>();
		nonContagiousReferences = new HashSet<String>();
		contagiousReferences = new HashSet<String>();
		infectedReferences = new HashSet<String>();
		isInfected = false;
		isContagious = false;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public boolean getIsInfected() {
		return isInfected;
	}

	public void setIsInfected(boolean isBroken) {
		this.isInfected = isBroken;
	}

	public boolean getIsContagious() {
		return isContagious;
	}

	public void setIsContagious(boolean isAbleToSpread) {
		this.isContagious = isAbleToSpread;
	}

	public void initStartParts() {
		setIsInfected(true);
		setIsContagious(true);
	}

	public void addReason(String reason) {
		reasons.add(reason);
	}

	public List<String> getReasons() {
		return reasons;
	}

	public Set<String> getNonContagiousReferences() {
		return nonContagiousReferences;
	}

	public boolean doNonContagiousReferencesContains(String name) {
		return nonContagiousReferences.contains(name);
	}

	public void addNonContagiousReferences(Collection<? extends String> references) {
		nonContagiousReferences.addAll(references);
	}

	public void addNonContagiousReference(String reference) {
		nonContagiousReferences.add(reference);
	}

	public Set<String> getContagiousReferences() {
		return contagiousReferences;
	}

	public boolean isContagiousReferencesContains(String name) {
		return contagiousReferences.contains(name);
	}

	public void addContagiousReferences(Collection<? extends String> references) {
		contagiousReferences.addAll(references);
	}

	public void addContagiousReference(String reference) {
		contagiousReferences.add(reference);
	}

	public void removeFromContagiousReferences(String reference) {
		contagiousReferences.remove(reference);
	}

	public Set<String> getInfectedReferences() {
		return infectedReferences;
	}

	public void addInfectedReferences(Collection<? extends String> references) {
		infectedReferences.addAll(references);
	}

	public void addInfectedReference(String reference) {
		infectedReferences.add(reference);
	}

	@Override
	public String toString() {
		return assignment.getIdentifier().getDisplayName();
	}

	public boolean isVisibleIn(Module dependentModule) {
		return assignment.getMyScope().getModuleScope().isVisible(CompilationTimeStamp.getBaseTimestamp(), dependentModule.getIdentifier(), assignment);
	}

	@Override
	public void check(AssignmentHandler definitionHandler) {
		checkIsInfected(definitionHandler);
		checkIsContagious(definitionHandler);
	}

	public void checkIsInfected(AssignmentHandler other) {
		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getIsInfected() && (doNonContagiousReferencesContains(otherName) || isContagiousReferencesContains(otherName))) {
			addInfectedReference(otherName);
			setIsInfected(true);
			addReason("It uses " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}

	public void checkIsContagious(AssignmentHandler other) {
		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getIsInfected() && isContagiousReferencesContains(otherName)) {
			addInfectedReference(otherName);
			setIsContagious(true);
			addReason("It uses contagious reference " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}
}
