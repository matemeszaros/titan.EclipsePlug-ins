/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeReferenceList;

/**
 * 
 * @author Peter Olah
 */
public final class AssignmentHandlerComponent extends ReferencesProcessor {
	private final Map<Definition, Set<String>> componentDefinitions;

	public AssignmentHandlerComponent(final Assignment assignment) {
		super(assignment);
		componentDefinitions = new HashMap<Definition, Set<String>>();
	}

	public void collectDataFrom(final ComponentTypeBody componentTypeBody) {
		for (final Definition tempDefinition : componentTypeBody.getDefinitions()) {
			ReferenceCollector referenceCollector = new ReferenceCollector();
			tempDefinition.accept(referenceCollector);
			Set<String> references = computeReferences(referenceCollector.getReferences());
			if (containsErroneousReference(referenceCollector.getReferences())) {
				setIsInfected(true);
			}
			componentDefinitions.put(tempDefinition, references);
		}

		processComponentTypeReferenceList(componentTypeBody.getAttributeExtensions());
		processComponentTypeReferenceList(componentTypeBody.getExtensions());
	}

	private void processComponentTypeReferenceList(final ComponentTypeReferenceList componentTypeReferenceList) {
		if (componentTypeReferenceList != null) {
			ReferenceCollector referenceCollector = new ReferenceCollector();
			componentTypeReferenceList.accept(referenceCollector);
			if (containsErroneousReference(referenceCollector.getReferences())) {
				setIsInfected(true);
			}
			addContagiousReferences(computeReferences(referenceCollector.getReferences()));
		}
	}

	@Override
	public Set<String> getNonContagiousReferences() {
		final Set<String> result = super.getNonContagiousReferences();
		
		for (final Map.Entry<Definition, Set<String>> entry : componentDefinitions.entrySet()) {
			final Set<String> references = entry.getValue();
			result.addAll(references);
		}
		
		return result;
	}

	@Override
	public void check(final AssignmentHandler assignmentHandler) {
		checkIsInfected(assignmentHandler);
		checkIsContagious(assignmentHandler);
	}

	@Override
	public void checkIsInfected(final AssignmentHandler other) {
		if (!other.getIsInfected()) {
			return;
		}
		String otherName = other.getAssignment().getIdentifier().getDisplayName();
		if (other.getAssignment().getLastTimeChecked() == null && isContagiousReferencesContains(otherName)) {
			setIsInfected(true);
			addInfectedReference(otherName);
			addReason("It uses " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
			return;
		}
		
		Set<String> infectedFields = computeInfectedFields(otherName);
		if (!infectedFields.isEmpty()) {
			addContagiousReferences(infectedFields);
			addInfectedReferences(infectedFields);
			setIsInfected(true);
			addReason("It uses " + otherName + "@" + other.getAssignment().getMyScope().getModuleScope().getIdentifier().getDisplayName() + " which is infected.");
		}
	}
//TODO ez lehetne boolean ?
	private Set<String> computeInfectedFields(final String definitionName) {
		Set<String> result = new HashSet<String>();
		for (Map.Entry<Definition, Set<String>> entry : componentDefinitions.entrySet()) {
			Set<String> references = entry.getValue();
			for (String referene : references) {
				if (referene.equals(definitionName)) {
					result.add(definitionName);
				}
			}
		}
		return result;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof ComponentTypeBody) {
			collectDataFrom((ComponentTypeBody) node);
			return V_SKIP;
		}
		return V_CONTINUE;
	}
}
