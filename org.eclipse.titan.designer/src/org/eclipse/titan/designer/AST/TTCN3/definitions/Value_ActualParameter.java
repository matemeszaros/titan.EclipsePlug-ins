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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a Value as its actual value.
 * 
 * @author Kristof Szabados
 * */
public final class Value_ActualParameter extends ActualParameter {

	private final IValue value;

	public Value_ActualParameter(final IValue value) {
		this.value = value;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (value != null) {
			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) value).updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		} else if (value != null) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null) {
			if (!value.accept(v)) {
				return false;
			}
		}
		return true;
	}
}
