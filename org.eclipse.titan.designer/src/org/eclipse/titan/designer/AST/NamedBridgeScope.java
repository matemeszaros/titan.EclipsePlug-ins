/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * This is a fake scope used to bridge the difference between two actual ones.
 * It should be used to fill in some space between two defined scope, to add some functionality.
 * 
 * For example to define the name the scope macro should be replaced with
 * (in this case neither the parent scope of the definition nor any of it's children have this data available,
 * And even though the definition itself has this information it is not a scope by default.)
 * 
 * @author Kristof Szabados
 * */
public final class NamedBridgeScope extends Scope {

	@Override
	public Assignment getAssBySRef(final CompilationTimeStamp timestamp, final Reference reference) {
		return getParentScope().getAssBySRef(timestamp, reference);
	}

	@Override
	public Assignment getEnclosingAssignment(final int offset) {
		return null; 
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {	
		//Do nothing
	}

	@Override
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT: return false;
		case ASTVisitor.V_SKIP: return true;
		}
		// no members
		if (v.leave(this)==ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}
