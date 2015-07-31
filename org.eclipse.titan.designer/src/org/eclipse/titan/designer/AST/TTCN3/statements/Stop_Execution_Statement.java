/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Stop_Execution_Statement extends Statement {
	private static final String STATEMENT_NAME = "stop";

	@Override
	public Statement_type getType() {
		return Statement_type.S_STOP_EXECUTION;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public void setMyScope(final Scope scope) {
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		// nothing to be done
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		// no members
		return true;
	}
}
