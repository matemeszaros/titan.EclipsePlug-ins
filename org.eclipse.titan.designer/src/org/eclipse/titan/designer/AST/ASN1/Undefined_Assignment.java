/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * An undefined assignment.
 * 
 * @author Kristof Szabados
 * */
public abstract class Undefined_Assignment extends ASN1Assignment {
	protected static final String CIRCULARASSIGNMENTCHAIN = "Circular assignment chain: {0}";
	protected static final String UNRECOGNISABLEASSIGNMENT = "Cannot recognise this assignment";

	/** the scope of the right side of this assignment. */
	protected Scope rightScope;
	/** the classified assignment. */
	protected ASN1Assignment realAssignment;

	public Undefined_Assignment(final Identifier id, final Ass_pard assPard) {
		super(id, assPard);
	}

	@Override
	public final Assignment_type getAssignmentType() {
		if (null != realAssignment) {
			return realAssignment.getAssignmentType();
		}

		return Assignment_type.A_UNDEF;
	}

	@Override
	public final String getAssignmentName() {
		if (null != realAssignment) {
			return realAssignment.getAssignmentName();
		}

		return "<undefined assignment>";
	}

	public final ASN1Assignment getRealAssignment(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return realAssignment;
	}

	@Override
	public final void setRightScope(final Scope rightScope) {
		if (null != realAssignment) {
			realAssignment.setRightScope(rightScope);
		}
		this.rightScope = rightScope;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != realAssignment) {
			realAssignment.setMyScope(scope);
		}
		rightScope = scope;
	}

	@Override
	public final ISetting getSetting(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null != realAssignment) {
			return realAssignment.getSetting(timestamp);
		}

		return null;
	}

	@Override
	public final IType getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		if (null != realAssignment) {
			return realAssignment.getType(timestamp);
		}

		return null;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
		}
		
	@Override
	public void check(final CompilationTimeStamp timestamp, IReferenceChain refChain) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (null != myScope && null != lastTimeChecked) {
			final Module module = myScope.getModuleScope();
			if (null != module) {
				if (module.getSkippedFromSemanticChecking()) {
					lastTimeChecked = timestamp;
					return;
				}
			}
		}

		lastTimeChecked = timestamp;

		if (null != assPard) {
			assPard.check(timestamp);
			return;
		}

		classifyAssignment(timestamp, null);
		if (null != realAssignment) {
			realAssignment.check(timestamp);
		}
	}

	@Override
	public final boolean isAssignmentType(final CompilationTimeStamp timestamp, final Assignment_type assignmentType,
			final IReferenceChain referenceChain) {
		check(timestamp);

		if (null == realAssignment) {
			return false;
		}

		return getIsErroneous() ? false : realAssignment.isAssignmentType(timestamp, assignmentType, referenceChain);
	}

	/**
	 * Classifies the actually unknown assignment.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * @param referenceChain
	 *                this reference chain is used to detect recursive
	 *                references if needed
	 * */
	protected abstract void classifyAssignment(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	@Override
	public final Object[] getOutlineChildren() {
		if (null == realAssignment) {
			return super.getOutlineChildren();
		}

		return realAssignment.getOutlineChildren();
	}

	@Override
	public final String getOutlineIcon() {
		if (null != realAssignment) {
			return realAssignment.getOutlineIcon();
		}
		return "titan.gif";
	}

	@Override
	public final void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != realAssignment) {
			realAssignment.addDeclaration(declarationCollector, i);
		}
	}

	// TODO: remove when location is fixed
	@Override
	public Location getLikelyLocation() {
		if (realAssignment != null) {
			return realAssignment.getLikelyLocation();
		}

		return super.getLikelyLocation();
	}

	@Override
	public final void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != realAssignment) {
			realAssignment.addProposal(propCollector, i);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (realAssignment != null) {
			realAssignment.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (realAssignment != null && !realAssignment.accept(v)) {
			return false;
		}
		return true;
	}
}
