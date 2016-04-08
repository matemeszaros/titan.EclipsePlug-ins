/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferenceChainElement;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent a ReferencedObjectClass. It is a DefinedOC or OCFromObject
 * or ValueSetFromObjects.
 * 
 * @author Kristof Szabados
 */
public final class ObjectClass_refd extends ObjectClass implements IReferenceChainElement {

	private static final String OBJECTCLASSEXPECTED = "ObjectClass reference was expected";

	private final Reference reference;
	private ObjectClass_Definition referencedLast;

	public ObjectClass_refd(final Reference reference) {
		this.reference = reference;

		if (null != reference) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public ObjectClass_refd newInstance() {
		final ObjectClass_refd oc = new ObjectClass_refd(reference);
		oc.setLocation(reference.getLocation());

		return oc;
	}

	@Override
	public String chainedDescription() {
		return "object class reference: " + reference;
	}

	@Override
	public Location getChainLocation() {
		if (null != reference && null != reference.getLocation()) {
			return reference.getLocation();
		}

		return null;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		reference.setMyScope(scope);
	}

	protected ObjectClass getRefd(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (null != reference && referenceChain.add(this)) {
			final Assignment assignment = reference.getRefdAssignment(timestamp, true);
			if (null == assignment) {
				return newObjectClassDefinitionInstance();
			}

			final ISetting setting = reference.getRefdSetting(timestamp);
			if (Setting_type.S_ERROR.equals(setting.getSettingtype())) {
				return newObjectClassDefinitionInstance();
			} else if (!Setting_type.S_OC.equals(setting.getSettingtype())) {
				reference.getLocation().reportSemanticError(OBJECTCLASSEXPECTED);
				return newObjectClassDefinitionInstance();
			}

			return (ObjectClass) setting;
		}

		return newObjectClassDefinitionInstance();
	}
	
	private ObjectClass_Definition newObjectClassDefinitionInstance() {
		return new ObjectClass_Definition();
	}

	@Override
	public ObjectClass_Definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain temporalReferenceChain;
		if (newChain) {
			temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			temporalReferenceChain = referenceChain;
			temporalReferenceChain.markState();
		}

		final ObjectClass objectClass = getRefd(timestamp, temporalReferenceChain);
		referencedLast = null;
		if (null != objectClass) {
			referencedLast = objectClass.getRefdLast(timestamp, temporalReferenceChain);
		}

		if (newChain) {
			temporalReferenceChain.release();
		} else {
			temporalReferenceChain.previousState();
		}

		return referencedLast;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		getRefdLast(timestamp, null);
	}

	@Override
	public void checkThisObject(final CompilationTimeStamp timestamp, final ASN1Object object) {
		final ObjectClass_Definition temp = getRefdLast(timestamp, null);
		temp.checkThisObject(timestamp, object);
	}

	@Override
	public FieldSpecifications getFieldSpecifications() {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return referencedLast.getFieldSpecifications();
	}

	@Override
	public ObjectClassSyntax_root getObjectClassSyntax(final CompilationTimeStamp timestamp) {
		check(timestamp);

		final ObjectClass_Definition temp = getRefdLast(timestamp, null);
		return temp.getObjectClassSyntax(timestamp);
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null != referencedLast) {
			referencedLast.addProposal(propCollector, i);
		}
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null != referencedLast) {
			referencedLast.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
