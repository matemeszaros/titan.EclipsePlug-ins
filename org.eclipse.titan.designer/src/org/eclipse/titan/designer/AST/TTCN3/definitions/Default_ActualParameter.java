/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
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
	private final ActualParameter defaultActualParameter;

	public Default_ActualParameter(final ActualParameter defaultActualParameter) {
		this.defaultActualParameter = defaultActualParameter;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
	}

	public ActualParameter getActualParameter() {
		return defaultActualParameter;
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (defaultActualParameter != null) {
			referenceChain.markState();
			defaultActualParameter.checkRecursions(timestamp, referenceChain);
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
		if (defaultActualParameter != null) {
			if (!defaultActualParameter.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
