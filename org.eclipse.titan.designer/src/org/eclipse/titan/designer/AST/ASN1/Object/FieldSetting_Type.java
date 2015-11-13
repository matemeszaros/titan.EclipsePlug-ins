/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ASN1.Object.FieldSpecification.Fieldspecification_types;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent type FieldSettings.
 * 
 * @author Kristof Szabados
 */
public final class FieldSetting_Type extends FieldSetting {

	private static final String TYPESETTINGEXPECTED = "Type setting was expected";

	private IASN1Type type;

	public FieldSetting_Type(final Identifier name, final IASN1Type type) {
		super(name);
		this.type = type;

		if (null != type) {
			type.setFullNameParent(this);
		}
	}

	@Override
	public FieldSetting_Type newInstance() {
		return new FieldSetting_Type(name.newInstance(), type.newInstance());
	}

	@Override
	public IASN1Type getSetting() {
		return type;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final FieldSpecification fieldSpecification) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (!Fieldspecification_types.FS_T.equals(fieldSpecification.getFieldSpecificationType())) {
			location.reportSemanticError(TYPESETTINGEXPECTED);
			type = new Boolean_Type();
			type.setIsErroneous(true);
			type.setFullNameParent(this);
		}

		lastTimeChecked = timestamp;

		type.check(timestamp);

		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		type.checkRecursions(timestamp, referenceChain);
		referenceChain.release();
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != type) {
			type.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != type) {
			type.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		return true;
	}
}
