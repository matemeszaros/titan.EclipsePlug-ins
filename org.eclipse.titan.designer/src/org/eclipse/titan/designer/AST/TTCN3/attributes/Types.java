/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Type;

/**
 * Type list used in port types to store attributes.
 * 
 * @author Kristof Szabados
 * */
public final class Types extends ASTNode {

	private final List<Type> types;

	public Types() {
		types = new ArrayList<Type>();
	}

	public void addType(final Type type) {
		types.add(type);
	}

	public int getNofTypes() {
		return types.size();
	}

	public Type getType(final int index) {
		return types.get(index);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (types != null) {
			for (Type t : types) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
