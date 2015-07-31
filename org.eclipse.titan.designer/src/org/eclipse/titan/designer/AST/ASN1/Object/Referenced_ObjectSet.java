/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.ASN1.IObjectSet_Element;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ParserFactory;

/**
 * Referenced ObjectSet.
 * 
 * @author Kristof Szabados
 */
public final class Referenced_ObjectSet extends ObjectSet implements IObjectSet_Element, IReferenceChainElement {

	private static final String OBJECTSETEXPECTED = "ObjectSet reference expected";
	public static final String MISMATCH = "ObjectClass mismatch: ObjectSet of class `{0}'' was expected instead of `{1}''";

	private final Reference reference;

	private ObjectSet osReferenced;
	private ObjectSet_definition referencedLast;

	public Referenced_ObjectSet(final Reference reference) {
		this.reference = reference;

		if (null != reference) {
			reference.setFullNameParent(this);
		}
	}

	@Override
	public Referenced_ObjectSet newInstance() {
		return new Referenced_ObjectSet(reference);
	}

	@Override
	public IObjectSet_Element newOseInstance() {
		return newInstance();
	}

	@Override
	public String chainedDescription() {
		return "objectSet reference: " + reference;
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
		if (null != reference) {
			reference.setMyScope(scope);
		}
	}

	public ObjectSet getRefd(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			final Assignment assignment = reference.getRefdAssignment(timestamp, true);
			if (null != assignment) {
				final ISetting setting = reference.getRefdSetting(timestamp);
				if (null != setting && !Setting_type.S_ERROR.equals(setting.getSettingtype())) {
					if (Setting_type.S_OS.equals(setting.getSettingtype())) {
						osReferenced = (ObjectSet) setting;
						return osReferenced;
					}

					location.reportSemanticError(OBJECTSETEXPECTED);
				}
			}
		}
		osReferenced = ParserFactory.createObjectSetDefinition();
		osReferenced.setMyGovernor(getMyGovernor());
		return osReferenced;
	}

	@Override
	public ObjectSet_definition getRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		IReferenceChain temporalReferenceChain;
		if (null == referenceChain) {
			temporalReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		} else {
			temporalReferenceChain = referenceChain;
		}

		referencedLast = getRefd(timestamp, temporalReferenceChain).getRefdLast(timestamp, temporalReferenceChain);

		if (null == referenceChain) {
			temporalReferenceChain.release();
		}

		return referencedLast;
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
			location.reportSemanticError(MessageFormat.format(MISMATCH, myClass.getFullName(), refdClass.getFullName()));

			osReferenced = ParserFactory.createObjectSetDefinition();
			osReferenced.setMyGovernor(myGovernor);
			osReferenced.check(timestamp);
		}
	}

	@Override
	public int getNofObjects() {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null == referencedLast) {
			return 0;
		}

		return referencedLast.getNofObjects();
	}

	@Override
	public ASN1Object getObjectByIndex(final int index) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null == referencedLast) {
			return null;
		}

		return referencedLast.getObjectByIndex(index);
	}

	@Override
	public void accept(final ObjectSetElement_Visitor visitor) {
		visitor.visitObjectSetReferenced(this);

	}

	/*
	 * public void set_fullname_ose(StringChainBuilder fullname) {
	 * setFullName(fullname); }
	 */

	@Override
	public void setMyScopeOse(final Scope scope) {
		setMyScope(scope);
	}

	@Override
	public void accept(final ObjectSetElementVisitor_objectCollector visitor) {
		visitor.visitObjectSet(this, false);
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null != osReferenced) {
			osReferenced.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		if (null != osReferenced) {
			osReferenced.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}
}
