/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;

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
 * Class to represent a ReferencedObject. It is a DefinedObject or
 * ObjectFromObject.
 * 
 * @author Kristof Szabados
 */
public final class ReferencedObject extends ASN1Object implements IReferenceChainElement {

	private static final String OBJECTEXPECTED = "Object reference expected";
	private static final String CIRCULAROBJECTREFERENCE = "Circular object reference chain: `{0}''";

	private final Reference reference;
	/** cache. */
	private ASN1Object objectReferenced;
	private Object_Definition referencedLast;

	public ReferencedObject(final Reference reference) {
		this.reference = reference;

		if (null != reference) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public ReferencedObject newInstance() {
		return new ReferencedObject(reference.newInstance());
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != reference) {
			reference.setMyScope(scope);
		}
	}

	@Override
	public Location getLocation() {
		if (null != reference && null != reference.getLocation()) {
			return reference.getLocation();
		}

		return location;
	}

	@Override
	public String chainedDescription() {
		return "object reference: " + reference;
	}

	@Override
	public Location getChainLocation() {
		return getLocation();
	}

	public ASN1Object getRefd(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final Assignment assignment = reference.getRefdAssignment(timestamp, true, referenceChain);
			if (null != assignment) {
				final ISetting setting = reference.getRefdSetting(timestamp);
				if (null != setting && !Setting_type.S_ERROR.equals(setting.getSettingtype())) {
					if (Setting_type.S_O.equals(setting.getSettingtype())) {
						objectReferenced = (ASN1Object) setting;
						return objectReferenced;
					}

					location.reportSemanticError(OBJECTEXPECTED);
				}
			}
		}
		objectReferenced = new Object_Definition(null);
		objectReferenced.setMyGovernor(myGovernor);
		return objectReferenced;
	}

	public Object_Definition getRefdLast(final CompilationTimeStamp timestamp) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(CIRCULAROBJECTREFERENCE, true);

		ASN1Object object = this;
		while (object instanceof ReferencedObject && !object.getIsErroneous(timestamp)) {
			object = ((ReferencedObject) object).getRefd(timestamp, referenceChain);
		}

		referenceChain.release();
		return (Object_Definition) object;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null == myGovernor) {
			return;
		}

		final ObjectClass myClass = myGovernor.getRefdLast(timestamp, null);
		final ObjectClass refdClass = getRefdLast(timestamp, null).getMyGovernor().getRefdLast(timestamp, null);
		if (myClass != refdClass) {
			location.reportSemanticError(MessageFormat.format(Referenced_ObjectSet.MISMATCH, myClass.getFullName(),
					refdClass.getFullName()));
			objectReferenced = new Object_Definition(null);
			objectReferenced.setIsErroneous(true);
			objectReferenced.setMyGovernor(myGovernor);
		}
	}

	@Override
	public Object_Definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain temporalReferenceChain;
		if (newChain) {
			temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			temporalReferenceChain = referenceChain;
		}

		referencedLast = getRefd(timestamp, temporalReferenceChain).getRefdLast(timestamp, temporalReferenceChain);

		if (newChain) {
			temporalReferenceChain.release();
		}

		return referencedLast;
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
