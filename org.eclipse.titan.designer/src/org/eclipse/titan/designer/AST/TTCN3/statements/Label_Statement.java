/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Label_Statement extends Statement {
	private static final String STATEMENT_NAME = "label";

	private final Identifier identifier;

	/** stores if the label is used or not. */
	private boolean used;

	/** stores statement index of this label in its parent statement block. */
	private int statementIndex;

	public Label_Statement(final Identifier identifier) {
		this.identifier = identifier;
		used = false;
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_LABEL;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		statementIndex = index;
	}

	public int getMyStatementBlockIndex() {
		return statementIndex;
	}

	public Identifier getLabelIdentifier() {
		return identifier;
	}

	public void setUsed(final boolean used) {
		this.used = used;
	}

	public boolean labelIsUsed() {
		return used;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (!GeneralConstants.IGNORE.equals(Goto_statement.banishGOTO)) {
			location.reportConfigurableSemanticProblem(Goto_statement.banishGOTO,
					"Usage of goto and label statements is not recommended as they usually break the structure of the code");
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void checkAllowedInterleave() {
		location.reportSemanticError("Label statement is not allowed within an interleave statment");
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		// label search not supported yet
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		return true;
	}
}
