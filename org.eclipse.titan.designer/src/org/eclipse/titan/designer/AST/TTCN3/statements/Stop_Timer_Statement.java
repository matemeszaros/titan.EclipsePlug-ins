/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Stop_Timer_Statement extends Statement {
	private static final String FULLNAMEPART = ".timerreference";
	private static final String STATEMENT_NAME = "stop timer";

	private final Reference timerReference;

	public Stop_Timer_Statement(final Reference timerReference) {
		this.timerReference = timerReference;

		if (timerReference != null) {
			timerReference.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_STOP_TIMER;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (timerReference == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		if (timerReference != null) {
			timerReference.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Start_Timer_Statement.checkTimerReference(timestamp, timerReference);

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (timerReference != null) {
			timerReference.updateSyntax(reparser, false);
			reparser.updateLocation(timerReference.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (timerReference == null) {
			return;
		}

		timerReference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (timerReference != null && !timerReference.accept(v)) {
			return false;
		}
		return true;
	}
}
