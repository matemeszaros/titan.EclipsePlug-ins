/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Repeat_Statement extends Statement {
	private static final String INCORRECTUSAGE = "Repeat statement cannot be used outside alt statements,"
			+ " altsteps or resonse and exception handling part of call operations";

	private static final String STATEMENT_NAME = "repeat";

	private AltGuards myAltGuards;

	@Override
	public Statement_type getType() {
		return Statement_type.S_REPEAT;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		this.myAltGuards = altGuards;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (myAltGuards == null) {
			location.reportSemanticError(INCORRECTUSAGE);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		location.reportSemanticError("Repeat statement is not allowed within an interleave statement");
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}
}
