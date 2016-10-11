/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Interleave_Statement class represents TTCN3 interleave statements.
 * 
 * @see AltGuards
 * @see AltGuard
 * 
 * @author Kristof Szabados
 * */
public final class Interleave_Statement extends Statement {
	private static final String FULLNAMEPART = ".ags";
	private static final String STATEMENT_NAME = "interleave";

	private final AltGuards altGuards;

	public Interleave_Statement(final AltGuards altGuards) {
		this.altGuards = altGuards;

		altGuards.setFullNameParent(this);
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_INTERLEAVE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (altGuards == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	public AltGuards getAltGuards() {
		return altGuards;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		altGuards.setMyScope(scope);
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		altGuards.setMyStatementBlock(statementBlock, index);
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		altGuards.setMyDefinition(definition);
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (altGuards != null) {
			altGuards.check(timestamp);
			altGuards.checkAllowedInterleave();
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void postCheck() {
		if (altGuards != null) {
			altGuards.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			if (altGuards != null) {
				if (reparser.envelopsDamage(altGuards.getLocation())) {
					altGuards.updateSyntax(reparser, true);
					reparser.updateLocation(altGuards.getLocation());
				}
			}
		}

		if (altGuards != null) {
			altGuards.updateSyntax(reparser, false);
			reparser.updateLocation(altGuards.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (altGuards == null) {
			return;
		}

		altGuards.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (altGuards != null && !altGuards.accept(v)) {
			return false;
		}
		return true;
	}
}
