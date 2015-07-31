/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.TTCN3Scope;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * The RunsOnScope class represents the TTCN3 specific 'runs on' scope, which is
 * a link to the contents of the Component type used as 'runs on' component.
 * 
 * @author Kristof Szabados
 * */
public final class RunsOnScope extends TTCN3Scope {
	private Component_Type componentType;
	private ComponentTypeBody componentDefinitions;

	public RunsOnScope(final Component_Type componentType, final Scope parentScope) {
		this.componentType = componentType;
		if (componentType == null) {
			componentDefinitions = null;
		} else {
			componentDefinitions = componentType.getComponentBody();
		}
		setParentScope(parentScope);
	}

	/**
	 * Sets the component type for this runs on scope.
	 * 
	 * @param componentType
	 *                the component type creating this runs on clause.
	 * */
	public void setComponentType(final Component_Type componentType) {
		this.componentType = componentType;
		if (componentType == null) {
			componentDefinitions = null;
		} else {
			componentDefinitions = componentType.getComponentBody();
		}
	}

	@Override
	public boolean hasAssignmentWithId(final CompilationTimeStamp timestamp, final Identifier identifier) {
		if (componentDefinitions != null && componentDefinitions.hasAssignmentWithId(timestamp, identifier)) {
			return true;
		}
		return super.hasAssignmentWithId(timestamp, identifier);
	}

	@Override
	public RunsOnScope getScopeRunsOn() {
		return this;
	}

	/**
	 * @return the component type
	 * */
	public Component_Type getComponentType() {
		return componentType;
	}

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		if (componentDefinitions != null && componentDefinitions.hasLocalAssignmentWithId(reference.getId())) {
			return componentDefinitions.getLocalAssignmentById(reference.getId());
		}
		if (parentScope != null) {
			return parentScope.getAssBySRef(timestamp, reference);
		}

		return null;
	}

	@Override
	public void addProposal(final ProposalCollector propCollector) {
		if (propCollector.getReference().getModuleIdentifier() == null) {
			if (componentDefinitions != null) {
				componentDefinitions.addProposal(propCollector);
			}
		}
		super.addProposal(propCollector);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector) {
		if (declarationCollector.getReference().getModuleIdentifier() == null) {
			if (componentDefinitions != null
					&& componentDefinitions.hasLocalAssignmentWithId(declarationCollector.getReference().getId())) {
				Definition def = componentDefinitions.getLocalAssignmentById(declarationCollector.getReference().getId());
				declarationCollector.addDeclaration(def);
			}
		}
		super.addDeclaration(declarationCollector);
	}

	@Override
	public Assignment getEnclosingAssignment(final int offset) {
		return null;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentType != null) {
			componentType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (componentDefinitions != null) {
			componentDefinitions.findReferences(referenceFinder, foundIdentifiers);
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
		if (componentType != null) {
			if (!componentType.accept(v)) {
				return false;
			}
		}
		if (componentDefinitions != null) {
			if (!componentDefinitions.accept(v)) {
				return false;
			}
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
