/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.types;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;

/**
 * ExceptionSpecification.
 * 
 * @author Kristof Szabados
 */
public final class ExceptionSpecification extends ASTNode {

	private final ASN1Type type;
	private final Value value;

	public ExceptionSpecification(final ASN1Type type, final Value value) {
		if (null == type) {
			this.type = new ASN1_Integer_Type();
		} else {
			this.type = type;
		}
		this.value = value;

		this.type.setFullNameParent(this);
		this.value.setFullNameParent(this);
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		type.setMyScope(scope);
		value.setMyScope(scope);
	}

	public IASN1Type getType() {
		return type;
	}

	public IValue getValue() {
		return value;
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}
}
