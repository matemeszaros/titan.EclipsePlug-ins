/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ASN1.ASN1Assignment;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;

/**
 * 
 * @author Peter Olah
 */
public final class AssignmentHandlerAFTRerences extends ReferencesProcessor {

	public AssignmentHandlerAFTRerences(final Assignment assignment) {
		super(assignment);
	}

	@Override
	public void check(final AssignmentHandler assignmentHandler) {
		if (assignmentHandler instanceof AssignmentHandlerComponent) {
			checkIsInfected((AssignmentHandlerComponent) assignmentHandler);
		} else {
			checkIsInfected(assignmentHandler);
		}
		checkIsContagious(assignmentHandler);
	}

	private Set<String> computeIsInfected(final Set<String> otherInfectedReferences) {

		Set<String> result = new HashSet<String>();

		Set<String> intersectionWithNonContagiousReferences = new HashSet<String>(getNonContagiousReferences());
		intersectionWithNonContagiousReferences.retainAll(otherInfectedReferences);

		Set<String> intersectionWithContagiousReferences = new HashSet<String>(getContagiousReferences());
		intersectionWithContagiousReferences.retainAll(otherInfectedReferences);

		result.addAll(intersectionWithNonContagiousReferences);
		result.addAll(intersectionWithContagiousReferences);
		return result;
	}

	public void checkIsInfected(final AssignmentHandlerComponent other) {
		if (!other.getIsInfected()) {
			return;
		}

		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getAssignment().getLastTimeChecked() == null && isContagiousReferencesContains(otherName)) {
			setIsInfected(true);
			addInfectedReference(otherName);
			return;
		}
		Set<String> infectedReferences = computeIsInfected(other.getInfectedReferences());
		if (!infectedReferences.isEmpty() || isContagiousReferencesContains(otherName)) {
			setIsInfected(true);
			addReason("It uses " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof ASN1Assignment) {
			ASN1Assignment assignment = (ASN1Assignment) node;
			if(assignment.getAssPard() != null) {
				return V_SKIP;
			}
		}
		if (node instanceof StatementBlock) {
			ReferenceCollector referenceCollector = new ReferenceCollector();
			node.accept(referenceCollector);
			Set<Reference> references = referenceCollector.getReferences();//TODO: broken if reference does not point anywhere
			addNonContagiousReferences(computeReferences(references));
			if(containsErroneousReference(references)) {
				setIsInfected(true);
			}
			return V_SKIP;
		}

		if (node instanceof Reference) {
			addContagiousReference(((Reference) node).getId().getDisplayName());
		}
		return V_CONTINUE;
	}
}
