/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectClass_refd;
import org.eclipse.titan.designer.AST.ASN1.Object.ObjectSet_definition;
import org.eclipse.titan.designer.AST.ISetting.Setting_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * An undefined assignment.
 * <p>
 * Can only be ObjectSet or ValueSet assignment because of the syntax
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public abstract class Undefined_Assignment_OS_or_VS extends Undefined_Assignment {

	protected final Reference reference;
	
	public Undefined_Assignment_OS_or_VS(final Identifier id, final Ass_pard ass_pard, final Reference reference) {
		super(id, ass_pard);
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
		final boolean new_chain = null == referenceChain;
		IReferenceChain temporalReferenceChain;
		if (null == referenceChain) {
			temporalReferenceChain = ReferenceChain.getInstance(CIRCULARASSIGNMENTCHAIN, true);
		} else {
			temporalReferenceChain = referenceChain;
			temporalReferenceChain.markState();
		}

		realAssignment = null;

		if (temporalReferenceChain.add(this)) {
			if (null != reference) {
				reference.setMyScope(getMyScope());
				if (!reference.refersToSettingType(timestamp, Setting_type.S_ERROR, temporalReferenceChain)) {
					if (identifier.isvalidAsnObjectSetReference()
							&& reference.refersToSettingType(timestamp, Setting_type.S_OC, temporalReferenceChain)) {
						realAssignment = new ObjectSet_Assignment(identifier, ass_pard, new ObjectClass_refd(reference),
								newObjectSetDefinitionInstance());
						// ass_pard = null;
						// left = null;
						// right = null;
						// asstype = A_OS;
					} else if (identifier.isvalidAsnValueSetReference()
							&& (reference.refersToSettingType(timestamp, Setting_type.S_T, temporalReferenceChain) || reference
									.refersToSettingType(timestamp, Setting_type.S_VS, temporalReferenceChain))) {
						final Referenced_Type type = new Referenced_Type(reference);
						type.setLocation(reference.getLocation());
						realAssignment = newValueSetAssignmentInstance( type );
						// left = null;
						// right = null;
						// asstype = A_VS;
					}
				}
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

		if (new_chain) {
			temporalReferenceChain.release();
		} else {
			temporalReferenceChain.previousState();
		}
	}
	
	protected abstract ObjectSet_definition newObjectSetDefinitionInstance();
	
	protected abstract ValueSet_Assignment newValueSetAssignmentInstance( final Referenced_Type aType );

}
