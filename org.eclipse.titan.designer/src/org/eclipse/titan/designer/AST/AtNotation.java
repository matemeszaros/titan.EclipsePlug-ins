/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.ASN1.Object.FieldName;

/**
 * Represent an ASN AtNotation
 * 
 * @author Kristof Szabados
 * */
public final class AtNotation extends ASTNode {

	/** number of "."s in compid.compid */
	private int levels;

	private FieldName componentIdentifiers;
	private Identifier objectClassFieldname;

	private IType firstComponent;
	private IType lastComponent;

	public AtNotation(final int levels, final FieldName fieldname) {
		this.levels = levels;
		componentIdentifiers = fieldname;
	}

	public int getLevels() {
		return levels;
	}

	public FieldName getComponentIdentifiers() {
		return componentIdentifiers;
	}

	public void setObjectClassFieldname(final Identifier name) {
		objectClassFieldname = name;
	}

	public void setFirstComponent(final IType type) {
		firstComponent = type;
	}

	public void setLastComponent(final IType type) {
		lastComponent = type;
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		// TODO
		return true;
	}
}
