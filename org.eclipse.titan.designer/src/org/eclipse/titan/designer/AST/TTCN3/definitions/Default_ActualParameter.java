/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has the value of a default actual
 * parameter that was assigned to the formal parameter.
 * 
 * @author Kristof Szabados
 * */
public final class Default_ActualParameter extends ActualParameter {
	// generated value
	private final ActualParameter default_actualParameter;

	public Default_ActualParameter(final ActualParameter default_actualParameter) {
		this.default_actualParameter = default_actualParameter;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
	}

	public ActualParameter getActualParameter() {
		return default_actualParameter;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (default_actualParameter != null) {
			referenceChain.markState();
			default_actualParameter.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (default_actualParameter != null) {
			if (!default_actualParameter.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
