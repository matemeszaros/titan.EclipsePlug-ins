/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.editors.DeclarationCollector;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent a TypeFieldSpec.
 * 
 * @author Kristof Szabados
 */
public final class Type_FieldSpecification extends FieldSpecification {

	private final Type definedType;

	public Type_FieldSpecification(final Identifier identifier, final boolean isOptional, final Type definedType) {
		super(identifier, isOptional);
		this.definedType = definedType;

		if (null != definedType) {
			definedType.setFullNameParent(this);
		}
	}

	@Override
	public Fieldspecification_types getFieldSpecificationType() {
		return Fieldspecification_types.FS_T;
	}

	@Override
	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		super.setMyObjectClass(objectClass);
		if (null != definedType) {
			definedType.setMyScope(myObjectClass.getMyScope());
		}
	}

	@Override
	public boolean hasDefault() {
		return null != definedType;
	}

	@Override
	public ISetting getDefault() {
		return definedType;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (null != definedType) {
			definedType.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != definedType) {
			definedType.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != definedType) {
			definedType.addProposal(propCollector, i);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (definedType != null && !definedType.accept(v)) {
			return false;
		}
		return true;
	}
}
