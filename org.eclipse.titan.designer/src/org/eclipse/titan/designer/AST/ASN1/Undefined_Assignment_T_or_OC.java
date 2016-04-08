/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_refd;
import org.eclipse.titan.designer.AST.ISetting.Setting_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * An undefined assignment.
 * <p>
 * Can only be Type or ObjectClass assignment because of the syntax
 * 
 * @author Kristof Szabados
 * */
public final class Undefined_Assignment_T_or_OC extends Undefined_Assignment {

	private final Reference reference;

	public Undefined_Assignment_T_or_OC(final Identifier id, final Ass_pard assPard, final Reference reference) {
		super(id, assPard);
		this.reference = reference;

		if (null != reference) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != reference) {
			reference.setMyScope(scope);
		}
	}

	@Override
	protected void classifyAssignment(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain temporalReferenceChain;
		if (newChain) {
			temporalReferenceChain = ReferenceChain.getInstance(CIRCULARASSIGNMENTCHAIN, true);
		} else {
			temporalReferenceChain = referenceChain;
			temporalReferenceChain.markState();
		}

		realAssignment = null;

		if (temporalReferenceChain.add(this)) {
			reference.setMyScope(rightScope);
			if (identifier.isvalidAsnObjectClassReference()
					&& reference.refersToSettingType(timestamp, Setting_type.S_OC, temporalReferenceChain)) {
				final ObjectClass_refd oc = new ObjectClass_refd(reference);
				oc.setLocation(reference.getLocation());
				realAssignment = new ObjectClass_Assignment(identifier, assPard, oc);
				// assPard = null;
				// asstype = Assignment.A_OC;
			} else if (identifier.isvalidAsnTyperef()
					&& (reference.refersToSettingType(timestamp, Setting_type.S_T, temporalReferenceChain) || reference
							.refersToSettingType(timestamp, Setting_type.S_VS, temporalReferenceChain))) {
				final Referenced_Type type = new Referenced_Type(reference);
				type.setLocation(reference.getLocation());

				realAssignment = new Type_Assignment(identifier, assPard, type);
				// assPard = null;
				// asstype = A_TYPE;
			}
		}

		if (null == realAssignment) {
			location.reportSemanticError(UNRECOGNISABLEASSIGNMENT);
		} else {
			realAssignment.setLocation(location);
			realAssignment.setMyScope(myScope);
			realAssignment.setRightScope(rightScope);
			realAssignment.setFullNameParent(this);
		}

		if (newChain) {
			temporalReferenceChain.release();
		} else {
			temporalReferenceChain.previousState();
		}
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new Undefined_Assignment_T_or_OC(identifier, null, reference.newInstance());
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
