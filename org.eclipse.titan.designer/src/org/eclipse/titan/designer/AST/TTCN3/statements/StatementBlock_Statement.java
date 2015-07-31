/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
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
 * The StatementBlock_Statement class represents TTCN3 statement blocks.
 * <p>
 * This is a TITAN Extension.
 * 
 * @author Kristof Szabados
 * */
public final class StatementBlock_Statement extends Statement {
	private static final String FULLNAMEPART = ".block";
	private static final String STATEMENT_NAME = "statement block";

	/**
	 * the actual statementblock.
	 * <p>
	 * Can not be null.
	 * */
	private final StatementBlock statementblock;

	public StatementBlock_Statement(final StatementBlock statementblock) {
		this.statementblock = statementblock;

		if (statementblock != null) {
			statementblock.setFullNameParent(this);
		}
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_BLOCK;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (statementblock == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		statementblock.setMyScope(scope);
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);

		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (statementblock != null) {
			statementblock.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	public void postCheck() {
		super.postCheck();

		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			if (statementblock != null) {
				if (reparser.envelopsDamage(statementblock.getLocation())) {
					statementblock.updateSyntax(reparser, true);
					reparser.updateLocation(statementblock.getLocation());
					return;
				}
			}

			throw new ReParseException();
		}

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (statementblock == null) {
			return;
		}

		statementblock.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}
}
