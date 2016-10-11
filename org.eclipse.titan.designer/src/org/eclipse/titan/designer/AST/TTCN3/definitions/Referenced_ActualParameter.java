/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a reference as its actual value.
 * 
 * @author Kristof Szabados
 * */
public final class Referenced_ActualParameter extends ActualParameter {

	private final Reference reference;

	public Referenced_ActualParameter(final Reference reference) {
		this.reference = reference;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done here
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (reference != null) {
			if (!reference.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
